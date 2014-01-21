package gipad.scheduling.choco;

import org.discovery.DiscoveryModel.model.*;

import gipad.execution.*;
import gipad.plan.action.*;

import gipad.scheduling.EntropyProperties;
import gipad.configuration.*;
import gipad.configuration.configuration.*;
import gipad.execution.SequencedExecutionGraph;
import gipad.plan.choco.*;
import gipad.scheduling.AbstractScheduler;
import gipad.plan.*;

public class Choco3RP extends AbstractScheduler {

	private ChocoCustom3RP planner;

	public Choco3RP(Configuration initialConfiguration, CostFunction costFunc) {
		super(initialConfiguration);
		planner = new ChocoCustom3RP(costFunc);

		planner.setRepairMode(true);
		planner.setTimeLimit(EntropyProperties.getEntropyPlanTimeout());
	}

	@Override
	public ComputingState computeReconfigurationPlan() {
		ComputingState res = ComputingState.VMRP_SUCCESS;

		ManagedElementList<VirtualMachine> queue = initialConfiguration
				.getRunnings();
		timeToComputeVMRP = System.currentTimeMillis();

		try {
			reconfigurationPlan = planner.compute(initialConfiguration, queue);
		} catch (PlanException e) {
			e.printStackTrace();
			res = ComputingState.VMRP_FAILED;
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

}
