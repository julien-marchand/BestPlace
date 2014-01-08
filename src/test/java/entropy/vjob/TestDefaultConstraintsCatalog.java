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

import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.DefaultVirtualMachine;
import entropy.vjob.builder.ConstraintBuilderException;
import entropy.vjob.builder.ConstraintsCatalog;
import entropy.vjob.builder.DefaultConstraintsCatalog;
import entropy.vjob.builder.MockConstraintBuilder;
import entropy.vjob.constraint.MockPlacementConstraint;

/**
 * Unit tests for ConstraintsCatalog.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestDefaultConstraintsCatalog {

    public void testInstantiation() {
        ConstraintsCatalog c = new DefaultConstraintsCatalog();
        Assert.assertEquals(c.getAvailableConstraints().size(), 0);
    }

    /**
     * Test add() and getters() in various condition
     */
    public void testAddConstraint() {
        DefaultConstraintsCatalog c = new DefaultConstraintsCatalog();
        MockConstraintBuilder m = new MockConstraintBuilder();
        Assert.assertTrue(c.add(m));
        Assert.assertEquals(c.getAvailableConstraints().size(), 1);
        Assert.assertTrue(c.getAvailableConstraints().contains("mock"));

        //Add another constraint with the same identifier. Should fail
        Assert.assertFalse(c.add(new MockConstraintBuilder()));
    }

    /**
     * Build with a registered constraint.
     */
    public void testBuild() {
        DefaultConstraintsCatalog c = new DefaultConstraintsCatalog();
        c.add(new MockConstraintBuilder());
        ExplodedMultiSet<DefaultVirtualMachine> e = new ExplodedMultiSet<DefaultVirtualMachine>();
        e.add(new ExplodedSet<DefaultVirtualMachine>(new DefaultManagedElementSet<DefaultVirtualMachine>(new DefaultVirtualMachine("VM1", 1, 1, 1))));
        e.add(new ExplodedSet<DefaultVirtualMachine>(new DefaultManagedElementSet<DefaultVirtualMachine>(new DefaultVirtualMachine("VM2", 1, 1, 1))));
        List<VJobElement> params = new LinkedList<VJobElement>();
        params.add(e);
        try {
            PlacementConstraint mc = c.buildConstraint("mock", params);
            Assert.assertNotNull(mc);
            Assert.assertEquals(mc.getClass(), MockPlacementConstraint.class);
            Assert.assertEquals(mc.getAllVirtualMachines().size(), 2);
        } catch (ConstraintBuilderException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    /**
     * Test the build with a unregistered constraint.
     */
    public void testBuildUnknown() {
        DefaultConstraintsCatalog c = new DefaultConstraintsCatalog();
        try {
            PlacementConstraint p = c.buildConstraint("toto", new LinkedList<VJobElement>());
            Assert.assertNull(p);
        } catch (ConstraintBuilderException ex) {
            Assert.fail(ex.getMessage());
        }
    }
}
