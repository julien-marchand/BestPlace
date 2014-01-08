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

/**
 * Exception related to the management of Entropy.
 * @author Fabien Hermenier
 */
public class EntropyException extends Exception {

    /**
     * A new exception with an error message.
     * @param s the error message
     */
    public EntropyException(String s) {
        super(s);
    }

    /**
     * An exception with an error message and a stack trace.
     * @param s the error message
     * @param t the exception to re-throw
     */
    public EntropyException(String s, Throwable t) {
        super(s, t);
    }
}
