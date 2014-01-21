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

package entropy.decision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import entropy.configuration.Configuration;

/**
 * An abstract decision module.
 *
 * @author Fabien Hermenier
 */
public abstract class DecisionModule {

    /**
     * The current logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("Decision");

    /**
     * The timeout in seconds to compute a new configuration.
     */
    private int timeout;

    /**
     * Compute a new configuration that is a solution to a problem for the current
     * observations. This configuration is not necessary reachable.
     *
     * @param cfg the current configuration
     * @return a new configuration, if exists that is a solution to the problem
     * @throws AssignmentException if at least one virtual machine is not assignable to a node
     */
    public abstract Configuration compute(Configuration cfg) throws AssignmentException;

    /**
     * Set the timeout value to solve this problem.
     *
     * @param val the value in seconds.
     */
    public void setTimeout(int val) {
        this.timeout = val;
    }

    /**
     * Get the current timeout value.
     *
     * @return the value in seconds.
     */
    public int getTimeout() {
        return this.timeout;
    }

    /**
     * Get the logger associated to the module.
     *
     * @return an initialized logger
     */
    public static Logger getLogger() {
        return DecisionModule.LOGGER;
    }
}
