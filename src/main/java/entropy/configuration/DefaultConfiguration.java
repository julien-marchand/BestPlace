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
package entropy.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * A configuration is an overview of a cluster. It shows all the virtual
 * machines and the nodes involved in the configuration and the assignment
 * of the virtual machines to the nodes.
 * <br/>
 * Each of the node is either <b>online</b> or <b>offline</b>.
 * <br/>
 * Each of the virtual machines can be either:
 * <ul>
 * <li><b>running</b> on an online node</li>
 * <li><b>waiting</b> on a <i>farm</i> to be launched</li>
 * <li><b>sleeping</b> on a node. In this state, the image of the virtual machine is stored on
 * the node</li>
 * </ul>
 * <p/>
 * TODO: Better getOverloadedNodesX().
 *
 * @author Fabien Hermenier
 */
public class DefaultConfiguration implements Configuration {

    /**
     * The VirtualMachines running on each node.
     */
    private Map<Node, ManagedElementSet<VirtualMachine>> invRunnings;

    /**
     * The VirtualMachines sleeping on each node.
     */
    private Map<Node, ManagedElementSet<VirtualMachine>> invSleepings;

    /**
     * The Node that host each VirtualMachine.
     */
    private Map<VirtualMachine, Node> affects;

    /**
     * List of nodes that are onlines.
     */
    private ManagedElementSet<Node> onlines;

    /**
     * List of nodes that are offlines.
     */
    private ManagedElementSet<Node> offlines;

    /**
     * List of virtual machines that are waiting.
     */
    private ManagedElementSet<VirtualMachine> waitings;

    /**
     * List of virtual machines that are running.
     */
    private ManagedElementSet<VirtualMachine> runnings;

    /**
     * List of the virtual machines that are sleeping.
     */
    private ManagedElementSet<VirtualMachine> sleepings;


    /**
     * The current list associated to each virtual machine.
     */
    private Map<VirtualMachine, ManagedElementSet<VirtualMachine>> currentList;

    /**
     * Instantiate a new empty Configuration.
     */
    public DefaultConfiguration() {

        this.currentList = new HashMap<VirtualMachine, ManagedElementSet<VirtualMachine>>();

        this.invRunnings = new HashMap<Node, ManagedElementSet<VirtualMachine>>();
        this.invSleepings = new HashMap<Node, ManagedElementSet<VirtualMachine>>();

        this.affects = new HashMap<VirtualMachine, Node>();

        this.onlines = new DefaultManagedElementSet<Node>();
        this.offlines = new DefaultManagedElementSet<Node>();

        this.waitings = new DefaultManagedElementSet<VirtualMachine>();
        this.runnings = new DefaultManagedElementSet<VirtualMachine>();
        this.sleepings = new DefaultManagedElementSet<VirtualMachine>();
    }

    /**
     * Copy constructor.
     * Only the assignements are cloned
     *
     * @param ref The reference Configuration
     */
    public DefaultConfiguration(Configuration ref) {
        this();
        for (Node n : ref.getOfflines()) {
            addOffline(n);
        }
        for (Node n : ref.getOnlines()) {
            addOnline(n);
            for (VirtualMachine vm : ref.getRunnings(n)) {
                setRunOn(vm, n);
            }
            for (VirtualMachine vm : ref.getSleepings(n)) {
                setSleepOn(vm, n);
            }
            for (VirtualMachine vm : ref.getWaitings()) {
                addWaiting(vm);
            }
        }
    }

