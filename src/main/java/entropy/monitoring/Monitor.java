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

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import entropy.configuration.Configuration;
import entropy.monitoring.sampler.ConfigurationSampler;

/**
 * A Monitor instance is used to retrieve the current configuration of an architecture.
 * It uses a ConfigurationAdapter to extract the configuration and a set of ConfigurationSampler
 * to simplify the configuration if it is specified.
 * @author Fabien Hermenier
 *
 */
public class Monitor {

    private static Logger logger = LoggerFactory.getLogger("Monitor");
	/**
	 * The list of samplers to apply.
	 */
	private List<ConfigurationSampler> samplers;
	
	/**
	 * The adapter to extract the configuration.
	 */
	private ConfigurationAdapter adapter;
	
	/**
	 * Make a new monitor.
	 * @param a the adapter to use.
	 */
	public Monitor(ConfigurationAdapter a) {
		this.adapter = a;
		this.samplers = new LinkedList<ConfigurationSampler>();
	}
	
	/**
	 * Attach a configuration sampler.
	 * It will be added at the end of the list of samplers.
	 * @param sampler the sampler to attach
	 */
	public void attach(ConfigurationSampler sampler) {
		this.samplers.add(sampler);
	}
	
	/**
	 * Get the configuration.
	 * First the configuration is extracted using the ConfigurationAdapter
	 * then, each attached sampler is used to simplify the configuration.
	 * @return a configuration
	 * @throws MonitoringException if an error occurs during the extraction of the configuration
	 */
	public Configuration getConfiguration() throws MonitoringException {
		Configuration tmp = adapter.extractConfiguration();
		for (ConfigurationSampler sampler : this.samplers) {
			tmp = sampler.sample(tmp);
		}
		return tmp;
	}
	
	/**
	 * Get the configuration adapter.
	 * @return the adapter
	 */
	public ConfigurationAdapter getConfigurationAdapter() {
		return this.adapter;
	}
	
	/**
	 * Get the list of sampler used to simplify the configuration.
	 * @return a list, may be empty
	 */
	public List<ConfigurationSampler> getAttachedSamplers() {
		return this.samplers;
	}

    /**
     * Get the logger.
     * @return an initialized logger
     */
    public static Logger getLogger() {
        return logger;
    }
}
