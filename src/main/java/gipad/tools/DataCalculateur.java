package gipad.tools;

import org.discovery.DiscoveryModel.model.Cpu;
import org.discovery.DiscoveryModel.model.HardwareSpecification;

public class DataCalculateur {

	private HardwareSpecification spec;

	public DataCalculateur(HardwareSpecification spec) {
		this.spec = spec;
	}

	public int getSumCPu() {
		int sum = 0;
		for (Cpu cpu : spec.cpus()) {
			sum += cpu.nbCores() * cpu.getCpuCapacity();
		}
		return sum;
	}
}