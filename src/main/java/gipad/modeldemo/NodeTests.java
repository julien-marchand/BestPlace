package gipad.modeldemo;

/* ============================================================
 * Discovery Project - DiscoveryModel
 * http://beyondtheclouds.github.io/
 * ============================================================
 * Copyright 2013 Discovery Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============================================================ */

import gipad.configuration.configuration.VirtualMachine;
import junit.framework.TestCase;

import org.discovery.DiscoveryModel.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NodeTests extends TestCase {

	static HardwareSpecification nodeHardwareSpecification = new HardwareSpecification(
			new ArrayList<Cpu>() {
				{
					add(new Cpu(4, 100));
					add(new Cpu(4, 100));
				}
			},

			new ArrayList<StorageDevice>() {
				{
					add(new StorageDevice("hd0", 512 * Units.GIGA() * Units.BYTE()));
					add(new StorageDevice("hd1", 256 * Units.GIGA()	* Units.BYTE()));
				}
			},

			new Memory(4 * Units.GIGA() * Units.BYTE()));

	static NetworkSpecification networkSpecification = new NetworkSpecification(
			new ArrayList<NetworkInterface>() {
				{
					add(new NetworkInterface("eth0", 1 * Units.GIGA() * Units.BYTE()));
					add(new NetworkInterface("eth1", 100 * Units.MEGA()	* Units.BYTE()));
				}
			});

	static Location nodeLocation = new Location("127.0.0.1", 3000);

	public void testNodeCreation() {

		// These lines illustrate how to create a node

		List<VirtualMachine> vms = new ArrayList<VirtualMachine>() {
			{
				add(new VirtualMachine("vm1", new VirtualMachineStates.Running(), VirtualMachineTests.vmHardwareSpecification));
				add(new VirtualMachine("vm2", new VirtualMachineStates.Running(), VirtualMachineTests.vmHardwareSpecification));
			}
		};

		Node node = new Node("node1", nodeHardwareSpecification, networkSpecification, nodeLocation, vms);
		assert (node != null);
	}

	public void testNodesIteration() {

		// Construction of:
		// a set of 10 nodes where
		// * each node contains 8 virtual machines

		int randomSeed = 1;
		final Random random = new Random(randomSeed);

		List<Node> nodes = new ArrayList<Node>() {
			{

				for (int i = 0; i < 10; i++) {
					final int nodeId = this.size();
					List<VirtualMachine> vms = new ArrayList<VirtualMachine>() {
						{
							for (int i = 0; i < 8; i++) {
								VirtualMachine vm = new VirtualMachine(String.format("vm %d-%d", nodeId, i), new VirtualMachineStates.Running(), VirtualMachineTests.vmHardwareSpecification);

								// setting a random cpu usage
								for (Cpu cpu : vm.hardwareSpecification()
										.cpus()) {
									cpu.setUsage(random.nextDouble());
								}

								add(vm);
							}
						}
					};

					Node node = new Node(String.format("node-%d", nodeId), nodeHardwareSpecification, networkSpecification, nodeLocation, vms);
					this.add(node);
				}
			}
		};

		// iterating over the nodes, to compute to total amount of CPU usage

		int cpuTotalNodes = 0;
		int cpuTotalUsage = 0;

		for (Node node : nodes) {

			for (Cpu cpu : node.hardwareSpecification().cpus()) {
				cpuTotalNodes += cpu.getCpuCapacity();
			}

			for (VirtualMachine vm : node.vms()) {
				for (Cpu cpu : vm.hardwareSpecification().cpus()) {
					cpuTotalUsage += cpu.getCurrentUsage();
				}
			}
		}

		System.out.println(String.format("This set of nodes has a cpu capacity of <%d>", cpuTotalNodes));
		System.out.println(String.format("The overall CPU consumption of the VMs is <%d>", cpuTotalUsage));
	}
}