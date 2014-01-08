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

import entropy.configuration.DefaultConfiguration;
import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.execution.driver.MockDriver;
import entropy.execution.driver.MockDriverFactory;
import entropy.plan.DefaultTimedReconfigurationPlan;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.action.Action;
import entropy.plan.action.Migration;
import entropy.plan.action.Run;

/**
 * Unit tests for BetterExecution.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestTimedReconfigurationExecuter {

    /**
     * A basic test of an execution.
     */
    public void testSimpleExecution() {
        DefaultConfiguration cfg = new DefaultConfiguration();

        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("VM1", 1, 1, 1);
        DefaultVirtualMachine vm2 = new DefaultVirtualMachine("VM2", 1, 1, 1);
        DefaultVirtualMachine vm3 = new DefaultVirtualMachine("VM3", 1, 1, 1);
        DefaultVirtualMachine vm4 = new DefaultVirtualMachine("VM4", 1, 1, 1);
        DefaultNode n1 = new DefaultNode("N1", 1, 1, 1);
        DefaultNode n2 = new DefaultNode("N2", 1, 1, 1);
        DefaultNode n3 = new DefaultNode("N3", 1, 1, 1);
        DefaultNode n4 = new DefaultNode("N4", 1, 1, 1);
        DefaultNode n5 = new DefaultNode("N5", 1, 1, 1);
        cfg.addOnline(n1);
        cfg.addOnline(n2);
        cfg.addOnline(n3);
        cfg.addOnline(n4);
        cfg.addOnline(n5);
        cfg.setRunOn(vm1, n1);
        cfg.setRunOn(vm2, n1);
        cfg.setRunOn(vm3, n2);
        cfg.setRunOn(vm4, n3);
        Action t1 = new Migration(vm1, n1, n2, 5, 8);
        Action t2 = new Migration(vm2, n1, n3, 1, 3);
        Action t3 = new Migration(vm3, n2, n4, 0, 5);
        Action t4 = new Migration(vm4, n3, n5, 0, 1);

        TimedReconfigurationPlan plan = new DefaultTimedReconfigurationPlan(cfg);
        Assert.assertTrue(plan.add(t1));
        Assert.assertTrue(plan.add(t2));
        Assert.assertTrue(plan.add(t3));
        Assert.assertTrue(plan.add(t4));
        MockDriverFactory factory = new MockDriverFactory();
        TimedReconfigurationExecuter be = new TimedReconfigurationExecuter(factory);
        be.start(plan);
        Assert.assertEquals(be.getUncommitedActions().size(), 0);
    }

    /**
     * Another test
     */
    public void test2() {
        MockDriver.MAX_JITTER = 5000;
        DefaultConfiguration cfg = new DefaultConfiguration();
        DefaultVirtualMachine[] vms = new DefaultVirtualMachine[6];
        for (int i = 0; i < vms.length; i++) {
            vms[i] = new DefaultVirtualMachine("VM" + (i + 1), 1, 1, 1);
        }
        DefaultNode[] ns = new DefaultNode[5];
        for (int i = 0; i < ns.length; i++) {
            ns[i] = new DefaultNode("N" + (i + 1), 1000, 1000, 1000);
            cfg.addOnline(ns[i]);
        }
        cfg.setRunOn(vms[2], ns[0]);
        cfg.setRunOn(vms[0], ns[0]);
        cfg.setRunOn(vms[1], ns[1]);
        cfg.setRunOn(vms[3], ns[1]);
        cfg.setRunOn(vms[4], ns[3]);
        cfg.addWaiting(vms[5]);
        TimedReconfigurationPlan plan = new DefaultTimedReconfigurationPlan(cfg);
        Assert.assertTrue(plan.add(new Migration(vms[2], ns[0], ns[1], 7, 9)));
        Assert.assertTrue(plan.add(new Migration(vms[0], ns[0], ns[2], 0, 10)));
        Assert.assertTrue(plan.add(new Migration(vms[1], ns[1], ns[2], 0, 5)));
        Assert.assertTrue(plan.add(new Migration(vms[3], ns[1], ns[3], 3, 7)));
        Assert.assertTrue(plan.add(new Migration(vms[4], ns[3], ns[2], 0, 3)));
        Assert.assertTrue(plan.add(new Run(vms[5], ns[3], 0, 5)));
        MockDriverFactory factory = new MockDriverFactory();
        TimedReconfigurationExecuter be = new TimedReconfigurationExecuter(factory);
        be.start(plan);
        Assert.assertEquals(be.getUncommitedActions().size(), 0);
    }

    public void test3() {
        MockDriver.MAX_JITTER = 2000;
        DefaultConfiguration cfg = new DefaultConfiguration();
        DefaultVirtualMachine[] vms = new DefaultVirtualMachine[6];
        for (int i = 0; i < vms.length; i++) {
            vms[i] = new DefaultVirtualMachine("VM" + (i + 1), 1, 1, 1);
        }
        DefaultNode[] ns = new DefaultNode[5];
        for (int i = 0; i < ns.length; i++) {
            ns[i] = new DefaultNode("N" + (i + 1), 1000, 1000, 1000);
            cfg.addOnline(ns[i]);
        }
        cfg.setRunOn(vms[2], ns[0]);
        cfg.setRunOn(vms[0], ns[0]);
        cfg.setRunOn(vms[1], ns[1]);
        cfg.setRunOn(vms[3], ns[1]);
        cfg.setRunOn(vms[4], ns[3]);
        cfg.addWaiting(vms[5]);
        TimedReconfigurationPlan plan = new DefaultTimedReconfigurationPlan(cfg);
        Assert.assertTrue(plan.add(new Migration(vms[2], ns[0], ns[1], 5, 9)));
        Assert.assertTrue(plan.add(new Migration(vms[0], ns[0], ns[2], 0, 10)));
        Assert.assertTrue(plan.add(new Migration(vms[1], ns[1], ns[2], 0, 5)));
        Assert.assertTrue(plan.add(new Migration(vms[3], ns[1], ns[3], 3, 5)));
        Assert.assertTrue(plan.add(new Migration(vms[4], ns[3], ns[2], 0, 3)));
        Assert.assertTrue(plan.add(new Run(vms[5], ns[3], 0, 5)));

        MockDriverFactory factory = new MockDriverFactory();
        TimedReconfigurationExecuter be = new TimedReconfigurationExecuter(factory);
        be.start(plan);
        Assert.assertEquals(be.getUncommitedActions().size(), 0);
    }
}
