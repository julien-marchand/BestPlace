/*
 * Copyright (c) 2009 Ecole des Mines de Nantes.
 * 
 *     This file is part of Entropy.
 * 
 *     Entropy is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     Entropy is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 * 
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
*/

package entropy.configuration;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for Configuration.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestConfiguration {

    /**
     * Make a default configuration for test purpose.
     * VM0 to VM9 are affected to node N0 to N9. Node N10 to N14 are unused
     * VirtualMachine VM10 to VM14 are unaffected
     *
     * @return the configuration
     */
    private static DefaultConfiguration makeDefaultConfiguration() {
        DefaultConfiguration c = new DefaultConfiguration();
        for (int i = 0; i < 10; i++) {
            DefaultVirtualMachine vm = new DefaultVirtualMachine("VM" + i, 1, 1024, 2);
            DefaultNode n = new DefaultNode("N" + i, 2, 100, 4096);
            c.addOnline(n);
            c.setRunOn(vm, n);
        }
        for (int i = 0; i < 5; i++) {
            c.addOnline(new DefaultNode("N1" + i, 1, 100, 4096));
        }

        for (int i = 0; i < 5; i++) {
            c.addOffline(new DefaultNode("-N1" + i, 1, 100, 4096));
        }

        for (int i = 10; i < 15; i++) {
            c.addWaiting(new DefaultVirtualMachine("VM" + i, 1, 0, 1024));
        }
        for (int i = 15; i < 20; i++) {
            c.setSleepOn(new DefaultVirtualMachine("VM" + i, 1, 0, 1024), c.getOnlines().get("N" + (i - 10)));
        }
        return c;
    }

    /**
     * Test getNodes().
     */
    public void testGetOnlines() {
        Configuration c = new DefaultConfiguration();
        Assert.assertEquals(c.getOnlines().size(), 0);
        c = TestConfiguration.makeDefaultConfiguration();
        Assert.assertEquals(c.getOnlines().size(), 15);
    }

    /**
     * Test getRunnings().
     */
    public void testGetRunnings() {
        Configuration c = TestConfiguration.makeDefaultConfiguration();
        Assert.assertEquals(c.getRunnings().size(), 10);
        for (int i = 0; i < 10; i++) {
            Node n = c.getOnlines().get("N" + i);
            VirtualMachine vm = c.getRunnings().get("VM" + i);

            Assert.assertEquals(c.getRunnings(n).size(), 1);
            Assert.assertTrue(c.getRunnings(n).contains(vm));
        }
    }

    /**
     * Test getSleepings().
     */
    public void testGetSleepings() {
        DefaultConfiguration c = TestConfiguration.makeDefaultConfiguration();
        Assert.assertEquals(c.getSleepings().size(), 5);
        Assert.assertEquals(c.getSleepings(c.getOnlines().get("N5")).size(), 1);
    }

    /**
     * Tets getWaitings().
     */
    public void testGetWaitings() {
        DefaultConfiguration c = TestConfiguration.makeDefaultConfiguration();
        Assert.assertEquals(c.getWaitings().size(), 5);
    }


    /**
     * Test equals() when the configurations are the same.
     */
    public void testEquals() {
        DefaultConfiguration c = TestConfiguration.makeDefaultConfiguration();
        DefaultConfiguration c2 = TestConfiguration.makeDefaultConfiguration();
        Assert.assertEquals(c2, c);
    }

    /**
     * Test equals() when the configuration are not the same, for several reasons.
     */
    public void testNotEquals() {
        DefaultConfiguration c = TestConfiguration.makeDefaultConfiguration();
        DefaultConfiguration c2 = TestConfiguration.makeDefaultConfiguration();

        //c2 has an additional VM
        c2.addWaiting(new DefaultVirtualMachine("new", 1, 2, 3));
        Assert.assertNotSame(c2, c);

        //Affectations are not the same
        c2 = TestConfiguration.makeDefaultConfiguration();
        c2.setRunOn(c2.getRunnings().get(0), c2.getOnlines().get(0));
        Assert.assertNotSame(c2, c);

        //c2 has not the same nodes
        c2 = TestConfiguration.makeDefaultConfiguration();
        c2.addOffline(new DefaultNode("NX", 1, 2, 3));
        Assert.assertNotSame(c2, c);
    }

    /**
     * Test the copy constructor.
     */
    public void testCopyConstructor() {
        Configuration c = TestConfiguration.makeDefaultConfiguration();
        Configuration copy = new DefaultConfiguration(c);
        //Basic check
        Assert.assertEquals((Object) copy.getRunnings(), (Object) c.getRunnings());
        Assert.assertEquals((Object) copy.getWaitings(), (Object) c.getWaitings());
        Assert.assertEquals((Object) copy.getSleepings(), (Object) c.getSleepings());

        Assert.assertEquals((Object) copy.getOnlines(), (Object) c.getOnlines());
        Assert.assertEquals((Object) copy.getOfflines(), (Object) c.getOfflines());

        for (Node n : c.getOnlines()) {
            Assert.assertEquals((Object) c.getRunnings(n), (Object) copy.getRunnings(n));
            Assert.assertEquals((Object) c.getSleepings(n), (Object) copy.getSleepings(n));
        }

        for (VirtualMachine vm : c.getSleepings()) {
            Assert.assertEquals(c.getSleepingLocation(vm), copy.getSleepingLocation(vm));
            Assert.assertEquals(c.getSleepingLocation(vm), copy.getSleepingLocation(vm));
        }

        for (VirtualMachine vm : c.getRunnings()) {
            Assert.assertEquals(c.getRunningLocation(vm), copy.getRunningLocation(vm));
            Assert.assertEquals(c.getRunningLocation(vm), copy.getRunningLocation(vm));
        }

        //Check
        //TODO: Make modification in the source and check cloning
        DefaultNode n = new DefaultNode("NY", 1, 2, 3);
        copy.addOnline(n);
        Assert.assertFalse(c.getOnlines().contains(n));

        c.addOnline(n);
        VirtualMachine vm = c.getRunnings().get("VM1");
        c.setRunOn(vm, n);

        Assert.assertNotSame(copy.getRunningLocation(vm), c.getRunningLocation(vm));
    }

    /**
     * Test addOnline in several conditions.
     */
    public void testAddOnline() {
        Configuration c = TestConfiguration.makeDefaultConfiguration();
        Node n = new DefaultNode("NA", 1, 2, 3);
        c.addOnline(n);
        Assert.assertEquals(c.getOnlines().size(), 16);
        Assert.assertTrue(c.getOnlines().contains(n));

        //A node that is offline.
        n = new DefaultNode("-N0", 1, 2, 3);
        c.addOnline(n);
        Assert.assertEquals(c.getOnlines().size(), 17);
        Assert.assertTrue(c.getOnlines().contains(n));
        Assert.assertFalse(c.getOfflines().contains(n));
    }

    /**
     * Test addOffline in several conditions.
     */
    public void testAddOffline() {
        DefaultConfiguration c = TestConfiguration.makeDefaultConfiguration();
        DefaultNode n = new DefaultNode("-N30", 1, 2, 3);
        Assert.assertEquals(c.getOfflines().size(), 5);
        c.addOffline(n);
        Assert.assertEquals(c.getOfflines().size(), 6);
        Assert.assertTrue(c.getOfflines().contains(n));

        //A node that is online.
        n = new DefaultNode("N12", 1, 2, 3);
        Assert.assertTrue(c.addOffline(n));
        Assert.assertFalse(c.getOnlines().contains(n));
        Assert.assertTrue(c.getOfflines().contains(n));

        //A node that contains virtual machines. So should not be allowed
        n = new DefaultNode("N3", 1, 2, 3);
        Assert.assertFalse(c.addOffline(n));
        Assert.assertFalse(c.getOfflines().contains(n));
    }

    /**
     * Test getAffected().
     */
    public void testGetPosition() {
        Configuration c = TestConfiguration.makeDefaultConfiguration();
        for (int i = 0; i < 10; i++) {
            Node n = c.getOnlines().get("N" + i);
            VirtualMachine vm = c.getRunnings().get("VM" + i);
            Assert.assertEquals(c.getRunningLocation(vm), n);
        }
    }


    /**
     * Dummy test for toString, to avoid NullPointerException.
     */
    public void testToString() {
        Assert.assertNotNull(TestConfiguration.makeDefaultConfiguration().toString());
    }

    /**
     * Tests for setRunOn() in several conditions.
     */
    public void testSetRunOn() {
        Configuration c = makeDefaultConfiguration();
        VirtualMachine vm = new DefaultVirtualMachine("toto", 1, 2, 3);
        Node n = c.getOnlines().get(0);
        c.setRunOn(vm, n);
        Assert.assertTrue(c.getRunnings().contains(vm));
        Assert.assertTrue(c.getRunnings(n).contains(vm));

        //Test with a VM that was waiting
        vm = c.getWaitings().get("VM10");
        c.setRunOn(vm, n);
        Assert.assertFalse(c.getWaitings().contains(vm));
        Assert.assertTrue(c.getRunnings().contains(vm));
        Assert.assertTrue(c.getRunnings(n).contains(vm));

        //Test with a VM that was  sleeping
        vm = c.getSleepings().get("VM15");
        Node oldNode = c.getSleepingLocation(vm);
        c.setRunOn(vm, n);
        Assert.assertFalse(c.getSleepings().contains(vm));
        Assert.assertFalse(c.getSleepings(oldNode).contains(vm));
        Assert.assertTrue(c.getRunnings(n).contains(vm));

        //Test on a offline node
        vm = new DefaultVirtualMachine("op", 1, 2, 3);
        n = c.getOfflines().get(0);
        c.setRunOn(vm, n);
        Assert.assertFalse(c.getRunnings().contains(vm));
        Assert.assertFalse(c.getRunnings(n).contains(vm));
        Assert.assertNotSame(c.getRunningLocation(vm), n);

        //Relocation of a VM
        c.setRunOn(vm, c.getOnlines().get(c.getOnlines().size() - 1));
        Assert.assertEquals(c.getRunningLocation(vm), c.getOnlines().get(c.getOnlines().size() - 1));
        Assert.assertFalse(c.getRunnings(c.getOnlines().get(0)).contains(vm));
        Assert.assertTrue(c.getRunnings(c.getOnlines().get(c.getOnlines().size() - 1)).contains(vm));
    }

    /**
     * Tests for setRunOn() in several conditions.
     */
    public void testSetSleepOn() {
        Configuration c = makeDefaultConfiguration();
        VirtualMachine vm = new DefaultVirtualMachine("toto", 1, 2, 3);
        Node n = c.getOnlines().get(0);
        c.setSleepOn(vm, n);
        Assert.assertTrue(c.getSleepings().contains(vm));
        Assert.assertTrue(c.getSleepings(n).contains(vm));
        Assert.assertEquals(c.getSleepingLocation(vm), n);

        //Test with a VM that was waiting
        vm = c.getWaitings().get("VM10");
        c.setSleepOn(vm, n);
        Assert.assertFalse(c.getWaitings().contains(vm));
        Assert.assertTrue(c.getSleepings().contains(vm));
        Assert.assertTrue(c.getSleepings(n).contains(vm));
        Assert.assertEquals(c.getSleepingLocation(vm), n);

        //Test with a VM that was  running
        vm = c.getRunnings().get("VM1");
        Node oldNode = c.getRunningLocation(vm);
        c.setSleepOn(vm, n);
        Assert.assertFalse(c.getRunnings().contains(vm));
        Assert.assertFalse(c.getRunnings(oldNode).contains(vm));
        Assert.assertTrue(c.getSleepings(n).contains(vm));
        Assert.assertEquals(c.getSleepingLocation(vm), n);

        //Test on a offline node
        vm = new DefaultVirtualMachine("op", 1, 2, 3);
        n = c.getOfflines().get(0);
        c.setSleepOn(vm, n);
        Assert.assertFalse(c.getSleepings().contains(vm));
        Assert.assertFalse(c.getSleepings(n).contains(vm));
        Assert.assertNotSame(c.getSleepingLocation(vm), n);

        //Test on a unknown node
    }

    /**
     * Test addWaiting() in several situations.
     */
    public void testAddWaiting() {
        DefaultConfiguration c = makeDefaultConfiguration();
        VirtualMachine vm = new DefaultVirtualMachine("hop", 1, 2, 3);
        c.addWaiting(vm);
        Assert.assertTrue(c.getWaitings().contains(vm));


        //Test with a VM that was  running
        vm = c.getRunnings().get("VM1");
        Node oldNode = c.getRunningLocation(vm);
        c.addWaiting(vm);
        Assert.assertFalse(c.getRunnings().contains(vm));
        Assert.assertFalse(c.getRunnings(oldNode).contains(vm));
        Assert.assertTrue(c.getWaitings().contains(vm));

        //Test with a VM that was  sleeping
        vm = c.getSleepings().get("VM15");
        oldNode = c.getSleepingLocation(vm);
        c.addWaiting(vm);
        Assert.assertFalse(c.getSleepings().contains(vm));
        Assert.assertFalse(c.getSleepings(oldNode).contains(vm));
        Assert.assertTrue(c.getWaitings().contains(vm));

    }

    /**
     * Test getRunningLocation().
     */
    public void testGetRunningLocation() {
        DefaultConfiguration c = makeDefaultConfiguration();
        Assert.assertEquals(c.getRunningLocation(new DefaultVirtualMachine("VM0", 1, 2, 3)), c.getOnlines().get("N0"));
        Assert.assertNull(c.getRunningLocation(new DefaultVirtualMachine("zob", 1, 2, 3)));
    }

    /**
     * Test getSleepingLocation().
     */
    public void testGetSleepingLocation() {
        DefaultConfiguration c = makeDefaultConfiguration();
        Assert.assertEquals(c.getSleepingLocation(new DefaultVirtualMachine("VM15", 1, 2, 3)), c.getOnlines().get("N5"));
        Assert.assertNull(c.getSleepingLocation(new DefaultVirtualMachine("zob", 1, 2, 3)));
    }

    /**
     * Test remove().
     */
    public void testRemove() {
        DefaultConfiguration c = makeDefaultConfiguration();
        DefaultNode n1 = new DefaultNode("n1", 1, 2, 3);
        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("VM1", 1, 2, 3);
        c.addOnline(n1);
        c.setRunOn(vm1, n1);
        c.remove(vm1);
        Assert.assertFalse(c.getRunnings().contains(vm1));
        Assert.assertNull(c.getRunningLocation(vm1));
    }
}
