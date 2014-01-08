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

import java.util.Arrays;


/**
 * An exception to signal an error while evaluate the duration of an action.
 *
 * @author Fabien Hermenier
 */
public class DurationEvaluationException extends Exception {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Make a new exception that preserve the stack trace.
     *
     * @param expr the expression where evaluation fail
     * @param t    the original stack trace
     * @param opts the parameters used to evaluate the expression
     */
    public DurationEvaluationException(String expr, Throwable t, Object... opts) {
        super("Unable to evaluation the expression '" + expr + "' with parameters: " + Arrays.toString(opts), t);
    }

}
