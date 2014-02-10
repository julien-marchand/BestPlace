package gipad.tools;

import org.discovery.DiscoveryModel.model.Cpu;
import org.discovery.DiscoveryModel.model.HardwareSpecification;
import org.discovery.DiscoveryModel.model.Node;

public class DataCalculateur {
	public static int getSumCPu(Node n) {
		int sum = 0;
		for (Cpu cpu : n.hardwareSpecification().cpus()) {
			sum += cpu.nbCores() * cpu.getCpuCapacity();
		}
		return sum;
	}

	public static double getSumCpuCurrentUsage(
			HardwareSpecification hardwareSpecification) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static double getSumOutUsage(
			HardwareSpecification hardwareSpecification) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static double getSumCpuUsage(
			HardwareSpecification hardwareSpecification) {
		// TODO Auto-generated method stub
		return 0;
	}
}