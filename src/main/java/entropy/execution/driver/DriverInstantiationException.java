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

import entropy.execution.ExecutorException;
import entropy.plan.action.Action;

/**
 * An exception to signal an error while trying to select a driver for an action.
 * @author Fabien Hermenier
 *
 */
public class DriverInstantiationException extends ExecutorException {
	
	/**
	 * Default serial UID.
	 */
	private static final long serialVersionUID = 7250467816195176472L;

	/**
	 * Specify that no driver are available to execute an action.
	 * @param a the action to execute
	 */
	public DriverInstantiationException(Action a) {
		super("No available driver for action '" + a + "'");
	}

    /**
     * Specify that a driver can not be used to execute an action.
     * @param a the action to execute
     * @param drv the driver
     * @param t the reason
     */
    public DriverInstantiationException(Action a, Class drv, Throwable t) {
        super("Unable to use driver '" + drv.getSimpleName() + "' to execute action '" + a + "'", t);
    }

}
