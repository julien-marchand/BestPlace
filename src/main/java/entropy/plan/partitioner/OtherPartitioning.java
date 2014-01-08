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

import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.vjob.Ban;
import entropy.vjob.ContinuousSpread;
import entropy.vjob.ExplodedMultiSet;
import entropy.vjob.ExplodedSet;
import entropy.vjob.Fence;
import entropy.vjob.OneOf;
import entropy.vjob.Spread;

/**
 * @author Fabien Hermenier
 */
public class OtherPartitioning implements PlanPartitioner {

    private List<Partition> parts;

    public OtherPartitioning(Configuration cfg) {
        this.parts = new ArrayList<Partition>();
    }

    @Override
    public void part(Fence f) throws PartitioningException {
        //Check if a partition already exists than contains both the set of VMs and node
        Partition parent = null;
        ManagedElementSet<Node> ns = f.getNodes().getElements();
        ManagedElementSet<VirtualMachine> vms = f.getAllVirtualMachines();

        for (Partition p : parts) {
            if (p.getNodes().containsAll(ns)) {
                if (parent == null) {
                    //System.err.println(f + " fit in " + p);
                    //VMs will belong to this partition
                    p.getVirtualMachines().addAll(vms);
                    p.getConstraints().add(f);
                    parent = p;
                } else {
                    throw new PartitioningException(vms + " already belong to a partition" + p);
                }
            }
        }
        if (parent == null) {
            Partition p = new Partition();
            p.getNodes().addAll(ns);
            p.getVirtualMachines().addAll(vms);
            p.getConstraints().add(f);
            parts.add(p);
            //System.err.println(f + " new part");
        }
    }

    @Override
    public void part(Ban b) throws PartitioningException {

    }

    @Override
	public void part(OneOf of) throws PartitioningException {
        //All the VMs must be on a single group
        Partition parent = null;
        ExplodedSet<VirtualMachine> vms = of.getAllVirtualMachines();
        //If all the VM are in a single VM partition
        //it is possible to filter the nodes group
        for (Partition p : parts) {
            if (p.getVirtualMachines().containsAll(vms)) {
                if (parent == null) {
                    //VMs will belong to this partition
                    p.getVirtualMachines().addAll(vms);

                    //Alter the candidate nodes: remove all nodes not in the part
                    ExplodedMultiSet<Node> g2 = new ExplodedMultiSet<Node>();
                    for (ExplodedSet<Node> s : of.getGroups().expand()) {
                        ExplodedSet<Node> ss = new ExplodedSet<Node>(s);
                        ss.retainAll(p.getNodes());
                        if (ss.size() > 0) {
                            g2.add(ss);
                        }
                    }
                    if (g2.size() == 1) {
                        p.getConstraints().add(new Fence(vms, g2.get(0)));
                    } else {
                        p.getConstraints().add(new OneOf(vms, g2));
                    }
                    parent = p;
                } else {
                    throw new PartitioningException(vms + " already belong to a partition" + p);
                }
            }
        }
        if (parent == null) {
            Partition p = new Partition();
            p.getNodes().addAll(of.getGroups().getElements());
            p.getVirtualMachines().addAll(vms);
            p.getConstraints().add(of);
            parts.add(p);
        }
    }

    @Override
    public void part(Spread c) throws PartitioningException {
        //We can split the spread constraint
        ManagedElementSet<VirtualMachine> vms = c.getAllVirtualMachines().clone();
        for (Partition p : parts) {
            if (p.getVirtualMachines().containsAll(vms)) {
                p.getConstraints().add(c);
                return;
            } else {
                //If vms are partially in, we can split the constraint
                if (vms.retainAll(p.getVirtualMachines())) {
                    if (vms.size() > 0) {
                        Spread sub = new ContinuousSpread(new ExplodedSet<VirtualMachine>(vms));
                        p.getConstraints().add(sub);
                    } else {
                        vms = c.getAllVirtualMachines().clone();
                    }
                }
            }
        }
    }

    @Override
    public List<Partition> getResultingPartitions() {
        return parts;
    }
}
