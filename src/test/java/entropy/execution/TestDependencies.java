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

package entropy.execution;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.plan.action.Action;
import entropy.plan.action.Run;
import entropy.plan.action.Stop;

/**
 * Unit tests for Dependencies.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestDependencies {
    DefaultVirtualMachine vm = new DefaultVirtualMachine("vm1", 1, 1, 1);

    DefaultNode n = new DefaultNode("n", 1, 1, 1);

    Action a = new Run(vm, n, 5, 0);
    Action a2 = new Stop(vm, n, 0, 5);

    /**
     * Test the instantation.
     */
    public void testInstantiation() {
        Dependencies dep = new Dependencies(a);
        Assert.assertEquals(dep.getAction(), a);
        Assert.assertNotNull(dep.toString());
    }

    /**
     * Tests around a feasible dependencies.
     */
    public void testFeasibleDependencies() {
        Dependencies dep = new Dependencies(a);
        Assert.assertTrue(dep.isFeasible());
        Assert.assertEquals(dep.getUnsatisfiedDependencies().size(), 0);
    }

    /**
     * Tests when the dependencies is not more feasible.
     */
    public void testAddDependencies() {
        Dependencies dep = new Dependencies(a);
        dep.addDependency(a);
        Assert.assertFalse(dep.isFeasible());
        dep.addDependency(a2);
        Assert.assertEquals(dep.getUnsatisfiedDependencies().size(), 2);
        Assert.assertTrue(dep.getUnsatisfiedDependencies().contains(a));
        Assert.assertTrue(dep.getUnsatisfiedDependencies().contains(a2));
        Assert.assertNotNull(dep.toString());
    }

    /**
     * Test removing a dependency until having a feasible action.
     */
    public void testRemoveDependencies() {
        Dependencies dep = new Dependencies(a);
        dep.addDependency(a);
        Assert.assertFalse(dep.isFeasible());
        dep.addDependency(a2);
        Assert.assertTrue(dep.removeDependency(a));
        Assert.assertEquals(dep.getUnsatisfiedDependencies().size(), 1);
        Assert.assertFalse(dep.removeDependency(a));
        Assert.assertTrue(dep.removeDependency(a2));
        Assert.assertTrue(dep.isFeasible());
    }
}
