package gipad.plan.choco;

import java.util.List;

import choco.kernel.solver.constraints.SConstraint;
import entropy.plan.choco.ReconfigurationProblem;

import gipad.configuration.Configuration;
import gipad.configuration.ManagedElementSet;
import gipad.configuration.Node;
import gipad.configuration.VirtualMachine;
import gipad.plan.durationEvaluator.DurationEvaluator;
import gipad.vjob.VJob;
import gipad.plan.CustomizablePlannerModule;
import gipad.plan.SolutionStatistics;
import gipad.plan.SolvingStatistics;
import gipad.plan.TimedReconfigurationPlan;

public class chocoCusomizable3RP extends CustomizablePlannerModule {

	public chocoCusomizable3RP(DurationEvaluator eval) {
		super(eval);
		// TODO Stub du constructeur généré automatiquement
	}
	private List<SConstraint> costConstraints;

    /**
     * The model.
     */
    private ReconfigurationProblem model;

    private boolean repair = true;

    private List<VJob> queue;

}
