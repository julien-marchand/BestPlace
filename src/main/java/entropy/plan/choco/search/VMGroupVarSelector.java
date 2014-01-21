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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.ResourcePicker;
import entropy.configuration.VirtualMachine;
import entropy.configuration.VirtualMachinesSetComparator;
import entropy.plan.choco.ReconfigurationProblem;

/**
 * A heuristic to select the VM group variable to assign to a value.
 * Biggest groups (ie groups that require the maximum amount of resources) are selected first.
 *
 * @author Fabien Hermenier
 */
public class VMGroupVarSelector extends AbstractIntVarSelector {


    private static IntDomainVar[] makeVars(ReconfigurationProblem s) {


        VirtualMachinesSetComparator cmp = new VirtualMachinesSetComparator(true, ResourcePicker.VMRc.cpuDemand);
        cmp.appendCriteria(true, ResourcePicker.VMRc.memoryConsumption);

        List<ManagedElementSet<VirtualMachine>> groups = new LinkedList<ManagedElementSet<VirtualMachine>>();
        groups.addAll(s.getVMGroups());
        Collections.sort(groups, cmp);

        IntDomainVar[] vs = new IntDomainVar[groups.size()];
        int i = 0;
        for (Iterator<ManagedElementSet<VirtualMachine>> ite = groups.iterator(); ite.hasNext(); ) {
            vs[i++] = s.getVMGroup(ite.next());
        }
        return vs;
    }

    /**
     * Make a new heuristic.
     *
     * @param s the solver to use
     */
    public VMGroupVarSelector(ReconfigurationProblem s) {
        super(s, makeVars(s));
    }

    @Override
    public IntDomainVar selectVar() {
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].isInstantiated()) {
                return vars[i];
            }
        }
        return null;
    }
}
