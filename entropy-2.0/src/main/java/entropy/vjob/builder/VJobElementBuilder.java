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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;

/**
 * Build the element of the vjobs from their
 * value.
 *
 * @author Fabien Hermenier
 */
public class VJobElementBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger("VJobBuilder");

    /**
     * The vmBuilder to instantiate virtual machines.
     */
    private VirtualMachineBuilder vmBuilder;

    /**
     * The current configuration to get existing nodes and virtual machines.
     */
    private Configuration curConfig;

    /**
     * Make a new vmBuilder.
     *
     * @param b the vmBuilder to use to instantiate new virtual machines
     */
    public VJobElementBuilder(VirtualMachineBuilder b) {
        vmBuilder = b;
    }

    /**
     * Set the configuration to use to retrieve existing elements.
     *
     * @param cfg the configuration
     */
    public void useConfiguration(Configuration cfg) {
        curConfig = cfg;
    }

    /**
     * Get the element as a node.
     *
     * @param id the identifier of the element
     * @return a node if such an element exists in the current configuration with the identifier.
     *         {@code null} otherwise
     */
    public Node matchAsNode(String id) {
        if (curConfig == null) {
            return null;
        }
        Node n = curConfig.getOnlines().get(id);
        if (n != null) {
            return n;
        }
        return curConfig.getOfflines().get(id);
    }

    /**
     * Get the element as a virtual machine.
     *
     * @param id the identifier of the element
     * @return a virtual machine if such an element exists in the current configuration or
     *         in the repository of virtual machines. the identifier. {@code null} otherwise
     */
    public VirtualMachine matchAsVirtualMachine(String id) {
        VirtualMachine vm = null;
        if (curConfig != null) {
            vm = curConfig.getRunnings().get(id);
            if (vm == null) {
                vm = curConfig.getWaitings().get(id);
            }
            if (vm == null) {
                vm = curConfig.getSleepings().get(id);
            }
        }
        if (vm == null && vmBuilder != null) {
            try {
                vm = vmBuilder.buildVirtualMachine(id);
            } catch (VirtualMachineBuilderException e) {
                LOGGER.error(e.getMessage());
            }
        }
        return vm;
    }

    /**
     * Get the builder used to instantiate virtual machines.
     *
     * @return a builder.
     */
    public VirtualMachineBuilder getVirtualMachineBuilder() {
        return vmBuilder;
    }
}
