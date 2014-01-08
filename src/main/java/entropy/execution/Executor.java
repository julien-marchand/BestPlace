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

package entropy.execution;

import entropy.execution.driver.Driver;
import entropy.execution.driver.DriverException;

/**
 * An executor that execute an action in parallel.
 * Once the execution is terminated, it is signaled to
 * the reconfiguration executer using the method commit.
 * @author Fabien Hermenier
 */
public class Executor extends Thread {

    /**
     * The driver that wrap the action to execute.
     */
    private Driver drv;

    /**
     * The exception thrown if an error occurred.
     */
    private DriverException exception = null;

    /**
     * The reconfiguration executor to signal the termination of the action to.
     */
    private TimedReconfigurationExecuter master;


    /**
     * Make a new executor.
     * @param driver the driver that handle the action to execute
     * @param e the reconfiguration executer
     */
    public Executor(Driver driver, TimedReconfigurationExecuter e) {
        this.drv = driver;
        this.master = e; 
    }

    /**
     * Start to execute the action in another thread.
     * When the action is terminated, it is signaled to the TimedReconfigurationExector
     * even if an error occurred
     */
    @Override
    public void run() {
        try {
            drv.execute();
        } catch (DriverException e) {           
            exception = e;
        } finally {
            master.commit(this);
        }
    }

    /**
     * Indicates wether the action succeed or not.
     * @return true if the action succeed.
     */
    public boolean hasSuceeded() {
        return this.exception == null;
    }

    /**
     * Get the exception if the execution did not succeed
     * @return the exception. null if the 
     */
    public DriverException getException() {
        return this.exception;
    }

    /**
     * Get the driver that handle the action to execute.
     * @return a driver
     */
    public Driver getDriver() {
        return this.drv;
    }
}
