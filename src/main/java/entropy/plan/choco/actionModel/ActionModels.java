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

package entropy.plan.choco.actionModel;

import java.util.ArrayList;
import java.util.List;

import solver.variables.IntVar;
import entropy.plan.choco.actionModel.slice.ConsumingSlice;
import entropy.plan.choco.actionModel.slice.DemandingSlice;
import entropy.plan.choco.actionModel.slice.Slice;

/**
 * A toolbox to extract some objects in a list of ActionModel.
 *
 * @author Fabien Hermenier
 */
public final class ActionModels {

    /**
     * ToolBox, no instantiation.
     */
    private ActionModels() {
    }

    /**
     * Extract all the demanding slices of a list of actions.
     *
     * @param actions the list of action
     * @return a list of demanding slice. May be empty
     */
    public static List<DemandingSlice> extractDemandingSlices(List<? extends ActionModel> actions) {
        List<DemandingSlice> slices = new ArrayList<DemandingSlice>();
        for (ActionModel a : actions) {
            if (a.getDemandingSlice() != null) {
                slices.add(a.getDemandingSlice());
            }
        }
        return slices;
    }

    /**
     * Extract all the consuming slices of a list of actions.
     *
     * @param actions the list of action
     * @return a list of consuming slice. May be empty
     */
    public static List<ConsumingSlice> extractConsumingSlices(List<? extends ActionModel> actions) {
        List<ConsumingSlice> slices = new ArrayList<ConsumingSlice>();
        for (ActionModel a : actions) {
            if (a.getConsumingSlice() != null) {
                slices.add(a.getConsumingSlice());
            }
        }
        return slices;
    }

    /**
     * Extract all the slices of a list of actions.
     *
     * @param actions the list of action
     * @return a list of slices. May be empty
     */
    public static List<Slice> extractSlices(List<? extends ActionModel> actions) {
        List<Slice> slices = new ArrayList<Slice>();
        for (ActionModel a : actions) {
            if (a.getConsumingSlice() != null) {
                slices.add(a.getConsumingSlice());
            }
            if (a.getDemandingSlice() != null) {
                slices.add(a.getDemandingSlice());
            }
        }
        return slices;
    }

    /**
     * Extract the start moments of an array of actions.
     *
     * @param actions the array of actions
     * @return an array of start moments, in the same order that the actions
     */
    public static IntVar[] extractStarts(ActionModel[] actions) {
    	IntVar[] vs = new IntVar[actions.length];
        for (int i = 0; i < actions.length; i++) {
            vs[i] = actions[i].start();
        }
        return vs;
    }

    /**
     * Extract the end moments of an array of actions.
     *
     * @param actions the array of actions
     * @return an array of end moments, in the same order that the actions
     */
    public static IntVar[] extractEnds(ActionModel[] actions) {
    	IntVar[] vs = new IntVar[actions.length];
        for (int i = 0; i < actions.length; i++) {
            vs[i] = actions[i].end();
        }
        return vs;
    }

    /**
     * Extract the durations of an array of actions.
     *
     * @param actions the array of actions
     * @return an array of durations, in the same order that the actions
     */
    public static IntVar[] extractDurations(ActionModel[] actions) {
    	IntVar[] vs = new IntVar[actions.length];
        for (int i = 0; i < actions.length; i++) {
            vs[i] = actions[i].getDuration();
        }
        return vs;
    }

    /**
     * Extract the durations of a list of actions.
     *
     * @param actions the list of actions
     * @return an array of durations, in the same order that the actions
     */
    public static IntVar[] extractDurations(List<ActionModel> actions) {
        return extractDurations(actions.toArray(new ActionModel[actions.size()]));
    }

    /**
     * Extract the cost of an array of actions.
     *
     * @param actions the array of actions
     * @return an array of costs, in the same order that the actions
     */
    public static IntVar[] extractCosts(ActionModel[] actions) {
    	IntVar[] vs = new IntVar[actions.length];
        for (int i = 0; i < actions.length; i++) {
            vs[i] = actions[i].getGlobalCost();
        }
        return vs;
    }

    /**
     * Extract the cost of a list of actions.
     *
     * @param actions the list of actions
     * @return an array of costs, in the same order that the actions
     */
    public static IntVar[] extractCosts(List<ActionModel> actions) {
        return extractCosts(actions.toArray(new ActionModel[actions.size()]));
    }

}