    /**
     * Set a virtual machine running on a node. The node must already be online.
     * If the virtual machine is already in a other location or state in the configuration, it is updated
     *
     * @param vm   the virtual machine
     * @param node the node that will host the virtual machine. Must be considered as online.
     * @return true if the vm is assigned on the node. False otherwise
     */
    @Override
	public boolean setRunOn(VirtualMachine vm, Node node) {
        if (this.getOnlines().contains(node)) {
            ManagedElementSet<VirtualMachine> list = this.currentList.get(vm);
            if (list != null) {
                Node oldNode = this.affects.get(vm);
                if (list == this.runnings) {
                    this.getRunnings(oldNode).remove(vm);
                } else if (list == this.sleepings) {
                    this.sleepings.remove(vm);
                    this.getSleepings(oldNode).remove(vm);
                } else if (list == this.waitings) {
                    this.waitings.remove(vm);
                }
            }
            //Impact on the reverse affectations
            this.runnings.add(vm);
            this.currentList.put(vm, this.runnings);
            this.affects.put(vm, node);
            this.getRunnings(node).add(vm);
            return true;
        }
        return false;
    }

    /**
     * Set a virtual machine sleeping on a node.
     * If the virtual machine is already in a other location or state in the configuration, it is updated
     *
     * @param vm   the virtual machine
     * @param node the node that will host the virtual machine. Must be considered as online.
     * @return false if the hosting node is offline or unknown
     */
    @Override
	public boolean setSleepOn(VirtualMachine vm, Node node) {
        if (this.getOnlines().contains(node)) {
            ManagedElementSet<VirtualMachine> list = this.currentList.get(vm);
            if (list != null) {
                Node oldNode = this.affects.get(vm);
                if (list == this.sleepings) {
                    this.getSleepings(oldNode).remove(vm);
                } else if (list == this.runnings) {
                    this.getRunnings(oldNode).remove(vm);
                    this.getRunnings().remove(vm);
                } else if (list == this.waitings) {
                    this.waitings.remove(vm);
                }
            }
            this.sleepings.add(vm);
            this.currentList.put(vm, this.sleepings);

            this.affects.put(vm, node);
            this.getSleepings(node).add(vm);
            return true;
        }
        return false;
    }

    /**
     * Set a virtual machine waiting.
     * If the virtual machine is already in a other location or state in the configuration, it is updated
     *
     * @param vm the virtual machine
     */
    @Override
	public void addWaiting(VirtualMachine vm) {
        ManagedElementSet<VirtualMachine> list = this.currentList.get(vm);
        if (list != null) {
            //We have to remove reverse assignment for consistency
            if (list == this.runnings) {
                this.invRunnings.get(this.affects.get(vm)).remove(vm);
            } else if (list == this.sleepings) {
                this.invSleepings.get(this.affects.get(vm)).remove(vm);
            }
            //No need to remove the key in this.affects.
            list.remove(vm);
        }
        this.waitings.add(vm);
        this.currentList.put(vm, this.waitings);
    }

    /**
     * Remove a virtual machine.
     *
     * @param vm the virtual machine to remove
     */
    @Override
	public void remove(VirtualMachine vm) {
        ManagedElementSet<VirtualMachine> list = this.currentList.get(vm);
        if (list != null) {
            //We have to remove reverse assignment for consistency
            if (list == this.runnings) {
                this.invRunnings.get(this.affects.get(vm)).remove(vm);
            } else if (list == this.sleepings) {
                this.invSleepings.get(this.affects.get(vm)).remove(vm);
            }
            //No need to remove the key in this.affects.
            list.remove(vm);
        }
        this.runnings.remove(vm);
        this.currentList.remove(vm);
    }

    /**
     * Get the list of nodes that are online.
     *
     * @return a list, may be empty
     */
    @Override
	public ManagedElementSet<Node> getOnlines() {
        return this.onlines;
    }

    /**
     * Set a node online. If the node is already in the configuration but in an another state, it is updated.
     *
     * @param node the node to add
     */
    @Override
	public void addOnline(Node node) {
        this.offlines.remove(node);
        this.onlines.add(node);
        if (this.invRunnings.get(node) == null) {
            this.invRunnings.put(node, new DefaultManagedElementSet<VirtualMachine>());
        }
        if (this.invSleepings.get(node) == null) {
            this.invSleepings.put(node, new DefaultManagedElementSet<VirtualMachine>());
        }
    }

