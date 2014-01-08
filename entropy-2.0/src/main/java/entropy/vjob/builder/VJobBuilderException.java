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

/**
 * A Exception related to an error while building a VJob.
 * @author Fabien Hermenier
 *
 */
public class VJobBuilderException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5502255795746863232L;

	/**
	 * Make an exception with a specific error message.
	 * @param msg the error message.
	 */
	public VJobBuilderException(String msg) {
		super(msg);
	}

    /**
     * Make an exception that preserve the stack trace.
     * @param msg the error message
     * @param t the original exception
     */
    public VJobBuilderException(String msg, Throwable t) {
        super(msg, t);
    }

        /**
     * Make an exception that preserve the stack trace.
     * @param t the original exception
     */
    public VJobBuilderException(Throwable t) {
        super(t);
    }
}
