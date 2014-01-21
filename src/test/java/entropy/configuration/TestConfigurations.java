/*
 * Copyright (c) Fabien Hermenier
 *
 * This file is part of Entropy.
 *
 * Entropy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Entropy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.configuration;

import static entropy.configuration.Configurations.State.Runnings;
import static entropy.configuration.Configurations.State.Sleepings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.parser.ConfigurationSerializerException;
import entropy.configuration.parser.PlainTextConfigurationSerializer;

/**
 * Unit tests for Configurations.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestConfigurations {

    private static final String RESOURCE_ROOT = "src/test/resources/entropy/configuration/TestConfigurations.";

    private Configuration makeConfiguration() {
        Configuration c = new SimpleConfiguration();
        for (int i = 0; i < 10; i++) {
            Node n = new SimpleNode("N" + (i + 1), 1, 5, 5);
            c.addOnline(n);
        }
        c.addOffline(new SimpleNode("N11", 4, 4, 4));

        c.setRunOn(new SimpleVirtualMachine("VM1", 1, 1, 1), c.getOnlines().get("N1"));
        c.setRunOn(new SimpleVirtualMachine("VM2", 1, 1, 1), c.getOnlines().get("N2"));
        c.setRunOn(new SimpleVirtualMachine("VM3", 1, 1, 1, 2, 2), c.getOnlines().get("N3"));
        c.setRunOn(new SimpleVirtualMachine("VM4", 1, 1, 1), c.getOnlines().get("N4"));
        c.setRunOn(new SimpleVirtualMachine("VM5", 1, 1, 1), c.getOnlines().get("N5"));
        c.setRunOn(new SimpleVirtualMachine("VM6", 1, 1, 1), c.getOnlines().get("N5"));
        c.setRunOn(new SimpleVirtualMachine("VM7", 1, 4, 4, 1, 1), c.getOnlines().get("N5"));
        c.setRunOn(new SimpleVirtualMachine("VM8", 1, 1, 1, 4, 4), c.getOnlines().get("N3"));
        c.setRunOn(new SimpleVirtualMachine("VM9", 1, 1, 1), c.getOnlines().get("N8"));
        c.setSleepOn(new SimpleVirtualMachine("VM10", 1, 1, 1), c.getOnlines().get("N2"));
        c.setSleepOn(new SimpleVirtualMachine("VM11", 1, 1, 1), c.getOnlines().get("N7"));
        return c;
    }

    public void testUsedNodes() {
        Configuration cfg = makeConfiguration();
        ManagedElementSet<Node> ns = Configurations.usedNodes(cfg, EnumSet.of(Runnings));
        Assert.assertEquals(ns.size(), 6);
        ns = Configurations.usedNodes(cfg, EnumSet.of(Sleepings));
        Assert.assertEquals(ns.size(), 2);
        ns = Configurations.usedNodes(cfg, EnumSet.of(Runnings, Sleepings));
        Assert.assertEquals(ns.size(), 7);
    }

    public void testUnusedNodes() {
        Configuration cfg = makeConfiguration();
        ManagedElementSet<Node> ns = Configurations.unusedNodes(cfg, Runnings);
        Assert.assertEquals(ns.size(), 4);
        ns = Configurations.unusedNodes(cfg, Sleepings);
        Assert.assertEquals(ns.size(), 8);
    }

    public void testCurrentlyOverloadedNodes() {
        Configuration cfg = makeConfiguration();
        ManagedElementSet<Node> ns = Configurations.currentlyOverloadedNodes(cfg);
        Assert.assertFalse(Configurations.isCurrentlyViable(cfg));
        Assert.assertEquals(ns.size(), 1);
        Assert.assertEquals(ns.get(0).getName(), "N5");
        cfg.remove(cfg.getRunnings().get("VM7"));
        Assert.assertTrue(Configurations.isCurrentlyViable(cfg));
    }

    public void testFutureOverloadedNodes() {
        Configuration cfg = makeConfiguration();
        Assert.assertFalse(Configurations.isFutureViable(cfg));
        Assert.assertEquals(Configurations.futureOverloadedNodes(cfg).size(), 1);
        Assert.assertEquals(Configurations.futureOverloadedNodes(cfg).get(0).getName(), "N3");
        cfg.remove(cfg.getRunnings().get("VM3"));
        Assert.assertTrue(Configurations.isFutureViable(cfg));

    }

    public void testSubConfiguration() {
        Configuration cfg = makeConfiguration();
        cfg.addWaiting(new SimpleVirtualMachine("VMXX", 1, 2, 3));

        ManagedElementSet<Node> ns = new SimpleManagedElementSet<Node>();
        ns.addAll(cfg.getOfflines());
        ns.add(cfg.getAllNodes().get("N1"));
        ns.add(cfg.getAllNodes().get("N2"));
        ns.add(cfg.getAllNodes().get("N3"));
        ns.add(cfg.getAllNodes().get("N11"));
        ManagedElementSet<VirtualMachine> vms = new SimpleManagedElementSet<VirtualMachine>();
        vms.add(cfg.getAllVirtualMachines().get("VM1"));
        vms.add(cfg.getAllVirtualMachines().get("VM2"));
        vms.add(cfg.getAllVirtualMachines().get("VM10"));
        vms.add(cfg.getAllVirtualMachines().get("VM3"));
        vms.add(cfg.getAllVirtualMachines().get("VM8"));
        vms.add(cfg.getAllVirtualMachines().get("VMXX"));


        try {
            Assert.assertEquals(Configurations.subConfiguration(cfg, cfg.getAllVirtualMachines(), cfg.getAllNodes()), cfg);

            Configuration c = Configurations.subConfiguration(cfg, vms, ns);

            for (VirtualMachine vm : vms) {
                Assert.assertEquals(c.getLocation(vm), cfg.getLocation(vm));
            }
            for (Node n : ns) {
                Assert.assertEquals(c.getRunnings(n), cfg.getRunnings(n));
                Assert.assertEquals(c.getSleepings(n), cfg.getSleepings(n));
            }
        } catch (ConfigurationsException e) {
            Assert.fail(e.getMessage(), e);
        }
        //TODO: check some failures
    }

    /**
     * Test configuration merging with good sub configurations
     */
    public void testGoodMerge() {
        try {
            Configuration c1 = PlainTextConfigurationSerializer.getInstance().read(RESOURCE_ROOT + "testMerge1.txt");
            Configuration c2 = PlainTextConfigurationSerializer.getInstance().read(RESOURCE_ROOT + "testMerge2.txt");
            Configuration expected = PlainTextConfigurationSerializer.getInstance().read(RESOURCE_ROOT + "testMerge-good.txt");
            Configuration res = Configurations.merge(c1, c2);
            List<Configuration> l = new ArrayList<Configuration>();
            l.add(c1);
            l.add(c2);
            Assert.assertEquals(res, expected);
            Assert.assertEquals(res, Configurations.merge(l));
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test configuration merging with non disjoint set of VMs
     */
    @Test(expectedExceptions = {ConfigurationsException.class})
    public void testBadMergeWithConflictingVMs() throws ConfigurationsException {
        try {
            Configuration c1 = PlainTextConfigurationSerializer.getInstance().read(RESOURCE_ROOT + "testMerge1.txt");
            Configuration c2 = PlainTextConfigurationSerializer.getInstance().read(RESOURCE_ROOT + "testMerge3.txt");
            Configurations.merge(c1, c2);
        } catch (IOException e) {
            Assert.fail(e.getMessage(), e);
        } catch (ConfigurationSerializerException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test configuration merging with nodes in conflicting states
     */
    @Test(expectedExceptions = {ConfigurationsException.class})
    public void testBadMergeWithConflictingNodes() throws ConfigurationsException {
        try {
            Configuration c1 = PlainTextConfigurationSerializer.getInstance().read(RESOURCE_ROOT + "testMerge1.txt");
            Configuration c2 = PlainTextConfigurationSerializer.getInstance().read(RESOURCE_ROOT + "testMerge4.txt");
            Configurations.merge(c1, c2);
        } catch (IOException e) {
            Assert.fail(e.getMessage(), e);
        } catch (ConfigurationSerializerException e) {
            Assert.fail(e.getMessage(), e);
        }
    }
}
