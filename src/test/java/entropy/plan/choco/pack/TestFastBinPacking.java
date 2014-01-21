package entropy.plan.choco.pack;


import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.ElementV;
import choco.cp.solver.search.BranchingFactory;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.plan.choco.constraint.pack.FastBinPacking;

/**
 * created sofdem - 18/10/11
 *
 * @author Sophie Demassey
 */
public class TestFastBinPacking {


    Solver s;
    IntDomainVar[] loads;
    IntDomainVar[] sizes;
    IntDomainVar[] bins;


    public void modelPack(int nBins, int capa, int nItems, int height) {
        int[] heights = new int[nItems];
        Arrays.fill(heights, height);
        modelPack(nBins, capa, heights);
    }

    public void modelPack(int nBins, int capa, int[] height) {
        int[] capas = new int[nBins];
        Arrays.fill(capas, capa);
        modelPack(capas, height);
    }

    public void modelPack(int[] capa, int[] height) {
        int nBins = capa.length;
        int nItems = height.length;
        s = new CPSolver();
        loads = new IntDomainVar[nBins];
        sizes = new IntDomainVar[nItems];
        bins = new IntDomainVar[nItems];
        for (int i = 0; i < nBins; i++) {
            loads[i] = s.createBoundIntVar("l" + i, 0, capa[i]);
        }
        for (int i = 0; i < nItems; i++) {
            sizes[i] = s.createIntegerConstant("s" + i, height[i]);
            bins[i] = s.createEnumIntVar("b" + i, 0, nBins);
        }
        SConstraint cPack = new FastBinPacking(s.getEnvironment(), loads, sizes, bins);
        s.post(cPack);
    }

    public void testPack(int nbSol) {
        s.getConfiguration().putFalse(Configuration.STOP_AT_FIRST_SOLUTION);
        s.generateSearchStrategy();
        s.launch();
        System.out.println(s.getNbSolutions());
        Assert.assertEquals((boolean) s.isFeasible(), nbSol != 0, "SAT");
        if (nbSol > 0) {
            Assert.assertEquals(s.getNbSolutions(), nbSol, "#SOL");
        }
        s.clear();
    }

    @Test(groups = {"unit"}, sequential = true)
    public void testLoadSup() {
        modelPack(5, 5, 5, 2);
        s.addGoal(BranchingFactory.minDomMinVal(s, bins));
        testPack(2220);
    }

    @Test(groups = {"unit"}, sequential = true)
    public void testGuillaume() {
        modelPack(2, 100, 3, 30);
        IntDomainVar margeLoad = s.createBoundIntVar("margeLoad", 0, 50);
        s.post(nth(bins[0], loads, margeLoad));
        ChocoLogging.setVerbosity(Verbosity.SILENT);
        testPack(2);
    }

    /**
     * var = array[index]
     */
    public SConstraint nth(IntDomainVar index, IntDomainVar[] array, IntDomainVar var) {
        return new ElementV(ArrayUtils.append(array, new IntDomainVar[]{index, var}), 0, s.getEnvironment());
    }


}