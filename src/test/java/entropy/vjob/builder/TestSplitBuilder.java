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

package entropy.vjob.builder;

import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.vjob.ExplodedSet;
import entropy.vjob.Split;
import entropy.vjob.VJobElement;

/**
 * Unit tests for LazySplitBuilder.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestSplitBuilder {

    /**
     * A simple test that create a constraint.
     */
    public void validCreation() {
        LazySplitBuilder mb = new LazySplitBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultVirtualMachine> s1 = new ExplodedSet<DefaultVirtualMachine>();
        s1.add(new DefaultVirtualMachine("vm1", 1, 1, 1));
        ExplodedSet<DefaultVirtualMachine> s2 = new ExplodedSet<DefaultVirtualMachine>();
        s2.add(new DefaultVirtualMachine("vm2", 1, 1, 1));
        s2.add(new DefaultVirtualMachine("vm3", 1, 1, 1));
        params.add(s1);
        params.add(s2);
        try {
            Split mc = mb.buildConstraint(params);
            Assert.assertEquals(mc.getFirstSet(), s1);
            Assert.assertEquals(mc.getSecondSet(), s2);
            Assert.assertEquals(mc.getAllVirtualMachines().size(), s1.size() + s2.size());
        } catch (ConstraintBuilderException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test with 1 set only
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testWithBadParamNumbers() throws ConstraintBuilderException {
        LazySplitBuilder mb = new LazySplitBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultVirtualMachine> s1 = new ExplodedSet<DefaultVirtualMachine>();
        s1.add(new DefaultVirtualMachine("vm1", 1, 1, 1));
        params.add(s1);
        mb.buildConstraint(params);
    }


    /**
     * Test with first set as a nodeset.
     *
     * @throws ConstraintBuilderException the expected exception
     */
    public void testWithTypeMismactch() throws ConstraintBuilderException {
        LazySplitBuilder mb = new LazySplitBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultNode> s1 = new ExplodedSet<DefaultNode>();
        s1.add(new DefaultNode("N1", 1, 1, 1));
        params.add(s1);
        ExplodedSet<DefaultVirtualMachine> s2 = new ExplodedSet<DefaultVirtualMachine>();
        s2.add(new DefaultVirtualMachine("vm2", 1, 1, 1));
        s2.add(new DefaultVirtualMachine("vm3", 1, 1, 1));
        params.add(s2);
        mb.buildConstraint(params);
    }

    /**
     * Test with empty vmset
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testWithEmptyFirstSet() throws ConstraintBuilderException {
        LazySplitBuilder mb = new LazySplitBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultVirtualMachine> s1 = new ExplodedSet<DefaultVirtualMachine>();
        params.add(s1);
        ExplodedSet<DefaultVirtualMachine> s2 = new ExplodedSet<DefaultVirtualMachine>();
        s2.add(new DefaultVirtualMachine("vm2", 1, 1, 1));
        s2.add(new DefaultVirtualMachine("vm3", 1, 1, 1));
        params.add(s2);
        mb.buildConstraint(params);
    }

    /**
     * Test with empty vmset
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testWithEmptySecondSet() throws ConstraintBuilderException {
        LazySplitBuilder mb = new LazySplitBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultVirtualMachine> s1 = new ExplodedSet<DefaultVirtualMachine>();
        ExplodedSet<DefaultVirtualMachine> s2 = new ExplodedSet<DefaultVirtualMachine>();
        s2.add(new DefaultVirtualMachine("vm2", 1, 1, 1));
        s2.add(new DefaultVirtualMachine("vm3", 1, 1, 1));
        params.add(s2);
        params.add(s1);
        mb.buildConstraint(params);
    }
}
