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

package entropy.plan.partitioner;

import java.util.ArrayList;
import java.util.List;

import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.vjob.PlacementConstraint;

/**
 * @author Fabien Hermenier
 */
public class Partition {

    private ManagedElementSet<VirtualMachine> vms;

    private ManagedElementSet<Node> nodes;

    private List<PlacementConstraint> constraints;

    public Partition() {
        this.vms = new SimpleManagedElementSet<VirtualMachine>();
        this.nodes = new SimpleManagedElementSet<Node>();
        this.constraints = new ArrayList<PlacementConstraint>();
    }

    public ManagedElementSet<Node> getNodes() {
        return this.nodes;
    }

    public ManagedElementSet<VirtualMachine> getVirtualMachines() {
        return this.vms;
    }


    public List<PlacementConstraint> getConstraints() {
        return this.constraints;
    }

    @Override
	public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(vms);
        b.append(" -> ");
        b.append(nodes);
        b.append("\n");
        b.append(constraints);
        return b.toString();
    }
}
