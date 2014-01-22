
package gipad.plan.choco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.discovery.DiscoveryModel.model.VirtualMachine;

import solver.constraints.Constraint;
import gipad.placementconstraint.*;
import gipad.configuration.CostFunction;
import gipad.configuration.ManagedElementList;
import gipad.configuration.SimpleManagedElementList;
import gipad.configuration.configuration.Configuration;
import gipad.configuration.configuration.Configurations;
import gipad.plan.*;



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
	        int nbConstraints = 0;

	        System.currentTimeMillis();
	        
	            for (PlacementConstraint c : vjob.getConstraints()) {
	                try {
	                    c.inject(model);
	                    if (!occurences.containsKey(c.getClass())) {
	                        occurences.put(c.getClass(), 0);
	                    }
	                    nbConstraints++;
	                    occurences.put(c.getClass(), 1 + occurences.get(c.getClass()));
	                } catch (Exception e) {
	                    Plan.logger.error(e.getMessage(), e);
	                }
	            }
	        
	        System.currentTimeMillis();

	        /*
	         * A pretty print of the problem
	         */
	        //The elements
	        Plan.logger.debug(run.size() + wait.size() + sleep.size() + stop.size() + " VMs: " +
	                run.size() + " will run; " + wait.size() + " will wait; " + sleep.size() + " will sleep; " + stop.size() + " will be stopped");
	        Plan.logger.debug(on.size() + off.size() + " nodes: " + on.size() + " to run; " + off.size() + " to halt");
	        Plan.logger.debug("Manage " + vms.size() + " VMs (" + (repair ? "repair" : "rebuild") + ")");
	        Plan.logger.debug("Timeout is " + getTimeLimit() + " seconds");

	        //The constraints
	        StringBuilder b = new StringBuilder();
	        b.append(nbConstraints + " constraints: ");
	        for (Map.Entry<Class, Integer> e : occurences.entrySet()) {
	            b.append(e.getValue() + " " + e.getKey().getSimpleName() + "; ");
	        }
	        Plan.logger.debug(b.toString());

	        /**
	         * globalCost is equals to the sum of each action costs.
	         */
	        IntDomainVar globalCost = model.createBoundIntVar("globalCost", 0, Choco.MAX_UPPER_BOUND);
	        List<ActionModel> allActions = new ArrayList<ActionModel>();
	        allActions.addAll(model.getVirtualMachineActions());
	        allActions.addAll(model.getNodeMachineActions());
	        IntDomainVar[] allCosts = ActionModels.extractCosts(allActions);
	        List<IntDomainVar> varCosts = new ArrayList<IntDomainVar>();
	        for (int i = 0; i < allCosts.length; i++) {
	            IntDomainVar c = allCosts[i];
	            if (c.isInstantiated() && c.getVal() == 0) {
	            } else {
	                varCosts.add(c);
	            }
	        }
	        IntDomainVar[] costs = varCosts.toArray(new IntDomainVar[varCosts.size()]);
	        //model.post(model.eq(globalCost, /*model.sum(costs)*/explodedSum(model, costs, 200, true)));
	        SConstraint cs = model.eq(globalCost, explodedSum(model, costs, 100, false));
	        costConstraints.add(cs);
	        //model.post(cs);

	        cs = model.leq(model.getEnd(), globalCost);
	        //costConstraints.add(cs);
	        model.post(cs);

	        try {
	            setTotalDurationBounds(globalCost, vms);
	        } catch (DurationEvaluationException e) {
	            throw new PlanException(e.getMessage(), e);
	        }
	        updateUB();

	        //TODO: Set the LB for the horizon && the end of each action
	        //cs = model.leq(model.getEnd(), explodedSum(model, ActionModels.extractDurations(allActions), 200, true));
	        //costConstraints.add(cs);
	        //model.post(cs);

	        if (getTimeLimit() > 0) {
	            model.setTimeLimit(getTimeLimit() * 1000);
	        }
	        //solver.clearGoals();
	        new BasicPlacementHeuristic2(globalCost).add(this);
	        new DummyPlacementHeuristic().add(this.getModel());
	        model.setDoMaximize(false);
	        model.setObjective(globalCost);
	        model.setRestart(false);
	        model.setFirstSolution(false);
	        model.generateSearchStrategy();
	        ISolutionPool sp = SolutionPoolFactory.makeInfiniteSolutionPool(model.getSearchStrategy());
	        model.getSearchStrategy().setSolutionPool(sp);

	        long ed = System.currentTimeMillis();
	        logger.debug((ed - st) + "ms to build the solver " + model.getNbIntConstraints() + " cstr " + model.getNbIntVars() + "+" + model.getNbBooleanVars() + " variables " + model.getNbConstants() + " cte");
	        model.launch();
	        Boolean ret = model.isFeasible();
	        if (ret == null) {
	            throw new PlanException("Unable to check wether a solution exists or not");
	        } else {
	            if (Boolean.FALSE.equals(ret)) {
	                throw new PlanException("No solution");
	            } else {
	                ReconfigurationPlan plan = model.extractSolution();
	                Configuration res = plan.getDestination();
	                if (Configurations.futureOverloadedNodes(res).size() != 0) {
	                    throw new PlanException("Resulting configuration is not viable: Overloaded nodes=" + Configurations.futureOverloadedNodes(res));
	                }

	                int cost = 0;
	                for (Action a : plan) {
	                    cost += a.getFinishMoment();
	                }
	                if (cost != globalCost.getVal()) {
	                    throw new PlanException("Practical cost of the plan (" + cost + ") and objective (" + globalCost.getVal() + ") missmatch:\n" + plan);
	                }
	               //Verify all Placement constraints are satisfier
	                return plan;
	            }
	        }
	        
		return null;
	}

}
