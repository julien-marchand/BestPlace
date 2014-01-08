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
import entropy.vjob.Fence;
import entropy.vjob.VJobElement;

/**
 * Unit tests for FenceBuilder.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestFenceBuilder {

    /**
     * Test fence({vm1,vm2},{n2,n3}}
     */
    public void testValid() {
        FenceBuilder mb = new FenceBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultVirtualMachine> s1 = new ExplodedSet<DefaultVirtualMachine>();
        s1.add(new DefaultVirtualMachine("vm1", 1, 1, 1));
        s1.add(new DefaultVirtualMachine("vm2", 1, 1, 1));
        ExplodedSet<DefaultNode> s2 = new ExplodedSet<DefaultNode>();
        s2.add(new DefaultNode("N2", 1, 1, 1));
        s2.add(new DefaultNode("N3", 1, 1, 1));
        params.add(s1);
        params.add(s2);
        try {
            Fence f = mb.buildConstraint(params);
            Assert.assertEquals(f.getAllVirtualMachines().size(), 2);
            Assert.assertEquals(f.getVirtualMachines(), s1);
            Assert.assertEquals(f.getNodes(), s2);
        } catch (ConstraintBuilderException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test fence(vmset).
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testBadParamsNumber() throws ConstraintBuilderException {
        FenceBuilder mb = new FenceBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultVirtualMachine> s1 = new ExplodedSet<DefaultVirtualMachine>();
        s1.add(new DefaultVirtualMachine("vm1", 1, 1, 1));
        s1.add(new DefaultVirtualMachine("vm2", 1, 1, 1));
        params.add(s1);
        mb.buildConstraint(params);
    }

    /**
     * Test fence(pset, pset).
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testTypeMismatch() throws ConstraintBuilderException {
        FenceBuilder mb = new FenceBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultNode> s1 = new ExplodedSet<DefaultNode>();
        s1.add(new DefaultNode("N1", 1, 1, 1));
        s1.add(new DefaultNode("N2", 1, 1, 1));
        params.add(s1);

        ExplodedSet<DefaultNode> s2 = new ExplodedSet<DefaultNode>();
        s1.add(new DefaultNode("N3", 1, 1, 1));
        s1.add(new DefaultNode("N4", 1, 1, 1));
        params.add(s2);
        mb.buildConstraint(params);
    }

    /**
     * Test fence({}, nodeset).
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testEmptyVMSet() throws ConstraintBuilderException {
        FenceBuilder mb = new FenceBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultVirtualMachine> s1 = new ExplodedSet<DefaultVirtualMachine>();
        params.add(s1);

        ExplodedSet<DefaultNode> s2 = new ExplodedSet<DefaultNode>();
        s2.add(new DefaultNode("N3", 1, 1, 1));
        s2.add(new DefaultNode("N4", 1, 1, 1));
        params.add(s2);
        mb.buildConstraint(params);
    }

    /**
     * Test fence(vmset, {}}).
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testEmptyNodeSet() throws ConstraintBuilderException {
        FenceBuilder mb = new FenceBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultVirtualMachine> s1 = new ExplodedSet<DefaultVirtualMachine>();
        s1.add(new DefaultVirtualMachine("VM1", 1, 1, 1));
        params.add(s1);

        ExplodedSet<DefaultNode> s2 = new ExplodedSet<DefaultNode>();
        params.add(s2);
        mb.buildConstraint(params);
    }
}
