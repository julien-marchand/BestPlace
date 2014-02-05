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

package gipad.placementconstraint;
import org.discovery.DiscoveryModel.model.Node;
import org.discovery.DiscoveryModel.model.VirtualMachine;

import solver.constraints.set.SCF;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.VF;
import gipad.configuration.ManagedElementList;
import gipad.configuration.SimpleManagedElementList;
import gipad.configuration.configuration.Configuration;
import gipad.plan.choco.ReconfigurationProblem;
import gipad.plan.choco.actionmodel.ActionModel;
import gipad.plan.choco.actionmodel.slice.DemandingSlice;
import gipad.plan.choco.actionmodel.slice.Slice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A placement constraint to ensure the given set of VMs will not be hosted
 * on nodes that host other VMs
 *
 * @author Fabien Hermenier
 */
public class Lonely implements PlacementConstraint {

    private ManagedElementList<VirtualMachine> vms;

    public Lonely(ManagedElementList<VirtualMachine> vms) {
        this.vms = vms;
    }

    @Override
    public void inject(ReconfigurationProblem core) {

        //Remove non future-running VMs
	ManagedElementList<VirtualMachine> goods = vms.clone();
        goods.retainAll(core.getFutureRunnings());

        //Two set variables. One denotes the nodes hosting the VMs, the other, the nodes hosting the other VMs.
        SetVar myNodes = VF.set("nodes4(" + vms + ")", 0, core.getNodes().length - 1, core.getSolver());//core.createEnumSetVar("nodes4(" + vms + ")", 0, core.getNodes().length - 1);
        SetVar otherNodes = VF.set("nodes!4(" + vms + ")", 0, core.getNodes().length - 1, core.getSolver());// core.createEnumSetVar("nodes!4(" + vms + ")", 0, core.getNodes().length - 1);

        ManagedElementList<VirtualMachine> otherVMs = core.getFutureRunnings().clone();
        otherVMs.removeAll(goods);


        //Link the assignment variables with the set
        //TODO: propose a MemberXY() constraints that takes an array of integer variables to improve performance
        List<DemandingSlice> myDSlices = extractDemandingSlices(core.getAssociatedActions(goods));
        IntVar<?>[] myAssigns = extractHosters(myDSlices.toArray(new Slice[myDSlices.size()]));

        List<DemandingSlice> otherDSlices = extractDemandingSlices(core.getAssociatedActions(otherVMs));
        IntVar<?>[] otherAssigns = extractHosters(otherDSlices.toArray(new Slice[otherDSlices.size()]));

        for (IntVar<?> v : otherAssigns) {
            if (v.instantiated()) {
                try {
                    otherNodes.addToKernel(v.getValue(), null);
                } catch (ContradictionException e) {
                   //FIXME  Plan.logger.error(e.getMessage());
                }
            } else {
        	core.getSolver().post(SCF.member(v, otherNodes));
            }
        }

        for (IntVar<?> v : myAssigns) {
            if (v.instantiated()) {
                try {
                    myNodes.addToKernel(v.getValue(), null);
                } catch (ContradictionException e) {
                    //FIXME debug log Plan.logger.error(e.getMessage());
                }
            } else {
                core.getSolver().post(SCF.member(v,myNodes));
            }
        }
        core.getSolver().post(SCF.disjoint(myNodes, otherNodes));

//        core.post(new Disjoint(core.getEnvironment(), myAssigns, otherAssigns, core.getNodes().length + 1));
    }
    
    /**
     * Extract all the demanding slices of a list of actions.
     *
     * @param actions the list of action
     * @return a list of demanding slice. May be empty
     */
    public static List<DemandingSlice> extractDemandingSlices(List<? extends ActionModel> actions) {
        List<DemandingSlice> slices = new ArrayList<DemandingSlice>();
        for (ActionModel a : actions) {
            if (a.getDemandingSlice() != null) {
                slices.add(a.getDemandingSlice());
            }
        }
        return slices;
    }
    
    /**
     * Extract all the hosters of an array of slices.
     *
     * @param slices the slices to consider
     * @return an array of assignement var, in an order similar to slices
     */
    public static IntVar<?>[] extractHosters(Slice[] slices) {
        IntVar<?>[] l = new IntVar[slices.length];
        for (int i = 0; i < slices.length; i++) {
            l[i] = slices[i].hoster();
        }
        return l;
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
    public ManagedElementList<VirtualMachine> getAllVirtualMachines() {
        return (ManagedElementList<VirtualMachine>) vms;
    }

    @Override
    public ManagedElementList<Node> getNodes() {
        return new SimpleManagedElementList<Node>();
    }

    /**
     * If the constraint is not satisfied, then misplaced VMs are those given
     * as a parameter that share nodes with other VMs.
     *
     * @param cfg the configuration
     * @return a set of virtual machines that may be empty
     */
    @Override
    public ManagedElementList<VirtualMachine> getMisPlaced(Configuration cfg) {
	ManagedElementList<VirtualMachine> bad = new SimpleManagedElementList<VirtualMachine>();
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
