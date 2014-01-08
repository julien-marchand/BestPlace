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

import choco.cp.solver.constraints.reified.ReifiedFactory;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.VirtualMachine;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.VirtualMachineActionModel;
import entropy.plan.choco.actionModel.slice.ConsumingSlice;
import entropy.plan.choco.actionModel.slice.DemandingSlice;

/**
 * A continuous implementation of Spread with provide stronger guarantee than {@link LazySpread}.
 * All the virtual machines involved in the constraints will never be hosted on the same node,
 * even during the reconfiguration process. For this purpose, migrations may be delayed to
 * enforce the non-overlapping between virtual machines.
 * <p/>
 * For a set of $n$ VMs, it uses $n*(n-1)$ implies constraints.
 * For each couple of VM (Vx,Vy), it enforces (C_x^h = D_y^h => C_x^ed <= D_y^st)
 *
 * @author Fabien Hermenier
 */
public class ContinuousSpread extends Spread {
    /**
     * Make a new constraint.
     *
     * @param vms the involved virtual machines
     */
    public ContinuousSpread(VJobSet<VirtualMachine> vms) {
        super(vms);
    }

    @Override
    public String toString() {
        return new StringBuilder("cSpread(").append(vms.pretty()).append(")").toString();
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

        //Consider only the currently running and the future running VMs
        ExplodedSet<VirtualMachine> runnings = new ExplodedSet<VirtualMachine>();
        ExplodedSet<VirtualMachine> ignored = new ExplodedSet<VirtualMachine>();
        for (VirtualMachine vm : getAllVirtualMachines()) {
            if (core.getFutureRunnings().contains(vm) || core.getSourceConfiguration().isRunning(vm)) {
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

            //The lazy spread implementation for the placement
            new LazySpread(runnings).inject(core);

            for (int i = 0; i < runnings.size(); i++) {
                VirtualMachine vmI = runnings.get(i);
                VirtualMachineActionModel aI = core.getAssociatedAction(vmI);
                for (int j = 0; j < i; j++) {
                    VirtualMachine vmJ = runnings.get(j);
                    VirtualMachineActionModel aJ = core.getAssociatedAction(vmJ);
                    DemandingSlice d = aI.getDemandingSlice();
                    ConsumingSlice c = aJ.getConsumingSlice();
                    if (d != null && c != null) {
                        //if slices already placed, and on different nodes, we can ignore this constraint, otherwise ...
                        if (!(c.hoster().isInstantiated() && d.hoster().isInstantiated() && c.hoster().getVal() != d.hoster().getVal())) {
                            IntDomainVar eq = core.createBooleanVar("eq");
                            core.post(ReifiedFactory.builder(eq, core.eq(d.hoster(), c.hoster()), core));
                            core.post(core.implies(eq, core.leq(c.end(), d.start())));
                        }
                    }
                    //The inverse relation
                    d = aJ.getDemandingSlice();
                    c = aI.getConsumingSlice();

                    if (d != null && c != null) {
                        //if slices already placed, and on different nodes, we can ignore this constraint, otherwise ...
                        if (!(c.hoster().isInstantiated() && d.hoster().isInstantiated() && c.hoster().getVal() != d.hoster().getVal())) {
                            IntDomainVar eq = core.createBooleanVar("eq");
                            core.post(ReifiedFactory.builder(eq, core.eq(d.hoster(), c.hoster()), core));
                            core.post(core.implies(eq, core.leq(c.end(), d.start())));
                        }
                    }
                }
            }
        }
    }
}
