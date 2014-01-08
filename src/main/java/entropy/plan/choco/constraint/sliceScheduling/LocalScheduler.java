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

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * @author Fabien Hermenier
 */
public class LocalScheduler {

    private int me;

    /**
     * My CPU capacity.
     */
    private int capacityCPU;

    /**
     * My memory capacity.
     */
    private int capacityMem;

    /**
     * out[i] = true <=> the consuming slice i will leave me.
     */
    private BitSet out;

    /**
     * The moment the consuming slices ends. Same order as the hosting variables.
     */
    private IntDomainVar[] cEnds;

    /**
     * The CPU height for each consuming slice. Same order as the hosting variables.
     */
    private int[] cCPUHeights;

    /**
     * The Memory height for each consuming slice. Same order as the hosting variables.
     */
    private int[] cMemHeights;

    //Demanding slice part
    /**
     * in[i] = true <=> the demanding slice i will come to me.
     */
    private IStateBitSet in;

    /*
     * The moment the demanding slices ends. Same order as the hosting variables.
     */
    private IntDomainVar[] dStarts;

    /**
     * The CPU height for each demanding slice. Same order as the hosting variable.
     */
    private int[] dCPUHeights;

    /**
     * The memory heighr for each demanding slice. Same order as the hosting variable.
     */
    private int[] dMemHeights;

    /**
     * The amount of free memory at startup.
     */
    private int startupFreeMem;

    /**
     * The amount of free CPU at startup.
     */
    private int startupFreeCPU;

    private static final int DEBUG = -1;

    private int[] associations;

    private int[] revAssociations;

    public static final int NO_ASSOCIATIONS = -1;

    private TIntIntHashMap profileMinCPU = new TIntIntHashMap();

    private TIntIntHashMap profileMinMem = new TIntIntHashMap();

    private int[] sortedMinProfile;

    private TIntIntHashMap profileMaxCPU = new TIntIntHashMap();

    private TIntIntHashMap profileMaxMem = new TIntIntHashMap();

    private int[] sortedMaxProfile;

    public LocalScheduler(int me,
                          int capacityCPU,
                          int capacityMem,
                          int[] cCPUHeights,
                          int[] cMemHeights,
                          IntDomainVar[] cEnds,
                          BitSet outs,
                          int[] dCPUHeights,
                          int[] dMemHeights,
                          IntDomainVar[] dStarts,
                          IStateBitSet in,
                          int[] assocs) {
        this.associations = assocs;
        this.me = me;
        this.capacityCPU = capacityCPU;
        this.capacityMem = capacityMem;
        this.cEnds = cEnds;
        this.cCPUHeights = cCPUHeights;
        this.cMemHeights = cMemHeights;

        this.dStarts = dStarts;
        this.dCPUHeights = dCPUHeights;
        this.dMemHeights = dMemHeights;
        this.in = in;
        this.out = outs;
        revAssociations = new int[cCPUHeights.length];
        for (int i = 0; i < revAssociations.length; i++) {
            revAssociations[i] = NO_ASSOCIATIONS;
        }

        for (int i = 0; i < associations.length; i++) {
            if (associations[i] != NO_ASSOCIATIONS) {
                revAssociations[associations[i]] = i;
            }
        }

        //The amount of free resources at startup
        startupFreeMem = capacityMem;
        startupFreeCPU = capacityCPU;

        for (int j = out.nextSetBit(0); j >= 0; j = out.nextSetBit(j + 1)) {
            startupFreeCPU -= cCPUHeights[j];
            startupFreeMem -= cMemHeights[j];
        }
    }

    public boolean propagate() throws ContradictionException {
        computeProfiles();
        if (!checkInvariant()) {
            return false;
        }
        updateDStartsSup();
        updateCEndsSup();
        updateDStartsInf();
        return true;
    }

    /**
     * Translation for a relatives resources changes to an absolute free resources.
     *
     * @param changes       the map that indicates the free CPU variation
     * @param sortedMoments the different moments sorted in ascending order
     */
    private void toAbsoluteFreeResources(TIntIntHashMap changes, int[] sortedMoments) {
        for (int i = 1; i < sortedMoments.length; i++) {
            int t = sortedMoments[i];
            int lastT = sortedMoments[i - 1];
            int lastFree = changes.get(lastT);

            changes.put(t, changes.get(t) + lastFree);
        }
    }

