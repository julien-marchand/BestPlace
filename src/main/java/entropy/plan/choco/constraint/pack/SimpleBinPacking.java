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

import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;

import java.util.Arrays;

import choco.cp.solver.constraints.global.pack.IPackSConstraint;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.opres.nosum.NoSumList;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.set.AbstractLargeSetIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;

/**
 * A simplified version of {@link choco.cp.solver.constraints.global.pack.PackSConstraint}. Required and candidate
 * loads are computed incrementally while some coding tips speeds up computation. The constraint does not
 * however support all the options in {@link choco.cp.solver.constraints.global.pack.PackSConstraint}
 *
 * @author Fabien Hermenier
 * @see choco.cp.solver.constraints.global.pack.PackSConstraint
 */
public class SimpleBinPacking extends AbstractLargeSetIntSConstraint implements IPackSConstraint, CustomPack {

    public final SimpleBinPackingFiltering filtering;

    protected final BoundNumberOfBins bounds;

    private IStateIntVector availableBins;

    /**
     * The constant size of each item.
     */
    protected final int[] iSizes;

    /**
     * The loads of the bins.
     */
    protected final IntDomainVar[] loads;

    /**
     * The bin of each item.
     */
    protected final IntDomainVar[] bins;


    private SetVar[] bSets;

    private IStateInt[] bCLoads;

    private IStateInt[] bRLoads;

    private IEnvironment env;

    public SimpleBinPacking(IEnvironment environment, SetVar[] itemSets, IntDomainVar[] loads, IntDomainVar[] sizes,
                            IntDomainVar[] bins, IntDomainVar nbNonEmpty) {
        super(ArrayUtils.append(loads, sizes, bins, new IntDomainVar[]{nbNonEmpty}), itemSets);
        this.loads = loads;
        this.env = environment;
        iSizes = new int[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            iSizes[i] = sizes[i].getVal();
        }
        this.bSets = itemSets;
        this.bCLoads = new IStateInt[bSets.length];
        this.bRLoads = new IStateInt[bSets.length];
        this.bins = bins;
        this.bounds = new BoundNumberOfBins();
        filtering = new SimpleBinPackingFiltering(this);
        availableBins = environment.makeBipartiteIntList(ArrayUtils.zeroToN(getNbBins()));

    }

    @Override
    public void fireAvailableBins() {
        final DisposableIntIterator iter = availableBins.getIterator();
        while (iter.hasNext()) {
            final int b = iter.next();
            if (svars[b].isInstantiated()) {
                iter.remove();
            }
        }
        iter.dispose();
    }

    @Override
    public final IStateIntVector getAvailableBins() {
        return availableBins;
    }

    public final int getRequiredSpace(int bin) {
        return bRLoads[bin].get();
    }

    @Override
    public final int getRemainingSpace(int bin) {
        return loads[bin].getSup() - getRequiredSpace(bin);
    }


    protected final boolean isSetEvent(final int varIdx) {
        return varIdx < svars.length;
    }

    protected final boolean isItemEvent(final int varIdx) {
        final int a = 2 * getNbBins() + getNbItems();
        final int b = a + getNbItems();
        return varIdx >= a && varIdx < b;
    }

    protected final int getItemIndex(final int varIdx) {
        return varIdx - 2 * getNbBins() - getNbItems();
    }


    @Override
	public final IntDomainVar[] getBins() {
        return bins;
    }

    public IStateInt getRLoad(int bIdx) {
        return bRLoads[bIdx];
    }

    public IStateInt getCLoad(int bIdx) {
        return bCLoads[bIdx];
    }

    //****************************************************************//
    //********* Filtering interface **********************************//
    //****************************************************************//


    @Override
    public final int getNbBins() {
        return svars.length;
    }

    @Override
    public final int getNbItems() {
        return iSizes.length;
    }


    @Override
    public final IntDomainVar[] getLoads() {
        return loads;
    }


    @Override
    public final int[] getSizes() {
        return null;
    }

    public final int[] getISizes() {
        return iSizes;
    }

