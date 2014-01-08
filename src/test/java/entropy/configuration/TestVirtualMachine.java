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

package entropy.configuration;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for VirtualMachine.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestVirtualMachine {

    /**
     * Test get/set memoryTotal.
     */
    public void testMemoryTotal() {
        final DefaultVirtualMachine n = new DefaultVirtualMachine("V1", 1, 3, 1024);
        // Test default value
        Assert.assertEquals(n.getMemoryConsumption(), 1024);

        // Test the binding
        n.updateValue(DefaultVirtualMachine.MEMORY, 2048);
        Assert.assertEquals(n.getMemoryConsumption(), 2048);
    }

    /**
     * Unit tests for get/set nbOfCPUs.
     */
    public void testNbOfCPUs() {
        final DefaultVirtualMachine n = new DefaultVirtualMachine("N1", 1, 3, 1024);
        // Test default value
        Assert.assertEquals(n.getNbOfCPUs(), 1);

        // Test the binding
        n.updateValue(DefaultVirtualMachine.CPU_NB, 2);
        Assert.assertEquals(n.getNbOfCPUs(), 2);
    }

    /**
     * Unit tests for get/set cpuConsumption.
     */
    public void testCPUConsumption() {
        final DefaultVirtualMachine n = new DefaultVirtualMachine("N1", 1, 3, 1024);
        // Test default value
        Assert.assertEquals(n.getCPUConsumption(), 3);

        // Test the binding
        n.updateValue(DefaultVirtualMachine.CPU_CONSUMPTION, 2);
        Assert.assertEquals(n.getCPUConsumption(), 2);
    }

    /**
     * Test the getters for resources needs when not known.
     */
    public void testWithNoNeeds() {
        DefaultVirtualMachine n = new DefaultVirtualMachine("N1", 1, 3, 1024);
        Assert.assertEquals(n.getCPUDemand(), 3);
        Assert.assertEquals(n.getMemoryDemand(), 1024);
        Assert.assertEquals(n.getValue(DefaultVirtualMachine.MEMORY_NEED), n.getValue(DefaultVirtualMachine.MEMORY_CONSUMPTION));
        Assert.assertEquals(n.getValue(DefaultVirtualMachine.CPU_NEED), n.getValue(DefaultVirtualMachine.CPU_CONSUMPTION));
    }

    /**
     * Test the setters for resources needs.
     */
    public void testWithNewNeeds() {
        DefaultVirtualMachine n = new DefaultVirtualMachine("N1", 1, 3, 1024);
        n.setCPUNeed(5);
        n.setMemoryNeed(10);
        Assert.assertEquals(n.getCPUDemand(), 5);
        Assert.assertEquals(n.getMemoryDemand(), 10);
    }

    /**
     * Test the copy constructor and check if there is no side-effects.
     */
    public void testCopyConstructor() {
        final DefaultVirtualMachine n = new DefaultVirtualMachine("N1", 1, 3, 1024);
        n.setCPUNeed(15);
        final DefaultVirtualMachine n2 = new DefaultVirtualMachine(n);
        Assert.assertEquals(n2.getMemoryConsumption(), 1024);
        Assert.assertEquals(n2.getMemoryDemand(), 1024);
        Assert.assertEquals(n2.getNbOfCPUs(), 1);
        Assert.assertEquals(n2.getCPUConsumption(), 3);
        Assert.assertEquals(n2.getCPUDemand(), 15);
        n.updateValue(DefaultVirtualMachine.CPU_NB, 2);
        n.updateValue(DefaultVirtualMachine.MEMORY, 2048);
        n.updateValue(DefaultVirtualMachine.CPU_CONSUMPTION, 5);
        Assert.assertEquals(n2.getMemoryConsumption(), 1024);
        Assert.assertEquals(n2.getNbOfCPUs(), 1);
        Assert.assertEquals(n2.getCPUConsumption(), 3);
    }

    /**
     * Test equals() in various conditions.
     */
    public void testEquals() {
        final DefaultVirtualMachine n = new DefaultVirtualMachine("N1", 1, 3, 1024);
        final DefaultVirtualMachine n2 = new DefaultVirtualMachine("N1", 2, 3, 4096);
        Assert.assertTrue(n.equals(n2));

        final DefaultVirtualMachine n3 = new DefaultVirtualMachine("N2", 2, 3, 4096);
        Assert.assertFalse(n.equals(n3));
        Assert.assertFalse(n.equals(new Object()));
    }
}
