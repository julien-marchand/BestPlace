package gipad.configuration.configuration;

import gipad.tools.ManagedElementList;

public interface Node {
	ManagedElementList<Core> getCores();
	int getId();
	long[] getCoresCapacities();
	long[] getMemCapacities();
	long[] getNetworkInCapacities();
	long[] getNetworkOutCapacities();
}
