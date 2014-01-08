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

import entropy.configuration.ManagedElement;

/**
 * A set composed with the union of two sets.
 *
 * @author Fabien Hermenier
 */
public class SetsUnion<T extends ManagedElement> extends ComposedSet<T> {

    private ExplodedSet<T> res;

    /**
     * Make a new composition.
     *
     * @param h the first set
     * @param t the second set
     */
    public SetsUnion(VJobSet<T> h, VJobSet<T> t) {
        this(h.pretty() + " + " + t.pretty(), h, t);
    }

    /**
     * Make a new composition.
     *
     * @param h the first set
     * @param t the second set
     */
    public SetsUnion(String label, VJobSet<T> h, VJobSet<T> t) {
        super(label, h, t);
        res = new ExplodedSet<T>(label);
        res.addAll(h.flatten());
        res.addAll(t.flatten());
    }

    /**
     * Textual representation of the union.
     *
     * @return "+"
     */
    @Override
    public String operator() {
        return " + ";
    }

    /**
     * Expand the content of a set.
     *
     * @return a new set which is the union of the first and the second set.
     */
    @Override
    public ExplodedSet<T> flatten() {
/*        ExplodedSet<T> ex = new ExplodedSet<T>();
        ex.addAll(leftOperand().flatten());
        ex.addAll(rightOperand().flatten());
        return ex;*/
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