    /**
     * Set a node offline. If the node is already in the configuration but in an another state, it is updated.
     * The node must not host any virtual machines
     *
     * @param node the node
     * @return true if the node is offline. False otherwise
     */
    @Override
	public boolean addOffline(Node node) {
        if (this.invRunnings.get(node) == null) {
            this.invRunnings.put(node, new DefaultManagedElementSet<VirtualMachine>());
        }
        if (this.invSleepings.get(node) == null) {
            this.invSleepings.put(node, new DefaultManagedElementSet<VirtualMachine>());
        }
        if (this.getRunnings(node).size() == 0
                && this.getSleepings(node).size() == 0) {
            this.onlines.remove(node);
            return this.offlines.add(node);
        } else {
            return false;
        }
    }


    /**
     * Get the nodes that are offline.
     *
     * @return a list of nodes, may be empty
     */
    @Override
	public ManagedElementSet<Node> getOfflines() {
        return this.offlines;
    }


    /**
     * Get the virtual machines that are running.
     *
     * @return a set of VirtualMachines, may be empty
     */
    @Override
	public ManagedElementSet<VirtualMachine> getRunnings() {
        return this.runnings;
    }

    /**
     * Get the virtual machines that are sleeping.
     *
     * @return a set of virtual machines, may be empty
     */
    @Override
	public ManagedElementSet<VirtualMachine> getSleepings() {
        return this.sleepings;
    }

    /**
     * Get the virtual machines that are sleeping on a node.
     *
     * @param n the node
     * @return a set of virtual machines, may be empty
     */
    @Override
	public ManagedElementSet<VirtualMachine> getSleepings(Node n) {
        return this.invSleepings.get(n);
    }

    /**
     * Get the virtual machines that are running on a node.
     *
     * @param n the node
     * @return a set of virtual machines, may be empty
     */
    @Override
	public ManagedElementSet<VirtualMachine> getRunnings(Node n) {
        return this.invRunnings.get(n);
    }

    /**
     * Get the location of a sleeping virtual machine.
     *
     * @param vm the virtual machine
     * @return its host, or null if the virtual machine is not defined as sleeping
     */
    @Override
	public Node getSleepingLocation(VirtualMachine vm) {
        if (this.sleepings.contains(vm)) {
            return this.affects.get(vm);
        }
        return null;
    }

    /**
     * Get the virtual machines that are waiting.
     *
     * @return a list, may be empty
     */
    @Override
	public ManagedElementSet<VirtualMachine> getWaitings() {
        return this.waitings;
    }

    /**
     * Get all the virtual machines involved in the configuration.
     *
     * @return a set, may be empty
     */
    @Override
	public ManagedElementSet<VirtualMachine> getAllVirtualMachines() {
        ManagedElementSet<VirtualMachine> all = new DefaultManagedElementSet<VirtualMachine>();
        all.addAll(getRunnings());
        all.addAll(getWaitings());
        all.addAll(getSleepings());
        return all;
    }

    /**
     * Get all the nodes involved in the configuration.
     *
     * @return a set, may be empty
     */
    @Override
	public ManagedElementSet<Node> getAllNodes() {
        ManagedElementSet<Node> all = new DefaultManagedElementSet<Node>();
        all.addAll(getOnlines());
        all.addAll(getOfflines());
        return all;
    }

    /**
     * Return the node that host a running virtual machine.
     *
     * @param vm The VirtualMachine
     * @return The node that host the VirtualMachine or null if
     *         the virtual machine is not defined as running
     */
    @Override
	public Node getRunningLocation(VirtualMachine vm) {
        if (this.runnings.contains(vm)) {
            return this.affects.get(vm);
        }
        return null;
    }


    /**
     * Return the subset of nodes that host at least one running VirtualMachine.
     *
     * @return subset of node that may be empty
     */
    public ManagedElementSet<Node> getUsedNodes() {
        ManagedElementSet<Node> set = new DefaultManagedElementSet<Node>();
        for (Node n : this.getOnlines()) {
            if (this.getRunnings(n).size() > 0) {
                set.add(n);
            }
        }
        return set;
    }

