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
import entropy.vjob.ExplodedSet;
import entropy.vjob.Lonely;
import entropy.vjob.VJobElement;

/**
 * Unit tests for LonelyBuilder.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestLonelyBuilder {

    /**
     * A simple test that create a constraint.
     */
    public void validCreation() {
        LonelyBuilder mb = new LonelyBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<VirtualMachine> s1 = new ExplodedSet<VirtualMachine>();
        s1.add(new SimpleVirtualMachine("vm1", 1, 1, 1));
        s1.add(new SimpleVirtualMachine("vm2", 1, 1, 1));
        s1.add(new SimpleVirtualMachine("vm3", 1, 1, 1));
        params.add(s1);
        try {
            Lonely mc = mb.buildConstraint(params);
            Assert.assertEquals(mc.getAllVirtualMachines(), s1);
        } catch (ConstraintBuilderException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test with the set as a nodeset.
     *
     * @throws entropy.vjob.builder.ConstraintBuilderException
     *          the expected exception
     */
    public void testWithTypeMismactch() throws ConstraintBuilderException {
        LonelyBuilder mb = new LonelyBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<Node> s1 = new ExplodedSet<Node>();
        s1.add(new SimpleNode("N1", 1, 1, 1));
        params.add(s1);
        mb.buildConstraint(params);
    }

    /**
     * Test with empty vmset
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testWithEmptyFirstSet() throws ConstraintBuilderException {
        LonelyBuilder mb = new LonelyBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<SimpleVirtualMachine> s1 = new ExplodedSet<SimpleVirtualMachine>();
        params.add(s1);
        mb.buildConstraint(params);
    }
}
