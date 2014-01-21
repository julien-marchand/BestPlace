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
package entropy.plan.action;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import entropy.configuration.DefaultConfiguration;
import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.execution.TimedExecutionGraph;
import entropy.plan.MockPlanVisualizer;

/**
 * Unit tests for Run.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestRun {

    /**
     * Dummy test to prevent NullPointerException.
     */
    public void testToString() {
        Run s = new Run(new DefaultVirtualMachine("VM1", 1, 2, 3), new DefaultNode("n1", 1, 2, 3));
        Assert.assertNotNull(s.toString());
    }


    /**
     * Dummy test to check call to visu.inject(this).
     */
    public void testVisualizationInjection() {
        MockPlanVisualizer visu = new MockPlanVisualizer();
        Run s = new Run(new DefaultVirtualMachine("VM1", 1, 2, 3), new DefaultNode("n1", 1, 2, 3));
        s.injectToVisualizer(visu);
        Assert.assertTrue(visu.isInjected(s));
    }

    /**
     * Test apply().
     */
    public void testApply() {
        DefaultConfiguration c = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("n1", 1, 2, 3);
        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("vm1", 1, 1, 1);
        c.addOnline(n1);
        c.addWaiting(vm1);
        Run r = new Run(vm1, n1);
        r.apply(c);
        Assert.assertEquals(c.getLocation(vm1), n1);
    }

    /**
     * Test isCompatibleWith() when all is right.
     */
    public void testIsCompatibleWith() {
        DefaultConfiguration c = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("n1", 1, 2, 3);
        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("vm1", 1, 1, 1);
        c.addOnline(n1);
        c.addWaiting(vm1);
        Run r = new Run(vm1, n1);
        try {
            Assert.assertTrue(r.apply(c));
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test isIncompatibleWith() when the action is not right.
     *
     * @param r the bad action
     */
    @Test(dataProvider = "getWrongRuns")
    public void testIsIncompatibleWith(Run r) {
        DefaultConfiguration c = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("n1", 1, 2, 3);
        DefaultNode n2 = new DefaultNode("n2", 1, 2, 3);
        c.addOnline(n1);
        c.addOffline(n2);
        c.addWaiting(new DefaultVirtualMachine("VM1", 1, 2, 3));
        c.setRunOn(new DefaultVirtualMachine("VM10", 1, 2, 3), n1);
        Assert.assertFalse(r.isCompatibleWith(c));
    }

    /**
     * Data provider for testWrongMigrationAppends(...).
     *
     * @return sample datas.
     */
    @DataProvider(name = "getWrongRuns")
    public Object[][] getWrongRuns() {
        return new Object[][]{
                {new Run(new DefaultVirtualMachine("VM5", 1, 1, 1), new DefaultNode("n1", 1, 2, 3))}, //a unknown VM
                {new Run(new DefaultVirtualMachine("VM10", 1, 1, 1), new DefaultNode("n1", 1, 2, 3))}, //bad state
                {new Run(new DefaultVirtualMachine("VM1", 1, 1, 1), new DefaultNode("n2", 1, 2, 3))}, //migration on a unknown node
        };
    }

    public void testInsertIntoGraph() {
        TimedExecutionGraph graph = new TimedExecutionGraph();
        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("VM1", 1, 2, 3);
        DefaultNode n1 = new DefaultNode("N1", 1, 2, 3);
        Run ma = new Run(vm1, n1);
        ma.insertIntoGraph(graph);
        Assert.assertTrue(graph.getIncomingsFor(n1).contains(ma));
    }
}
