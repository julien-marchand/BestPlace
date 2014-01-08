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

import java.util.BitSet;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A bin packing constraint similar to {@link choco.cp.solver.constraints.global.pack.PackSConstraint}
 * but oriented satisfaction rather than optimization as the number of empty bins is not considered here.
 * It enforces a list of constant-size items (in decreasing order of size) to be packed into bins of limited capacities (their maximum loads), using 2 invariants:
 * 1) global load O(nbBins): the sum of the bin loads is equal to the sum of the size items
 * 2) knapsack on each bin  O(nbItems x nbBins): the bin load is bounded by the total size of the assigned items and the total size of the assigned and candidate items.
 * A simple improvement of this latter upper bound is possible by activating the "big items" optimization:
 * big items are defined for a given bin such that no two of them can be assigned simultaneously to the bin without violating its capacity.
 * The upper bound then becomes the total size of the assigned and candidate small items + the maximal size among the big candidate items.
 * With option ({@code BigItemsPolicy.STATIC}) the list of big candidates is computed once for each bin according to its initial capacity,
 * but as items are ordered, the list can also be maintained dynamically for almost free at each item assignment/removal ({@code BigItemsPolicy.DYNAMIC})
 *
 * @author Sophie Demassey, Fabien Hermenier
 * @see choco.cp.solver.constraints.global.pack.PackSConstraint
 */
public class FastBinPacking extends AbstractLargeIntSConstraint implements CustomPack {

    /**
     * The solver environment.
     */
    private IEnvironment env;

    /**
     * The number of bins.
     */
    private final int nbBins;

    /**
     * The bin assigned to each item.
     */
    protected final IntDomainVar[] bins;

    /**
     * The constant size of each item in decreasing order.
     */
    protected final int[] iSizes;

    /**
     * The sum of the item sizes.
     */
    private final long sumISizes;

    /**
     * The load of each bin.
     */
    protected final IntDomainVar[] loads;

    /**
     * The candidate items for each bin (possible but not required assignments)
     */
    private IStateBitSet[] candidates;

    /**
     * The total size of the required + candidate items for each bin.
     */
    private IStateInt[] bTLoads;

    /**
     * The total size of the required items for each bin.
     */
    private IStateInt[] bRLoads;

    /**
     * The sum of the bin load LBs.
     */
    private IStateInt sumLoadInf;

    /**
     * The sum of the bin load UBs.
     */
    private IStateInt sumLoadSup;

    /**
     * Has some bin load variable changed since the last propagation ?
     */
    private IStateBool loadsHaveChanged;


    /**
     * The bins having candidate items.
     */
    private IStateBitSet availableBins;

    /**
     * "big items" optimization policies:
     * 1) no optimization, or 2) static computation of the list of big candidates for each bin, or 3) dynamic maintenance of the list
     */
    public enum BigItemsPolicy {
        IGNORE, STATIC, DYNAMIC
    }

    /**
     * "big items" optimization flag.
     */
    private BigItemsPolicy bigItemsPolicy;

    /**
     * "big items" optimization default flag value (no optimization).
     */
    public static final BigItemsPolicy DEFAULT_BIG_ITEMS_POLICY = BigItemsPolicy.IGNORE;

    /**
     * "big items" optimization: for each bin, the first "small" candidate item which can be hosted along with any bigger items.
     */
    private IStateInt[] firstSmall;

    /**
     * "big items" optimization: the bins having a required big item (only for the STATIC policy).
     */
    private IStateBitSet bigRequired;

