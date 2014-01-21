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

import java.util.Iterator;

import entropy.configuration.DefaultManagedElement;

/**
 * The difference between two sets.
 *
 * @author Fabien Hermenier
 */
public class SetsDifference<T extends DefaultManagedElement> extends ComposedSet<T> {

    ExplodedSet<T> res;

    /**
     * Make a new composition.
     *
     * @param h the first set
     * @param t the second set
     */
    public SetsDifference(VJobSet<T> h, VJobSet<T> t) {
        this(h.pretty() + " - " + t.pretty(), h, t);
    }

    /**
     * Make a new composition.
     *
     * @param h the first set
     * @param t the second set
     */
    public SetsDifference(String label, VJobSet<T> h, VJobSet<T> t) {
        super(label, h, t);
        res = new ExplodedSet<T>(label, h.flatten());
        res.removeAll(t.flatten());
    }


    /**
     * Textual representation of the difference.
     *
     * @return "-"
     */
    @Override
    public String operator() {
        return " - ";
    }

    /**
     * Expand the content of a set.
     *
     * @return a new set which is the first set without all the elements of the second set.
     */
    @Override
    public ExplodedSet<T> flatten() {
        return res;
    }

    @Override
    public int size() {
        return res.size();
    }

    @Override
    public boolean contains(T o) {
        return res.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return res.iterator();
    }

}
