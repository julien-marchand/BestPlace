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
package entropy.vjob;

import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.choco.ReconfigurationProblem;

/**
 * An interface to specify some constraints related to the planification of the action.
 *
 * @author Fabien Hermenier
 */
public interface PlacementConstraint {

    /**
     * Textual representation of the constraint.
     *
     * @return a String
     */
    @Override
	String toString();

    /**
     * Apply the constraint on a planner module.
     */
    void inject(ReconfigurationProblem core);

    /**
     * Check that the constraint is satified in a configuration.
     *
     * @param cfg the configuration to check
     * @return true if the constraint is satistied
     */
    boolean isSatisfied(Configuration cfg);

    /**
     * Get the virtual machines involved in the constraints.
     *
     * @return a set of virtual machines.
     */
    ExplodedSet<VirtualMachine> getAllVirtualMachines();

    /**
     * Get the nodes explicitely involved in the constraints.
     *
     * @return a set of nodes that may be empty
     */
    ExplodedSet<Node> getNodes();

    /**
     * Get all the mis-placed virtual machines in a configuration.
     *
     * @param cfg the configuration
     * @return a set of virtual machines where their position violate the constraint.
     */
    ExplodedSet<VirtualMachine> getMisPlaced(Configuration cfg);

}
