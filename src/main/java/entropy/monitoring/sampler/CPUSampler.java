/*
 * Copyright (c) 2009 Ecole des Mines de Nantes.
 * 
 *     This file is part of Entropy.
 * 
 *     Entropy is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     Entropy is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 * 
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
*/
package entropy.monitoring.sampler;

import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.SimpleConfiguration;
import entropy.configuration.VirtualMachine;

/**
 * Sample the CPU resources of nodes and VMs involved in a configuration.
 * <p/>
 * <p/>
 * For nodes, CPU capacity is rounded to the nearest lower integer that divide the parameter.
 * For VMs, CPU consumption is rounded to the nearest upper integer that divide the parameter.
 *
 * @author Fabien Hermenier
 */
public class CPUSampler implements ConfigurationSampler {

    /**
     * The divider to use.
     */
    private int divider;

    /**
     * Make a new CPUSampler using a specific divider.
     *
     * @param div the divider to use
     */
    public CPUSampler(int div) {
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

    @Override
    public Configuration sample(Configuration cfg) {
        Configuration sampled = new SimpleConfiguration();
        for (Node n : cfg.getOnlines()) {
            Node copy = n.clone();//new DefaultNode(n);
            copy.setCPUCapacity((int) Math.floor(n.getCPUCapacity() / (double) this.getDivider()));
            sampled.addOnline(copy);
        }
        for (Node n : cfg.getOfflines()) {
            Node copy = n.clone();//new DefaultNode(n);
            copy.setCPUCapacity((int) Math.floor(n.getCPUCapacity() / (double) this.getDivider()));
            sampled.addOffline(copy);
        }

        for (VirtualMachine vm : cfg.getRunnings()) {
            VirtualMachine copy = vm.clone();//new DefaultVirtualMachine(vm);
            copy.setCPUConsumption((int) Math.ceil(vm.getCPUConsumption() / (double) this.getDivider()));
            sampled.setRunOn(copy, sampled.getOnlines().get(cfg.getLocation(vm).getName()));
        }

        for (VirtualMachine vm : cfg.getSleepings()) {
            VirtualMachine copy = vm.clone();//new DefaultVirtualMachine(vm);
            copy.setCPUConsumption((int) Math.ceil(vm.getCPUConsumption() / (double) this.getDivider()));
            sampled.setSleepOn(copy, sampled.getOnlines().get(cfg.getLocation(vm).getName()));
        }

        for (VirtualMachine vm : cfg.getWaitings()) {
            VirtualMachine copy = vm.clone();//new DefaultVirtualMachine(vm);
            copy.setCPUConsumption((int) Math.ceil(vm.getCPUConsumption() / (double) this.getDivider()));
            sampled.addWaiting(copy);
        }

        return sampled;
    }
}
