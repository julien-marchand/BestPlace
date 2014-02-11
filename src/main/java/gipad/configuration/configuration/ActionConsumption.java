package gipad.configuration.configuration;

/**
 * 
 * @author Clement
 *
 */
public class ActionConsumption {

	private static final double MILLE = 1000;

	/**
	 * en Mo
	 */
	private double memory;
	
	/**
	 * en Mo/s
	 */
	private double bandwidthOut;
	
	/**
	 * en Mo/s
	 */
	private double bandwidthIn;
	
	/**
	 * en ???
	 */
	private double[] cpu;
	
	/**
	 * 
	 * @param memory
	 * @param cpu
	 * @param bandwidthOut
	 * @param bandwithIn
	 */
	public ActionConsumption(double memory, double[] cpu, double bandwidthOut, double bandwithIn) {
		this.memory = memory;
		this.bandwidthOut = bandwidthOut;
		this.bandwidthIn = bandwithIn;
		this.cpu = cpu;
	}
	
	/**
	 * en ko/s
	 * @return
	 */
	public int getBandwidthOut() {
		return (int) (MILLE * bandwidthOut);
	}
	
	/**
	 * en ko/s
	 * @return
	 */
	public int getBandwidthIn() {
		return (int) (MILLE * bandwidthIn);
	}
	
	/**
	 * en ko
	 * @return
	 */
	public int getMemory() {
		return (int) (MILLE * memory);
	}
	
	/**
	 * 
	 * @return
	 */
	public double[] getCpu() {
		return cpu;
	}
}
