/*
 * Copyright (c) 2010 Ecole des Mines de Nantes.
 *
 *      This file is part of Entropy.
 *
 *      Entropy is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      Entropy is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */
package entropy.plan.durationEvaluator;

import entropy.PropertiesHelper;
import entropy.PropertiesHelperException;

/**
 * A Factory to get make a DurationEvaluator.
 * @author Fabien Hermenier
 *
 */
public final class DurationEvaluatorFactory {

	/**
	 * Root for actions identifier.
	 */
	public static final String BASE_PROPERTY = "plan.action.duration.";
	
	/**
	 * Identifier of the duration of a migration.
	 */
	public static final String MIGRATION_DURATION_PROPERTY = BASE_PROPERTY + "migrate";

	/**
	 * Identifier of the duration of a run..
	 */
	public static final String RUN_DURATION_PROPERTY = BASE_PROPERTY + "run";
	
	/**
	 * Identifier of the duration of a stop.
	 */
	public static final String STOP_DURATION_PROPERTY = BASE_PROPERTY + "stop";
	
	/**
	 * Identifier of the duration of a local suspend.
	 */
	public static final String LOCAL_SUSPEND_DURATION_PROPERTY = BASE_PROPERTY + "suspend@local";
	
	/**
	 * Identifier of the duration of a local resume.
	 */
	public static final String LOCAL_RESUME_DURATION_PROPERTY = BASE_PROPERTY + "resume@local";
	
	/**
	 * Identifier of the duration of a remote resume.
	 */
	public static final String REMOTE_RESUME_DURATION_PROPERTY = BASE_PROPERTY + "resume@remote";

	/**
	 * Identifier of the duration of a startup.
	 */
	public static final String STARTUP_DURATION_PROPERTY = BASE_PROPERTY + "startup";

	/**
	 * Identifier of the duration of a shutdown.
	 */
	public static final String SHUTDOWN_DURATION_PROPERTY = BASE_PROPERTY + "shutdown";

	/**
	 * No instantiation please.
	 */
	private DurationEvaluatorFactory() {
		
	}
	
	/**
	 * Get a DurationEvaluator by reading properties.
	 * @param props the properties that contain the expressions.
	 * @return a duration evaluator
	 * @throws entropy.PropertiesHelperException if the property file does not contains all the required properties
	 */
	public static DurationEvaluator readFromProperties(PropertiesHelper props) throws PropertiesHelperException {
		String mig = props.getRequiredProperty(MIGRATION_DURATION_PROPERTY);
		String run = props.getRequiredProperty(RUN_DURATION_PROPERTY);
		String stop = props.getRequiredProperty(STOP_DURATION_PROPERTY);
		String suspend = props.getRequiredProperty(LOCAL_SUSPEND_DURATION_PROPERTY);
		String localResume = props.getRequiredProperty(LOCAL_RESUME_DURATION_PROPERTY);
		String remoteResume = props.getRequiredProperty(REMOTE_RESUME_DURATION_PROPERTY);
		String startup = props.getRequiredProperty(STARTUP_DURATION_PROPERTY);
		String shutdown = props.getRequiredProperty(SHUTDOWN_DURATION_PROPERTY);
		return new ANTLRDurationEvaluator(mig, stop, run, suspend, localResume, remoteResume, startup, shutdown);
	}
}
