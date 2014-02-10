package gipad.tools;

import org.discovery.DiscoveryModel.model.Cpu;
import org.discovery.DiscoveryModel.model.HardwareSpecification;
import org.discovery.DiscoveryModel.model.NetworkInterface;
import org.discovery.DiscoveryModel.model.Node;
import org.discovery.DiscoveryModel.model.VirtualMachine;

public class DataCalculator {
	public static double getSumCpuUsage(HardwareSpecification hs) {
		long sum = 0;
		for (Cpu cpu : hs.cpus()) {
			sum += cpu.usage();
		}
		return sum;
	}
	
	public static double getSumNetwork
	
	public static double getSumNetworkUsage(VirtualMachine vm) {
		long sum = 0;
		for (NetworkInterface ni : vm.hardwareSpecification().networkInterfaces()) {
			sum += ni.nbCores() * cpu.getCpuCapacity();
		}
		return sum;
	}
}