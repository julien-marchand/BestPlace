package gipad.configuration.configuration;

import java.util.List;

public interface INode {

	//general
	
	String name();

	int getId();

	List<VirtualMachine> vms();

	//CPU
	List<Core> getCores();
	
	long[] getCoresCapacities();
	
	//Memory
	long[] getMemCapacities();

	// Network
	List<NetworkInterface> getNetworkInterfaces();
	
	long[] getNetworkInCapacities();

	long[] getNetworkOutCapacities();}