    public SetVar getSetBin(int bIdx) {
        return bSets[bIdx];
    }

    @Override
    public final NoSumList getStatus(int bin) {
        return null;
    }

    @Override
    public final boolean pack(int item, int bin) throws ContradictionException {
        //ChocoLogging.getSearchLogger().finest(svars[bin].pretty() + " pack(" + item + ", " + bin + ")");
        boolean ret = false;
        if (svars[bin].getDomain().getEnveloppeDomain().contains(item) && svars[bin].addToKernel(item, this, false)) {
            ret = true;
            bRLoads[bin].add(iSizes[item]);
            bCLoads[bin].add(-1 * iSizes[item]);
        }
        if (svars[bin].isInDomainKernel(item) && bins[item].fastCanBeInstantiatedTo(bin)) {
            //Update the cLoads & rLoads of the bin the item is packed in

            /* final DisposableIntIterator iter = bins[item].getDomain().getIterator();
        //remove from other env
        try {
            while (iter.hasNext()) {
                final int b = iter.next();
                if (bin != b) {
                    //ChocoLogging.getSearchLogger().finest("Remove " + item + " from env of " + b);
                    if (svars[b].remFromEnveloppe(item, this, false)) {
                        bCLoads[b].add(-1 * iSizes[item]);
                    }
                }
            }
        } finally {
            iter.dispose();
        }    */
            //remove from other env
            for (int b = bins[item].getInf(); b <= bins[item].getSup(); b = bins[item].getNextDomainValue(b)) {
                if (bin != b) {
                    //ChocoLogging.getSearchLogger().finest("Remove " + item + " from env of " + b);
                    if (svars[b].remFromEnveloppe(item, this, false)) {
                        bCLoads[b].add(-1 * iSizes[item]);
                    }
                }
            }
        } else {
            this.fail();
        }
        bins[item].instantiate(bin, this, true);
        return ret;
    }

    public final boolean simplePack(int item, int bin) throws ContradictionException {
        //ChocoLogging.getSearchLogger().finest(svars[bin].pretty() + " simplePack(" + item + ", " + bin + ")");
        boolean ret = false;
        if (svars[bin].getDomain().getEnveloppeDomain().contains(item) && svars[bin].addToKernel(item, this, true)) {
            ret = true;
        }
        if (svars[bin].isInDomainKernel(item) && bins[item].fastCanBeInstantiatedTo(bin)) {
            //Update the cLoads & rLoads of the bin the item is packed in
/*            final DisposableIntIterator iter = bins[item].getDomain().getIterator();
            //remove from other env
            try {
                while (iter.hasNext()) {
                    final int b = iter.next();
                    if (bin != b) {
                        //ChocoLogging.getSearchLogger().finest("Remove " + item + " from env of " + b);
                        ret |= svars[b].remFromEnveloppe(item, this, true);
                    }
                }
            } finally {
                iter.dispose();
            }  */
            //remove from other env
            for (int b = bins[item].getInf(); b <= bins[item].getSup(); b = bins[item].getNextDomainValue(b)) {
                if (bin != b) {
                    //ChocoLogging.getSearchLogger().finest("Remove " + item + " from env of " + b);
                    ret |= svars[b].remFromEnveloppe(item, this, true);
                }
            }

        } else {
            this.fail();
        }
        bins[item].instantiate(bin, this, true);
        return ret;
    }

    @Override
    public final boolean remove(int item, int bin) throws ContradictionException {
        //ChocoLogging.getSearchLogger().finest(svars[bin].pretty() + " remove(" + item + ", " + bin + ")");
        boolean res = svars[bin].remFromEnveloppe(item, this, false);
        if (res) {
            bCLoads[bin].add(-1 * iSizes[item]);
        }
        bins[item].removeVal(bin, this, true);
        if (bins[item].isInstantiated()) {
            final int b = bins[item].getVal();
            if (svars[b].addToKernel(item, this, false)) {
                //ChocoLogging.getSearchLogger().finest(svars[bin].pretty() + " remove may " + item + " add to the kernel of " + b);
                bRLoads[b].add(iSizes[item]);
                bCLoads[b].add(-1 * iSizes[item]);
            }
        }
        return res;
    }

