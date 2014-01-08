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
package entropy.vjob;

import choco.kernel.solver.ContradictionException;
import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.slice.Slice;
import gnu.trove.TIntHashSet;

/**
 * A constraint to enforce a set of virtual machines
 * to be hosted on a single group of physical elements.
 *
 * @author Fabien Hermenier
 */
public class Fence implements PlacementConstraint {

    /**
     * The list of possible groups of nodes.
     */
    private VJobSet<Node> group;

    /**
     * The list of VMs involved in the constraint.
     */
    private VJobSet<VirtualMachine> vms;

    /**
     * Make a new constraint that enforce all the virtual machines
     * to be hosted on a single group of nodes.
     *
     * @param vms   the set of VMs to assign.
     * @param group the group of nodes.
     */
    public Fence(VJobSet<VirtualMachine> vms, VJobSet<Node> group) {
        this.vms = vms;
        this.group = group;
    }

    /**
     * Get the virtual machines involved in the constraint.
     *
     * @return a set of VMs. should not be empty
     */
    @Override
    public ExplodedSet<VirtualMachine> getAllVirtualMachines() {
        return this.vms.flatten();
    }

    /**
     * Get the set of nodes involved in the constraint.
     *
     * @return a set of nodes, should not be empty
     */
    @Override
	public ExplodedSet<Node> getNodes() {
        return this.group.flatten();
    }

    /**
     * Get the set of virtual machines involved in the constraint.
     *
     * @return a set of virtual machines, should not be empty
     */
    public VJobSet<VirtualMachine> getVirtualMachines() {
        return this.vms;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("fence(").append(vms.pretty());
        buffer.append(", ").append(group.pretty());
        buffer.append(")");
        return buffer.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Fence that = (Fence) o;
        return group.equals(that.group) && vms.equals(that.vms);
    }

    @Override
    public int hashCode() {
        int result = vms.hashCode();
        result = 31 * result + group.hashCode();
        return result;
    }

    @Override
    public void inject(ReconfigurationProblem core) {

        ManagedElementSet<VirtualMachine> runnings = new SimpleManagedElementSet<VirtualMachine>();
        ManagedElementSet<VirtualMachine> ignored = new SimpleManagedElementSet<VirtualMachine>();
        for (VirtualMachine vm : getAllVirtualMachines()) {
            if (core.getFutureRunnings().contains(vm)) {
                runnings.add(vm);
            } else {
                ignored.add(vm);
            }
        }
        if (runnings.isEmpty()) {
            VJob.logger.debug(this + " is entailed. No VMs are running");
        } else {
            if (!ignored.isEmpty()) {
                VJob.logger.debug(this + " ignores non-running VMs: " + ignored);
            }

            if (getNodes().size() == 1) { //Only 1 possible destination node, so we directly instantiate the variable.
                for (VirtualMachine vm : runnings) {
                    Slice t = core.getAssociatedAction(vm).getDemandingSlice();
                    if (t != null) {
                        try {
                            t.hoster().setVal(core.getNode(getNodes().get(0)));
                        } catch (ContradictionException e) {
                            VJob.logger.error(e.getMessage(), e);
                        }
                    }
                }
            } else {
                TIntHashSet iExlude = new TIntHashSet();
                for (Node n : core.getSourceConfiguration().getOnlines()) {
                    if (!getNodes().contains(n)) {
                        iExlude.add(core.getNode(n));
                    }
                }

                for (Node n : core.getSourceConfiguration().getOfflines()) {
                    if (!getNodes().contains(n)) {
                        iExlude.add(core.getNode(n));
                    }
                }

                //Domain restriction. Remove all the non-involved nodes
                int[] idxs = iExlude.toArray();
                for (VirtualMachine vm : runnings) {
                    Slice t = core.getAssociatedAction(vm).getDemandingSlice();
                    if (t != null) {
                        for (int x : idxs) {
                            try {
                                t.hoster().remVal(x);
                            } catch (ContradictionException e) {
                                VJob.logger.error(e.getMessage(), e);
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
     * @return true if the running VMs are hosted on more than one group
     */
    @Override
    public boolean isSatisfied(Configuration cfg) {
        if (getNodes().isEmpty()) {
            VJob.logger.error("No group of nodes was specified");
            return false;
        }
        for (VirtualMachine vm : getAllVirtualMachines()) {
            if (cfg.isRunning(vm) && !getNodes().getElements().contains(cfg.getLocation(vm))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ExplodedSet<VirtualMachine> getMisPlaced(Configuration cfg) {
        ExplodedSet<VirtualMachine> bad = new ExplodedSet<VirtualMachine>();
        for (VirtualMachine vm : getAllVirtualMachines()) {
            if (cfg.isRunning(vm) && !getNodes().getElements().contains(cfg.getLocation(vm))) {
                bad.add(vm);
            }
        }
        return bad;
    }
}
