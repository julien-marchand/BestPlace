package gipad.tools;

import org.discovery.DiscoveryModel.model.VirtualMachine;

public interface Core {
	ManagedElementList<VirtualMachine> getVM();
	double getCapacity();
}
