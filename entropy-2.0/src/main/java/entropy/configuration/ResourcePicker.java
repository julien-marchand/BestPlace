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

/**
 * A tool to retrieve a resource from a virtual machine or a node using an identifier.
 *
 * @author Fabien Hermenier
 */
public final class ResourcePicker {

    /**
     * The resources of a virtual machine.
     */
    public static enum VMRc {

        /**
         * retrieve {@link VirtualMachine#getNbOfCPUs()} .
         */
        nbOfCPUs,

        /**
         * retrieve {@link VirtualMachine#getCPUConsumption()}.
         */
        cpuConsumption,

        /**
         * retrieve {@link VirtualMachine#getCPUDemand()} .
         */
        cpuDemand,

        /**
         * retrieve {@link VirtualMachine#getMemoryConsumption()} .
         */
        memoryConsumption,

        /**
         * retrieve {@link VirtualMachine#getMemoryDemand()} .
         */
        memoryDemand
    }

    /**
     * The resources of a node.
     */
    public static enum NodeRc {

        /**
         * retrieve {@link Node#getNbOfCPUs()} .
         */
        nbOfCPUs,

        /**
         * retrieve {@link Node#getCPUCapacity()} .
         */
        cpuCapacity,

        /**
         * retrieve {@link Node#getMemoryCapacity()} .
         */
        memoryCapacity
    }

    /**
     * Utility class. No instantiation.
     */
    private ResourcePicker() {
    }

    /**
     * Get the resource value of a virtual machine associated to an identifier.
     *
     * @param vm the virtual machine
     * @param r  the resource to retrieve
     * @return the value of the resource
     */
    public static int get(VirtualMachine vm, VMRc r) {
        switch (r) {
            case cpuConsumption:
                return vm.getCPUConsumption();
            case cpuDemand:
                return vm.getCPUDemand();
            case memoryConsumption:
                return vm.getMemoryConsumption();
            case memoryDemand:
                return vm.getMemoryDemand();
            case nbOfCPUs:
                return vm.getNbOfCPUs();
        }
        return -1;
    }

    /**
     * Get the resource value of a node associated to an identifier.
     *
     * @param n the node
     * @param r the resource to retrieve
     * @return the value of the resource
     */
    public static int get(Node n, NodeRc r) {
        switch (r) {
            case cpuCapacity:
                return n.getCPUCapacity();
            case memoryCapacity:
                return n.getMemoryCapacity();
            case nbOfCPUs:
                return n.getNbOfCPUs();
        }
        return -1;
    }
}
