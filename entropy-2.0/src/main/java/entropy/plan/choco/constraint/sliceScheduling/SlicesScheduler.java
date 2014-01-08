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

import gnu.trove.TIntIntHashMap;

import java.util.Arrays;
import java.util.BitSet;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.structure.SBitSet;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * @author Fabien Hermenier
 */
public class SlicesScheduler extends AbstractLargeIntSConstraint {

    private LocalScheduler[] scheds;

    private IntDomainVar[] cHosters;

    private IntDomainVar[] cEnds;

    private IntDomainVar[] dHosters;

    private IntDomainVar[] dStarts;

    private int[] capacityCPU;

    private int[] capacityMem;

    private int[] dCPUHeights;

    private int[] dMemHeights;

    private int[] cCPUHeights;

    private int[] cMemHeights;

    private IStateInt toInstantiate;

    private IEnvironment env;

    private IStateBitSet[] ins;

    public SlicesScheduler(IEnvironment env,
                           int[] capacityCPU,
                           int[] capacityMem,
                           IntDomainVar[] cHosters,
                           int[] cCPUHeights,
                           int[] cMemHeights,
                           IntDomainVar[] cEnds,
                           IntDomainVar[] dHosters,
                           int[] dCPUHeights,
                           int[] dMemHeights,
                           IntDomainVar[] dStarts,
                           int[] assocs) {

        super(ArrayUtils.append(dHosters, cHosters, cEnds, dStarts));
        this.env = env;
        this.cHosters = cHosters;
        this.dHosters = dHosters;
        this.cEnds = cEnds;
        this.dStarts = dStarts;
        this.capacityCPU = capacityCPU;
        this.capacityMem = capacityMem;
        this.dCPUHeights = dCPUHeights;
        this.dMemHeights = dMemHeights;

        this.cCPUHeights = cCPUHeights;
        this.cMemHeights = cMemHeights;


        scheds = new LocalScheduler[capacityCPU.length];

        BitSet[] outs = new BitSet[scheds.length];
        for (int i = 0; i < scheds.length; i++) {
            outs[i] = new BitSet(cHosters.length);
            //scheds[i].setOuts(outs[i]);
        }

        for (int i = 0; i < cHosters.length; i++) {
            outs[cHosters[i].getVal()].set(i);
        }


        this.ins = new IStateBitSet[scheds.length];
        for (int i = 0; i < scheds.length; i++) {
            ins[i] = new SBitSet(env, dHosters.length);
            scheds[i] = new LocalScheduler(i, capacityCPU[i], capacityMem[i],
                    cCPUHeights,
                    cMemHeights,
                    cEnds,
                    outs[i],
                    dCPUHeights,
                    dMemHeights,
                    dStarts,
                    ins[i],
                    assocs);
        }
    }

    @Override
    public void awake() throws ContradictionException {

        this.toInstantiate = env.makeInt(dHosters.length);

        //Check whether some hosting variable are already instantiated
        for (int i = 0; i < dHosters.length; i++) {
            if (dHosters[i].isInstantiated()) {
                int nIdx = dHosters[i].getVal();
                toInstantiate.add(-1);
                ins[nIdx].set(i);
            }
        }
    }

