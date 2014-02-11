package gipad.configuration.configuration;

import java.util.List;

public class VirtualMachine implements IVirtualMachine {
	private org.discovery.DiscoveryModel.model.VirtualMachine vm;
	
	@Override
	public double getMemoryCurrentUsage() {
		// TODO Stub de la méthode généré automatiquement
		return 0;
	}

	@Override
	public double getMemoryUsage() {
		// TODO Stub de la méthode généré automatiquement
		return 0;
	}

	@Override
	public List<NetworkInterface> getNetworkInterfaces() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public String name() {
		return vm.name();
	}

	@Override
	public long[] getCoreUsage() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public List<Core> getCores() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

}
