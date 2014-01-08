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

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Compare two set of virtual machines by summing some resources.
 *
 * @author Fabien Hermenier
 */
public class VirtualMachinesSetComparator implements Comparator<ManagedElementSet<VirtualMachine>> {

    /**
     * The comparison criteria.
     */
    private List<ResourcePicker.VMRc> rcNames;

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
    public VirtualMachinesSetComparator(boolean asc, ResourcePicker.VMRc rc) {
        this.rcNames = new LinkedList<ResourcePicker.VMRc>();
        this.ascendings = new LinkedList<Integer>();
        this.appendCriteria(asc, rc);
    }

    /**
     * Add a sorting criteria.
     *
     * @param rc  the identifier of comparison criteria
     * @param asc true for an ascending comparison.
     */
    public final void appendCriteria(boolean asc, ResourcePicker.VMRc rc) {
        this.rcNames.add(rc);
        if (asc) {
            this.ascendings.add(1);
        } else {
            this.ascendings.add(-1);
        }
    }

    @Override
    public int compare(ManagedElementSet<VirtualMachine> s1, ManagedElementSet<VirtualMachine> s2) {

        int[] sumS1 = ManagedElementSets.sum(s1, rcNames.toArray(new ResourcePicker.VMRc[rcNames.size()]));
        int[] sumS2 = ManagedElementSets.sum(s2, rcNames.toArray(new ResourcePicker.VMRc[rcNames.size()]));

        double diff = 0;
        for (int i = 0; i < rcNames.size(); i++) {
            diff = ascendings.get(i) * (sumS1[i] - sumS2[i]);
            if (diff != 0) {
                break;
            }
        }

        if (diff > 0) {
            return 1;
        } else if (diff < 0) {
            return -1;
        }
        return 0;
    }
}
