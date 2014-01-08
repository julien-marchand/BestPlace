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

/**
 * This exception occured when the execution of a action through a driver failed. 
 * @author Fabien Hermenier
 *
 */
public class DriverException extends ExecutorException {

	/**
	 * Default serial ID.
	 */
	private static final long serialVersionUID = 7134615330643299846L;

	/**
	 * The driver concerned by the exception.
	 */
	private Driver driver;
		
	/**
	 * A new exception with a specific error message.
	 * @param drv The concerned driver
	 * @param msg The error message
	 */
	public DriverException(Driver drv, String msg) {
        super("Error while executing " + drv + ": " + msg);
		this.driver = drv;		
	}

    /**
     * A new exception that preserve the stack trace.
     * @param drv the involved driver
     * @param msg the error message
     * @param t the original stack
     */
    public DriverException(Driver drv, String msg, Throwable t) {
        super("Error while executing " + drv + ": " + msg, t);
        this.driver = drv;
    }
	
	/**
	 * Get the driver concerned by the exception.
	 * @return a driver
	 */
	public Driver getDriver() {
		return this.driver;
	}
}
