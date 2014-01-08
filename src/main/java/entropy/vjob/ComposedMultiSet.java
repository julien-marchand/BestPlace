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

import java.util.List;

import entropy.configuration.DefaultManagedElement;
import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.ManagedElementSet;

/**
 * An abstract multiset, composed of two multiset linked through an operator.
 *
 * @author Fabien Hermenier
 */
public abstract class ComposedMultiSet<T extends DefaultManagedElement> implements VJobMultiSet<T> {

    /**
     * The label of the set.
     */
    private String label;

    /**
     * The first set.
     */
    private VJobMultiSet<T> first;

    /**
     * The second set.
     */
    private VJobMultiSet<T> second;

    private static final String LPARA = "(";

    private static final String RPARA = ")";

    /**
     * Make a new composition.
     *
     * @param h the first set
     * @param t the second set
     */
    public ComposedMultiSet(VJobMultiSet<T> h, VJobMultiSet<T> t) {
        this.first = h;
        this.second = t;
    }

    /**
     * Make a new labeled composition.
     *
     * @param id the variable name of the resulting multiset
     * @param h  the first multi set
     * @param t  the second multi set
     */
    public ComposedMultiSet(String id, VJobMultiSet<T> h, VJobMultiSet<T> t) {
        this(h, t);
        label = id;
    }

    /**
     * Get the first set.
     *
     * @return the first set
     */
    public VJobMultiSet<T> first() {
        return this.first;
    }

    /**
     * Get the second set.
     *
     * @return the second set
     */
    public VJobMultiSet<T> second() {
        return this.second;
    }

    /**
     * Textual representation of the set.
     *
     * @return the description of the first set, then the operation, then the second set
     */
    @Override
	public String toString() {

        boolean usePara = false;

        StringBuilder b = new StringBuilder();

        if (first() instanceof ComposedMultiSet && !((ComposedMultiSet) first()).operator().equals(operator())) {
            usePara = true;
        }

        if (usePara) {
            b.append(LPARA);
        }
        b.append(first().pretty());
        if (usePara) {
            b.append(RPARA);
        }

        b.append(operator());

        usePara = false;
        if (second() instanceof ComposedMultiSet && !((ComposedMultiSet) second()).operator().equals(operator())) {
            usePara = true;
        }

        if (usePara) {
            b.append(LPARA);
        }
        b.append(second().pretty());
        if (usePara) {
            b.append(RPARA);
        }

        return b.toString();
    }

    @Override
    public String pretty() {
        if (label != null) {
            return label;
        }
        return toString();
    }

    @Override
    public ManagedElementSet<T> getElements() {
        ManagedElementSet<T> all = new DefaultManagedElementSet<T>();
        List<ExplodedSet<T>> l = expand();
        for (ExplodedSet<T> s : l) {
            all.addAll(s);
        }
        return all;
    }

    /**
     * The operation between the first and the second set.
     *
     * @return the textual representation of the operator
     */
    public abstract String operator();

    @Override
    public void setLabel(String id) {
        label = id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String definition() {
        return toString();
    }
}
