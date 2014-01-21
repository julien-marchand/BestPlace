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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.TestHelper;
import entropy.configuration.Configuration;
import entropy.configuration.DefaultConfiguration;
import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.choco.ChocoCustomRP;
import entropy.plan.durationEvaluator.MockDurationEvaluator;
import entropy.vjob.BasicVJob;
import entropy.vjob.ExplodedMultiSet;
import entropy.vjob.ExplodedSet;
import entropy.vjob.Fence;
import entropy.vjob.OneOf;
import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJob;

/**
 * Unit tests for OneOf
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestOneOf {

    /**
     * Location of resources used for tests.
     */
    public static final String RESOURCES_LOCATION = "src/test/resources/entropy/vjob/constraint/TestOneOf.";

    public void testWithOneGroup() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "dst.txt");
        ManagedElementSet<VirtualMachine> t1 = new DefaultManagedElementSet<VirtualMachine>();
        VirtualMachine vm1 = src.getAllVirtualMachines().get("VM1");
        VirtualMachine vm2 = src.getAllVirtualMachines().get("VM2");
        t1.add(vm1);
        t1.add(vm2);
        ManagedElementSet<Node> g1 = new DefaultManagedElementSet<Node>();
        Node n3 = src.getOnlines().get("N3");
        Node n4 = src.getOnlines().get("N4");
        g1.add(n3);
        g1.add(n4);
        try {
            ChocoCustomRP plan = new ChocoCustomRP(new MockDurationEvaluator(1, 2, 3, 4, 5, 6, 7, 8));
            List<VJob> vjobs = new ArrayList<VJob>();
            VJob v = new BasicVJob("v1");
            v.addVirtualMachines(new ExplodedSet<VirtualMachine>(t1));
            Set<ManagedElementSet<Node>> ns = new HashSet<ManagedElementSet<Node>>();
            ns.add(g1);
            v.addConstraint(new Fence(new ExplodedSet<VirtualMachine>(t1), new ExplodedSet<Node>(g1)));
            vjobs.add(v);
            TimedReconfigurationPlan p = plan.compute(src,
                    dst.getRunnings(),
                    dst.getWaitings(),
                    dst.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    dst.getOnlines(),
                    dst.getOfflines(),
                    vjobs);
            Assert.assertEquals(p.size(), 3);
            Configuration res = p.getDestination();
            Node n1 = res.getAllNodes().get("N1");
            Node n2 = res.getAllNodes().get("N2");
            Assert.assertTrue(!res.getRunnings(n1).contains(vm1) && !res.getRunnings(n2).contains(vm1));
            Assert.assertTrue(!res.getRunnings(n1).contains(vm2) && !res.getRunnings(n2).contains(vm2));

            for (PlacementConstraint c : v.getConstraints()) {
                if (!c.isSatisfied(res)) {
                    Assert.fail(c + " is not satisfied");
                }
            }

        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    public void testWithTwoGroup() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "dst.txt");
        ManagedElementSet<VirtualMachine> t1 = new DefaultManagedElementSet<VirtualMachine>();
        VirtualMachine vm1 = src.getAllVirtualMachines().get("VM1");
        VirtualMachine vm2 = src.getAllVirtualMachines().get("VM2");
        VirtualMachine vm3 = src.getAllVirtualMachines().get("VM3");
        t1.add(vm1);
        t1.add(vm2);
        t1.add(vm3);
        ManagedElementSet<Node> g1 = new DefaultManagedElementSet<Node>();
        ManagedElementSet<Node> g2 = new DefaultManagedElementSet<Node>();
        Node n3 = src.getOnlines().get("N3");
        Node n4 = src.getOnlines().get("N4");
        g1.add(n3);
        g1.add(n4);
        g2.add(src.getOnlines().get("N1"));
        g2.add(src.getOnlines().get("N2"));
        try {
            ChocoCustomRP plan = new ChocoCustomRP(new MockDurationEvaluator(1, 2, 3, 4, 5, 6, 7, 8));
            plan.setRepairMode(false);
            List<VJob> vjobs = new ArrayList<VJob>();
            VJob v = new BasicVJob("v1");
            v.addVirtualMachines(new ExplodedSet<VirtualMachine>(t1));
            ExplodedMultiSet<Node> ns = new ExplodedMultiSet<Node>();
            ns.add(new ExplodedSet<Node>(g1));
            ns.add(new ExplodedSet<Node>(g2));
            OneOf f = new OneOf(new ExplodedSet<VirtualMachine>(t1), ns);
            v.addConstraint(f);
            vjobs.add(v);
            TimedReconfigurationPlan p = plan.compute(src,
                    dst.getRunnings(),
                    dst.getWaitings(),
                    dst.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    dst.getOnlines(),
                    dst.getOfflines(),
                    vjobs);
            System.err.println(p);
            Configuration res = p.getDestination();
            System.err.println(res);

            Assert.assertEquals(p.size(), 3);
            Node n1 = res.getAllNodes().get("N1");
            Node n2 = res.getAllNodes().get("N2");
            Assert.assertEquals(res.getRunnings(n1).size(), 0);
            Assert.assertEquals(res.getRunnings(n2).size(), 0);
            Assert.assertTrue(res.getRunnings(n3).contains(vm1) || res.getRunnings(n3).contains(vm2) || res.getRunnings(n3).contains(vm3));
            Assert.assertTrue(res.getRunnings(n4).contains(vm1) || res.getRunnings(n4).contains(vm2) || res.getRunnings(n4).contains(vm3));
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
     * Test isSatisfied() in various situations.
     */
    public void testIsSatisfied() {
        Configuration cfg = new DefaultConfiguration();
        Node n1 = new DefaultNode("N1", 1, 1, 1);
        Node n2 = new DefaultNode("N2", 1, 1, 1);
        Node n3 = new DefaultNode("N3", 1, 1, 1);
        Node n4 = new DefaultNode("N4", 1, 1, 1);
        Set<ManagedElementSet<Node>> grps = new HashSet<ManagedElementSet<Node>>();

        ManagedElementSet<Node> grp1 = new DefaultManagedElementSet<Node>();
        grp1.add(n1);
        grp1.add(n2);
        grps.add(grp1);

        ManagedElementSet<Node> grp2 = new DefaultManagedElementSet<Node>();
        grp2.add(n3);
        grp2.add(n4);
        grps.add(grp2);

        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("VM1", 1, 1, 1);
        DefaultVirtualMachine vm2 = new DefaultVirtualMachine("VM2", 1, 1, 1);
        DefaultVirtualMachine vm3 = new DefaultVirtualMachine("VM3", 1, 1, 1);
        DefaultVirtualMachine vm4 = new DefaultVirtualMachine("VM4", 1, 1, 1);

        cfg.addOnline(n1);
        cfg.addOnline(n2);
        cfg.addOnline(n3);
        cfg.addOnline(n4);
        cfg.setRunOn(vm1, n1);
        cfg.setRunOn(vm2, n2);
        cfg.setRunOn(vm4, n3);
        cfg.setSleepOn(vm3, n2);

        ManagedElementSet<VirtualMachine> vms = new DefaultManagedElementSet<VirtualMachine>();
        vms.add(vm1);
        vms.add(vm2);
        vms.add(vm3);
        Assert.assertFalse(new Fence(new ExplodedSet<VirtualMachine>(cfg.getAllVirtualMachines()), new ExplodedSet<Node>(new DefaultManagedElementSet<Node>())).isSatisfied(cfg));
        Assert.assertTrue(new Fence(new ExplodedSet<VirtualMachine>(vms), new ExplodedSet<Node>(grp1)).isSatisfied(cfg));
        grp1.remove(n1);
        Assert.assertFalse(new Fence(new ExplodedSet<VirtualMachine>(cfg.getAllVirtualMachines()), new ExplodedSet<Node>(grp1)).isSatisfied(cfg));
    }
}
