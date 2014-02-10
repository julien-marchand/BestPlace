/*
 * Copyright (c) Fabien Hermenier
 *
 * This file is part of Entropy.
 *
 * Entropy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Entropy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package gipad.plan;


import java.util.Iterator;

import gipad.configuration.configuration.Configuration;
import gipad.configuration.*;
import gipad.execution.*;
import gipad.plan.action.*;
import gipad.tools.ManagedElementList;


/**
 * Interface to specify a plan composed of time-bounded actions.
 *
 * @author Fabien Hermenier
 */
public interface SequencedReconfigurationPlan extends Iterable<Action> {

    /**
     * Get the initial configuration.
     *
     * @return the source configuration
     */
    Configuration getSource();

    /**
     * Get the configuration once all the actions are applied.
     *
     * @return the destination configuration
     */
    Configuration getDestination();

    /**
     * Add a new action to the plan.
     * At most one action per element of the initial configuration.
     *
     * @param ta the action to add
     * @return true if the action is added
     */
    boolean add(Action ta);

    /**
     * A String representation of a plan.
     * Indicated a timeline with the start moment and the end moment of each action
     *
     * @return the representation
     */
    @Override
    String toString();

    @Override
    Iterator<Action> iterator();

    /**
     * Get the number of action in the plan.
     *
     * @return a positive integer
     */
    int size();

    /**
     * Transform the plan into an execution graph.
     *
     * @return an execution graph
     */
    SequencedExecutionGraph extractExecutionGraph();

    /**
     * Return the theorical duration of a reconfiguration plan.
     *
     * @return the finish moment of the last action to execute
     */
    int getDuration();

    /**
     * Get all the actions to perform.
     *
     * @return a list of actions. May be empty
     */
    ManagedElementList<Action> getActions();
}
