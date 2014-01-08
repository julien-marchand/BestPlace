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

package entropy.plan.choco.constraint;


import entropy.configuration.Configuration;
import entropy.configuration.VirtualMachine;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.vjob.ExplodedSet;

/**
 * A basic interface to specify a constraint that is focused
 * on a whole reconfiguration problem
 *
 * @author Fabien Hermenier
 */
public interface GlobalConstraint {

    /**
     * Add the constraint to a model of a reconfiguration problem.
     *
     * @param rp the model
     */
    void add(ReconfigurationProblem rp);

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
     * Get all the mis-placed virtual machines in a configuration.
     *
     * @param cfg the configuration
     * @return a set of virtual machines where their position violate the constraint.
     */
    ExplodedSet<VirtualMachine> getMisPlaced(Configuration cfg);
}
