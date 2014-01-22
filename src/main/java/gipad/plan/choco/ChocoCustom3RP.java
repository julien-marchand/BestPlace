
package gipad.plan.choco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.discovery.DiscoveryModel.model.VirtualMachine;

import solver.Solver;
import solver.constraints.Constraint;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.variables.IntVar;
import util.ESat;
import gipad.placementconstraint.*;
import gipad.configuration.CostFunction;
import gipad.configuration.ManagedElementList;
import gipad.configuration.SimpleManagedElementList;
import gipad.configuration.configuration.Configuration;
import gipad.configuration.configuration.Configurations;
import gipad.plan.*;
import gipad.plan.action.Action;



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
	
    /** TODO
     * Get some Stats about the model
     * @return
     */
    /*public List<SolutionStatistics> getSolutionsStatistics() {
        if (model == null) {
            return new ArrayList<SolutionStatistics>();
        }
        return this.model.getSolutionsStatistics();
    }

    /**
     * @return some statistics about the solving process
     *//*
    public SolvingStatistics getSolvingStatistics() {
        if (model == null) {
            return SolvingStatistics.getStatisticsForNotSolvingProcess();
        }
        return model.getSolvingStatistics();
    }*/
	

	
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
	        
	        model = new DefaultReconfigurationProblem(src, vms, this.costFunc);
	        
	        System.currentTimeMillis();
	        Map<Class, Integer> occurences = new HashMap<Class, Integer>();
	      
	        //Inject placement constraints
	        // A pretty print of the problem
	         
	        /**
	         * globalCost is equals to the sum of each action costs.
	         */
	        IntVar globalCost = model.createBoundIntVar("globalCost", 0, Choco.MAX_UPPER_BOUND);
	        List<Action> allActions = new ArrayList<Action>();
	        allActions.addAll(model.getVirtualMachineActions());
	        allActions.addAll(model.getNodeMachineActions());
	        IntVar[] allCosts = ActionModels.extractCosts(allActions);
	        List<IntVar> varCosts = new ArrayList<IntVar>();
	        for (int i = 0; i < allCosts.length; i++) {
	            IntVar c = allCosts[i];
	            if (c.instantiated() && c.getValue() == 0) {
	            } else {
	                varCosts.add(c);
	            }
	        }
	        IntVar[] costs = varCosts.toArray(new IntVar[varCosts.size()]);
	        //model.post(model.eq(globalCost, /*model.sum(costs)*/explodedSum(model, costs, 200, true)));
	       
	          
	        //Setting total duration bounds of all the variables and updating the UB of the variables -- à la main 

	        //TODO: Set the LB for the horizon && the end of each action
	        //cs = model.leq(model.getEnd(), explodedSum(model, ActionModels.extractDurations(allActions), 200, true));
	        //costConstraints.add(cs);
	        //model.post(cs);

	        if (getTimeLimit() > 0) {
	            SearchMonitorFactory.limitTime(model.getSolver(), getTimeLimit() * 1000);
	        }
	        //Configure search : Heuristics + Objectiv
	        model.getSolver().findSolution();//launch();
	        Boolean ret = model.getSolver().isFeasible()==ESat.TRUE;
	        if (ret == null) {
	            throw new PlanException("Unable to check wether a solution exists or not");
	        } else {
	            if (Boolean.FALSE.equals(ret)) {
	                throw new PlanException("No solution");
	            } else {
	                SequencedReconfigurationPlan plan = model.extractSolution();
	                Configuration res = plan.getDestination();
	                if (Configurations.futureOverloadedNodes(res).size() != 0) {
	                    throw new PlanException("Resulting configuration is not viable: Overloaded nodes=" + Configurations.futureOverloadedNodes(res));
	                }

	                int cost = 0;
	                for (Action a : plan) {
	                    cost += a.getFinishMoment();
	                }
	                if (cost != globalCost.getValue()) {
	                    throw new PlanException("Practical cost of the plan (" + cost + ") and objective (" + globalCost.getValue() + ") missmatch:\n" + plan);
	                }
	               //Verify all Placement constraints are satisfied
	                return plan;
	            }
	        }
	        
		return null;
	}

}
