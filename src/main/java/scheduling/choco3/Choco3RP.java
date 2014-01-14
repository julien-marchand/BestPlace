package scheduling.choco3;

import entropy.configuration.Configuration;
import entropy.plan.choco.ChocoCustomRP;
import scheduling.AbstractScheduler;

public class Choco3RP extends AbstractScheduler{
	
	private ChocoCustom3RP planner;
	
	public Choco3RP(Configuration initialConfiguration){
		super(initialConfiguration);
		planner =  new ChocoCustom3RP(CostFunction costFunc);
		
	}

	@Override
	public ComputingState computeReconfigurationPlan() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void applyReconfigurationPlan() {
		// TODO Auto-generated method stub
		
	}

}