    /**
     * Return the subset of nodes that host no running VirtualMachine.
     *
     * @return a subset of node that may be empty
     */
    public ManagedElementSet<Node> getUnusedNodes() {
        ManagedElementSet<Node> set = new DefaultManagedElementSet<Node>();
        for (Node n : this.getOnlines()) {
            if (this.getRunnings(n).size() == 0) {
                set.add(n);
            }
        }
        return set;
    }

    /**
     * Retrun the subset of nodes that are currently overloaded.
     * A node is considered as overloaded if the total memory or CPU currently
     * consumed by the virtual machines it hosts is greater than its memory or CPU capacity.
     *
     * @return a subset of nodes, may be empty.
     */
    public ManagedElementSet<Node> getCurrentlyOverloadedNodes() {
        ManagedElementSet<Node> nodes = new DefaultManagedElementSet<Node>();
        for (Node n : this.getOnlines()) {
            int cpuCapa = n.getCPUCapacity();
            int memCapa = n.getMemoryCapacity();
            for (VirtualMachine vm : invRunnings.get(n)) {
                cpuCapa -= vm.getCPUConsumption();
                memCapa -= vm.getMemoryConsumption();
                if (cpuCapa < 0 || memCapa < 0) {
                    nodes.add(n);
                    break;
                }
            }
        }
        return nodes;
    }

