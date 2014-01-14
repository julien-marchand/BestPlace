package gipad.plan.choco;

import java.util.List;
import org.discovery.DiscoveryModel.model.*;

import choco.kernel.solver.constraints.SConstraint;
import entropy.plan.choco.ReconfigurationProblem;

import gipad.vjob.VJob;
import gipad.plan.CustomizablePlannerModule;

public class ChocoCustom3RP {
	
	private List<SConstraint> costConstraints;

    /**
     * The model.
     */
    private ReconfigurationProblem model;

    private boolean repair = true;

    private List<VJob> queue;
    
	public ChocoCustom3RP() {
		super();
	}

}
