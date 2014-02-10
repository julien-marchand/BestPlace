package gipad.configuration.configuration;

import java.util.List;

public interface Node {
	
	List<Core> getCores();

	int getId();

	List<VirtualMachine> vms();

	String name();
}
