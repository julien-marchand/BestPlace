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
import entropy.vjob.Ban;
import entropy.vjob.VJobElement;
import entropy.vjob.VJobSet;

/**
 * A builder to create Ban constraints.
 *
 * @author Fabien Hermenier
 */
public class BanBuilder implements PlacementConstraintBuilder {

    @Override
    public String getIdentifier() {
        return "ban";
    }

    @Override
    public String getSignature() {
        return "ban(<vmset>,<nodeset>)";
    }

    /**
     * Build a ban constraint.
     *
     * @param args must be 2 VJobset, first contains virtual machines and the second nodes. Each set must not be empty
     * @return a constraint
     * @throws ConstraintBuilderException if arguments are not compatible with the constraint
     */
    @Override
    public Ban buildConstraint(List<VJobElement> args) throws ConstraintBuilderException {
        if (args.size() != 2) {
            throw new ConstraintBuilderException(this);
        }
        try {
            VJobSet<VirtualMachine> vms = (VJobSet<VirtualMachine>) args.get(0);
            VJobSet<Node> ns = (VJobSet<Node>) args.get(1);
            if (vms.size() == 0 || ns.size() == 0) {
                throw new ConstraintBuilderException("Empty sets not allowed");
            }
            return new Ban((VJobSet<VirtualMachine>) args.get(0), (VJobSet<Node>) args.get(1));
        } catch (ClassCastException e) {
            throw new ConstraintBuilderException(getSignature(), e);
        }

    }
}
