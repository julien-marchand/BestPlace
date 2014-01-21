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

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for PropertiesHelper.
 * @author Fabien Hermenier
 */
@Test(groups = {"unit" })
public class TestPropertiesHelper {

	/**
	 * Default path for test resources.
	 */
	public static final String RESOURCES_DIR = "src/test/resources/entropy/TestPropertiesHelper.";
	
	/**
	 * Test creation with an unknown file.
	 * @throws IOException shoud be thrown
	 * 
	 */
	@Test(expectedExceptions = {IOException.class })
	public void testWithUnknownFile() throws IOException {
		new PropertiesHelper("dummy");
	}
	
	/**
	 * Test getRequiredProperty() with an existing property.
	 */
	public void testGetRequiredPropertyWithExistantProperty() {
		try {
			PropertiesHelper props = new PropertiesHelper(RESOURCES_DIR + "testGetRequiredProperty.txt");
			Assert.assertEquals(props.getRequiredProperty("oneProp"), "ok");
			Assert.assertEquals(props.getRequiredPropertyAsInt("twoProp"), 2);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Test getRequiredProperty() with a property that has not the good format.
	 * @throws entropy.WrongPropertyTypeException the exception we expect
	 */
	@Test(expectedExceptions = {WrongPropertyTypeException.class })
	public void testGetRequiredPropertyWithIncorrectFormat() throws WrongPropertyTypeException {
		try {
			PropertiesHelper props = new PropertiesHelper(RESOURCES_DIR + "testGetRequiredProperty.txt");
			props.getRequiredPropertyAsInt("oneProp");
		} catch (IOException e) {
			Assert.fail(e.getMessage(), e);
		} catch (MissingRequiredPropertyException e) {
            Assert.fail(e.getMessage(), e);
        }
	}
	
	/**
	 * Test getRequiredProperty() with an unknown property.
	 */
	@Test(expectedExceptions = {MissingRequiredPropertyException.class })
	public void testGetRequiredPropertyWithUnknownProperty() throws MissingRequiredPropertyException {
		try {
			PropertiesHelper props = new PropertiesHelper(RESOURCES_DIR + "testGetRequiredProperty.txt");
			props.getRequiredProperty("unknown");
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	/**
	 * Test getOptionalProperty() for various situations.
	 */
	public void testGetOptionalProperty() {
		PropertiesHelper props = TestHelper.readEntropyProperties(RESOURCES_DIR + "testGetRequiredProperty.txt");
			
		//Property exists, must return the current value
		Assert.assertEquals(props.getOptionalProperty("oneProp", "myDefault"), "ok");			 						
		//Unknown property, must return the default value
        try {
		    Assert.assertEquals(props.getOptionalProperty("unknown", 2), 2);
            Assert.assertEquals(props.getOptionalProperty("unknown", "myDefault"), "myDefault");
        } catch (WrongPropertyTypeException e) {
            Assert.fail(e.getMessage(), e);
        }		
	}
	/**
	 * Test getOptionalProperty() when the current value has not the same type
	 * than the default Value.
	 * @throws WrongPropertyTypeException The exception we expect
	 */
	@Test(expectedExceptions = {WrongPropertyTypeException.class })
	public void testGetOptionalPropertyWithWrongFormat() throws WrongPropertyTypeException {
		PropertiesHelper props = TestHelper.readEntropyProperties(RESOURCES_DIR + "testGetRequiredProperty.txt");
		Assert.assertEquals(props.getOptionalProperty("oneProp", 2), "ok");
	}

	/**
	 * Test isDefined().
	 */
	public void testIsDefined() {
		PropertiesHelper props = TestHelper.readEntropyProperties(RESOURCES_DIR + "testGetRequiredProperty.txt");
		Assert.assertTrue(props.isDefined("oneProp"));
		Assert.assertFalse(props.isDefined("unknown"));
	}
}
