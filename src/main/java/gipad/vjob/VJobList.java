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

package gipad.vjob;

import entropy.configuration.ManagedElement;

/**
 * An abstract set that is a part of a VJob.
 *
 * @author Fabien Hermenier
 */
public interface VJobList<T extends ManagedElement> extends VJobElement<T>, Iterable<T> {

    /**
     * Expand the content of a set.
     *
     * @return a set of elements.
     */
    ExplodedList<T> flatten();

    /**
     * Number of elements in the set.
     *
     * @return a positive integer.
     */
    int size();

    /**
     * Indicates if an element belong to the set or not.
     *
     * @param o the object to look for
     * @return {@code true} if the object is in the set
     */
    boolean contains(T o);
}
