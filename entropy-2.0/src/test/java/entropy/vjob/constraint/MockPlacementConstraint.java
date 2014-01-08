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
package entropy.vjob.constraint;

import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.vjob.ExplodedSet;
import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJobMultiSet;

/**
 * A mock assignment constraint for test purpose.
 *
 * @author Fabien Hermenier
 */
public class MockPlacementConstraint implements PlacementConstraint {

    private VJobMultiSet<VirtualMachine> sets;

    public MockPlacementConstraint() {

    }

    public MockPlacementConstraint(VJobMultiSet<VirtualMachine> vmsets) {
        this.sets = vmsets;
    }

    @Override
    public void inject(ReconfigurationProblem plan) {
        throw new UnsupportedOperationException();
    }

    @Override
	public String toString() {
        return "mock(" + sets.pretty() + ")";
    }

    /**
     * Check that the constraint is satified in a configuration.
     *
     * @param cfg the configuration to check
     * @return true
     */
    @Override
    public boolean isSatisfied(Configuration cfg) {
        return true;
    }

    @Override
    public ExplodedSet<VirtualMachine> getAllVirtualMachines() {
        return new ExplodedSet<VirtualMachine>(sets.getElements());
    }

    @Override
    public ExplodedSet<VirtualMachine> getMisPlaced(Configuration cfg) {
        return new ExplodedSet<VirtualMachine>();
    }

    @Override
    public ExplodedSet<Node> getNodes() {
        return new ExplodedSet<Node>();
    }
}
