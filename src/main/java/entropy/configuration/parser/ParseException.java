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

package entropy.configuration.parser;

/**
 * Exception related to a error while parsing a Configuration.
 * @author Fabien Hermenier
 *
 */
public class ParseException extends Exception {

	/**
	 * Default Serial UID.
	 */
	private static final long serialVersionUID = 5124766834627096743L;

	/**
	 * Make a new exception with a specific error message.
	 * @param message The error message
	 */
	public ParseException(String message) {
		super(message);
	}

    /**
     * A new exception that preserve the stack trace.
     * @param msg the error message
     * @param t the original exception
     */
    public ParseException(String msg, Throwable t) {
        super(msg, t);
    }

}
