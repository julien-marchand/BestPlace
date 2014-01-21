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
package entropy.monitoring.sampler;


import entropy.configuration.Configuration;
import entropy.configuration.DefaultConfiguration;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;

/**
 * Sample the memory resources of nodes and VMs involved in a configuration.
 * <p/>
 * For nodes, memory capacity is rounded to the nearest lower integer that divide the parameter.
 * For VMs, memory consumption is rounded to the nearest upper integer that divide the parameter.
 *
 * @author Fabien Hermenier
 */
public class MemorySampler implements ConfigurationSampler {

    /**
     * The divider to use.
     */
    private int divider;

    /**
     * Make a new MemorySampler using a specific divider.
     *
     * @param div the divider to use
     */
    public MemorySampler(int div) {
        this.divider = div;
    }

    /**
     * Get the used divider.
     *
     * @return the divider
     */
    public int getDivider() {
        return this.divider;
    }

    /**
     * Sample the memory resources of a configuration.
     *
     * @param cfg the configuration to sample
     * @return the new sampled configuration
     */
    @Override
    public Configuration sample(Configuration cfg) {
        Configuration sampled = new DefaultConfiguration();
        for (Node n : cfg.getOnlines()) {
            Node copy = n.clone();//new DefaultNode(n);
            copy.setMemoryCapacity((int) Math.floor(n.getMemoryCapacity() / (double) this.getDivider()));
            sampled.addOnline(copy);
        }
        for (Node n : cfg.getOfflines()) {
            Node copy = n.clone();//new DefaultNode(n);
            copy.setMemoryCapacity((int) Math.floor(n.getMemoryCapacity() / (double) this.getDivider()));
            sampled.addOffline(copy);
        }

        for (VirtualMachine vm : cfg.getRunnings()) {
            VirtualMachine copy = vm.clone();//new DefaultVirtualMachine(vm);
            copy.setMemoryConsumption((int) Math.ceil(vm.getMemoryConsumption() / (double) this.getDivider()));
            sampled.setRunOn(copy, sampled.getOnlines().get(cfg.getLocation(vm).getName()));
        }

        for (VirtualMachine vm : cfg.getSleepings()) {
            VirtualMachine copy = vm.clone();//new DefaultVirtualMachine(vm);
            copy.setMemoryConsumption((int) Math.ceil(vm.getMemoryConsumption() / (double) this.getDivider()));
            sampled.setSleepOn(copy, sampled.getOnlines().get(cfg.getLocation(vm).getName()));
        }

        for (VirtualMachine vm : cfg.getWaitings()) {
            VirtualMachine copy = vm.clone();//new DefaultVirtualMachine(vm);
            copy.setMemoryConsumption((int) Math.ceil(vm.getMemoryConsumption() / (double) this.getDivider()));
            sampled.addWaiting(copy);
        }

        return sampled;
    }
}
