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

package entropy.plan.choco.search;

import java.util.LinkedList;
import java.util.List;

import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.VirtualMachineActionModel;
import entropy.plan.choco.actionModel.slice.DemandingSlice;

/**
 * A Var selector that focuses on the currently running or sleeping VMs
 * that will be running and move because their current location is no more
 * possible (node has been ban or the VM is fenced). Non-running VMs are ignored.
 *
 * @author Fabien Hermenier
 */
public class ExcludedVirtualMachines extends AbstractIntVarSelector {

    /**
     * The demanding slices to consider.
     */
    private List<VirtualMachineActionModel> actions;

    private Configuration cfg;

    private ReconfigurationProblem rp;

    /**
     * Make a new heuristic.
     * By default, the heuristic doesn't touch the scheduling constraints.
     *
     * @param s   the solver to use to extract the assignment variables
     * @param src the initial configuration
     */
    public ExcludedVirtualMachines(ReconfigurationProblem s, Configuration src, ManagedElementSet<VirtualMachine> vms) {
        super(/*makeVars(solver, src)*/s);
        //System.err.println("to exclude: " + vms);
        cfg = src;

        this.rp = s;
        this.actions = new LinkedList<VirtualMachineActionModel>();
        //Get all the involved slices
        for (VirtualMachine vm : vms) {
            if (rp.getFutureRunnings().contains(vm) /*&& src.isSleeping(vm)/*&& (src.isWaiting(vm) || src.isSleeping(vm))*/) {
                actions.add(rp.getAssociatedAction(vm));
            }
        }
        //System.err.println(actions);
    }

    @Override
    public IntDomainVar selectVar() {
        for (VirtualMachineActionModel a : actions) {
            if (!a.getDemandingSlice().hoster().isInstantiated()) {
                VirtualMachine vm = a.getVirtualMachine();
                /*if (cfg.isWaiting(vm)) {
                    Plan.logger.debug(vm.getName() + " has to move (to run)");
                    return a.getDemandingSlice().hoster();
                } */
                Node n = cfg.getLocation(vm);
                if (n != null) {
                    //VM was running
                    DemandingSlice slice = a.getDemandingSlice();
                    if (!slice.hoster().canBeInstantiatedTo(rp.getNode(n))) {
                        //Plan.logger.debug(vm.getName() + " has to move (to migrate)");
                        return slice.hoster();
                    } else {
                        //Plan.logger.debug("Nothing for " + a);
                    }
                } else {
                    //Plan.logger.debug("but " + a);
                }
            }
        }
        //Plan.logger.debug("NO MORE EXCLUDED VM");
        return null;
    }
}
