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


import java.io.IOException;

import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.execution.TimedExecutionGraph;
import entropy.plan.parser.TimedReconfigurationPlanSerializer;
import entropy.plan.visualization.PlanVisualizer;

/**
 * An action that migrate a virtual machine between one online node to another.
 *
 * @author Fabien Hermenier
 */
public class Migration extends MovingVirtualMachineAction {

    /**
     * Make a new time-unbounded migration.
     *
     * @param v   the virtual machine to migrate
     * @param src the source node
     * @param dst the destination node
     */
    public Migration(VirtualMachine v, Node src, Node dst) {
        this(v, src, dst, -1, -1);
    }

    /**
     * Make a new time-bounded migration.
     *
     * @param v   the virtual machine to migrate
     * @param src the source node
     * @param dst the destination node
     * @param s   the moment the action starts.
     * @param f   the moment the action finish
     */
    public Migration(VirtualMachine v, Node src, Node dst, int s, int f) {
        super(v, src, dst, s, f);
    }

    /**
     * Test if this action is equals to another object.
     *
     * @param o the object to compare with
     * @return true if ref is an instanceof Migration and if both
     *         instance involve the same virtual machine and the same nodes
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            Migration m = (Migration) o;
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

    @Override
    public String toString() {
        return new StringBuilder("migrate(")
                .append(this.getVirtualMachine().getName())
                .append(",").append(this.getHost().getName())
                .append(",").append(this.getDestination().getName())
                .append(")").toString();
    }

    /**
     * Apply the action by running the virtual machine to its destination node for a specific configuration.
     *
     * @param c the configuration
     */
    @Override
    public boolean apply(Configuration c) {
        return c.setRunOn(getVirtualMachine(), getDestination());
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
     * Hosting node must be online on the source configuration and run the VM. Destination node must
     * be online in the destination configuration and run the VM.
     *
     * @param src the source configuration
     * @param dst the configuration to reach
     * @return true if the action is compatible with the configurations
     */
    @Override
    public boolean isCompatibleWith(Configuration src, Configuration dst) {
        //Source node must be online at startup, destination node must be online at the end
        if (!src.isOnline(getHost()) || !dst.isOnline(getDestination())) {
            return false;
        }
        //VM must be running at the beginning and the end
        if (!src.isRunning(getVirtualMachine()) || !dst.isRunning(getVirtualMachine())) {
            return false;
        }
        //Check the hosting node of the VM.
        if (!src.getLocation(getVirtualMachine()).equals(getHost())
                || dst.getLocation(getVirtualMachine()).equals(getDestination())) {
            return false;
        }
        return true;
    }

    /**
     * Insert the action as an incoming action for the destination node
     * and as an outgoing action for the current hosting node.
     *
     * @param g the graph to use
     * @return true if the action is inserted successfully. False otherwise
     */
    @Override
    public boolean insertIntoGraph(TimedExecutionGraph g) {
        return g.getIncomingsFor(getDestination()).add(this) &&
                g.getOutgoingsFor(getHost()).add(this);
    }

    @Override
    public void injectToVisualizer(PlanVisualizer vis) {
        vis.inject(this);
    }

    @Override
    public void serialize(TimedReconfigurationPlanSerializer s) throws IOException {
        s.serialize(this);
    }
}
