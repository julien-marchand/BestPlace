package gipad.vjob;

import gipad.configuration.SimpleManagedElementList;
import gipad.placementconstraint.PlacementConstraint;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.discovery.DiscoveryModel.model.Node;
import org.discovery.DiscoveryModel.model.VirtualMachine;

public class VJobImpl implements VJob{

	@Override
	public String id() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public SimpleManagedElementList<VirtualMachine> getVirtualMachines() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public SimpleManagedElementList<Node> getNodes() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public boolean addConstraint(PlacementConstraint c) {
		// TODO Stub de la méthode généré automatiquement
		return false;
	}

	@Override
	public boolean removeConstraint(PlacementConstraint c) {
		// TODO Stub de la méthode généré automatiquement
		return false;
	}

	@Override
	public List<PlacementConstraint> getConstraints() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public boolean addVirtualMachines(SimpleManagedElementList<VirtualMachine> e) {
		// TODO Stub de la méthode généré automatiquement
		return false;
	}

	@Override
	public boolean addVirtualMachine(VirtualMachine vm) {
		// TODO Stub de la méthode généré automatiquement
		return false;
	}

	@Override
	public boolean addNodes(SimpleManagedElementList<Node> e) {
		// TODO Stub de la méthode généré automatiquement
		return false;
	}

	@Override
	public List<SimpleManagedElementList<Node>> getMultiNodeSets() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public SimpleManagedElementList<Node> getMultiNodeSet(String var) {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public List<SimpleManagedElementList<VirtualMachine>> getMultiVirtualMachineSets() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public SimpleManagedElementList<VirtualMachine> getMultiVirtualMachineSet(
			String var) {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public List<SimpleManagedElementList<Node>> getNodeSets() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public SimpleManagedElementList<Node> getNodeSet(String var) {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public List<SimpleManagedElementList<VirtualMachine>> getVirtualMachineSets() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public SimpleManagedElementList<VirtualMachine> getVirtualMachineSet(
			String var) {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public Collection<String> getVariables() {
		// TODO Stub de la méthode généré automatiquement
		return null;
	}

	@Override
	public void store(File f) throws IOException {
		// TODO Stub de la méthode généré automatiquement
		
	}

}
