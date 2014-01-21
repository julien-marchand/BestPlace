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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJobElement;

/**
 * Default implementation of ConstraintCalalog.
 *
 * @author Fabien Hermenier
 */
public class DefaultConstraintsCatalog implements ConstraintsCatalog {

    /**
     * The map to get the builder associated to a constraint.
     */
    private Map<String, PlacementConstraintBuilder> builders;

    /**
     * Build a new catalog.
     */
    public DefaultConstraintsCatalog() {
        this.builders = new HashMap<String, PlacementConstraintBuilder>();

    }

    /**
     * Add a constraint builder to the catalog.
     * There must not be another builder with the same identifier in the catalog
     *
     * @param c the constraint to add
     * @return true if the constraintbuilder has been added.
     */
    public boolean add(PlacementConstraintBuilder c) {
        if (this.builders.containsKey(c.getIdentifier())) {
            return false;
        }
        this.builders.put(c.getIdentifier(), c);
        return true;
    }

    @Override
    public Set<String> getAvailableConstraints() {
        return this.builders.keySet();
    }

    @Override
    public PlacementConstraint buildConstraint(String id, List<VJobElement> params) throws ConstraintBuilderException {
        if (!this.builders.containsKey(id)) {
            return null;
        }
        return builders.get(id).buildConstraint(params);
    }
}
