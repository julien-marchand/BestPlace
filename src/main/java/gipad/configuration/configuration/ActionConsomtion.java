package gipad.configuration.configuration;

/**
 * 
 * @author Clement
 *
 */
public class ActionConsomtion {

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
	public ActionConsomtion(double memory, double bandwidth) {
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
}
