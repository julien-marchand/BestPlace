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

package entropy.configuration;

import gnu.trove.TIntArrayList;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * A compator for nodes. May consider several criterion.
 *
 * @author Fabien Hermenier
 */
public class NodeComparator implements Serializable, Comparator<Node> {

    /**
     * The comparison criteria.
     */
    private List<ResourcePicker.NodeRc> rcs;

    /**
     * Indicates the ordering type for each criterion.
     * 1 for an ascending, -1 otherwise
     */
    private TIntArrayList ascendings;

    /**
     * Create a new comparator.
     *
     * @param asc true to make a ascending comparison
     * @param rc  the comparison criteria
     */
    public NodeComparator(boolean asc, ResourcePicker.NodeRc rc) {
        this.rcs = new LinkedList<ResourcePicker.NodeRc>();
        this.ascendings = new TIntArrayList();
        this.appendCriteria(asc, rc);
    }

    /**
     * Add a sorting criteria.
     *
     * @param rc  the identifier of comparison criteria
     * @param asc true for an ascending comparison.
     */
    public final void appendCriteria(boolean asc, ResourcePicker.NodeRc rc) {
        this.rcs.add(rc);
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
     * @param n1 The first element
     * @param n2 The second element
     * @return a negative, zero or positive integer that indicates respectively that
     *         e1 is before, equals or after e2 for the specific sort declared in the constructor.
     */
    @Override
    public int compare(Node n1, Node n2) {
        for (int i = 0; i < rcs.size(); i++) {
            int v1 = ResourcePicker.get(n1, rcs.get(i));
            int v2 = ResourcePicker.get(n2, rcs.get(i));
            int res = ascendings.get(i) * (v1 - v2);
            if (res != 0) {
                return res;
            }
        }
        return 0;
    }
}
