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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for ResourcePicker.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestResourcePicker {

    public void testVM() {
        VirtualMachine vm = new SimpleVirtualMachine("VM1", 1, 2, 3, 4, 5);
        Assert.assertEquals(ResourcePicker.get(vm, ResourcePicker.VMRc.cpuConsumption), 2);
        Assert.assertEquals(ResourcePicker.get(vm, ResourcePicker.VMRc.memoryConsumption), 3);
        Assert.assertEquals(ResourcePicker.get(vm, ResourcePicker.VMRc.cpuDemand), 4);
        Assert.assertEquals(ResourcePicker.get(vm, ResourcePicker.VMRc.memoryDemand), 5);
    }

    public void testNode() {
        Node n = new SimpleNode("N1", 1, 2, 3);
        Assert.assertEquals(ResourcePicker.get(n, ResourcePicker.NodeRc.cpuCapacity), 2);
        Assert.assertEquals(ResourcePicker.get(n, ResourcePicker.NodeRc.memoryCapacity), 3);
    }

}
