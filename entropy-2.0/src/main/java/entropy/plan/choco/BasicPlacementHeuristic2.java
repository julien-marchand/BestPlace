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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Configuration;
import entropy.configuration.Configurations;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.ResourcePicker;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.configuration.VirtualMachineComparator;
import entropy.plan.choco.actionModel.ActionModel;
import entropy.plan.choco.actionModel.ActionModels;
import entropy.plan.choco.actionModel.VirtualMachineActionModel;
import entropy.plan.choco.search.ExcludedVirtualMachines;
import entropy.plan.choco.search.HosterVarSelector;
import entropy.plan.choco.search.NodeGroupSelector;
import entropy.plan.choco.search.PureIncomingFirst;
import entropy.plan.choco.search.StayFirstSelector2;
import entropy.plan.choco.search.VMGroupVarSelector;

/**
 * A placement heuristic focused on each VM.
 * First place the VMs, then plan the changes.
 *
 * @author Fabien Hermenier
 */
public class BasicPlacementHeuristic2 implements CorePlanHeuristic {

    private IntDomainVar totalDuration;

    /**
     * Make a new placement heuristic.
     *
     * @param globalCost the global cost of the plan
     */
    public BasicPlacementHeuristic2(IntDomainVar globalCost) {
        this.totalDuration = globalCost;
    }

    /**
     * To compare VMs in a descending order, wrt. their memory consumption.
     */
    private VirtualMachineComparator dsc = new VirtualMachineComparator(false, ResourcePicker.VMRc.memoryConsumption);

    @Override
    public void add(ChocoCustomRP plan) {
        ReconfigurationProblem rp = plan.getModel();
        Configuration src = rp.getSourceConfiguration();

        //Get the VMs to move
        ManagedElementSet<VirtualMachine> onBadNodes = new SimpleManagedElementSet<VirtualMachine>();

        //Quick hack for the VMs to run.
        //TODO: check
        //onBadNodes.addAll(src.getWaitings());

        for (Node n : Configurations.futureOverloadedNodes(src)) {
            onBadNodes.addAll(src.getRunnings(n));
        }

        onBadNodes.addAll(src.getSleepings());

        ManagedElementSet<VirtualMachine> onGoodNodes = src.getRunnings().clone();
        onGoodNodes.removeAll(onBadNodes);

        Collections.sort(onGoodNodes, dsc);
        Collections.sort(onBadNodes, dsc);

        List<VirtualMachineActionModel> goodActions = rp.getAssociatedActions(onGoodNodes);
        List<VirtualMachineActionModel> badActions = rp.getAssociatedActions(onBadNodes);
        //System.err.println(goodActions);
        //System.err.println(badActions);
        //Desactivate cost constraint
        /*for (SConstraint sc : plan.getCostConstraints()) {
            ((Propagator) sc).setPassive();
        } */
        //Go for the VMgroup variable
        VMGroupVarSelector vmGrp = new VMGroupVarSelector(rp);
        rp.addGoal(new AssignVar(vmGrp, new NodeGroupSelector(rp, NodeGroupSelector.Option.bfMem)));

        //Now the VMs associated to group of nodes
        //ManagedElementSet<VirtualMachine> inGroup = new DefaultManagedElementSet<VirtualMachine>();
        if (plan.getQueue().size() != 0) {

            ManagedElementSet<Node> overloaded = Configurations.futureOverloadedNodes(src);
            ManagedElementSet<Node> underloaded = src.getAllNodes().clone();
            underloaded.removeAll(Configurations.futureOverloadedNodes(src));
            List<ManagedElementSet<Node>> favorites = new ArrayList<ManagedElementSet<Node>>();
            favorites.add(underloaded);
            favorites.add(overloaded);


            //Get the VMs to move for exclusion issue
            ManagedElementSet<VirtualMachine> vmsToExlude = rp.getSourceConfiguration().getAllVirtualMachines().clone();
            Collections.sort(vmsToExlude, dsc);
            rp.addGoal(new AssignVar(new ExcludedVirtualMachines(rp, rp.getSourceConfiguration(), vmsToExlude), new StayFirstSelector2(rp, rp.getSatisfyDSlicesHeightConstraint(), StayFirstSelector2.Option.wfMem)));

            //VMs to run
            ManagedElementSet<VirtualMachine> vmsToRun = rp.getSourceConfiguration().getWaitings().clone();
            vmsToRun.removeAll(rp.getFutureWaitings());
            List<VirtualMachineActionModel> runActions = rp.getAssociatedActions(vmsToRun);

            for (ManagedElementSet<VirtualMachine> vms : rp.getVMGroups()) {
                ManagedElementSet<VirtualMachine> sorted = vms.clone();
                Collections.sort(sorted, dsc);
                List<VirtualMachineActionModel> inGroupActions = rp.getAssociatedActions(sorted);
                HosterVarSelector selectForInGroups = new HosterVarSelector(rp, ActionModels.extractDemandingSlices(inGroupActions));
                rp.addGoal(new AssignVar(selectForInGroups, new StayFirstSelector2(rp, rp.getSatisfyDSlicesHeightConstraint(), StayFirstSelector2.Option.wfMem)));
                //solver.addGoal(new AssignVar(selectForInGroups, new StayFirstSelectorWithFavorites(solver, favorites, cpuPack, memPack, StayFirstSelectorWithFavorites.Option.wfMem)));

                //inGroup.addAll(vms);
            }

            //System.err.println("bad: " + badActions);
            //System.err.println("good: " + goodActions);
            //System.err.println("run: " + runActions);
            HosterVarSelector selectForBads = new HosterVarSelector(rp, ActionModels.extractDemandingSlices(badActions));
            rp.addGoal(new AssignVar(selectForBads, new StayFirstSelector2(rp, rp.getSatisfyDSlicesHeightConstraint(), StayFirstSelector2.Option.wfMem)));
            //solver.addGoal(new AssignVar(selectForBads, new StayFirstSelectorWithFavorites(solver, favorites, cpuPack, memPack, StayFirstSelectorWithFavorites.Option.wfMem)));

            HosterVarSelector selectForGoods = new HosterVarSelector(rp, ActionModels.extractDemandingSlices(goodActions));
            rp.addGoal(new AssignVar(selectForGoods, new StayFirstSelector2(rp, rp.getSatisfyDSlicesHeightConstraint(), StayFirstSelector2.Option.wfMem)));
            //solver.addGoal(new AssignVar(selectForGoods, new StayFirstSelectorWithFavorites(solver, favorites, cpuPack, memPack, StayFirstSelectorWithFavorites.Option.wfMem)));

            HosterVarSelector selectForRuns = new HosterVarSelector(rp, ActionModels.extractDemandingSlices(runActions));
            rp.addGoal(new AssignVar(selectForRuns, new StayFirstSelector2(rp, rp.getSatisfyDSlicesHeightConstraint(), StayFirstSelector2.Option.wfMem)));

        }

        ///SCHEDULING PROBLEM


        List<ActionModel> actions = new ArrayList<ActionModel>();
        for (VirtualMachineActionModel vma : rp.getVirtualMachineActions()) {
            actions.add(vma);
        }
        rp.addGoal(new AssignVar(new PureIncomingFirst(rp, actions, plan.getCostConstraints()), new MinVal()));

        rp.addGoal(new AssignVar(new StaticVarOrder(rp, new IntDomainVar[]{rp.getEnd(), totalDuration}), new MinVal()));

    }
}
