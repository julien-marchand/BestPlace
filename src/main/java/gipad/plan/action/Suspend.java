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


import gipad.configuration.configuration.Configuration;
import org.discovery.DiscoveryModel.model.*;
import gipad.execution.*;

/**
 * An action that suspend a running virtual machine to disk.
 *
 * @author Fabien Hermenier
 */
public class Suspend extends MovingVirtualMachineAction {


    /**
     * Make a new time-unbounded suspend action.
     *
     * @param vm   the virtual machine to suspend
     * @param n    The node that host the virtual machine
     * @param dest the destination node.
     */
    public Suspend(VirtualMachine vm, Node n, Node dest) {
        this(vm, n, dest, 0, 0);
    }

    /**
     * Make a new time-bounded suspend action.
     *
     * @param vm   the virtual machine to suspend
     * @param n    The node that host the virtual machine
     * @param dest the destination node.
     * @param s    the moment the action starts.
     * @param f    the moment the action finish
     */
    public Suspend(VirtualMachine vm, Node n, Node dest, int s, int f) {
        super(vm, n, dest, s, f);
    }

    /**
     * Get a textual representation of the action.
     *
     * @return a String!
     */
    @Override
    public String toString() {
        return new StringBuilder("suspend(")
                .append(getVirtualMachine().name())
                .append(",")
                .append(getDestination().name()).append(")").toString();

    }

    /**
     * Apply the action by putting the virtual machine into sleep on its destination node for a specified configuration.
     *
     * @param c the configuration
     */
    @Override
    public boolean apply(Configuration c) {
        return c.setSleepOn(this.getVirtualMachine(), this.getDestination());
    }

    /**
     * Check the compatibility of the action with a source configuration.
     * The hosting node must be online and running the virtual machine
     *
     * @param src the configuration to check
     * @return {@code true} if the action is compatible
     */
    @Override
    public boolean isCompatibleWith(Configuration src) {
        return (src.isOnline(getHost()) &&
                src.isRunning(getVirtualMachine()) &&
                src.getLocation(getVirtualMachine()).equals(getHost()) &&
                src.isOnline(getDestination()));
    }


    /**
     * Check the compatibility of the action with a source and a destination configuration.
     * The source node must be online and running the VM while the destination node must be online and the VM should be sleep on it
     *
     * @param src the source configuration
     * @param dst the configuration to reach
     * @return true if the action is compatible with the configurations
     */
    @Override
    public boolean isCompatibleWith(Configuration src, Configuration dst) {
        if (!src.isOnline(getHost()) ||
                !src.isRunning(getVirtualMachine()) ||
                !src.getLocation(getVirtualMachine()).equals(getHost())) {
            return false;
        }
        return (!dst.isOnline(getDestination()) ||
                !dst.isSleeping(getVirtualMachine()) ||
                !dst.getLocation(getVirtualMachine()).equals(getDestination()));
    }

    /**
     * Test if this action is equals to another object.
     *
     * @param o the object to compare with
     * @return true if ref is an instanceof Suspend and if both
     *         instance involve the same virtual machine and the same nodes
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            Suspend m = (Suspend) o;
            return this.getVirtualMachine().equals(m.getVirtualMachine())
                    && this.getHost().equals(m.getHost())
                    && this.getDestination().equals(m.getDestination());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getVirtualMachine().hashCode() * 31
                + this.getHost().hashCode() * 31
                + this.getDestination().hashCode() * 31;
    }

    /**
     * Insert the action as an outgoing action.
     *
     * @param g the graph to use
     * @return true if the action is inserted successfully. False otherwise
     */
    @Override
    public boolean insertIntoGraph(SequencedExecutionGraph g) {
        return g.getOutgoingsFor(getDestination()).add(this);
    }

//    @Override
//    public void injectToVisualizer(PlanVisualizer vis) {
//        vis.inject(this);
//    }
//
//    @Override
//    public void serialize(TimedReconfigurationPlanSerializer s) throws IOException {
//        s.serialize(this);
//    }

}
