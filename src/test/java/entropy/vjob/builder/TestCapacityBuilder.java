/*
 * Copyright (c) Fabien Hermenier
 *
 *        This file is part of Entropy.
 *
 *        Entropy is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU Lesser General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        Entropy is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU Lesser General Public License for more details.
 *
 *        You should have received a copy of the GNU Lesser General Public License
 *        along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.vjob.builder;

import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.Node;
import entropy.configuration.SimpleNode;
import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VirtualMachine;
import entropy.vjob.Capacity;
import entropy.vjob.ExplodedSet;
import entropy.vjob.VJobElement;

/**
 * Unit tests for LonelyBuilder.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestCapacityBuilder {

    /**
     * A simple test that create a constraint.
     */
    public void validCreation() {
        CapacityBuilder cb = new CapacityBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<Node> s1 = new ExplodedSet<Node>();
        s1.add(new SimpleNode("N1", 1, 1, 1));
        s1.add(new SimpleNode("N2", 1, 1, 1));
        s1.add(new SimpleNode("N3", 1, 1, 1));
        params.add(s1);
        params.add(new NumberElement(7));
        try {
            Capacity c = cb.buildConstraint(params);
            Assert.assertEquals(c.getNodes(), s1);
            Assert.assertEquals(c.getMaximumCapacity(), 7);
            Assert.assertTrue(c.getAllVirtualMachines().isEmpty());
            System.err.println(c);
        } catch (ConstraintBuilderException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test with the set as a vmset.
     *
     * @throws ConstraintBuilderException the expected exception
     */
    public void testWithTypeMismactch() throws ConstraintBuilderException {
        CapacityBuilder mb = new CapacityBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<VirtualMachine> s1 = new ExplodedSet<VirtualMachine>();
        s1.add(new SimpleVirtualMachine("VM1", 1, 1, 1));
        params.add(s1);
        params.add(new NumberElement(7));
        mb.buildConstraint(params);
    }

    /**
     * Test with empty node.
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testWithEmptyFirstSet() throws ConstraintBuilderException {
        CapacityBuilder mb = new CapacityBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<Node> s1 = new ExplodedSet<Node>();
        params.add(s1);
        params.add(new NumberElement(7));
        mb.buildConstraint(params);
    }

    /**
     * Test without capacity
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testWithNoCapacity() throws ConstraintBuilderException {
        CapacityBuilder mb = new CapacityBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<Node> s1 = new ExplodedSet<Node>();
        s1.add(new SimpleNode("N1", 1, 1, 1));
        params.add(s1);
        mb.buildConstraint(params);
    }

    /**
     * Test with bad capacity
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testWithBadCapacity() throws ConstraintBuilderException {
        CapacityBuilder mb = new CapacityBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<Node> s1 = new ExplodedSet<Node>();
        s1.add(new SimpleNode("N1", 1, 1, 1));
        params.add(s1);
        params.add(new ExplodedSet<Node>());
        mb.buildConstraint(params);
    }

    /**
     * Test with bad capacity
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testWithNegativeCapacity() throws ConstraintBuilderException {
        CapacityBuilder mb = new CapacityBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<Node> s1 = new ExplodedSet<Node>();
        s1.add(new SimpleNode("N1", 1, 1, 1));
        params.add(s1);
        params.add(new NumberElement(-7));
        mb.buildConstraint(params);
    }
}