    /**
     * Check wether the current configuration is overloaded or not.
     *
     * @return true if at least one node is overloaded
     */
    public boolean isCurrentlyConsistent() {
        for (Node n : this.getOnlines()) {
            int cpuCapa = n.getCPUCapacity();
            int memCapa = n.getMemoryCapacity();
            for (VirtualMachine vm : invRunnings.get(n)) {
                cpuCapa -= vm.getCPUConsumption();
                memCapa -= vm.getMemoryConsumption();
                if (cpuCapa < 0 || memCapa < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Retuun the subset of nodes that can not satisfy the resource demand of the VMs.
     * A node is considered as overloaded if the total memory or CPU demand
     * of the virtual machines it hosts is greater than its memory or CPU capacity.
     *
     * @return a subset of nodes, may be empty.
     */
    public ManagedElementSet<Node> getUnacceptableNodes() {
        ManagedElementSet<Node> nodes = new DefaultManagedElementSet<Node>();
        for (Node n : this.getOnlines()) {
            int cpuCapa = n.getCPUCapacity();
            int memCapa = n.getMemoryCapacity();
            for (VirtualMachine vm : getRunnings(n)) {
                cpuCapa -= vm.getCPUDemand();
                memCapa -= vm.getMemoryDemand();
                if (cpuCapa < 0 || memCapa < 0) {
                    nodes.add(n);
                    break;
                }
            }
        }
        return nodes;
    }

    /**
     * Get a configuration that use all the nodes but only a subset of
     * virtual machines.
     *
     * @param vms the subset of virtual machines
     * @return a new Configuration
     */
    public Configuration getSubConfiguration(ManagedElementSet<VirtualMachine> vms) {
        Configuration c = new DefaultConfiguration();
        for (Node n : this.getOnlines()) {
            c.addOnline(n);
        }
        for (Node n : this.getOfflines()) {
            c.addOffline(n);
        }
        for (VirtualMachine vm : vms) {
            Node n = this.getRunningLocation(vm);
            if (n != null) {
                c.setRunOn(vm, n);
            } else {
                n = this.getSleepingLocation(vm);
                if (n != null) {
                    c.setSleepOn(vm, n);
                } else {
                    c.addWaiting(vm);
                }
            }
        }
        return c;
    }

    /**
     * Textual representation of the configuration.
     *
     * @return the textual representation
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (Node n : getOnlines()) {
            buf.append(n.getName());
            buf.append(":");
            for (VirtualMachine vm : this.getRunnings(n)) {
                buf.append(" ");
                buf.append(vm.getName());
            }
            if (this.getSleepings(n).size() > 0) {

                for (VirtualMachine vm : this.getSleepings(n)) {
                    buf.append(" (");
                    buf.append(vm.getName());
                    buf.append(")");
                }
            }
            buf.append("\n");
        }
        for (Node n : getOfflines()) {
            buf.append("(");
            buf.append(n.getName());
            buf.append(")\n");
        }
        buf.append("FARM");
        for (VirtualMachine vm : this.getWaitings()) {
            buf.append(" ");
            buf.append(vm.getName());
        }
        buf.append("\n");
        return buf.toString();
    }

    /**
     * Check the equality of two configuration.
     *
     * @param o The object to compare with
     * @return true if o is a Configuration, if both configuration have the same virtual machines and nodes
     *         and if the assignment of the virtual machines is the same
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o instanceof Configuration) {
            Configuration ref = (Configuration) o;
            //Check the states of each component
            //Check presence, not order!
            if (this.getRunnings().equals(ref.getRunnings())
                    && this.getSleepings().equals(ref.getSleepings())
                    && this.getWaitings().equals(ref.getWaitings())
                    && this.getOnlines().equals(ref.getOnlines())
                    && this.getOfflines().equals(ref.getOfflines())
                    ) {
                //Check the assignements of VMs
                for (Node n : this.getOnlines()) {
                    if (!this.getRunnings(n).equals(ref.getRunnings(n))) {
                        return false;
                    } else if (!this.getSleepings(n).equals(ref.getSleepings(n))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (Node n : this.getOnlines()) {
            hashCode += getRunnings(n).hashCode() * 31;
            hashCode += getSleepings(n).hashCode() * 31;
        }
        hashCode += getWaitings().hashCode() * 31;
        return hashCode;
    }

    /**
     * Test if a node is online.
     *
     * @param n the node
     * @return true if the node is online
     */
    @Override
	public boolean isOnline(Node n) {
        return this.getOnlines().contains(n);
    }

    /**
     * Test if a node is offline.
     *
     * @param n the node
     * @return true if the node is offline
     */
    @Override
	public boolean isOffline(Node n) {
        return this.getOfflines().contains(n);
    }


    /**
     * Test if a virtual machine is running.
     *
     * @param vm the virtual machine
     * @return true if the virtual machine is running
     */
    @Override
	public boolean isRunning(VirtualMachine vm) {
        return this.getRunnings().contains(vm);
    }

    /**
     * Test if a virtual machine is waiting.
     *
     * @param vm the virtual machine
     * @return true if the virtual machine is waiting
     */
    @Override
	public boolean isWaiting(VirtualMachine vm) {
        return this.getWaitings().contains(vm);
    }

    /**
     * Test if a virtual machine is sleeping.
     *
     * @param vm the virtual machine
     * @return true if the virtual machine is sleeping
     */
    @Override
	public boolean isSleeping(VirtualMachine vm) {
        return this.getSleepings().contains(vm);
    }

    /**
     * Get all the virtual machines running on a set of nodes.
     *
     * @param ns the set of nodes
     * @return a set of virtual machines, may be empty
     */
    @Override
	public ManagedElementSet<VirtualMachine> getRunnings(ManagedElementSet<Node> ns) {
        ManagedElementSet<VirtualMachine> vms = new DefaultManagedElementSet<VirtualMachine>();
        for (Node n : ns) {
            vms.addAll(getRunnings(n));
        }
        return vms;
    }

    @Override
    public boolean remove(Node n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getLocation(VirtualMachine vm) {
        if (isRunning(vm)) {
            return getRunningLocation(vm);
        } else if (isSleeping(vm)) {
            return getSleepingLocation(vm);
        }
        return null;
    }

    @Override
    public Configuration clone() {
        return new DefaultConfiguration(this);
    }

    @Override
    public boolean contains(Node n) {
        return getOnlines().contains(n) || getOfflines().contains(n);
    }

    @Override
    public boolean contains(VirtualMachine vm) {
        return getRunnings().contains(vm) || getWaitings().contains(vm) || getSleepings().contains(vm);
    }
}
