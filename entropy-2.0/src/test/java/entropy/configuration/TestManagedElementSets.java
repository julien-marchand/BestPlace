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

import static entropy.configuration.ResourcePicker.NodeRc.cpuCapacity;
import static entropy.configuration.ResourcePicker.NodeRc.memoryCapacity;
import static entropy.configuration.ResourcePicker.VMRc.cpuConsumption;
import static entropy.configuration.ResourcePicker.VMRc.cpuDemand;
import static entropy.configuration.ResourcePicker.VMRc.memoryConsumption;
import static entropy.configuration.ResourcePicker.VMRc.memoryDemand;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for ManagedElementSets.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestManagedElementSets {

    private ManagedElementSet<VirtualMachine> makeDefaultVMSet() {
        ManagedElementSet<VirtualMachine> s = new SimpleManagedElementSet<VirtualMachine>();
        s.add(new SimpleVirtualMachine("VM1", 1, 2, 3, 4, 5));
        s.add(new SimpleVirtualMachine("VM2", 1, 3, 2, 7, 5));
        s.add(new SimpleVirtualMachine("VM3", 1, 2, 5, 2, 8));
        s.add(new SimpleVirtualMachine("VM4", 2, 1, 3, 5, 5));
        s.add(new SimpleVirtualMachine("VM5", 2, 1, 3));
        return s;
    }

    private ManagedElementSet<Node> makeDefaultNodeSet() {
        ManagedElementSet<Node> s = new SimpleManagedElementSet<Node>();
        s.add(new SimpleNode("N1", 1, 2, 2));
        s.add(new SimpleNode("N2", 1, 3, 4));
        s.add(new SimpleNode("N3", 1, 2, 1));
        s.add(new SimpleNode("N4", 2, 1, 4));
        s.add(new SimpleNode("N5", 2, 1, 7));
        return s;
    }

    public void testNodeSum() {
        ManagedElementSet<Node> s = makeDefaultNodeSet();
        int[] sum = ManagedElementSets.sum(s, cpuCapacity, memoryCapacity);
        Assert.assertEquals(Arrays.toString(sum), Arrays.toString(new int[]{9, 18}));
    }

    public void testNodeMin() {
        ManagedElementSet<Node> s = makeDefaultNodeSet();
        Node minCpu = ManagedElementSets.min(s, cpuCapacity);
        Node minMem = ManagedElementSets.min(s, memoryCapacity);
        Assert.assertEquals(minCpu.getCPUCapacity(), 1);
        Assert.assertEquals(minMem.getMemoryCapacity(), 1);

        s = new SimpleManagedElementSet<Node>();
        Assert.assertNull(ManagedElementSets.min(s, cpuCapacity));
    }

    public void testNodeMax() {
        ManagedElementSet<Node> s = makeDefaultNodeSet();
        Node maxCpu = ManagedElementSets.max(s, cpuCapacity);
        Node maxMem = ManagedElementSets.max(s, memoryCapacity);
        Assert.assertEquals(maxCpu.getCPUCapacity(), 3);
        Assert.assertEquals(maxMem.getMemoryCapacity(), 7);

        s = new SimpleManagedElementSet<Node>();
        Assert.assertNull(ManagedElementSets.max(s, cpuCapacity));
    }

    public void testSum() {
        ManagedElementSet<VirtualMachine> s = makeDefaultVMSet();

        int[] sum = ManagedElementSets.sum(s, cpuConsumption, cpuDemand, memoryConsumption, memoryDemand);
        Assert.assertEquals(Arrays.toString(sum), Arrays.toString(new int[]{9, 19, 16, 26}));
    }

    public void testMin() {
        ManagedElementSet<VirtualMachine> s = makeDefaultVMSet();
        VirtualMachine minCpuCons = ManagedElementSets.min(s, cpuConsumption);
        VirtualMachine minCpuDemand = ManagedElementSets.min(s, cpuDemand);
        VirtualMachine minMemCons = ManagedElementSets.min(s, memoryConsumption);
        VirtualMachine minMemDemand = ManagedElementSets.min(s, memoryDemand);
        Assert.assertEquals(minCpuCons.getCPUConsumption(), 1);
        Assert.assertEquals(minCpuDemand.getCPUDemand(), 1);
        Assert.assertEquals(minMemCons.getMemoryConsumption(), 2);
        Assert.assertEquals(minMemDemand.getMemoryDemand(), 3);

        s = new SimpleManagedElementSet<VirtualMachine>();
        Assert.assertNull(ManagedElementSets.min(s, cpuConsumption));
    }

    public void testMax() {
        ManagedElementSet<VirtualMachine> s = makeDefaultVMSet();
        VirtualMachine maxCpuCons = ManagedElementSets.max(s, cpuConsumption);
        VirtualMachine maxCpuDemand = ManagedElementSets.max(s, cpuDemand);
        VirtualMachine maxMemCons = ManagedElementSets.max(s, memoryConsumption);
        VirtualMachine maxMemDemand = ManagedElementSets.max(s, memoryDemand);
        Assert.assertEquals(maxCpuCons.getCPUConsumption(), 3);
        Assert.assertEquals(maxCpuDemand.getCPUDemand(), 7);
        Assert.assertEquals(maxMemCons.getMemoryConsumption(), 5);
        Assert.assertEquals(maxMemDemand.getMemoryDemand(), 8);

        s = new SimpleManagedElementSet<VirtualMachine>();
        Assert.assertNull(ManagedElementSets.max(s, cpuConsumption));
    }
}
