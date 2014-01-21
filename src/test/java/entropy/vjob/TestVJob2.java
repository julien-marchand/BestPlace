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

package entropy.vjob;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.vjob.constraint.MockPlacementConstraint;

/**
 * Unit tests for {@link BasicVJob}.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestVJob2 {

    /**
     * Test the creation of an empty vjobs.
     */
    public void testInstantiate() {
        VJob v = new BasicVJob("test");
        Assert.assertEquals(v.id(), "test");

        Assert.assertEquals(v.getConstraints().size(), 0);
        Assert.assertEquals(v.getNodes().size(), 0);
        Assert.assertEquals(v.getVirtualMachines().size(), 0);
        Assert.assertEquals(v.getNodeSets().size(), 0);
        Assert.assertEquals(v.getVirtualMachineSets().size(), 0);
        Assert.assertEquals(v.getMultiNodeSets().size(), 0);
        Assert.assertEquals(v.getMultiVirtualMachineSets().size(), 0);
        Assert.assertEquals(v.getVariables().size(), 0);
    }

    /**
     * Add set of virtual machines, get it, check the variables and toString().
     */
    public void testAddVirtualMachines() {
        VJob v = new BasicVJob("test");
        ExplodedSet<VirtualMachine> t1 = new ExplodedSet<VirtualMachine>("$T1");
        RangeOfElements<VirtualMachine> t2 = new RangeOfElements<VirtualMachine>("VM[1,3,5,7,9]");
        for (int i = 0; i < 10; i++) {
            VirtualMachine vm = new DefaultVirtualMachine("VM" + i, 1, 1, 1);
            if (i % 2 == 0) {
                t1.add(vm);
            } else {
                t2.add(vm);
            }
        }
        Assert.assertTrue(v.addVirtualMachines(t1));
        Assert.assertTrue(v.addVirtualMachines(t2));
        Assert.assertEquals(v.getVirtualMachineSets().size(), 2);
        Assert.assertTrue(v.getVirtualMachineSets().contains(t1));
        Assert.assertTrue(v.getVirtualMachineSets().contains(t2));
        Assert.assertEquals(v.getVirtualMachines().size(), 10);
        Assert.assertEquals(v.getVariable("$T1"), t1);
        Assert.assertEquals(v.toString(), "$T1 = {VM0, VM2, VM4, VM6, VM8};");
    }

    /**
     * Add a multiset of virtual machines, get it, check the variables and toString().
     */
    public void testAddMultiVirtualMachineSets() {
        VJob v = new BasicVJob("test");
        ExplodedSet<VirtualMachine> t1 = new ExplodedSet<VirtualMachine>("$T1");
        RangeOfElements<VirtualMachine> t2 = new RangeOfElements<VirtualMachine>("VM[1,3,5,7,9]");
        ExplodedMultiSet<VirtualMachine> e = new ExplodedMultiSet<VirtualMachine>("$ALL");
        e.add(t1);
        e.add(t2);
        for (int i = 0; i < 10; i++) {
            DefaultVirtualMachine vm = new DefaultVirtualMachine("VM" + i, 1, 1, 1);
            if (i % 2 == 0) {
                t1.add(vm);
            } else {
                t2.add(vm);
            }
        }
        Assert.assertTrue(v.addVirtualMachines(e));
        Assert.assertEquals(v.getMultiVirtualMachineSets().size(), 1);
        Assert.assertTrue(v.getMultiVirtualMachineSets().contains(e));
        Assert.assertEquals(v.getVirtualMachines().size(), 10);
        Assert.assertEquals(v.getVariable("$ALL"), e);
        Assert.assertEquals(v.toString(), "$ALL = {$T1, VM[1,3,5,7,9]};");
    }

    /**
     * Add a multiset of nodes, get it, check the variables and toString().
     */
    public void testAddMultiNodeSets() {
        VJob v = new BasicVJob("test");
        ExplodedSet<Node> t1 = new ExplodedSet<Node>("$T1");
        RangeOfElements<Node> t2 = new RangeOfElements<Node>("N[1,3,5,7,9]");
        ExplodedMultiSet<Node> e = new ExplodedMultiSet<Node>("$ALL");
        e.add(t1);
        e.add(t2);
        for (int i = 0; i < 10; i++) {
            DefaultNode n = new DefaultNode("N" + i, 1, 1, 1);
            if (i % 2 == 0) {
                t1.add(n);
            } else {
                t2.add(n);
            }
        }
        Assert.assertTrue(v.addNodes(e));
        Assert.assertEquals(v.getMultiNodeSets().size(), 1);
        Assert.assertTrue(v.getMultiNodeSets().contains(e));
        Assert.assertEquals(v.getNodes().size(), 10);
        Assert.assertEquals(v.getVariable("$ALL"), e);
        Assert.assertEquals(v.toString(), "$ALL = {$T1, N[1,3,5,7,9]};");
    }

    /**
     * Add set of nodes, get it, check the variables and toString().
     */
    public void testAddNodes() {
        VJob v = new BasicVJob("test");
        ExplodedSet<Node> t1 = new ExplodedSet<Node>();
        RangeOfElements<Node> t2 = new RangeOfElements<Node>("N[1,3,5,7,9]", "$T2");
        for (int i = 0; i < 10; i++) {
            Node n = new DefaultNode("N" + i, 1, 1, 1);
            if (i % 2 == 0) {
                t1.add(n);
            } else {
                t2.add(n);
            }
        }
        Assert.assertTrue(v.addNodes(t1));
        Assert.assertTrue(v.addNodes(t2));
        Assert.assertEquals(v.getNodeSets().size(), 2);
        Assert.assertTrue(v.getNodeSets().contains(t1));
        Assert.assertTrue(v.getNodeSets().contains(t2));
        Assert.assertEquals(v.getNodes().size(), 10);
        Assert.assertEquals(v.getVariable("$T2"), t2);
        Assert.assertEquals(v.toString(), "$T2 = N[1,3,5,7,9];");
    }

    /**
     * Some tests around variables
     */
    public void testVariables() {
        VJob v = new BasicVJob("test");
        Assert.assertTrue(v.addNodes(new ExplodedSet<Node>("$T1")));
        Assert.assertEquals(v.getVariable("$T1").getLabel(), "$T1");
        Assert.assertNull(v.getVariable("$TT"));
        Assert.assertTrue(v.getVariables().contains("$T1"));
    }

    /**
     * Add, remove, list constraints, check toString().
     */
    public void testConstraints() {
        VJob v = new BasicVJob("test");
        ExplodedSet<VirtualMachine> t1 = new ExplodedSet<VirtualMachine>("$T1");
        ExplodedSet<VirtualMachine> t2 = new ExplodedSet<VirtualMachine>("$T2");
        ExplodedMultiSet<VirtualMachine> a = new ExplodedMultiSet<VirtualMachine>("$ALL");
        a.add(t1);
        a.add(t2);
        PlacementConstraint c = new MockPlacementConstraint(a);
        Assert.assertTrue(v.addConstraint(c));
        Assert.assertEquals(v.getConstraints().size(), 1);
        Assert.assertTrue(v.getConstraints().contains(c));
        Assert.assertEquals(v.toString(), c.toString() + ";");
        Assert.assertTrue(v.removeConstraint(c));
        Assert.assertFalse(v.removeConstraint(c));
        Assert.assertEquals(v.getConstraints().size(), 0);
    }
}
