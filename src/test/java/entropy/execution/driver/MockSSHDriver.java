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
 * A Mock for a SSHDriver, for tests purpose.
 * @author Fabien Hermenier
 *
 */
public class MockSSHDriver extends SSHDriver {

	/**
	 * Make a new mock.
	 * @param a the action to execute
	 * @param props the properties to use
	 */
	public MockSSHDriver(Action a, PropertiesHelper props) throws PropertiesHelperException {
		super(a, props);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCommandToExecute() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRemoteHostname() {
		return null;
	}
}
