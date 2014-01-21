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
import entropy.configuration.DefaultConfiguration;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.TimedReconfigurationPlanModelHelper;

/**
 * Simple unit tests for Slice.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestSlice {

    /**
     * Test the instantiation and the getters.
     */
    public void testGets() {
        CPSolver s = new CPSolver();
        IntDomainVar start = s.createBoundIntVar("start", 0, 10);
        IntDomainVar end = s.createBoundIntVar("end", 0, 10);
        IntDomainVar duration = s.createBoundIntVar("duration", 0, 10);
        IntDomainVar host = s.createBoundIntVar("host", 0, 10);
        Slice slice = new Slice("myName", host, s.createTaskVar("", start, end, duration), 1, 2);
        Assert.assertEquals(slice.start(), start);
        Assert.assertEquals(slice.end(), end);
        Assert.assertEquals(slice.duration(), duration);
        Assert.assertEquals(slice.hoster(), host);
        Assert.assertEquals(slice.getName(), "myName");
        Assert.assertEquals(slice.getCPUheight(), 1);
        Assert.assertEquals(slice.getMemoryheight(), 2);
    }

    /**
     * Dummy test to avoid NullPointerException.
     */
    public void testPretty() {
        CPSolver s = new CPSolver();
        IntDomainVar start = s.createBoundIntVar("start", 0, 10);
        IntDomainVar end = s.createBoundIntVar("end", 0, 10);
        IntDomainVar duration = s.createBoundIntVar("duration", 0, 10);
        IntDomainVar host = s.createBoundIntVar("host", 0, 10);
        Slice task = new Slice("myName", host, s.createTaskVar("", start, end, duration), 1, 5);
        Assert.assertNotNull(task.pretty());
        Assert.assertFalse(task.pretty().contains("null"));
    }

    /**
     * Test isEquivalent() in various situations.
     */
    public void testIsEquivalent() {
        CPSolver s = new CPSolver();
        IntDomainVar start = s.createBoundIntVar("start", 0, 10);
        IntDomainVar end = s.createBoundIntVar("end", 0, 10);
        IntDomainVar duration = s.createBoundIntVar("duration", 0, 10);
        IntDomainVar host = s.createBoundIntVar("host", 0, 10);
        Slice task = new Slice("myName", host, s.createTaskVar("", start, end, duration), 1, 3);
        Slice task2 = new Slice("myName2", host, s.createTaskVar("", start, end, duration), 1, 3);
        Assert.assertTrue(task.equals(task2));
        Assert.assertFalse(task.equals(null));
        IntDomainVar h2 = s.createBoundIntVar("host2", 0, 10);
        task2 = new Slice("myName2", h2, s.createTaskVar("", start, end, duration), 1, 2);
        Assert.assertFalse(task.equals(task2));
    }

    public void testFixDuration() {
        CPSolver s = new CPSolver();
        IntDomainVar start = s.createBoundIntVar("start", 0, 10);
        IntDomainVar end = s.createBoundIntVar("end", 0, 10);
        IntDomainVar duration = s.createBoundIntVar("duration", 0, 10);
        IntDomainVar host = s.createBoundIntVar("host", 0, 10);
        Slice slice = new Slice("myName", host, s.createTaskVar("", start, end, duration), 1, 2);
        slice.fixDuration(2);
        Assert.assertEquals(slice.duration().getDomainSize(), 1);
        Assert.assertEquals(slice.duration().getInf(), 2);
        Assert.assertEquals(slice.duration().getSup(), 2);
    }

    /**
     * Not really a good test.
     */
    public void testAddToModel() {
        DefaultConfiguration src = new DefaultConfiguration();
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, src);
        IntDomainVar start = model.createBoundIntVar("start", 0, 10);
        IntDomainVar end = model.createBoundIntVar("end", 0, 10);
        IntDomainVar duration = model.createBoundIntVar("duration", 0, 10);
        IntDomainVar host = model.createBoundIntVar("host", 0, 10);
        Slice slice = new Slice("myName", host, model.createTaskVar("", start, end, duration), 1, 2);
        int x = model.getNbIntVars();
        int y = model.getNbIntConstraints();

        slice.addToModel(model);
        Assert.assertEquals(model.getNbIntVars() - x, 0);
        Assert.assertEquals(model.getNbIntConstraints() - y, 2);
    }
}
