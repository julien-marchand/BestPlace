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

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;


/**
 * Simplified version of {@link choco.cp.solver.constraints.global.pack.PackFiltering}.
 * An assertion checks the consistency of the loads with the set model. So take care of disabling assertions
 * when benching
 *
 * @author Fabien Hermenier
 * @see choco.cp.solver.constraints.global.pack.PackFiltering
 */
public final class SimpleBinPackingFiltering {

    private final SimpleBinPacking cstr;

    /**
     * The constant size of each item.
     */
    protected final int[] iSizes;

    /**
     * The loads of the bins.
     */
    protected final IntDomainVar[] loads;


    private SetVar setBin;

    /**
     * The no fix point.
     */
    private boolean noFixPoint;

    protected final SumDataStruct loadSum;


    /**
     * Instantiates a new 1BP constraint.
     *
     * @param pack the packing constraint
     */
    public SimpleBinPackingFiltering(SimpleBinPacking pack) {
        this.cstr = pack;
        this.loads = cstr.getLoads();
        this.iSizes = this.cstr.getISizes();
        loadSum = new SumDataStruct(loads, computeTotalSize());
    }

    /**
     * Compute the total size and check that sizes are constant.
     */
    private int computeTotalSize() {
        int l = 0;
        int last = Integer.MAX_VALUE;
        int len = iSizes.length;
        for (int i = 0; i < len; i++) {
            int s = iSizes[i];
            if (s > last) {
                throw new SolverException("size must be sorted according to non increasing order");
            } else {
                l += s;
                last = s;
            }
        }
        return l;
    }


    /**
     * Update the minimal load of a given bin.
     *
     * @param bin  the index of bin
     * @param load the new load
     * @throws ContradictionException the contradiction exception
     */
    protected void updateInfLoad(final int bin, final int load) throws ContradictionException {
        noFixPoint |= cstr.updateInfLoad(bin, load);
    }


    /**
     * Update the maximal load of a given bin.
     *
     * @param bin  the index of bin
     * @param load the new load
     * @throws ContradictionException the contradiction exception
     */
    protected void updateSupLoad(final int bin, final int load) throws ContradictionException {
        noFixPoint |= cstr.updateSupLoad(bin, load);
    }

    /**
     * Do not update status
     */
    protected void pack(final int item, final int bin) throws ContradictionException {
        //ChocoLogging.getBranchingLogger().finest("filtering pack(" + item + ", " + bin + ")");
        noFixPoint |= cstr.pack(item, bin);
    }


    /**
     * Do not update status
     */
    protected void remove(final int item, final int bin) throws ContradictionException {
        //ChocoLogging.getBranchingLogger().finest("filtering remove(" + item + ", " + bin + ")");
        noFixPoint |= cstr.remove(item, bin);
    }


    //	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%//
    //	%%%%%%%%%%%%%%%%%%%%%%%%%% TYPICAL MODEL %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%//
    //	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%//


    /**
     * The minimum and maximum load of each bin {@link SimpleBinPacking#loads } is maintained according to the domains of the bin assignment variables.
     *
     * @param bin the index of the bin
     * @throws ContradictionException the contradiction exception
     */
    protected void loadMaintenance(final int bin) throws ContradictionException {
        //sumEnvelop(bin); //Bad for performance!!
        assert checkLoadConsistency(bin);
        updateInfLoad(bin, cstr.getRLoad(bin).get());
        updateSupLoad(bin, cstr.getCLoad(bin).get() + cstr.getRLoad(bin).get());
    }

