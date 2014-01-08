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

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.plan.Plan;
import entropy.plan.action.Shutdown;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.slice.DemandingSlice;

/**
 * An action to model the shutdown process of a node.
 *
 * @author Fabien Hermenier
 */
public class ShutdownNodeActionModel extends NodeActionModel {

    private IntDomainVar end;

    private Configuration src;

    private IntDomainVar cost;

    /**
     * Make a new action.
     * <p/>
     * TODO: describe additional constraints
     *
     * @param model the model of the reconfiguration problem
     * @param n     the node involved in the action
     * @param d     the duration of the action
     */
    public ShutdownNodeActionModel(ReconfigurationProblem model, Node n, int d) {
        super(n);
        this.src = model.getSourceConfiguration();
        this.dSlice = new DemandingSlice(model, "shutdown(" + n.getName() + ")", model.getNode(n), n.getCPUCapacity(), n.getMemoryCapacity());
        dSlice.addToModel(model);
        end = model.getEnd();
        cost = model.getEnd();
        duration = model.createIntegerConstant("d(shutdown(" + n.getName() + ")", d);
        try {

            dSlice.duration().setInf(d);
        } catch (ContradictionException e) {
            Plan.logger.error(e.getMessage(), e);
        }
    }


    /**
     * The action starts at the moment the slice starts.
     *
     * @return <code>getDemandingSlice().start()</code>
     */
    @Override
    public IntDomainVar start() {
        return dSlice.start();
    }

    /**
     * The action ends at the moment the slice ends.
     *
     * @return <code>getDemandingSlice().end()</code>
     */
    @Override
    public IntDomainVar end() {
        return end;
    }

    @Override
    public Shutdown getDefinedAction(ReconfigurationProblem solver) {
        //If the node was online, shutdown action. No action otherwise
        if (solver.getSourceConfiguration().isOnline(getNode())) {
            Shutdown sh = new Shutdown(getNode(), start().getVal(), end().getVal());
            return sh;
        } else {

            return null;
        }
    }

    @Override
    public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
        return cfg.addOffline(getNode());
    }

    @Override
    public String toString() {
        return new StringBuilder("shutdown(").append(getNode().getName()).append(")").toString();
    }

    @Override
    public DemandingSlice getDemandingSlice() {
        return dSlice;
    }

    @Override
    public IntDomainVar getGlobalCost() {
        return this.cost;
    }
}
