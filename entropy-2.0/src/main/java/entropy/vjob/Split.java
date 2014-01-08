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

package entropy.vjob;

import java.util.HashSet;
import java.util.Set;

import entropy.configuration.Configuration;
import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;

/**
 * A constraint to enforce that two vmset can not share nodes.
 * All the VMs of the first set can not be hosted on any node that host VMs of
 * the second set.
 * <p/>
 * TODO: Test and compare this implementation vs. a set based implementation
 * TODO: redondant semantic wrt. Spread, same objective, different managed elements
 *
 * @author Fabien Hermenier
 */
public abstract class Split implements PlacementConstraint {

    /**
     * The first vmset.
     */
    private VJobSet<VirtualMachine> set1;

    /**
     * The second vmset.
     */
    private VJobSet<VirtualMachine> set2;

    /**
     * Make a new constraint.
     *
     * @param vmset1 the first set of virtual machines
     * @param vmset2 the second set of virtual machines
     */
    public Split(VJobSet<VirtualMachine> vmset1, VJobSet<VirtualMachine> vmset2) {
        this.set1 = vmset1;
        this.set2 = vmset2;
    }

    /**
     * Get the first set of virtual machines.
     *
     * @return a set of VMs. Should not be empty
     */
    public final ManagedElementSet<VirtualMachine> getFirstSet() {
        return this.set1.flatten();
    }

    /**
     * Get the second set of virtual machines.
     *
     * @return a set of VMs. Should not be empty
     */
    public final ManagedElementSet<VirtualMachine> getSecondSet() {
        return this.set2.flatten();
    }

    @Override
    public String toString() {
        return new StringBuilder("split(").append(set1.pretty())
                .append(", ").append(set2.pretty())
                .append(")").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Split that = (Split) o;
        return getFirstSet().equals(that.getFirstSet()) && getSecondSet().equals(that.getSecondSet());
    }

    @Override
    public int hashCode() {
        int result = getFirstSet().hashCode();
        result = 31 * result + getSecondSet().hashCode();
        return result;
    }

    /**
     * Check that the constraint is satified in a configuration.
     *
     * @param cfg the configuration to check
     * @return true if the VMs are hosted on distinct group of nodes
     */
    @Override
    public boolean isSatisfied(Configuration cfg) {
        Set<Node> firstNodes = new HashSet<Node>();
        Set<Node> secondNodes = new HashSet<Node>();
        if (getFirstSet().size() == 0 || getSecondSet().size() == 0) {
            VJob.logger.error(this.toString() + ": Some sets of virtual machines are empty");
            return false;
        }

        for (VirtualMachine vm : getFirstSet()) {
            if (cfg.isRunning(vm)) {
                firstNodes.add(cfg.getLocation(vm));
            }
        }
        for (VirtualMachine vm : getSecondSet()) {
            if (cfg.isRunning(vm)) {
                secondNodes.add(cfg.getLocation(vm));
            }
        }

        for (Node n : firstNodes) {
            if (secondNodes.contains(n)) {
                ManagedElementSet<Node> ns = new DefaultManagedElementSet<Node>();
                ns.addAll(firstNodes);
                ns.retainAll(secondNodes);
                VJob.logger.error(this.toString() + ": Nodes host VMs of the two groups: " + ns);
                return false;
            }
        }
        for (Node n : secondNodes) {
            if (firstNodes.contains(n)) {
                ManagedElementSet<Node> ns = new DefaultManagedElementSet<Node>();
                ns.addAll(secondNodes);
                ns.retainAll(firstNodes);
                VJob.logger.error(this.toString() + ": Nodes host VMs of the two groups: " + ns);
                return false;
            }
        }
        return true;
    }

    @Override
    public ExplodedSet<VirtualMachine> getAllVirtualMachines() {
        ExplodedSet<VirtualMachine> all = new ExplodedSet<VirtualMachine>(getFirstSet());
        all.addAll(getSecondSet());
        return all;
    }

    @Override
    public ExplodedSet<Node> getNodes() {
        return new ExplodedSet<Node>();
    }

    @Override
    public ExplodedSet<VirtualMachine> getMisPlaced(Configuration cfg) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
