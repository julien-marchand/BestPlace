
package gipad.plan.choco;

import java.util.List;

import org.discovery.DiscoveryModel.model.VirtualMachine;

import entropy.plan.SolutionStatistics;
import entropy.plan.SolvingStatistics;
import entropy.plan.choco.ReconfigurationProblem;
import gipad.configuration.CostFunction;
import gipad.configuration.ManagedElementList;
import gipad.configuration.configuration.Configuration;
import gipad.plan.*;
import gipad.vjob.VJob;



public class ChocoCustom3RP implements Plan{
	
	private Constraint[] costConstraints;
	
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
    
	public ChocoCustom3RP(CostFunction costFunc) {
		this.costFunc = costFunc;
	}

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
	
	public CostFunction getCostFunction(){
		return this.costFunc;
	}
	
    /**
     * Get statistics about the computed solutions.
     * Solutions are sorted in an ascending duration.
     *
     * @return a list of statistics that may be empty
     */
    public  List<SolutionStatistics> getSolutionsStatistics(){
    	return null;
    }

    /**
     * Get statistics about the solving process.
     *
     * @return some statistics
     */
    public SolvingStatistics getSolvingStatistics(){
    	return null;
    }
	

	
	public SequencedReconfigurationPlan compute(Configuration src, ManagedElementList<VirtualMachine> q) throws PlanException {
		// TODO Auto-generated method stub
		return null;
	}

}