    /**
     * constructor of the FastBinPacking global constraint
     *
     * @param environment  the solver environment
     * @param loads        array of nbBins variables, each figuring the total size of the items assigned to it, usually initialized to [0, capacity]
     * @param sizes        array of nbItems CONSTANTS in DECREASING order, each figuring the size of i-th biggest item
     * @param bins         array of nbItems variables, each figuring the possible bins an item can be assigned to, usually initialized to [0, nbBins-1]
     * @param bigItemsFlag {@code IGNORE, STATIC, DYNAMIC} the policy to optimize the load upper bounding according to big items
     */
    public FastBinPacking(IEnvironment environment, IntDomainVar[] loads, IntDomainVar[] sizes, IntDomainVar[] bins, BigItemsPolicy bigItemsFlag) {
        super(ArrayUtils.append(bins, loads));
        assert checkArgs(loads, sizes, bins);
        this.env = environment;
        this.loads = loads;
        this.nbBins = loads.length;
        this.bins = bins;
        this.iSizes = new int[sizes.length];
        long sum = 0;
        for (int i = 0; i < sizes.length; i++) {
            this.iSizes[i] = sizes[i].getVal();
            sum += iSizes[i];
        }
        this.sumISizes = sum;
        this.bTLoads = new IStateInt[nbBins];
        this.bRLoads = new IStateInt[nbBins];
        this.bigItemsPolicy = bigItemsFlag;
        if (bigItemsPolicy != BigItemsPolicy.IGNORE) {
            this.firstSmall = new IStateInt[nbBins];
            if (bigItemsPolicy == BigItemsPolicy.STATIC) {
                this.bigRequired = env.makeBitSet(nbBins);
            }
        }
    }

    /**
     * constructor of the FastBinPacking global constraint without the "big items" optimization
     *
     * @param environment the solver environment
     * @param loads       array of nbBins variables, each figuring the total size of the items assigned to it, usually initialized to [0, capacity]
     * @param sizes       array of nbItems CONSTANTS in DECREASING order, each figuring the size of i-th biggest item
     * @param bins        array of nbItems variables, each figuring the possible bins an item can be assigned to, usually initialized to [0, nbBins-1]
     */
    public FastBinPacking(IEnvironment environment, IntDomainVar[] loads, IntDomainVar[] sizes, IntDomainVar[] bins) {
        this(environment, loads, sizes, bins, DEFAULT_BIG_ITEMS_POLICY);
    }

