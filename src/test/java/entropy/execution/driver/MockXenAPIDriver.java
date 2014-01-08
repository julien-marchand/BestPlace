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

import entropy.PropertiesHelper;
import entropy.PropertiesHelperException;
import entropy.plan.action.Action;

/**
 * A Mock to test XenAPIDriver.
 * @author Fabien Hermenier
 *
 */
public class MockXenAPIDriver extends XenAPIDriver {

	/**
	 * Make a new mock.
	 * @param a the action to execute
	 * @param properties the properties to configure the driver
     * @throws entropy.PropertiesHelperException if an error occurred while configuring the driver
	 */
	public MockXenAPIDriver(Action a, PropertiesHelper properties) throws PropertiesHelperException {
		super(a, properties);	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws DriverException {
		
	}

}
