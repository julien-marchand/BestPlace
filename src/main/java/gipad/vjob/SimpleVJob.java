package gipad.vjob;

import gipad.configuration.ManagedElementList;
import gipad.placementconstraint.PlacementConstraint;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.discovery.DiscoveryModel.model.Node;
import org.discovery.DiscoveryModel.model.VirtualMachine;

public class SimpleVJob implements VJob{

	private ManagedElementList<VirtualMachine> vms;
	
	private ManagedElementList<Node> nodes;
	
	private List<PlacementConstraint> constraints;
	
	private String id;
	
	@Override
	public String id() {
		return id;
	}

	@Override
	public ManagedElementList<VirtualMachine> getVirtualMachines() {
		return vms;
	}

	@Override
	public ManagedElementList<Node> getNodes() {
		return nodes;
	}

	@Override
	public boolean addConstraint(PlacementConstraint c) {
		constraints.add(c);
		return true;
	}

	@Override
	public boolean removeConstraint(PlacementConstraint c) {
		return constraints.remove(c);
	}

	@Override
	public List<PlacementConstraint> getConstraints() {
		return constraints;
	}

	@Override
	public boolean addVirtualMachines(ManagedElementList<VirtualMachine> e) {
		return vms.addAll(e);
	}

	@Override
	public boolean addVirtualMachine(VirtualMachine vm) {
		return vms.add(vm);
	}

	@Override
	public boolean addNodes(ManagedElementList<Node> e) {
		return nodes.addAll(e);
	}

	@Override
	public List<ManagedElementList<Node>> getMultiNodeSets() {
		return null;
	}

	@Override
	public ManagedElementList<Node> getMultiNodeSet(String var) {
		return null;
	}

	@Override
	public List<ManagedElementList<VirtualMachine>> getMultiVirtualMachineSets() {
		return null;
	}

	@Override
	public ManagedElementList<VirtualMachine> getMultiVirtualMachineSet(String var) {
		return null;
	}

	@Override
	public List<ManagedElementList<Node>> getNodeSets() {
		return null;
	}

	@Override
	public ManagedElementList<Node> getNodeSet(String var) {
		return null;
	}

	@Override
	public List<ManagedElementList<VirtualMachine>> getVirtualMachineSets() {
		return null;
	}

	@Override
	public ManagedElementList<VirtualMachine> getVirtualMachineSet(String var) {
		return null;
	}

	@Override
	public Collection<String> getVariables() {
		return null;
	}

	@Override
	public void store(File f) throws IOException {
		
	}

}
