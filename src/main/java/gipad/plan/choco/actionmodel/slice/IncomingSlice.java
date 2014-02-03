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
public class IncomingSlice extends Slice {

    /**
     * Make a demanding slice.
     *
     * @param model the model of the reconfiguration problem
     * @param name the name of the slice
     * @param cpu   the CPU heights of the slice
     * @param mem   the memory height of the slice
     * @param bwOut the output bandwidth of the slice
     * @param bwIn the input bandwidth of the slice
     */
    public IncomingSlice(ReconfigurationProblem model, String name, int[] cpu, int mem, int bwOut, int bwIn) {
        super(name,
                VF.enumerated("h(" + name + ")", 0, model.getNodes().length - 1, model.getSolver()),
                new Task(VF.enumerated("s(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver()),
    					VF.enumerated("d(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver()),
    					VF.enumerated("e(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver())),
    					cpu,
    					mem,
    					bwOut,
    					bwIn);
    }



    /**
     * Make a demanding slice.
     *
     * @param core   the model of the reconfiguration problem
     * @param name   the name of the slice
     * @param hoster the index of the node that will host the slice
     * @param cpu   the CPU heights of the slice
     * @param mem   the memory height of the slice
     * @param bwOut the output bandwidth of the slice
     * @param bwIn the input bandwidth of the slice
     */
    public IncomingSlice(ReconfigurationProblem model, String name, int hoster, int[] cpu, int mem, int bwOut, int bwIn) {
        super(name,
                VF.fixed("h(" + name + ")", hoster, model.getSolver()),
                new Task(VF.enumerated("s(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver()),
    					VF.enumerated("d(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver()),
    					VF.enumerated("e(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver())),
    					cpu,
    					mem,
    					bwOut,
    					bwIn);
    }

    /**
     * Make a demanding slice.
     *
     * @param core   the model of the reconfiguration problem
     * @param name   the name of the slice
     * @param hoster the index of the node that will host the slice
     * @param start  the moment the action start
     * @param cpu   the CPU heights of the slice
     * @param mem   the memory height of the slice
     * @param bwOut the output bandwidth of the slice
     * @param bwIn the input bandwidth of the slice
     */
    public IncomingSlice(ReconfigurationProblem model, String name, int hoster, int start, int[] cpu, int mem, int bwOut, int bwIn) {
        super(name,
                VF.fixed("h(" + name + ")", hoster, model.getSolver()),
                new Task(VF.fixed("s(" + name + ")", start, model.getSolver()),
    					VF.enumerated("d(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver()),
    					VF.enumerated("e(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver())),
    					cpu,
    					mem,
    					bwOut,
    					bwIn);
    }
    

    /**
     * Make a demanding slice.
     *
     * @param core   the model of the reconfiguration problem
     * @param name   the name of the slice
     * @param hoster the index of the node that will host the slice
     * @param cpu   the CPU heights of the slice
     * @param mem   the memory height of the slice
     * @param bwOut the output bandwidth of the slice
     * @param bwIn the input bandwidth of the slice
     * @param duration the fixed duration of the slice
     */
    public IncomingSlice(ReconfigurationProblem model, String name, int hoster, int[] cpu, int mem, int bwOut, int bwIn, int duration) {
        super(name,
                VF.fixed("h(" + name + ")", hoster, model.getSolver()),
                new Task(VF.enumerated("s(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver()),
                		VF.fixed("d(" + name + ")", duration, model.getSolver()),
    					VF.enumerated("e(" + name + ")", 0, ReconfigurationProblem.MAX_TIME, model.getSolver())),
    					cpu,
    					mem,
    					bwOut,
    					bwIn);
    }



    public IncomingSlice(String name, IntVar hoster, Task t, int[] cpu, int mem, int bwOut, int bwIn) {
        super(name, hoster, t, cpu, mem, bwOut, bwIn);
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
     * Fix the node that will host the slice.
     *
     * @param idx the index of the node
     */
    public void fixHoster(int idx) {
        try {
            this.hoster().instantiateTo(idx, Cause.Null);
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