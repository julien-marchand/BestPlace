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
package gipad.placementconstraint;

import org.discovery.DiscoveryModel.model.Node;
import org.discovery.DiscoveryModel.model.VirtualMachine;

import gipad.configuration.configuration.Configuration;
import gipad.plan.choco.ReconfigurationProblem;
import gipad.plan.choco.actionmodel.slice.Slice;
import gipad.tools.ManagedElementList;
import gipad.tools.SimpleManagedElementList;



/**
 * A constraint to enforce a set of virtual machines to avoid
 * to be hosted on a group of nodes.
 *
 * @author Fabien Hermenier
 */
public class Ban implements PlacementConstraint {

    /**
     * The set of nodes to exlude.
     */
    private ManagedElementList<Node> nodes;

    /**
     * The set of VMs involved in the constraint.
     */
    private ManagedElementList<VirtualMachine> vms;

    /**
     * Make a new constraint.
     *
     * @param vms   the VMs to assign
     * @param nodes the nodes to exclude
     */
    public Ban(ManagedElementList<VirtualMachine> vms, ManagedElementList<Node> nodes) {
        this.nodes = nodes;
        this.vms = vms;
    }

    /**
     * Get the set of nodes involved in the constraint.
     *
     * @return a set of nodes
     */
    @Override
	public ManagedElementList<Node> getNodes() {
        return (ManagedElementList<Node>) this.nodes;
    }

    /**
     * Get all the virtual machines involved in the constraint.
     *
     * @return a set of VMs. Should not be empty
     */
    public ManagedElementList<VirtualMachine> getVirtualMachines() {
        return this.vms;
    }

    /**
     * Get all the virtual machines involved in the constraint.
     *
     * @return a set of VMs. Should not be empty
     */
    @Override
    public ManagedElementList<VirtualMachine> getAllVirtualMachines() {
        return (ManagedElementList<VirtualMachine>) this.vms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Ban that = (Ban) o;

        return (nodes.equals(that.nodes) && getAllVirtualMachines().equals(that.getAllVirtualMachines()));
    }

    @Override
    public int hashCode() {
        int result = getAllVirtualMachines().hashCode();
        result = 31 * result + nodes.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("ban(").append(vms.prettyOut());
        buffer.append(", ");
        buffer.append(nodes.prettyOut());
        buffer.append(")");
        return buffer.toString();
    }


    /**
     * Apply the constraint to the plan to all the VMs in a future running state.
     * FIXME: What about running VMs that will be suspended ?
     *
     * @param core the plan to customize. Must implement {@link entropy.plan.choco.ChocoCustomizablePlannerModule}
     */
    @Override
    public void inject(ReconfigurationProblem core) {

        //Get only the future running VMS
        ManagedElementList<VirtualMachine> runnings = new SimpleManagedElementList<VirtualMachine>();
        ManagedElementList<VirtualMachine> ignored = new SimpleManagedElementList<VirtualMachine>();
        for (VirtualMachine vm : getAllVirtualMachines()) {
            if (core.getFutureRunnings().contains(vm)) {
                runnings.add(vm);
            } else {
                ignored.add(vm);
            }
        }
        if (runnings.size() == 0) {
            //FIXME debug log VJob.logger.debug(this + " is entailed. No VMs are running");
        } else {
            if (ignored.size() > 0) {
        	//FIXME debug log VJob.logger.debug(this + " ignores non-running VMs: " + ignored);
            }
            //Exclude all the nodes from the assign vars
            if (runnings.size() > 0) {
                int[] nodesIdx = new int[getNodes().size()];
                int i = 0;
                for (Node n : getNodes()) {
                    nodesIdx[i++] = core.getNode(n);
                }
                for (VirtualMachine vm : runnings) {
                    Slice t = core.getAssociatedAction(vm).getDemandingSlice();
                    if (t != null) {
                        for (int x = 0; x < nodesIdx.length; x++) {
                            try {
                                t.hoster().removeValue(nodesIdx[x],null);
                            } catch (Exception e) {
                        	//FIXME debug log VJob.logger.error(e.getMessage(), e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Check that the constraint is satified in a configuration.
     *
     * @param cfg the configuration to check
     * @return true if the VMs are not running on the banned nodes.
     */
    @Override
    public boolean isSatisfied(Configuration cfg) {
        ManagedElementList<Node> ns = (ManagedElementList<Node>) getNodes();
        for (VirtualMachine vm : getAllVirtualMachines()) {
            if (cfg.isRunning(vm) && ns.contains(cfg.getLocation(vm))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ManagedElementList<VirtualMachine> getMisPlaced(Configuration cfg) {
	ManagedElementList<VirtualMachine> bad = new SimpleManagedElementList<VirtualMachine>();
        for (VirtualMachine vm : getAllVirtualMachines()) {
            if (cfg.isRunning(vm) && getNodes().contains(cfg.getLocation(vm))) {
                bad.add(vm);
            }
        }
        return bad;
    }
}