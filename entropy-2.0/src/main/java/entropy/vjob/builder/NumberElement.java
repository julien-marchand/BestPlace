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

package entropy.vjob.builder;

import entropy.configuration.ManagedElementSet;
import entropy.vjob.VJobElement;

/**
 * A non-solution to handle in less than 10 minutes integers in Plasma.
 * TODO: make a non-shamy solution
 *
 * @author Fabien Hermenier
 */
public class NumberElement implements VJobElement {

    private int nb;

    private String label;

    public NumberElement(String label, int x) {
        this.label = label;
        this.nb = x;
    }

    public NumberElement(int x) {
        this.nb = x;
    }

    public int getValue() {
        return this.nb;
    }

    @Override
    public void setLabel(String id) {
        this.label = id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String pretty() {
        if (label != null) {
            return label;
        }
        return Integer.toString(this.nb);
    }

    @Override
    public String definition() {
        return Integer.toString(this.nb);
    }

    @Override
    public ManagedElementSet getElements() {
        throw new UnsupportedOperationException();
    }
}
