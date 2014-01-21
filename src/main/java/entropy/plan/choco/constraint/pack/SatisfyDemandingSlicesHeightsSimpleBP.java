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

package entropy.plan.choco.constraint.pack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;
import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.Plan;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.slice.DemandingSlice;
import entropy.plan.choco.actionModel.slice.Slice;
import entropy.plan.choco.actionModel.slice.SliceComparator;
import entropy.vjob.ExplodedSet;

/**
 * A constraint to assign a host with a sufficient amount of resources to satisfy
 * all the heights of the demanding slices.
 * The constraint is based on two dynamic bin packing constraints.
 *
 * @author Fabien Hermenier
 */
public class SatisfyDemandingSlicesHeightsSimpleBP implements SatisfyDemandingSliceHeights {

    private SetVariable[] bins;

    private Map<IntegerVariable, Integer> idxVM2Hoster = new HashMap<IntegerVariable, Integer>();

    private SimpleBinPacking cPack;

    private SimpleBinPacking mPack;

    public SatisfyDemandingSlicesHeightsSimpleBP() {

    }

    public SetVariable getBin(int idx) {
        return bins[idx];
    }

    public int getHosterBinIndex(IntegerVariable v) {
        return idxVM2Hoster.get(v);
    }

    @Override
    public void add(ReconfigurationProblem rp) {
        //SetVar []bins = new SetVar[rp.getNodes().length];
        List<DemandingSlice> demandingCPU = new ArrayList<DemandingSlice>(rp.getDemandingSlices());
        List<DemandingSlice> demandingMem = new ArrayList<DemandingSlice>(rp.getDemandingSlices());

        //Remove slices with an height = 0
        for (ListIterator<DemandingSlice> ite = demandingCPU.listIterator(); ite.hasNext(); ) {
            DemandingSlice d = ite.next();
            if (d.getCPUheight() == 0) {
                ite.remove();
            }
        }

        for (ListIterator<DemandingSlice> ite = demandingMem.listIterator(); ite.hasNext(); ) {
            DemandingSlice d = ite.next();
            if (d.getMemoryheight() == 0) {
                ite.remove();
            }
        }

        new LinkedList<String>();
        //ManagedElementSet<Node> ns = cfg.getAllNodes();
        //Node[] ns = model.getNodes();
        Node[] ns = rp.getNodes();
        if (demandingCPU.size() != 0) {
            List<IntDomainVar> demandCPU = new ArrayList<IntDomainVar>();
            List<IntDomainVar> assignsCPU = new ArrayList<IntDomainVar>();

            SetVar[] sets = new SetVar[ns.length];
            IntDomainVar[] capaCPU = new IntDomainVar[sets.length];
            for (int i = 0; i < ns.length; i++) {
                //if (model.getSourceConfiguration().isOnline(ns[i])) {
                capaCPU[i] = rp.getFreeCPU(ns[i]);
                sets[i] = rp.createEnumSetVar("slicesCPU(" + ns[i].getName() + ")", 0, demandingCPU.size() - 1);
                /*} else {
                    capaCPU[i] = constant(0);
                    sets[i] = emptySet();//makeSetVar("slicesCPU(" + n.getName() + ")", new int[]{});
                } */
                //bins = sets;
            }

            //Sort in descending order
            Collections.sort(demandingCPU, new SliceComparator(false, SliceComparator.ResourceType.cpuConsumption));
            for (int i = 0; i < demandingCPU.size(); i++) {
                demandCPU.add(rp.createIntegerConstant(i + " #dCPU", demandingCPU.get(i).getCPUheight()));
                assignsCPU.add(demandingCPU.get(i).hoster());
                //    this.idxVM2Hoster.put(assignsCPU.get(i), i);
            }
            IntDomainVar[] demands = demandCPU.toArray(new IntDomainVar[demandCPU.size()]);
            //Plan.logger.debug("Pack \n\tcapa:" + Arrays.toString(capaCPU) + "\n\tdemand: " + demandCPU + "\n\tassigns" + assignsCPU);

            IntDomainVar nbNonEmpty = rp.createBoundIntVar("non-empty", 0, sets.length);
            cPack = new SimpleBinPacking(rp.getEnvironment(),
                    sets,
                    capaCPU,
                    demands,
                    assignsCPU.toArray(new IntDomainVar[assignsCPU.size()]),

                    nbNonEmpty);
            //cPack.readOptions(opts);
            //TODO: options
            rp.post(cPack);
        }

        //opts.add(SimpleBinPacking.ADDITIONAL_RULES.getOption());
        if (demandingMem.size() != 0) {
            List<IntDomainVar> demandMem = new ArrayList<IntDomainVar>();
            List<IntDomainVar> assignsMem = new ArrayList<IntDomainVar>();
            SetVar[] sets = new SetVar[ns.length];
            IntDomainVar[] capaMem = new IntDomainVar[sets.length];
            for (int i = 0; i < ns.length; i++) {
                capaMem[i] = rp.getFreeMem(ns[i]);
                sets[i] = rp.createEnumSetVar("slicesMem(" + ns[i].getName() + ")", 0, demandingMem.size() - 1);
            }

            Collections.sort(demandingMem, new SliceComparator(false, SliceComparator.ResourceType.memoryConsumption));
            for (Slice task : demandingMem) {
                demandMem.add(rp.createIntegerConstant(task.getName() + "#dMem", task.getMemoryheight()));
                assignsMem.add(task.hoster());
            }
            IntDomainVar[] demands = demandMem.toArray(new IntDomainVar[demandMem.size()]);
            //Plan.logger.debug("Pack \n\tcapa:" + Arrays.toString(capaMem) + "\n\tdemand: " + demandMem + "\n\tassigns" + assignsMem);
            IntDomainVar nbNonEmpty = rp.createBoundIntVar("non-empty", 0, sets.length);


            mPack = new SimpleBinPacking(rp.getEnvironment(),
                    sets,
                    capaMem,
                    demands,
                    assignsMem.toArray(new IntDomainVar[assignsMem.size()]),
                    nbNonEmpty);


            //mPack.readOptions(opts);
            rp.post(mPack);
        }
        Plan.logger.debug("SatisfyDemandingSlicesHeightsSimpleBP branched");
    }

    @Override
    public CustomPack getCoreCPUPacking() {
        return cPack;
    }

    @Override
    public CustomPack getCoreMemPacking() {
        return mPack;
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
        return cPack == null ? 0 : cPack.getRemainingSpace(bin);
    }

    @Override
    public int getRemainingMemory(int bin) {
        return mPack == null ? 0 : mPack.getRemainingSpace(bin);
    }
}
