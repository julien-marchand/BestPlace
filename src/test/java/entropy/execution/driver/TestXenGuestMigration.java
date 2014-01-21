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

import entropy.TestHelper;
import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.plan.action.Migration;

/**
 * Unit tests for XenGuestMigration.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestXenGuestMigration {

    /**
     * Test the customization with the default properties.
     */
    public void testDefaultProperties() {
        try {
            XenGuestMigration m = new XenGuestMigration(new Migration(new DefaultVirtualMachine("vm1", 0, 0, 0),
                    new DefaultNode("n1", 0, 0, 0),
                    new DefaultNode("n2", 0, 0, 0)), TestHelper.readDefaultEntropyProperties());
            Assert.assertEquals(m.getRelocationPort(), 8002);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
