package gipad.tools;

import gipad.configuration.configuration.Core;
import gipad.configuration.configuration.NetworkInterface;
import gipad.configuration.configuration.VirtualMachine;

public class DataCalculator {

	public static double getSumCpuUsage(VirtualMachine vm) {
		double sum = 0;
		for (Core core : vm.getCores()) {
			sum += core.getUsage();
		}
		return sum;
	}
	
	public static double getSumNetworkUsage(VirtualMachine vm) {
		double sum = 0;

		for (NetworkInterface ni : vm.getNetworkInterfaces()) {
			
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
