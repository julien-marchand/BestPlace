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

package entropy.plan.choco.actionModel.slice;

import java.util.Comparator;

/**
 * A comparator to compare the resource consumption of Slice.
 *
 * @author Fabien Hermenier
 */
public class SliceComparator implements Comparator<Slice> {

    /**
     * The possible comparison criteria.
     */
    public enum ResourceType {
        cpuConsumption, memoryConsumption
    }

    /**
     * The current comparison criteria.
     */
    private ResourceType resource;

    /**
     * Ascending or descending order.
     */
    private boolean ascending;

    /**
     * Make a new comparator.
     *
     * @param asc true for an ascending comparison
     * @param rc  the comparison criteria
     */
    public SliceComparator(boolean asc, ResourceType rc) {
        this.ascending = asc;
        this.resource = rc;
    }

    @Override
    public int compare(Slice o1, Slice o2) {
        int order = ascending ? 1 : -1;
        if (resource == ResourceType.cpuConsumption) {
            return order * (o1.getCPUheight() - o2.getCPUheight());
        }
        return order * (o1.getMemoryheight() - o2.getMemoryheight());
    }
}
