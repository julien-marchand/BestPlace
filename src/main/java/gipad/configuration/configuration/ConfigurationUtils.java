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

package gipad.configuration.configuration;

import entropy.configuration.ConfigurationsException;
import gipad.tools.DC;
import gipad.tools.ManagedElementList;
import gipad.tools.SimpleManagedElementList;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Common tools related to Configuration
 * 
 * @author Fabien Hermenier
 */
public final class ConfigurationUtils {

	public static enum State {
		/**
		 * Specify running virtual machines.
		 */
		Runnings,

		/**
		 * Specify sleeping virtual machines.
		 */
		Sleepings
	}

	/**
	 * Utility class. No instantiation.
	 */
	private ConfigurationUtils() {
	}

	/**
	 * Return the subset of online nodes that host at least one virtual machines
	 * 
	 * @param cfg
	 *            the configuration to browse
	 * @param wrt
	 *            the hosting type to consider
	 * @return subset of node that may be empty
	 */
	public static ManagedElementList<Node> usedNodes(Configuration cfg, EnumSet<State> wrt) {
		ManagedElementList<Node> ns = new SimpleManagedElementList<Node>();

		if (wrt.contains(State.Runnings)) {
			for (VirtualMachine vm : cfg.getRunnings()) {
				ns.add(cfg.getLocation(vm));
			}
		}
		if (wrt.contains(State.Sleepings)) {
			for (VirtualMachine vm : cfg.getSleepings()) {
				ns.add(cfg.getLocation(vm));
			}
		}

		return ns;
	}

	/**
	 * Return the subset of online nodes that does not host virtual machines.
	 * 
	 * @param cfg
	 *            the configuration to browse
	 * @param wrt
	 *            the hosting type to consider
	 * @return a subset of node that may be empty
	 */
	public static ManagedElementList<Node> unusedNodes(Configuration cfg, State wrt) {
		ManagedElementList<Node> ns = new SimpleManagedElementList<Node>();
		for (Node n : cfg.getOnlines()) {
			if (wrt == State.Runnings && cfg.getRunnings(n).size() == 0) {
				ns.add(n);
			}
			if (wrt == State.Sleepings && cfg.getSleepings(n).size() == 0) {
				ns.add(n);
			}

		}
		return ns;
	}

	/**
	 * Return the subset of nodes that are currently overloaded. A node is
	 * considered as overloaded if the total memory or CPU currently consumed by
	 * the virtual machines it hosts is greater than its memory or CPU capacity.
	 * 
	 * @return a subset of nodes, may be empty.
	 */
	public static ManagedElementList<Node> getOverloadedNodes(Configuration cfg) {
        ManagedElementList<Node> nodes = new SimpleManagedElementList<Node>();
        for (Node n : cfg.getOnlines()) {
            if(isOverloaded(cfg, n)){
            	nodes.add(n);
            	break;
        	}
        }
        return nodes;
    }

	public static boolean isOverloaded(Configuration cfg, Node n){
		long[] coreCapa = n.getCoresCapacities();
	    long[] memCapa = n.getMemCapacities();
	    long[] networkInCapa = n.getNetworkInCapacities();
	    long[] networkOutCapa = n.getNetworkOutCapacities();
	    for (VirtualMachine vm : cfg.getRunnings(n)) {
	        coreCapa = computeFreeUsage(coreCapa, vm.getCoreUsage());
	        memCapa = computeFreeUsage(memCapa, vm.getCoreUsage());
	        networkInCapa = computeFreeUsage(networkInCapa, vm.getCoreUsage());
	        networkOutCapa = computeFreeUsage(networkOutCapa, vm.getCoreUsage());
	        if (isResourceViolated(coreCapa) || isResourceViolated(memCapa) || 
	        		isResourceViolated(networkInCapa) || isResourceViolated(networkOutCapa)) {
	            return true;
	        }
	    }
	    return true;
    }
	
