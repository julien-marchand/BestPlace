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
import entropy.configuration.VirtualMachine;
import entropy.plan.Plan;
import entropy.plan.action.Stop;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.slice.ConsumingSlice;

/**
 * Model a stop action.
 * The action is modeled with a consuming action.
 * The action starts at the beginning of the slice to stop the VM as earlier
 * as possible.
 *
 * @author Fabien Hermenier
 */
public class StopActionModel extends VirtualMachineActionModel {

    /**
     * Make a new stop action.
     * <p/>
     * Following constraints are added:
     * <ul>
     * <li>{@code slice.duration().inf = actionDuration }</li>
     * <li>{@code end() = start() + actionDuration }</li>
     * <li>{@code actionDuration <= slice.duration() }</li>
     * <li>{@code actionDuration < model.getEnd() }</li>
     * </ul>
     *
     * @param model the model of the reconfiguration problem
     * @param vm    the virtual machine involved in the action
     * @param d     the duration of the action
     */
    public StopActionModel(ReconfigurationProblem model, VirtualMachine vm, int d) {
        super(vm);
        model.createBoundIntVar("start(stop(" + vm.getName() + "))", 0, ReconfigurationProblem.MAX_TIME);
        this.cSlice = new ConsumingSlice(model, "stop(" + vm.getName() + ")", model.getSourceConfiguration().getLocation(vm), vm.getCPUConsumption(), vm.getMemoryConsumption());
        duration = model.createIntegerConstant("d(stop(" + vm.getName() + "))", d);
        //aStart = end - duration
        try {
            cSlice.duration().setInf(d);
            cSlice.end().setInf(d);
        } catch (ContradictionException e) {
            Plan.logger.error(e.getMessage(), e);
        }
        getConsumingSlice().addToModel(model);

    }

    /**
     * Get the moment the action starts.
     * This moment may differ to the moment the slice starts.
     *
     * @return a positive moment between the beginning and the end of the slice
     */
    @Override
    public final IntDomainVar start() {
        return cSlice.start();
    }

    @Override
    public final IntDomainVar end() {
        return duration;
    }

    @Override
    public Stop getDefinedAction(ReconfigurationProblem solver) {
        return new Stop(getVirtualMachine(),
                solver.getNode(cSlice.hoster().getVal()),
                start().getVal(),
                end().getVal());
    }

    @Override
    public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
        return true;
    }

    @Override
    public ConsumingSlice getConsumingSlice() {
        return this.cSlice;
    }
}
