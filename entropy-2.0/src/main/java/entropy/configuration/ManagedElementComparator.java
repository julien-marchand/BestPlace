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

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO: Change multicriterion + multi order
 * A generic comparator to compare two managed element.
 * The comparison criteria can be specified in the constructor
 *
 * @param <T> the instance that is a subtype of ManagedElement
 * @author Fabien Hermenier
 */
public class ManagedElementComparator<T extends DefaultManagedElement> implements Serializable, Comparator<T> {

    /**
     * Default serial UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The comparison criteria.
     */
    private List<String> rcNames;

    /**
     * Indicates the ordering type for each criterion.
     * 1 for an ascending, -1 otherwise
     */
    private List<Integer> ascendings;

    /**
     * Create a new comparator.
     *
     * @param asc true to make a ascending comparison
     * @param rc  the comparison criteria
     */
    public ManagedElementComparator(boolean asc, String rc) {
        this.rcNames = new LinkedList<String>();
        this.ascendings = new LinkedList<Integer>();
        this.appendCriteria(asc, rc);
    }

    /**
     * Add a sorting criteria.
     *
     * @param rc  the identifier of comparison criteria
     * @param asc true for an ascending comparison.
     */
    public final void appendCriteria(boolean asc, String rc) {
        this.rcNames.add(rc);
        if (asc) {
            this.ascendings.add(1);
        } else {
            this.ascendings.add(-1);
        }
    }

    /**
     * Compare two managed element.
     * The comparison is made following the list of criterion specified in the constructor. The criterion
     * are compared in the order they were specified and the comparison stop after the first difference between
     * a comparison. If the value of a comparison criteria is null for e1 or e2, this criteria comparison is ignored.
     *
     * @param e1 The first element
     * @param e2 The second element
     * @return a negative, zero or positive integer that indicates respectively that
     *         e1 is before, equals or after e2 for the specific sort declared in the constructor.
     */
    @Override
    public int compare(T e1, T e2) {
        for (int i = 0; i < rcNames.size(); i++) {
            Comparable c1 = e1.getValue(rcNames.get(i));
            Comparable c2 = e2.getValue(rcNames.get(i));
            if (c1 != null && c2 != null) {
                int ret = c1.compareTo(c2);
                //The two values are differents, we return the comparison
                //Otherwise, we compare the two nexts values
                if (ret != 0) {
                    return ascendings.get(i) * ret;
                }
            }
        }
        return 0;
    }
}
