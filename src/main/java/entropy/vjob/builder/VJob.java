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

package entropy.vjob.builder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.vjob.ExplodedSet;
import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJobElement;
import entropy.vjob.VJobMultiSet;
import entropy.vjob.VJobSet;

/**
 * Interface to specify a VJob.
 * A Vjob is composed of virtual machines and physical nodes.
 *
 * @author Fabien Hermenier
 */
public interface VJob {

    Logger logger = LoggerFactory.getLogger("VJob");

    /**
     * The identifier of the vjob.
     *
     * @return a String
     */
    String id();

    /**
     * The virtual machines involved in the vjob.
     *
     * @return a set of virtual machines. May be empty
     */
    ExplodedSet<VirtualMachine> getVirtualMachines();

    /**
     * The nodes involved in the vjob.
     *
     * @return a set of nodes. May be empty
     */
    ExplodedSet<Node> getNodes();

    /**
     * Add a placement constraint on the vjob.
     *
     * @param c the placement constraint
     * @return {@code true} if the constraint is added
     */
    boolean addConstraint(PlacementConstraint c);

    /**
     * Remove a placement constraint on the vjob.
     *
     * @param c the constraint to remove
     * @return {@code true} if the constraint is removed.
     */
    boolean removeConstraint(PlacementConstraint c);

    /**
     * List all the placement constraint of the vjob.
     *
     * @return a list of constraints. May be empty
     */
    List<PlacementConstraint> getConstraints();

    /**
     * Add virtual machines to the vjob.
     *
     * @param e the virtual machines to add
     * @return {@code true} if the virtual machines where added
     */
    boolean addVirtualMachines(VJobSet<VirtualMachine> e);

    /**
     * Add a multi set of virtual machines to the vjob.
     *
     * @param e the multiset to add
     * @return {@code true} if the virtual machines where added
     */
    boolean addVirtualMachines(VJobMultiSet<VirtualMachine> e);

    /**
     * Add a virtual machine.
     *
     * @param vm the virtual machine to add
     * @return {@code true} if the virtual machine is added
     */
    boolean addVirtualMachine(VirtualMachine vm);

    /**
     * Add nodes to the vjob.
     *
     * @param e the nodes to add
     * @return {@code true} if the nodes were added
     */
    boolean addNodes(VJobSet<Node> e);

    /**
     * Add a multi set of nodes to the vjob.
     *
     * @param e the multi set to add
     * @return {@code true} if the nodes where added
     */
    boolean addNodes(VJobMultiSet<Node> e);

    /**
     * Get all the multi sets of nodes.
     *
     * @return a list of multiset, may be empty.
     */
    List<VJobMultiSet<Node>> getMultiNodeSets();

    /**
     * Get a multi set of nodes using its label.
     *
     * @param var the identifier of the multi set
     * @return the multi set of nodes associated to the label if exists. {@code null} otherwise
     */
    VJobMultiSet<Node> getMultiNodeSet(String var);

    /**
     * Get all the multi sets of virtual machines.
     *
     * @return a list of multiset, may be empty.
     */
    List<VJobMultiSet<VirtualMachine>> getMultiVirtualMachineSets();

    /**
     * Get a multi set of virtual machines using its label.
     *
     * @param var the identifier of the multi set
     * @return the multi set of virtual machines associated to the label if exists. {@code null} otherwise
     */
    VJobMultiSet<VirtualMachine> getMultiVirtualMachineSet(String var);

    /**
     * Get all the sets of nodes.
     *
     * @return a list of sets, may be empty.
     */
    List<VJobSet<Node>> getNodeSets();

    /**
     * Get a set of node using its label.
     *
     * @param var the identifier of the set
     * @return the set of nodes associated to the label if exists. {@code null} otherwise
     */
    VJobSet<Node> getNodeSet(String var);

    /**
     * Get all the sets of virtual machines.
     *
     * @return a list of sets, may be empty.
     */
    List<VJobSet<VirtualMachine>> getVirtualMachineSets();

    /**
     * Get a set of virtual machines using its label.
     *
     * @param var the identifier of the set
     * @return the set of virtual machines associated to the label if exists. {@code null} otherwise
     */
    VJobSet<VirtualMachine> getVirtualMachineSet(String var);

    /**
     * Get all the labeled elements.
     *
     * @return a list of labeled elements. May be empty
     */
    Collection<String> getVariables();

    /**
     * Get the element associated to a label.
     *
     * @param label the label
     * @return the element if exists. {@code null} otherwise
     */
    VJobElement getVariable(String label);

    /**
     * Store the vjob into a file.
     *
     * @param f the file
     * @throws IOException if an error occurred while storing the vjob.
     */
    void store(File f) throws IOException;

}
