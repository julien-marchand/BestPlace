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
import entropy.configuration.ManagedElementSet;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.action.Action;
import entropy.plan.choco.ChocoCustomRP;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.durationEvaluator.MockDurationEvaluator;
import entropy.vjob.BasicVJob;
import entropy.vjob.ContinuousSpread;
import entropy.vjob.ExplodedSet;
import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJob;

/**
 * Unit tests for ContinuousSpread.
 *
 * @author Fabien Hermenier
 */
@Test(groups = "unit")
public class TestContinuousSpread {

    /**
     * Location of resources used for tests.
     */
    public static final String RESOURCES_LOCATION = "src/test/resources/entropy/vjob/constraint/TestContinuousSpread.";

    /**
     * A basic test on spread.
     * VM1 have to be hosted on N2 for resources issues. With the constraint, VM2 will have to be migrated first.
     */
    public void basicTest() {
        //ChocoLogging.setVerbosity(Verbosity.FINEST);
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "dst.txt");
        ManagedElementSet<VirtualMachine> t1 = new SimpleManagedElementSet<VirtualMachine>();
        t1.add(src.getAllVirtualMachines().get("VM1"));
        t1.add(src.getAllVirtualMachines().get("VM2"));
        ChocoCustomRP plan = new ChocoCustomRP(new MockDurationEvaluator(3, 2, 3, 4, 5, 6, 7, 8));
        plan.setRepairMode(false);
        List<VJob> vjobs = new ArrayList<VJob>();
        try {
            VJob v = new BasicVJob("v1");
            v.addVirtualMachines(new ExplodedSet<VirtualMachine>(t1));
            v.addConstraint(new ContinuousSpread(new ExplodedSet<VirtualMachine>(t1)));
            vjobs.add(v);
            TimedReconfigurationPlan p = plan.compute(src,
                    dst.getRunnings(),
                    dst.getWaitings(),
                    dst.getSleepings(),
                    new SimpleManagedElementSet<VirtualMachine>(),
                    dst.getOnlines(),
                    dst.getOfflines(),
                    vjobs);
            Assert.assertEquals(p.size(), 3);
            ReconfigurationProblem rp = plan.getModel();
            Action m1 = plan.getModel().getAssociatedAction(src.getAllVirtualMachines().get("VM1")).getDefinedAction(rp);
            Action m2 = plan.getModel().getAssociatedAction(src.getAllVirtualMachines().get("VM2")).getDefinedAction(rp);
            Assert.assertTrue(m1.getStartMoment() >= m2.getFinishMoment());

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

    public void testWithNonRunningVMs() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "src2.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "dst2.txt");
        ManagedElementSet<VirtualMachine> t1 = new SimpleManagedElementSet<VirtualMachine>();
        t1.addAll(src.getAllVirtualMachines());
        ChocoCustomRP plan = new ChocoCustomRP(new MockDurationEvaluator(1, 2, 3, 4, 5, 6, 7, 8));
        plan.setRepairMode(false);
        List<VJob> vjobs = new ArrayList<VJob>();
        try {
            VJob v = new BasicVJob("v1");
            v.addVirtualMachines(new ExplodedSet<VirtualMachine>(t1));
            PlacementConstraint c1 = new ContinuousSpread(new ExplodedSet<VirtualMachine>(t1));
            v.addConstraint(c1);
            vjobs.add(v);
            TimedReconfigurationPlan p = plan.compute(src,
                    dst.getRunnings(),
                    dst.getWaitings(),
                    dst.getSleepings(),
                    new SimpleManagedElementSet<VirtualMachine>(),
                    dst.getOnlines(),
                    dst.getOfflines(),
                    vjobs);
            Assert.assertEquals(p.size(), 3);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }
}
