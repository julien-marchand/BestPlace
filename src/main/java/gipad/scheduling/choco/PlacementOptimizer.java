package gipad.scheduling.choco;

import java.util.ArrayList;
import java.util.Collection;

import org.discovery.DiscoveryModel.model.*;

import gipad.exception.PlanException;
import gipad.execution.*;
import gipad.plan.action.*;

import gipad.scheduling.EntropyProperties;
import gipad.configuration.*;
import gipad.configuration.configuration.*;
import gipad.execution.SequencedExecutionGraph;
import gipad.plan.choco.*;
import gipad.scheduling.AbstractScheduler;
import gipad.tools.ManagedElementList;
import gipad.tools.SimpleManagedElementList;
import gipad.plan.*;

/**Scheduling module
 * is using a gipad.plan.choco.ChocoCustom3RP to compute a new configuration based on the initialOne
 * This planner is returning a SequentedReconfigurationPlan which is used in applyReconfigurationPlan()
 * @author Pocman
 *
 */
public class PlacementOptimizer extends AbstractScheduler {

	private ChocoCustom3RP planner;

	public PlacementOptimizer(Configuration initialConfiguration, CostFunction costFunc) {
		super(initialConfiguration);
		planner = new ChocoCustom3RP(costFunc);

		planner.setRepairMode(true);
		planner.setTimeLimit(EntropyProperties.getEntropyPlanTimeout());
	}

	@Override
	public ComputingState computeReconfigurationPlan() {
		ComputingState res = ComputingState.SUCCESS;

		ManagedElementList<VirtualMachine> queue = initialConfiguration.getRunnings();
		
		timeToComputeVMRP = System.currentTimeMillis();

		try {
			reconfigurationPlan = planner.compute(initialConfiguration, queue);
		} catch (PlanException e) {
			e.printStackTrace();
			res = ComputingState.PLACEMENT_FAILED;
			timeToComputeVMRP = System.currentTimeMillis() - timeToComputeVMRP;
			reconfigurationPlan = null;
		}

		if(reconfigurationPlan != null){
			if(reconfigurationPlan.getActions().isEmpty())
				res = ComputingState.NO_RECONFIGURATION_NEEDED;
			
			reconfigurationPlanCost = reconfigurationPlan.getDuration();
			newConfiguration = reconfigurationPlan.getDestination();
			nbMigrations = computeNbMigrations();
			reconfigurationGraphDepth = computeReconfigurationGraphDepth();	
		}		
		return res; 
	}
	
	//Get the number of migrations
	private int computeNbMigrations(){
		int nbMigrations = 0;

		for (Action a : reconfigurationPlan.getActions()){
			if(a instanceof Migration){
				nbMigrations++;
			}
		}
		
		return nbMigrations;
	}
	
	//Get the depth of the reconfiguration graph
	//May be compared to the number of steps in Entropy 1.1.1
	//Return 0 if there is no action, and (1 + maximum number of dependencies) otherwise
	private int computeReconfigurationGraphDepth(){
		if(reconfigurationPlan.getActions().isEmpty()){
			return 0;
		}
		
		else{
			int maxNbDeps = 0;
			SequencedExecutionGraph g = reconfigurationPlan.extractExecutionGraph();
			int nbDeps;
	
			//Set the reverse dependencies map
			for (Dependencies dep : g.extractDependencies()) {
				nbDeps = dep.getUnsatisfiedDependencies().size();
				
				if (nbDeps > maxNbDeps)
					maxNbDeps = nbDeps;
			}
	
			return 1 + maxNbDeps;
		}
	}

	@Override
	public void applyReconfigurationPlan() {
		// TODO Auto-generated method stub

	}
	
	Configuration extractConfiguration(Collection<XHost> xhosts){
		Configuration extractedConf = new SimpleConfiguration();
		
        ManagedElementList<Node> nodes = new SimpleManagedElementList<Node>();
        Node  node = null;

        // Add nodes
        for (XHost tmpH:xhosts){

            //Hardware Specification
            ArrayList<Cpu> cpus = new ArrayList<Cpu>();
            cpus.add(new Cpu(tmpH.getNbCores(), tmpH.getCPUCapacity()/tmpH.getNbCores()));

            HardwareSpecification nodeHardwareSpecification = new HardwareSpecification(
                     cpus,
                    // StorageDevice are not yet implemented within the Simgrid framework
                    new ArrayList<StorageDevice>() {{
                        add(new StorageDevice("hd0", 512 * Units.GIGA()));
                    }},

                    new Memory(tmpH.getMemSize() * Units.MEGA())
            );

            ArrayList<NetworkInterface> nets = new ArrayList<NetworkInterface>();
            nets.add(new NetworkInterface("eth0", tmpH.getNetBW() * Units.MEGA()));
            NetworkSpecification networkSpecification = new NetworkSpecification(nets);

            Location nodeLocation = new Location(tmpH.getIP(), 3000);
            ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>();
            node = new Node(tmpH.getName(), nodeHardwareSpecification, networkSpecification, nodeLocation, vms);

            for(XVM tmpVM:tmpH.getRunnings()) {
                ArrayList<Cpu> cpusVM = new ArrayList<Cpu>();
                cpusVM.add(new Cpu((int)tmpVM.getCore(), 100));
                HardwareSpecification vmHardwareSpecification = new HardwareSpecification(
                        cpusVM,
                        // Not used see above
                        new ArrayList<StorageDevice>() {{
                            add(new StorageDevice("hd0", 100 * Units.GIGA()));
                        }},
                        new Memory(tmpVM.getMemSize()* Units.MEGA())
                );

                ArrayList<NetworkInterface> netsVM = new ArrayList<NetworkInterface>();
                nets.add(new NetworkInterface("eth0", tmpVM.getNetBW() * Units.MEGA()));
                NetworkSpecification networkSpecificationVMs = new NetworkSpecification(netsVM);

                // TODO 1./ Jonathan should add networkSpecification for a VM.
                // TODO 2./ Jonathan should encaspulates networkSpecification into HardwareSpecification (net should appear at
                // the same level than CPU/mem/...
                node.addVm(new VirtualMachine(tmpVM.getName(),  new VirtualMachineStates.Running(), vmHardwareSpecification));
                
            }
            nodes.add(node);
        }
//        extractedConf.addOnline(nodes);
        extractedConf.addOnline(nodes);
        //return nodes;
        return extractedConf;
    }

}
