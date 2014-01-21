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

package entropy.plan.choco.constraint.pack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.Plan;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.slice.DemandingSlice;
import entropy.plan.choco.actionModel.slice.SliceComparator;
import entropy.vjob.ExplodedSet;

/**
 * A constraint to assign a host with a sufficient amount of resources to satisfy
 * all the heights of the demanding slices.
 * The constraint is based on two dynamic bin packing constraints.
 *
 * @author Fabien Hermenier
 */
public class SatisfyDemandingSlicesHeightsFastMultiDimensionBP implements SatisfyDemandingSliceHeights {

    private FastMultiBinPacking pack;

    public SatisfyDemandingSlicesHeightsFastMultiDimensionBP() {

    }

    @Override
    public void add(ReconfigurationProblem rp) {
        List<DemandingSlice> dSlices = new ArrayList<DemandingSlice>(rp.getDemandingSlices());

        Collections.sort(dSlices, new SliceComparator(false, SliceComparator.ResourceType.cpuConsumption));

        int[][] sizes = new int[2][];
        sizes[0] = new int[dSlices.size()];
        sizes[1] = new int[dSlices.size()];

        IntDomainVar[] assigns = new IntDomainVar[dSlices.size()];
        for (int i = 0; i < dSlices.size(); i++) {
            sizes[0][i] = dSlices.get(i).getCPUheight();
            sizes[1][i] = dSlices.get(i).getMemoryheight();
            assigns[i] = dSlices.get(i).hoster();
        }

        Node[] ns = rp.getNodes();
        IntDomainVar[][] capas = new IntDomainVar[2][];
        capas[0] = new IntDomainVar[ns.length];
        capas[1] = new IntDomainVar[ns.length];

        for (int i = 0; i < ns.length; i++) {
            capas[0][i] = rp.getFreeCPU(ns[i]);
            capas[1][i] = rp.getFreeMem(ns[i]);
        }

        pack = new FastMultiBinPacking(rp.getEnvironment(), capas, sizes, assigns);
        rp.post(pack);

        Plan.logger.debug("SatisfyDemandingSlicesHeightsCustomBP branched");
    }

    @Override
    public CustomPack getCoreCPUPacking() {
        return null;
    }

    @Override
    public CustomPack getCoreMemPacking() {
        return null;
    }

    @Override
    public boolean isSatisfied(Configuration cfg) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ExplodedSet<VirtualMachine> getAllVirtualMachines() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ExplodedSet<VirtualMachine> getMisPlaced(Configuration cfg) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getRemainingCPU(int bin) {
        return this.pack.getRemainingSpace(0, bin);
    }

    @Override
    public int getRemainingMemory(int bin) {
        return this.pack.getRemainingSpace(1, bin);
    }
}
