package gipad.plan;

import gipad.configuration.ManagedElementList;
import gipad.configuration.configuration.Configuration;
import gipad.exception.PlanException;

import org.discovery.DiscoveryModel.model.VirtualMachine;

public interface Plan {
	
	  /**
     * Compute a new DefaultSequencedReconfigurationPlan that satisfy all the constraints applied to the model.
     *
     * @param src   The source configuration. It must be viable.
     * @param q the vjobs
     * @return a plan if it exists.
     * @throws PlanException if an error occurred while planing the action to reach the state of the nodes and the virtual machines
	 * @throws gipad.exception.PlanException 
     */
	public SequencedReconfigurationPlan compute(Configuration src, 
			ManagedElementList<VirtualMachine> q) throws PlanException;
	
	

}
