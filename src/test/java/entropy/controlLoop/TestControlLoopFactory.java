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
package entropy.controlLoop;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.PropertiesHelper;
import entropy.PropertiesHelperException;
import entropy.TestHelper;
import entropy.monitoring.MockConfigurationAdapter;
import entropy.monitoring.Monitor;

/**
 * Tests for ControlLoopFactory.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestControlLoopFactory {

    /**
     * The base for the test resources.
     */
    public static final String RESOURCES_DIR = "src/test/resources/entropy/controlLoop/TestControlLoopFactory.";

    public void testCustomizableLoop() {
        try {
            PropertiesHelper props = new PropertiesHelper(RESOURCES_DIR + "customizable.properties");
            ControlLoopFactory f = new ControlLoopFactory(props);
            Monitor m = new Monitor(new MockConfigurationAdapter());
            ControlLoop l = f.makeControlLoop(m);
            Assert.assertTrue(l instanceof CustomizableControlLoop);
            Assert.assertEquals(l.getLogsDir(), "./logs");
            CustomizableControlLoop dyn = (CustomizableControlLoop) l;
            Assert.assertEquals(dyn.getAssignTimeout(), 10);
            Assert.assertEquals(dyn.getPlanTimeout(), 15);
            Assert.assertEquals(dyn.getMasterVJobFile(), "./nodesParts.txt");

        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test detection of an invalid implementation.
     *
     * @throws ControlLoopFactoryException the exception we expect
     */
    @Test(expectedExceptions = {ControlLoopFactoryException.class})
    public void testWithBadImpl() throws ControlLoopFactoryException {
        PropertiesHelper props;
        try {
            props = new PropertiesHelper(RESOURCES_DIR + "badImpl.properties");
            ControlLoopFactory f = new ControlLoopFactory(props);
            f.makeControlLoop(null);
        } catch (IOException e) {
            Assert.fail(e.getMessage(), e);
        } catch (PropertiesHelperException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test with default properties.
     */
    public void testDefaultProperties() {
        try {
            PropertiesHelper props = new PropertiesHelper(TestHelper.DISTRIBUTION_PROPERTIES);
            ControlLoopFactory f = new ControlLoopFactory(props);
            f.makeControlLoop(null);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }
}