    public final boolean simpleRemove(int item, int bin) throws ContradictionException {
        //ChocoLogging.getSearchLogger().finest(svars[bin].pretty() + " simpleRemove(" + item + ", " + bin + ")");
        boolean res = svars[bin].remFromEnveloppe(item, this, true);
        bins[item].removeVal(bin, this, true);
        if (bins[item].isInstantiated()) {
            final int b = bins[item].getVal();
            if (svars[b].addToKernel(item, this, true)) {
                //ChocoLogging.getSearchLogger().finest(svars[bin].pretty() + " remove may " + item + " add to the kernel of " + b);
            }
        }
        return res;
    }

    @Override
    public final boolean updateInfLoad(int bin, int load) throws ContradictionException {
        return loads[bin].updateInf(load, this, true);
    }

    @Override
    public final boolean updateNbNonEmpty(int min, int max) throws ContradictionException {
        boolean res = false;
        final int idx = ivars.length - 1;
        ivars[idx].updateInf(min, this, true);
        ivars[idx].updateSup(max, this, true);
        return res;
    }

    @Override
    public final boolean updateSupLoad(int bin, int load) throws ContradictionException {
        return loads[bin].updateSup(load, this, true);
    }


    //****************************************************************//
    //********* Events *******************************************//
    //****************************************************************//


    @Override
    public boolean isConsistent() {
        // really no idea. wait and propagate
        return false;
    }

    protected final void checkBounds(int item) throws ContradictionException {
        bins[item].updateInf(0, this, true);
        bins[item].updateSup(svars.length - 1, this, true);
    }

    protected final void checkEnveloppes() throws ContradictionException {
        for (int bin = 0; bin < svars.length; bin++) {
            int inf;
            // check if envelope is empty, to avoid infinite loop
            while ((inf = svars[bin].getEnveloppeInf()) < 0 && svars[bin].remFromEnveloppe(inf, this, true)) {
                //bCLoads[bin].add(-1 * iSizes[inf]);
                //assert(bCLoads[bin].get() >= 0);
            }
            int sup;
            // check if envelope is empty, to avoid infinite loop
            while ((sup = svars[bin].getEnveloppeSup()) > bins.length - 1 &&
                    svars[bin].remFromEnveloppe(sup, this, true)) {
                //bCLoads[bin].add(-1 * iSizes[sup]);
                //assert(bCLoads[bin].get() >= 0);
            }
        }
    }

    @Override
    public void awake() throws ContradictionException {
        //initial channeling
        checkEnveloppes();
        for (int item = 0; item < bins.length; item++) {
            checkBounds(item);
            if (bins[item].isInstantiated()) {
                //the item is packed
                final int b0 = bins[item].getVal();
                svars[b0].addToKernel(item, this, false);
                for (int b = 0; b < b0; b++) {
                    svars[b].remFromEnveloppe(item, this, false);
                }
                for (int b = b0 + 1; b < svars.length; b++) {
                    svars[b].remFromEnveloppe(item, this, false);
                }
            } else {
                for (int bin = 0; bin < svars.length; bin++) {
                    if (svars[bin].isInDomainEnveloppe(item)) {
                        //item could be packed here
                        if (svars[bin].isInDomainKernel(item)) {
                            //item is packed
                            bins[item].instantiate(bin, this, false);
                        } else if (!bins[item].fastCanBeInstantiatedTo(bin)) {
                            //in fact, channeling fails
                            svars[bin].remFromEnveloppe(item, this, false);
                        }
                        //channeling ok envelope-domain
                    } else {
                        //otherwise remove from domain
                        bins[item].removeVal(bin, this, false);
                    }
                }
            }
        }

        //Initial r & c load
        DisposableIntIterator iterK = null;
        DisposableIntIterator iterE = null;

        try {
            for (int i = 0; i < bSets.length; i++) {
                SetVar s = bSets[i];
                int r = 0;

                iterK = s.getDomain().getKernelIterator();
                while (iterK.hasNext()) {
                    r += iSizes[iterK.next()];
                }

                int c = 0;
                iterE = s.getDomain().getOpenDomainIterator();
                while (iterE.hasNext()) {
                    c += iSizes[iterE.next()];
                }
                bRLoads[i] = env.makeInt(r);
                bCLoads[i] = env.makeInt(c);
                assert filtering.checkLoadConsistency(i);
            }
        } finally {
            if (iterK != null) {
                iterK.dispose();
            }
            if (iterE != null) {
                iterE.dispose();
            }

        }
/*        for (int i = 0; i < svars.length; i++) {
            ChocoLogging.getSearchLogger().finest(svars[i].pretty());
        }*/
    }

