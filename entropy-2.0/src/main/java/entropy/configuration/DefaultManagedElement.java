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

package entropy.configuration;

import java.util.HashMap;
import java.util.Iterator;

/**
 * A element managed by Entropy. Each element must have a unique name per
 * specialization used as an identifier.
 *
 * @author Fabien Hermenier
 */
public abstract class DefaultManagedElement implements ManagedElement {

    /**
     * The identifier of the element.
     */
    private String id;

    /**
     * Map of values.
     */
    private HashMap<String, Comparable<?>> values;

    /**
     * Instanciate a new ManagedElement.
     *
     * @param name The identifier. Unique for each specialization of the class
     */
    public DefaultManagedElement(final String name) {
        this.id = name;
        this.values = new HashMap<String, Comparable<?>>();
    }

    /**
     * Return the name of the element.
     *
     * @return Its name
     */
    @Override
	public final String getName() {
        return this.id;
    }

    /**
     * Update the current value of an information.
     *
     * @param key   the identifier of the information
     * @param value the new value
     */
    public final void updateValue(final String key, final Comparable<?> value) {
        this.values.put(key, value);
    }

    /**
     * Return the current value of an information.
     *
     * @param key The identifier of the information
     * @return the current value. May be null
     */
    public final Comparable<?> getValue(final String key) {
        return this.values.get(key);
    }


    /**
     * Indicate if current element is equivalent to another. This comparison is
     * made on several values. If no resources are specified, the comparison is
     * made on all the resources stored into the current element
     *
     * @param e       The element to compare with
     * @param rcNames The list of resources used to make the equivalences
     * @return true if the both element have all informations equals
     */
    public final boolean isEquivalent(final DefaultManagedElement e, final String... rcNames) {
        // The list of rcs used for the comparison
        String[] rcUsed;
        if (rcNames.length == 0) {
            rcUsed = this.values.keySet().toArray(new String[1]);
        } else {
            rcUsed = rcNames;
        }
        for (final String rc : rcUsed) {
            if (e.getValue(rc) == null || getValue(rc) == null || !getValue(rc).equals(e.getValue(rc))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the hashcode of the identifier.
     *
     * @return this.getName().hashCode();
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Textual representation of the element.
     *
     * @return a String
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(getName());
        buf.append("(");
        String key;
        for (Iterator<String> ite = this.values.keySet().iterator(); ite.hasNext();) {
            key = ite.next();
            if (this.values.get(key) != null) {
                buf.append(key);
                buf.append("=");
                buf.append(this.values.get(key).toString());
            }
            if (ite.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append(")");
        return buf.toString();
    }
}
