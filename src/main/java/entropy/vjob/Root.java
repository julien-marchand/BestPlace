/*
 * Copyright (c) 2010 Fabien Hermenier
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

package entropy.vjob;

import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.VirtualMachineActionModel;
import entropy.plan.choco.actionModel.slice.ConsumingSlice;
import entropy.plan.choco.actionModel.slice.DemandingSlice;

/**
 * A constraint to force local operation. A running VM can not be migrated.
 * Other VMs are ignored.
 * <p/>
 * TODO: Test and state if preemptible VMs have to be considered.
 *
 * @author Fabien Hermenier
 */
public class Root implements PlacementConstraint {

    /**
     * The VMs to manipulate.
     */
    private VJobSet<VirtualMachine> vms;

    /**
     * Make a new constraint.
     *
     * @param v the VMs to consider.
     */
    public Root(VJobSet<VirtualMachine> v) {
        this.vms = v;
    }

    /**
     * TODO: documentation
     *
     * @param core
     */
    @Override
    public void inject(ReconfigurationProblem core) {
        for (VirtualMachine vm : vms) {
            VirtualMachineActionModel a = core.getAssociatedAction(vm);
            if (a != null) {
                ConsumingSlice cSlice = a.getConsumingSlice();
                DemandingSlice dSlice = a.getDemandingSlice();
                if (cSlice != null && dSlice != null) {
                    core.post(core.eq(cSlice.hoster(), dSlice.hoster()));
                }
            }
        }
    }

    /**
     * Entailed method
     *
     * @param configuration the configuration to check
     * @return {@code true}
     */
    @Override
    public boolean isSatisfied(Configuration configuration) {
        return true;
    }

    /**
     * Get the VMs involved in the constraint.
     *
     * @return a set of virtual machines, should not be empty
     */
    @Override
    public ExplodedSet<VirtualMachine> getAllVirtualMachines() {
        return vms.flatten();
    }

    @Override
    public ExplodedSet<Node> getNodes() {
        return new ExplodedSet<Node>();
    }

    /**
     * Entailed method. No VMs may be misplaced without consideration of the reconfiguration plan.
     *
     * @param configuration the configuration to check
     * @return an empty set
     */
    @Override
    public ExplodedSet<VirtualMachine> getMisPlaced(Configuration configuration) {
        return new ExplodedSet<VirtualMachine>();
    }

    @Override
    public String toString() {
        return new StringBuilder("root(").append(getAllVirtualMachines()).append(")").toString();
    }
}
