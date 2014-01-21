/*
 * Copyright (c) 2010 Fabien Hermenier
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

package entropy.vjob;

import java.util.ArrayList;
import java.util.List;

import choco.cp.solver.constraints.global.AmongGAC;
import choco.cp.solver.constraints.global.Occurrence;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.VirtualMachineActionModel;
import entropy.plan.choco.actionModel.slice.DemandingSlice;
import entropy.plan.choco.actionModel.slice.Slices;
import gnu.trove.TIntHashSet;

/**
 * A constraint to restrict the number of virtual machines a set
 * of nodes can host simultaneously.
 * <p/>
 * TODO: Improve the parent API for a better integration of constraints only focused on nodes
 * TODO: create a builder.
 * TODO: test
 *
 * @author Fabien Hermenier
 */
public class Capacity implements PlacementConstraint {
    private int max;

    private VJobSet<Node> nodes;

    private final static ExplodedSet<VirtualMachine> emptyVMs = new ExplodedSet<VirtualMachine>();

    /**
     * Make a new constraint.
     *
     * @param ns the nodes to consider
     * @param m  the maximum hosting capacity of all the nodes.
     */
    public Capacity(VJobSet<Node> ns, int m) {
        max = m;
        nodes = ns;
    }

    @Override
    public String toString() {
        return "capacity(" + nodes.pretty() + ", " + max + ")";
    }

    @Override
    public void inject(ReconfigurationProblem core) {

        ManagedElementSet<Node> onlines = new SimpleManagedElementSet<Node>();
        for (Node n : nodes) {
            if (core.getFutureOnlines().contains(n)) {
                onlines.add(n);
            }
        }

        TIntHashSet involved = new TIntHashSet();
        int[] nIdxs = new int[onlines.size()];
        int i = 0;
        int minIdx = Integer.MAX_VALUE;
        int maxIdx = -1;
        for (Node n : onlines) {
            int idx = core.getNode(n);
            nIdxs[i++] = idx;
            if (idx > maxIdx) {
                maxIdx = idx;
            }
            if (idx < minIdx) {
                minIdx = idx;
            }
            involved.add(idx);
        }

        //TODO: no need to have constraints if max > the amount of future running VMs
        //TODO: a false() constraint if the given nodes are all the future online nodes
        //      while max < the amount of future running VMs

        if (max == 0 && !onlines.isEmpty()) {
            //max == 0, so we directly remove the nodes
            // from the VMs d-slices hoster variable.
            for (VirtualMachineActionModel a : core.getVirtualMachineActions()) {
                DemandingSlice dSlice = a.getDemandingSlice();
                if (dSlice != null) {
                    for (int x = 0; i < nIdxs.length; x++) {
                        try {
                            dSlice.hoster().remVal(nIdxs[x]);
                        } catch (ContradictionException e) {
                            VJob.logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        } else if (onlines.size() > 1) {   //More than one node, so we restrict the sum of sets cardinality
            IntDomainVar card = core.createBoundIntVar("c", 0, max);
            List<IntDomainVar> toWatch = new ArrayList<IntDomainVar>();
            for (DemandingSlice s : core.getDemandingSlices()) {
                IntDomainVar v = s.hoster();
                if (v.isInstantiated()) {
                    int x = v.getVal();
                    if (x >= minIdx && x <= maxIdx && involved.contains(x)) {
                        toWatch.add(v);
                    }
                } else {
                    toWatch.add(v);
                }
            }
            core.post(new AmongGAC(ArrayUtils.append(toWatch.toArray(new IntDomainVar[toWatch.size()]), new IntDomainVar[]{card}), nIdxs, core.getEnvironment()));

            //IntDomainVar[] hs = Slices.extractHosters(core.getDemandingSlices());
            //core.post(new AmongGAC(ArrayUtils.append(hs, new IntDomainVar[]{card}), nIdxs, core.getEnvironment()));
        } else if (!onlines.isEmpty()) {  //One node, only restrict the cardinality of its set model.
            IntDomainVar card = core.createBoundIntVar("c", max, max);
            IntDomainVar[] hs = Slices.extractHosters(core.getDemandingSlices());
            core.post(new Occurrence(ArrayUtils.append(hs, new IntDomainVar[]{card}), nIdxs[0], true, false, core.getEnvironment()));
        }
    }

    /**
     * Check that the nodes does not host a number of VMs greater
     * than the maximum specified
     *
     * @param configuration the configuration to check
     * @return {@code true} if the constraint is satisfied.
     */
    @Override
    public boolean isSatisfied(Configuration configuration) {
        int nb = 0;
        for (Node n : nodes.getElements()) {
            nb += configuration.getRunnings(n).size();
        }
        if (nb > max) {
            VJob.logger.debug(nodes.pretty() + " host " + nb + " virtual machinew but maximum allowed is " + max);
            return false;
        }
        return true;
    }

    @Override
    public ExplodedSet<VirtualMachine> getAllVirtualMachines() {
        return emptyVMs;
    }

    /**
     * If the amount of VMs exceed its capacity, it returns all the hosted VMs
     *
     * @param configuration the configuration to check
     * @return a set of virtual machines that may be empty
     */
    @Override
    public ExplodedSet<VirtualMachine> getMisPlaced(Configuration configuration) {
        ExplodedSet<VirtualMachine> bad = new ExplodedSet<VirtualMachine>();
        int nb = 0;
        for (Node n : nodes.getElements()) {
            ManagedElementSet<VirtualMachine> vms = configuration.getRunnings(n);
            nb += vms.size();
            bad.addAll(vms); // just in case to avoid a double loop
        }
        if (nb < getMaximumCapacity()) {
            bad.clear(); //Its clean, so no VMs are misplaced
        }
        return bad;
    }

    /**
     * Get the nodes involved in the constraint.
     *
     * @return a set of nodes. Should not be empty
     */
    @Override
	public ExplodedSet<Node> getNodes() {
        return this.nodes.flatten();
    }

    /**
     * Get the maximum number of virtual machines
     * the set of nodes can host simultaneously
     *
     * @return a positive integer
     */
    public int getMaximumCapacity() {
        return this.max;
    }
}
