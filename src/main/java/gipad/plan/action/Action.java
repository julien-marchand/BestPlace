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
package gipad.plan.action;


//import java.io.IOException;

import gipad.configuration.configuration.Configuration;
import gipad.execution.SequencedExecutionGraph;
//import entropy.plan.parser.TimedReconfigurationPlanSerializer;
//import entropy.plan.visualization.PlanVisualizer;

/**
 * Abstract definition of an action that reconfigure a architecture. An action manipulate either virtual machines or nodes.
 * Regarding to the duration of its execution, the action can be unbounded or bounded.
 *
 * @author Fabien Hermenier
 */
public abstract class Action {

    /**
     * The moment  the execution starts.
     */
    private int startMoment = -1;

    /**
     * The moment the execution is finished.
     */
    private int finishMoment = -1;

    /**
     * Make a non time-bounded action.
     */
    public Action() {

    }

    /**
     * Make a new time-bounded action.
     *
     * @param s The moment the execution starts.
     * @param f the moment the execution stops.
     */
    public Action(int s, int f) {
        this.setStartMoment(s);
        this.setFinishMoment(f);
    }

    /**
     * Set the moment the execution starts.
     *
     * @param s the moment the execution start.
     */
    public final void setStartMoment(int s) {
        this.startMoment = s;
    }

    /**
     * Set the moment the execution stops.
     *
     * @param f the moment the execution stop.
     */
    public final void setFinishMoment(int f) {
        this.finishMoment = f;
    }

    /**
     * Get the moment the action starts.
     *
     * @return the moment the execution start
     */
    public final int getStartMoment() {
        return this.startMoment;
    }

    /**
     * Get the moment the action stops.
     *
     * @return the moment the execution stops.
     */
    public final int getFinishMoment() {
        return this.finishMoment;
    }

    /**
     * Check if the action can by applied to the source configuration.
     *
     * @param src the configuration to check
     * @return {@code true} is the action is compatible
     */
    public abstract boolean isCompatibleWith(Configuration src);

    /**
     * Check if the action allow to reach a part of a destination configuration
     * from a source configuration.
     *
     * @param src the source configuration
     * @param dst the configuration to reach
     * @return {@code true} if the action is compatible with the configuration
     */
    public abstract boolean isCompatibleWith(Configuration src, Configuration dst);

    /**
     * Apply the action to a configuration.
     *
     * @param c the configuration to modify
     * @return {@code true} if the action was applied
     */
    public abstract boolean apply(Configuration c);

    /**
     * Insert this component into a graph.
     *
     * @param graph the graph to insert the component into
     * @return {@code true} if the component was inserted. False otherwise
     */
    public abstract boolean insertIntoGraph(SequencedExecutionGraph graph);

    /**
     * Get a textual representation of the action
     *
     * @return a string
     */
    @Override
	public abstract String toString();

//    /**
//     * Inject the action into a visualizer.
//     *
//     * @param vis the visualizer to inject into
//     */
//    public abstract void injectToVisualizer(PlanVisualizer vis);
//
//
//    /**
//     * Serialize the action.
//     *
//     * @param s the serializer to use
//     * @throws IOException if an error occurred while serializing the action.
//     */
//    public abstract void serialize(TimedReconfigurationPlanSerializer s) throws IOException;
}
