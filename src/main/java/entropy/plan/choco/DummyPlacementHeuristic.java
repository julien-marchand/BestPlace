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

package entropy.plan.choco;

import solver.variables.SetVar;

/**
 * A dummy placement heuristic.
 * Branch on all the variables in a static manner, and select the minimum value for each selected variable.
 *
 * @author Fabien Hermenier
 */
public class DummyPlacementHeuristic implements PlanHeuristic {

    @Override
    public void add(ReconfigurationProblem m) {
        IntDomainVar [] foo = new IntDomainVar[m.getNbIntVars()];
        SetVar [] bar = new SetVar[m.getNbSetVars()];

        for (int i = 0; i < foo.length; i++) {
            foo[i] = m.getIntVarQuick(i);
        }

        for (int i = 0; i < bar.length; i++) {
            bar[i] = m.getSetVarQuick(i);
        }

        m.addGoal(new AssignVar(new StaticVarOrder(m,
                foo), new MinVal()));
        m.addGoal(new AssignVar(new StaticSetVarOrder(m,
                bar), new MinVal()));

    }
}
