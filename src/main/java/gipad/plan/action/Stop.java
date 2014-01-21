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
 * An action to stop a virtual machine running on an online node.
 *
 * @author Fabien Hermenier
 */
public class Stop extends VirtualMachineAction {

    /**
     * Make a new time-unbounded action.
     *
     * @param v the virtual machine to stop
     * @param n the hosting node
     */
    public Stop(VirtualMachine v, Node n) {
        this(v, n, 0, 0);
    }

    /**
     * Make a new time-bounded action.
     *
     * @param v the virtual machine to stop
     * @param n the hosting node
     * @param s the moment the action start.
     * @param f the moment the action finish
     */
    public Stop(VirtualMachine v, Node n, int s, int f) {
        super(v, n, s, f);
    }

    /**
     * Apply the action by removing the virtual machine from a specified configuration.
     *
     * @param c the configuration
     */
    @Override
    public boolean apply(Configuration c) {
        c.remove(this.getVirtualMachine());
        return true;
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
                src.getLocation(getVirtualMachine()).equals(getHost()));
    }

    /**
     * Check the compatibility of the action with a source and a destination configuration.
     * The VM must be initially running and not be a part of the destination configuration
     *
     * @param src the source configuration
     * @param dst the configuration to reach
     * @return true if the action is compatible with the configurations
     */
    @Override
    public boolean isCompatibleWith(Configuration src, Configuration dst) {
        return (!src.isRunning(getVirtualMachine()) ||
                dst.isRunning(getVirtualMachine()) ||
                dst.isWaiting(getVirtualMachine()) ||
                dst.isSleeping(getVirtualMachine()));
    }

    /**
     * Textual representation of the action.
     *
     * @return a String
     */
    @Override
	public String toString() {
        return new StringBuilder("stop(")
                .append(getVirtualMachine().name())
                .append(",")
                .append(getHost().name()).append(")").toString();
    }


    /**
     * Insert the action as an outgoing action.
     *
     * @param g the graph to use
     * @return true if the action is inserted.
     */
    @Override
    public boolean insertIntoGraph(SequencedExecutionGraph g) {
        return g.getOutgoingsFor(getHost()).add(this);
    }

    /**
     * Test if this action is equals to another object.
     *
     * @param o the object to compare with
     * @return true if ref is an instanceof Stop and if both
     *         instance involve the same virtual machine and the same nodes
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            Stop m = (Stop) o;
            return this.getVirtualMachine().equals(m.getVirtualMachine())
                    && this.getHost().equals(m.getHost());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getVirtualMachine().hashCode() * 31
                + this.getHost().hashCode() * 31;
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
