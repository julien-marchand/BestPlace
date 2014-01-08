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

import java.util.ArrayList;
import java.util.List;

import choco.cp.solver.constraints.integer.Element;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.choco.ReconfigurationProblem;

/**
 * A constraint to enforce a set of virtual machines
 * to be hosted on a single group of physical elements among those give in parameters.
 *
 * @author Fabien Hermenier
 */
public class OneOf implements PlacementConstraint {

    /**
     * The list of possible groups of nodes.
     */
    private VJobMultiSet<Node> groups;

    /**
     * The list of VMs involved in the constraint.
     */
    private VJobSet<VirtualMachine> vms;

    /**
     * Make a new constraint that enforce all the virtual machines
     * to be hosted on a single group of node among those given in parameters.
     *
     * @param vms    the set of VMs to assign.
     * @param groups the list of possible groups of nodes.
     */
    public OneOf(VJobSet<VirtualMachine> vms, VJobMultiSet<Node> groups) {
        this.vms = vms;
        this.groups = groups;
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

    @Override
    public ExplodedSet<Node> getNodes() {
        return new ExplodedSet<Node>(this.groups.getElements());
    }

    /**
     * Get the different groups of nodes involved in the constraint.
     *
     * @return a set of groups. May be empty
     */
    public VJobMultiSet<Node> getGroups() {
        return this.groups;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("oneOf(").append(vms.pretty());
        buffer.append(", ").append(groups.pretty()).append(")");
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
        OneOf that = (OneOf) o;
        return groups.equals(that.groups) && getAllVirtualMachines().equals(that.getAllVirtualMachines());
    }

    @Override
    public int hashCode() {
        int result = getAllVirtualMachines().hashCode();
        result = 31 * result + groups.hashCode();
        return result;
    }

    @Override
    public void inject(ReconfigurationProblem core) {

        if (getGroups().size() == 0) {
            VJob.logger.debug("Ignoring " + this + ", no groups were specified");
            return;
        }

        //Get only the future running VMs
        ManagedElementSet<VirtualMachine> runnings = getAllVirtualMachines().clone();
        runnings.retainAll(core.getFutureRunnings());
        VJob.logger.debug(this + " only consider " + runnings);


        //Now, we create a group variable & all that stuff
        IntDomainVar vmGrpId = null;
        List<ManagedElementSet<Node>> grps = new ArrayList<ManagedElementSet<Node>>();
        List<ExplodedSet<Node>> ns = getGroups().expand();
        for (int i = 0; i < ns.size(); i++) {
            grps.add(ns.get(i));
        }
        vmGrpId = core.makeGroup(getAllVirtualMachines(), grps);

        //ChocoCustomizablePlannerModule.LOGGER.debug(vmGrpId + " " + Arrays.toString(toKeep));
        //ManagedElementSet<Node> toKeep = groups.getElements();
        //new Fence(getAllVirtualMachines(), new ExplodedSet<Node>(toKeep)).inject(core);
        //model.addConstraint(Choco.among(vmGrpId, toKeep));
        List<ExplodedSet<Node>> all = getGroups().expand();
        ManagedElementSet<Node> involved = getGroups().getElements();

        //Must be the position of the VM in the context of this group
        int[] values = new int[involved.size()];

        for (int i = 0; i < values.length; i++) {
            Node n = core.getNode(i);
            //Get the group that belong to each node
            for (int j = 0; j < getGroups().size(); j++) {
                ManagedElementSet<Node> grp = all.get(j);
                if (grp.contains(n)) {
                    values[i] = j;
                    break;
                }
            }
        }

        for (VirtualMachine vm : runnings) {
            IntDomainVar assign = core.getAssociatedAction(vm).getDemandingSlice().hoster();
            SConstraint c = new Element(assign, /*values*/core.getNodesGroupId(), vmGrpId);
            //if (vm.getName().equals("VM518")) {
            //System.err.println(System.currentTimeMillis() + " " + assign.pretty());
            //System.err.println(c.pretty());
            //System.err.flush();
            //}
            core.post(c);
        }
    }

    /**
     * Check that the constraint is satisfied in a configuration.
     *
     * @param cfg the configuration to check
     * @return true if the running VMs are hosted on more than one group
     */
    @Override
    public boolean isSatisfied(Configuration cfg) {
        if (getGroups().size() == 0) {
            VJob.logger.error("No group of nodes was specified");
            return false;
        }

        VirtualMachine vm1 = null;
        for (VirtualMachine vm : vms) {
            if (cfg.isRunning(vm)) {
                vm1 = vm;
                break;
            }
        }
        if (vm1 == null) {
            //No running VMs, no need to check
            return true;
        }
        Node n1 = cfg.getLocation(vm1);
        VJobSet<Node> selectedGroup = null;
        for (VJobSet<Node> grp : getGroups().expand()) {
            if (grp.contains(n1)) {
                selectedGroup = grp;
                break;
            }
        }
        if (selectedGroup == null) {
            VJob.logger.error(this + ": " + vm1.getName() + " is running on an invalid group");
            return false;
        }
        for (VirtualMachine vm : getAllVirtualMachines()) {
            Node n = cfg.getLocation(vm);
            if (cfg.isRunning(vm)) {
                if (!selectedGroup.contains(n)) {
                    //ChocoCustomizablePlannerModule.LOGGER.error(this +": " + vm.getName() + " is not running into " + selectedGroup + " instead: " + n.getName());
                    return false;
                }
            }
        }

        return true;
    }


    @Override
    public ExplodedSet<VirtualMachine> getMisPlaced(Configuration cfg) {
        if (!isSatisfied(cfg)) {
            return getAllVirtualMachines();
        }
        return new ExplodedSet<VirtualMachine>();
    }
}