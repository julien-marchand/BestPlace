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
import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJobElement;
import entropy.vjob.VJobMultiSet;
import entropy.vjob.constraint.MockPlacementConstraint;

/**
 * A mock PlacementConstraintBuilder that build MockPlacementConstraint.
 *
 * @author Fabien Hermenier
 */
public class MockConstraintBuilder implements PlacementConstraintBuilder {

    @Override
    public String getIdentifier() {
        return "mock";
    }

    @Override
    public String getSignature() {
        return getIdentifier() + "(<vmset>)";
    }

    @Override
    public PlacementConstraint buildConstraint(List<VJobElement> params) throws ConstraintBuilderException {
        try {
            return new MockPlacementConstraint((VJobMultiSet<VirtualMachine>) params.get(0));
        } catch (ClassCastException e) {
            throw new ConstraintBuilderException(this);
        }
    }
}
