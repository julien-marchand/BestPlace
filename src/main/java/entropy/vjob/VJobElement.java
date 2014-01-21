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

package entropy.vjob;

import entropy.configuration.ManagedElement;
import entropy.configuration.ManagedElementSet;

/**
 * An interface to specify an element composing a VJob.
 * The element may have a label to use when composing other elements.
 *
 * @author Fabien Hermenier
 */
public interface VJobElement<T extends ManagedElement> {

    /**
     * Set the label of the set.
     *
     * @param id the label
     */
    void setLabel(String id);

    /**
     * Get the label of the set.
     *
     * @return the label if it is defined or {@code null}
     */
    String getLabel();

    /**
     * Get the short textual representation of the element.
     *
     * @return a String
     */
    String pretty();

    /**
     * Get the textual definition of the element.
     *
     * @return a String
     */
    String definition();

    /**
     * Get all the elements.
     *
     * @return a list of element that may be empty
     */
    ManagedElementSet<T> getElements();

}
