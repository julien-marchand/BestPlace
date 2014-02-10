package gipad.configuration.configuration;

import java.util.List;

public interface VirtualMachine {

	double getMemoryCurrentUsage();

	double getMemoryUsage();

	List<Core> getCores();

	List<NetworkInterface> getNetworkInterfaces();

	String name();

}