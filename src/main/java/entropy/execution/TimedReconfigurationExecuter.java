/*
 * Copyright (c) 2010 Ecole des Mines de Nantes.
 *
 *      This file is part of Entropy.
 *
 *      Entropy is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      Entropy is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */
package entropy.execution;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import entropy.execution.driver.Driver;
import entropy.execution.driver.DriverFactory;
import entropy.execution.driver.DriverInstantiationException;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.action.Action;

/**
 * An execution module dedicated to the execution of time bounded Action. All feasible actions are made in parallel.
 * The execution module try to execute new action when a Action is committed.
 *
 * @author Fabien Hermenier
 */
public class TimedReconfigurationExecuter {

    /**
     * Get the dependencies, an action is for
     */
    private Map<Action, List<Dependencies>> revDependencies;

    /**
     * The factory to create drivers.
     */
    private DriverFactory factory;

    /**
     * All the action that don't have been commited.
     */
    private List<Action> uncommited;

    private Logger logger = LoggerFactory.getLogger("Actuator");

    /**
     * Make a new Execution module for a specific plan. Each action will be executed after
     * adaptation by a specific DriverFactory
     *
     * @param f the DriverFactory to transfrom the action into a driver
     */
    public TimedReconfigurationExecuter(DriverFactory f) {
        this.factory = f;
        this.revDependencies = new HashMap<Action, List<Dependencies>>();
        this.uncommited = new LinkedList<Action>();
    }

    /**
     * Start the execution of a plan.
     * The method is blocking and ends once all the actions have been performed
     *
     * @param plan the plan to execute
     */
    public void start(TimedReconfigurationPlan plan) {

        revDependencies.clear();
        uncommited.clear();
        uncommited.addAll(plan.getActions());

        TimedExecutionGraph g = plan.extractExecutionGraph();

        //Set the reverse dependencies map
        for (Dependencies dep : g.extractDependencies()) {
            for (Action a : dep.getUnsatisfiedDependencies()) {
                if (!revDependencies.containsKey(a)) {
                    revDependencies.put(a, new LinkedList<Dependencies>());
                }
                revDependencies.get(a).add(dep);

            }
        }

        //Start the feasible actions
        // ie, actions with a start moment equals to 0.
        for (Action a : plan) {
            if (a.getStartMoment() == 0) {
                instantiateAndStart(a);
            }
        }

        //Check each second if all the actions has been performed
        while (uncommited.size() != 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Associate an action with a driver and start its execution
     * using a {@link Executor}.
     *
     * @param a the action to instantiate and start
     */
    private void instantiateAndStart(Action a) {
        try {
            Driver drv = factory.transform(a);
            new Executor(drv, this).start();
        } catch (DriverInstantiationException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * Commit the execution of an action.
     * If the execution of the action succeed. Unlocked actions
     * are started.
     *
     * @param e the executor that commit the action
     */
    public void commit(Executor e) {
        Action a = e.getDriver().getAction();
        uncommited.remove(a);
        if (e.hasSuceeded()) {
            if (revDependencies.containsKey(a)) {
                //Get the associated depenencies and update it
                for (Dependencies dep : revDependencies.get(a)) {
                    dep.removeDependency(a);
                    //Launch new feasible actions.
                    if (dep.isFeasible()) {
                        instantiateAndStart(dep.getAction());
                    }
                }
            }
        } else {
            logger.error("Reconfiguration aborded: " +
                    e.getException().getMessage(), e.getException());
        }
    }

    /**
     * Get all the uncommited actions.
     * A reconfiguration process is terminated once
     * all the actions on a plan has been commited
     *
     * @return a list that may be empty
     */
    public List<Action> getUncommitedActions() {
        return this.uncommited;
    }
}
