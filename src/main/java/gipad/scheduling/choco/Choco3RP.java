package gipad.scheduling.choco;

import java.io.IOException;

import entropy.configuration.DefaultConfiguration;
import entropy.configuration.parser.FileConfigurationSerializerFactory;
import scheduling.EntropyProperties;
import gipad.configuration.*;
import gipad.configuration.configuration.*;
import gipad.plan.choco.ChocoCustom3RP;
import gipad.scheduling.AbstractScheduler;

public class Choco3RP extends AbstractScheduler{
	
	private ChocoCustom3RP planner;
	
	public Choco3RP(Configuration initialConfiguration, CostFunction costFunc){
		super(initialConfiguration);
		planner =  new ChocoCustom3RP(costFunc);
		
		planner.setRepairMode(true); //true by default for ChocoCustomRP/Entropy2.1; false by default for ChocoCustomPowerRP/Entrop2.0
		planner.setTimeLimit(EntropyProperties.getEntropyPlanTimeout());
		
		try {
            String fileName = "logs/entropy/configuration/" + "-"+ System.currentTimeMillis() + ".txt";
            FileConfigurationSerializerFactory.getInstance().write((DefaultConfiguration) initialConfiguration, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }	
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
