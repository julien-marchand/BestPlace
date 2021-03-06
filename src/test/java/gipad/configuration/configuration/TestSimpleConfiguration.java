package gipad.configuration.configuration;

import gipad.tools.ManagedElementList;
import gipad.tools.SimpleManagedElementList;

import java.util.ArrayList;

import junit.framework.Assert;

import org.discovery.DiscoveryModel.model.HardwareSpecification;
import org.discovery.DiscoveryModel.model.NetworkInterface;
import org.discovery.DiscoveryModel.model.Node;
import org.discovery.DiscoveryModel.model.Units;
import org.discovery.DiscoveryModel.model.VirtualMachine;
import org.junit.Test;

public class TestSimpleConfiguration {
	
	SimpleConfiguration conf = new SimpleConfiguration();
	final ManagedElementList<VirtualMachine> vms = new SimpleManagedElementList<VirtualMachine>();
	ManagedElementList<Node> nodes = new SimpleManagedElementList<Node>();
	
	{
		for (int i = 0; i < 10; i++) {
			
			vms.add(new VirtualMachine("vm" + i, null, null));
//			new NetworkSpecification(new ArrayList)
		}
		
		nodes.add(new Node("node0", generateNetworkInterface(10), null, new ArrayList<VirtualMachine>() {
			{
				add(vms.get(0));
				add(vms.get(1));
			}
		}));
		nodes.add(new Node("node1", generateNetworkInterface(100), null, new ArrayList<VirtualMachine>() {
			{
				add(vms.get(2));
				add(vms.get(3));
			}
		}));
		nodes.add(new Node("node2", generateNetworkInterface(100), null, new ArrayList<VirtualMachine>() {
			{
				add(vms.get(4));
				add(vms.get(5));
				add(vms.get(6));
			}
		}));
		nodes.add(new Node("node3", generateNetworkInterface(100), null, new ArrayList<VirtualMachine>() {
			{
				add(vms.get(7));
				add(vms.get(8));
				add(vms.get(9));
			}
		}));
		
		conf.addOnline(nodes);
	}
	
	HardwareSpecification generateNetworkInterface(final int i) {
		return new HardwareSpecification(null, new ArrayList<NetworkInterface>() {
			{
				add(new NetworkInterface("lan0", i * Units.MEGA()));
			}
		}, null, null);
	}
	
	@Test
	public void getNodeBandwith() {
		long val = conf.getBandwidth(nodes.get(0), nodes.get(2));
		Assert.assertEquals(10l, val);
	}
	
	@Test
	public void getVMBandwidth() {		
		long val = conf.getBandwidth(vms.get(4), vms.get(5));
		Assert.assertEquals(100l, val);
	}
}
