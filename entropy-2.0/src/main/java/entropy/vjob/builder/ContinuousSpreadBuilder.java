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
import entropy.vjob.ContinuousSpread;
import entropy.vjob.VJobElement;
import entropy.vjob.VJobSet;

/**
 * A builder to make ContinuousSpread constraints.
 *
 * @author Fabien Hermenier
 */
public class ContinuousSpreadBuilder implements PlacementConstraintBuilder {

    @Override
    public String getIdentifier() {
        return "cSpread";
    }

    @Override
    public String getSignature() {
        return "cSpread(<vmset>)";
    }

    @Override
    public ContinuousSpread buildConstraint(List<VJobElement> args) throws ConstraintBuilderException {
        if (args.size() != 1) {
            throw new ConstraintBuilderException(this);
        }
        try {
            VJobSet<VirtualMachine> vms = (VJobSet<VirtualMachine>) args.get(0);
            if (vms.size() == 0) {
                throw new ConstraintBuilderException("Empty set not allowed");
            }
            return new ContinuousSpread(vms);
        } catch (ClassCastException e) {
            throw new ConstraintBuilderException(getSignature(), e);
        }
    }
}
