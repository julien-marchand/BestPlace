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

import entropy.configuration.DefaultConfiguration;
import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.vjob.ExplodedSet;

/**
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestSplit {

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
        DefaultVirtualMachine vm4 = new DefaultVirtualMachine("VM4", 1, 1, 1);


        ManagedElementSet<VirtualMachine> s1 = new DefaultManagedElementSet<VirtualMachine>();
        ManagedElementSet<VirtualMachine> s2 = new DefaultManagedElementSet<VirtualMachine>();

        s1.add(vm1);
        s2.add(vm3);
        s2.add(vm4);

        cfg.addOnline(n1);
        cfg.addOnline(n2);
        cfg.setRunOn(vm1, n1);
        cfg.setRunOn(vm2, n1);
        cfg.setRunOn(vm4, n2);
        cfg.setSleepOn(vm3, n2);

        Assert.assertFalse(new MockSplit(new ExplodedSet<VirtualMachine>(new DefaultManagedElementSet<VirtualMachine>()), new ExplodedSet<VirtualMachine>(new DefaultManagedElementSet<VirtualMachine>())).isSatisfied(cfg));
        Assert.assertTrue(new MockSplit(new ExplodedSet<VirtualMachine>(s1), new ExplodedSet<VirtualMachine>(s2)).isSatisfied(cfg));
        s2.add(vm2);
        Assert.assertFalse(new MockSplit(new ExplodedSet<VirtualMachine>(s1), new ExplodedSet<VirtualMachine>(s2)).isSatisfied(cfg));
    }
}
