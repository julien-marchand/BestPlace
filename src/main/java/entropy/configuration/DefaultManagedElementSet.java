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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

/**
 * A set of ManagedElement.
 * <p/>
 * TODO: Check if there is no more add* methods to override to avoid multiple addition of a same element.
 * FIXME: Behavior fix optional fields
 *
 * @param <E> The subclass of ManagedElements contained in the set
 * @author Fabien Hermenier
 */
public class DefaultManagedElementSet<E extends ManagedElement> extends ArrayList<E> implements ManagedElementSet<E> {

    private HashMap<String, E> map;

    /**
     * Default serial UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new empty set.
     */
    public DefaultManagedElementSet() {
        super();
        map = new HashMap<String, E>();
    }

    /**
     * Make a set with a first element in it.
     *
     * @param elem the element to add
     */
    public DefaultManagedElementSet(final E elem) {
        this();
        this.add(elem);
    }

    /**
     * Copy constructor.
     *
     * @param ref the reference
     */
    public DefaultManagedElementSet(final ManagedElementSet<E> ref) {
        this();
        this.addAll(ref);
        //super(ref);
    }

    /**
     * Retrieve a element from its identifier.
     *
     * @param id The identifier of the element
     * @return The element or null if it is not in the set
     */
    @Override
	public E get(String id) {
        return map.get(id);
    }

    /**
     * Merge this current list with a other list.
     *
     * @param elements the other list
     * @return true is the current list has changed, false otherwise
     */
    @Override
	public boolean addAll(ManagedElementSet<E> elements) {
        boolean changed = false;
        for (E e : elements) {
            if (this.add(e)) {
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Add a new element.
     * The element is added iff there is no already an element with the same name in the list.
     *
     * @param e The element to add
     * @return true if the element is added.
     */
    @Override
    public final boolean add(E e) {
        if (map.containsKey(e.getName())) {
            return false;
        }
        map.put(e.getName(), e);
        return super.add(e);
    }

    /**
     * Test the equality between two object.
     *
     * @param o The object to compare with
     * @return true if o is an instance of ManagedElementSet and if all the element of each set
     *         is contained into the other.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o instanceof ManagedElementSet) {
            //Cannot use super.equals() has we do not consider ordering
            ManagedElementSet<E> ref = (ManagedElementSet<E>) o;
            if (ref.size() != this.size()) {
                return false;
            }

            for (E e : ref) {
                if (!contains(e)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (E obj : this) {
            hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
        }
        return hashCode;
    }


    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("{");
        for (ListIterator<E> li = this.listIterator(); li.hasNext();) {
            buffer.append(li.next().getName());
            if (li.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append("}");
        return buffer.toString();
    }

    /**
     * Check if an element is in a set or not.
     *
     * @param e the element to look for
     * @return {@code true} if the element is in the set
     */
    @Override
	public boolean contains(E e) {
        return map.containsKey(e.getName());
    }

    /**
     * Remove an element for the set.
     *
     * @param o the element to remove
     * @return {@code true} if the element is removed
     */
    @Override
	public boolean remove(E o) {
        if (super.remove(o)) {
            map.remove(o.getName());
            return true;
        }
        return false;
    }

    @Override
    public E remove(int i) {
        E e = super.remove(i);
        if (e != null) {
            map.remove(e.getName());
        }
        return e;
    }

    @Override
    public void clear() {
        super.clear();
        map.clear();
    }

    @Override
    public boolean containsAll(ManagedElementSet<E> elems) {
        for (E e : elems) {
            if (!this.contains(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeAll(ManagedElementSet<E> elems) {
        boolean changed = false;
        for (E e : elems) {
            changed |= remove(e);
        }
        return changed;
    }

    @Override
    public boolean retainAll(ManagedElementSet<E> elems) {
        int i = 0;
        boolean ret = false;
        while (i < size()) {
            E o = get(i);
            if (!elems.contains(o)) {
                remove(o);
                ret = true;
            } else {
                i++;
            }
        }
        return ret;
    }

    @Override
    public ManagedElementSet<E> clone() {
        return new DefaultManagedElementSet<E>(this);
    }
}
