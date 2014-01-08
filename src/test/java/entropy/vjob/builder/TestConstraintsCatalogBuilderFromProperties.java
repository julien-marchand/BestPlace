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

import entropy.PropertiesHelper;
import entropy.TestHelper;

/**
 * Unit tests for ConstraintsCatalogBuilderFromProperties.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestConstraintsCatalogBuilderFromProperties {

    private static final String RESOURCES_ROOT = "src/test/resources/entropy/vjob/builder/TestConstraintsCatalogBuilderFromProperties.";

    public void testWithNoConstraints() {
        PropertiesHelper props = TestHelper.readEntropyProperties(RESOURCES_ROOT + "noConstraints.txt");
        ConstraintsCatalogBuilder builder = new ConstraintsCatalogBuilderFromProperties(props);
        try {
            ConstraintsCatalog c = builder.build();
            Assert.assertEquals(c.getAvailableConstraints().size(), 0);
        } catch (ConstraintsCalalogBuilderException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    public void testWithAvailableConstraints() {
        PropertiesHelper props = TestHelper.readEntropyProperties(RESOURCES_ROOT + "fine.txt");
        ConstraintsCatalogBuilder builder = new ConstraintsCatalogBuilderFromProperties(props);
        try {
            ConstraintsCatalog c = builder.build();
            Assert.assertEquals(c.getAvailableConstraints().size(), 1);
            Assert.assertTrue(c.getAvailableConstraints().contains("mock"));
        } catch (ConstraintsCalalogBuilderException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    @Test(expectedExceptions = {ConstraintsCalalogBuilderException.class})
    public void testWithBadFQCN() throws ConstraintsCalalogBuilderException {
        PropertiesHelper props = TestHelper.readEntropyProperties(RESOURCES_ROOT + "badFQCN.txt");
        ConstraintsCatalogBuilder builder = new ConstraintsCatalogBuilderFromProperties(props);
        builder.build();
    }

    @Test(expectedExceptions = {ConstraintsCalalogBuilderException.class})
    public void testWithMissingLocation() throws ConstraintsCalalogBuilderException {
        PropertiesHelper props = TestHelper.readEntropyProperties(RESOURCES_ROOT + "noAvailable.txt");
        ConstraintsCatalogBuilder builder = new ConstraintsCatalogBuilderFromProperties(props);
        builder.build();
    }

    public void testWithDefaultProperties() {
        PropertiesHelper props = TestHelper.readDefaultEntropyProperties();
        ConstraintsCatalogBuilder builder = new ConstraintsCatalogBuilderFromProperties(props);
        try {
            ConstraintsCatalog c = builder.build();
            Assert.assertEquals(c.getAvailableConstraints().size(), 7);
        } catch (ConstraintsCalalogBuilderException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

}
