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

package gipad.placementconstraint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.discovery.DiscoveryModel.model.Node;
import org.discovery.DiscoveryModel.model.VirtualMachine;

import gipad.configuration.ManagedElementList;
import gipad.configuration.SimpleManagedElementList;
import gipad.configuration.configuration.Configuration;

/**
 * A constraint to ensure a set of VMs will be hosted on different nodes.
 *
 * @author Fabien Hermenier
 */
public abstract class Spread implements PlacementConstraint {

    /**
     * The VMs involved in the constraint.
     */
    protected ManagedElementList<VirtualMachine> vms;

    /**
     * Make a new constraint.
     *
     * @param vms the involved virtual machines
     */
    public Spread(ManagedElementList<VirtualMachine> vms) {
        this.vms = vms;
    }

    /**
     * Get the virtual machines involved in the constraint.
     *
     * @return a set of virtual machines. Should not be empty
     */
    @Override
    public ManagedElementList<VirtualMachine> getAllVirtualMachines() {
        return (ManagedElementList<VirtualMachine>) this.vms;
    }

    /**
     * Get the set of virtual machines involved in the constraint.
     *
     * @return a set of virtual machine. Should not be empty
     */
    public ManagedElementList<VirtualMachine> getVirtualMachines() {
        return this.vms;
    }

    @Override
    public ManagedElementList<Node> getNodes() {
        return new SimpleManagedElementList<Node>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Spread that = (Spread) o;
        return getAllVirtualMachines().equals(that.getAllVirtualMachines());
    }


    @Override
    public int hashCode() {
        return getAllVirtualMachines().hashCode();
    }

    /**
     * Check that the constraint is satified in a configuration.
     *
     * @param cfg the configuration to check
     * @return true if the running VMs are hosted on distinct nodes
     */
    @Override
    public boolean isSatisfied(Configuration cfg) {
        HashSet<Node> used = new HashSet<Node>();
        for (VirtualMachine vm : getAllVirtualMachines()) {
            Node h = cfg.getLocation(vm);
            if (cfg.isRunning(vm)) {
                if (used.contains(h)) {
                    return false;
                }
                used.add(h);
            }
        }
        return true;
    }

    @Override
    public ManagedElementList<VirtualMachine> getMisPlaced(Configuration cfg) {
        Map<Node, ManagedElementList<VirtualMachine>> spots = new HashMap<Node, ManagedElementList<VirtualMachine>>();
        ManagedElementList<VirtualMachine> bad = new SimpleManagedElementList<VirtualMachine>();
        for (VirtualMachine vm : getAllVirtualMachines()) {
            Node h = cfg.getLocation(vm);
            if (cfg.isRunning(vm)) {
                if (!spots.containsKey(h)) {
                    spots.put(h, new SimpleManagedElementList<VirtualMachine>());
                }
                spots.get(h).add(vm);
            }

        }
        for (Map.Entry<Node, ManagedElementList<VirtualMachine>> e : spots.entrySet()) {
            if (e.getValue().size() > 1) {
                bad.addAll(e.getValue());
               //FIXME debug log VJob.logger.debug(e.getValue() + " are hosted on the same node: '" + e.getKey().getName() + "'");
            }
        }
        return bad;
    }
}
