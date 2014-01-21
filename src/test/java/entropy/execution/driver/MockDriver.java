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

package entropy.execution.driver;

import entropy.plan.action.Action;

/**
 * A mock driver.
 * The execution times vary between 0 to 1 seconds. The driver may fail
 * @author Fabien Hermenier
 *
 */
public final class MockDriver extends Driver {
	
	/**
	 * The action to execute.
	 */
	private Action a;
	
	/**
	 * Status of the driver.
	 */
	private boolean success = false;
	
	/**
	 * Must the driver fail ?. 
	 */
	private boolean fail = false;

    /**
     * The maximum jitter of the execution time in milliseconds.
     */
    public static int MAX_JITTER = 2000;

	/**	
	 * New mock.
	 * @param action the action to execute
	 * @param mustFail indicates wether the driver has to fail or not
	 */
	public MockDriver(Action action, boolean mustFail) {
		super(action);
		this.a = action;
		this.fail = mustFail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws DriverException {		
		long sleep = (long) (/*a.getFinishMoment() - a.getStartMoment() + */Math.random() * MAX_JITTER);		
		try {
			System.out.println("Starting " + a.toString() + " for " + sleep + " msecs.");
			Thread.sleep(sleep);
			System.out.println("Ending " + a.toString());
			if (fail) {
				throw new DriverException(this, "fail");
			}
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}		
		success = true;		
	}

	/**
	 * Indicates wether the execution of the driver succeed or not.
	 * @return true if the execution succeed
	 */
	public boolean isSuccess() {
		return this.success;
	}
	
	/**
	 * Get the name of the driver.
	 * @return the name
	 */
	@Override
	public String toString() {
		return this.a.toString();
	}
}
