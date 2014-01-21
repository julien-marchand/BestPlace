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
 * Simplify the configuration if the VMs are "CPU burning".
 * If all the nodes have the same CPU capacities, then it becomes
 * equals to the number of physical CPUs.
 * <p/>
 * Moreover, if the cpu consumption
 * of a virtual machine is below a threshold, the simplificaion makes it consumes a entire physical CPU.
 * Otherwise, it consumption is considered as neglictible and becomes equal to 0
 *
 * @author Fabien Hermenier
 */
public class CPUBurningVMsSampler implements ConfigurationSampler {

    /**
     * The CPU consumption threshold to use.
     */
    private int threshold;

    /**
     * Make a new sampler with a specific threshold
     *
     * @param th the threshold to use. > 0
     */
    public CPUBurningVMsSampler(int th) {
        this.threshold = th;
    }

    /**
     * Get the threshold.
     *
     * @return the threshold.
     */
    public int getThreshold() {
        return this.threshold;
    }

    @Override
    public Configuration sample(Configuration c) {
        Configuration newConf = new SimpleConfiguration();
        for (Node n : c.getOnlines()) {
            Node copyNode = n.clone();//new DefaultNode(n);
            copyNode.setCPUCapacity(copyNode.getNbOfCPUs());
            newConf.addOnline(copyNode);
        }
        for (Node n : c.getOfflines()) {
            Node copyNode = n.clone();//new DefaultNode(n);
            copyNode.setCPUCapacity(copyNode.getNbOfCPUs());
            newConf.addOffline(copyNode);
        }

        for (VirtualMachine vm : c.getRunnings()) {
            VirtualMachine copy = vm.clone();//new DefaultVirtualMachine(vm);
            if (copy.getCPUConsumption() > this.getThreshold()) {
                copy.setCPUConsumption(copy.getNbOfCPUs());
            } else {
                copy.setCPUConsumption(0);
            }
            newConf.setRunOn(copy, newConf.getOnlines().get(c.getLocation(vm).getName()));
        }


        for (VirtualMachine vm : c.getWaitings()) {
            VirtualMachine copy = vm.clone();//new DefaultVirtualMachine(vm);
            if (copy.getCPUConsumption() > this.getThreshold()) {
                copy.setCPUConsumption(copy.getNbOfCPUs());
            } else {
                copy.setCPUConsumption(0);
            }
            newConf.addWaiting(copy);
        }

        for (VirtualMachine vm : c.getSleepings()) {
            VirtualMachine copy = vm.clone();//new DefaultVirtualMachine(vm);
            if (copy.getCPUConsumption() > this.getThreshold()) {
                copy.setCPUConsumption(copy.getNbOfCPUs());
            } else {
                copy.setCPUConsumption(0);
            }
            newConf.setSleepOn(copy, newConf.getOnlines().get(c.getLocation(vm).getName()));
        }
        return newConf;
    }

}
