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

import choco.cp.solver.constraints.global.BoundAllDiff;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.slice.Slice;

/**
 * Implementation of Spread that ensure all the VMs will be on a distinct node
 * at the end of the reconfiguration process. However, they may be on a same node
 * during the reconfiguration. For stronger guarantees, see {@link ContinuousSpread}
 *
 * @author Fabien Hermenier
 * @see ContinuousSpread
 */
public class LazySpread extends Spread {
    /**
     * Make a new constraint.
     *
     * @param vms the involved virtual machines
     */
    public LazySpread(VJobSet<VirtualMachine> vms) {
        super(vms);
    }

    /**
     * Apply the constraint to the plan if the VM must be in the running state.
     * The constraint is applied on all the future running virtual machines given at instantiation.
     * Others are ignored.
     *
     * @param core the plan to customize. Must implement {@link entropy.plan.choco.ChocoCustomizablePlannerModule}
     */
    @Override
    public void inject(ReconfigurationProblem core) {

        //Get only the future running VMS
        ManagedElementSet<VirtualMachine> runnings = new DefaultManagedElementSet<VirtualMachine>();
        ManagedElementSet<VirtualMachine> ignored = new DefaultManagedElementSet<VirtualMachine>();
        for (VirtualMachine vm : getAllVirtualMachines()) {
            if (core.getFutureRunnings().contains(vm)) {
                runnings.add(vm);
            } else {
                ignored.add(vm);
            }
        }
        if (runnings.size() == 0) {
            VJob.logger.debug(this + " is entailed. No VMs are running");
        } else {
            if (ignored.size() > 0) {
                VJob.logger.debug(this + " ignores non-running VMs: " + ignored);
            }
            //Get all the host variable
            List<IntDomainVar> vars = new ArrayList<IntDomainVar>();
            for (VirtualMachine vm : runnings) {
                Slice t = core.getAssociatedAction(vm).getDemandingSlice();
                if (t != null) {
                    vars.add(t.hoster());
                }
            }
            //core.post(new AllDifferent(vars.toArray(new IntDomainVar[vars.size()]), core.getEnvironment()));
            core.post(new BoundAllDiff(vars.toArray(new IntDomainVar[vars.size()]), true));
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("lSpread(").append(vms.pretty()).append(")").toString();
    }
}
