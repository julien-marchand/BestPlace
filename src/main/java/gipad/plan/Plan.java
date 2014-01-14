/*
 * Copyright (c) Fabien Hermenier
 *
 * This file is part of Entropy.
 *
 * Entropy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Entropy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package gipad.plan;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.vjob.VJob;

/**
 * @author Fabien Hermenier
 */
public interface Plan {

    Logger logger = LoggerFactory.getLogger("Plan");

    /**
     * Compute a new DefaultTimedReconfigurationPlan that satisfy all the constraints applied to the model.
     *
     * @param src   The source configuration. It must be viable.
     * @param run   The set of virtual machines that must be running at the end of the process
     * @param wait  The set of virtual machines that must be waiting at the end of the process
     * @param sleep The set of virtual machines that must be sleeping at the end of the process
     * @param stop  The set of virtual machines that must be terminated at the end of the process
     * @param on    The set of nodes that must be online at the end of the process
     * @param off   The set of nodes that must be offline at the end of the process
     * @param queue the vjobs
     * @return a plan if it exists.
     * @throws PlanException if an error occurred while planing the action to reach the state of the nodes and the virtual machines
     */
    TimedReconfigurationPlan compute(Configuration src,
                                     ManagedElementSet<VirtualMachine> run,
                                     ManagedElementSet<VirtualMachine> wait,
                                     ManagedElementSet<VirtualMachine> sleep,
                                     ManagedElementSet<VirtualMachine> stop,
                                     ManagedElementSet<Node> on,
                                     ManagedElementSet<Node> off,
                                     List<VJob> queue) throws PlanException;
}
