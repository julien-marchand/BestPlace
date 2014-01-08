/*
 * Copyright (c) Fabien Hermenier
 *
 * This file is part of Entropy.
 *
 * Entropy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Entropy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */
package entropy.plan.choco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import choco.Choco;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.search.ISolutionPool;
import choco.kernel.solver.search.SolutionPoolFactory;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Configuration;
import entropy.configuration.Configurations;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.plan.CustomizablePlannerModule;
import entropy.plan.Plan;
import entropy.plan.PlanException;
import entropy.plan.SolutionStatistics;
import entropy.plan.SolvingStatistics;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.action.Action;
import entropy.plan.choco.actionModel.ActionModel;
import entropy.plan.choco.actionModel.ActionModels;
import entropy.plan.choco.actionModel.VirtualMachineActionModel;
import entropy.plan.choco.actionModel.slice.Slice;
import entropy.plan.durationEvaluator.DurationEvaluationException;
import entropy.plan.durationEvaluator.DurationEvaluator;
import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJob;

/**
 * A CustomizablePlannerModule based on Choco.
 *
 * @author Fabien Hermenier
 */
public class ChocoCustomRP extends CustomizablePlannerModule {

    private List<SConstraint> costConstraints;

    /**
     * The model.
     */
    private ReconfigurationProblem model;

    private boolean repair = true;

    private List<VJob> queue;

    /**
     * Make a new plan module.
     *
     * @param eval to evaluate the duration of the actions.
     */
    public ChocoCustomRP(DurationEvaluator eval) {
        super(eval);
        costConstraints = new LinkedList<SConstraint>();
    }

    /**
     * Get the model.
     *
     * @return the model to express constraints.
     */
    public ReconfigurationProblem getModel() {
        return this.model;
    }

    @Override
    public List<SolutionStatistics> getSolutionsStatistics() {
        if (model == null) {
            return new ArrayList<SolutionStatistics>();
        }
        return this.model.getSolutionsStatistics();
    }

    /**
     * @return some statistics about the solving process
     */
    @Override
    public SolvingStatistics getSolvingStatistics() {
        if (model == null) {
            return SolvingStatistics.getStatisticsForNotSolvingProcess();
        }
        return model.getSolvingStatistics();
    }

