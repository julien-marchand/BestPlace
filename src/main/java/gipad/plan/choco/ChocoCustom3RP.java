package gipad.plan.choco;

import java.util.List;

import choco.kernel.solver.constraints.SConstraint;
import entropy.plan.choco.ReconfigurationProblem;
import gipad.vjob.VJob;

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

}
