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
package entropy.plan;


import entropy.configuration.Configuration;
import entropy.configuration.Node;

/**
 * An exception to describe a non viable source configuration for a planning problem.
 * A configuration is not viable if for one node, the sum of CPU consumptions or  memory consumptions
 * is superior to the CPU or the memory capacity of the node.
 *
 * @author Fabien Hermenier
 */
public class NonViableSourceConfigurationException extends PlanException {

    /**
     * Default serial ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * A sample overloaded node.
     */
    private Node node;

    /**
     * The source configuration.
     */
    private Configuration source;

    /**
     * Make a new exception related to a specific configuration and a sample node.
     *
     * @param src the source configuration
     * @param n   an overloaded node.
     */
    public NonViableSourceConfigurationException(Configuration src, Node n) {
        super("Source configuration is not viable: node '" + n.getName() + "' is overloaded");
        this.source = src;
        this.node = n;
    }

    /**
     * Get the sample overloaded node.
     *
     * @return a node.
     */
    public Node getNode() {
        return this.node;
    }

    /**
     * Get the non viable configuration.
     *
     * @return a configuration
     */
    public Configuration getSourceConfiguration() {
        return this.source;
    }
}
