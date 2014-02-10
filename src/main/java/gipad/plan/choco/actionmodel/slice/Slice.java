package gipad.plan.choco.actionmodel.slice;

import java.util.Arrays;

import solver.Cause;
import solver.constraints.ICF;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.Task;


import gipad.plan.Plan;
import gipad.plan.choco.ReconfigurationProblem;

/**
 * Model a period where a managed element is consuming CPU and memory and bandwidth resources during
 * a bounded amount of time on a node.
 *
 *@author Thomas Pocreau
 */
public class Slice {

    private Task task;

    /**
     * Indicates the identifier of slice hoster.
     */
    private IntVar hoster;

    /**
     * The CPU height of the slice.
     */
    private int[] cpuHeight;

    /**
     * The memory height of the slice.
     */
    private int memHeight;
    
    /**
     * The output bandwidth of the slice
     */
    private IntVar bwOutput;
    
    /**
     * The input bandwith of the slice
     */
    private IntVar bwInput;

    /**
     * name of the slice
     */
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
                 IntVar h,
                 Task t,
                 int[] cpuHeight,
                 int memHeight,
                 IntVar bwOutput,
                 IntVar bwInput) {
        this.name = name;
        task = t;
        hoster = h;
        this.cpuHeight = cpuHeight;
        this.memHeight = memHeight;
        this.bwOutput = bwOutput;
        this.bwInput = bwInput;
    }

    /**
     * Get the CPU consumption of the slice during its activity.
     *
     * @return a positive integer
     */
    public int[] getCPUheight() {
        return this.cpuHeight;
    }

    /**
     * Get the output bandwith consumption of the slice during its activity
     * 
     * @return a positive integer
     */
    public IntVar getBwOutput() {
		return bwOutput;
	}

    /**
     * Get the input bandwith consumption of the slice during its activity
     * 
     * @return a positive integer
     */
	public IntVar getBwInput() {
		return bwInput;
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
    public IntVar hoster() {
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
        builder.append(getName()).append("{[").append(getStart().getLB()).append(",");
        if (getStart().getUB() == ReconfigurationProblem.MAX_TIME) {
            builder.append("MAX");
        } else {
            builder.append(getStart().getUB());
        }
        builder.append("] + [")
                .append(duration().getLB()).append(",");
        if (duration().getUB() == ReconfigurationProblem.MAX_TIME) {
            builder.append("MAX");
        } else {
            builder.append(duration().getUB());
        }
        builder.append("] = [")
                .append(getEnd().getLB()).append(",");
        if (getEnd().getLB() == ReconfigurationProblem.MAX_TIME) {
            builder.append("MAX");
        } else {
            builder.append(getEnd().getUB());
        }
        builder.append("] on [")
                .append(hoster().getLB()).append(",").append(hoster().getUB()).append("]}");
        return builder.toString();
    }

    public static int improvable = 0;
    public static int nonImpr = 0;

    /**
     * Add the slice to the model.The following are added:
     * <ul>
     * <li>A constraint to enforce all the variables to be inferior or equals to <code>model.getEnd()</code></li>
     * </ul>
     *
     * @param core the current model of the reconfiguration problem
     */
    public void addToModel(ReconfigurationProblem core) {
    	core.getSolver().post(ICF.arithm(this.getEnd(), "<=", core.getEnd()));
    }

    /**
     * Get the moment the slice starts.
     *
     * @return a positive moment
     */
    public IntVar getStart() {
        return task.getStart();
    }
    
    public void setStart(IntVar start){
    	task.setStart(start);
    }

    public void setEnd(IntVar end){
    	task.setEnd(end);
    }
    /**
     * Get the duration of the slice.
     *
     * @return a positive moment
     */
    public IntVar duration() {
        return task.getDuration();
    }
    
    public void setDuration(IntVar dur){
    	task.setDuration(dur);
    }

    public void setHoster(IntVar hoster) {
		this.hoster = hoster;
	}

	public void setBwOutput(IntVar bwOutput) {
		this.bwOutput = bwOutput;
	}

	public void setBwInput(IntVar bwInput) {
		this.bwInput = bwInput;
	}

	/**
     * Get the moment the slice ends.
     *
     * @return a positive moment
     */
    public IntVar getEnd() {
        return task.getEnd();
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
            this.duration().instantiateTo(d, Cause.Null);
        } catch (Exception e) {
            Plan.logger.error(e.getMessage(), e);
        }
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bwInput.hashCode();
		result = prime * result + bwOutput.hashCode();
		result = prime * result + Arrays.hashCode(cpuHeight);
		result = prime * result + ((hoster == null) ? 0 : hoster.hashCode());
		result = prime * result + memHeight;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Slice other = (Slice) obj;
		if (bwInput != other.bwInput)
			return false;
		if (bwOutput != other.bwOutput)
			return false;
		if (!Arrays.equals(cpuHeight, other.cpuHeight))
			return false;
		if (hoster == null) {
			if (other.hoster != null)
				return false;
		} else if (!hoster.equals(other.hoster))
			return false;
		if (memHeight != other.memHeight)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		return true;
	}
}