    public boolean checkArgs(IntDomainVar[] loads, IntDomainVar[] sizes, IntDomainVar[] bins) {
        //if (!bins[0].hasEnumeratedDomain()) return false;
        for (int i = 1; i < sizes.length; i++) {
            if (/*!bins[i].hasEnumeratedDomain() ||*/ !sizes[i].isInstantiated() || sizes[i - 1].getVal() < sizes[i].getVal()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final int getRemainingSpace(int bin) {
        return loads[bin].getSup() - bRLoads[bin].get();
    }

    @Override
    public IStateBitSet getCandidates(int bin) {
        return candidates[bin];
    }

    /**
     * is the "big items" optimization active ?
     *
     * @return {@code true} if active (static or dynamic)
     */
    public boolean doBigItemsOpt() {
        return bigItemsPolicy != BigItemsPolicy.IGNORE;
    }

    /**
     * is the "big items" optimization active ?
     *
     * @return current policy
     */
    public BigItemsPolicy getBigItemsPolicy() {
        return bigItemsPolicy;
    }

    /**
     * set "big items" optimization
     *
     * @param bigItemsPolicy wanted policy
     */
    public void setBigItemsPolicy(BigItemsPolicy bigItemsPolicy) {
        this.bigItemsPolicy = bigItemsPolicy;
    }

    /**
     * print the list of candidate items for a given bin
     *
     * @param bin bin index
     * @return list of the item indices, between braces, separated by spaces
     */
    public String prettyCandidates(int bin) {
        StringBuilder s = new StringBuilder("{");
        for (int i = candidates[bin].nextSetBit(0); i >= 0; i = candidates[bin].nextSetBit(i + 1)) {
            s.append(i).append(' ');
        }
        return s.append('}').toString();
    }

    //****************************************************************//
    //********* Events ***********************************************//
    //****************************************************************//

    @Override
    public int getFilteredEventMask(int idx) {
        if (idx < bins.length) {
            return IntVarEvent.REMVAL_MASK;
        }
        //return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
        return IntVarEvent.BOUNDS_MASK;
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        int[] l = new int[nbBins];
        int[] c = new int[nbBins];
        for (int i = 0; i < bins.length; i++) {
            final int b = tuple[i];
            l[b] += iSizes[i];
            c[b]++;
        }
        for (int b = 0; b < nbBins; b++) {
            if (tuple[b + bins.length] != l[b]) {
                ChocoLogging.getBranchingLogger().warning("Bad load of " + b + " = " + tuple[b + bins.length] + " expected =" + l[b]);
                return false;
            }
        }
        return true;
    }

    @Override
    /**
     * initialize the internal data: availableBins, candidates, binRequiredLoads, binTotalLoads, sumLoadInf, sumLoadSup
     * shrink the item-to-bins assignment variables: 0 <= bins[i] <= nbBins
     * shrink the bin load variables: binRequiredLoad <= binLoad <= binTotalLoad
     */
    public void awake() throws ContradictionException {
        availableBins = env.makeBitSet(nbBins);
        candidates = new IStateBitSet[nbBins];
        for (int b = 0; b < nbBins; b++) {
            candidates[b] = env.makeBitSet(bins.length);
        }
        int[] rLoads = new int[nbBins];
        int[] cLoads = new int[nbBins];

        for (int i = 0; i < bins.length; i++) {
            bins[i].updateInf(0, this, false);
            bins[i].updateSup(nbBins - 1, this, false);
            if (bins[i].isInstantiated()) {
                rLoads[bins[i].getVal()] += iSizes[i];
            } else {
                DisposableIntIterator it = bins[i].getDomain().getIterator();
                try {
                    while (it.hasNext()) {
                        int b = it.next();
                        candidates[b].set(i);
                        cLoads[b] += iSizes[i];
                    }
                } finally {
                    it.dispose();
                }
            }
        }

        int sumLoadInf = 0;
        int sumLoadSup = 0;
        for (int b = 0; b < nbBins; b++) {
            bRLoads[b] = env.makeInt(rLoads[b]);
            bTLoads[b] = env.makeInt(rLoads[b] + cLoads[b]);
            loads[b].updateInf(rLoads[b], this, false);
            loads[b].updateSup(rLoads[b] + cLoads[b], this, false);
            if (!candidates[b].isEmpty()) {
                availableBins.set(b);
                if (doBigItemsOpt()) {
                    int decLoad = bigItemsInit(b);
                    if (decLoad < 0) {
                        int r = bTLoads[b].add(decLoad);
                        loads[b].updateSup(r, this, false);
                    }
                }
            }
            sumLoadInf += loads[b].getInf();
            sumLoadSup += loads[b].getSup();
        }
        this.sumLoadInf = env.makeInt(sumLoadInf);
        this.sumLoadSup = env.makeInt(sumLoadSup);
        this.loadsHaveChanged = env.makeBool(false);

        assert checkLoadConsistency() && checkCandidatesConsistency();

        propagate();
    }

    @Override
    /**
     * propagate 1) globally: sumItemSizes == sumBinLoads 2) on each bin: sumAssignedItemSizes == binLoad
     * rule 1.0: if sumSizes < sumBinLoadInfs or sumSizes > sumBinLoadSups then fail
     * rule 1.1, for each bin: sumItemSizes - sumOtherBinLoadSups <= binLoad <= sumItemSizes - sumOtherBinLoadInfs
     * rule 2.0, for each bin: binRequiredLoad <= binLoad <= binTotalLoad
     * rule 2.1, for each bin and candidate item: if binRequiredLoad + itemSize > binLoadSup then remove item from bin
     * rule 2.2, for each bin and candidate item: if binTotalLoad - itemSize < binLoadInf then pack item into bin
     * with "big items" optimization, the last rule is not valid for big items but can be replaced by:
     * rule 2.3: if smallItemSizes < binLoadInf then remove big candidates with size < binLoadInf-smallItemSizes
     * and update binLoadInf as binRequiredLoad + the size of the smallest big remaining candidate
     */
    public void propagate() throws ContradictionException {
        recomputeLoadSums();
        boolean noFixPoint = true;
        while (noFixPoint) {
            if (sumISizes > sumLoadSup.get() || sumISizes < sumLoadInf.get()) {
                fail();
            }
            noFixPoint = false;
            for (int b = availableBins.nextSetBit(0); b >= 0; b = availableBins.nextSetBit(b + 1)) {
                noFixPoint |= filterLoadInf(b, Math.max(bRLoads[b].get(), (int) sumISizes - sumLoadSup.get() + loads[b].getSup()));
                noFixPoint |= filterLoadSup(b, Math.min(bTLoads[b].get(), (int) sumISizes - sumLoadInf.get() + loads[b].getInf()));
                //if (bigItemsPolicy == BigItemsPolicy.DYNAMIC) noFixPoint |= bigItemsKnapsackAdditionalFiltering(b);
                noFixPoint |= propagateKnapsack(b);
            }
        }
        assert checkLoadConsistency() && checkCandidatesConsistency();
    }

    /**
     * recompute the sum of the min/max loads only if at least one variable bound has been updated outside the constraint
     */
    private void recomputeLoadSums() {
        if (!loadsHaveChanged.get()) {
            return;
        }
        loadsHaveChanged.set(false);
        int sumLoadInf = 0;
        int sumLoadSup = 0;
        for (int b = 0; b < nbBins; b++) {
            sumLoadInf += loads[b].getInf();
            sumLoadSup += loads[b].getSup();
        }
        this.sumLoadInf.set(sumLoadInf);
        this.sumLoadSup.set(sumLoadSup);
    }

//@Override
///**
// * on loads variables: update sum of the bin load LBs + UBs, check it against sumItemSizes, then delay propagation
// */
//public void awakeOnInst(int varIdx) throws ContradictionException {
//	awakeOnBounds(varIdx);
//}
//
//@Override
///**
// * on loads variables: update sum of the bin load LBs + UBs, check it against sumItemSizes, then delay propagation
// */
//public void awakeOnBounds(int varIdx) throws ContradictionException {
//	varIdx -= bins.length;
//	int oldInf = loads[varIdx].getInf();
//	int oldSup = loads[varIdx].getSup();
//	DisposableIntIterator it = loads[varIdx].getDomain().getDeltaIterator();
//	try {
//		while (it.hasNext()) {
//			int v = it.next();
//			if (v<oldInf) {
//				oldInf = v;
//			} else if (v>oldSup) {
//				oldSup = v;
//			}
//		}
//	} finally {
//		it.dispose();
//	}
//	int varInf = loads[varIdx].getInf()-oldInf;
//	if (varInf > 0) {
//		sumLoadInf.add(varInf);
//		if (sumISizes < sumLoadInf.get()) {
//			fail();
//		}
//	}
//	int varSup = loads[varIdx].getSup()-oldSup;
//	if (varSup < 0) {
//		sumLoadSup.add(varSup);
//		if (sumISizes > sumLoadSup.get()) {
//			fail();
//		}
//	}
//	this.constAwake(false);
//}
//
//    @Override
//    /**
//     * on loads variables: update sum of the bin load LBs, check it against sumItemSizes, then delay propagation
//     */
//    public void awakeOnInf(int varIdx) throws ContradictionException {
//        varIdx -= bins.length;
//	    if (loads[varIdx].isInstantiated()) return;
//        int oldInf = Choco.MAX_UPPER_BOUND;
//        DisposableIntIterator it = loads[varIdx].getDomain().getDeltaIterator();
//        try {
//            while (it.hasNext()) {
//                oldInf = Math.min(oldInf, it.next());
//            }
//        } finally {
//            it.dispose();
//        }
//        int r = sumLoadInf.add(loads[varIdx].getInf() - oldInf);
//        if (sumISizes < r) {
//            fail();
//        }
//        this.constAwake(false);
//    }
//
//    @Override
//    /**
//     * on loads variables: update sum of the bin load UBs, check it against sumItemSizes, then delay propagation
//     */
//    public void awakeOnSup(int varIdx) throws ContradictionException {
//        varIdx -= bins.length;
//	    if (loads[varIdx].isInstantiated()) return;
//        int oldSup = Choco.MIN_LOWER_BOUND;
//        DisposableIntIterator it = loads[varIdx].getDomain().getDeltaIterator();
//        try {
//            while (it.hasNext()) {
//                oldSup = Math.max(oldSup, it.next());
//            }
//        } finally {
//            it.dispose();
//        }
//        int r = sumLoadSup.add(loads[varIdx].getSup() - oldSup);
//        if (sumISizes > r) {
//            fail();
//        }
//        this.constAwake(false);
//    }

    /**
     * on loads variables: delay propagation
     */
    @Override
    public void awakeOnInf(int varIdx) throws ContradictionException {
        loadsHaveChanged.set(true);
    }

    /**
     * on loads variables: delay propagation
     */
    @Override
    public void awakeOnSup(int varIdx) throws ContradictionException {
        loadsHaveChanged.set(true);
    }


    /**
     * on bins variables: propagate the removal of item-to-bins assignments.
     * 1) update the candidate and check to decrease the load UB of each removed bins: binLoad <= binTotalLoad
     * 2) if item is assigned: update the required and check to increase the load LB of the bin: binLoad >= binRequiredLoad
     *
     * @throws ContradictionException on the load variables
     */
    @Override
    public void awakeOnRemovals(int iIdx, DisposableIntIterator deltaDomain) throws ContradictionException {
        try {
            while (deltaDomain.hasNext()) {
                removeItem(iIdx, deltaDomain.next());
            }
        } finally {
            deltaDomain.dispose();
        }
        if (vars[iIdx].isInstantiated()) {
            assignItem(iIdx, vars[iIdx].getVal());
        }
        this.constAwake(false);
    }

    //****************************************************************//
    //********* VARIABLE FILTERING ***********************************//
    //****************************************************************//

    /**
     * synchronize the internal data when an item is assigned to a bin:
     * remove the item from the candidate list of the bin and balance its size from the candidate to the required load of the bin
     * check to update the LB of load[bin]
     *
     * @param item item index
     * @param bin  bin index
     * @throws ContradictionException on the load[bin] variable
     */
    private void assignItem(int item, int bin) throws ContradictionException {
        if (candidates[bin].get(item)) {
            int r = bRLoads[bin].add(iSizes[item]);
            filterLoadInf(bin, r);
            candidates[bin].clear(item);
            if (candidates[bin].isEmpty()) {
                availableBins.clear(bin);
            } else if (doBigItemsOpt()) {
                r = bTLoads[bin].add(bigItemsAssign(item, bin));
                filterLoadSup(bin, r);
            }
        }
    }

    /**
     * synchronize the internal data when an item is removed from a bin:
     * remove the item from the candidate list of the bin and reduce the candidate load of the bin
     * check to update the UB of load[bin]
     *
     * @param item item index
     * @param bin  bin index
     * @throws ContradictionException on the load[bin] variable
     */
    private void removeItem(int item, int bin) throws ContradictionException {
        if (candidates[bin].get(item)) {
            candidates[bin].clear(item);
            if (candidates[bin].isEmpty()) {
                availableBins.clear(bin);
            }
            int r = bTLoads[bin].add(-1 * iSizes[item]);
            if (doBigItemsOpt()) {
                r = bTLoads[bin].add(bigItemsRemove(item, bin));
            }
            filterLoadSup(bin, r);
        }
    }

    /**
     * increase the LB of the bin load and the sum of the bin load LBs
     *
     * @param bin        bin index
     * @param newLoadInf new LB of the bin load
     * @return {@code true} if LB is increased.
     * @throws ContradictionException on the load[bin] variable
     */
    private boolean filterLoadInf(int bin, int newLoadInf) throws ContradictionException {
        int inc = newLoadInf - loads[bin].getInf();
        if (inc > 0) {
            loads[bin].updateInf(newLoadInf, this, false);
            int r = sumLoadInf.add(inc);
            if (sumISizes < r) {
                fail();
            }
            return true;
        }
        return false;
    }

    /**
     * decrease the UB of the bin load and the sum of the bin load UBs
     *
     * @param bin        bin index
     * @param newLoadSup new UB of the bin load
     * @return {@code true} if UB is decreased.
     * @throws ContradictionException on the load[bin] variable
     */
    private boolean filterLoadSup(int bin, int newLoadSup) throws ContradictionException {
        int dec = newLoadSup - loads[bin].getSup();
        if (dec < 0) {
            loads[bin].updateSup(newLoadSup, this, false);
            int r = sumLoadSup.add(dec);
            if (sumISizes > r) {
                fail();
            }
            return true;
        }
        return false;
    }

    /**
     * propagate the knapsack constraint on a given bin:
     * 1) remove the candidate items bigger than the remaining free space (when binRequiredLoad + itemSize > binLoadSup)
     * 2) pack the candidate items necessary to reach the load LB (when binTotalLoad - itemSize < binLoadInf).
     * code optimized according to the decreasing order of the item sizes.
     * the loads are also filtered within this constraint (rather in the propagate loop) because considered bins eventually become unavailable
     *
     * @param bin bin index
     * @return {@code true} if at least one item is removed or packed.
     * @throws ContradictionException on the bins or loads variables
     */
    private boolean propagateKnapsack(int bin) throws ContradictionException {
        boolean ret = false;
        for (int item = candidates[bin].nextSetBit(0); item >= 0; item = candidates[bin].nextSetBit(item + 1)) {
            if (iSizes[item] + bRLoads[bin].get() > loads[bin].getSup()) {
                removeItem(item, bin);
                bins[item].removeVal(bin, this, false);
                if (bins[item].isInstantiated()) {
                    assignItem(item, bins[item].getVal());
                }
                ret = true;
            } else if (bTLoads[bin].get() - iSizes[item] < loads[bin].getInf() && (!doBigItemsOpt() || item >= firstSmall[bin].get())) {
                assignItem(item, bin);
                DisposableIntIterator domain = bins[item].getDomain().getIterator();
                try {
                    while (domain.hasNext()) {
                        int b = domain.next();
                        if (b != bin) {
                            removeItem(item, b);
                        }
                    }
                } finally {
                    domain.dispose();
                }
                bins[item].instantiate(bin, this, false);
                ret = true;
            } else {
                break;
            }
        }
        return ret;
    }

    //****************************************************************//
    //********* Checkers *********************************************//
    //****************************************************************//

    /**
     * Check the consistency of the required and candidate loads with regards to the assignment variables:
     * for each bin: sumAssignedItemSizes == binRequiredLoad, sumAllPossibleItemSizes == binTotalLoad
     * rule 3, for each bin: binRequiredLoad <= binLoad <= binTotalLoad
     *
     * @return {@code false} if not consistent.
     */
    private boolean checkLoadConsistency() {
        boolean check = true;
        int[] rs = new int[nbBins];
        int[] cs = new int[nbBins];
        for (int i = 0; i < bins.length; i++) {
            if (bins[i].isInstantiated()) {
                rs[bins[i].getVal()] += iSizes[i];
            } else {
                DisposableIntIterator it = bins[i].getDomain().getIterator();
                try {
                    while (it.hasNext()) {
                        cs[it.next()] += iSizes[i];
                    }
                } finally {
                    it.dispose();
                }
            }
        }
        int sumLoadInf = 0;
        int sumLoadSup = 0;
        for (int b = 0; b < rs.length; b++) {
            if (rs[b] != bRLoads[b].get()) {
                ChocoLogging.getBranchingLogger().warning(loads[b].pretty() + " required=" + bRLoads[b].get() + " expected=" + rs[b]);
                check = false;
            }
            if (!doBigItemsOpt() && rs[b] + cs[b] != bTLoads[b].get()) {
                ChocoLogging.getBranchingLogger().warning(loads[b].pretty() + " total=" + bTLoads[b].get() + " expected=" + (rs[b] + cs[b]));
                check = false;
            }
            if (loads[b].getInf() < rs[b]) {
                ChocoLogging.getBranchingLogger().warning(loads[b].pretty() + " LB expected >=" + rs[b]);
                check = false;
            }
            if (loads[b].getSup() > rs[b] + cs[b]) {
                ChocoLogging.getBranchingLogger().warning(loads[b].pretty() + " UB expected <=" + (rs[b] + cs[b]));
                check = false;
            }
            sumLoadInf += loads[b].getInf();
            sumLoadSup += loads[b].getSup();
        }
        if (this.sumLoadInf.get() != sumLoadInf) {
            ChocoLogging.getBranchingLogger().warning("Sum Load LB = " + this.sumLoadInf.get() + " expected =" + sumLoadInf);
            check = false;
        }
        if (this.sumLoadSup.get() != sumLoadSup) {
            ChocoLogging.getBranchingLogger().warning("Sum Load UB = " + this.sumLoadSup.get() + " expected =" + sumLoadSup);
            check = false;
        }
        ChocoLogging.flushLogs();
        if (!check) {
            for (int b = 0; b < rs.length; b++) {
                System.out.println(loads[b].pretty() + " required=" + bRLoads[b].get() + " (" + rs[b] + ") total=" + bTLoads[b].get() + " (" + (rs[b] + cs[b]) + ")");
            }
            System.out.println("Sum Load LB = " + this.sumLoadInf.get() + " (" + sumLoadInf + ")");
            System.out.println("Sum Load UB = " + this.sumLoadSup.get() + " (" + sumLoadSup + ")");
            for (int i = 0; i < bins.length; i++) {
                System.out.println(bins[i].pretty());
            }
        }
        return check;
    }

    /**
     * Check that the candidate lists are aligned with the assignment variables:
     * item is in candidates[bin] iff bin is in bins[item]
     *
     * @return {@code false} if not consistent.
     */
    private boolean checkCandidatesConsistency() {
        BitSet[] bs = new BitSet[nbBins];
        for (int bin = 0; bin < nbBins; bin++) {
            bs[bin] = new BitSet(iSizes.length);
        }
        for (int i = 0; i < bins.length; i++) {
            if (!bins[i].isInstantiated()) {
                DisposableIntIterator it = bins[i].getDomain().getIterator();
                try {
                    while (it.hasNext()) {
                        bs[it.next()].set(i);
                    }
                } finally {
                    it.dispose();
                }
            }
        }
        for (int b = 0; b < nbBins; b++) {
            for (int i = 0; i < bs[b].size(); i++) {
                if (bs[b].get(i) != candidates[b].get(i)) {
                    ChocoLogging.getBranchingLogger().warning("candidate i '" + i + "' for bin '" + b + ": " + candidates[b].get(i) + " expected: " + bs[b].get(i));
                    ChocoLogging.getBranchingLogger().warning("candidates for b: " + this.prettyCandidates(b));
                    ChocoLogging.flushLogs();
                    return false;
                }
            }
        }
        return true;
    }

    //****************************************************************//
    //********* Big Items Optimization  ******************************//
    //****************************************************************//

    /**
     * initialize the big candidates list and compute the candidate load decrement for a given bin
     * s.t. all candidates < firstSmall are mutually exclusive ("big candidates") thus:
     * binLoad <= sumSmallCandidateSizes + maxBigCandidateSizes
     *
     * @param bin bin index
     * @return the candidate load decrement = maxBigCandidateSizes - sumBigCandidateSizes <= 0
     */
    private int bigItemsInit(int bin) {
        int firstB = candidates[bin].nextSetBit(0);
        firstSmall[bin] = env.makeInt(firstB);

        return bigItemsUpdateBigs(bin, firstB, firstB);
    }

    /**
     * find new big candidates (from previous firstSmall) and compute the candidate load decrement for a given bin
     *
     * @param bin    bin index
     * @param firstB index of the current first candidate
     * @param firstS index of the previous first small candidate
     * @return the candidate load decrement = maxBigCandidateSizes - sumBigNewCandidateSizes <= 0
     */
    private int bigItemsUpdateBigs(int bin, int firstB, int firstS) {
        int decLoad = 0;
        int maxSpace = loads[bin].getSup() - bRLoads[bin].get();
        int i = candidates[bin].nextSetBit(firstS + 1);
        while (i >= 0 && iSizes[firstS] + iSizes[i] > maxSpace) {
            decLoad -= iSizes[firstS];
            firstS = i;
            i = candidates[bin].nextSetBit(i + 1);
        }
        // all candidates are big items
        if (i < 0) {
            decLoad -= iSizes[firstS];
            firstS = bins.length;
        }
        firstSmall[bin].set(firstS);
        // no big candidate => no max term
        return (firstS == firstB) ? decLoad : decLoad + iSizes[firstB];
    }

    private int bigItemsAssign(int item, int bin) throws ContradictionException {
        assert bigItemsPolicy != BigItemsPolicy.IGNORE;
        return (bigItemsPolicy == BigItemsPolicy.STATIC) ? bigItemsAssignStatic(item, bin) : bigItemsAssignDynamic(item, bin);
    }

    /**
     * update binTotalLoad if the new assigned item is a big candidate:
     * if item was the biggest candidate => max term iSizes[item] remains unchanged
     * else =>  max term iSizes[biggest] becomes iSizes[item] (all other big items will be removed in propagate)
     *
     * @param item item index
     * @param bin  bin index
     * @return the required + candidate load decrement <=0
     */
    private int bigItemsAssignStatic(int item, int bin) {
        if (item < firstSmall[bin].get()) {
            assert !bigRequired.get(bin); // no 2 big items can be assigned to bin
            bigRequired.set(bin);
            int firstB = candidates[bin].nextSetBit(0);
            if (firstB < item) {
                return iSizes[item] - iSizes[firstB];
            }
        }
        return 0;
    }

    /**
     * remove all other big candidates and update binTotalLoad if the new assigned item was a big candidate,
     * then update firstSmall and binTotalLoad by searching for new big candidates
     *
     * @param item item index
     * @param bin  bin index
     * @return the required + candidate load decrement <=0
     * @throws ContradictionException on the bins variables
     */
    private int bigItemsAssignDynamic(int item, int bin) throws ContradictionException {
        int firstB = candidates[bin].nextSetBit(0);
        int firstS = firstSmall[bin].get();
        int decLoad = -((firstB < firstS) ? ((item < firstB) ? iSizes[item] : iSizes[firstB]) : 0);

        if (item < firstS) { // the new assigned item is a big item => remove all big candidates
            decLoad += iSizes[item]; // itemSize is now in binRequiredLoad
            for (; firstB >= 0 && firstB < firstS; firstB = candidates[bin].nextSetBit(firstB + 1)) {
                candidates[bin].clear(firstB);
                bins[firstB].removeVal(bin, this, false);
                if (bins[firstB].isInstantiated()) {
                    assignItem(firstB, bins[firstB].getVal());
                }
            }
            if (candidates[bin].isEmpty()) {
                availableBins.clear(bin);
                assert bTLoads[bin].get() + decLoad == bRLoads[bin].get();
                return decLoad;
            }
        } else if (item == firstS) {  // firstS must be a candidate item
            firstS = candidates[bin].nextSetBit(firstS + 1);
        }
        return decLoad + bigItemsUpdateBigs(bin, firstB, firstS);
    }

    private int bigItemsRemove(int item, int bin) {
        assert bigItemsPolicy != BigItemsPolicy.IGNORE;
        return (bigItemsPolicy == BigItemsPolicy.STATIC) ? bigItemsRemoveStatic(item, bin) : bigItemsRemoveDynamic(item, bin);
    }

    /**
     * update binTotalLoad if the new removed item is a big candidate:
     * if a big item is already assigned => the max term iSizes[bigRequired] remains unchanged (and iSizes[item] must be readded)
     * else if item was the only big candidate => the max term iSizes[item] disappear
     * else if item was the biggest candidate => the max term iSizes[item] becomes iSizes[newBiggest]
     * else if item was not the biggest candidate => the max term iSizes[biggest] remains unchanged (and iSizes[item] must be readded)
     *
     * @param item item index
     * @param bin  bin index
     * @return the candidate load decrement <=0
     */
    private int bigItemsRemoveStatic(int item, int bin) {
        int firstS = firstSmall[bin].get();
        if (item < firstS) {
            if (bigRequired.get(bin)) {
                return iSizes[item];
            }
            int firstB = candidates[bin].nextSetBit(0);
            if (firstB >= 0 && firstB < firstS) {
                return iSizes[Math.max(firstB, item)];
            }
        }
        return 0;
    }

    /**
     * update binTotalLoad if the new removed item is a big candidate and update firstSmall if it is the removed item
     * and if only one small item remains
     *
     * @param item item index
     * @param bin  bin index
     * @return the candidate load decrement <=0
     */
    private int bigItemsRemoveDynamic(int item, int bin) {
        if (candidates[bin].isEmpty()) return 0;
        int decLoad = 0;
        int firstS = firstSmall[bin].get();
        int firstB = candidates[bin].nextSetBit(0);
        if (item < firstS) {
            if (firstB < firstS) {
                decLoad += iSizes[Math.max(firstB, item)]; // new max
            }
        } else if (item == firstS) { // firstS must be a candidate
            firstS = candidates[bin].nextSetBit(firstS + 1);
        } else if (candidates[bin].nextSetBit(firstS + 1) < 0) { // no lonely small candidate
            if (firstB < firstS) decLoad -= iSizes[firstS];
            firstS = bins.length;
        }
        firstSmall[bin].set(firstS);
        return decLoad;
    }
}