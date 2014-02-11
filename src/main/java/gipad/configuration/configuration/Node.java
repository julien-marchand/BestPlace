package gipad.configuration.configuration;

import java.util.ArrayList;
import java.util.List;

public class Node implements INode {
	
	private static int current_id = 0;
	
	private org.discovery.DiscoveryModel.model.Node node;

	private int id;

	public Node(org.discovery.DiscoveryModel.model.Node node) {
		this.node = node;
		//FIXME Bag things
		this.id = current_id++;
	}
	
	@Override
	public String name() {
		return node.name();
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public List<IVirtualMachine> getVms() {
		List<IVirtualMachine> res = new ArrayList<IVirtualMachine>();
		for (org.discovery.DiscoveryModel.model.VirtualMachine vm : node.vms())
			res.add(new VirtualMachine(vm));
		return res;
	}

	@Override
	public long[] getCoreCapacities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long[] getMemCapacities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long[] getNetworkInCapacities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long[] getNetworkOutCapacities() {
		// TODO Auto-generated method stub
		return null;
	}

}
