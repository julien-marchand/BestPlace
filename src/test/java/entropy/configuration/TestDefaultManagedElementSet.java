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

package entropy.configuration;

import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Unit tests related to ManagedElementSet.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestDefaultManagedElementSet {

    /**
     * Make a default set of elements.
     *
     * @return a proper set
     */
    private static DefaultManagedElementSet<MockDefaultManagedElement> makeDefaultSet() {
        final DefaultManagedElementSet<MockDefaultManagedElement> set = new DefaultManagedElementSet<MockDefaultManagedElement>();
        MockDefaultManagedElement n = new MockDefaultManagedElement("N0");
        n.updateValue(DefaultNode.CPU_NB, 1);
        n.updateValue(DefaultNode.MEMORY_TOTAL, 4096);
        set.add(n);

        n = new MockDefaultManagedElement("N1");
        n.updateValue(DefaultNode.CPU_NB, 1);
        n.updateValue(DefaultNode.MEMORY_TOTAL, 4096);
        set.add(n);

        n = new MockDefaultManagedElement("N2");
        n.updateValue(DefaultNode.CPU_NB, 1);
        n.updateValue(DefaultNode.MEMORY_TOTAL, 2048);
        set.add(n);

        n = new MockDefaultManagedElement("N3");
        n.updateValue(DefaultNode.CPU_NB, 2);
        n.updateValue(DefaultNode.MEMORY_TOTAL, 2048);
        set.add(n);

        return set;
    }

    public void testGetFromName() {
        final DefaultManagedElementSet<MockDefaultManagedElement> orig = TestDefaultManagedElementSet
                .makeDefaultSet();
        Assert.assertTrue(orig.get("N0").equals(new MockDefaultManagedElement("N0")));
        Assert.assertNull(orig.get("N7"));
    }

    /**
     * Check that the copy constructor makes a deep copy.
     */
    public void testCopyConstructor() {
        final DefaultManagedElementSet<MockDefaultManagedElement> orig = TestDefaultManagedElementSet
                .makeDefaultSet();
        final DefaultManagedElementSet<MockDefaultManagedElement> copy = new DefaultManagedElementSet<MockDefaultManagedElement>(
                orig);

        // Not the same reference
        Assert.assertFalse(orig == copy,
                "The copy should not have the same reference");

        // But the same content
        Assert.assertEquals(copy, orig);

        // A modification of the orig should not affect the copy
        // i.e: check the shallow copy
        MockDefaultManagedElement n = new MockDefaultManagedElement("N5");
        n.updateValue(DefaultNode.CPU_NB, 2);
        n.updateValue(DefaultNode.MEMORY_TOTAL, 1024);
        orig.add(n);
        Assert.assertEquals(copy.size(), 4,
                "The copy set should not have been modified");
        // A modification of a element into the orig should not affect the copy
        // i.e: check the deep copy
        n = orig.get("N0");
        n.updateValue(DefaultNode.CPU_NB, -1);
        Assert.assertEquals(copy.get("N0").getValue(DefaultNode.CPU_NB), -1,
                "The copy of the node should have been modified");
    }

    /**
     * Test the non-possibility of having 2 elements with the same name.
     */
    public void testAdd() {
        final DefaultManagedElementSet<MockDefaultManagedElement> orig = TestDefaultManagedElementSet
                .makeDefaultSet();
        Assert.assertFalse(orig.add(new MockDefaultManagedElement("N0")));
        Assert.assertEquals(orig.size(), 4);
    }

    public void testRemove() {
        final DefaultManagedElementSet<MockDefaultManagedElement> orig = TestDefaultManagedElementSet
                .makeDefaultSet();
        final MockDefaultManagedElement m = orig.get("N1");
        Assert.assertTrue(orig.remove(m));
        Assert.assertFalse(orig.contains(m));
    }

    /**
     * Test the equals() method.
     */
    public void testEquals() {
        final DefaultManagedElementSet<MockDefaultManagedElement> orig = TestDefaultManagedElementSet.makeDefaultSet();
        DefaultManagedElementSet<MockDefaultManagedElement> clone = TestDefaultManagedElementSet.makeDefaultSet();
        Assert.assertEquals(clone, orig);
        clone.remove(clone.size() - 1);
        Assert.assertNotSame(clone, orig);

        clone = TestDefaultManagedElementSet.makeDefaultSet();
        orig.remove(clone.size() - 1);
        Assert.assertNotSame(clone, orig);

        Assert.assertFalse(orig.equals(new Object()));
    }


    /**
     * Test addAll().
     */
    public void testAddAll() {
        DefaultManagedElementSet<MockDefaultManagedElement> set = makeDefaultSet();
        DefaultManagedElementSet<MockDefaultManagedElement> s2 = new DefaultManagedElementSet<MockDefaultManagedElement>();
        Assert.assertFalse(set.addAll(s2));
        s2.add(new MockDefaultManagedElement("N0"));
        Assert.assertFalse(set.addAll(s2));
        s2.add(new MockDefaultManagedElement("aaaa"));
        Assert.assertTrue(set.addAll(s2));
    }

    public void testRetainAll() {
        DefaultManagedElementSet<MockDefaultManagedElement> set = makeDefaultSet();
        DefaultManagedElementSet<MockDefaultManagedElement> toKeep = new DefaultManagedElementSet<MockDefaultManagedElement>();
        MockDefaultManagedElement n1 = set.get("N1");
        MockDefaultManagedElement n2 = set.get("N2");
        MockDefaultManagedElement n3 = set.get("N3");
        MockDefaultManagedElement n7 = new MockDefaultManagedElement("N7");
        toKeep.add(n1);
        toKeep.add(n2);
        toKeep.add(n7);
        Assert.assertTrue(set.retainAll(toKeep));
        Assert.assertTrue(set.contains(n1));
        Assert.assertTrue(set.contains(n2));
        Assert.assertFalse(set.contains(n3));
        Assert.assertNull(set.get("N0"));
    }
}
