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

package entropy;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for EntropyBuilder.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestEntropyBuilder {

    /**
     * Location of the test resources.
     */
    private static final String RESOURCES = "src/test/resources/entropy/TestEntropyBuilder.";

    /**
     * Test with all the necessary stuff.
     */
    public void testWithAllNeeds() {
        PropertiesHelper p = TestHelper.readEntropyProperties(RESOURCES + "fine.txt");
        try {
            Entropy e = EntropyBuilder.buildFromProperties(p);
            Assert.assertEquals(e.getSleepDelay(), 10);
            Assert.assertEquals(e.getRegistryPort(), 1099);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }
}
