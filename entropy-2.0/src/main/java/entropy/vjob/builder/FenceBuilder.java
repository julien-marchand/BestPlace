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
import entropy.vjob.Fence;
import entropy.vjob.VJobElement;
import entropy.vjob.VJobSet;

/**
 * A builder to make Fence constraint.
 *
 * @author Fabien Hermenier
 */
public class FenceBuilder implements PlacementConstraintBuilder {

    @Override
    public String getIdentifier() {
        return "fence";
    }

    @Override
    public String getSignature() {
        return "fence(<vmset>, <nodeset>)";
    }

    /**
     * Build a constraint.
     *
     * @param args the parameters to use. Must be 2 non-empty set. One of virtual machines and one of nodes.
     * @return a constraint
     * @throws ConstraintBuilderException if an error occurred while building the constraint.
     */
    @Override
    public Fence buildConstraint(List<VJobElement> args) throws ConstraintBuilderException {
        if (args.size() != 2) {
            throw new ConstraintBuilderException(this);
        }
        try {
            VJobSet<VirtualMachine> vms = (VJobSet<VirtualMachine>) args.get(0);
            VJobSet<Node> ns = (VJobSet<Node>) args.get(1);
            if (vms.size() == 0 || ns.size() == 0) {
                throw new ConstraintBuilderException("Empty sets not allowed");
            }
            return new Fence(vms, ns);
        } catch (ClassCastException e) {
            throw new ConstraintBuilderException(getSignature(), e);
        }
    }
}
