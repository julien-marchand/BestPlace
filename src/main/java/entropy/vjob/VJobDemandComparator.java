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


import java.util.Comparator;

import entropy.configuration.ResourcePicker;
import entropy.configuration.VirtualMachinesSetComparator;

/**
 * A comparator of VJob regards to their VMs.
 *
 * @author Fabien Hermenier
 */
public class VJobDemandComparator implements Comparator<VJob> {

    /**
     * The comparator to consider the VMs of the vjobs.
     */
    private VirtualMachinesSetComparator cmp;

    /**
     * Build a new comparator.
     *
     * @param asc {@code true} for an ascending sort
     * @param rc  the resource to consider for the comparison
     */
    public VJobDemandComparator(boolean asc, ResourcePicker.VMRc rc) {
        cmp = new VirtualMachinesSetComparator(asc, rc);
    }

    /**
     * Append a comparison criteria.
     * If the previous criterion provides no difference between the vjobs, then
     * the comparison is made wrt. this criteria
     *
     * @param asc {@code true} for an ascending sort
     * @param rc  the resource to consider for the comparison
     */
    public void appendCriteria(boolean asc, ResourcePicker.VMRc rc) {
        cmp.appendCriteria(asc, rc);
    }

    @Override
    public int compare(VJob v1, VJob v2) {
        return cmp.compare(v1.getVirtualMachines(), v2.getVirtualMachines());
    }
}
