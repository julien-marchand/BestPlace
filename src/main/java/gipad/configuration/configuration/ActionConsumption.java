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
	private double bandwidthOut;
	
	/**
	 * 
	 */
	private double bandwidthIn;
	/**
	 * 
	 */
	private double[] cpu;
	
	/**
	 * 
	 * @param memory
	 * @param bandwidth
	 */
	public ActionConsumption(double memory, double[] cpu, double bandwidthOut, double bandwithIn) {
		this.memory = memory;
		this.bandwidthOut = bandwidthOut;
		this.bandwidthIn = bandwithIn;
		this.cpu = cpu;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getBandwidthOut() {
		return bandwidthOut;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getBandwidthIn() {
		return bandwidthIn;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getMemory() {
		return memory;
	}
	
	/**
	 * 
	 * @return
	 */
	public double[] getCpu() {
		return cpu;
	}
}