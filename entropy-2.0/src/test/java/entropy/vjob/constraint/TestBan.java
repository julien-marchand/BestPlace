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

import java.util.LinkedList;
import java.util.List;

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
import entropy.vjob.Ban;
import entropy.vjob.BasicVJob;
import entropy.vjob.ExplodedSet;
import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJob;

/**
 * Unit tests for ChocoBan.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestBan {

    /**
     * Location of resources used for tests.
     */
    public static final String RESOURCES_LOCATION = "src/test/resources/entropy/vjob/constraint/TestBan.";


    /**
     * Test with a set composed only with future running VMs.
     */
    public void testWithAllRunnings() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "dst.txt");

        ChocoCustomRP plan = new ChocoCustomRP(new MockDurationEvaluator(1, 2, 3, 4, 5, 6, 7, 8));

        try {
            ManagedElementSet<Node> ns = new DefaultManagedElementSet<Node>();
            Node n1 = src.getAllNodes().get("N1");
            Node n3 = src.getAllNodes().get("N3");
            ns.add(n1);
            ns.add(n3);
            VJob v = new BasicVJob("v");
            v.addVirtualMachines(new ExplodedSet<VirtualMachine>(src.getAllVirtualMachines()));
            ManagedElementSet<VirtualMachine> vms = new DefaultManagedElementSet<VirtualMachine>();
            VirtualMachine vm1 = v.getVirtualMachines().get("VM1");
            VirtualMachine vm2 = v.getVirtualMachines().get("VM2");
            VirtualMachine vm4 = v.getVirtualMachines().get("VM4");
            vms.add(vm1);
            vms.add(vm2);
            v.addConstraint(new Ban(new ExplodedSet<VirtualMachine>(vms), new ExplodedSet<Node>(ns)));
            v.addConstraint(new Ban(new ExplodedSet<VirtualMachine>(new DefaultManagedElementSet<VirtualMachine>(vm4)), new ExplodedSet<Node>(new DefaultManagedElementSet<Node>(n3))));
            v.addConstraint(new Ban(new ExplodedSet<VirtualMachine>(new DefaultManagedElementSet<VirtualMachine>(vm4)), new ExplodedSet<Node>(new DefaultManagedElementSet<Node>(src.getAllNodes().get("N2")))));
            List<VJob> queue = new LinkedList<VJob>();
            queue.add(v);
            TimedReconfigurationPlan p = plan.compute(src,
                    dst.getRunnings(),
                    dst.getWaitings(),
                    dst.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    dst.getOnlines(),
                    dst.getOfflines(),
                    queue);
            Configuration res = p.getDestination();
            Node n2 = res.getAllNodes().get("N2");
            Assert.assertEquals(p.size(), 2);
            Assert.assertEquals(res.getLocation(vm1), n2);
            Assert.assertEquals(res.getLocation(vm2), n2);
            Assert.assertEquals(res.getLocation(vm4), n1);

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
     * Test with a set composed with runnings & non-running VMs.
     * The constraint is not considered for non-running VMs.
     */
    public void testWithNonRunnings() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "dst2.txt");
        ChocoCustomRP plan = new ChocoCustomRP(new MockDurationEvaluator(1, 2, 3, 4, 5, 6, 7, 8));
        try {
            ManagedElementSet<Node> ns = new DefaultManagedElementSet<Node>();
            Node n1 = src.getAllNodes().get("N1");
            Node n2 = src.getAllNodes().get("N2");
            Node n3 = src.getAllNodes().get("N3");
            ns.add(n1);
            ns.add(n2);
            VJob v = new BasicVJob("v");
            v.addVirtualMachines(new ExplodedSet<VirtualMachine>(src.getAllVirtualMachines()));
            ManagedElementSet<VirtualMachine> vms = new DefaultManagedElementSet<VirtualMachine>();
            VirtualMachine vm3 = v.getVirtualMachines().get("VM3");
            VirtualMachine vm2 = v.getVirtualMachines().get("VM2");
            VirtualMachine vm4 = v.getVirtualMachines().get("VM4");
            vms.add(vm3);
            vms.add(vm2);
            Ban b1 = new Ban(new ExplodedSet<VirtualMachine>(vms), new ExplodedSet<Node>(ns));
            v.addConstraint(b1);
            //An entailed constraint, vm4 is sleeping so stay on a node supposed to be avoided
            Ban b2 = new Ban(new ExplodedSet<VirtualMachine>(new DefaultManagedElementSet<VirtualMachine>(vm4)), new ExplodedSet<Node>(new DefaultManagedElementSet<Node>(n3)));
            v.addConstraint(b2);
            List<VJob> queue = new LinkedList<VJob>();
            queue.add(v);
            TimedReconfigurationPlan p = plan.compute(src,
                    dst.getRunnings(),
                    dst.getWaitings(),
                    dst.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    dst.getOnlines(),
                    dst.getOfflines(),
                    queue);
            Configuration res = p.getDestination();
            Assert.assertEquals(p.size(), 2);
            Assert.assertEquals(res.getLocation(vm3), n3);
            Assert.assertEquals(res.getLocation(vm2), n2);
            Assert.assertEquals(res.getLocation(vm4), n3);

            Assert.assertTrue(b1.isSatisfied(res));
            Assert.assertTrue(b2.isSatisfied(res));

        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test isSatisfied() in various situations.
     */
    public void testIsSatisfied() {
        DefaultConfiguration cfg = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("N1", 1, 1, 1);
        DefaultNode n2 = new DefaultNode("N2", 1, 1, 1);
        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("VM1", 1, 1, 1);
        DefaultVirtualMachine vm2 = new DefaultVirtualMachine("VM2", 1, 1, 1);
        DefaultVirtualMachine vm3 = new DefaultVirtualMachine("VM3", 1, 1, 1);

        cfg.addOnline(n1);
        cfg.addOnline(n2);
        cfg.setRunOn(vm1, n1);
        cfg.setRunOn(vm2, n1);
        cfg.setSleepOn(vm3, n2);
        Assert.assertTrue(new Ban(new ExplodedSet<VirtualMachine>(cfg.getAllVirtualMachines()), new ExplodedSet<Node>(new DefaultManagedElementSet<Node>(n2))).isSatisfied(cfg));
        Assert.assertFalse(new Ban(new ExplodedSet<VirtualMachine>(cfg.getAllVirtualMachines()), new ExplodedSet<Node>(new DefaultManagedElementSet<Node>(n1))).isSatisfied(cfg));
        Assert.assertFalse(new Ban(new ExplodedSet<VirtualMachine>(cfg.getAllVirtualMachines()), new ExplodedSet<Node>(cfg.getAllNodes())).isSatisfied(cfg));
    }
}
