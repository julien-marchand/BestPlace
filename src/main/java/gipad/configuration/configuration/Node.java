package gipad.configuration.configuration;

import gipad.tools.ManagedElementList;

public interface Node {
	ManagedElementList<Core> getCores();
	int getId();
	String name();
	long[] getCoresCapacities();
	long[] getMemCapacities();
	long[] getNetworkInCapacities();
	long[] getNetworkOutCapacities();
}
