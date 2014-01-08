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

package entropy.vjob.constraint;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.TestHelper;
import entropy.configuration.Configuration;
import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.choco.ChocoCustomRP;
import entropy.plan.durationEvaluator.MockDurationEvaluator;
import entropy.vjob.BasicVJob;
import entropy.vjob.ExplodedSet;
import entropy.vjob.LazySpread;
import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJob;

/**
 * Unit tests for ChocoLazySpread.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestLazySpread {

    /**
     * Location of resources used for tests.
     */
    public static final String RESOURCES_LOCATION = "src/test/resources/entropy/vjob/constraint/TestSpread.";


    /**
     * A test with only future running VMs.
     */
    public void testWithAllRunnings() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "dst.txt");
        ManagedElementSet<VirtualMachine> t1 = new DefaultManagedElementSet<VirtualMachine>();
        t1.add(src.getAllVirtualMachines().get("VM1"));
        t1.add(src.getAllVirtualMachines().get("VM2"));
        ChocoCustomRP plan = new ChocoCustomRP(new MockDurationEvaluator(1, 2, 3, 4, 5, 6, 7, 8));
        List<VJob> vjobs = new ArrayList<VJob>();
        try {
            VJob v = new BasicVJob("v1");
            v.addVirtualMachines(new ExplodedSet<VirtualMachine>(t1));
            v.addConstraint(new LazySpread(new ExplodedSet<VirtualMachine>(t1)));
            vjobs.add(v);
            TimedReconfigurationPlan p = plan.compute(src,
                    dst.getRunnings(),
                    dst.getWaitings(),
                    dst.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    dst.getOnlines(),
                    dst.getOfflines(),
                    vjobs);
            Assert.assertEquals(p.size(), 2);
            Configuration res = p.getDestination();
            Assert.assertNotSame(res.getLocation(src.getAllVirtualMachines().get("VM1")),
                    res.getLocation(src.getAllVirtualMachines().get("VM2")));
            for (PlacementConstraint c : v.getConstraints()) {
                if (!c.isSatisfied(res)) {
                    Assert.fail(c + " is not satisfied");
                }
            }

        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test with VMs that will be running.
     */
    public void testWithSomeRunnings() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "src2.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "dst2.txt");
        ManagedElementSet<VirtualMachine> t1 = new DefaultManagedElementSet<VirtualMachine>();
        t1.add(src.getAllVirtualMachines().get("VM1"));
        t1.add(src.getAllVirtualMachines().get("VM2"));
        ChocoCustomRP plan = new ChocoCustomRP(new MockDurationEvaluator(3, 2, 3, 4, 5, 6, 7, 8));
        List<VJob> vjobs = new ArrayList<VJob>();
        try {
            VJob v = new BasicVJob("v1");
            v.addVirtualMachines(new ExplodedSet<VirtualMachine>(t1));
            v.addConstraint(new LazySpread(new ExplodedSet<VirtualMachine>(t1)));
            vjobs.add(v);
            TimedReconfigurationPlan p = plan.compute(src,
                    dst.getRunnings(),
                    dst.getWaitings(),
                    dst.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    dst.getOnlines(),
                    dst.getOfflines(),
                    vjobs);
            Assert.assertEquals(p.size(), 0);
            Configuration res = p.getDestination();
            for (PlacementConstraint c : v.getConstraints()) {
                if (!c.isSatisfied(res)) {
                    Assert.fail(c + " is not satisfied");
                }
            }

        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }
}
