package entropy.plan.choco.constraint.pack;/*
* Created by IntelliJ IDEA.
* User: sofdem - sophie.demassey{at}mines-nantes.fr
* Date: 15/08/11 - 01:28
*/

import java.util.BitSet;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * @author Sophie Demassey
 */
public class FastMultiBinPacking extends AbstractLargeIntSConstraint implements CustomPack {

    private IEnvironment env;

    /**
     * The bin assigned to each item [I].
     */
    protected final IntDomainVar[] bins;

    /**
     * The constant size of each item on each dimension [DxI].
     */
    protected final int[][] iSizes;

    /**
     * The sum of the item sizes on each dimension [D].
     */
    private final long[] sumISizes;

    /**
     * The load of each bin  on each dimension [DxB].
     */
    protected final IntDomainVar[][] loads;

    /**
     * The candidate items for each bin (possible but not required assignments) [B].
     */
    private IStateBitSet[] candidates;

    /**
     * The total size of the candidate + required items for each bin on each dimension [DxB].
     */
    private IStateInt[][] bTLoads;

    /**
     * The total size of the required items for each bin on each dimension [DxB].
     */
    private IStateInt[][] bRLoads;

    /**
     * The sum of the bin load LBs on each dimension [D].
     */
    private IStateInt[] sumLoadInf;

    /**
     * The sum of the bin load UBs on each dimension [D].
     */
    private IStateInt[] sumLoadSup;

    /**
     * The remaining available bins (having candidate items).
     */
    private IStateBitSet availableBins;


    /**
     * nb of dimensions.
     */
    private final int nbDims;

    /**
     * nb of bins.
     */
    private final int nbBins;

    /**
     * constructor of the FastBinPacking global constraint
     * @param environment the solver environment
     * @param loads array of nbDims x nbBins variables, each figuring the total size of the items assigned to it, usually initialized to [0, capacity]
     * @param sizes array of nbDim x nbItems CONSTANTS ordered in decreasing order on the first dimension, each figuring the size of i-th item
     * @param bins  array of nbItems variables, each figuring the possible bins an item can be assigned to, usually initialized to [0, nbBins-1]
     */
    public FastMultiBinPacking(IEnvironment environment, IntDomainVar[][] loads, int[][] sizes, IntDomainVar[] bins) {
        super(ArrayUtils.append(bins, ArrayUtils.flatten(loads)));
        this.nbDims = sizes.length;
        this.nbBins = loads[0].length;
        this.loads = loads;
        this.env = environment;
        this.iSizes = sizes;
        this.sumISizes = new long[nbDims];
        for (int d=0; d<nbDims; d++) {
            long sum = 0;
            for (int i = 0; i < sizes[d].length; i++) {
                sum += iSizes[d][i];
            }
            sumISizes[d] = sum;
        }
        this.bTLoads = new IStateInt[nbDims][nbBins];
        this.bRLoads = new IStateInt[nbDims][nbBins];
        this.sumLoadInf = new IStateInt[nbDims];
        this.sumLoadSup = new IStateInt[nbDims];
        this.bins = bins;
    }

    @Override
    public final int getRemainingSpace(int bin) {
        throw new UnsupportedOperationException("the dimension must be specified.");
    }

    public final int getRemainingSpace(int dim, int bin) {
        return loads[dim][bin].getSup() - bRLoads[dim][bin].get();
    }

    @Override
    public IStateBitSet getCandidates(int bin) {
        return candidates[bin];
    }

    //****************************************************************//
    //********* Events ***********************************************//
    //****************************************************************//

