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
 * An exception to signal an error when building a constraint.
 *
 * @author Fabien Hermenier
 */
public class ConstraintBuilderException extends Exception {

    /**
     * Make a new exception that signal a signature error when
     * trying to build a constraint
     *
     * @param builder the builder associated to the constraint
     */
    public ConstraintBuilderException(PlacementConstraintBuilder builder) {
        super("Unable to build constraint '" + builder.getIdentifier() + "'. Expected signature: " + builder.getSignature());
    }

    /**
     * A new exception with an error message.
     *
     * @param message the error message
     */
    public ConstraintBuilderException(String message) {
        super(message);
    }

    /**
     * A new exception that rethrown an exception.
     *
     * @param message the error message
     * @param t       the exception to rethrow
     */
    public ConstraintBuilderException(String message, Throwable t) {
        super(message, t);
    }
}
