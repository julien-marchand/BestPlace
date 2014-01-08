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

package entropy.plan.partitioner;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.SimpleConfiguration;
import entropy.configuration.SimpleNode;
import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VirtualMachine;
import entropy.vjob.ExplodedMultiSet;
import entropy.vjob.ExplodedSet;
import entropy.vjob.Fence;
import entropy.vjob.OneOf;

/**
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestPlanPartitioner {

    public void testSimpleFence() {
        Configuration cfg = new SimpleConfiguration();

        //Some nodes
        ExplodedSet<Node>[] parts = new ExplodedSet[4];
        for (int i = 0; i < parts.length; i++) {
            parts[i] = new ExplodedSet<Node>("$P" + (i + 1));
            for (int j = 0; j < 5; j++) {
                Node n = new SimpleNode("N" + (10 * i + j + 1), 10, 10, 10);
                parts[i].add(n);
                cfg.addOnline(n);
            }
        }

        //Some VMs
        ExplodedSet<VirtualMachine>[] apps = new ExplodedSet[5];
        for (int i = 0; i < apps.length; i++) {
            apps[i] = new ExplodedSet<VirtualMachine>("$A" + (i + 1));
            for (int j = 0; j < 10; j++) {
                VirtualMachine vm = new SimpleVirtualMachine("VM" + (i * 10 + j + 1), 1, 1, 1);
                apps[i].add(vm);
                cfg.addWaiting(vm);
            }
        }

        PlanPartitioner part = new OtherPartitioning(cfg);
        for (int i = 0; i < apps.length; i++) {
            Fence f = new Fence(apps[i], parts[i % parts.length]);
            try {
                part.part(f);
            } catch (PartitioningException e) {
                Assert.fail();
            }
        }
        List<Partition> ps = part.getResultingPartitions();
        Assert.assertEquals(ps.size(), 4);
        Assert.assertTrue(ps.get(0).getNodes().equals(parts[0]) && ps.get(0).getVirtualMachines().containsAll(apps[0]) && ps.get(0).getVirtualMachines().containsAll(apps[4]));
        Assert.assertTrue(ps.get(1).getNodes().equals(parts[1]) && ps.get(1).getVirtualMachines().containsAll(apps[1]));
        Assert.assertTrue(ps.get(2).getNodes().equals(parts[2]) && ps.get(2).getVirtualMachines().containsAll(apps[2]));
        Assert.assertTrue(ps.get(3).getNodes().equals(parts[3]) && ps.get(3).getVirtualMachines().containsAll(apps[3]));
    }

    /**
     * Test oneOf in presence of a fence constraint
     */
    public void testFenceOneOf() {
        Configuration cfg = new SimpleConfiguration();

        //Some nodes
        ExplodedSet<Node>[] parts = new ExplodedSet[2];
        for (int i = 0; i < parts.length; i++) {
            parts[i] = new ExplodedSet<Node>("$P" + (i + 1));
            for (int j = 0; j < 5; j++) {
                Node n = new SimpleNode("N" + (10 * i + j + 1), 10, 10, 10);
                parts[i].add(n);
                cfg.addOnline(n);
            }
        }

        //Some VMs
        ExplodedSet<VirtualMachine> app = new ExplodedSet<VirtualMachine>("$A");
        ExplodedSet<VirtualMachine> sub = new ExplodedSet<VirtualMachine>("$A/2");
        for (int i = 0; i < 10; i++) {
            VirtualMachine vm = new SimpleVirtualMachine("VM" + (i + 1), 1, 1, 1);
            cfg.addWaiting(vm);
            if (i < 5) {
                sub.add(vm);
            }
            app.add(vm);
        }

        PlanPartitioner part = new OtherPartitioning(cfg);
        ExplodedMultiSet<Node> m = new ExplodedMultiSet<Node>();
        m.add(parts[0]);
        m.add(parts[1]);
        OneOf of = new OneOf(app, m);
        Fence f = new Fence(app, parts[0]);

        try {
            part.part(f);
            part.part(of);
        } catch (PartitioningException e) {
            Assert.fail(e.getMessage(), e);
        }
        part.getResultingPartitions();
    }

    public void testSinglePartition() {

    }
}
