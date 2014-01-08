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


import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;
import entropy.configuration.Node;
import entropy.plan.Plan;
import entropy.plan.choco.ReconfigurationProblem;

/**
 * A consuming slice is a slice that starts at the beginning of a reconfiguration process. The slice
 * is already hosted on a node.
 *
 * @author Fabien Hermenier
 */
public class ConsumingSlice extends Slice {

    /**
     * Make a new consuming slice.
     *
     * @param model the model of the reconfiguration problem
     * @param name  the identifier of the slice
     * @param node  the current hoster of the slice
     * @param cpu   the CPU height of the slice
     * @param mem   the memory height of the slice
     */
    public ConsumingSlice(ReconfigurationProblem model, String name, Node node, int cpu, int mem) {
        super(name,
                model.createIntegerConstant("h(" + name + ")", model.getNode(node)),
                model.createTaskVar(name,
                        model.getStart(),
                        model.createBoundIntVar("ed(" + name + ")", 0, ReconfigurationProblem.MAX_TIME),
                        model.createBoundIntVar("d(" + name + ")", 0, ReconfigurationProblem.MAX_TIME)),
                cpu,
                mem
        );
    }

    /**
     * Make a new consuming slice.
     *
     * @param model    the model of the reconfiguration problem
     * @param name     the identifier of the slice
     * @param node     the hosting node of the slice
     * @param cpu      the CPU height of the slice
     * @param mem      the memory height of the slice
     * @param duration the fixed duration of the slice
     */
    public ConsumingSlice(ReconfigurationProblem model, String name, Node node, int cpu, int mem, int duration) {
        super(name,
                model.createIntegerConstant("h(" + name + ")", model.getNode(node)),
                model.createTaskVar(name,
                        model.getStart(),
                        //new IntDomainVarAddCste(model, "ed(" + name + ")", model.getStart(), duration),
                        model.createBoundIntVar("ed(" + name + ")", 0, ReconfigurationProblem.MAX_TIME),
                        model.createIntegerConstant("d(" + name + ")", duration)),
                cpu,
                mem
        );

    }

    public ConsumingSlice(String name, IntDomainVar host, TaskVar t, int cpu, int mem) {
        super(name, host, t, cpu, mem);
    }

    /**
     * Fix the end moment of the slice.
     *
     * @param t the moment the action ends
     */
    public void fixEnd(int t) {
        try {
            this.end().setVal(t);
        } catch (Exception e) {
            Plan.logger.error(e.getMessage(), e);
        }
    }

}
