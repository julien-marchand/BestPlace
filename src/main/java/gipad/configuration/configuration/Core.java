package gipad.configuration.configuration;

import gipad.tools.ManagedElementList;

public interface Core {
	ManagedElementList<VirtualMachine> getVM();
	double getCapacity();
}
