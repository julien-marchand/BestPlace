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
package entropy.decision.vjobScheduler;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.TestHelper;
import entropy.configuration.Configuration;
import entropy.configuration.VirtualMachine;
import entropy.vjob.VJob;
import entropy.vjob.builder.MockVirtualMachineBuilder;
import entropy.vjob.builder.VJobBuilder;
import entropy.vjob.builder.VJobElementBuilder;
import entropy.vjob.queue.FCFSPool;
import entropy.vjob.queue.VJobsPool;

/**
 * Unit tests for FirstFitVJobScheduler.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestFirstFitVJobScheduler {

    /**
     * The base of the test resources.
     */
    public static final String RESOURCES_DIR = "src/test/resources/entropy/decision/vjobScheduler/TestFirstFitVJobScheduler.";

    /**
     * Basic test of compute().
     */
    public void testValidAppend1() {

        VJobsPool q = new FCFSPool();
        Configuration c = TestHelper.readConfiguration(RESOURCES_DIR + "confValidAppend1.txt");

        try {
            VJobBuilder b = new VJobBuilder(new VJobElementBuilder(new MockVirtualMachineBuilder()), null);
            b.getElementBuilder().useConfiguration(c);
            VJob l = b.build("lease1", new File(RESOURCES_DIR + "lease1.txt"));
            q.add(l);

            FirstFitVJobScheduler ff = new FirstFitVJobScheduler(q);
            Configuration res = ff.compute(c);
            for (VirtualMachine vm : l.getVirtualMachines()) {
                Assert.assertTrue(res.getRunnings().contains(vm), "looking for " + vm);
            }
            Assert.assertEquals(res.getRunnings().size(), l.getVirtualMachines().size());
            Assert.assertEquals((Object) res.getOnlines(), (Object) c.getOnlines());
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test on a first fit decrease.
     */
    public void testValidAppend2() {

        VJobsPool q = new FCFSPool();
        Configuration c = TestHelper.readConfiguration(RESOURCES_DIR + "confValidAppend2.txt");

        try {
            VJobBuilder b = new VJobBuilder(new VJobElementBuilder(new MockVirtualMachineBuilder()), null);
            b.getElementBuilder().useConfiguration(c);
            VJob l = b.build("lease2", new File(RESOURCES_DIR + "lease2.txt"));
            q.add(l);
            FirstFitVJobScheduler ff = new FirstFitVJobScheduler(q);
            Configuration res = ff.compute(c);
            Assert.assertEquals(res.getRunnings(), l.getVirtualMachines());
            Assert.assertEquals(res.getOnlines(), c.getOnlines());
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * First fit decrease with an configuration that already host nodes.
     */
    public void testValidAppend3() {
        VJobsPool q = new FCFSPool();
        try {
            Configuration c = TestHelper.readConfiguration(RESOURCES_DIR + "testValidAppend3_src.txt");
            VJobBuilder b = new VJobBuilder(new VJobElementBuilder(new MockVirtualMachineBuilder()), null);
            b.getElementBuilder().useConfiguration(c);
            VJob v = b.build("lease3", new File(RESOURCES_DIR + "lease3.txt"));
            q.add(v);
            FirstFitVJobScheduler ff = new FirstFitVJobScheduler(q);
            Configuration res = ff.compute(c);
            Assert.assertEquals(res.getRunnings().size(), v.getVirtualMachines().size());
            Assert.assertEquals(res.getOnlines(), c.getOnlines());
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Result must be a configuration without any affected virtual machines.
     */
    public void testInvalidAppend1() {
        VJobsPool q = new FCFSPool();

        Configuration c = TestHelper.readConfiguration(RESOURCES_DIR + "confInvalidAppend1.txt");
        Configuration res = null;
        try {
            VJobBuilder b = new VJobBuilder(new VJobElementBuilder(new MockVirtualMachineBuilder()), null);
            b.getElementBuilder().useConfiguration(c);
            q.add(b.build("lease1", new File(RESOURCES_DIR + "lease1.txt")));
            FirstFitVJobScheduler ff = new FirstFitVJobScheduler(q);
            res = ff.compute(c);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        Assert.assertEquals(res.getOnlines(), c.getOnlines());
        Assert.assertEquals(res.getRunnings().size(), 0);
    }

    /**
     * Compute with 3 leases, all can fit.
     */
    public void testCompute1() {
        VJobsPool q = new FCFSPool();

        Configuration c = TestHelper.readConfiguration(RESOURCES_DIR + "confCompute1.txt");

        try {
            VJobBuilder b = new VJobBuilder(new VJobElementBuilder(new MockVirtualMachineBuilder()), null);
            b.getElementBuilder().useConfiguration(c);
            VJob l1 = b.build("lease1", new File(RESOURCES_DIR + "lease1.txt"));
            VJob l2 = b.build("lease2", new File(RESOURCES_DIR + "lease2.txt"));
            VJob l3 = b.build("lease3", new File(RESOURCES_DIR + "lease3.txt"));
            q.add(l1);
            q.add(l2);
            q.add(l3);

            FirstFitVJobScheduler ff = new FirstFitVJobScheduler(q);
            Configuration res = ff.compute(c);
            Assert.assertEquals(res.getRunnings().size(), l1.getVirtualMachines().size() + l2.getVirtualMachines().size() + l3.getVirtualMachines().size());
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Compute with 3 leases, second can't fit.
     */
    public void testCompute2() {
        VJobsPool q = new FCFSPool();
        Configuration c = TestHelper.readConfiguration(RESOURCES_DIR + "confCompute2.txt");
        try {
            VJobBuilder b = new VJobBuilder(new VJobElementBuilder(new MockVirtualMachineBuilder()), null);
            b.getElementBuilder().useConfiguration(c);
            VJob l1 = b.build("leasee1", new File(RESOURCES_DIR + "lease1.txt"));
            VJob l2 = b.build("leasee2", new File(RESOURCES_DIR + "lease2.txt"));
            VJob l3 = b.build("leasee3", new File(RESOURCES_DIR + "lease3.txt"));
            q.add(l1);
            q.add(l2);
            q.add(l3);
            FirstFitVJobScheduler ff = new FirstFitVJobScheduler(q);
            Configuration res = ff.compute(c);
            Assert.assertEquals(res.getRunnings().size(), l1.getVirtualMachines().size() + l3.getVirtualMachines().size());
            for (VirtualMachine vm : l2.getVirtualMachines()) {
                Assert.assertTrue(res.getWaitings().contains(vm));
            }
            Assert.assertEquals(res.getWaitings().size(), l2.getVirtualMachines().size());

        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }
}
