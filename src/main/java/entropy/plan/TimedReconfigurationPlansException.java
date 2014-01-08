/*
 * Copyright (c) Fabien Hermenier
 *
 *        This file is part of Entropy.
 *
 *        Entropy is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU Lesser General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        Entropy is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU Lesser General Public License for more details.
 *
 *        You should have received a copy of the GNU Lesser General Public License
 *        along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.plan;

/**
 * Exception related to utility class TimedReconfigurationPlans.
 *
 * @author Fabien Hermenier
 */
public class TimedReconfigurationPlansException extends Exception {

    /**
     * New Exception with only an error message
     *
     * @param s the error message
     */
    public TimedReconfigurationPlansException(String s) {
        super(s);
    }

    /**
     * New exception with an error message, re-throwing another exception
     *
     * @param s the error message
     * @param t the exception to re-throw
     */
    public TimedReconfigurationPlansException(String s, Throwable t) {
        super(s, t);
    }

}