	public static long[] computeFreeUsage(long[] capacities, long[] consumption){		
		for(int i=0; i<capacities.length && i<consumption.length; i++){
			
		}
	}
	
	public static boolean isResourceViolated(long[] resource){
		for (int i = 0; i < resource.length; i++) {
			if(resource[i] < 0)
				return false;
		}
		return true;
	}

	/**
	 * Return the subset of nodes that can not satisfy the resource demand of
	 * the VMs. A node is considered as overloaded if the total memory or CPU
	 * demand of the virtual machines it hosts is greater than its memory or CPU
	 * capacity.
	 * 
	 * @return a subset of nodes, may be empty.
	 */
	/*
	 * public static ManagedElementList<Node>
	 * futureOverloadedNodes(Configuration cfg) { ManagedElementList<Node> nodes
	 * = new SimpleManagedElementList<Node>(); for (Node n : cfg.getOnlines()) {
	 * int sumCpuCapa = 0; for(Cpu cpu : n.hardwareSpecification().cpus()){
	 * sumCpuCapa += cpu.getCpuCapacity(); } double memCapa =
	 * n.hardwareSpecification().memory().capacity(); int networkCapa = 0;
	 * for(NetworkInterface network :
	 * n.networkSpecification().networkInterfaces()){ networkCapa +=
	 * network.maxBandwidth(); } for (VirtualMachine vm : cfg.getRunnings(n)) {
	 * sumCpuCapa -= vm.hardwareSpecification().cpus().get(0); memCapa -=
	 * vm.getMemoryDemand(); networkCapa -= vm; vm. if (cpuCapa < 0 || memCapa <
	 * 0) { nodes.add(n); break; } } } return nodes; }
	 */

	/**
	 * Check whether the current configuration is overloaded or not.
	 * 
	 * @return true if at least one node is overloaded
	 */
	/*
	 * public static boolean isCurrentlyViable(Configuration cfg) { for (Node n
	 * : cfg.getOnlines()) { int cpuCapa = n.getCPUCapacity(); int memCapa =
	 * n.getMemoryCapacity(); for (VirtualMachine vm : cfg.getRunnings(n)) {
	 * cpuCapa -= vm.getCPUConsumption(); memCapa -= vm.getMemoryConsumption();
	 * if (cpuCapa < 0 || memCapa < 0) { return false; } } } return true; }
	 */

	/**
	 * Check whether the current configuration will be overloaded once all the
	 * virtual machines will want to uses their demanded resources.
	 * 
	 * @return true if at least one node is overloaded
	 */
	/*
	 * public static boolean isFutureViable(Configuration cfg) { for (Node n :
	 * cfg.getOnlines()) { int cpuCapa = n.getCPUCapacity(); int memCapa =
	 * n.getMemoryCapacity(); for (VirtualMachine vm : cfg.getRunnings(n)) {
	 * cpuCapa -= vm.getCPUDemand(); memCapa -= vm.getMemoryDemand(); if
	 * (cpuCapa < 0 || memCapa < 0) { return false; } } } return true; }
	 */

