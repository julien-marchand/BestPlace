package gipad.plan.choco.actionmodel.slice;

import gipad.configuration.configuration.ActionConsumption;
import gipad.configuration.configuration.*;
import gipad.plan.Plan;
import gipad.plan.choco.ReconfigurationProblem;

import solver.Cause;
import solver.variables.IntVar;
import solver.variables.Task;
import solver.variables.VF;



/**
 * A consuming slice is a slice that starts at the beginning of a reconfiguration process. The slice
 * is already hosted on a node.
 *
 * @author Thomas Pocreau
 */
public class ConsumingSlice extends Slice {

    /**
     * Make a new consuming slice.
     *
     * @param model the model of the reconfiguration problem
     * @param name  the identifier of the slice
     * @param node  the current hoster of the slice
     * @param cpu   the CPU heights of the slice
     * @param mem   the memory height of the slice
     * @param bwOut the output bandwidth of the slice
     * @param bwIn the input bandwidth of the slice
     */
	
    public ConsumingSlice(ReconfigurationProblem model, String name, VirtualMachine vm, ActionConsumption consumption, Configuration conf) {
    	super(name, 
    			VF.fixed(conf.getLocation(vm).getId(), model.getSolver()),
    			new Task(model.getStart(),
    					VF.enumerated("d(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver()),
    					VF.enumerated("e(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver())),
    					consumption.getCPU(),
    					consumption.getMemory(),
    					VF.fixed(consumption.getBandwidthOut(), model.getSolver()),
    					VF.fixed(consumption.getBandwidthIn(), model.getSolver()));
    }

//    /**
//     * Make a new consuming slice.
//     *
//     * @param model    the model of the reconfiguration problem
//     * @param name     the identifier of the slice
//     * @param node     the hosting node of the slice
//     * @param cpu   the CPU heights of the slice
//     * @param mem   the memory height of the slice
//     * @param bwOut the output bandwidth of the slice
//     * @param bwIn the input bandwidth of the slice
//     * @param duration the fixed duration of the slice
//     */
//    public ConsumingSlice(ReconfigurationProblem model, String name, int node, int[] cpu, int mem, int bwOut, int bwIn, int duration) {
//    	super(name, 
//    			VF.fixed("h(" + name + ")", node, model.getSolver()),
//    			new Task(model.getStart(),
//    					VF.enumerated("d(" + name + ")", duration, duration, model.getSolver()),
//    					VF.enumerated("e(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver())),
//    					cpu,
//    					mem,
//    					bwOut,
//    					bwIn);
//
//    }
//
//    public ConsumingSlice(String name, IntVar node, Task t, int[] cpu, int mem, int bwOut, int bwIn) {
//        super(name, node, t, cpu, mem, bwOut, bwIn);
//    }

    /**
     * Fix the end moment of the slice.
     *
     * @param t the moment the action ends
     */
    public void fixEnd(int t) {
        try {
            this.getEnd().instantiateTo(t, Cause.Null);
        } catch (Exception e) {
            Plan.logger.error(e.getMessage(), e);
        }
    }

}
