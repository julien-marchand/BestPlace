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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.ManagedElement;
import entropy.configuration.ManagedElementSet;

/**
 * A Multi set of managed elements.
 *
 * @author Fabien Hermenier
 */
public class ExplodedMultiSet<T extends ManagedElement> implements VJobMultiSet<T> {

    /**
     * The label of the multi set.
     */
    private String label;

    private int size = 0;
    /**
     * The content.
     */
    private List<VJobSet<T>> content;

    /**
     * Make a new empty multiset.
     */
    public ExplodedMultiSet() {
        content = new ArrayList<VJobSet<T>>();
    }

    /**
     * Make an exploded set wrapping a list of managed elements.
     *
     * @param back the elements to include
     */
    public ExplodedMultiSet(List<ManagedElementSet<T>> back) {
        this();
        for (ManagedElementSet<T> e : back) {
            add(new ExplodedSet<T>(e));
        }
    }

    /**
     * Make a new empty multiset with a label.
     *
     * @param id the label
     */
    public ExplodedMultiSet(String id) {
        this();
        label = id;
    }

    @Override
    public void setLabel(String id) {
        this.label = id;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    /**
     * Add a set of element.
     *
     * @param set the set to add
     * @return true if the set is added
     */
    public final boolean add(VJobSet<T> set) {
        boolean res = content.add(set);
        if (res) {
            size++;
        }
        return res;
    }

    @Override
    public List<ExplodedSet<T>> expand() {
        List<ExplodedSet<T>> l = new LinkedList<ExplodedSet<T>>();
        for (VJobSet<T> set : content) {
            l.add(set.flatten());
        }
        return l;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("{");
        for (Iterator<VJobSet<T>> ite = content.iterator(); ite.hasNext();) {
            VJobSet<T> set = ite.next();
            b.append(set.pretty());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        b.append("}");
        return b.toString();
    }

    @Override
    public String pretty() {
        if (label == null) {
            return toString();
        }
        return label;
    }

    @Override
    public String definition() {
        return toString();
    }

    @Override
    public ManagedElementSet<T> getElements() {
        ManagedElementSet<T> r = new DefaultManagedElementSet<T>();
        for (VJobSet<T> t : content) {
            r.addAll(t.getElements());
        }
        return r;
    }

    /**
     * Get a set at a specified position.
     *
     * @param idx the position of the set to get
     * @return a set
     */
    public VJobSet<T> get(int idx) {
        return content.get(idx);
    }

    @Override
    public int size() {
        return size;
    }
}
