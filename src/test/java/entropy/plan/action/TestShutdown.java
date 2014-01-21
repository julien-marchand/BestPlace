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
 * Unit tests for the action Shutdown.
 *
 * @author fabien Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestShutdown {

    /**
     * Test the getters.
     */
    public void testGets() {
        Shutdown s = new Shutdown(new DefaultNode("N1", 1, 1, 1));
        Assert.assertEquals(s.getNode(), new DefaultNode("N1", 1, 1, 1));
    }

    /**
     * Test the equals() method for various situations.
     */
    public void testEquals() {
        Shutdown s = new Shutdown(new DefaultNode("N1", 1, 1, 1));
        Shutdown s2 = new Shutdown(new DefaultNode("N1", 1, 1, 1));
        Shutdown s3 = new Shutdown(new DefaultNode("N2", 1, 1, 1));
        Assert.assertEquals(s, s2);
        Assert.assertNotSame(s, s3);
    }

    /**
     * Dummy test to prevent NullPointerException.
     */
    public void testToString() {
        Shutdown s = new Shutdown(new DefaultNode("N1", 1, 1, 1));
        Assert.assertNotNull(s.toString());
    }

    /**
     * Dummy test to check call to visu.inject(this).
     */
    public void testVisualizationInjection() {
        MockPlanVisualizer visu = new MockPlanVisualizer();
        Shutdown s = new Shutdown(new DefaultNode("N1", 1, 1, 1));
        s.injectToVisualizer(visu);
        Assert.assertTrue(visu.isInjected(s));
    }

    /**
     * Test apply().
     */
    public void testApply() {
        DefaultConfiguration c = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("n1", 1, 2, 3);
        c.addOnline(n1);
        Shutdown s = new Shutdown(n1);
        s.apply(c);
        Assert.assertTrue(c.getOfflines().contains(n1));
    }

    /**
     * Test isCompatibleWith() when all is right.
     */
    public void testIsCompatibleWith() {
        DefaultConfiguration c = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("n1", 1, 2, 3);
        c.addOnline(n1);
        Shutdown s = new Shutdown(n1);
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
    @Test(dataProvider = "getWrongShutdowns")
    public void testIsIncompatibleWith(Shutdown s) {
        DefaultConfiguration c = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("n1", 1, 2, 3);
        DefaultNode n2 = new DefaultNode("n2", 1, 2, 3);
        DefaultNode n3 = new DefaultNode("n3", 1, 2, 3);
        c.addOnline(n1);
        c.addOffline(n2);
        c.addOnline(n3);
        c.setRunOn(new DefaultVirtualMachine("VM1", 1, 2, 3), n1);
        c.setSleepOn(new DefaultVirtualMachine("VM10", 1, 2, 3), n3);
        Assert.assertFalse(s.apply(c));
    }

    /**
     * Data provider for testWrongMigrationAppends(...).
     *
     * @return sample datas.
     */
    @DataProvider(name = "getWrongShutdowns")
    public Object[][] getWrongShutdowns() {
        return new Object[][]{
                {new Shutdown(new DefaultNode("n1", 1, 2, 3))}, //host running VM
                {new Shutdown(new DefaultNode("n2", 1, 2, 3))}, //bad state
                {new Shutdown(new DefaultNode("n3", 1, 2, 3))} //host sleeping VM
        };
    }

    public void testInsertIntoGraph() {
        TimedExecutionGraph graph = new TimedExecutionGraph();
        DefaultNode n1 = new DefaultNode("N1", 1, 2, 3);
        Shutdown ma = new Shutdown(n1);
        ma.insertIntoGraph(graph);
        Assert.assertTrue(graph.getIncomingsFor(n1).contains(ma));
    }
}
