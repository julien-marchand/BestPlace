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
package entropy.plan.choco.actionModel;

import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;
import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.plan.Plan;
import entropy.plan.action.Action;
import entropy.plan.action.Startup;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.slice.ConsumingSlice;

/**
 * An action to model a potential boot of a node.
 * The action is modeled with a consuming action.
 * <p/>
 * TODO: Use sets for bins to detect future hosting nodes easily.
 *
 * @author Fabien Hermenier
 */
public class BootableNodeActionModel extends NodeActionModel {

    private IntDomainVar cost;

    /**
     * Make a new action.
     * <p/>
     * The following constraint is added into the model:
     * <ul>
     * <li>{@code getConsumingSlice().duration() = cost }</li>
     * </ul>
     *
     * @param model the model of the reconfiguration problem
     * @param n     the node involved in the action
     * @param d     the duration of the action if it occurred
     */
    public BootableNodeActionModel(ReconfigurationProblem model, Node n, int d) {
        super(n);
        cSlice = new ConsumingSlice(model, "boot?(" + n.getName() + ")", n, n.getCPUCapacity(), n.getMemoryCapacity(), d);

        cost = model.createEnumIntVar("cost(" + toString() + ")", new int[]{0, d});
        //m2.addVariable(cost);
        SetVar s = model.getSetModel(getNode());
        model.post(model.implies(model.gt(s.getCard(), 0), model.eq(cost, d)));
        try {
            cSlice.end().setInf(d);
        } catch (Exception e) {
            Plan.logger.error(e.getMessage(), e);
        }

        cSlice.addToModel(model);
    }

    /**
     * Return the start of the slice.
     *
     * @return <code>getConsumingSlice().start()</code>
     */
    @Override
    public final IntDomainVar start() {
        return cSlice.start();
    }

    /**
     * Return the end of the slice.
     *
     * @return <code>getConsumingSlice().end()</code>
     */
    @Override
    public final IntDomainVar end() {
        return cSlice.end();
    }

    @Override
    public Action getDefinedAction(ReconfigurationProblem solver) {
        if (cost.getVal() != 0) {
            return new Startup(getNode(), start().getVal(), end().getVal());
        }
        return null;
    }

    @Override
    public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
        //Check weither a VM will be on the node to boot it.
        if (cost.getVal() != 0) {
            cfg.addOnline(getNode());
        } else {
            cfg.addOffline(getNode());
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder("boot(").append(getNode().getName()).append(")").toString();
    }

    @Override
	public IntDomainVar getDuration() {
        return cost;
    }
}
