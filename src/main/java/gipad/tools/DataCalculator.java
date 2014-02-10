package gipad.tools;

import gipad.configuration.configuration.VirtualMachine;

public class DataCalculator {
	public static double getSumCpuUsage(HardwareSpecification hs) {
		long sum = 0;
		for (Cpu cpu : hs.cpus()) {
			sum += cpu.usage();
		}
		return sum;
	}
	
	public static double getSumNetwork() {
		return 0;
	}
	
	public static double getSumNetworkUsage(VirtualMachine vm) {
		long sum = 0;

		for (NetworkInterface ni : vm.hardwareSpecification().networkInterfaces()) {
			sum += ni.nbCores() * cpu.getCpuCapacity();
		}
		return sum;
	}
	
	public ManagedElementList<ICore> getCoreList(Node n){
//		for(n.hardwareSpecification().cpus().)
	}
	
	public static int getSumCPu(Node n) {
		int sum = 0;
		for (Cpu cpu : n.hardwareSpecification().cpus()) {
			sum += cpu.nbCores() * cpu.getCpuCapacity();
		}
		return sum;
	}

	public static double getSumCpuCurrentUsage(VirtualMachine vm) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static double getSumOutUsage(VirtualMachine vm) {
		// TODO Auto-generated method stub
		return 0;
	}
}
