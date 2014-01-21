
package gipad.plan.choco;

import java.util.ArrayList;
import java.util.List;

import org.discovery.DiscoveryModel.model.VirtualMachine;

import solver.constraints.Constraint;

import entropy.configuration.Configurations;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.SimpleManagedElementSet;
import entropy.plan.SolutionStatistics;
import entropy.plan.SolvingStatistics;
import entropy.plan.choco.ReconfigurationProblem;
import gipad.placementconstraint.*;
import gipad.configuration.CostFunction;
import gipad.configuration.ManagedElementList;
import gipad.configuration.SimpleManagedElementList;
import gipad.configuration.configuration.Configuration;
import gipad.plan.*;
import gipad.vjob.VJob;



public class ChocoCustom3RP implements Plan{
	
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

    private ManagedElementList<VirtualMachine> queue;
    
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

		 	queue = q;

	        model = null;

	        ManagedElementList<VirtualMachine> vms = null;
	        if (repair) {
	            //Look for the VMs to consider
	        	//We don't have any placement constraint for the moment
	            vms = new SimpleManagedElementList<VirtualMachine>();
	            for (VirtualMachine v : queue) {
	                for (PlacementConstraint c : src.getPlacementConstraints()) {
	                    if (!c.isSatisfied(src)) {
	                        vms.addAll(c.getMisPlaced(src));
	                    }
	                }
	            }
	            //Hardcore way for the packing. TODO: externalize
	            //System.err.println("pack issue:" + src.getRunnings(src.getUnacceptableNodes()));
	            vms.addAll(src.getRunnings(Configurations.futureOverloadedNodes(src)));
	        } else {
	            vms = src.getAllVirtualMachines();
	        }
	        
		return null;
	}

}
