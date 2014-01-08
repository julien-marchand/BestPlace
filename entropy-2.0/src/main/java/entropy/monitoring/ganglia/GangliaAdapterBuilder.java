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

package entropy.monitoring.ganglia;

import java.io.IOException;

import entropy.MissingRequiredPropertyException;
import entropy.PropertiesHelper;
import entropy.WrongPropertyTypeException;
import entropy.monitoring.MonitorFactoryException;
import entropy.monitoring.sampler.ConfigurationAdapterBuilder;

/**
 * A builder to create a GangliaConfigurationAdapter.
 *
 * @author Fabien Hermenier
 */
public class GangliaAdapterBuilder implements ConfigurationAdapterBuilder {

    /**
     * The identifier of the property that indicates the hostname of the GMetad.
     */
    public static final String HOSTNAME_PROPERTY = "host";

    /**
     * The identifier of the property that indicates the port of GMetad.
     */
    public static final String PORT_PROPERTY = "port";


    /**
     * The location of the distribution properties file for the adapter.
     */
    private static final String PROPERTIES_PATH = "config/ganglia.properties";

    /**
     * Build the adapter using a custom properties file.
     *
     * @param props the pathname to the properties file
     * @return the ConfigurationAdapter
     * @throws MonitorFactoryException if an error occurred while create the adapter
     */
    @Override
    public GangliaConfigurationAdapter build(String props) throws MonitorFactoryException {
        try {
            PropertiesHelper helper = new PropertiesHelper(props);
            return new GangliaConfigurationAdapter(helper.getRequiredProperty(HOSTNAME_PROPERTY),
                    helper.getRequiredPropertyAsInt(PORT_PROPERTY));
        } catch (MissingRequiredPropertyException e) {
            throw new MonitorFactoryException("Unable to instantiate Gangia monitoring module: " + e.getMessage(), e);
        } catch (WrongPropertyTypeException e) {
            throw new MonitorFactoryException("Unable to instantiate Ganglia monitoring module: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new MonitorFactoryException("Unable to instantiate Ganglia monitoring module: " + e.getMessage(), e);
        }

    }

    @Override
    public String getDistributionConfigurationFile() {
        return PROPERTIES_PATH;
    }
}