    private void computeProfiles() {
        //Sur de ce qui est utilise sur la ressource
        profileMinCPU.clear();
        profileMinMem.clear();

        //Maximum simultanee dans le pire des cas sur la ressource
        profileMaxCPU.clear();
        profileMaxMem.clear();


        profileMinCPU.put(0, capacityCPU - startupFreeCPU);
        profileMaxCPU.put(0, capacityCPU - startupFreeCPU);
        profileMinMem.put(0, capacityMem - startupFreeMem);
        profileMaxMem.put(0, capacityMem - startupFreeMem);
        for (int i = out.nextSetBit(0); i >= 0; i = out.nextSetBit(i + 1)) {
            int t = cEnds[i].getInf();
            if (associatedToDSliceOnCurrentNode(i) &&
                    dCPUHeights[revAssociations[i]] > cCPUHeights[i]) {
                if (me == DEBUG) {
                    ChocoLogging.getBranchingLogger().finest(me + " " + cEnds[i].pretty() + " increasing");
                }
                profileMaxCPU.put(t, profileMaxCPU.get(t) - cCPUHeights[i]);
                profileMaxMem.put(t, profileMaxMem.get(t) - cMemHeights[i]);
            } else {
                if (me == DEBUG) {
                    ChocoLogging.getBranchingLogger().finest(me + " " + cEnds[i].pretty() + " decreasing or non-associated (" + (revAssociations[i] >= 0 ? dStarts[revAssociations[i]].pretty() : "no rev") + "?)");
                }
                profileMinCPU.put(t, profileMinCPU.get(t) - cCPUHeights[i]);
                profileMinMem.put(t, profileMinMem.get(t) - cMemHeights[i]);
            }

            t = cEnds[i].getSup();
            if (associatedToDSliceOnCurrentNode(i) &&
                    dCPUHeights[revAssociations[i]] > cCPUHeights[i]) {
                profileMinCPU.put(t, profileMinCPU.get(t) - cCPUHeights[i]);
                profileMinMem.put(t, profileMinMem.get(t) - cMemHeights[i]);
            } else {
                profileMaxCPU.put(t, profileMaxCPU.get(t) - cCPUHeights[i]);
                profileMaxMem.put(t, profileMaxMem.get(t) - cMemHeights[i]);
            }
        }

        for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
            int t = dStarts[i].getSup();
            profileMinCPU.put(t, profileMinCPU.get(t) + dCPUHeights[i]);
            profileMinMem.put(t, profileMinMem.get(t) + dMemHeights[i]);

            t = dStarts[i].getInf();
            profileMaxCPU.put(t, profileMaxCPU.get(t) + dCPUHeights[i]);
            profileMaxMem.put(t, profileMaxMem.get(t) + dMemHeights[i]);
        }

        //Now transforms into an absolute profile
        sortedMinProfile = null;
        sortedMinProfile = profileMinCPU.keys();
        Arrays.sort(sortedMinProfile);

        sortedMaxProfile = null;
        sortedMaxProfile = profileMaxCPU.keys();
        profileMaxCPU.keys(sortedMaxProfile);
        Arrays.sort(sortedMaxProfile);

        toAbsoluteFreeResources(profileMinCPU, sortedMinProfile);
        toAbsoluteFreeResources(profileMinMem, sortedMinProfile);
        toAbsoluteFreeResources(profileMaxCPU, sortedMaxProfile);
        toAbsoluteFreeResources(profileMaxMem, sortedMaxProfile);

        if (me == DEBUG) {
            ChocoLogging.getBranchingLogger().finest("---" + me + "--- startup=(" + startupFreeCPU + "; " + startupFreeMem + ") init=(" + capacityCPU + "; " + capacityMem + ")");
            for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
                ChocoLogging.getBranchingLogger().finest((dStarts[i].isInstantiated() ? "!" : "?") + " " + dStarts[i].pretty() + " " + dCPUHeights[i] + " " + dMemHeights[i]);
            }

            for (int i = out.nextSetBit(0); i >= 0; i = out.nextSetBit(i + 1)) {
                ChocoLogging.getBranchingLogger().finest((cEnds[i].isInstantiated() ? "!" : "?") + " " + cEnds[i].pretty() + " " + cCPUHeights[i] + " " + cMemHeights[i]);
            }
            ChocoLogging.getBranchingLogger().finest("---");


