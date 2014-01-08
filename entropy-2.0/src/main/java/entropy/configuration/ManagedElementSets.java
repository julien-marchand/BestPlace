/*
 * Copyright (c) Fabien Hermenier
 *
 * This file is part of Entropy.
 *
 * Entropy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Entropy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.configuration;

import java.util.Random;

/**
 * Various tools extracting datas from ManagedElementSet.
 *
 * @author Fabien Hermenier
 */
public final class ManagedElementSets {

    private static final Random rnd = new Random();

    /**
     * No instantiation.
     */
    private ManagedElementSets() {
    }

    /**
     * Sum the resources of a set of virtual machines
     *
     * @param criteria the resources to consider
     * @return the sum of each resource in the same order they was provided
     */
    public static int[] sum(ManagedElementSet<VirtualMachine> vms, ResourcePicker.VMRc... criteria) {
        int[] res = new int[criteria.length];
        for (VirtualMachine vm : vms) {
            for (int i = 0; i < criteria.length; i++) {
                res[i] += ResourcePicker.get(vm, criteria[i]);
            }
        }
        return res;
    }

    /**
     * Sum the resources of a set of nodes
     *
     * @param criteria the resources to consider
     * @return the sum of each resource in the same order they were provided
     */
    public static int[] sum(ManagedElementSet<Node> ns, ResourcePicker.NodeRc... criteria) {
        int[] res = new int[criteria.length];
        for (Node n : ns) {
            for (int i = 0; i < criteria.length; i++) {
                res[i] += ResourcePicker.get(n, criteria[i]);
            }
        }
        return res;
    }

    /**
     * Get the virtual machines using the biggest
     * amount of a given resource
     *
     * @param criterion the resource to consider
     * @return the selected virtual machine or {@code null} if there is no virtual machines in the set
     */
    public static VirtualMachine max(ManagedElementSet<VirtualMachine> vms, ResourcePicker.VMRc criterion) {
        VirtualMachine max = null;
        for (VirtualMachine vm : vms) {
            if (max == null || ResourcePicker.get(max, criterion) < ResourcePicker.get(vm, criterion)) {
                max = vm;
            }
        }
        return max;
    }

    /**
     * Get the virtual machines using the smallest
     * amount of a given resource
     *
     * @param criterion the resource to consider
     * @return the selected virtual machine or {@code null} if there is no virtual machines in the set
     */
    public static VirtualMachine min(ManagedElementSet<VirtualMachine> vms, ResourcePicker.VMRc criterion) {
        VirtualMachine min = null;
        for (VirtualMachine vm : vms) {
            if (min == null || ResourcePicker.get(min, criterion) > ResourcePicker.get(vm, criterion)) {
                min = vm;
            }
        }
        return min;
    }

    /**
     * Get the node with the biggest capacity
     * of a given resource.
     *
     * @param criterion the resource to consider
     * @return the selected node or {@code null} if there is no nodes in the set
     */
    public static Node max(ManagedElementSet<Node> ns, ResourcePicker.NodeRc criterion) {
        Node max = null;
        for (Node n : ns) {
            if (max == null || ResourcePicker.get(max, criterion) < ResourcePicker.get(n, criterion)) {
                max = n;
            }
        }
        return max;
    }

    /**
     * Get the node with the biggest capacity
     * of a given resource
     *
     * @param criterion the resource to consider
     * @return the selected node or {@code null} if there is no nodes in the set
     */
    public static Node min(ManagedElementSet<Node> ns, ResourcePicker.NodeRc criterion) {
        Node min = null;
        for (Node n : ns) {
            if (min == null || ResourcePicker.get(min, criterion) > ResourcePicker.get(n, criterion)) {
                min = n;
            }
        }
        return min;
    }

    /**
     * Pick up a random virtual machine from a set
     *
     * @param elems the elements to browse
     * @return the selected virtual machine
     */
    public static VirtualMachine randomVirtualMachine(ManagedElementSet<VirtualMachine> elems) {
        int s = elems.size();
        return elems.get(rnd.nextInt(s));
    }

    /**
     * Pick up a random node from a set
     *
     * @param elems the elements to browse
     * @return the selected node
     */
    public static Node randomNode(ManagedElementSet<Node> elems) {
        int s = elems.size();
        return elems.get(rnd.nextInt(s));
    }

}
