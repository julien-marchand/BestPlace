package gipad.configuration.configuration;

import java.util.List;

public interface IVirtualMachine {

	double getMemoryCurrentUsage();

	double getMemoryUsage();

	List<Core> getCores();

	List<NetworkInterface> getNetworkInterfaces();

	String name();

	long[] getCoreUsage();
}