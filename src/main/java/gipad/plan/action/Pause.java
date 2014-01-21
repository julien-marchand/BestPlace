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
 * Pause a running virtual machine.
 * <p/>
 * TODO: Implement
 *
 * @author Fabien Hermenier
 */
public class Pause extends VirtualMachineAction {

    /**
     * Make a new time-bounded action.
     *
     * @param v the virtual machine to pause
     * @param n the hosting node
     * @param s the moment to start the action
     * @param f the moment the action ends
     */
    public Pause(VirtualMachine v, Node n, int s, int f) {
        super(v, n, s, f);
    }

    /**
     * Make a new time-unbounded action.
     *
     * @param v the virtual machine to pause
     * @param n the hosting node
     */
    public Pause(VirtualMachine v, Node n) {
        this(v, n, 0, 0);
    }

    /**
     * Check the compatibility of the action with a source configuration.
     * Not implemented
     *
     * @param src the configuration to check
     * @return {@code true} if the action is compatible
     */
    @Override
    public boolean isCompatibleWith(Configuration src) {
        throw new UnsupportedOperationException();
    }


    /**
     * Insert the action as an outgoing action.
     *
     * @param g the graph to use
     * @return true if the insertion succeed.
     */
    @Override
    public boolean insertIntoGraph(SequencedExecutionGraph g) {
        return g.getOutgoingsFor(getHost()).add(this);

    }

    @Override
    public boolean apply(Configuration c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Check the compatibility of the action with a source and a destination configuration.
     * Not implemented
     *
     * @param src the source configuration
     * @param dst the configuration to reach
     * @return true if the action is compatible with the configurations
     */
    @Override
    public boolean isCompatibleWith(Configuration src, Configuration dst) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return new StringBuilder("pause(").append(getVirtualMachine().name()).append(")").toString();
    }

//    @Override
//    public void injectToVisualizer(PlanVisualizer vis) {
//        vis.inject(this);
//    }

    /**
     * Test if this action is equals to another object.
     *
     * @param o the object to compare with
     * @return true if ref is an instanceof UnPause and if both
     *         instance involve the same virtual machine and the same nodes
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            Pause m = (Pause) o;
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
//    public void serialize(TimedReconfigurationPlanSerializer s) throws IOException {
//        s.serialize(this);
//    }
}
