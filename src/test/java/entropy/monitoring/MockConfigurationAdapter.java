/*
 * Copyright (c) 2009 Ecole des Mines de Nantes.
 * 
 *     This file is part of Entropy.
 * 
 *     Entropy is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     Entropy is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 * 
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
*/
package entropy.monitoring;

import entropy.configuration.Configuration;

/**
 * A Mock object to return the configuration we want.
 *
 * @author Fabien Hermenier
 */
public class MockConfigurationAdapter extends ConfigurationAdapter {

    /**
     * The configuration to return.
     */
    private Configuration conf;

    /**
     * load a configuration that will be returned with <code>extractConfiguration()</code>.
     *
     * @param c the configuration
     */
    public void useConfiguration(Configuration c) {
        this.conf = c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration extractConfiguration() throws MonitoringException {
        return this.conf;
    }

}