            ChocoLogging.getBranchingLogger().finest("profileMin=" + prettyProfile(sortedMinProfile, profileMinCPU, profileMinMem));
            ChocoLogging.getBranchingLogger().finest("profileMax=" + prettyProfile(sortedMaxProfile, profileMaxCPU, profileMaxMem));
        }
    }

    private boolean associatedToDSliceOnCurrentNode(int cSlice) {
        if (revAssociations[cSlice] != NO_ASSOCIATIONS
                && in.get(revAssociations[cSlice])) {
            if (me == DEBUG) {
                ChocoLogging.getBranchingLogger().finest(me + " " + cEnds[cSlice].getName() + " with " + dStarts[revAssociations[cSlice]]);
            }
            return true;
        }
        return false;
    }

    private boolean associatedToCSliceOnCurrentNode(int dSlice) {
        if (associations[dSlice] != NO_ASSOCIATIONS
                && out.get(associations[dSlice])) {
            if (me == DEBUG) {
                ChocoLogging.getBranchingLogger().finest(me + " " + dStarts[dSlice].getName() + " with " + cEnds[associations[dSlice]]);
            }
            return true;
        }
        return false;
    }

    private String prettyProfile(int[] ascMoments, TIntIntHashMap cpuProfile, TIntIntHashMap memProfile) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < ascMoments.length; i++) {
            int t = ascMoments[i];
            b.append(t);
            b.append(":(");
            b.append(cpuProfile.get(t));
            b.append(",");
            b.append(memProfile.get(t));
            b.append(")");
            if (i != ascMoments.length - 1) {
                b.append(" ");
            }
        }
        return b.toString();
    }

    private boolean checkInvariant() throws ContradictionException {
        for (int i = 0; i < sortedMinProfile.length; i++) {
            int t = sortedMinProfile[i];
            if (profileMinCPU.get(t) > capacityCPU || profileMinMem.get(t) > capacityMem) {
                if (me == DEBUG) {
                    ChocoLogging.getBranchingLogger().warning(me + ": Invalid profile at moment " + t + " - " + prettyProfile(sortedMinProfile, profileMinCPU, profileMinMem));
                }
                return false;
            }
        }
        return true;
    }

    private void updateDStartsInf() throws ContradictionException {
        for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
            if (!dStarts[i].isInstantiated() && !associatedToCSliceOnCurrentNode(i)) {
                int dCpu = dCPUHeights[i];
                int dMem = dMemHeights[i];

                int lastT = -1;
                for (int x = sortedMinProfile.length - 1; x >= 0; x--) {
                    int t = sortedMinProfile[x];
                    if (t <= dStarts[i].getInf()) {
                        break;
                    }
                    int prevT = sortedMinProfile[x - 1];
                    if (t <= dStarts[i].getSup()
                            && (profileMinCPU.get(prevT) + dCpu > capacityCPU || profileMinMem.get(prevT) + dMem > capacityMem)) {
                        lastT = t;
                        break;
                    }
                }
                if (lastT != -1) {
                    if (me == DEBUG) {
                        ChocoLogging.getBranchingLogger().finest(me + ": " + dStarts[i].pretty() + " lb =" + lastT);
                    }
                    dStarts[i].setInf(lastT);
                }
            }
        }
    }

    private void updateDStartsSup() throws ContradictionException {

        int lastSup = -1;
        for (int i = sortedMaxProfile.length - 1; i >= 0; i--) {
            int t = sortedMaxProfile[i];
            if (profileMaxCPU.get(t) <= capacityCPU && profileMaxMem.get(t) <= capacityMem) {
                lastSup = t;
            } else {
                break;
            }
        }
        if (me == DEBUG) {
            ChocoLogging.getBranchingLogger().finest(me + ": lastSup=" + lastSup);
        }
        if (lastSup != -1) {
            for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
                if (!dStarts[i].isInstantiated() && !associatedToCSliceOnCurrentNode(i) && dStarts[i].getSup() > lastSup) {
                    int s = Math.max(dStarts[i].getInf(), lastSup);
                    if (me == DEBUG) {
                        ChocoLogging.getBranchingLogger().finest(me + ": " + dStarts[i].pretty() + " ub=" + s + ");");
                    }
                    dStarts[i].setSup(s);
                }
            }
        }
    }

    private void updateCEndsSup() throws ContradictionException {
        for (int i = out.nextSetBit(0); i >= 0; i = out.nextSetBit(i + 1)) {
            if (!cEnds[i].isInstantiated() && !associatedToDSliceOnCurrentNode(i)) {
                int cCpu = cCPUHeights[i];
                int cMem = cMemHeights[i];
                int lastT = -1;
                for (int x = 0; x < sortedMinProfile.length; x++) {
                    int t = sortedMinProfile[x];
                    if (t >= cEnds[i].getSup()) {
                        break;
                    } else if (t >= cEnds[i].getInf() && (profileMinCPU.get(t) + cCpu > capacityCPU
                            || profileMinMem.get(t) + cMem > capacityMem)) {
                        lastT = t;
                        break;
                    }
                }
                if (lastT != -1) {
                    if (me == DEBUG) {
                        ChocoLogging.getBranchingLogger().finest(me + ": " + cEnds[i].pretty() + " cEndsSup =" + lastT);
                    }
                    cEnds[i].setSup(lastT);
                }

            }
        }
    }
}
