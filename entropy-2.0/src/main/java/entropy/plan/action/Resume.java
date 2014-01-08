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
 * An action to resume a VirtualMachine on an online node. The state of the virtual machine comes to "sleeping" to "running".
 *
 * @author Fabien Hermenier
 */
public class Resume extends MovingVirtualMachineAction {

    /**
     * Make a new resume action.
     *
     * @param vm   the virtual machine to resume
     * @param from the node that currently host the virtual machine image
     * @param to   the node that will host the virtual machine
     */
    public Resume(VirtualMachine vm, Node from, Node to) {
        super(vm, from, to);
    }

    /**
     * Make a new time-bounded resume.
     *
     * @param vm   the virtual machine to resume
     * @param from the source node
     * @param to   the destination node
     * @param st   the moment the action starts.
     * @param end  the moment the action finish
     */
    public Resume(VirtualMachine vm, Node from, Node to, int st, int end) {
        super(vm, from, to, st, end);
    }

    /**
     * Textual representation of the action.
     *
     * @return a String!
     */
    @Override
    public String toString() {
        return new StringBuilder("resume(")
                .append(getVirtualMachine().getName())
                .append(",")
                .append(getDestination().getName()).append(")").toString();
    }

    @Override
    public boolean apply(Configuration c) {
        return c.setRunOn(this.getVirtualMachine(), this.getDestination());
    }

    /**
     * Check the compatibility of the action with a source configuration.
     * The hosting node must be online and hosting the sleeping the virtual machine
     *
     * @param src the configuration to check
     * @return {@code true} if the action is compatible
     */
    @Override
    public boolean isCompatibleWith(Configuration src) {
        return (src.isOnline(getHost()) &&
                src.isSleeping(getVirtualMachine()) &&
                src.getLocation(getVirtualMachine()).equals(getHost()) &&
                src.isOnline(getDestination()));
    }


    /**
     * Check the compatibility of the action with a source and a destination configuration.
     * Destination node must be online and running the VM on the destination configuration
     * while VM must be sleeping on the hosting node on the source configuration
     * The VM must be waiting in the source configuration.
     *
     * @param src the source configuration
     * @param dst the configuration to reach
     * @return true if the action is compatible with the configurations
     */
    @Override
    public boolean isCompatibleWith(Configuration src, Configuration dst) {
        if (!src.isOnline(getHost()) ||
                !src.isSleeping(getVirtualMachine()) ||
                !src.getLocation(getVirtualMachine()).equals(getHost())) {
            return false;
        }
        if (!dst.isOnline(getDestination()) ||
                !dst.isRunning(getVirtualMachine()) ||
                !dst.getLocation(getVirtualMachine()).equals(getDestination())) {
            return false;
        }
        return true;
    }

    /**
     * Test if this action is equals to another object.
     *
     * @param o the object to compare with
     * @return true if ref is an instanceof Resume and if both
     *         instance involve the same virtual machine and the same nodes
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            Resume m = (Resume) o;
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
     * Insert the action as an incoming action.
     *
     * @param g the graph to use
     * @return true if the action is inserted successfully. False otherwise
     */
    @Override
    public boolean insertIntoGraph(TimedExecutionGraph g) {
        return g.getIncomingsFor(getDestination()).add(this);
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
