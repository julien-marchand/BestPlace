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
 * Tests for the action Migration.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestMigration {

    /**
     * Test for the getters().
     */
    public void testGets() {
        Migration m = new Migration(new DefaultVirtualMachine("VM1", 1, 1, 1), new DefaultNode("N1", 1, 1, 1), new DefaultNode("N2", 1, 1, 1));
        Assert.assertEquals(m.getDestination(), new DefaultNode("N2", 1, 1, 1));
    }

    /**
     * Test the equals() method for various contexts.
     */
    public void testEquals() {
        Migration m = new Migration(new DefaultVirtualMachine("VM1", 1, 1, 1), new DefaultNode("N1", 1, 1, 1), new DefaultNode("N2", 1, 1, 1));
        Migration m2 = new Migration(new DefaultVirtualMachine("VM1", 1, 1, 1), new DefaultNode("N1", 1, 1, 1), new DefaultNode("N2", 1, 1, 1));
        Assert.assertEquals(m, m2);

        //Must fails
        Migration m3 = new Migration(new DefaultVirtualMachine("VM2", 1, 1, 1), new DefaultNode("N1", 1, 1, 1), new DefaultNode("N2", 1, 1, 1));
        Assert.assertNotSame(m, m3);
        Migration m4 = new Migration(new DefaultVirtualMachine("VM1", 1, 1, 1), new DefaultNode("N2", 1, 1, 1), new DefaultNode("N2", 1, 1, 1));
        Assert.assertNotSame(m, m4);
        Migration m5 = new Migration(new DefaultVirtualMachine("VM1", 1, 1, 1), new DefaultNode("N1", 1, 1, 1), new DefaultNode("N3", 1, 1, 1));
        Assert.assertNotSame(m, m5);
    }

    /**
     * Dummy test to prevent NullPointerException.
     */
    public void testToString() {
        Migration m1 = new Migration(new DefaultVirtualMachine("vm1", 1, 2, 3),
                new DefaultNode("n1", 1, 2, 3),
                new DefaultNode("n2", 1, 2, 3));
        Assert.assertNotNull(m1.toString());
    }


    /**
     * Dummy test to check call to visu.inject(this).
     */
    public void testVisualizationInjection() {
        MockPlanVisualizer visu = new MockPlanVisualizer();
        Migration s = new Migration(new DefaultVirtualMachine("vm1", 1, 2, 3),
                new DefaultNode("n1", 1, 2, 3),
                new DefaultNode("n2", 1, 2, 3));
        s.injectToVisualizer(visu);
        Assert.assertTrue(visu.isInjected(s));
    }

    /**
     * Test apply().
     */
    public void testApply() {
        DefaultConfiguration c = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("n1", 1, 2, 3);
        DefaultNode n2 = new DefaultNode("n2", 1, 2, 3);
        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("vm1", 1, 1, 1);
        c.addOnline(n1);
        c.addOnline(n2);
        c.setRunOn(vm1, n1);
        Migration m = new Migration(vm1, n1, n2);
        m.apply(c);
        Assert.assertEquals(c.getLocation(vm1), n2);
    }

    /**
     * Test isCompatibleWith() when all is right.
     */
    public void testIsCompatibleWith() {
        DefaultConfiguration c = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("n1", 1, 2, 3);
        DefaultNode n2 = new DefaultNode("n2", 1, 2, 3);
        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("vm1", 1, 1, 1);
        c.addOnline(n1);
        c.addOnline(n2);
        c.setRunOn(vm1, n1);
        Migration m = new Migration(vm1, n1, n2);
        Assert.assertTrue(m.apply(c));
    }

    /**
     * Test isIncompatibleWith() when the action is not right.
     *
     * @param m the migration
     */
    @Test(dataProvider = "getWrongMigrations")
    public void testIsIncompatibleWith(Migration m) {
        DefaultConfiguration c = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("n1", 1, 2, 3);
        DefaultNode n2 = new DefaultNode("n2", 1, 2, 3);
        c.addOnline(n1);
        c.addOnline(n2);
        c.setRunOn(new DefaultVirtualMachine("VM1", 1, 2, 3), n1);
        c.setSleepOn(new DefaultVirtualMachine("VM10", 1, 2, 3), n1);
        Assert.assertFalse(m.isCompatibleWith(c));
    }


    /**
     * Data provider for testWrongMigrationAppends(...).
     *
     * @return sample datas.
     */
    @DataProvider(name = "getWrongMigrations")
    public Object[][] wrongMigrationAppendsData() {
        return new Object[][]{
                {new Migration(new DefaultVirtualMachine("VM5", 1, 1, 1), new DefaultNode("n1", 1, 2, 3), new DefaultNode("n2", 1, 2, 3))}, //migration of a unknown VM
                {new Migration(new DefaultVirtualMachine("VM10", 1, 1, 1), new DefaultNode("n1", 1, 2, 3), new DefaultNode("n2", 1, 2, 3))}, //bad state
                {new Migration(new DefaultVirtualMachine("VM1", 1, 1, 1), new DefaultNode("n2", 1, 2, 3), new DefaultNode("n1", 1, 2, 3))}, //migration on a VM mislocated.
                {new Migration(new DefaultVirtualMachine("VM1", 1, 1, 1), new DefaultNode("n1", 1, 2, 3), new DefaultNode("n7", 1, 2, 3))}, //migration on a unknow node
                {new Migration(new DefaultVirtualMachine("VM1", 1, 1, 1), new DefaultNode("N55", 1, 1, 1), new DefaultNode("n2", 1, 2, 3))} //migration from a unknown node
        };
    }

    public void testInsertIntoGraph() {
        TimedExecutionGraph graph = new TimedExecutionGraph();
        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("VM1", 1, 2, 3);
        DefaultNode n1 = new DefaultNode("N1", 1, 2, 3);
        DefaultNode n2 = new DefaultNode("N2", 1, 2, 3);
        Migration ma = new Migration(vm1, n1, n2);
        ma.insertIntoGraph(graph);
        Assert.assertTrue(graph.getOutgoingsFor(n1).contains(ma));
        Assert.assertTrue(graph.getIncomingsFor(n2).contains(ma));
    }
}
