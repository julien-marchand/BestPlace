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
package entropy.execution.driver;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.PropertiesHelper;
import entropy.TestHelper;
import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.plan.action.Stop;

/**
 * Unit tests for SSHStop.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestSSHStop {
    /**
     * The base of the resources path.
     */
    public static final String RESOURCES_BASE = "src/test/resources/entropy/execution/driver/TestSSHStop.";

    /**
     * Test getCommandToExecute().
     */
    public void testGetCommandToExecute() {
        Stop r = new Stop(new DefaultVirtualMachine("myVirtualMachine", 1, 2, 3), new DefaultNode("theSource", 1, 2, 3));
        try {
            PropertiesHelper props = TestHelper.readEntropyProperties(RESOURCES_BASE + "testGets.txt");
            SSHStop s = new SSHStop(r, props);
            Assert.assertEquals(s.getCommandToExecute(), "plz stop myVirtualMachine. Its on fire on theSource");
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test getRemoteHostname.
     */
    public void testGetRemoteHostname() {
        Stop r = new Stop(new DefaultVirtualMachine("myVirtualMachine", 1, 2, 3), new DefaultNode("theSource", 1, 2, 3));
        try {
            PropertiesHelper props = TestHelper.readEntropyProperties(RESOURCES_BASE + "testGets.txt");
            SSHStop s = new SSHStop(r, props);
            Assert.assertEquals(s.getRemoteHostname(), "theSource");
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Driver used by default, so we check if initial
     * parameters are good.
     */
    public void testDefaultProperties() {
        Stop r = new Stop(new DefaultVirtualMachine("vm1", 1, 2, 3), new DefaultNode("n1", 1, 2, 3));
        try {
            PropertiesHelper props = TestHelper.readDefaultEntropyProperties();
            SSHStop s = new SSHStop(r, props);
            Assert.assertEquals(s.getUsername(), "root");
            Assert.assertEquals(s.getIdentityFile(), "config/privateKey");
            Assert.assertEquals(s.getCommandToExecute(), "xm destroy vm1");
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }
}
