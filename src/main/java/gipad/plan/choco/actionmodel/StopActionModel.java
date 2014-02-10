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
package gipad.plan.choco.actionmodel;

import gipad.configuration.configuration.*;
import gipad.plan.action.Stop;
import gipad.plan.choco.ReconfigurationProblem;
import gipad.plan.choco.actionmodel.slice.ConsumingSlice;
import gipad.plan.choco.actionmodel.slice.DemandingSlice;
import gipad.plan.choco.actionmodel.slice.IncomingSlice;
import gipad.plan.choco.actionmodel.slice.LeavingSlice;

import solver.Cause;
import solver.constraints.ICF;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.VF;



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
     *
     * @param model the model of the reconfiguration problem
     * @param vm    the virtual machine involved in the action
     * @param d     the duration of the action
     */
    public StopActionModel(ReconfigurationProblem model, Configuration conf, VirtualMachine vm) {
        super(vm);
        super.conf = conf;
        super.cSlice = new ConsumingSlice(model, "stop(" + vm.name() + ")", vm ,conf.getIncoming(vm), conf);
        super.lSlice = new LeavingSlice(model, "stop(" + vm.name() + ")", vm, conf.getDemanding(vm), conf);
        
        cSlice.addToModel(model);
        lSlice.addToModel(model);
        
        //La durée de chargement sur le disque est fixe (elle dépend de la taille de la vm)
        //TODO adapter pour ajouter l'activité cpu
        model.getSolver().post(ICF.arithm(lSlice.duration(), "=", conf.getStopDuration(vm)));
        
        model.getSolver().post(ICF.arithm(cSlice.getEnd(), "=", lSlice.getStart()));
      //La durée totale de l'action model est la somme des durées des deux slices
        super.duration = VF.enumerated("stop_dur(" + vm.name() + ")", 0, model.MAX_TIME ,model.getSolver());
        model.getSolver().post(ICF.sum(new IntVar[]{cSlice.duration(), lSlice.duration()}, super.duration));
                
        //pas d'utilisation de la bande passante pour charger une VM dans le disque
        try {
			lSlice.getBwInput().instantiateTo(0, Cause.Null);
			lSlice.getBwOutput().instantiateTo(0, Cause.Null);
		} catch (ContradictionException e) {
			e.printStackTrace();
		}

    }

    @Override
    public final IntVar start() {
        return cSlice.getStart();
    }

    @Override
    public final IntVar end() {
        return lSlice.getEnd();
    }

    @Override
    public Stop getDefinedAction(ReconfigurationProblem solver) {
        return new Stop(getVirtualMachine(),
                solver.getNode(cSlice.hoster().getValue()),
                start().getValue(),
                end().getValue());
    }

    @Override
    public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
        return true;
    }

    @Override
    public ConsumingSlice getConsumingSlice() {
        return this.cSlice;
    }

	@Override
	public IntVar getGlobalCost() {
		return super.duration;
	}
}