	/**
	 * Compute a sub configuration that only consider a subset of nodes and
	 * virtual machines All the virtual machines must be hosted on the subset of
	 * nodes or be in a waiting state
	 * 
	 * @param vms
	 *            the subset of virtual machines
	 * @param nodes
	 *            the subset of nodes
	 * @return a new configuration is the operation succeed, {@code null}
	 *         otherwise
	 */
	public static Configuration subConfiguration(Configuration cfg,
			ManagedElementList<VirtualMachine> vms, ManagedElementList<Node> nodes)
			throws ConfigurationsException {
		Configuration sub = new SimpleConfiguration();
		ManagedElementList<VirtualMachine> cpy = vms.clone();
		for (Node n : nodes) {
			if (cfg.isOffline(n)) {
				sub.addOffline(n);
			} else if (cfg.isOnline(n)) {
				sub.addOnline(n);
				ManagedElementList<VirtualMachine> runs = cfg.getRunnings(n);
				ManagedElementList<VirtualMachine> sleeps = cfg.getSleepings(n);
				if (!cpy.containsAll(runs)) {
					throw new ConfigurationsException(cfg, "VMs on node " + n.name() + "(" + runs
							+ ") does not only contains VMs in " + cpy);
				}
				if (!cpy.containsAll(sleeps)) {
					throw new ConfigurationsException(cfg, "VMs on node " + n.name() + "(" + sleeps
							+ ") does not only contains VMs in " + cpy);
				}

				cpy.removeAll(runs);
				cpy.removeAll(sleeps);

				for (VirtualMachine vm : runs) {
					sub.setRunOn(vm, n);
				}

				for (VirtualMachine vm : sleeps) {
					sub.setSleepOn(vm, n);
				}
			} else {
				throw new ConfigurationsException(cfg, "Unknown node " + n.getName());
			}
		}
		if (!cfg.getWaitings().isEmpty() && !cfg.getWaitings().containsAll(cpy)) {
			throw new ConfigurationsException(cfg, "Waiting VMs (" + cfg.getWaitings()
					+ ") does not only contains VMs in " + cpy);
		}
		for (VirtualMachine vm : cpy) {
			sub.addWaiting(vm);
		}

		return sub;
	}

	/**
	 * Merge a list of Configurations. The set of virtual machines in the
	 * configuration is supposed to be disjoint while if some nodes are presents
	 * in different configurations, there state have to be the same
	 * 
	 * @param cfgs
	 *            the list of configurations to merge
	 * @return the resulting configuration
	 * @throws ConfigurationsException
	 *             if configurations are conflicting between each other
	 */
	public static Configuration merge(List<Configuration> cfgs) throws ConfigurationsException {
		return merge(cfgs.toArray(new Configuration[cfgs.size()]));
	}

	/**
	 * Merge a list of Configurations. The set of virtual machines in the
	 * configuration is supposed to be disjoint while if some nodes are presents
	 * in different configurations, there state have to be the same
	 * 
	 * @param cfgs
	 *            the list of configurations to merge
	 * @return the resulting configuration
	 * @throws ConfigurationsException
	 *             if configurations are conflicting between each other
	 */
	public static Configuration merge(Configuration... cfgs) throws ConfigurationsException {
		Configuration res = new SimpleConfiguration();
		for (Configuration c : cfgs) {
			if (!Collections.disjoint(c.getAllVirtualMachines(), res.getAllVirtualMachines())) {
				throw new ConfigurationsException(c,
						"Set of virtual machines are not disjoint with already merged configurations");
			}
			for (Node n : c.getOfflines()) {
				if (res.isOnline(n)) {
					throw new ConfigurationsException(c, "Conflicting state for node '" + n.name() + "'");
				}
				if (!res.isOffline(n)) {
					res.addOffline(n);
				}
			}
			for (Node n : c.getOnlines()) {
				if (res.isOffline(n)) {
					throw new ConfigurationsException(c, "Conflicting state for node '" + n.name() + "'");
				}
				if (!res.isOnline(n)) {
					res.addOnline(n);
				}
				for (VirtualMachine vm : c.getRunnings(n)) {
					if (!res.setRunOn(vm, n)) {
						throw new ConfigurationsException(c, "Unable to place running '" + vm.name()
								+ "' on '" + n.name() + "'");
					}
				}
				for (VirtualMachine vm : c.getSleepings(n)) {
					if (!res.setSleepOn(vm, n)) {
						throw new ConfigurationsException(c, "Unable to place sleeping '" + vm.name()
								+ "' on '" + n.name() + "'");
					}
				}
			}
			for (VirtualMachine vm : c.getWaitings()) {
				res.addWaiting(vm);
			}
		}
		return res;
	}
}
