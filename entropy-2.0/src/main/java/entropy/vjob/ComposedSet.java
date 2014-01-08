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
 * An abstract set that is a composition of two sets.
 *
 * @author Fabien Hermenier
 */
public abstract class ComposedSet<T extends ManagedElement> implements VJobSet<T> {

    private String label;

    /**
     * The first set.
     */
    private VJobSet<T> left;

    /**
     * The second set.
     */
    private VJobSet<T> right;

    private static final String LPARA = "(";

    private static final String RPARA = ")";

    /**
     * Make a new composition.
     *
     * @param h the first set
     * @param t the second set
     */
    public ComposedSet(VJobSet<T> h, VJobSet<T> t) {
        this.left = h;
        this.right = t;
    }

    /**
     * Make a new labelled composition.
     *
     * @param id the label of the composite
     * @param h  the first set
     * @param t  the second set
     */
    public ComposedSet(String id, VJobSet<T> h, VJobSet<T> t) {
        this(h, t);
        label = id;
    }

    /**
     * Get the first set.
     *
     * @return the first set
     */
    public VJobSet<T> leftOperand() {
        return this.left;
    }

    /**
     * Get the second set.
     *
     * @return the second set
     */
    public VJobSet<T> rightOperand() {
        return this.right;
    }

    /**
     * Textual representation of the set.
     *
     * @return the description of the first set, then the operation, then the second set
     */
    @Override
	public String toString() {

        /**
         * We use parenthesis when the left or the right operand is a ComposedSet, with a different operator.
         */
        boolean usePara = false;

        StringBuilder b = new StringBuilder();

        if (leftOperand() instanceof ComposedSet && !((ComposedSet) leftOperand()).operator().equals(operator())) {
            usePara = true;
        }

        if (usePara) {
            b.append(LPARA);
        }
        b.append(leftOperand().pretty());
        if (usePara) {
            b.append(RPARA);
        }

        b.append(operator());

        usePara = false;
        if (rightOperand() instanceof ComposedSet && !((ComposedSet) rightOperand()).operator().equals(operator())) {
            usePara = true;
        }

        if (usePara) {
            b.append(LPARA);
        }
        b.append(rightOperand().pretty());
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
        return definition();
    }


    @Override
    public String definition() {
        return toString();
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
    public ManagedElementSet getElements() {
        return flatten();
    }
}
