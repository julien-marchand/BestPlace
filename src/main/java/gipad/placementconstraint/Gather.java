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

import entropy.configuration.Configuration;
import entropy.configuration.VirtualMachine;
import entropy.plan.choco.ReconfigurationProblem;
import gipad.configuration.ManagedElementList;


/**
 * A constraint to assign a set of virtual machines to a single node.
 *
 * @author Fabien Hermenier
 */
public class Gather implements PlacementConstraint {

    /**
     * The involved VMs.
     */
    private ManagedElementList<VirtualMachine> vms;

    /**
     * Make a new constraint.
     *
     * @param vms A non-empty set of virtual machines
     */
    public Gather(ManagedElementList<VirtualMachine> vms) {
        this.vms = vms;
    }

    /**
     * Get the virtual machines involved in the constraint.
     *
     * @return a set of VMs. Should not be empty
     */
    @Override
    public ManagedElementList<VirtualMachine> getAllVirtualMachines() {
        return this.vms;
    }

    @Override
    public ExplodedSet<Node> getNodes() {
        return new ExplodedSet<Node>();
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("gather(").append(vms.pretty()).append(")");
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
        Gather that = (Gather) o;
        return getAllVirtualMachines().equals(that.getAllVirtualMachines());
    }

    @Override
    public int hashCode() {
        return getAllVirtualMachines().hashCode();
    }

    @Override
    public void inject(ReconfigurationProblem core) {

        //Get only the future running VMs
        ManagedElementSet<VirtualMachine> runnings = new DefaultManagedElementSet<VirtualMachine>(getAllVirtualMachines());
        runnings.retainAll(core.getFutureRunnings());
        VJob.logger.debug(this + " only consider " + runnings);

        for (int i = 0; i < runnings.size(); i++) {
            for (int j = 0; j < i; j++) {
                Slice t1 = core.getAssociatedAction(runnings.get(i)).getDemandingSlice();
                Slice t2 = core.getAssociatedAction(runnings.get(j)).getDemandingSlice();
                core.post(core.eq(t1.hoster(), t2.hoster()));
            }
        }
    }

    /**
     * Check that the constraint is satified in a configuration.
     *
     * @param cfg the configuration to check
     * @return true if the running VMs are hosted on the same node
     */
    @Override
    public boolean isSatisfied(Configuration cfg) {
        if (getAllVirtualMachines().size() == 0) {
            VJob.logger.debug("No virtual machines was specified");
            return true;
        }
        Node usedNode = null;
        for (VirtualMachine vm : getAllVirtualMachines()) {
            if (cfg.isRunning(vm)) {
                Node n = cfg.getLocation(vm);
                if (usedNode == null) {
                    usedNode = n;
                } else if (!n.equals(usedNode)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get VMs that seems to be misplaced.
     * If all the VMs are not running on the same node, all the VMs
     * are considered as misplaced as the node that they were supposed
     * to be hosted on is not guarantee to be known at 100%.
     */
    @Override
    public ExplodedSet<VirtualMachine> getMisPlaced(Configuration cfg) {
        if (!isSatisfied(cfg)) {
            return getAllVirtualMachines();

        }
        return new ExplodedSet<VirtualMachine>();
    }

    @Override
    public void inject(ReconfigurationProblem core) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public boolean isSatisfied(Configuration cfg) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public ManagedElementList<VirtualMachine> getMisPlaced(Configuration cfg) {
	// TODO Auto-generated method stub
	return null;
    }
}
