/*
 * Copyright (c) 2010 Ecole des Mines de Nantes.
 *
 *      This file is part of Entropy.
 *
 *      Entropy is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      Entropy is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.plan.choco.actionModel.slice;

import org.testng.Assert;
import org.testng.annotations.Test;

import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Unit tests for SlicesUtils.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestSlices {

    private static CPSolver s = new CPSolver();

    private Slice[] buildSlices() {

        int size = 5;
        Slice[] slices = new Slice[size];
        for (int i = 0; i < size; i++) {
            slices[i] = new Slice("s" + i,
                    s.createBoundIntVar("h" + i, 0, 5),
                    s.createTaskVar("toto",
                            s.makeConstantIntVar(0),
                            s.makeConstantIntVar(5),
                            s.makeConstantIntVar(5)),
                    i,
                    i * 2
            );
        }
        return slices;
    }

    public void testExtractHosters() {
        Slice[] slices = buildSlices();
        IntDomainVar[] hosters = Slices.extractHosters(slices);
        for (int i = 0; i < hosters.length; i++) {
            Assert.assertEquals(hosters[i].getName(), "h" + i);
        }
    }

    public void testExtractCPUHeights() {
        Slice[] slices = buildSlices();
        int[] cpus = Slices.extractCPUHeights(slices);
        for (int i = 0; i < cpus.length; i++) {
            Assert.assertEquals(cpus[i], i);
        }
    }

    public void testExtractMemoryHeights() {
        Slice[] slices = buildSlices();
        int[] mems = Slices.extractMemoryHeights(slices);
        for (int i = 0; i < mems.length; i++) {
            Assert.assertEquals(mems[i], i * 2);
        }
    }

}
