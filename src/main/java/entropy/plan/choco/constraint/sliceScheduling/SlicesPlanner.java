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

package entropy.plan.choco.constraint.sliceScheduling;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.Plan;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.ActionModel;
import entropy.plan.choco.actionModel.slice.ConsumingSlice;
import entropy.plan.choco.actionModel.slice.DemandingSlice;
import entropy.plan.choco.actionModel.slice.Slice;
import entropy.plan.choco.actionModel.slice.Slices;
import entropy.plan.choco.constraint.GlobalConstraint;
import entropy.vjob.ExplodedSet;

/**
 * A global constraint to help to plan all the slices in a reconfiguration problem.
 *
 * @author Fabien Hermenier
 */
public class SlicesPlanner implements GlobalConstraint {

    @Override
    public void add(ReconfigurationProblem rp) {
        List<DemandingSlice> dS = new LinkedList<DemandingSlice>();
        List<ConsumingSlice> cS = new LinkedList<ConsumingSlice>();

        List<int[]> linked = new ArrayList<int[]>();
        int dIdx = 0;
        int cIdx = 0;
        List<ActionModel> allActions = new ArrayList<ActionModel>();
        allActions.addAll(rp.getNodeMachineActions());
        allActions.addAll(rp.getVirtualMachineActions());
        //System.err.println(rp.getNodeMachineActions());
        if (allActions.size() == 0) {
            return;
        }
        for (ActionModel na : allActions) {
            if (na.getDemandingSlice() != null && na.getConsumingSlice() != null) {
                linked.add(new int[]{dIdx, cIdx});
            }
            if (na.getDemandingSlice() != null) {
                dS.add(dIdx, na.getDemandingSlice());
                dIdx++;
            }
            if (na.getConsumingSlice() != null) {
                cS.add(cIdx, na.getConsumingSlice());
                cIdx++;
            }
        }

        Slice[] dSlices = dS.toArray(new Slice[dS.size()]);
        Slice[] cSlices = cS.toArray(new Slice[cS.size()]);

        int[] cCPUH = Slices.extractCPUHeights(cSlices);
        int[] cMemH = Slices.extractMemoryHeights(cSlices);
        IntDomainVar[] cHosters = Slices.extractHosters(cSlices);
        IntDomainVar[] cEnds = Slices.extractEnds(cSlices);

        int[] dCPUH = Slices.extractCPUHeights(dSlices);
        int[] dMemH = Slices.extractMemoryHeights(dSlices);
        IntDomainVar[] dHosters = Slices.extractHosters(dSlices);
        IntDomainVar[] dStart = Slices.extractStarts(dSlices);

        int[] associations = new int[dHosters.length];
        for (int i = 0; i < associations.length; i++) {
            associations[i] = PlanMySlices.NO_ASSOCIATIONS; //No associations task
        }
        for (int i = 0; i < linked.size(); i++) {
            int[] assoc = linked.get(i);
            associations[assoc[0]] = assoc[1];
        }
        //printLinked(linked);
        int[] capaCPU = new int[rp.getNodes().length];
        int[] capaMem = new int[rp.getNodes().length];
        for (int idx = 0; idx < rp.getNodes().length; idx++) {
            Node n = rp.getNodes()[idx];
            //for (Node n : model.getInvolvedNodes()) {
            //int idx = model.getInNode(n);
            capaMem[idx] = n.getMemoryCapacity();
            capaCPU[idx] = n.getCPUCapacity();
            //        rp.post(new PlanMySlices(rp.getEnvironment(), idx, n.getCPUCapacity(), n.getMemoryCapacity(), cHosters, cCPUH, cMemH, cEnds,
            //       dHosters, dCPUH, dMemH, dStart, associations));
        }
        Plan.logger.debug("SlicesPlanner branched");

        rp.post(new SlicesScheduler(rp.getEnvironment(), capaCPU, capaMem, cHosters, cCPUH, cMemH, cEnds,
                dHosters, dCPUH, dMemH, dStart, associations));
    }

    @Override
    public boolean isSatisfied(Configuration cfg) {
        return true;
    }

    @Override
    public ExplodedSet<VirtualMachine> getAllVirtualMachines() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ExplodedSet<VirtualMachine> getMisPlaced(Configuration cfg) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
