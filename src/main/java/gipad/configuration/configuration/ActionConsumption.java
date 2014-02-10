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
	public int getMemory() {
		return (int)memory;
	}

	public int[] getCPU() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getBandwidthOut() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getBandwidthIn() {
		// TODO Auto-generated method stub
		return 0;
	}
}
