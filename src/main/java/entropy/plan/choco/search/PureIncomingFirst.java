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

package entropy.plan.choco.search;

import java.util.BitSet;
import java.util.List;

import choco.kernel.common.Constant;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.Plan;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.ActionModel;
import entropy.plan.choco.actionModel.ActionModels;
import entropy.plan.choco.actionModel.VirtualMachineActionModel;
import entropy.plan.choco.actionModel.slice.DemandingSlice;

/**
 * An heuristic to branch first on the start moment of actions
 * that arrive on nodes without any outgoing actions.
 *
 * @author Fabien Hermenier
 */
public class PureIncomingFirst extends AbstractIntVarSelector {


    private IntDomainVar[] hoster;

    private IntDomainVar[] starts;

    private int[] oldPos;

    private BitSet[] outs;

    private BitSet[] ins;

    private List<SConstraint> constraints;

    /**
     * Make a new heuristics
     *
     * @param solver  the solver to use
     * @param actions the actions to consider.
     */
    public PureIncomingFirst(ReconfigurationProblem solver, List<ActionModel> actions, List<SConstraint> costConstraints) {
        super(solver, ActionModels.extractStarts(actions.toArray(new ActionModel[actions.size()])));
        this.pb = solver;
        this.constraints = costConstraints;
        Configuration cfg = solver.getSourceConfiguration();

        hoster = new IntDomainVar[solver.getVirtualMachineActions().size()];
        starts = new IntDomainVar[solver.getVirtualMachineActions().size()];
        List<VirtualMachineActionModel> vmActions = solver.getVirtualMachineActions();
        VirtualMachine[] vms = new VirtualMachine[vmActions.size()];
        oldPos = new int[vms.length];
        outs = new BitSet[solver.getNodes().length];
        ins = new BitSet[solver.getNodes().length];
        for (int i = 0; i < solver.getNodes().length; i++) {
            outs[i] = new BitSet();
            ins[i] = new BitSet();
        }

        for (int i = 0; i < hoster.length; i++) {
            VirtualMachineActionModel action = vmActions.get(i);
            DemandingSlice slice = action.getDemandingSlice();
            if (slice != null) {
                IntDomainVar h = vmActions.get(i).getDemandingSlice().hoster();
                IntDomainVar s = vmActions.get(i).getDemandingSlice().start();
                hoster[i] = h;
                starts[i] = s;
                vms[i] = action.getVirtualMachine();
                Node n = cfg.getLocation(vms[i]);
                if (n == null) {
                    oldPos[i] = -1;
                } else {
                    oldPos[i] = solver.getNode(n);
                    outs[solver.getNode(n)].set(i);     //VM i was on node n
                }
            }
        }
    }

    private boolean first = true;

    private ReconfigurationProblem pb;

    @Override
    public IntDomainVar selectVar() {
        if (first) {
            first = !first;
            Plan.logger.info("Activate cost constraints");
            Plan.logger.info("End:" + pb.getEnd().pretty());
            for (SConstraint sc : constraints) {
                pb.postCut(sc);
            }
            try {
                pb.propagate();
            } catch (ContradictionException e) {
                e.printStackTrace();
                //   Plan.logger.error(e.getMessage(), e);
                pb.setFeasible(false);
                pb.post(Constant.FALSE);
            }
        }
        for (int i = 0; i < ins.length; i++) {
            ins[i].clear();
        }

        BitSet stays = new BitSet();
        //At this moment, all the hoster of the demanding slices are computed.
        //for each node, we compute the number of incoming and outgoing
        for (int i = 0; i < hoster.length; i++) {
            if (hoster[i] != null && hoster[i].isInstantiated()) {
                int newPos = hoster[i].getVal();
                if (oldPos[i] != -1 && newPos != oldPos[i]) {
                    //The VM has move                    
                    ins[newPos].set(i);
                } else if (oldPos[i] != -1 && newPos == oldPos[i]) {
                    stays.set(i);
                }
            }
        }

        //TODO: start with nodes with a sufficient amount of free resources at startup
        for (int x = 0; x < outs.length; x++) {
            if (outs[x].cardinality() == 0) { //no outgoing VMs
                BitSet in = ins[x];
                for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
                    if (starts[i] != null && !starts[i].isInstantiated()) {
                        return starts[i];
                    }
                }
            }
        }
        //ChocoLogging.getBranchingLogger().finest("No more pure incoming");
        //TODO: Decreasing stay at end
        //TODO: association between slice on the same node
        for (int i = stays.nextSetBit(0); i >= 0; i = stays.nextSetBit(i + 1)) {
            if (starts[i] != null && !starts[i].isInstantiated()) {
                return starts[i];
            }
        }
        for (int x = 0; x < outs.length; x++) {
            BitSet in = ins[x];
            //For all the incoming
            for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
                if (starts[i] != null && !starts[i].isInstantiated()) {
                    return starts[i];
                }
            }
        }

        for (int i = 0; i < starts.length; i++) {
            IntDomainVar start = starts[i];
            if (starts[i] != null && !start.isInstantiated()) {
                return start;
            }
        }
        ChocoLogging.getBranchingLogger().finest("No more variables to instantiate here");
        return null;
    }
}
