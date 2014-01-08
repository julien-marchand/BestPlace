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
package entropy.plan.durationEvaluator;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.PropertiesHelper;
import entropy.PropertiesHelperException;
import entropy.TestHelper;

/**
 * Unit tests for DurationEvaluatorFactory.
 * @author Fabien Hermenier
 *
 */
@Test(groups = {"unit"})
public class TestDurationEvaluatorFactory {

	/**
	 * Root of the test resources.
	 */
	public static final String RESOURCES_BASE = "src/test/resources/entropy/plan/durationEvaluator/TestDurationEvaluatorFactory.";
	
	/**
	 * Test the creation with a valid properties file.
	 */
	public void testReadFromGoodProperties() {
		PropertiesHelper props = TestHelper.readEntropyProperties(RESOURCES_BASE + "goodProperties.properties");
        try {
		    DurationEvaluatorFactory.readFromProperties(props);
        } catch (PropertiesHelperException e) {
            Assert.fail(e.getMessage(), e);
        }
	}
	
	/**
	 * Test the instantiation with an invalid properties file.
	 * @throws entropy.PropertiesHelperException the exception we expect
	 */
	@Test(expectedExceptions = {PropertiesHelperException.class})
	public void testReadFromBadProperties() throws PropertiesHelperException {
		PropertiesHelper props = TestHelper.readEntropyProperties(RESOURCES_BASE + "badProperties.properties");
		DurationEvaluatorFactory.readFromProperties(props);
	}
	
	/**
	 * Test the instantiation with the basic properties of the distribution.
	 */
	public void testReadFromDistributionProperties() {
		PropertiesHelper props = TestHelper.readDefaultEntropyProperties();
        try {
			DurationEvaluatorFactory.readFromProperties(props);
        } catch (PropertiesHelperException e) {
            Assert.fail(e.getMessage(), e);
        }

	}
}
