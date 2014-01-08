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
package entropy.plan.action;


import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;

/**
 * An abstract action related to a virtual machine hosted on an online node.
 *
 * @author Fabien Hermenier
 */
public abstract class VirtualMachineAction extends Action {

    /**
     * The VirtualMachine involved in the action.
     */
    private VirtualMachine vm;

    /**
     * The node that host the virtual machine.
     */
    private Node host;

    /**
     * Make a new time-unbounded action on a specific virtual machine.
     *
     * @param v the virtual machine involved in the action
     * @param n The host of the virtual machine
     */
    public VirtualMachineAction(VirtualMachine v, Node n) {
        this(v, n, 0, 0);
    }

    /**
     * Make a new time bounded action on a virtual machine.
     *
     * @param v the virtual machine
     * @param n the node that host the virtual machine
     * @param s the moment the execution starts
     * @param f the moment the execution finish
     */
    public VirtualMachineAction(VirtualMachine v, Node n, int s, int f) {
        super(s, f);
        this.vm = v;
        this.host = n;
    }

    /**
     * Get the VirtualMachine involved in the action.
     *
     * @return the VirtualMachine
     */
    public VirtualMachine getVirtualMachine() {
        return this.vm;
    }

    /**
     * Get the node that host the virtual machine.
     *
     * @return a Node
     */
    public Node getHost() {
        return this.host;
    }
}
