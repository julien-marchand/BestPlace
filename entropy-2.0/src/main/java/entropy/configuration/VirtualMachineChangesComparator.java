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
 * A comparator to sort VMs depending on their resources variation.
 * As an example for an ascending comparison based on the variation on the CPU resources
 * if vm1 consumes 40 units and asks for 90, its variation is greater than vm2 that consumes
 * 100 units and asks for 130 units.
 *
 * @author Fabien Hermenier
 */
public class VirtualMachineChangesComparator implements Comparator<DefaultVirtualMachine> {

    /**
     * Compare in ascending or descending order.
     */
    private boolean asc;

    /**
     * The comparison criteria.
     */
    private List<Criterion> criteria;

    /**
     * The possible comparison criterion.
     */
    public static enum Criterion {
        memVariation, cpuVariation
    }

    /**
     * Make a new comparator.
     *
     * @param ascending true for a ascending comparison
     * @param c         the comparison criteria.
     */
    public VirtualMachineChangesComparator(boolean ascending, Criterion c) {
        asc = ascending;
        criteria = new LinkedList<Criterion>();
        criteria.add(c);
    }

    @Override
    public int compare(DefaultVirtualMachine o1, DefaultVirtualMachine o2) {
        int order = asc ? 1 : -1;

        for (int i = 0; i < criteria.size(); i++) {
            int d1;
            int d2;
            if (criteria.get(i).equals(Criterion.cpuVariation)) {
                d1 = o1.getCPUConsumption() - o1.getCPUDemand();
                d2 = o2.getCPUConsumption() - o2.getCPUDemand();
            } else {
                d1 = o1.getMemoryConsumption() - o1.getMemoryDemand();
                d2 = o2.getMemoryConsumption() - o2.getMemoryDemand();

            }
            if (d1 != d2) {
                //System.err.println("d1=" + d1 + " d2=" + d2);
                return (order * (d1 - d2));
            }

        }
        return 0;
    }

    /**
     * Append a comparison criterion.
     *
     * @param c the criteria to add
     */
    public void appendCriterion(Criterion c) {
        criteria.add(c);
    }
}
