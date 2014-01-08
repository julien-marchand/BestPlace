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

package entropy.decision;

import java.util.Collections;

import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.NodeComparator;
import entropy.configuration.SimpleConfiguration;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.configuration.VirtualMachineComparator;

/**
 * Implementation of the FirstFitPlacement algorithm. Each VirtualMachine is
 * assigned to the first node with a sufficient amount of free resources. The
 * VirtualMachines and the Nodes may be sorted to specify an initial order of
 * Nodes and for assigning the VirtualMachine
 *
 * @author Fabien Hermenier
 */
public class FirstFitPlacement extends DecisionModule {

    /**
     * VirtualMachine comparator for specifying an order to assign VMs.
     */
    private VirtualMachineComparator vmComp = null;

    /**
     * Node comparator for selecting hosting nodes.
     */
    private NodeComparator nodeComp = null;

    /**
     * The current nodes (used to update free capacities).
     */
    private ManagedElementSet<Node> tmpNodes;

    /**
     * New Simple FirstFitPlacement that consider specific observations.
     */
    public FirstFitPlacement() {
        this(null, null);
    }

    /**
     * New FirstFitPlacement that use specific sorter for the list of virtual machines and nodes.
     *
     * @param vmCmp   The comparator to use to sort the virtual machines. May be null.
     * @param nodeCmp The comparator to use to sort the nodes. May be null.
     */
    public FirstFitPlacement(VirtualMachineComparator vmCmp, NodeComparator nodeCmp) {
        this.vmComp = vmCmp;
        this.nodeComp = nodeCmp;
        this.tmpNodes = new SimpleManagedElementSet<Node>();
    }

    /**
     * Check if a virtual machine can fit on a node.
     *
     * @param vm   The virtual machine to assign
     * @param node The potential host
     * @return true if the CPU capacity of the node is >= the consumption of the virtual machine. Same for the memory requirement
     *         and consumption.
     */
    private static boolean canFit(VirtualMachine vm, Node node) {
        return (vm.getCPUConsumption() <= node.getCPUCapacity())
                && (vm.getMemoryConsumption() <= node.getMemoryCapacity());
    }

    /**
     * Assign a virtual machine to a node.
     * The virtual machine must fit to obtain a viable configuration
     *
     * @param conf the configuration
     * @param node the hoster.
     * @param vm   the hostee.
     */
    private void assignVirtualMachine(Configuration conf, Node node, VirtualMachine vm) {
        //We use the original node, not the temporary
        conf.setRunOn(vm, node);
        node.setCPUCapacity(node.getCPUCapacity() - vm.getCPUConsumption());
        node.setMemoryCapacity(node.getMemoryCapacity() - vm.getMemoryConsumption());
    }

    /**
     * Compute an assignment of each virtual machine involved in a configuration.
     *
     * @param curConf the current configuration
     * @return a new configuration that respect the CPU and memory requirements of all the virtual machines.
     * @throws AssignmentException if at least one virtual machine can not be assigned to a node
     */
    @Override
    public Configuration compute(Configuration curConf) throws AssignmentException {

        //Deep copy of the nodes
        for (Node n : curConf.getOnlines()) {
            this.tmpNodes.add(n.clone());
        }
        final ManagedElementSet<VirtualMachine> vms = curConf.getRunnings().clone();

        if (this.nodeComp != null) {
            Collections.sort(tmpNodes, this.nodeComp);
        }
        if (this.vmComp != null) {
            Collections.sort(vms, this.vmComp);
        }
        Configuration newConf = new SimpleConfiguration();
        for (Node n : curConf.getOnlines()) {
            newConf.addOnline(n);
        }
        for (Node n : curConf.getOfflines()) {
            newConf.addOffline(n);
        }

        for (VirtualMachine vm : vms) {
            boolean assigned = false;
            for (Node node : tmpNodes) {
                if (FirstFitPlacement.canFit(vm, node)) {
                    assignVirtualMachine(newConf, node, vm);
                    assigned = true;
                    break;
                }
            }
            if (!assigned) {
                throw new AssignmentException(newConf, vm);
            }
        }
        return newConf;
    }
}
