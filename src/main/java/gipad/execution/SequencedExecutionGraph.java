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

package gipad.execution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.discovery.DiscoveryModel.model.Node;
import gipad.plan.action.*;
/**
 * A graph to represent dependencies between the
 * actions to perform on a graph
 *
 * @author Fabien Hermenier
 */
public class SequencedExecutionGraph {

    /**
     * All the actions that terminate on a node.
     */
    private Map<Node, Set<Action>> incomings;

    /**
     * All the actions that starts on a node.
     */
    private Map<Node, Set<Action>> outgoings;

    /**
     * Instantiate a new empty graph.
     */
    public SequencedExecutionGraph() {
        this.incomings = new HashMap<Node, Set<Action>>();
        this.outgoings = new HashMap<Node, Set<Action>>();
    }

    /**
     * Extracts all the dependencies of the graph.
     * A dependencie occures when the start moment of an incoming action
     * is grater or equals to the finish moment of outgoings action that involve a same
     * node.
     *
     * @return a list of dependencies. May be empty
     */
    public Set<Dependencies> extractDependencies() {
        Set<Dependencies> l = new HashSet<Dependencies>();
        for (Map.Entry<Node, Set<Action>> e : this.incomings.entrySet()) {
            for (Action a : e.getValue()) {
                Dependencies dep = new Dependencies(a);
                for (Action a2 : getOutgoingsFor(e.getKey())) {
                    if (a2 != a && a2.getFinishMoment() <= a.getStartMoment()) {
                        dep.addDependency(a2);
                    }
                }
                l.add(dep);
            }
        }
        return l;
    }

    /**
     * Get the incoming actions for a node.
     * An incoming action is an action that have to book resources on the node
     * in order to be executed
     *
     * @param n the node
     * @return a list, may be empty
     */
    public Set<Action> getIncomingsFor(Node n) {
        if (!this.incomings.containsKey(n)) {
            this.incomings.put(n, new HashSet<Action>());
        }
        return this.incomings.get(n);
    }

    /**
     * Get the outgoing actions for a node.
     * An outgoing action is an action that liberates some resources on the node
     * when it is performed
     *
     * @param n the node
     * @return a list, may be empty
     */
    public Set<Action> getOutgoingsFor(Node n) {
        if (!this.outgoings.containsKey(n)) {
            this.outgoings.put(n, new HashSet<Action>());
        }
        return this.outgoings.get(n);
    }

    /**
     * Format the graph as an event-oriented plan using the direct dependencies
     * between the actions.
     *
     * @return a String
     */
    public String toEventAgenda() {
        StringBuilder buffer = new StringBuilder();
        Set<Dependencies> deps = this.extractDependencies();
        Map<Set<Action>, Set<Action>> events = new HashMap<Set<Action>, Set<Action>>();
        for (Dependencies dep : deps) {
            if (!events.containsKey(dep.getUnsatisfiedDependencies())) {
                events.put(dep.getUnsatisfiedDependencies(), new HashSet<Action>());
            }
            events.get(dep.getUnsatisfiedDependencies()).add(dep.getAction());
        }
        for (Map.Entry<Set<Action>, Set<Action>> e : events.entrySet()) {
            if (e.getKey().size() == 0) {
                buffer.append("     ");
            } else {
                for (Iterator<Action> a = e.getKey().iterator(); a.hasNext();) {
                    buffer.append("!").append(a.next());
                    if (a.hasNext()) {
                        buffer.append("& ");
                    }
                }
            }
            buffer.append(" -> ");
            for (Iterator<Action> a = e.getValue().iterator(); a.hasNext();) {
                buffer.append(a.next());
                if (a.hasNext()) {
                    buffer.append(" & ");
                }
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
