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


import entropy.configuration.Configuration;

/**
 * A utility class to sample the resources in some different ways.
 * 
 * @author Fabien Hermenier
 *
 */
public interface ConfigurationSampler {

	/**
	 * Convert byte into megabyte.
	 */
	int MEGABYTES = 1024 * 1024;
	
	/**
	 * Convert byte into Gigabytes.
	 */
	int GYGABYTES = MEGABYTES * 1024;
	

	/**
	 * Sample the resources of nodes and virtual machines in a configuration.
	 * @param cfg the configuration to manipulate
	 * @return a new configuration, sampled
	 */
	Configuration sample(Configuration cfg);
}
