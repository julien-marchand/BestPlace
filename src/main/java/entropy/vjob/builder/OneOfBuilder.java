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

import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.vjob.ExplodedSet;
import entropy.vjob.Fence;
import entropy.vjob.OneOf;
import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJobElement;
import entropy.vjob.VJobMultiSet;
import entropy.vjob.VJobSet;

/**
 * A builder to make OneOf constraint.
 *
 * @author Fabien Hermenier
 */
public class OneOfBuilder implements PlacementConstraintBuilder {

    @Override
    public String getIdentifier() {
        return "oneOf";
    }

    @Override
    public String getSignature() {
        return "onOf(<vmset>, <multinodeset>)";
    }

    /**
     * Build a constraint.
     *
     * @param args the argument. Must be a non-empty set of virtual machines and a multiset of nodes with
     *             at least two non-empty sets. If the multi set contains only one set, a {@code Fence} constraint is created
     * @return the constraint
     * @throws ConstraintBuilderException if an error occurred while building the constraint
     */
    @Override
    public PlacementConstraint buildConstraint(List<VJobElement> args) throws ConstraintBuilderException {

        if (args.size() != 2) {
            throw new ConstraintBuilderException(this);
        }
        try {
            VJobSet<VirtualMachine> vms = (VJobSet<VirtualMachine>) args.get(0);
            VJobMultiSet<Node> grps = (VJobMultiSet<Node>) args.get(1);
            if (vms.size() == 0 || grps.size() == 0) {
                throw new ConstraintBuilderException("Empty set not allowed");
            }
            if (grps.size() == 1) {
                //Optimize, a Fence constraint is more efficient
                return new Fence((VJobSet<VirtualMachine>) args.get(0), grps.expand().get(0));
            }
            for (ExplodedSet<Node> ns : grps.expand()) {
                if (ns.size() == 0) {
                    throw new ConstraintBuilderException("Empty set not allowed");
                }
            }
            return new OneOf((VJobSet<VirtualMachine>) args.get(0), (VJobMultiSet<Node>) args.get(1));
        } catch (ClassCastException e) {
            throw new ConstraintBuilderException(getSignature(), e);
        }
    }
}