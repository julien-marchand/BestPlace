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
 * Unit tests for Node.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestNode {

    /**
     * Tests related to MEMORY_TOTAL.
     * Tests on getter and setter
     */
    public void testMemoryTotal() {
        final DefaultNode n = new DefaultNode("N1", 1, 100, 1024);
        // Test default value
        Assert.assertEquals(n.getMemoryCapacity(), 1024);

        // Test the binding
        n.updateValue(DefaultNode.MEMORY_TOTAL, 2048);
        Assert.assertEquals(n.getMemoryCapacity(), 2048);
    }

    /**
     * Tests related to NbOfCPUs.
     * Tests on getter and setter
     */
    public void testNbOfCPUs() {
        final DefaultNode n = new DefaultNode("N1", 1, 100, 1024);
        // Test default value
        Assert.assertEquals(n.getNbOfCPUs(), 1);

        // Test the binding
        n.updateValue(DefaultNode.CPU_NB, 2);
        Assert.assertEquals(n.getNbOfCPUs(), 2);
    }

    /**
     * Tests related to CPU_CAPACITY.
     * Tests on getter and setter
     */
    public void testCPUCapacity() {
        final DefaultNode n = new DefaultNode("N1", 1, 100, 1024);
        // Test default value
        Assert.assertEquals(n.getCPUCapacity(), 100);

        // Test the binding
        n.updateValue(DefaultNode.CPU_CAPACITY, 200);
        Assert.assertEquals(n.getCPUCapacity(), 200);
    }


    /**
     * Tests related to the copy constructor.
     */
    public void testCopyConstructor() {
        final DefaultNode n = new DefaultNode("N1", 1, 100, 1024);
        n.setHypervisorID("myHyp");
        n.setIPAddress("myIP");
        n.setMACAddress("myMAC");
        n.setMigrationDriverID("migDriver");
        n.setShutdownDriverID("myShutdown");
        n.setStartupDriverID("myStartup");
        n.setResumeDriverID("myResume");
        n.setSuspendDriverID("mySuspend");
        n.setRunDriverID("myRun");
        n.setStopDriverID("myStop");

        final DefaultNode n2 = new DefaultNode(n);
        Assert.assertEquals(n2.getMemoryCapacity(), 1024);
        Assert.assertEquals(n2.getNbOfCPUs(), 1);
        n.updateValue(DefaultNode.CPU_NB, 2);
        n.updateValue(DefaultNode.MEMORY_TOTAL, 2048);

        Assert.assertEquals(n2.getMemoryCapacity(), 1024);
        Assert.assertEquals(n2.getNbOfCPUs(), 1);
        Assert.assertEquals(n2.getHypervisorID(), "myHyp");
        Assert.assertEquals(n2.getIPAddress(), "myIP");
        Assert.assertEquals(n2.getMACAddress(), "myMAC");
        Assert.assertEquals(n2.getMigrationDriverID(), "migDriver");
        Assert.assertEquals(n2.getStartupDriverID(), "myStartup");
        Assert.assertEquals(n2.getShutdownDriverID(), "myShutdown");
        Assert.assertEquals(n2.getRunDriverID(), "myRun");
        Assert.assertEquals(n2.getResumeDriverID(), "myResume");
        Assert.assertEquals(n2.getSuspendDriverID(), "mySuspend");
        Assert.assertEquals(n2.getStopDriverID(), "myStop");
    }

    /**
     * Tests related to equals().
     */
    public void testEquals() {
        final DefaultNode n = new DefaultNode("N1", 1, 100, 1024);
        final DefaultNode n2 = new DefaultNode("N1", 2, 100, 4096);
        Assert.assertTrue(n.equals(n2));

        final DefaultNode n3 = new DefaultNode("N2", 2, 100, 4096);
        Assert.assertFalse(n.equals(n3));
        Assert.assertFalse(n.equals(new Object()));
    }
}