    protected void checkDeltaDomain(int item) throws ContradictionException {
        final DisposableIntIterator iter = bins[item].getDomain().getDeltaIterator();
        if (iter.hasNext()) {
            try {
                while (iter.hasNext()) {
                    final int b = iter.next();
                    svars[b].remFromEnveloppe(item, this, true);
                }
            } finally {
                iter.dispose();
            }
        } else {
            throw new SolverException("empty delta domain for bin " + item);
        }
    }

    @Override
    public void awakeOnBounds(int varIndex) throws ContradictionException {
        //ChocoLogging.getSearchLogger().finest(pretty() +" awakeOnBounds(" + varIndex + ")");
        if (isItemEvent(varIndex)) {
            final int item = getItemIndex(varIndex);
            //the item is not packed
            //so, we can safely remove from other envelopes
            checkDeltaDomain(item);
        }
        this.constAwake(false);
    }


    @Override
    public void awakeOnInf(int varIdx) throws ContradictionException {
        awakeOnBounds(varIdx);
    }

    @Override
    public void awakeOnInst(int varIdx) throws ContradictionException {
        if (isSetEvent(varIdx)) {
            //ChocoLogging.getSearchLogger().finest(svars[varIdx].pretty() + " awakeOnSetInst(" + svars[varIdx].pretty() + ") r=" + bRLoads[varIdx].get() + " c=" + bCLoads[varIdx].get());
            DisposableIntIterator iter = svars[varIdx].getDomain().getKernelIterator();
            try {
                while (iter.hasNext()) {
                    final int item = iter.next();
                    if (!bins[item].isInstantiated()) {
                        simplePack(item, varIdx);
                    }
                }
            } finally {
                iter.dispose();
            }
            iter = svars[varIdx].getDomain().getEnveloppeDomain().getDeltaIterator();
            try {
                while (iter.hasNext()) {
                    final int item = iter.next();
                    if (bins[item].fastCanBeInstantiatedTo(varIdx)) {
                        simpleRemove(item, varIdx);
                    }
                }
            } finally {
                iter.dispose();
            }
        } else if (isItemEvent(varIdx)) {
            final int item = getItemIndex(varIdx);
            //ChocoLogging.getSearchLogger().finest(bins[item].pretty() +" awakeOnBinInst(" +bins[item].getVal()+ ")");
            final int b = bins[item].getVal();
            checkDeltaDomain(item);
            simplePack(item, b);
        }
        constAwake(false);
    }

    @Override
    public void awakeOnKer(int varIdx, int x) throws ContradictionException {
        //ChocoLogging.getSearchLogger().finest(svars[varIdx].pretty() +" awakeOnKer: +item " + x + " (" + bRLoads[varIdx].get() + "+" + iSizes[x] + ")");
        this.bRLoads[varIdx].add(iSizes[x]);
        this.bCLoads[varIdx].add(-1 * iSizes[x]);
        simplePack(x, varIdx);
        //ChocoLogging.getSearchLogger().finest(svars[varIdx].pretty() +" awakeOnKer: r=" + bRLoads[varIdx].get());
        this.constAwake(false);
    }

