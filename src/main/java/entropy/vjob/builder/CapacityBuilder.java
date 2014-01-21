/*
 * Copyright (c) Fabien Hermenier
 *
 *        This file is part of Entropy.
 *
 *        Entropy is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU Lesser General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        Entropy is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU Lesser General Public License for more details.
 *
 *        You should have received a copy of the GNU Lesser General Public License
 *        along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.vjob.builder;

import java.util.List;

import entropy.configuration.Node;
import entropy.vjob.Capacity;
import entropy.vjob.VJobElement;
import entropy.vjob.VJobSet;

/**
 * A builder to make Capacity constraints.
 *
 * @author Fabien Hermenier
 */
public class CapacityBuilder implements PlacementConstraintBuilder {

    @Override
    public String getIdentifier() {
        return "capacity";
    }

    @Override
    public String getSignature() {
        return "capacity(<nodeset>, int)";
    }

    @Override
    public Capacity buildConstraint(List<VJobElement> args) throws ConstraintBuilderException {
        if (args.size() != 2) {
            throw new ConstraintBuilderException(this);
        }
        try {
            VJobSet<Node> ns = (VJobSet<Node>) args.get(0);
            if (ns.size() == 0) {
                throw new ConstraintBuilderException("Empty set not allowed");
            }
            NumberElement e = (NumberElement) args.get(1);
            int v = e.getValue();
            if (v < 0) {
                throw new ConstraintBuilderException("Negative capacity not allowed (" + v + ")");
            }
            return new Capacity(ns, v);
        } catch (ClassCastException e) {
            throw new ConstraintBuilderException(getSignature(), e);
        }
    }
}
