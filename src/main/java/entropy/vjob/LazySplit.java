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

import java.util.LinkedList;
import java.util.List;

import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.VirtualMachine;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.ActionModel;
import entropy.plan.choco.actionModel.ActionModels;
import entropy.plan.choco.actionModel.slice.DemandingSlice;
import entropy.plan.choco.actionModel.slice.Slice;
import entropy.plan.choco.actionModel.slice.Slices;

/**
 * A lazy implementation of Split focused only on the demanding slices.
 * So during a reconfiguration process, some VMs of the two set may be hosted
 * on a common nodes.
 *
 * @author Fabien Hermenier
 */
public class LazySplit extends Split {

    /**
     * Make a new constraint.
     *
     * @param vmset1 the first set of virtual machines
     * @param vmset2 the second set of virtual machines
     */
    public LazySplit(VJobSet<VirtualMachine> vmset1, VJobSet<VirtualMachine> vmset2) {
        super(vmset1, vmset2);
    }

    /**
     * TODO documentation
     *
     * @param core
     */
    @Override
    public void inject(ReconfigurationProblem core) {
        List<ActionModel> actions1 = new LinkedList<ActionModel>();
        List<ActionModel> actions2 = new LinkedList<ActionModel>();
        for (VirtualMachine vm : getFirstSet()) {
            actions1.add(core.getAssociatedAction(vm));
        }

        for (VirtualMachine vm : getSecondSet()) {
            actions2.add(core.getAssociatedAction(vm));
        }

        List<DemandingSlice> slices1 = ActionModels.extractDemandingSlices(actions1);
        List<DemandingSlice> slices2 = ActionModels.extractDemandingSlices(actions2);

        IntDomainVar[] hosters1 = Slices.extractHosters(slices1.toArray(new Slice[slices1.size()]));
        IntDomainVar[] hosters2 = Slices.extractHosters(slices2.toArray(new Slice[slices2.size()]));
        for (int i = 0; i < hosters1.length; i++) {
            for (int j = 0; j < hosters2.length; j++) {
                core.post(core.neq(hosters1[i], hosters2[j]));
            }
        }
    }

    @Override
    public String toString() {
        return "lSplit(" + getFirstSet() + "," + getSecondSet() + ")";
    }
}
