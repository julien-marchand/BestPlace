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

package entropy.vjob.constraint;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import choco.kernel.common.logging.ChocoLogging;
import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.SimpleConfiguration;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.SimpleNode;
import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VirtualMachine;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.choco.ChocoCustomRP;
import entropy.plan.durationEvaluator.MockDurationEvaluator;
import entropy.vjob.BasicVJob;
import entropy.vjob.ExplodedSet;
import entropy.vjob.Lonely;
import entropy.vjob.VJob;

/**
 * Unit tests for (@link Lonely}.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestLonely {

    public void testInstantiation() {
        ExplodedSet<VirtualMachine> vms = new ExplodedSet<VirtualMachine>();
        vms.add(new SimpleVirtualMachine("VM1", 1, 1, 1));
        vms.add(new SimpleVirtualMachine("VM2", 1, 1, 1));
        vms.add(new SimpleVirtualMachine("VM3", 1, 1, 1));
        Lonely l = new Lonely(vms);
        Assert.assertEquals(l.getAllVirtualMachines(), vms);
        Assert.assertNotNull(l.toString());
    }

    public void testIsSatisfied() {
        Configuration cfg = new SimpleConfiguration();
        for (int i = 0; i < 10; i++) {
            Node n = new SimpleNode("N" + i, 5, 5, 5);
            cfg.addOnline(n);
        }

        for (int i = 0; i < 20; i++) {
            VirtualMachine vm = new SimpleVirtualMachine("VM" + i, 1, 1, 1);
            cfg.setRunOn(vm, cfg.getAllNodes().get(i % cfg.getAllNodes().size()));
        }

        ExplodedSet<VirtualMachine> s1 = new ExplodedSet<VirtualMachine>();
        s1.add(cfg.getRunnings().get("VM0"));
        s1.add(cfg.getRunnings().get("VM1"));
        s1.add(cfg.getRunnings().get("VM10"));
        s1.add(cfg.getRunnings().get("VM11"));
        Lonely l1 = new Lonely(s1);
        Assert.assertTrue(l1.isSatisfied(cfg));

        ExplodedSet<VirtualMachine> s2 = new ExplodedSet<VirtualMachine>();
        s2.add(cfg.getRunnings().get("VM0"));
        s2.add(cfg.getRunnings().get("VM1"));
        s2.add(cfg.getRunnings().get("VM2"));
        s2.add(cfg.getRunnings().get("VM7"));
        Lonely l2 = new Lonely(s2);
        Assert.assertFalse(l2.isSatisfied(cfg));
    }

    public void testGetMisplaced() {
        Configuration cfg = new SimpleConfiguration();
        for (int i = 0; i < 10; i++) {
            Node n = new SimpleNode("N" + i, 5, 5, 5);
            cfg.addOnline(n);
        }

        for (int i = 0; i < 20; i++) {
            VirtualMachine vm = new SimpleVirtualMachine("VM" + i, 1, 1, 1);
            cfg.setRunOn(vm, cfg.getAllNodes().get(i % cfg.getAllNodes().size()));
        }

        ExplodedSet<VirtualMachine> s1 = new ExplodedSet<VirtualMachine>();
        s1.add(cfg.getRunnings().get("VM0"));
        s1.add(cfg.getRunnings().get("VM1"));
        s1.add(cfg.getRunnings().get("VM10"));
        s1.add(cfg.getRunnings().get("VM11"));
        Lonely l1 = new Lonely(s1);
        Assert.assertEquals(l1.getMisPlaced(cfg).size(), 0); //Cause it is satisfied

        ExplodedSet<VirtualMachine> s2 = new ExplodedSet<VirtualMachine>();
        s2.add(cfg.getRunnings().get("VM0"));
        s2.add(cfg.getRunnings().get("VM1"));
        s2.add(cfg.getRunnings().get("VM10"));
        s2.add(cfg.getRunnings().get("VM11"));
        s2.add(cfg.getRunnings().get("VM2"));
        s2.add(cfg.getRunnings().get("VM7"));
        Lonely l2 = new Lonely(s2);
        ExplodedSet<VirtualMachine> bads = new ExplodedSet<VirtualMachine>();
        bads.add(cfg.getRunnings().get("VM2"));
        bads.add(cfg.getRunnings().get("VM7"));
        Assert.assertEquals(l2.getMisPlaced(cfg), bads);
    }

    public void test1() {
        Configuration cfg = new SimpleConfiguration();
        for (int i = 0; i < 5; i++) {
            Node n = new SimpleNode("N" + i, 5, 5, 5);
            cfg.addOnline(n);
        }

        for (int i = 0; i < 10; i++) {
            VirtualMachine vm = new SimpleVirtualMachine("VM" + i, 1, 1, 1);
            cfg.setRunOn(vm, cfg.getAllNodes().get(i % cfg.getAllNodes().size()));
        }

        ExplodedSet<VirtualMachine> s2 = new ExplodedSet<VirtualMachine>();
        s2.add(cfg.getRunnings().get("VM0"));
        s2.add(cfg.getRunnings().get("VM1"));
        Lonely l2 = new Lonely(s2);

        ChocoCustomRP plan = new ChocoCustomRP(new MockDurationEvaluator(3, 2, 3, 4, 5, 6, 7, 8));
        //ChocoLogging.setVerbosity(Verbosity.SEARCH);
        plan.setRepairMode(false);
        List<VJob> vjobs = new ArrayList<VJob>();
        try {
            VJob v = new BasicVJob("v1");
            v.addVirtualMachines(new ExplodedSet<VirtualMachine>(cfg.getAllVirtualMachines()));
            v.addConstraint(l2);
            vjobs.add(v);
            //plan.setTimeLimit(10);

            TimedReconfigurationPlan p = plan.compute(cfg,
                    cfg.getAllVirtualMachines(),
                    cfg.getWaitings(),
                    cfg.getSleepings(),
                    new SimpleManagedElementSet<VirtualMachine>(),
                    cfg.getOnlines(),
                    cfg.getOfflines(),
                    vjobs);
            System.err.println(p);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        } finally {
            System.err.flush();
            ChocoLogging.flushLogs();
        }
    }
}
