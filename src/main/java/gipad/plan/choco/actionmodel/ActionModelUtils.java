package gipad.plan.choco.actionmodel;

import gipad.plan.choco.actionmodel.slice.*;

import java.util.ArrayList;
import java.util.List;


public class ActionModelUtils {
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
}
