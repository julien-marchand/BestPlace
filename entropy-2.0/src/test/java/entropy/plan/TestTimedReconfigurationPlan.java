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
package entropy.plan;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.TestHelper;
import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.action.Migration;

/**
 * Unit tests for DefaultTimedReconfigurationPlan.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestTimedReconfigurationPlan {

    private static final String RESOURCES_LOCATION = "src/test/resources/entropy/plan/TestTimedReconfigurationPlan.";

    /**
     * Create a basic reconfiguration plan for tests purpose.
     *
     * @return the plan
     */
    private DefaultTimedReconfigurationPlan createPlan() {
        return new DefaultTimedReconfigurationPlan(TestHelper.readConfiguration(RESOURCES_LOCATION + "conf.txt"));
    }

    /**
     * Fill the plan with some migrations.
     *
     * @param plan the plan to fill
     */
    private void fill(TimedReconfigurationPlan plan) {
        Configuration src = plan.getSource();
        VirtualMachine vm1 = src.getRunnings().get("VM1");
        VirtualMachine vm2 = src.getRunnings().get("VM2");
        VirtualMachine vm3 = src.getRunnings().get("VM3");
        VirtualMachine vm4 = src.getRunnings().get("VM4");
        Node n1 = src.getOnlines().get("N1");
        Node n2 = src.getOnlines().get("N2");
        Node n3 = src.getOnlines().get("N3");
        Node n4 = src.getOnlines().get("N4");
        Node n5 = src.getOnlines().get("N5");

        plan.add(new Migration(vm1, n1, n2, 5, 8));
        plan.add(new Migration(vm2, n1, n3, 1, 3));
        plan.add(new Migration(vm3, n2, n4, 0, 5));
        plan.add(new Migration(vm4, n3, n5, 0, 1));
    }

    /**
     * Test add() and size().
     */
    public void testAddAndSize() {
        TimedReconfigurationPlan plan = createPlan();
        Assert.assertEquals(plan.size(), 0);
        fill(plan);
        Assert.assertEquals(plan.size(), 4);
    }

    /**
     * Test getDuration() when add is valid or not.
     */
    public void testGetDuration() {
        TimedReconfigurationPlan plan = createPlan();
        Configuration src = plan.getSource();

        VirtualMachine vm1 = src.getRunnings().get("VM1");
        VirtualMachine vm2 = src.getRunnings().get("VM2");

        Node n1 = src.getOnlines().get("N1");
        Node n2 = src.getOnlines().get("N2");
        Node n3 = src.getOnlines().get("N3");

        Assert.assertEquals(plan.getDuration(), 0);
        plan.add(new Migration(vm2, n1, n3, 1, 3));
        Assert.assertEquals(plan.getDuration(), 3);
        plan.add(new Migration(vm1, n1, n2, 5, 8));
        Assert.assertEquals(plan.getDuration(), 8);
    }

    /**
     * Dummy test for toString() to detect NullPointerException.
     */
    public void testToString() {
        TimedReconfigurationPlan plan = createPlan();
        Assert.assertNotNull(plan.toString());
        fill(plan);
        Assert.assertNotNull(plan.toString());
    }

}