    @SuppressWarnings("deprecation")
	@Override
    public TimedReconfigurationPlan compute(Configuration src,
                                            ManagedElementSet<VirtualMachine> run,
                                            ManagedElementSet<VirtualMachine> wait,
                                            ManagedElementSet<VirtualMachine> sleep,
                                            ManagedElementSet<VirtualMachine> stop,
                                            ManagedElementSet<Node> on,
                                            ManagedElementSet<Node> off,
                                            List<VJob> q) throws PlanException {

        long st = System.currentTimeMillis();
        queue = q;

        model = null;

        ManagedElementSet<VirtualMachine> vms = null;
        if (repair) {
            //Look for the VMs to consider
            vms = new SimpleManagedElementSet<VirtualMachine>();
            for (VJob v : queue) {
                for (PlacementConstraint c : v.getConstraints()) {
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
        System.currentTimeMillis();
        //System.err.println(run);
        //System.err.println(vms);
        //System.err.println(on);
        //System.err.println(off);
        model = new DefaultReconfigurationProblem(src, run, wait, sleep, stop, vms, on, off, this.getDurationEvaluator());
        System.currentTimeMillis();
        //System.err.println((t2 - t1) + " ms to make the core RP");
        Map<Class, Integer> occurences = new HashMap<Class, Integer>();
        int nbConstraints = 0;

        System.currentTimeMillis();
        for (VJob vjob : queue) {
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
            Plan.logger.debug("#nodes= " + model.getNodeCount() +
                    ", #backtracks= " + model.getBackTrackCount() +
                    ", #duration= " + model.getTimeCount() +
                    ", #nbsol= " + model.getNbSolutions());
            if (Boolean.FALSE.equals(ret)) {
                throw new PlanException("No solution");
            } else {
                TimedReconfigurationPlan plan = model.extractSolution();
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
                for (VJob vjob : queue) {
                    for (PlacementConstraint c : vjob.getConstraints()) {
                        if (!c.isSatisfied(res)) {
                            throw new PlanException("Resulting configuration does not satisfy '" + c.toString() + "'");
                        }
                    }
                }
                return plan;
            }
        }
    }

    /**
     * Estimate the lower and the upper bound of model.getEnd()
     *
     * @param totalDuration the totalDuration of all the action
     * @throws entropy.plan.durationEvaluator.DurationEvaluationException
     *          if an error occured during evaluation of the durations.
     */
    private void setTotalDurationBounds(IntDomainVar totalDuration, ManagedElementSet<VirtualMachine> vms) throws DurationEvaluationException {
        for (VirtualMachine vm : vms) { //FIXME: Bad, should consider the real actions
            getDurationEvaluator().evaluateMigration(vm);
        }
        int sup = ReconfigurationProblem.MAX_TIME;//Math.min(maxEnd, ReconfigurationProblem.MAX_TIME);
        int min = 0;
        try {
            model.getEnd().setInf(min);
            model.getEnd().setSup(sup);
            totalDuration.setInf(min);
            totalDuration.setSup(sup);
        } catch (Exception e) {
            Plan.logger.warn(e.getMessage(), e);
        }
        Plan.logger.debug(totalDuration.pretty());
        Plan.logger.debug(model.getEnd().pretty());
    }

    /**
     * Update the upper bounds of all the variable to simplify the problem.
     */
    private void updateUB() {
        int ub = model.getEnd().getSup();
        List<ActionModel> allActionModels = new LinkedList<ActionModel>(model.getNodeMachineActions());
        allActionModels.addAll(model.getVirtualMachineActions());

        try {
            for (VirtualMachineActionModel a : model.getVirtualMachineActions()) {
                if (a.end().getSup() > ub) {
                    a.end().setSup(ub);
                }
                if (a.start().getSup() > ub) {
                    a.start().setSup(ub);
                }

                if (a.getGlobalCost().getSup() > ub) {
                    a.getGlobalCost().setSup(ub);
                }

                Slice task = a.getDemandingSlice();
                if (task != null) {
                    if (task.end().getSup() > ub) {
                        task.end().setSup(ub);
                    }
                    if (task.start().getSup() > ub) {
                        task.start().setSup(ub);
                    }
                    if (task.duration().getSup() > ub) {
                        task.duration().setSup(ub);
                    }
                }

                task = a.getConsumingSlice();
                if (task != null) {
                    if (task.end().getSup() > ub) {
                        task.end().setSup(ub);
                    }
                    if (task.start().getSup() > ub) {
                        task.start().setSup(ub);
                    }
                    if (task.duration().getSup() > ub) {
                        task.duration().setSup(ub);
                    }
                }
            }
        } catch (Exception e) {
            Plan.logger.warn(e.getMessage(), e);
        }
    }

    /**
     * Get all the vjobs managed by the module
     *
     * @return a list of vjobs, may be empty
     */
    public List<VJob> getQueue() {
        return queue;
    }

    /**
     * Use the repair mode.
     *
     * @param b {@code true} to use the repair mode
     */
    public void setRepairMode(boolean b) {
        this.repair = b;
    }


    /**
     * Make a sum of a large number of variables using
     * decomposition
     *
     * @param m    the model
     * @param vars the variables to sum
     * @param step the size of the subsums.
     * @return the variable storing the result of the sum.
     */
    private IntExp explodedSum(ReconfigurationProblem m, IntDomainVar[] vars, int step, boolean post) {
        int s = vars.length > step ? step : vars.length;
        IntDomainVar[] subSum = new IntDomainVar[s];
        int nbSubs = (int) Math.ceil(vars.length / step);
        if (vars.length % step != 0) {
            nbSubs++;
        }
        IntDomainVar[] ress = new IntDomainVar[nbSubs];

        int curRes = 0;
        int shiftedX = 0;
        for (int i = 0; i < vars.length; i++) {
            subSum[shiftedX++] = vars[i];
            if (shiftedX == subSum.length) {
                IntDomainVar subRes = m.createBoundIntVar("subSum[" + (i - shiftedX + 1) + ".." + i + "]", 0, ReconfigurationProblem.MAX_TIME);
                SConstraint c = m.eq(subRes, m.sum(subSum));
                if (post) {
                    m.post(c);
                } else {
                    costConstraints.add(c);
                }
                ress[curRes++] = subRes;
                if (i != vars.length - 1) {
                    int remainder = vars.length - (i + 1);
                    s = remainder > step ? step : remainder;
                    subSum = new IntDomainVar[s];
                }
                shiftedX = 0;
            }
        }
        return m.sum(ress);
    }

    public List<SConstraint> getCostConstraints() {
        return this.costConstraints;
    }
}
