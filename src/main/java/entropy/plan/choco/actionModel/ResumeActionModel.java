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
import entropy.configuration.VirtualMachine;
import entropy.plan.action.Resume;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.slice.DemandingSlice;

/**
 * Model the action that resume a virtual machine.
 * The action is modeled with one demanding slice. The action will start at the
 * beginning of the slice however, it may finish before the end of the slice (if the duration of the action
 * is < to the duration of the slice). This little hack tends to resume the VM sooner.
 *
 * @author Fabien Hermenier
 */
public class ResumeActionModel extends VirtualMachineActionModel {

    /**
     * The moment the action ends.
     */
    private IntVar finish;

    /**
     * Make a new resume action model.
     * <p/>
     * Add the action to the model with the following constraints.
     * <ul>
     * <li>{@code resumeDuration = {localValue, remoteValue}}</li>
     * <li>{@code hoster() =  currentNode <=> resumeDuration=localValue }</li>
     * <li>{@code duration().inf == Math.min(remoteCost, localCost) }</li>
     * <li>{@code duration() >= resumeDuration }</li>
     * <li>{@code end() > start() }</li>
     * </ul>
     *
     * @param model       the model of the reconfiguration problem
     * @param vm          the virtual machine associated to the action
     * @param localValue  the duration of a local resume
     * @param remoteValue the duration of the remote resume
     */
    public ResumeActionModel(ReconfigurationProblem model, VirtualMachine vm, int localValue, int remoteValue) {
        super(vm);
        this.finish = model.createBoundIntVar("end(resume(" + vm.getName() + "))", 0, ReconfigurationProblem.MAX_TIME);
        this.dSlice = new DemandingSlice(model, "resume(" + getVirtualMachine().getName() + ")", vm.getCPUDemand(), vm.getMemoryDemand());

        duration = model.createEnumIntVar("duration(resume(" + getVirtualMachine().getName() + "))", new int[]{localValue, remoteValue});

        dSlice.addToModel(model);

        int curLocation = model.getNode(model.getSourceConfiguration().getLocation(getVirtualMachine()));

        IntVar stay = model.createBooleanVar("rt(" + getVirtualMachine().getName() + ")");
        model.post(ReifiedFactory.builder(stay, model.eq(curLocation, dSlice.hoster()), model));
        model.post(model.ifOnlyIf(stay, model.eq(duration, localValue)));
        model.post(model.leq(finish, model.getEnd()));
        //always: a.end() >= a.start() && a.end() <= max(a.start() + localValue, start + endValue)
        model.post(model.geq(dSlice.duration(), duration));
        model.post(model.eq(finish, model.plus(start(), duration)));
    }

    /**
     * Get the moment the action finishes
     * The slice may finish later !
     *
     * @return the moment the action ends
     */
    @Override
    public final IntVar end() {
        return finish;
    }

    /**
     * Get the moment the action starts.
     *
     * @return the moment the demanding slice starts.
     */
    @Override
    public final IntVar start() {
        return dSlice.start();
    }

    @Override
    public Resume getDefinedAction(ReconfigurationProblem solver) {
        return new Resume(getVirtualMachine(),
                solver.getSourceConfiguration().getLocation(getVirtualMachine()),
                solver.getNode(dSlice.hoster().getVal()),
                start().getVal(),
                end().getVal());
    }

    @Override
    public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
        cfg.setRunOn(getVirtualMachine(), solver.getNode(dSlice.hoster().getVal()));
        return true;
    }

    @Override
    public String toString() {
        return "resume(" + getVirtualMachine().getName() + ")";
    }

}
