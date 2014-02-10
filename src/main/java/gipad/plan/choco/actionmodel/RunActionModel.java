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

import gipad.configuration.configuration.Configuration;
import gipad.plan.action.Run;
import gipad.plan.choco.ReconfigurationProblem;
import gipad.plan.choco.actionmodel.slice.DemandingSlice;
import gipad.plan.choco.actionmodel.slice.IncomingSlice;

import org.discovery.DiscoveryModel.model.Node;
import org.discovery.DiscoveryModel.model.VirtualMachine;

import solver.ICause;
import solver.constraints.ICF;
import solver.constraints.LCF;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.variables.IntVar;
import solver.variables.VF;


/**
 * Model an action that run a virtual machine.
 * The action is modeled with one demanding slice and a Incoming slice
 * The action will start at the beginning of the slice however, it may finish before the end of the slice
 * (if the duration of the action is < to the duration of the slice).
 * This little hack tends to run the VM sooner.
 *
 */
public class RunActionModel extends VirtualMachineActionModel {

    /**
     * Make a new run action.
     * <p/>
     * The following constraints are added:
     * <ul>
     * <li></li>
     * <li>{@code run.duration = d.duration + i.duration}</li>
     * <li>{@code i.duration = d(i.hoster())}</li>
     * <li>{@code i.end() = d.start() }</li>
     * <li>{@code i.hoster() = d.hoster() }</li>
     * </ul>
     *
     * @param model the model of the reconfiguration problem
     * @param vm    the virtual machine associated to the action
     * @param d     the duration of the action
     */
    public RunActionModel(ReconfigurationProblem model, Configuration conf, VirtualMachine vm) {
        super(vm);
        super.conf = conf;
        super.iSlice = new IncomingSlice(model, "run(" + vm.name() + ")", conf.getIncoming(vm), conf);
        super.dSlice = new DemandingSlice(model, "run(" + vm.name() + ")", conf.getDemanding(vm), conf);
 
        iSlice.addToModel(model);
        dSlice.addToModel(model);

        
//        //la durée du démarrage ne dépend pas de l'hote mais uniquement du volume de la VM
//        for(Node n : conf.getAllNodes()){
//        	model.getSolver().post(LCF.ifThen(ICF.arithm(iSlice.hoster(), "=", n.getId()), ICF.arithm(iSlice.duration(), "=", conf.getRunDuration(n, vm))));
//        }
        //La durée de chargement en mémoire est fixe (elle dépend de la taille de la vm)
        model.getSolver().post(ICF.arithm(iSlice.duration(), "=", conf.getRunDuration(vm)));
        model.getSolver().post(ICF.arithm(iSlice.end(), "=", dSlice.start()));
        //La durée totale de l'action model est la somme des durées des deux slices
        super.duration = VF.enumerated("run_dur(" + vm.name() + ")", 0, model.MAX_TIME ,model.getSolver());
        model.getSolver().post(ICF.sum(new IntVar[]{dSlice.duration(), iSlice.duration()}, super.duration));
        
        model.getSolver().post(ICF.arithm(iSlice.hoster(), "=", dSlice.hoster()));
        //pas d'utilisation de la bande passante pour charger une VM en mémoire
        iSlice.getBwInput().instantiateTo(0, null);
        iSlice.getBwOutput().instantiateTo(0, null);
        
    }

    @Override
    public Run getDefinedAction(ReconfigurationProblem solver) {
        return new Run(getVirtualMachine(),
                solver.getNode(dSlice.hoster().getValue()),
                start().getValue(),
                end().getValue());
    }

    @Override
    public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
        cfg.setRunOn(getVirtualMachine(), solver.getNode(dSlice.hoster().getValue()));
        return true;
    }

    @Override
    public final IntVar start() {
        return iSlice.start();
    }

    @Override
    public final IntVar end() {
        return dSlice.end();
    }


    @Override
    public String toString() {
        return new StringBuilder("run(").append(getVirtualMachine().name()).append(")").toString();
    }

    @Override
    public IntVar getGlobalCost() {
        return super.duration;
    }
}
