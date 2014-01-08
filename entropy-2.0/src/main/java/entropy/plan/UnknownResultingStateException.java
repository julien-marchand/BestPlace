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

import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;

/**
 * An exception that define an incoherent resulting state for a virtual machine or a node.
 *
 * @author Fabien Hermenier
 */
public class UnknownResultingStateException extends PlanException {

    /**
     * An exception to show a virtual machine does not belong to any possible resulting state.
     *
     * @param vm the virtual machine
     */
    public UnknownResultingStateException(VirtualMachine vm) {
        super("State of virtual machine '" + vm.getName() + "' is not defined");
    }

    /**
     * An exception to show a node does not belong to any possible resulting state.
     *
     * @param n the node
     */
    public UnknownResultingStateException(Node n) {
        super("State of node '" + n.getName() + "' is not defined");
    }
}
