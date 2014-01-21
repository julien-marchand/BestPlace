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

package entropy.plan.choco.actionModel;

import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Configuration;
import entropy.configuration.DefaultNode;
import entropy.plan.action.Action;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.slice.ConsumingSlice;
import entropy.plan.choco.actionModel.slice.DemandingSlice;

/**
 * A Mock object for ActionModel for test purpose
 *
 * @author Fabien Hermenier
 */
public class MockActionModel extends ActionModel {

    private IntDomainVar st;

    private IntDomainVar ed;

    private ConsumingSlice cSlice = null;

    private DemandingSlice dSlice = null;

    /**
     * Make a new mock.
     * Start and end moment are respectively named "start(" + name + ")" and "end(" + name + ")".
     *
     * @param model     the model to retrieve the variables
     * @param name      the name of the mock
     * @param consuming true to use a consuming slice. Named "c(" + name + ")"
     * @param demanding true to use a demanding slice. Named "d(" + name + ")"
     */
    public MockActionModel(ReconfigurationProblem model, String name, boolean consuming, boolean demanding) {
        st = model.createBoundIntVar("start(" + name + ")", 0, 1);
        ed = model.createBoundIntVar("end(" + name + ")", 0, 1);
        if (consuming) {
            cSlice = new ConsumingSlice(model, "c(" + name + ")", new DefaultNode(name, 1, 1, 1), 1, 1);
        }
        if (demanding) {
            dSlice = new DemandingSlice(model, "d(" + name + ")", 1, 1);
        }
        duration = model.createIntegerConstant("duration", 10);
    }

    @Override
    public IntDomainVar start() {
        return st;
    }

    @Override
    public IntDomainVar end() {
        return ed;
    }

    @Override
    public Action getDefinedAction(ReconfigurationProblem solver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConsumingSlice getConsumingSlice() {
        return cSlice;
    }

    @Override
    public DemandingSlice getDemandingSlice() {
        return dSlice;
    }


}
