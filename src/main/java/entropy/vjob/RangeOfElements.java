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

/**
 * Denotes a set of element selected through a pattern.
 *
 * @author Fabien Hermenier
 */
public class RangeOfElements<T extends ManagedElement> extends ExplodedSet<T> {

    /**
     * The pattern.
     */
    private String range;

    /**
     * Make a new set.
     *
     * @param pattern the pattern used to select the elements.
     * @param label   the label of the set. Optionnal
     */
    public RangeOfElements(String pattern, String label) {
        setLabel(label);
        this.range = pattern;
    }

    /**
     * Make a new set.
     *
     * @param pattern the pattern used to select the elements.
     */
    public RangeOfElements(String pattern) {
        this(pattern, null);
    }

    /**
     * @return the label if defined, the pattern otherwise
     */
    @Override
    public String pretty() {
        String lbl = getLabel();
        if (lbl != null) {
            return lbl;
        }
        return range;
    }

    /**
     * @return the pattern.
     */
    @Override
    public String definition() {
        return range;
    }

    @Override
    public String toString() {
        return definition();
    }
}
