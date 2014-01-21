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
package entropy.monitoring;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import entropy.PropertiesHelper;
import entropy.PropertiesHelperException;
import entropy.monitoring.sampler.CPUBurningVMsSampler;
import entropy.monitoring.sampler.CPUSampler;
import entropy.monitoring.sampler.ConfigurationAdapterBuilder;
import entropy.monitoring.sampler.MemorySampler;

/**
 * A Factory to create Monitor instances by reading some properties.
 * <p/>
 * TODO: Describes the properties
 *
 * @author Fabien Hermenier
 */
public class MonitorFactory {

    public static final String ROOT_PROPERTY = "monitoring";

    public static final String ADAPTER = ROOT_PROPERTY + ".adapter";

    /**
     * The property that define the implementation to use.
     */
    public static final String ADAPTER_IMPL_PROPERTY = ADAPTER + ".impl";


    //Stuff related to black and white lists
    /**
     * The property that define the path of the nodes black list.
     */
    public static final String NODES_BL_PROPERTY = ADAPTER + ".nodesBlacklist";

    /**
     * The property that define the path of the nodes black list.
     */
    public static final String NODES_WL_PROPERTY = ADAPTER + ".nodesWhitelist";

    //Stuff related to the samplers

    public static final String SAMPLER = ROOT_PROPERTY + ".simplify";

    public static final String USE_SAMPLERS = SAMPLER + ".activate";

    //memory sampler
    public static final String MEM_SAMPLER = SAMPLER + ".mem";

    public static final String MEM_SAMPLER_IMPL = MEM_SAMPLER + ".impl";

    public static final String MEM_SAMPLER_SAMPLER_IMPL = "sampler";

    public static final String MEM_SAMPLER_SAMPLER_IMPL_SIZE = MEM_SAMPLER + "." + MEM_SAMPLER_SAMPLER_IMPL + ".size";


    //CPU samplers
    public static final String CPU_SAMPLER = SAMPLER + ".cpu";

    public static final String CPU_SAMPLER_IMPL = CPU_SAMPLER + ".impl";

    public static final String CPU_SAMPLER_SAMPLER_IMPL = "sampler";

    public static final String CPU_SAMPLER_SAMPLER_IMPL_SIZE = CPU_SAMPLER + "." + CPU_SAMPLER_SAMPLER_IMPL + ".size";

    public static final String CPU_SAMPLER_BURNING_IMPL = "CPUBurning";

    public static final String CPU_SAMPLER_BURNING_IMPL_SIZE = CPU_SAMPLER + "." + CPU_SAMPLER_BURNING_IMPL + ".threshold";

    /**
     * The properties to use.
     */
    private PropertiesHelper properties;

    /**
     * Create a new factory using specific properties.
     *
     * @param props the properties to use
     */
    public MonitorFactory(PropertiesHelper props) {
        this.properties = props;
    }

    /**
     * Create a new monitor instance.
     *
     * @return the initialized instance
     * @throws entropy.PropertiesHelperException
     *                                 if an error occurred while configuring the adapter
     * @throws MonitorFactoryException If an error occurred during the instantiation
     */
    public Monitor createMonitor() throws PropertiesHelperException, MonitorFactoryException {

        Monitor m = new Monitor(createConfAdapter());
        try {
            attachNodeLists(m);
        } catch (IOException e) {
            throw new MonitorFactoryException("Error while reading the nodes file", e);
        }
        attachSamplers(m);
        return m;
    }

