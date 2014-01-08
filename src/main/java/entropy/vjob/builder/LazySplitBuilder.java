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

import entropy.configuration.VirtualMachine;
import entropy.vjob.LazySplit;
import entropy.vjob.Split;
import entropy.vjob.VJobElement;
import entropy.vjob.VJobSet;

/**
 * A builder for LazySplitBuilder.
 *
 * @author Fabien Hermenier
 */
public class LazySplitBuilder implements PlacementConstraintBuilder {

    @Override
    public String getIdentifier() {
        return "lSplit";
    }

    @Override
    public String getSignature() {
        return "lSplit(<vmset>,<vmset>)";
    }

    /**
     * Build a constraint.
     *
     * @param args the parameters of the constraint. Must be 2 non-empty set of virtual machines.
     * @return the constraint
     * @throws ConstraintBuilderException if an error occurred while building the constraint
     */
    @Override
    public Split buildConstraint(List<VJobElement> args) throws ConstraintBuilderException {
        if (args.size() != 2) {
            throw new ConstraintBuilderException(this);
        }
        try {
            VJobSet<VirtualMachine> vms1 = (VJobSet<VirtualMachine>) args.get(0);
            VJobSet<VirtualMachine> vms2 = (VJobSet<VirtualMachine>) args.get(1);
            if (vms1.size() == 0 || vms2.size() == 0) {
                throw new ConstraintBuilderException("Empty set not allowed");
            }
            return new LazySplit(vms1, vms2);
        } catch (ClassCastException e) {
            throw new ConstraintBuilderException(getSignature(), e);
        }
    }

}
