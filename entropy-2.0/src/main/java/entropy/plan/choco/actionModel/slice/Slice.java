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
package entropy.plan.choco.actionModel.slice;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;
import entropy.plan.Plan;
import entropy.plan.choco.ReconfigurationProblem;

/**
 * Model a period where a managed element is consuming CPU and memory resources during
 * a bounded amount of time on a node.
 *
 * @author Fabien Hermenier
 */
public class Slice {

    private TaskVar task;

    /**
     * Indicates the identifier of slice hoster.
     */
    private IntDomainVar hoster;

    /**
     * The CPU height of the slice.
     */
    private int cpuHeight;

    /**
     * The memory height of the slice.
     */
    private int memHeight;

    private String name;

    /**
     * Make a new slice.
     *
     * @param name      the name of the slice
     * @param h         the hoster of the slice (its identifier)
     * @param t         The associated task variable
     * @param cpuHeight the CPU height of the slice
     * @param memHeight the memory height of the slice
     */
    public Slice(String name,
                 IntDomainVar h,
                 TaskVar t,
                 int cpuHeight,
                 int memHeight) {
        this.name = name;
        task = t;
        hoster = h;
        this.cpuHeight = cpuHeight;
        this.memHeight = memHeight;
    }

    /**
     * Get the CPU consumption of the slice during its activity.
     *
     * @return a positive integer
     */
    public int getCPUheight() {
        return this.cpuHeight;
    }

    /**
     * Get the memory consumption of the slice during its activity.
     *
     * @return a positive integer
     */
    public int getMemoryheight() {
        return this.memHeight;
    }

    /**
     * Get the node that host the slice.
     *
     * @return the index of the node.
     */
    public IntDomainVar hoster() {
        return hoster;
    }

    /**
     * @return <code>this.pretty()</code>
     */
    @Override
    public String toString() {
        return this.pretty();
    }

    /**
     * Nice print of the slice.
     *
     * @return a formatted String
     */
    public String pretty() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName()).append("{[").append(start().getInf()).append(",");
        if (start().getSup() == ReconfigurationProblem.MAX_TIME) {
            builder.append("MAX");
        } else {
            builder.append(start().getSup());
        }
        builder.append("] + [")
                .append(duration().getInf()).append(",");
        if (duration().getSup() == ReconfigurationProblem.MAX_TIME) {
            builder.append("MAX");
        } else {
            builder.append(duration().getSup());
        }
        builder.append("] = [")
                .append(end().getInf()).append(",");
        if (end().getInf() == ReconfigurationProblem.MAX_TIME) {
            builder.append("MAX");
        } else {
            builder.append(end().getSup());
        }
        builder.append("] on [")
                .append(hoster().getInf()).append(",").append(hoster().getSup()).append("]}");
        return builder.toString();
    }

    public static int improvable = 0;
    public static int nonImpr = 0;

    /**
     * Add the slice to the model.The following are added:
     * <ul>
     * <li>The variables {@code end()}, {@code start()}, {@code duration()} and {@code hoster()}</li>
     * <li>the constraint <code>start() + duration() = end()</code></li>
     * <li>A constraint to enforce all the variables to be inferior or equals to <code>model.getEnd()</code></li>
     * </ul>
     *
     * @param core the current model of the reconfiguration problem
     */
    public void addToModel(ReconfigurationProblem core) {
        core.post(core.leq(duration(), core.getEnd()));
        if (start().isInstantiated() && duration().isInstantiated()) {
            try {
                end().setVal(duration().getVal() + start().getVal());
            } catch (ContradictionException e) {
                System.err.println(e.getMessage());
            }
        } else {
            core.post(core.eq(end(), core.plus(start(), duration())));
        }

    }

    /**
     * Get the moment the slice starts.
     *
     * @return a positive moment
     */
    public IntDomainVar start() {
        return task.start();
    }

    /**
     * Get the duration of the slice.
     *
     * @return a positive moment
     */
    public IntDomainVar duration() {
        return task.duration();
    }

    /**
     * Get the moment the slice ends.
     *
     * @return a positive moment
     */
    public IntDomainVar end() {
        return task.end();
    }

    /**
     * Get the name of the slice.
     *
     * @return a String
     */
    public String getName() {
        return name;
    }

    /**
     * Set the duration of the slice as a constant.
     *
     * @param d a positive duration
     */
    public void fixDuration(int d) {
        try {
            duration().setVal(d);
        } catch (Exception e) {
            Plan.logger.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Slice slice = (Slice) o;

        if (cpuHeight != slice.cpuHeight || memHeight != slice.memHeight) {
            return false;
        }
        if (!hoster.equals(slice.hoster)) {
            return false;
        }
        if (!task.start().equals(slice.task.start()) ||
                !task.end().equals(slice.task.end()) ||
                !task.duration().equals(slice.task.duration())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = task.hashCode();
        result = 31 * result + hoster.hashCode();
        result = 31 * result + cpuHeight;
        result = 31 * result + memHeight;
        return result;
    }
}
