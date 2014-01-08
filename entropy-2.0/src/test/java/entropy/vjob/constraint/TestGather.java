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

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.Configuration;
import entropy.configuration.DefaultNode;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.SimpleConfiguration;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.SimpleNode;
import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VirtualMachine;
import entropy.vjob.ExplodedSet;
import entropy.vjob.Gather;

/**
 * Unit tests for Gather.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestGather {

    /**
     * Test isSatisfied() in various situations.
     */
    public void testIsSatisfied() {
        Configuration cfg = new SimpleConfiguration();
        Node n1 = new SimpleNode("N1", 1, 1, 1);
        Node n2 = new SimpleNode("N2", 1, 1, 1);
        VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 1, 1, 1);
        VirtualMachine vm2 = new SimpleVirtualMachine("VM2", 1, 1, 1);
        VirtualMachine vm3 = new SimpleVirtualMachine("VM3", 1, 1, 1);
        VirtualMachine vm4 = new SimpleVirtualMachine("VM4", 1, 1, 1);

        cfg.addOnline(n1);
        cfg.addOnline(n2);
        cfg.setRunOn(vm1, n1);
        cfg.setRunOn(vm2, n1);
        cfg.setRunOn(vm4, n2);
        cfg.setSleepOn(vm3, n2);

        ManagedElementSet<VirtualMachine> vms = new SimpleManagedElementSet<VirtualMachine>();
        vms.add(vm1);
        vms.add(vm2);
        Assert.assertTrue(new Gather(new ExplodedSet<VirtualMachine>(new SimpleManagedElementSet<VirtualMachine>())).isSatisfied(cfg));
        Assert.assertFalse(new Gather(new ExplodedSet<VirtualMachine>(cfg.getAllVirtualMachines())).isSatisfied(cfg));
        Assert.assertTrue(new Gather(new ExplodedSet<VirtualMachine>(vms)).isSatisfied(cfg));
    }

    public void testGetMisplaced() {
        Configuration cfg = new SimpleConfiguration();
        Node n1 = new DefaultNode("N1", 1, 1, 1);
        Node n2 = new DefaultNode("N2", 1, 1, 1);
        VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 1, 1, 1);
        VirtualMachine vm2 = new SimpleVirtualMachine("VM2", 1, 1, 1);
        VirtualMachine vm3 = new SimpleVirtualMachine("VM3", 1, 1, 1);
        VirtualMachine vm4 = new SimpleVirtualMachine("VM4", 1, 1, 1);

        cfg.addOnline(n1);
        cfg.addOnline(n2);
        cfg.setRunOn(vm1, n1);
        cfg.setRunOn(vm2, n1);
        cfg.setRunOn(vm4, n2);
        cfg.setSleepOn(vm3, n2);

        ManagedElementSet<VirtualMachine> vms = new SimpleManagedElementSet<VirtualMachine>();
        vms.add(vm1);
        vms.add(vm2);
        Assert.assertEquals(new Gather(new ExplodedSet<VirtualMachine>(new SimpleManagedElementSet<VirtualMachine>())).getMisPlaced(cfg), new SimpleManagedElementSet<VirtualMachine>());
        ExplodedSet<VirtualMachine> baddies = new ExplodedSet<VirtualMachine>(cfg.getAllVirtualMachines());
        Assert.assertEquals(new Gather(baddies).getMisPlaced(cfg), baddies);
        Assert.assertEquals(new Gather(new ExplodedSet<VirtualMachine>(vms)).getMisPlaced(cfg), new ExplodedSet<VirtualMachine>());
    }
}
