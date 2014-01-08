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
 * An abstract action to describe a virtual machine that move from one online node to one another.
 * The state of the virtual machine may change during the action.
 *
 * @author Fabien Hermenier
 */
public abstract class MovingVirtualMachineAction extends VirtualMachineAction {

    /**
     * The destination node.
     */
    private Node dest;

    /**
     * Make a new time-unbounded action.
     *
     * @param v   the virtual machine to move
     * @param src the source node
     * @param dst the destination node
     */
    public MovingVirtualMachineAction(VirtualMachine v, Node src, Node dst) {
        this(v, src, dst, 0, 0);
    }

    /**
     * Make a new time-bounded action.
     *
     * @param v   the virtual machine to move
     * @param src the source node
     * @param dst the destination node
     * @param s   the moment the execution of the action starts.
     * @param f   the moment the execution finish.
     */
    public MovingVirtualMachineAction(VirtualMachine v, Node src, Node dst, int s, int f) {
        super(v, src, s, f);
        this.dest = dst;

    }

    /**
     * Get the destination node.
     *
     * @return a node
     */
    public Node getDestination() {
        return this.dest;
    }
}
