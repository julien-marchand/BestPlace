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

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.vjob.Ban;
import entropy.vjob.ExplodedSet;
import entropy.vjob.Fence;
import entropy.vjob.OneOf;
import entropy.vjob.Spread;

/**
 * @author Fabien Hermenier
 */
public class GraphPartitioner implements PlanPartitioner {

    private BitSet[] matrix;

    private ManagedElementSet<Node> nodes;

    private ManagedElementSet<VirtualMachine> vms;

    private int idxVMs;

    private HashMap<VirtualMachine, Integer> vmToInt;
    private HashMap<Node, Integer> nodeToInt;

    public GraphPartitioner(Configuration cfg) {
        this.vms = cfg.getAllVirtualMachines();
        this.nodes = cfg.getAllNodes();
        idxVMs = vms.size();
        matrix = new BitSet[vms.size() + nodes.size()];
        //For 0 to idxVMS - 1, its only VMs, so they are linked to all nodes
        //ie all the bitset between 0 and idxVMs - 1 have their bit from idxVMs to the end, set
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = new BitSet(matrix.length);
            if (i < idxVMs) {
                matrix[i].set(idxVMs, matrix.length);
            }
        }

        vmToInt = new HashMap<VirtualMachine, Integer>();
        nodeToInt = new HashMap<Node, Integer>();
        for (int i = 0; i < vms.size(); i++) {
            vmToInt.put(vms.get(i), i);
        }

        for (int i = 0; i < nodeToInt.size(); i++) {
            nodeToInt.put(nodes.get(i), i + idxVMs);
        }

    }

    @Override
    public void part(Fence f) throws PartitioningException {
        //For each VM, we remove the edge between VMs and nodes not in fence
        for (VirtualMachine vm : f.getVirtualMachines()) {
            BitSet s = matrix[vmToInt.get(vm)];
            for (int i = s.nextSetBit(0); i >= 0; i = s.nextSetBit(i + 1)) {
                Node n = nodes.get(i - idxVMs);
                if (f.getNodes().contains(n)) {
                    s.clear(i);
                }
            }
        }
    }

    @Override
    public void part(Ban b) throws PartitioningException {
        //For each VM, we remove the edge between VMs and nodes in ban
        for (VirtualMachine vm : b.getVirtualMachines()) {
            BitSet s = matrix[vmToInt.get(vm)];
            for (Node n : b.getNodes()) {
                s.clear(nodeToInt.get(n));
            }
        }
    }

    @Override
    public void part(Spread s) throws PartitioningException {
        //Create an edge between each VM.
        ExplodedSet<VirtualMachine> vs = s.getAllVirtualMachines();
        for (int i = 0; i < vs.size(); i++) {
            VirtualMachine vi = vs.get(i);
            int x = vmToInt.get(vi);
            for (int j = 0; j < i; j++) {
                VirtualMachine vj = vs.get(j);
                int y = vmToInt.get(vj);
                matrix[x].set(y);
                matrix[y].set(x);
            }

        }
    }

    public void printMatrix() {
        for (int i = 0; i < matrix.length; i++) {
            BitSet b = matrix[i];
            if (i == idxVMs) {
                System.err.println("--- Nodes Edges ---");
            }
            System.err.println(b);
        }
    }

    @Override
    public void part(OneOf s) throws PartitioningException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> getResultingPartitions() {
        return null;
    }
}
