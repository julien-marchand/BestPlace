package gipad.plan.choco;

import java.util.List;
import org.discovery.DiscoveryModel.model.*;

import choco.kernel.solver.constraints.SConstraint;
import entropy.plan.choco.ReconfigurationProblem;
import gipad.vjob.VJob;
import gipad.configuration.CostFunction;
import gipad.plan.CustomizablePlannerModule;


public class ChocoCustom3RP {
	
	private List<SConstraint> costConstraints;

    /**
     * The model.
     */
    private ReconfigurationProblem model;

    /**
     * Mode (repair - consolidate)
     */
    private boolean repair = true;

    private List<VJob> queue;
    
	public ChocoCustom3RP() {
		super();
	}
	
	public ChocoCustom3RP(CostFunction costFunc) {
		// TODO Auto-generated constructor stub
	}

	public void setRepairMode(boolean b) {
		// TODO Auto-generated method stub
		
	}

	public void setTimeLimit(int entropyPlanTimeout) {
		// TODO Auto-generated method stub
		
	}

}
