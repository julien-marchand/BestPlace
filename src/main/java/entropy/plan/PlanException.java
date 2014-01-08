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

/**
 * An exception that occurred during the building of a
 * reconfiguration plan.
 * @author Fabien Hermenier
 *
 */
public class PlanException extends Exception {

	/**
	 * Default Serial UID.
	 */
	private static final long serialVersionUID = -7718478877311948257L;

	/**
	 * The DOT graph that cause the exception.
	 */
	private String dot;
	/**
	 * Create a new exception with a specified error message.
	 * @param dotGraph the graph in DOT format
	 * @param msg the error message
	 */
	public PlanException(String dotGraph, String msg) {
		super(msg);
		this.dot = dotGraph;
	}
	
	/**
	 * Create a new exception.
	 * @param msg the error message
	 */
	public PlanException(String msg) {
		super(msg);
	}

    /**
     * An exception that re-throw another exception.
     * @param msg the error message
     * @param t the exception to re-throw
     */
    public PlanException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
	 * Get the Graph that cause the exception.
	 * @return the graph in the DOT format
	 */
	public String getDotGraph() {
		return this.dot;
	}

}
