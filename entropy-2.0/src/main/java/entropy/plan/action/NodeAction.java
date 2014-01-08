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
package entropy.plan.action;


import entropy.configuration.Node;

/**
 * An abstract action dedicated to the action that manipulate the state of a node.
 *
 * @author Fabien Hermenier
 */
public abstract class NodeAction extends Action {

    /**
     * The node involved in the action.
     */
    private Node node;

    /**
     * Make a new time-unbounded action specific to a node.
     *
     * @param n the node
     */
    public NodeAction(Node n) {
        this(n, 0, 0);

    }

    /**
     * Make a new time-bounded action.
     *
     * @param n the involved node
     * @param s the moment the execution of the action starts.
     * @param f the moment the execution finish
     */
    public NodeAction(Node n, int s, int f) {
        super(s, f);
        this.node = n;
    }

    /**
     * Get the node involved in the action.
     *
     * @return the node.
     */
    public Node getNode() {
        return node;
    }
}
