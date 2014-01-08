/*
 * Copyright (c) 2009 Ecole des Mines de Nantes.
 * 
 *     This file is part of Entropy.
 * 
 *     Entropy is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     Entropy is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 * 
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
*/
package entropy.monitoring;

/**
 * An exception related to the monitoring.
 * @author Fabien Hermenier
 *
 */
public class MonitoringException extends Exception {

	/**
	 * Default serialID.
	 */
	private static final long serialVersionUID = -8514592891628156236L;

	/**
	 * Make a new exception.
	 * @param message the error message
	 */
	public MonitoringException(String message) {
		super(message);
	}

    /**
     * Make a new exception that rethrow another exception.
     * @param message the error message
     * @param t the original throwable
     */
    public MonitoringException(String message, Throwable t) {
        super(message, t);
    }
}
