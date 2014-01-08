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
import entropy.plan.action.Suspend;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.slice.ConsumingSlice;

/**
 * Model a suspend action.
 * The action is modeled with a consuming slice.
 * The action as the beginning of the c-slice to liberate the resources as earlier as possible.
 *
 * @author Fabien Hermenier
 */
public class SuspendActionModel extends VirtualMachineActionModel {

    /**
     * Make a new suspend action.
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
     * @param vm    the virtual machine associated to the action
     * @param d     the duration of the action
     */
    public SuspendActionModel(ReconfigurationProblem model, VirtualMachine vm, int d) {
        super(vm);
        this.cSlice = new ConsumingSlice(model, "suspend(" + getVirtualMachine().getName() + ")", model.getSourceConfiguration().getLocation(vm), vm.getCPUConsumption(), vm.getMemoryConsumption());
        duration = model.createIntegerConstant("d(suspend(" + vm.getName() + "))", d);
        try {
            cSlice.duration().setInf(d);
        } catch (ContradictionException e) {
            Plan.logger.error(e.getMessage(), e);
        }
        cSlice.addToModel(model);

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

    /**
     * Get the moment the action ends.
     *
     * @return the moment the consuming slice ends.
     */
    @Override
    public final IntDomainVar end() {
        return duration;
    }

    @Override
    public Suspend getDefinedAction(ReconfigurationProblem solver) {
        return new Suspend(getVirtualMachine(),
                solver.getNode(cSlice.hoster().getVal()),
                solver.getNode(cSlice.hoster().getVal()),
                start().getVal(),
                end().getVal());
    }

    @Override
    public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
        return cfg.setSleepOn(getVirtualMachine(), solver.getNode(cSlice.hoster().getVal()));
    }

    @Override
    public ConsumingSlice getConsumingSlice() {
        return this.cSlice;
    }

    @Override
    public String toString() {
        return "suspend(" + getVirtualMachine().getName() + ")";
    }
}
