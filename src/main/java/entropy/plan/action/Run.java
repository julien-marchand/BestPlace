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
 * An action that demand to run a virtual machine on an online node. The virtual machine comes to the state "running" for "waiting".
 *
 * @author Fabien Hermenier
 */
public class Run extends VirtualMachineAction {

    /**
     * Make a new start action.
     * VirtualMachine must be in state waiting.
     *
     * @param vm The virtual machine to start.
     * @param to The destination node.
     */
    public Run(VirtualMachine vm, Node to) {
        super(vm, to);
    }

    /**
     * Make a new time-bounded run.
     *
     * @param vm  the virtual machine to run
     * @param to  the destination node
     * @param st  the moment the action starts.
     * @param end the moment the action finish
     */
    public Run(VirtualMachine vm, Node to, int st, int end) {
        super(vm, to, st, end);
    }

    /**
     * Textual representation of the action.
     *
     * @return a String
     */
    @Override
    public String toString() {
        return new StringBuilder("run(")
                .append(getVirtualMachine().getName())
                .append(",")
                .append(getHost().getName()).append(")").toString();
    }

    @Override
    public boolean apply(Configuration c) {
        return c.setRunOn(this.getVirtualMachine(), this.getHost());
    }

    /**
     * Check the compatibility of the action with a source configuration.
     * The hosting node must be online and the virtual machine must be waiting
     *
     * @param src the configuration to check
     * @return {@code true} if the action is compatible
     */
    @Override
    public boolean isCompatibleWith(Configuration src) {
        return (src.isOnline(getHost()) &&
                src.isWaiting(getVirtualMachine()));
    }

    /**
     * Check the compatibility of the action with a source and a destination configuration.
     * Hosting must be online on the destination configuration and run the VM.
     * The VM must be waiting in the source configuration.
     *
     * @param src the source configuration
     * @param dst the configuration to reach
     * @return true if the action is compatible with the configurations
     */
    @Override
    public boolean isCompatibleWith(Configuration src, Configuration dst) {
        return (!src.isWaiting(getVirtualMachine())
                || !dst.isOnline(getHost())
                || !dst.isRunning(getVirtualMachine())
                || !dst.getLocation(getVirtualMachine()).equals(getHost()));
    }

    /**
     * Insert the action as an incoming action.
     *
     * @param g the graph
     * @return true if the action is inserted successfully
     */
    @Override
    public boolean insertIntoGraph(TimedExecutionGraph g) {
        return g.getIncomingsFor(getHost()).add(this);
    }

    /**
     * Test if this action is equals to another object.
     *
     * @param o the object to compare with
     * @return true if ref is an instanceof Run and if both
     *         instance involve the same virtual machine and the same nodes
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            Run m = (Run) o;
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

    @Override
    public void injectToVisualizer(PlanVisualizer vis) {
        vis.inject(this);
    }

    @Override
    public void serialize(TimedReconfigurationPlanSerializer s) throws IOException {
        s.serialize(this);
    }
}
