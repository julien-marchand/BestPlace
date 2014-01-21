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
package entropy.plan;


import java.util.List;

import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.durationEvaluator.DurationEvaluator;
import entropy.vjob.VJob;

/**
 * A plan module that can be customized using constraints on VJobs.
 *
 * @author Fabien Hermenier
 */
public abstract class CustomizablePlannerModule implements Plan {

    /**
     * The duration evaluator.
     */
    private DurationEvaluator durEval;

    /**
     * The timeout to limit the solving process.
     */
    private int timeout;

    /**
     * Make a new module.
     *
     * @param eval the evaluator for each action
     */
    public CustomizablePlannerModule(DurationEvaluator eval) {
        this.durEval = eval;
    }

    /**
     * Get the duration evaluator used to estimate the duration of the actions.
     *
     * @return the evaluator
     */
    public final DurationEvaluator getDurationEvaluator() {
        return this.durEval;
    }

    /**
     * Compute a new DefaultTimedReconfigurationPlan that satisfy all the constraints applied to the model.
     *
     * @param src   The source configuration. It must be viable.
     * @param run   The set of virtual machines that must be running at the end of the process
     * @param wait  The set of virtual machines that must be waiting at the end of the process
     * @param sleep The set of virtual machines that must be sleeping at the end of the process
     * @param stop  The set of virtual machines that must be terminated at the end of the process
     * @param on    The set of nodes that must be online at the end of the process
     * @param off   The set of nodes that must be offline at the end of the process
     * @param queue the vjobs
     * @return a plan if it exists.
     * @throws PlanException if an error occurred while planing the action to reach the state of the nodes and the virtual machines
     */
    @Override
	public abstract TimedReconfigurationPlan compute(Configuration src,
                                                     ManagedElementSet<VirtualMachine> run,
                                                     ManagedElementSet<VirtualMachine> wait,
                                                     ManagedElementSet<VirtualMachine> sleep,
                                                     ManagedElementSet<VirtualMachine> stop,
                                                     ManagedElementSet<Node> on,
                                                     ManagedElementSet<Node> off,
                                                     List<VJob> queue) throws PlanException;

    /**
     * Set the timelimit to solve the problem.
     *
     * @param seconds the time in second
     */
    public final void setTimeLimit(int seconds) {
        this.timeout = seconds;
    }

    /**
     * Get the timelimit to solve the problem.
     *
     * @return the time in seconds
     */
    public final int getTimeLimit() {
        return this.timeout;
    }

    /**
     * Get statistics about the computed solutions.
     * Solutions are sorted in an ascending duration.
     *
     * @return a list of statistics that may be empty
     */
    public abstract List<SolutionStatistics> getSolutionsStatistics();

    /**
     * Get statistics about the solving process.
     *
     * @return some statistics
     */
    public abstract SolvingStatistics getSolvingStatistics();
}
