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

package entropy.monitoring.sampler;

import entropy.monitoring.ConfigurationAdapter;
import entropy.monitoring.MonitorFactoryException;

/**
 * An interface to specify a builder to make ConfigurationAdapter.
 *
 * @author Fabien Hermenier
 */
public interface ConfigurationAdapterBuilder {

    /**
     * Build the configuration adapter.
     *
     * @param configurationFile the pathname to the configuration file
     * @return the instantiated configuration adapter
     * @throws MonitorFactoryException if an error occurred while creating the adapter.
     */
    ConfigurationAdapter build(String configurationFile) throws MonitorFactoryException;

    /**
     * Get the name of the configuration file in the distribution.
     * File must be located in the folder 'config'.
     *
     * @return a filename
     */
    String getDistributionConfigurationFile();
}