    @Override
    public void propagate() throws ContradictionException {
        if (isFull2()) {
            for (int i = 0; i < scheds.length; i++) {
                if (!scheds[i].propagate()) {
                    fail();
                }
            }
        }
    }

    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        if (idx < dHosters.length) {
            toInstantiate.add(-1);
            int nIdx = vars[idx].getVal();
            //ChocoLogging.getSearchLogger().finest(idx + " on " + nIdx);
            ins[nIdx].set(idx);
            //ChocoLogging.getSearchLogger().finest(vars[idx].pretty());
            for (int x = 0; x < scheds.length; x++) {
                if (x != nIdx) {
                    //ChocoLogging.getSearchLogger().finest(x + " not on " + idx);
                    ins[x].clear(idx);
                } else {
                    //ChocoLogging.getSearchLogger().finest(x + " on " + idx);
                    ins[x].set(idx);
                }
            }
        }
        this.constAwake(false);
    }

    private boolean isFull2() {
        return toInstantiate.get() == 0;
    }

    @Override
    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK;
    }

    @Override
    public boolean isSatisfied() {
        int[] vals = new int[vars.length];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = vars[i].getVal();
        }
        return isSatisfied(vals);
    }

    @Override
    public boolean isSatisfied(int[] vals) {
        //Split this use tab to ease the analysis
        int[] dHostersVals = new int[dHosters.length];
        int[] dStartsVals = new int[dStarts.length];
        int[] cHostersVals = new int[cHosters.length];
        int[] cEndsVals = new int[cEnds.length];

        //dHosters, cHosters, cEnds, dStarts
        for (int i = 0; i < dHosters.length; i++) {
            dHostersVals[i] = vals[i];
            dStartsVals[i] = vals[i + dHosters.length + cHosters.length + cEnds.length];
        }

        for (int i = 0; i < cHosters.length; i++) {
            cHostersVals[i] = vals[i + dHosters.length];
            cEndsVals[i] = vals[i + dHosters.length + cHosters.length];
        }


        //A hashmap to save the changes of each node (relatives to the previous moment) in the resources distribution
        TIntIntHashMap[] cpuChanges = new TIntIntHashMap[capacityCPU.length];
        TIntIntHashMap[] memChanges = new TIntIntHashMap[capacityMem.length];
        for (int i = 0; i < capacityMem.length; i++) {
            cpuChanges[i] = new TIntIntHashMap();
            memChanges[i] = new TIntIntHashMap();
        }
        for (int i = 0; i < dHostersVals.length; i++) {
            int nIdx = dHostersVals[i];
            cpuChanges[nIdx].put(dStartsVals[i], cpuChanges[nIdx].get(dStartsVals[i]) - dCPUHeights[i]);
            memChanges[nIdx].put(dStartsVals[i], memChanges[nIdx].get(dStartsVals[i]) - dMemHeights[i]);
        }

        int[] currentFreeCPU = Arrays.copyOf(capacityCPU, capacityCPU.length);
        int[] currentFreeMem = Arrays.copyOf(capacityMem, capacityMem.length);
        for (int i = 0; i < cHostersVals.length; i++) {
            int nIdx = cHostersVals[i];
            cpuChanges[nIdx].put(cEndsVals[i], cpuChanges[nIdx].get(cEndsVals[i]) + cCPUHeights[i]);
            memChanges[nIdx].put(cEndsVals[i], memChanges[nIdx].get(cEndsVals[i]) + cMemHeights[i]);
            currentFreeCPU[nIdx] -= cCPUHeights[i];
            currentFreeMem[nIdx] -= cMemHeights[i];
        }


        for (int x = 0; x < capacityCPU.length; x++) {
            //Now we check the evolution of the absolute free space.
            ChocoLogging.getBranchingLogger().finest("--- " + x + " isSatisfied() ---");
            for (int i = 0; i < cHostersVals.length; i++) {
                ChocoLogging.getBranchingLogger().finest(i + " " + cEnds[i].pretty() + " ends at " + cEndsVals[i]);
            }
            for (int i = 0; i < dHostersVals.length; i++) {
                ChocoLogging.getBranchingLogger().finest(dStarts[i].pretty());
            }
            ChocoLogging.getBranchingLogger().finest(x + " currentFreeCPU=" + currentFreeCPU[x]);
            ChocoLogging.getBranchingLogger().finest(x + " currentFreeMem=" + currentFreeMem[x]);
            ChocoLogging.getBranchingLogger().finest(cpuChanges[x].toString());
            ChocoLogging.getBranchingLogger().finest(memChanges[x].toString());

            for (int i = 0; i < cpuChanges[x].keys().length; i++) {
                currentFreeCPU[x] += cpuChanges[x].get(i);
                currentFreeMem[x] += memChanges[x].get(i);
                if (currentFreeCPU[x] < 0 || currentFreeMem[x] < 0) {
                    ChocoLogging.getMainLogger().severe(x + " at moment " + i + ": freeCPU=" + currentFreeCPU[x] + ", freeMem=" + currentFreeMem[x]);
                    //System.exit(1);
                    return false;
                }
            }
        }
        return true;
    }
}
