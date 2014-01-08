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

import entropy.configuration.DefaultNode;
import entropy.plan.action.Startup;

/**
 * Unit tests for WoLStartup.
 *
 * @author fabien
 */
@Test(groups = {"unit"})
public class TestWoLStartup {

    /**
     * Dummy test.
     * We just sent WoL packet to the localhost
     */
    public void testSend() {

        DefaultNode n = new DefaultNode("localhost", 1, 1, 2);
        n.setMACAddress("FF:FF:FF:FF:FF:FF");
        WoLStartup action = new WoLStartup(new Startup(n));
        try {
            action.execute();
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }

    }

    /**
     * Check wether an exception occurs when the node does not
     * have a MAC address.
     *
     * @throws DriverException the exception we execpt
     */
    @Test(expectedExceptions = {DriverException.class})
    public void testOnNodeWithoutMAC() throws DriverException {

        DefaultNode n = new DefaultNode("localhost", 1, 1, 2);
        WoLStartup action = new WoLStartup(new Startup(n));
        action.execute();
    }
}
