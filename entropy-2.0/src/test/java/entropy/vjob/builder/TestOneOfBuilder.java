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
import entropy.vjob.ExplodedMultiSet;
import entropy.vjob.ExplodedSet;
import entropy.vjob.Fence;
import entropy.vjob.OneOf;
import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJobElement;

/**
 * Unit tests for OneOfBuilder.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestOneOfBuilder {

    public void testOk() {
        OneOfBuilder b = new OneOfBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultVirtualMachine> vms = new ExplodedSet<DefaultVirtualMachine>();
        vms.add(new DefaultVirtualMachine("VM1", 1, 1, 1));
        ExplodedMultiSet<DefaultNode> grps = new ExplodedMultiSet<DefaultNode>();
        ExplodedSet<DefaultNode> g1 = new ExplodedSet<DefaultNode>();
        ExplodedSet<DefaultNode> g2 = new ExplodedSet<DefaultNode>();
        g1.add(new DefaultNode("N1", 1, 1, 1));
        g2.add(new DefaultNode("N2", 1, 1, 1));
        grps.add(g1);
        grps.add(g2);
        params.add(vms);
        params.add(grps);
        try {
            PlacementConstraint c = b.buildConstraint(params);
            Assert.assertEquals(c.getClass(), OneOf.class);
            Assert.assertEquals(c.getAllVirtualMachines(), vms);
            Assert.assertEquals(((OneOf) c).getGroups(), grps);
        } catch (ConstraintBuilderException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test with no multi-nodeset.
     *
     * @throws ConstraintBuilderException the exception we expect
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testWithBadParamsNumber() throws ConstraintBuilderException {
        OneOfBuilder b = new OneOfBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultVirtualMachine> vms = new ExplodedSet<DefaultVirtualMachine>();
        vms.add(new DefaultVirtualMachine("VM1", 1, 1, 1));
        ExplodedMultiSet<DefaultNode> grps = new ExplodedMultiSet<DefaultNode>();
        ExplodedSet<DefaultNode> g1 = new ExplodedSet<DefaultNode>();
        ExplodedSet<DefaultNode> g2 = new ExplodedSet<DefaultNode>();
        g1.add(new DefaultNode("N1", 1, 1, 1));
        g2.add(new DefaultNode("N2", 1, 1, 1));
        grps.add(g1);
        grps.add(g2);
        params.add(vms);
        b.buildConstraint(params);
    }

    /**
     * The vmset is empty.
     *
     * @throws ConstraintBuilderException the exception we expect
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testWithEmptyVMSet() throws ConstraintBuilderException {
        OneOfBuilder b = new OneOfBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultVirtualMachine> vms = new ExplodedSet<DefaultVirtualMachine>();
        ExplodedMultiSet<DefaultNode> grps = new ExplodedMultiSet<DefaultNode>();
        ExplodedSet<DefaultNode> g1 = new ExplodedSet<DefaultNode>();
        ExplodedSet<DefaultNode> g2 = new ExplodedSet<DefaultNode>();
        g1.add(new DefaultNode("N1", 1, 1, 1));
        g2.add(new DefaultNode("N2", 1, 1, 1));
        grps.add(g1);
        grps.add(g2);
        params.add(vms);
        params.add(grps);
        b.buildConstraint(params);
    }

    /**
     * One of the nodeset composing the multiset if empty.
     *
     * @throws ConstraintBuilderException the exception we expect
     */
    @Test(expectedExceptions = {ConstraintBuilderException.class})
    public void testWithEmptyNodeSet() throws ConstraintBuilderException {
        OneOfBuilder b = new OneOfBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultVirtualMachine> vms = new ExplodedSet<DefaultVirtualMachine>();
        vms.add(new DefaultVirtualMachine("VM1", 1, 1, 1));
        ExplodedMultiSet<DefaultNode> grps = new ExplodedMultiSet<DefaultNode>();
        ExplodedSet<DefaultNode> g1 = new ExplodedSet<DefaultNode>();
        ExplodedSet<DefaultNode> g2 = new ExplodedSet<DefaultNode>();
        g2.add(new DefaultNode("N2", 1, 1, 1));
        grps.add(g1);
        grps.add(g2);
        params.add(vms);
        params.add(grps);
        b.buildConstraint(params);
    }

    /**
     * Test with a single node set. Check if a fence constraint is created.
     */
    public void testWithSingleGroup() {
        OneOfBuilder b = new OneOfBuilder();
        List<VJobElement> params = new LinkedList<VJobElement>();
        ExplodedSet<DefaultVirtualMachine> vms = new ExplodedSet<DefaultVirtualMachine>();
        vms.add(new DefaultVirtualMachine("VM1", 1, 1, 1));
        ExplodedMultiSet<DefaultNode> grps = new ExplodedMultiSet<DefaultNode>();
        ExplodedSet<DefaultNode> g2 = new ExplodedSet<DefaultNode>();
        g2.add(new DefaultNode("N2", 1, 1, 1));
        grps.add(g2);
        params.add(vms);
        params.add(grps);
        try {
            PlacementConstraint c = b.buildConstraint(params);
            Assert.assertEquals(c.getClass(), Fence.class);
            Assert.assertEquals(c.getAllVirtualMachines(), vms);
            Assert.assertEquals(((Fence) c).getNodes(), g2);
        } catch (ConstraintBuilderException e) {
            Assert.fail(e.getMessage(), e);
        }

    }
}
