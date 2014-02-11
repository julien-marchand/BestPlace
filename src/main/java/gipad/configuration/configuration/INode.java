package gipad.configuration.configuration;

import java.util.List;

public interface INode {

	//general
	
	String name();

	int getId();

	//vms
	List<IVirtualMachine> getVms();

	//CPU
//	List<Core> getCores();
	
	long[] getCoreCapacities();
	
	//Memory
	long[] getMemCapacities();

	// Network
//	List<INetworkInterface> getNetworkInterfaces();
	
	long[] getNetworkInCapacities();

	long[] getNetworkOutCapacities();
	
}