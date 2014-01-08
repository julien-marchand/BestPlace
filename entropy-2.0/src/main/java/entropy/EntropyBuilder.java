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

package entropy;

import java.io.IOException;

import entropy.controlLoop.ControlLoop;
import entropy.controlLoop.ControlLoopFactory;
import entropy.controlLoop.ControlLoopFactoryException;
import entropy.monitoring.MonitorFactory;
import entropy.monitoring.MonitorFactoryException;

/**
 * Build an instance of Entropy using a properties file.
 *
 * @author Fabien Hermenier
 */
public final class EntropyBuilder {

    /**
     * The key to specify the registry port.
     */
    public static final String REGISTRY_PORT_KEY = "entropy.registry.port";

    /**
     * The key to specify the delay between two iteration of a control loop.
     */
    public static final String SLEEP_DELAY_KEY = "entropy.delayBetweenLoops";

    /**
     * No instantiation.
     */
    private EntropyBuilder() {

    }

    /**
     * Build an instance of Entropy using a properties file.
     *
     * @param props the properties
     * @return an instance of Entropy
     * @throws IOException                 if an error occurred while configuring Entropy
     * @throws PropertiesHelperException   if an error occurred while reading the properties to build Entropy
     * @throws MonitorFactoryException     if an error occured while instantiating the Monitor
     * @throws ControlLoopFactoryException if an error occurred while instantiating the control loop
     */
    public static Entropy buildFromProperties(PropertiesHelper props) throws IOException, PropertiesHelperException, MonitorFactoryException, ControlLoopFactoryException {
        MonitorFactory mf = new MonitorFactory(props);
        ControlLoopFactory ctlFactory = new ControlLoopFactory(props);
        Entropy e;
        ControlLoop loop = ctlFactory.makeControlLoop(mf.createMonitor());
        e = new Entropy(loop);
        e.setRegistryPort(props.getRequiredPropertyAsInt(REGISTRY_PORT_KEY));
        e.setSleepDelay(props.getRequiredPropertyAsInt(SLEEP_DELAY_KEY));
        return e;
    }
}
