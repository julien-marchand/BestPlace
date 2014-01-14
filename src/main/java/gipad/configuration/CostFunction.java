package gipad.configuration;

import entropy.plan.durationEvaluator.DurationEvaluator;

/**Interface of the Cost Function for the scheduler
 * We have to create a function able to balance between many dimension
 * See the "Fil Rouge"
 * @author Pocman
 *
 */
public interface CostFunction {
	DurationEvaluator getDurationEvaluator();
}
