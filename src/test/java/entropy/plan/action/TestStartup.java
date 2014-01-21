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
import entropy.execution.TimedExecutionGraph;
import entropy.plan.MockPlanVisualizer;

/**
 * Unit tests for the action Startup.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestStartup {

    /**
     * Test the getters.
     */
    public void testGets() {
        Startup s = new Startup(new DefaultNode("N1", 1, 1, 1));
        Assert.assertEquals(s.getNode(), new DefaultNode("N1", 1, 1, 1));
    }

    /**
     * Test  equals().
     */
    public void testEquals() {
        Startup s = new Startup(new DefaultNode("N1", 1, 1, 1));
        Startup s2 = new Startup(new DefaultNode("N1", 1, 1, 1));
        Startup s3 = new Startup(new DefaultNode("N2", 1, 1, 1));
        Assert.assertEquals(s, s2);
        Assert.assertNotSame(s, s3);
    }

    /**
     * Dummy test to prevent NullPointerException.
     */
    public void testToString() {
        Startup s = new Startup(new DefaultNode("N1", 1, 1, 1));
        Assert.assertNotNull(s.toString());
    }

    /**
     * Dummy test to check call to visu.inject(this).
     */
    public void testVisualizationInjection() {
        MockPlanVisualizer visu = new MockPlanVisualizer();
        Startup s = new Startup(new DefaultNode("N1", 1, 1, 1));
        s.injectToVisualizer(visu);
        Assert.assertTrue(visu.isInjected(s));
    }

    /**
     * Test apply().
     */
    public void testApply() {
        DefaultConfiguration c = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("n1", 1, 2, 3);
        c.addOffline(n1);
        Startup s = new Startup(n1);
        s.apply(c);
        Assert.assertTrue(c.getOnlines().contains(n1));
    }

    /**
     * Test isCompatibleWith() when all is right.
     */
    public void testIsCompatibleWith() {
        DefaultConfiguration c = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("n1", 1, 2, 3);
        c.addOffline(n1);
        Startup s = new Startup(n1);
        try {
            Assert.assertTrue(s.apply(c));
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test isIncompatibleWith() when the action is not right.
     *
     * @param s the bad action
     */
    @Test(dataProvider = "getWrongStartups")
    public void testIsIncompatibleWith(Startup s) {
        DefaultConfiguration c = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("n1", 1, 2, 3);
        c.addOnline(n1);
        Assert.assertFalse(s.isCompatibleWith(c));
    }

    /**
     * Data provider for testWrongMigrationAppends(...).
     *
     * @return sample datas.
     */
    @DataProvider(name = "getWrongStartups")
    public Object[][] getWrongStartups() {
        return new Object[][]{
                {new Startup(new DefaultNode("n1", 1, 2, 3))}, //host running VM
        };
    }

    public void testInsertIntoGraph() {
        TimedExecutionGraph graph = new TimedExecutionGraph();
        DefaultNode n1 = new DefaultNode("N1", 1, 2, 3);
        Startup ma = new Startup(n1);
        ma.insertIntoGraph(graph);
        Assert.assertTrue(graph.getOutgoingsFor(n1).contains(ma));
    }
}
