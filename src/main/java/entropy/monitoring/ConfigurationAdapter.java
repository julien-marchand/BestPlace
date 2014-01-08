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

package entropy.monitoring;

import java.util.LinkedList;
import java.util.List;

import entropy.configuration.Configuration;
import entropy.configuration.Node;

/**
 * Extract a configuration from a monitoring system.
 *
 * @author Fabien Hermenier
 */
public abstract class ConfigurationAdapter {

    /**
     * the black list for the nodes.
     */
    private List<String> nodesBl;

    /**
     * the white list for the nodes.
     */
    private List<String> nodesWl;

    /**
     * Default step  to round CPU capacity and consumption.
     */
    public static final int DEFAULT_CPU_STEP = 1;

    /**
     * Make a new Configuration adapter.
     */
    public ConfigurationAdapter() {
        this.nodesBl = new LinkedList<String>();
        this.nodesWl = new LinkedList<String>();
    }

    /**
     * Get the current configuration.
     *
     * @return a consistent configuration
     * @throws MonitoringException if an error occurs
     */
    public abstract Configuration extractConfiguration() throws MonitoringException;

    /**
     * Return the closest number, with a certain step.
     *
     * @param nb   the number
     * @param step the step
     * @return a number
     */
    public static int round(int nb, int step) {
        int div = nb / step;
        int up = (div + 1) * step;
        int down = div * step;
        if ((up - nb) < (nb - down)) {
            return up;
        }
        return down;
    }

    /**
     * Get a CPU value using current metrics and default step.
     *
     * @param mhz   The CPU frequency
     * @param nbCPU the number of CPU allocated
     * @return round(mhzxnbCPU, DEFAULT_CPU_STEP);
     */
    public static int getCPUCapacity(int mhz, int nbCPU) {
        return round(mhz * nbCPU, DEFAULT_CPU_STEP);
    }

    /**
     * Get the CPU consumption of a virtual machine. The virtual machine must have 1 virtual CPU.
     *
     * @param cpuPct the percentage of CPU consumed by the virtual machine
     * @param host   the host of the virtual machine
     * @return the CPU consumption
     */
    public static int getCPUConsumption(float cpuPct, Node host) {
        return round((int) (cpuPct * host.getCPUCapacity() / host.getNbOfCPUs() / 100), DEFAULT_CPU_STEP);
    }

    /**
     * Set the nodes black list.
     *
     * @param nodes the list
     */
    public void setNodesBlackList(List<String> nodes) {
        this.nodesBl = nodes;
    }

    /**
     * Get the nodes black list.
     *
     * @return a list, that may be empty
     */
    public List<String> getNodesBlackList() {
        return this.nodesBl;
    }

    /**
     * Set the nodes white list.
     *
     * @param nodes the list
     */
    public void setNodesWhiteList(List<String> nodes) {
        this.nodesWl = nodes;
    }

    /**
     * Get the nodes white list.
     *
     * @return a list, that may be empty
     */
    public List<String> getNodesWhiteList() {
        return this.nodesWl;
    }
}
