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

import solver.variables.IntVar;
import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.plan.action.Startup;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.slice.ConsumingSlice;

/**
 * An action to model the boot of a node.
 * The action is modeled with a consuming action.
 *
 * @author Fabien Hermenier
 */
public class BootNodeActionModel extends NodeActionModel {


    /**
     * Make a new action.
     * The following constraint is added into the model:
     * <ul>
     * <li><code>getConsumingSlice().duration() = cost</code></li>
     * </ul>
     *
     * @param model the model of the reconfiguration problem
     * @param n     the node involved in the action
     * @param d     the duration of the action
     */
    public BootNodeActionModel(ReconfigurationProblem model, Node n, int d) {
        super(n);
        cSlice = new ConsumingSlice(model, "boot(" + n.getName() + ")", n, n.getCPUCapacity(), n.getMemoryCapacity(), d);
        duration = model.createIntegerConstant("d(boot(" + n.getName() + "))", d);

        //TODO: check if it was not a mistake: cSlice.fixDuration(0);
        cSlice.fixEnd(d);
        cSlice.addToModel(model);
    }

    /**
     * Return the start of the action.
     *
     * @return <code>getConsumingSlice().start()</code>
     */
    @Override
    public IntVar start() {
        return cSlice.start();
    }

    /**
     * Return the end of the action.
     *
     * @return <code>getConsumingSlice().end()</code>
     */
    @Override
    public IntVar end() {
        return cSlice.end();
    }

    @Override
    public Startup getDefinedAction(ReconfigurationProblem solver) {
        return new Startup(getNode(),
                start().getVal(),
                end().getVal());
    }

    @Override
    public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
        cfg.addOnline(getNode());
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder("boot(").append(getNode().getName()).append(")").toString();
    }
}