    @Override
    public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
        //ChocoLogging.getSearchLogger().finest(svars[varIdx].pretty() + " awakeOnEnv: -item " + x + " (" + bCLoads[varIdx].get() + "-" + iSizes[x] + ")");
        bCLoads[varIdx].add(-1 * iSizes[x]);
        bins[x].removeVal(varIdx, this, true);

        //if the item is packed, update variables
        if (bins[x].isInstantiated()) {
            final int b = bins[x].getVal();
            svars[b].addToKernel(x, this, true);
        }
        this.constAwake(false);
    }

    @Override
    public void awakeOnRem(int varIdx, int val) throws ContradictionException {
        if (isItemEvent(varIdx)) {
            int item = getItemIndex(varIdx);
            //ChocoLogging.getSearchLogger().finest(bins[item].pretty() + " awakeOnRem(" + item + ", " + val + ")");
            svars[val].remFromEnveloppe(item, this, true);

            if (bins[item].isInstantiated()) {
                final int b = bins[item].getVal();
                svars[b].addToKernel(item, this, true);
            }
        }
        this.constAwake(false);
    }

    @Override
    public void awakeOnSup(int varIdx) throws ContradictionException {
        //ChocoLogging.getSearchLogger().finest(pretty() + " awakeOnSup(" + varIdx + ")");
        awakeOnBounds(varIdx);
    }

    @Override
    public void propagate() throws ContradictionException {
        do {
            //ChocoLogging.getSearchLogger().finest("propagate " + pretty());
            filtering.propagate();
            if (!bounds.computeBounds(false)) {
                fail();
            }
        } while (updateNbNonEmpty(bounds.getMinimumNumberOfBins(), bounds.getMaximumNumberOfBins()));


    }

    @Override
    public boolean isSatisfied() {
        int[] l = new int[loads.length];
        int[] c = new int[loads.length];
        for (int i = 0; i < bins.length; i++) {
            final int b = bins[i].getVal();
            if (!svars[b].isInDomainKernel(i)) {
                ChocoLogging.getBranchingLogger().warning("Bad channeling for " + svars[b] + " should contains " + i);
                return false; //check channeling
            }
            l[b] += iSizes[i];
            c[b]++;
        }
        int nbb = 0;
        for (int i = 0; i < loads.length; i++) {
            if (svars[i].getCard().getVal() != c[i]) {
                ChocoLogging.getBranchingLogger().warning("card of set " + i + " = " + svars[i].getCard().getVal() + " expected=" + c[i]);
                return false; //check cardinality
            }
            if (loads[i].getVal() != l[i]) {
                ChocoLogging.getBranchingLogger().warning("Load of " + i + " = " + loads[i].getVal() + " expected=" + l[i]);
                return false; //check load
            }
            if (c[i] != 0) {
                nbb++;
            }
        }
        boolean b = ivars[ivars.length - 1].getVal() == nbb;
        if (!b) {
            ChocoLogging.getBranchingLogger().warning("Bad number of bins: " + ivars[ivars.length - 1].getVal() + " but want " + nbb);
        }
        return b;//check number of bins
    }

    protected final class BoundNumberOfBins {

        private final int[] remainingSpace;

        private final TIntArrayList itemsMLB;

        protected int capacityMLB;

        private final TIntArrayList binsMLB;

        private int sizeIMLB;

        private int totalSizeCLB;

        private final TIntArrayList binsCLB;

        protected int nbEmpty;

        protected int nbSome;

        protected int nbFull;

        protected int nbNewCLB;

        private final TIntProcedure minimumNumberOfNewBins = new TIntProcedure() {
            @Override
            public boolean execute(int arg0) {
                nbNewCLB++;
                if (totalSizeCLB <= arg0) {
                    return false;
                }
                totalSizeCLB -= arg0;
                return true;
            }
        };


        public BoundNumberOfBins() {
            itemsMLB = new TIntArrayList(getNbBins() + getNbItems());
            binsMLB = new TIntArrayList(getNbBins());
            binsCLB = new TIntArrayList(getNbBins());
            remainingSpace = new int[getNbBins()];
        }


        public void reset() {
            Arrays.fill(remainingSpace, 0);
            itemsMLB.resetQuick();
            capacityMLB = 0;
            binsMLB.resetQuick();
            totalSizeCLB = 0;
            binsCLB.resetQuick();
            nbEmpty = 0;
            nbSome = 0;
            nbFull = 0;
            nbNewCLB = 0;
        }

        /**
         * add unpacked items (MLB) compute their total size (CLB).
         */
        private void handleItems() {
            final int n = getNbItems();
            for (int i = 0; i < n; i++) {
                final int iSize = iSizes[i];
                if (bins[i].isInstantiated()) {
                    remainingSpace[bins[i].getVal()] -= iSize;
                } else {
                    totalSizeCLB += iSize;
                    itemsMLB.add(iSize);
                }
            }
            sizeIMLB = itemsMLB.size();
        }


        /**
         * compute the remaining space in each bin and the cardinality of sets (empty, partially filled, full)
         */
        private void handleBins() {
            final int n = getNbBins();
            //compute the number of empty, partially filled and closed bins
            //also compute the remaining space in each open bins
            for (int b = 0; b < n; b++) {
                if (svars[b].isInstantiated()) {
                    //we ignore closed bins
                    if (loads[b].isInstantiatedTo(0)) {
                        nbEmpty++;
                    } else {
                        nbFull++;
                    }
                } else {
                    //the bins is used by the modified lower bound
                    binsMLB.add(b);
                    remainingSpace[b] += loads[b].getSup();
                    capacityMLB = Math.max(capacityMLB, remainingSpace[b]);
                    if (svars[b].getKernelDomainSize() > 0) {
                        //partially filled
                        nbSome++;
                        totalSizeCLB -= remainingSpace[b]; //fill partially filled bin before empty ones
                    } else {
                        //still empty
                        binsCLB.add(remainingSpace[b]); //record empty bins to fill them later
                    }
                }
            }
        }

        private void computeMinimumNumberOfNewBins() {
            binsCLB.sort();
            binsCLB.forEachDescending(minimumNumberOfNewBins);
        }

        /**
         * @param useDDFF do we use advanced and costly bounding procedure for a feasibility test.
         * @return <code>false</code>  if the current state is infeasible.
         */
        public boolean computeBounds(boolean useDDFF) {
            reset();
            //the order of the following calls is important
            handleItems();
            handleBins();
            if (!itemsMLB.isEmpty()) {
                //if( sizeMLB < maximumNumberOfNewBins.get() ) maximumNumberOfNewBins.set(sizeMLB);
                //there is unpacked items
                if (totalSizeCLB > 0) {
                    //compute an estimation of the minimal number of additional bins.
                    if (binsCLB.isEmpty()) {
                        return false;  //no more available bins for remaining unpacked items
                    }
                    computeMinimumNumberOfNewBins();
                }
                if (getMinimumNumberOfBins() > ivars[ivars.length - 1].getSup()) {
                    return false; //the continuous bound prove infeasibility
                }
            }
            return true;
        }

        public int getMaximumNumberOfBins() {
            return Math.min(getNbBins() - nbEmpty, nbFull + nbSome + sizeIMLB);
        }

        public int getMinimumNumberOfBins() {
            return nbFull + nbSome + nbNewCLB;
        }
    }

    public String prettyEnvelop(int bIdx) {
        DisposableIntIterator ite = svars[bIdx].getDomain().getOpenDomainIterator();
        StringBuilder b = new StringBuilder("{");
        while (ite.hasNext()) {
            int i = ite.next();
            b.append(i);
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        ite.dispose();
        b.append("}");
        return b.toString();
    }

    public String prettyKernel(int bIdx) {
        DisposableIntIterator ite = svars[bIdx].getDomain().getKernelIterator();
        StringBuilder b = new StringBuilder("{");
        while (ite.hasNext()) {
            int i = ite.next();
            b.append(i);
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        ite.dispose();
        b.append("}");
        return b.toString();
    }

    @Override
    public IStateBitSet getCandidates(int bin) {
        return null;
    }
}