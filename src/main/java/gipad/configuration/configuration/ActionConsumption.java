package gipad.configuration.configuration;

/**
 * 
 * @author Clement
 *
 */
public class ActionConsumption {

	/**
	 * 
	 */
	private double memory;
	
	/**
	 * 
	 */
	private double bandwidth;

	/**
	 * 
	 * @param memory
	 * @param bandwidth
	 */
	public ActionConsumption(double memory, double bandwidth) {
		this.memory = memory;
		this.bandwidth = bandwidth;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getBandwidth() {
		return bandwidth;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getMemory() {
		return memory;
	}

	public int[] getCPU() {
	    // TODO Auto-generated method stub
	    return null;
	}
}
