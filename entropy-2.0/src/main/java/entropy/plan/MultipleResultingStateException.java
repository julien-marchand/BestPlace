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


import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;

/**
 * An exception to signal a virtual machine or a node is in several resulting state simultaneously.
 *
 * @author Fabien Hermenier
 */
public class MultipleResultingStateException extends PlanException {

    /**
     * An exception to show a virtual machine belong to several states.
     *
     * @param vm         the virtual machines
     * @param runnings   the virtual machines in the state running
     * @param sleepings  the virtual machines in the state sleeping
     * @param waitings   the virtual machines in the state waiting
     * @param terminated the virtual machines in the state terminated
     */
    public MultipleResultingStateException(VirtualMachine vm,
                                           ManagedElementSet<VirtualMachine> runnings,
                                           ManagedElementSet<VirtualMachine> sleepings,
                                           ManagedElementSet<VirtualMachine> waitings,
                                           ManagedElementSet<VirtualMachine> terminated) {
        super(vm.getName() + " can not be in several states: " +
                " runnings= " + runnings.contains(vm) +
                ", sleepings= " + sleepings.contains(vm) +
                ", waitings= " + waitings.contains(vm) +
                ", terminated= " + terminated.contains(vm));
    }

    /**
     * An exception to show a node belong to several states.
     *
     * @param n        the node
     * @param onlines  the nodes in the state online
     * @param offlines the nodes in the state offline
     */
    public MultipleResultingStateException(Node n,
                                           ManagedElementSet<Node> onlines,
                                           ManagedElementSet<Node> offlines) {
        super(n.getName() + " can not be in severals states: " +
                "onlines= " + onlines.contains(n) +
                ", offlines= " + offlines.contains(n)
        );
    }
}
