
package gipad.plan.choco;

import java.util.ArrayList;
import java.util.List;

import org.discovery.DiscoveryModel.model.VirtualMachine;

import solver.constraints.Constraint;

import entropy.plan.SolutionStatistics;
import entropy.plan.SolvingStatistics;
import entropy.plan.choco.ReconfigurationProblem;
import gipad.configuration.CostFunction;
import gipad.configuration.ManagedElementList;
import gipad.configuration.configuration.Configuration;
import gipad.plan.*;
import gipad.vjob.VJob;



public class ChocoCustom3RP implements Plan{
	
//	private Constraint[] costConstraints;
	
	private CostFunction costFunc;

    /**
     * The model.
     */
    private ReconfigurationProblem model;

    /**
     * Mode (repair - consolidate)
     */
    private boolean repair = true;
    
    /**
     * The timeout to limit the solving process.
     */
    private int timeout;

    private List<VJob> queue;
    
    ///////////Constructeur//////////////
    
	public ChocoCustom3RP(CostFunction costFunc) {
		this.costFunc = costFunc;
	}

	
	//////////Getter && Setter///////////
	
	public void setRepairMode(boolean b) {
		this.repair = b;
	}

    /**
     * Get the timelimit to solve the problem.
     *
     * @return the time in seconds
     */
    public int getTimeLimit() {
        return this.timeout;
    }
    
    /**
     * Set the timelimit to solve the problem.
     *
     * @param seconds the time in second
     */
	public void setTimeLimit(int entropyPlanTimeout) {
		this.timeout = entropyPlanTimeout;
	}
	
	/**
	 * Get cost function
	 * @return CostFunction
	 */
	public CostFunction getCostFunction(){
		return this.costFunc;
	}
	
    /**
     * Get the model.
     * Can be null if Compute has not been run yet
     * @return the model to express constraints.
     */
    public ReconfigurationProblem getModel() {
        return this.model;
    }
	
    /**
     * Get some Stats about the model
     * @return
     */
    public List<SolutionStatistics> getSolutionsStatistics() {
        if (model == null) {
            return new ArrayList<SolutionStatistics>();
        }
        return this.model.getSolutionsStatistics();
    }

    /**
     * @return some statistics about the solving process
     */
    public SolvingStatistics getSolvingStatistics() {
        if (model == null) {
            return SolvingStatistics.getStatisticsForNotSolvingProcess();
        }
        return model.getSolvingStatistics();
    }
	

	
	public SequencedReconfigurationPlan compute(Configuration src, ManagedElementList<VirtualMachine> q) throws PlanException {
		// TODO Auto-generated method stub
		return null;
	}

}
