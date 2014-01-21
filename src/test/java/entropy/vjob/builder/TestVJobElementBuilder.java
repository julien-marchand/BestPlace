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

package entropy.vjob.builder;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.DefaultConfiguration;
import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;

/**
 * Unit tests for {@link VJobElementBuilder}.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestVJobElementBuilder {

    /**
     * Test the matching of node in various situations
     */
    public void TestNodeMatching() {

        DefaultConfiguration cfg = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("N1", 1, 1, 1);
        DefaultNode n2 = new DefaultNode("N2", 1, 1, 1);
        cfg.addOnline(n1);
        cfg.addOnline(n2);
        //VMBuilder is optional here
        VJobElementBuilder eb = new VJobElementBuilder(null);

        //No configuration, so null
        Assert.assertNull(eb.matchAsNode("N3"));
        Assert.assertNull(eb.matchAsNode("N1"));

        //A configuration is used
        eb.useConfiguration(cfg);
        Assert.assertNull(eb.matchAsNode("N3"));
        Assert.assertEquals(eb.matchAsNode("N1"), n1);
    }

    /**
     * Test VM matching is several conditions.
     */
    public void testVirtualMachineMatching() {
        DefaultConfiguration cfg = new DefaultConfiguration();
        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("VM1", 1, 1, 1);
        DefaultVirtualMachine vm2 = new DefaultVirtualMachine("VM2", 1, 1, 1);
        cfg.addWaiting(vm1);
        MockVirtualMachineBuilder mock = new MockVirtualMachineBuilder();
        mock.farm.add("VM2");
        VJobElementBuilder eb = new VJobElementBuilder(null);

        //No vmBuilder nor configuration, so null
        Assert.assertNull(eb.matchAsVirtualMachine("VM1"));
        Assert.assertNull(eb.matchAsVirtualMachine("VM2"));

        //A configuration, so only vm1
        eb.useConfiguration(cfg);
        Assert.assertEquals(eb.matchAsVirtualMachine("VM1"), vm1);
        Assert.assertNull(eb.matchAsVirtualMachine("VM2"));

        //A vmBuilder, so only vm2
        eb = new VJobElementBuilder(mock);
        Assert.assertEquals(eb.matchAsVirtualMachine("VM2"), vm2);
    }
}
