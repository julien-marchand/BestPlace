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

import java.util.List;

import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.slice.DemandingSlice;
import entropy.plan.choco.actionModel.slice.Slices;

/**
 * A Var selector that focuses on the assignment var of the demanding slices.
 * To improve the process, it is possible to desactivate the scheduling constraints
 * at the beginning of the heuristic. However, don't forget to activate them at the end.
 *
 * @author Fabien Hermenier
 */
public class HosterVarSelector extends AbstractIntVarSelector {

    /**
     * Make a new heuristic.
     * By default, the heuristic doesn't touch the scheduling constraints.
     *
     * @param solver the solver to use to extract the assignment variables
     * @param slices the slices to consider
     */
    public HosterVarSelector(ReconfigurationProblem solver, List<DemandingSlice> slices) {
        super(solver, Slices.extractHosters(slices.toArray(new DemandingSlice[slices.size()])));
    }

    @Override
    public IntDomainVar selectVar() {
        //System.err.println(Arrays.toString(vars));
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].isInstantiated()) {
                return vars[i];
            }
        }
        //Plan.logger.debug("No move VMs to place");
        return null;
    }

}
