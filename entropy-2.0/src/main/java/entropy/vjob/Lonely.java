/*
 * Copyright (c) Fabien Hermenier
 *
 *        This file is part of Entropy.
 *
 *        Entropy is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU Lesser General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        Entropy is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU Lesser General Public License for more details.
 *
 *        You should have received a copy of the GNU Lesser General Public License
 *        along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.vjob;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import choco.cp.solver.constraints.set.MemberXY;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;
import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.Plan;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.ActionModels;
import entropy.plan.choco.actionModel.slice.DemandingSlice;
import entropy.plan.choco.actionModel.slice.Slice;
import entropy.plan.choco.actionModel.slice.Slices;

/**
 * A placement constraint to ensure the given set of VMs will not be hosted
 * on nodes that host other VMs
 *
 * @author Fabien Hermenier
 */
public class Lonely implements PlacementConstraint {

    private VJobSet<VirtualMachine> vms;

    public Lonely(VJobSet<VirtualMachine> vms) {
        this.vms = vms;
    }

    @Override
    public void inject(ReconfigurationProblem core) {

        //Remove non future-running VMs
        ManagedElementSet<VirtualMachine> goods = vms.getElements().clone();
        goods.retainAll(core.getFutureRunnings());

        //Two set variables. One denotes the nodes hosting the VMs, the other, the nodes hosting the other VMs.
        SetVar myNodes = core.createEnumSetVar("nodes4(" + vms + ")", 0, core.getNodes().length - 1);
        SetVar otherNodes = core.createEnumSetVar("nodes!4(" + vms + ")", 0, core.getNodes().length - 1);

        ManagedElementSet<VirtualMachine> otherVMs = core.getFutureRunnings().clone();
        otherVMs.removeAll(goods);


        //Link the assignment variables with the set
        //TODO: propose a MemberXY() constraints that takes an array of integer variables to improve performance
        List<DemandingSlice> myDSlices = ActionModels.extractDemandingSlices(core.getAssociatedActions(goods));
        IntDomainVar[] myAssigns = Slices.extractHosters(myDSlices.toArray(new Slice[myDSlices.size()]));

        List<DemandingSlice> otherDSlices = ActionModels.extractDemandingSlices(core.getAssociatedActions(otherVMs));
        IntDomainVar[] otherAssigns = Slices.extractHosters(otherDSlices.toArray(new Slice[otherDSlices.size()]));

        for (IntDomainVar v : otherAssigns) {
            if (v.isInstantiated()) {
                try {
                    otherNodes.addToKernel(v.getVal(), null, false);
                } catch (ContradictionException e) {
                    Plan.logger.error(e.getMessage());
                }
            } else {
                core.post(new MemberXY(otherNodes, v));
            }
        }

        for (IntDomainVar v : myAssigns) {
            if (v.isInstantiated()) {
                try {
                    myNodes.addToKernel(v.getVal(), null, false);
                } catch (ContradictionException e) {
                    Plan.logger.error(e.getMessage());
                }
            } else {
                core.post(new MemberXY(myNodes, v));
            }
        }
        core.post(new choco.cp.solver.constraints.set.Disjoint(myNodes, otherNodes));

//        core.post(new Disjoint(core.getEnvironment(), myAssigns, otherAssigns, core.getNodes().length + 1));
    }

    @Override
    public boolean isSatisfied(Configuration cfg) {
        Set<Node> s1 = new HashSet<Node>();
        for (VirtualMachine vm : vms) {
            if (cfg.isRunning(vm)) {
                s1.add(cfg.getLocation(vm));
            }
        }
        //If one of the other VMs is running into the nodes, then fail
        for (VirtualMachine vm : cfg.getRunnings()) {
            if (!vms.contains(vm) && s1.contains(cfg.getLocation(vm))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ExplodedSet<VirtualMachine> getAllVirtualMachines() {
        return vms.flatten();
    }

    @Override
    public ExplodedSet<Node> getNodes() {
        return new ExplodedSet<Node>();
    }

    /**
     * If the constraint is not satisfied, then misplaced VMs are those given
     * as a parameter that share nodes with other VMs.
     *
     * @param cfg the configuration
     * @return a set of virtual machines that may be empty
     */
    @Override
    public ExplodedSet<VirtualMachine> getMisPlaced(Configuration cfg) {
        ExplodedSet<VirtualMachine> bad = new ExplodedSet<VirtualMachine>();
        Set<Node> s1 = new HashSet<Node>();
        for (VirtualMachine vm : vms) {
            if (cfg.isRunning(vm)) {
                s1.add(cfg.getLocation(vm));
            }
        }
        //If one of the other VMs is running into the nodes, then fail
        for (VirtualMachine vm : cfg.getRunnings()) {
            if (!vms.contains(vm)) {
                Node n = cfg.getLocation(vm);
                if (s1.contains(n)) {         //Other VMs share reserved nodes
                    for (VirtualMachine vm2 : cfg.getRunnings(n)) {
                        if (vms.contains(vm2)) {
                            bad.add(vm2);
                        }
                    }
                }
            }
        }
        return bad;
    }

    @Override
    public String toString() {
        return new StringBuilder("lonely(").append(vms).append(")").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lonely lonely = (Lonely) o;
        return lonely.equals(vms);
    }

    @Override
    public int hashCode() {
        return vms.hashCode();
    }
}
