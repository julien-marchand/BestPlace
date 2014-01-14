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

package gipad.configuration;

/**
 * Interface to specify a working node.
 * A node has a certain CPU and memory capacity that can be used to run VMs.
 *
 * @author Fabien Hermenier
 */
public interface Node extends ManagedElement {

    /**
     * Return the number of CPUs dedicated for Virtual Machines.
     *
     * @return a positive integer
     */
    int getNbOfCPUs();

    /**
     * Return the capacity of each CPU.
     *
     * @return a positive integer
     */
    int getCPUCapacity();

    /**
     * Return the amount of memory dedicated for Virtual Machines.
     *
     * @return a positive amount
     */
    int getMemoryCapacity();

    /**
     * Set the number of physical CPUs available for the virtual machines.
     *
     * @param nb a positive integer
     */
    void setNbOfCPUs(int nb);

    /**
     * Set the CPU capacity of each CPU of the node available to the virtual machines
     *
     * @param c a positvie integer
     */
    void setCPUCapacity(int c);

    /**
     * Set the memory capacity of the node, available to the virtual machines
     *
     * @param m a positive integer
     */
    void setMemoryCapacity(int m);

    /**
     * Deep copy of the node. All the resources capacity
     * are copied
     *
     * @return a copy of the node
     */
    Node clone();

    /**
     * Get the identifier of the startup driver.
     *
     * @return an identifier. May be null if there is no driver
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    String getStartupDriverID();

    /**
     * Get the identifier of the shutdown driver.
     *
     * @return an identifier. May be null if there is no driver
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    String getShutdownDriverID();

    /**
     * Get the identifier of the migration driver.
     *
     * @return an identifier. May be null if there is no driver
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    String getMigrationDriverID();

    /**
     * Get the identifier of the hypervisor.
     *
     * @return an identifier. May be null if there is no hypervisor associated to this node
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    String getHypervisorID();

    /**
     * Get the MAC Address of this node.
     *
     * @return a MAC address that may be null
     */
    String getMACAddress();

    /**
     * Get the IP address of this node.
     *
     * @return an IP address that may be null
     */
    String getIPAddress();

    /**
     * Associate a startup driver to this node.
     *
     * @param id the identifier of the driver
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    void setStartupDriverID(String id);

    /**
     * Associate a shutdown driver to this node.
     *
     * @param id the identifier of the driver
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    void setShutdownDriverID(String id);

    /**
     * Associate a migration driver to this node.
     *
     * @param id the identifier of the driver
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    void setMigrationDriverID(String id);

    /**
     * Associate an hypervisor to this node.
     *
     * @param id the identifier of the hypervisor
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    void setHypervisorID(String id);

    /**
     * Set the MAC address of this node.
     *
     * @param mac The MAC address
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    void setMACAddress(String mac);

    /**
     * Set the IP address of this node.
     *
     * @param ip the IP address
     */
    void setIPAddress(String ip);

    /**
     * Get the identifier of the suspend driver.
     *
     * @return an identifier. May be null if there is no driver associated to this node
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    String getSuspendDriverID();

    /**
     * Associate a suspend driver to this node.
     *
     * @param id the identifier of the driver
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    void setSuspendDriverID(String id);

    /**
     * Associate a resume driver to this node.
     *
     * @param id the identifier of the driver
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    void setResumeDriverID(String id);

    /**
     * Get the identifier of the resume driver.
     *
     * @return an identifier. May be null if there is no driver associated to this node
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    String getResumeDriverID();

    /**
     * Get the identifier of the run driver.
     *
     * @return an identifier. May be null if there is no driver associated to this node
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    String getRunDriverID();

    /**
     * Associate a run driver to this node.
     *
     * @param id the identifier of the driver
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    void setRunDriverID(String id);

    /**
     * Get the identifier of the stop driver.
     *
     * @return an identifier. May be null if there is no driver associated to this node
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    String getStopDriverID();

    /**
     * Associate a stop driver to this node.
     *
     * @param id the identifier of the driver
     * @deprecated this information should be carried by the execution module
     */
    @Deprecated
    void setStopDriverID(String id);
}
