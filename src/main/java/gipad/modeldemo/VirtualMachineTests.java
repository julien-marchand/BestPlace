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

import junit.framework.TestCase;
import org.discovery.DiscoveryModel.model.*;

import java.util.ArrayList;

public class VirtualMachineTests extends TestCase {

	// static final int BYTE = 8;
	// static final int KILO = 1024;
	// static final int MEGA = 1024 * KILO;
	// static final int GIGA = 1024 * MEGA;

	static HardwareSpecification vmHardwareSpecification = new HardwareSpecification(
			new ArrayList<Cpu>() {
				{
					add(new Cpu(1, 100));
				}
			},

			new ArrayList<StorageDevice>() {
				{
					add(new StorageDevice("hd0", 100 * Units.GIGA() * Units.BYTE()));
				}
			},

			new Memory(512 * Units.MEGA() * Units.BYTE())
	);

	public void testVirtualMachineStatesEquality() {

		// Two instances of <Running> state should be equals
		VirtualMachineState aFirstRunningState = new VirtualMachineStates.Running();
		VirtualMachineState aSecondRunningState = new VirtualMachineStates.Running();

		assert (aFirstRunningState.isEquals(aSecondRunningState));
		assert (aSecondRunningState.isEquals(aFirstRunningState));
	}

	public void testVirtualMachineStatesDifference() {

		// Two instances of <Running> and <Pause> state should be different
		NormalVirtualMachineState runningState = new VirtualMachineStates.Running();
		NormalVirtualMachineState pausedState = new VirtualMachineStates.Paused();

		assertFalse(runningState.isEquals(pausedState));
		assertFalse(pausedState.isEquals(runningState));
	}

	public void testVirtualMachineCreation() {

		// These lines illustrate how to create a virtual machine
		VirtualMachine vm = new VirtualMachine("vm1",
				new VirtualMachineStates.Running(), vmHardwareSpecification);
		assert (vm != null);
	}
}