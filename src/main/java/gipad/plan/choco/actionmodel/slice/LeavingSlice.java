package gipad.plan.choco.actionmodel.slice;

import gipad.plan.Plan;
import gipad.plan.choco.ReconfigurationProblem;

import org.discovery.DiscoveryModel.model.Node;

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
public class LeavingSlice extends Slice {

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
    public LeavingSlice(ReconfigurationProblem model, String name, int node, int[] cpu, int mem, int bwOut, int bwIn) {
    	super(name, 
    			VF.fixed("h(" + name + ")", node, model.getSolver()),
    			new Task(VF.enumerated("s(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver()),
    					VF.enumerated("d(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver()),
    					VF.enumerated("e(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver())),
    					cpu,
    					mem,
    					bwOut,
    					bwIn);
    }

    /**
     * Make a new consuming slice.
     *
     * @param model    the model of the reconfiguration problem
     * @param name     the identifier of the slice
     * @param node     the hosting node of the slice
     * @param cpu   the CPU heights of the slice
     * @param mem   the memory height of the slice
     * @param bwOut the output bandwidth of the slice
     * @param bwIn the input bandwidth of the slice
     * @param duration the fixed duration of the slice
     */
    public LeavingSlice(ReconfigurationProblem model, String name, int node, int[] cpu, int mem, int bwOut, int bwIn, int duration) {
    	super(name, 
    			VF.fixed("h(" + name + ")", node, model.getSolver()),
    			new Task(VF.enumerated("s(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver()),
    					VF.fixed("d(" + name + ")", duration, model.getSolver()),
    					VF.enumerated("e(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver())),
    					cpu,
    					mem,
    					bwOut,
    					bwIn);
    }
    
    /**
     * Make a new consuming slice.
     *
     * @param model    the model of the reconfiguration problem
     * @param name     the identifier of the slice
     * @param start  the fixed start of the slice
     * @param node     the hosting node of the slice
     * @param cpu   the CPU heights of the slice
     * @param mem   the memory height of the slice
     * @param bwOut the output bandwidth of the slice
     * @param bwIn the input bandwidth of the slice
     */
    public LeavingSlice(ReconfigurationProblem model, String name, int start, int node, int[] cpu, int mem, int bwOut, int bwIn) {
    	super(name, 
    			VF.fixed("h(" + name + ")", node, model.getSolver()),
    			new Task(VF.fixed("s(" + name + ")", start, model.getSolver()),
    					VF.enumerated("d(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver()),
    					VF.enumerated("e(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver())),
    					cpu,
    					mem,
    					bwOut,
    					bwIn);
    }

    public LeavingSlice(String name, IntVar host, Task t, int[] cpu, int mem, int bwOut, int bwIn) {
        super(name, host, t, cpu, mem, bwOut, bwIn);
    }

    /**
     * Fix the end moment of the slice.
     *
     * @param t the moment the action ends
     */
    public void fixEnd(int t) {
        try {
            this.end().instantiateTo(t, Cause.Null);
        } catch (Exception e) {
            Plan.logger.error(e.getMessage(), e);
        }
    }
    
    /**
     * Fix the start moment of the slice.
     *
     * @param t the moment the action starts
     */
    public void fixStart(int t) {
        try {
            this.start().instantiateTo(t, Cause.Null);
        } catch (Exception e) {
            Plan.logger.error(e.getMessage(), e);
        }
    }

}