    /**
     * Create the ConfigurationAdapter.
     *
     * @return an initialized adapter.
     * @throws entropy.PropertiesHelperException
     *                                 if an error occurred while configuring the adapter
     * @throws MonitorFactoryException If an error occurred during the instantiation
     */
    private ConfigurationAdapter createConfAdapter() throws PropertiesHelperException, MonitorFactoryException {
        String impl = this.properties.getRequiredProperty(ADAPTER_IMPL_PROPERTY);
        try {
            Class cl = getClass().getClassLoader().loadClass(impl);
            ConfigurationAdapterBuilder b = (ConfigurationAdapterBuilder) cl.newInstance();
            return b.build(b.getDistributionConfigurationFile());
        } catch (ClassNotFoundException e) {
            throw new MonitorFactoryException("Unable to locate implementation '" + impl + "'" + e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new MonitorFactoryException("Unable to instantiate '" + impl + "'" + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new MonitorFactoryException("Error while accessing to '" + impl + "':" + e.getMessage(), e);
        }
    }

    /**
     * Attach the white or the black node list to a ConfigurationAdapter
     *
     * @param mon the monitor
     * @throws entropy.PropertiesHelperException
     *                                 if an error occurred while configuring the driver
     * @throws MonitorFactoryException If an error occurred during the instantiation
     * @throws IOException             if an error occurred while reading the nodes file
     */
    private void attachNodeLists(Monitor mon) throws IOException, PropertiesHelperException, MonitorFactoryException {
        if (properties.isDefined(NODES_BL_PROPERTY)
                && properties.isDefined(NODES_WL_PROPERTY)) {
            throw new MonitorFactoryException("Having a blacklist and a whitelist is not supported");
        }

        if (properties.isDefined(NODES_BL_PROPERTY)) {
            mon.getConfigurationAdapter().setNodesBlackList(readNodes(properties.getRequiredProperty(NODES_BL_PROPERTY)));
        } else if (properties.isDefined(NODES_WL_PROPERTY)) {
            mon.getConfigurationAdapter().setNodesWhiteList(readNodes(properties.getRequiredProperty(NODES_WL_PROPERTY)));
        }

    }

    /**
     * Read a file line by lines.
     *
     * @param file the file to read
     * @return a list of lines, may be empty
     * @throws MonitorFactoryException If an error occurred during the instantiation
     * @throws IOException             if an error occurred while reading the nodes file
     */
    private List<String> readNodes(String file) throws IOException, MonitorFactoryException {
        LinkedList<String> bl;
        BufferedReader reader = null;
        try {
            FileReader r = new FileReader(file);
            reader = new BufferedReader(r);
            String line = reader.readLine();
            bl = new LinkedList<String>();
            while (line != null) {
                bl.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new MonitorFactoryException("Error while reading '" + file + "'", e);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return bl;
    }

    /**
     * Attach a set of samplers to a monitor.
     *
     * @param mon the monitor to alter
     * @throws MonitorFactoryException If an error occurred during the instantiation
     * @throws entropy.PropertiesHelperException
     *                                 if an error occurred while configuring the driver
     */
    private void attachSamplers(Monitor mon) throws PropertiesHelperException, MonitorFactoryException {
        if (this.properties.isDefined(MEM_SAMPLER_IMPL)) {
            String impl = this.properties.getRequiredProperty(MEM_SAMPLER_IMPL);
            if (impl.equals(MEM_SAMPLER_SAMPLER_IMPL)) {
                mon.attach(new MemorySampler(this.properties.getRequiredPropertyAsInt(MEM_SAMPLER_SAMPLER_IMPL_SIZE)));
            } else {
                throw new MonitorFactoryException("Unknown implementation '" + impl + "' for property '" + MEM_SAMPLER_IMPL + "'");
            }
        }
        if (this.properties.isDefined(CPU_SAMPLER_IMPL)) {
            String impl = this.properties.getRequiredProperty(CPU_SAMPLER_IMPL);
            if (impl.equals(CPU_SAMPLER_SAMPLER_IMPL)) {
                mon.attach(new CPUSampler(this.properties.getRequiredPropertyAsInt(CPU_SAMPLER_SAMPLER_IMPL_SIZE)));
            } else if (impl.equals(CPU_SAMPLER_BURNING_IMPL)) {
                mon.attach(new CPUBurningVMsSampler(this.properties.getRequiredPropertyAsInt(CPU_SAMPLER_BURNING_IMPL_SIZE)));
            } else {
                throw new MonitorFactoryException("Unknown implementation '" + impl + "' for property '" + CPU_SAMPLER_IMPL + "'");
            }
        }
    }
}
