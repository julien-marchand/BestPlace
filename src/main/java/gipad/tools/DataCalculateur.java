package gipad.tools;

import org.discovery.DiscoveryModel.model.Cpu;
import org.discovery.DiscoveryModel.model.Node;

public class DataCalculateur {
	public static int getSumCPu(Node n) {
		int sum = 0;
		for (Cpu cpu : n.hardwareSpecification().cpus()) {
			sum += cpu.nbCores() * cpu.getCpuCapacity();
		}
		return sum;
	}
}