package gipad.configuration.configuration;

import gipad.tools.ManagedElementList;
import gipad.tools.SimpleManagedElementList;

import java.util.List;

public class Node implements INode {
	private org.discovery.DiscoveryModel.model.Node node;
	
	@Override
	public List<Core> getCores() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public int getId() {
		// TODO
		return 0;
	}

	@Override
	public ManagedElementList<VirtualMachine> vms() {
		// TODO
		return null;
	}

	@Override
	public String name() {
		return node.name();
	}

	@Override
	public List<NetworkInterface> getNetworkInterfaces() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public long[] getCoresCapacities() {
		return null;
	}

	@Override
	public long[] getMemCapacities() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public long[] getNetworkInCapacities() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public long[] getNetworkOutCapacities() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

}
