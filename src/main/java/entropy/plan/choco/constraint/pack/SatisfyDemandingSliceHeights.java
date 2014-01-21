/*
 * Copyright (c) 2010 Ecole des Mines de Nantes and Fabien Hermenier
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

package entropy.plan.choco.constraint.pack;

import entropy.plan.choco.constraint.GlobalConstraint;

/**
 * Created by IntelliJ IDEA.
 * User: fhermeni
 * Date: 15/12/10
 * Time: 19:01
 * To change this template use File | Settings | File Templates.
 */
public interface SatisfyDemandingSliceHeights extends GlobalConstraint {

    /**
     * Get the constraint that pack the VMs wrt. their CPU usage.
     *
     * @return a constraint
     */
    CustomPack getCoreCPUPacking();

    /**
     * Get the constraint that pack the VMs wrt. their memory usage.
     *
     * @return a constraint
     */
    CustomPack getCoreMemPacking();

    int getRemainingCPU(int bin);

    int getRemainingMemory(int bin);
}