    @Override
    public int getFilteredEventMask(int idx) {
        if (idx<bins.length) {
            return IntVarEvent.REMVAL_MASK;
        }
        return IntVarEvent.BOUNDS_MASK;
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        int[][] l = new int[nbDims][nbBins];
        int[] c = new int[nbBins];
        for (int i = 0; i < bins.length; i++) {
            final int b = tuple[i];
            for (int d=0; d < nbDims; d++) {
                l[d][b] += iSizes[d][i];
            }
            c[b]++;
        }
        int shift = bins.length;
        for (int d=0; d < nbDims; d++) {
            for (int i = 0; i < nbBins; i++) {
                if (tuple[i+shift] != l[d][i]) {
                    ChocoLogging.getBranchingLogger().warning("Bad load of " + i + " in dim " + d + " = " + tuple[i+shift] + " expected =" + l[d][i]);
                    return false;
                }
            }
            shift += nbBins;
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
        int[][] rLoads = new int[nbDims][nbBins];
        int[][] cLoads = new int[nbDims][nbBins];

        for (int i = 0; i < bins.length; i++) {
            bins[i].updateInf(0, this, false);
            bins[i].updateSup(nbBins - 1, this, false);
            if (bins[i].isInstantiated()) {
                for (int d=0; d < nbDims; d++) {
                    rLoads[d][bins[i].getVal()] += iSizes[d][i];
                }
            } else {
                DisposableIntIterator it = bins[i].getDomain().getIterator();
                try {
                    while (it.hasNext()) {
                        int b = it.next();
                        candidates[b].set(i);
                        for (int d=0; d < nbDims; d++) {
                            cLoads[d][b] += iSizes[d][i];
                        }
                    }
                } finally {
                    it.dispose();
                }
            }
        }

        for (int d=0; d < nbDims; d++) {
            int sumLoadInf = 0;
            int sumLoadSup = 0;
            for (int b = 0; b < nbBins; b++) {
                bRLoads[d][b] = env.makeInt(rLoads[d][b]);
                bTLoads[d][b] = env.makeInt(rLoads[d][b]+cLoads[d][b]);
                loads[d][b].updateInf(rLoads[d][b], this, false);
                sumLoadInf += loads[d][b].getInf();
                loads[d][b].updateSup(rLoads[d][b]+cLoads[d][b], this, false);
                sumLoadSup += loads[d][b].getSup();
                if (!candidates[b].isEmpty() && d==0) {
                    availableBins.set(b);
                }
            }
            this.sumLoadInf[d] = env.makeInt(sumLoadInf);
            this.sumLoadSup[d] = env.makeInt(sumLoadSup);
        }
        assert checkLoadConsistency() && checkCandidatesConsistency();

        propagate();
    }

    @Override
    /**
     * propagate 1) globally: sumItemSizes == sumBinLoads 2) on each bin: sumAssignedItemSizes == binLoad
     * rule 1: if sumSizes > sumBinLoadSups then fail
     * rule 2, for each bin: sumItemSizes - sumOtherBinSups <= binLoad <= sumItemSizes - sumOtherBinInfs
     * rule 3, for each bin: binRequiredLoad <= binLoad <= binTotalLoad
     * rule 4, for each bin and candidate item: if binRequiredLoad + itemSize > binLoadSup then remove item from bin
     * rule 5, for each bin and candidate item: if binTotalLoad - itemSize < binLoadInf then pack item into bin
     */
    public void propagate() throws ContradictionException {
        //ChocoLogging.getSearchLogger().finest("propagate " + pretty());
        boolean noFixPoint = true;
        while (noFixPoint) {
            noFixPoint = false;
            for (int d=0; d<nbDims; d++) {
                if (sumISizes[d] > sumLoadSup[d].get()) {
                    fail();
                }
            }
            for (int b = availableBins.nextSetBit(0); b >= 0; b = availableBins.nextSetBit(b+1)) {
                for (int d=0; d<nbDims; d++) {
                    noFixPoint |= loadInfFiltering(d, b, Math.max(bRLoads[d][b].get(), (int) sumISizes[d] - sumLoadSup[d].get() + loads[d][b].getSup()));
                    noFixPoint |= loadSupFiltering(d, b, Math.min(bTLoads[d][b].get(), (int) sumISizes[d] - sumLoadInf[d].get() + loads[d][b].getInf()));
                }
                noFixPoint |= propagateMultiKnapsack(b);
            }
        }
        assert checkLoadConsistency() && checkCandidatesConsistency();
    }

    @Override
    /**
     * delayed propagation of the bound updates of a bin load variable
     */
    public void awakeOnBounds(int varIdx) throws ContradictionException {
        //ChocoLogging.getSearchLogger().finest(vars[varIdx].pretty() + " awakeOnBounds");
        constAwake(false);
    }

    @Override
    /**
     * propagate the removal of an item-to-bins assignment variable:
     * 1) update the candidate and check to decrease the load UB of each removed bins: binLoad <= binTotalLoad
     * 2) if item is assigned: update the required and check to increase the load LB of the bin: binLoad >= binRequiredLoad
     * @throws ContradictionException on the load variables
     */
    public void awakeOnRemovals(int iIdx, DisposableIntIterator deltaDomain) throws ContradictionException {
        //ChocoLogging.getSearchLogger().finest(vars[iIdx].pretty() + " awakeOnRem");
        if (iIdx < bins.length) {
            try {
                while (deltaDomain.hasNext()) {
                    int bin = deltaDomain.next();
                    if (updateRemoveItemFromBin(iIdx, bin)) {
                        loadSupFiltering(bin, bTLoads);
                    }
                }
            } finally {
                deltaDomain.dispose();
            }
            if (vars[iIdx].isInstantiated()) {
                int bin = vars[iIdx].getVal();
                if (updatePackItemToBin(iIdx, bin)) {
                    loadInfFiltering(bin, bRLoads);
                }
            }
        }
        this.constAwake(false);
    }

    //****************************************************************//
    //********* VARIABLE FILTERING ***********************************//
    //****************************************************************//


    /**
     * update the internal data corresponding to the assignment of an item to a bin:
     * remove the item from the candidate list of the bin and balance its size from the candidate to the required load of the bin
     * @param item item index
     * @param bin bin index
     * @return true if the update had not already been performed
     */
    private boolean updatePackItemToBin(int item, int bin) {
        if (candidates[bin].get(item)) {
            candidates[bin].clear(item);
            if (candidates[bin].isEmpty()) {
                availableBins.clear(bin);
            }
            for (int d=0; d<nbDims; d++) {
                bRLoads[d][bin].add(iSizes[d][item]);
            }
            return true;
        }
        return false;
    }

    /**
     * update the internal data corresponding to the removal of an item from a bin:
     * remove the item from the candidate list of the bin and reduce the candidate load of the bin
     * @param item item index
     * @param bin bin index
     * @return true if the update had not already been performed
     */
    private boolean updateRemoveItemFromBin(int item, int bin) {
        if (candidates[bin].get(item)) {
            candidates[bin].clear(item);
            if (candidates[bin].isEmpty()) {
                availableBins.clear(bin);
            }
            for (int d=0; d<nbDims; d++) {
                bTLoads[d][bin].add(-1 * iSizes[d][item]);
            }
            return true;
        }
        return false;
    }

    /**
     * increase the LB of the bin load and the sum of the bin load LBs
     * @param bin bin index
     * @param newLoads new LB of the bin load
     * @return {@code true} if LB is increased in at least one dimension.
     * @throws ContradictionException on the load[bin] variable
     */
    private boolean loadInfFiltering(int bin, IStateInt[][] newLoads) throws ContradictionException {
        boolean ret = false;
        for (int d=0; d<nbDims; d++) {
            ret |= loadInfFiltering(d, bin, newLoads[d][bin].get());
        }
        return ret;
    }

    /**
     * increase the LB of the bin load and the sum of the bin load LBs
     * @param dim dimension index
     * @param bin bin index
     * @param newLoadInf new LB of the bin load
     * @return {@code true} if LB is increased.
     * @throws ContradictionException on the load[bin] variable
     */
    private boolean loadInfFiltering(int dim, int bin, int newLoadInf) throws ContradictionException {
        int inc = newLoadInf-loads[dim][bin].getInf();
        if (inc>0) {
            loads[dim][bin].updateInf(newLoadInf, this, false);
            sumLoadInf[dim].add(inc);
            return true;
        }
        return false;
    }

    /**
     * decrease the UB of the bin load and the sum of the bin load UBs
     * @param bin bin index
     * @param newLoads new UB of the bin load
     * @return {@code true} if UB is decreased in at least one dimension.
     * @throws ContradictionException on the load[bin] variable
     */
    private boolean loadSupFiltering(int bin, IStateInt[][] newLoads) throws ContradictionException {
        boolean ret = false;
        for (int d=0; d<nbDims; d++) {
            ret |= loadSupFiltering(d, bin, newLoads[d][bin].get());
        }
        return ret;
    }

    /**
     * decrease the UB of the bin load and the sum of the bin load UBs
     * @param dim dimension index
     * @param bin bin index
     * @param newLoadSup new UB of the bin load
     * @return {@code true} if UB is decreased.
     * @throws ContradictionException on the load[bin] variable
     */
    private boolean loadSupFiltering(int dim, int bin, int newLoadSup) throws ContradictionException {
        int dec = newLoadSup - loads[dim][bin].getSup();
        if (dec<0) {
            loads[dim][bin].updateSup(newLoadSup, this, false);
            sumLoadSup[dim].add(dec);
            return true;
        }
        return false;
    }

    /**
     * propagate the knapsack constraint on a given bin:
     * 1) remove the candidate items bigger than the remaining free space (when binRequiredLoad + itemSize > binLoadSup)
     * 2) pack the candidate items necessary to reach the load LB (when binTotalLoad - itemSize < binLoadInf).
     * the loads are also filtered within this constraint (rather in the propagate loop) because considered bins are eventually became unavailable
     * @param bin bin index
     * @return {@code true} if at least one item is removed or packed.
     * @throws ContradictionException on the bins or loads variables
     */
    private boolean propagateMultiKnapsack(int bin) throws ContradictionException {
        int d;
        int item;
        boolean ret = false, up=true;
        for (item = candidates[bin].nextSetBit(0); item >= 0 && up; item = candidates[bin].nextSetBit(item + 1)) {
            up = false;
            for (d=0; d<nbDims && (iSizes[d][item] + bRLoads[d][bin].get() <= loads[d][bin].getSup()); d++);
            if (d<nbDims && updateRemoveItemFromBin(item, bin)) {
                bins[item].removeVal(bin, this, false);
                loadSupFiltering(bin, bTLoads);
                if (bins[item].isInstantiated()) {
                    int b = bins[item].getVal();
                    updatePackItemToBin(item, b);
                        loadInfFiltering(b, bRLoads);
                }
                up = true;
            }
            for (d=0; d<nbDims && (bTLoads[d][bin].get() - iSizes[d][item] >= loads[d][bin].getInf()); d++);
            if (d<nbDims && updatePackItemToBin(item, bin)) {
                DisposableIntIterator domain = bins[item].getDomain().getIterator();
                try {
                    while (domain.hasNext()) {
                        int b = domain.next();
                        if (b != bin) {
                            updateRemoveItemFromBin(item, b);
                            loadSupFiltering(b, bTLoads);

                        }
                    }
                } finally {
                    domain.dispose();
                }
                bins[item].instantiate(bin, this, false);
                loadInfFiltering(bin, bRLoads);

                up = true;
            }
            ret |= up;
        }
        return ret;
    }

    //****************************************************************//
    //********* Checkers *********************************************//
    //****************************************************************//

    /**
     * Check the consistency of the required and candidate loads with regards to the assignment variables:
     * for each bin: sumAssignedItemSizes == binRequiredLoad, sumPossibleItemSizes == binTotalLoad
     * rule 3, for each bin: binRequiredLoad <= binLoad <= binTotalLoad
     * @return {@code false} if not consistent.
     */
    private boolean checkLoadConsistency() {
        boolean check = true;
        for (int d=0; d<nbDims; d++) {
            int[] rs = new int[nbBins];
            int[] cs = new int[nbBins];
            for (int i = 0; i < bins.length; i++) {
                if (bins[i].isInstantiated()) {
                    rs[bins[i].getVal()] += iSizes[d][i];
                } else {
                    DisposableIntIterator it = bins[i].getDomain().getIterator();
                    try {
                        while (it.hasNext()) {
                            int bin = it.next();
                            cs[bin] += iSizes[d][i];
                        }
                    } finally {
                        it.dispose();
                    }
                }
            }

            int sumLoadInf = 0;
            int sumLoadSup = 0;
            for (int b = 0; b < rs.length; b++) {
                if (rs[b] != bRLoads[d][b].get()) {
                    ChocoLogging.getBranchingLogger().warning(loads[d][b].pretty() + " required=" + bRLoads[d][b].get() + " expected=" + rs[b]);
                    check = false;
                }
                if (rs[b]+cs[b] != bTLoads[d][b].get()) {
                    ChocoLogging.getBranchingLogger().warning(loads[d][b].pretty() + " total=" + bTLoads[d][b].get() + " expected=" + (rs[b]+cs[b]));
                    check = false;
                }
                if (loads[d][b].getInf() < rs[b]) {
                    ChocoLogging.getBranchingLogger().warning(loads[d][b].pretty() + " LB expected >=" + rs[b]);
                    check = false;
                }
                if (loads[d][b].getSup() > rs[b]+cs[b]) {
                    ChocoLogging.getBranchingLogger().warning(loads[d][b].pretty() + " UB expected <=" + (rs[b]+cs[b]));
                    check = false;
                }
                sumLoadInf += loads[d][b].getInf();
                sumLoadSup += loads[d][b].getSup();
            }
            if (this.sumLoadInf[d].get() != sumLoadInf) {
                ChocoLogging.getBranchingLogger().warning("Sum Load LB = " + this.sumLoadInf[d].get() + " expected =" + sumLoadInf);
                check = false;
            }
            if (this.sumLoadSup[d].get() != sumLoadSup) {
                ChocoLogging.getBranchingLogger().warning("Sum Load UB = " + this.sumLoadSup[d].get() + " expected =" + sumLoadSup);
                check = false;
            }
        }
        ChocoLogging.flushLogs();
        return check;
    }

    /**
     * Check that the candidate lists are aligned with the assignment variables:
     * item is in candidates[bin] iff bin is in bins[item]
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
                        int bin = it.next();
                        bs[bin].set(i);
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
                    ChocoLogging.flushLogs();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * print the list of candidate items for a given bin
     * @param bin bin index
     * @return list of item indices, between braces, separated by spaces
     */
    public String prettyCandidates(int bin) {
        StringBuilder s = new StringBuilder("{");
        for (int i = candidates[bin].nextSetBit(0); i >= 0; i = candidates[bin].nextSetBit(i + 1)) {
            s.append(i);
            s.append(' ');
        }
        s.append('}');
        return s.toString();
    }

}

