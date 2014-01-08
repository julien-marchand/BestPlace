/*
 * Copyright (c) Fabien Hermenier
 *
 * This file is part of Entropy.
 *
 * Entropy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Entropy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.plan.choco.actionModel;


import entropy.configuration.Node;

/**
 * An abstract action to model an action focused on a node
 *
 * @author Fabien Hermenier
 */
public abstract class NodeActionModel extends ActionModel {

    /**
     * The node associated to the action.
     */
    private Node node;

    /**
     * Make a new action.
     *
     * @param n the node
     */
    public NodeActionModel(Node n) {
        node = n;
    }

    /**
     * Get the node associated to the action.
     *
     * @return a node
     */
    public Node getNode() {
        return node;
    }
}