    /**
     * Check the consistency of the rloads and the cloads with regards ot the set variable denoting the hosting
     * capability of a bin.
     *
     * @param bin the bin to analyse
     * @return {@code false} if the consistency is not viable.
     * @throws ContradictionException if an error occurred
     */
    public boolean checkLoadConsistency(int bin) throws ContradictionException {
        DisposableIntIterator ite = cstr.getSetBin(bin).getDomain().getKernelIterator();
        int r = 0;
        while (ite.hasNext()) {
            r += iSizes[ite.next()];
        }
        ite.dispose();

        ite = cstr.getSetBin(bin).getDomain().getOpenDomainIterator();
        int c = 0;
        while (ite.hasNext()) {
            c += iSizes[ite.next()];
        }
        ite.dispose();

        if (r != cstr.getRLoad(bin).get()) {
            //ChocoLogging.getSearchLogger().finest(loads[bin].pretty() + " env" + cstr.prettyEnvelop(bin) + "=" + cstr.getCLoad(bin).get() + " ker=" + cstr.prettyKernel(bin) + "=" + cstr.getRLoad(bin).get());
            ChocoLogging.getBranchingLogger().warning(loads[bin].pretty() + " current r=" + cstr.getRLoad(bin).get() + " expected=" + r);
            ChocoLogging.flushLogs();
            return false;
        }
        if (c != cstr.getCLoad(bin).get()) {
            //ChocoLogging.getSearchLogger().finest(loads[bin].pretty() + " env" + cstr.prettyEnvelop(bin) + "=" + cstr.getCLoad(bin).get() + " ker=" + cstr.prettyKernel(bin) + "=" + cstr.getRLoad(bin).get());
            ChocoLogging.getBranchingLogger().warning(loads[bin].pretty() + " (r=" + r + ") current c=" + cstr.getCLoad(bin).get() + " expected=" + c);
            ChocoLogging.flushLogs();
            return false;
        }
        return true;
    }

    /**
     * The minimum and maximum load of each bin {@link SimpleBinPacking#loads } is maintained according to the domains of the bin assignment variables.
     *
     * @param bin the bin
     * @throws ContradictionException the contradiction exception
     */
    protected void loadSizeAndCoherence(final int bin) throws ContradictionException {
        loadSum.updateBounds(bin);
        updateInfLoad(bin, loadSum.boundInf);
        updateSupLoad(bin, loadSum.boundSup);
    }

    /**
     * Single item elimination and commitment.
     *
     * @param bin the bin
     * @throws ContradictionException the contradiction exception
     */
    protected void singleItemEliminationAndCommitment(final int bin) throws ContradictionException {
        DisposableIntIterator iter = setBin.getDomain().getOpenDomainIterator();
        final int lInf = loads[bin].getInf();
        final int lSup = loads[bin].getSup();
        try {
            while (iter.hasNext()) {
                final int iIdx = iter.next();
                final int iSize = iSizes[iIdx];
                final int rLoad = cstr.getRLoad(bin).get();
                if (iSize + rLoad > lSup) {
                    remove(iIdx, bin);
                } else if (rLoad + cstr.getCLoad(bin).get() - iSize < lInf) {
                    pack(iIdx, bin);
                } else {
                    break;
                }
            }
        } finally {
            iter.dispose();
        }
    }


    //	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%//
    //	%%%%%%%%%%%%%%%%%%%%%%%%%% ADDITIONAL RULES %%%%%%%%%%%%%%%%%%%%%%%%%%%%//
    //	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%//


    //	****************************************************************//
    //	********* PROPAGATION LOOP *************************************//
    //	****************************************************************//


    public void propagate() throws ContradictionException {
        final IStateIntVector abins = cstr.getAvailableBins();
        final int n = abins.size();
        noFixPoint = true;
        while (noFixPoint) {
            noFixPoint = false;
            loadSum.update();
            for (int i = 0; i < n; i++) {
                propagate(abins.quickGet(i));
            }
        }
        cstr.fireAvailableBins();

    }

    /**
     * @throws ContradictionException the contradiction exception
     */
    private void propagate(final int bin) throws ContradictionException {
        setBin = cstr.getSetBin(bin);
        loadSizeAndCoherence(bin);
        loadMaintenance(bin);
        singleItemEliminationAndCommitment(bin);
    }


    static final class SumDataStruct {

        /**
         * variables to sum
         */
        protected final IntDomainVar[] vars;

        /**
         * the constant sum.
         */
        public final int sum;

        protected int sumMinusInfs;

        protected int sumMinusSups;

        public int boundInf;
        public int boundSup;

        private int size;

        public SumDataStruct(IntDomainVar[] vars, int sum) {
            super();
            this.vars = vars;
            this.sum = sum;
            this.size = vars.length;
        }

        public void update() {
            sumMinusInfs = sum;
            sumMinusSups = sum;
            for (int i = 0; i < size; i++) {
                sumMinusInfs -= vars[i].getInf();
                sumMinusSups -= vars[i].getSup();
            }
        }

        public void updateBounds(int idx) {
            boundInf = sumMinusSups + vars[idx].getSup();
            boundSup = sumMinusInfs + vars[idx].getInf();
        }
    }
}
