/*
 * Copyright (c) Fabien Hermenier
 *
 * This file is part of Entropy.
 *
 * Entropy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Entropy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package gipad.configuration;

import java.util.List;

/**
 * Specify a list of managed elements without duplicated elements.
 * The position of the element has to be maintained.
 *
 * @author Fabien Hermenier
 */
public interface ManagedElementList<E> extends List<E> {

    /**
     * Get the element associated with an identifier.
     *
     * @param id the identifier of the element
     * @return the element if exists. {@code null} otherwise
     */
    E get(String id);

    /**
     * Get the element at a specified index.
     *
     * @param idx the position of the element.
     * @return the element if exists. {@code null} otherwise
     */
    @Override
	E get(int idx);

    /**
     * Check whether an element is in the set or not.
     *
     * @param e the element
     * @return {@code true} if the element is in the set. {@code false} otherwise
     */
    boolean containsElement(E e);

    /**
     * Check whether all the elements are in the set or not.
     *
     * @param elems the elements
     * @return {@code true} if all the elements are in the set. {@code false} otherwise
     */

    boolean containsAll(ManagedElementList<E> elems);

    /**
     * Add an element in the set if it is not already in.
     *
     * @param e the element to add
     * @return {@code true} if the element is now in the set. {@code false} otherwise
     */
    @Override
	boolean add(E e);

    /**
     * Inserts the specified element at the specified position in this set.
     * Shifts the element currently at that position (if any) and any subsequent elements
     * to the right (adds one to their indices).
     *
     * @param i index at which the specified element is to be inserted
     * @param e element to be inserted
     */
    @Override
	void add(int i, E e);

    /**
     * Remove an element at a position
     *
     * @param i the position of the element to remove
     * @return the removed element. {@code null} if the element was not in the set
     */
    @Override
	E remove(int i);

    /**
     * Remove an element from the set.
     *
     * @param e the element to remove
     * @return {@code true} if the element is removed. {@code false} otherwise
     */
    boolean removeElement(E e);

    /**
     * Remove all the element of the set.
     */
    @Override
	void clear();

    /**
     * Add all the non-present element into the set.
     *
     * @param elems the elements to add
     * @return {@code true} if at least one element has been added
     */
    boolean addAll(ManagedElementList<E> elems);

    /**
     * Remove all the given element from a set
     *
     * @param elems the elements to remove
     * @return {@code true} if at least one element has been removed
     */
    boolean removeAll(ManagedElementList<E> elems);

    /**
     * Retains only the elements that are contained in the set.
     *
     * @param elems the element to keep
     * @return {@code true} if the set has been altered, {@code false} otherwise
     */
    boolean retainAll(ManagedElementList<E> elems);

    /**
     * Shallow copy of the set. Elements inside the set are not duplicated.
     *
     * @return a copy of the set.
     */
    ManagedElementList<E> clone();

    /**
     * Get the number of elements in the set.
     *
     * @return an integer >= 0
     */
    @Override
	int size();
}
