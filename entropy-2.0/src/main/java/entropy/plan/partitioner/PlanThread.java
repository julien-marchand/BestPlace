/*
 * Copyright (c) Fabien Hermenier
 *
 *        This file is part of Entropy.
 *
 *        Entropy is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU Lesser General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        Entropy is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU Lesser General Public License for more details.
 *
 *        You should have received a copy of the GNU Lesser General Public License
 *        along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.plan.partitioner;

import java.util.ArrayList;
import java.util.List;

import entropy.configuration.Configuration;
import entropy.configuration.Configurations;
import entropy.configuration.ConfigurationsException;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.PlanException;
import entropy.plan.SolutionStatistics;
import entropy.plan.SolvingStatistics;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.choco.ChocoCustomRP;
import entropy.plan.durationEvaluator.DurationEvaluator;
import entropy.vjob.BasicVJob;
import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJob;

/**
 * Wrap a solving process into a Thread.
 *
 * @author Fabien Hermenier
 */
public class PlanThread extends Thread {

    private ChocoCustomRP m;

    private ManagedElementSet<VirtualMachine> run;
    private ManagedElementSet<VirtualMachine> wait;
    private ManagedElementSet<VirtualMachine> sleep;
    private ManagedElementSet<VirtualMachine> stop;
    private ManagedElementSet<Node> on;
    private ManagedElementSet<Node> off;
    private List<VJob> queue;
    private Configuration cfg;

    private TimedReconfigurationPlan plan;

    private PlanException ex = null;

    private boolean repair;

    private int timeout;

    private SolvingStatistics solvingStats;

    private List<SolutionStatistics> solutions;

    public void setRepairMode(boolean mode) {
        repair = mode;
    }

    public void setTimeout(int t) {
        this.timeout = t;
    }

    /**
     * Compute a new DefaultTimedReconfigurationPlan that satisfy all the constraints applied to the model.
     *
     * @param part  The partition to use
     * @param eval  the duration estimation of the action
     * @param src   The source configuration. It must be viable.
     * @param run   The set of virtual machines that must be running at the end of the process
     * @param wait  The set of virtual machines that must be waiting at the end of the process
     * @param sleep The set of virtual machines that must be sleeping at the end of the process
     * @param stop  The set of virtual machines that must be terminated at the end of the process
     * @param on    The set of nodes that must be online at the end of the process
     * @param off   The set of nodes that must be offline at the end of the process
     */
    public PlanThread(Partition part,
                      DurationEvaluator eval,
                      Configuration src,
                      ManagedElementSet<VirtualMachine> run,
                      ManagedElementSet<VirtualMachine> wait,
                      ManagedElementSet<VirtualMachine> sleep,
                      ManagedElementSet<VirtualMachine> stop,
                      ManagedElementSet<Node> on,
                      ManagedElementSet<Node> off) throws ConfigurationsException, PlanException {

        //Divide the original configuration
        cfg = Configurations.subConfiguration(src, part.getVirtualMachines(), part.getNodes());
        solutions = new ArrayList<SolutionStatistics>();
        //Divide the vms
        this.run = run.clone();
        this.wait = wait.clone();
        this.sleep = sleep.clone();
        this.stop = stop.clone();
        this.on = on.clone();
        this.off = off.clone();

        this.run.retainAll(part.getVirtualMachines());
        this.wait.retainAll(part.getVirtualMachines());
        this.sleep.retainAll(part.getVirtualMachines());
        this.stop.retainAll(part.getVirtualMachines());
        this.off.retainAll(part.getNodes());
        this.on.retainAll(part.getNodes());

        m = new ChocoCustomRP(eval);
        VJob vj = new BasicVJob(getRunID());
        for (PlacementConstraint x : part.getConstraints()) {
            vj.addConstraint(x);
        }
        queue = new ArrayList<VJob>();
        queue.add(vj);
    }

    /**
     * Get statistics of the computed solutions
     *
     * @return a list of statistics thatmay be empty
     */
    public List<SolutionStatistics> getSolutionsStatistics() {
        if (m != null) {
            return m.getSolutionsStatistics();
        }
        return solutions;
    }

    /**
     * Get the statistics about the solving process
     *
     * @return some statistics
     */
    public SolvingStatistics getSolvingStatistics() {
        if (m != null) {
            return m.getSolvingStatistics();
        }
        return solvingStats;
    }

    @Override
    public void run() {
        try {
            m.setRepairMode(repair);
            m.setTimeLimit(timeout);
            plan = m.compute(cfg, run, wait, sleep, stop, on, off, queue);

            //Copy statistics to liberate memory more easily (no more references to the problem)
            solvingStats = m.getSolvingStatistics().clone();
            solutions.clear();
            for (SolutionStatistics st : m.getSolutionsStatistics()) {
                solutions.add(st.clone());
            }
            //Plan.logger.debug(getRunID() + " " + plan.toString());
            m = null;
        } catch (PlanException e) {
            this.ex = e;
        } finally {
            m = null;
        }
    }

    /**
     * Get the exception that may occurred during the solving process
     *
     * @return an exception or {@code null} if no exception occurred
     */
    public PlanException getException() {
        return ex;
    }

    /**
     * Get the computed plan
     *
     * @return a plan or {@code null} if no plan was computed
     */
    public TimedReconfigurationPlan getResultingPlan() {
        return plan;
    }

    /**
     * Get the id of the solving process.
     *
     * @return a String
     */
    public String getRunID() {
        return new StringBuilder("Partition ").toString();
    }

    @Override
	public String toString() {
        return getRunID();
    }
}
