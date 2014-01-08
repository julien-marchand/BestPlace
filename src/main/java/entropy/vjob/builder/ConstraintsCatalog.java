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

package entropy.vjob.builder;

import java.util.List;
import java.util.Set;

import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJobElement;

/**
 * A catalog contains several constraints available to restrict the placement
 * of virtual machines.
 *
 * @author Fabien Hermenier
 */
public interface ConstraintsCatalog {
    /**
     * Get all the available constraints.
     *
     * @return a set of constraint identifier. May be empty
     */
    Set<String> getAvailableConstraints();

    /**
     * Build the constraint associaed to its identifier using specific parameters.
     *
     * @param id     the identifier of the constraint
     * @param params the parameters to give to the constraint
     * @return a placement constraint or {@code null} if no constraint is associated to the given identifier
     * @throws ConstraintBuilderException if an error occurred while building the constraint
     */
    PlacementConstraint buildConstraint(String id, List<VJobElement> params) throws ConstraintBuilderException;
}
