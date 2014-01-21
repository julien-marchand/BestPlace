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

package entropy.vjob;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;

/**
 * Basic implementation.
 * TODO: Implement equals and hashCode.
 *
 * @author Fabien Hermenier
 */
public class BasicVJob implements VJob {

    /**
     * Identifier of the vjob.
     */
    private String id;

    private static final String EQUALS = " = ";

    private static final String ENDL = ";";

    /**
     * The constraints.
     */
    private List<PlacementConstraint> constraints;


    /**
     * All the elements related to virtual machines.
     */
    private List<VJobSet<VirtualMachine>> vmSets;

    /**
     * All the elements related to nodes.
     */
    private List<VJobSet<Node>> nodeSets;

    /**
     * All the elements related to virtual machines.
     */
    private List<VJobMultiSet<VirtualMachine>> multiVmSets;

    /**
     * All the elements related to nodes.
     */
    private List<VJobMultiSet<Node>> multiNodeSets;

    private List<String> labels;

    private Map<String, VJobElement> variables;

    private ExplodedSet<VirtualMachine> allVMs;

    /**
     * Make a new VJob.
     *
     * @param identifier the identifier of the vjob
     */
    public BasicVJob(String identifier) {
        this.id = identifier;
        this.constraints = new ArrayList<PlacementConstraint>();
        this.vmSets = new LinkedList<VJobSet<VirtualMachine>>();
        this.nodeSets = new LinkedList<VJobSet<Node>>();

        this.multiVmSets = new LinkedList<VJobMultiSet<VirtualMachine>>();
        this.multiNodeSets = new LinkedList<VJobMultiSet<Node>>();

        this.labels = new LinkedList<String>();
        this.variables = new HashMap<String, VJobElement>();
        this.allVMs = new ExplodedSet<VirtualMachine>("$default");
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public boolean addVirtualMachines(VJobSet<VirtualMachine> vms) {
        String lbl = vms.getLabel();
        if (lbl != null) {
            variables.put(lbl, vms);
            labels.add(lbl);
        }
        return vmSets.add(vms);
    }

    @Override
    public boolean addVirtualMachines(VJobMultiSet<VirtualMachine> e) {
        String lbl = e.getLabel();
        if (lbl != null) {
            variables.put(lbl, e);
            labels.add(lbl);
        }
        return multiVmSets.add(e);
    }

    @Override
    public boolean addNodes(VJobSet<Node> nodes) {
        String lbl = nodes.getLabel();
        if (lbl != null) {
            variables.put(lbl, nodes);
            labels.add(lbl);
        }

        return nodeSets.add(nodes);
    }

    @Override
    public boolean addNodes(VJobMultiSet<Node> e) {
        String lbl = e.getLabel();
        if (lbl != null) {
            variables.put(lbl, e);
            labels.add(lbl);
        }

        return multiNodeSets.add(e);
    }

    @Override
    public List<String> getVariables() {
        return labels;
    }

    @Override
    public VJobElement getVariable(String label) {
        return variables.get(label);
    }

    @Override
    public String toString() {

        StringBuilder b = new StringBuilder();
        if (allVMs.size() > 0) {
            b.append(allVMs.getLabel());
            b.append(EQUALS);
            b.append(allVMs.definition());
            b.append(ENDL);
        }
        for (Iterator<String> it = labels.iterator(); it.hasNext();) {
            String lbl = it.next();
            VJobElement e = variables.get(lbl);
            if (e.getElements().size() == 0) {
                continue;
            }
            b.append(e.getLabel());
            b.append(EQUALS);
            b.append(e.definition());
            b.append(ENDL);
            if (it.hasNext() || constraints.size() > 0) {
                b.append("\n");
            }
        }
        for (Iterator<PlacementConstraint> it = constraints.iterator(); it.hasNext();) {
            PlacementConstraint c = it.next();
            b.append(c);
            b.append(ENDL);
            if (it.hasNext()) {
                b.append("\n");
            }
        }
        return b.toString();


    }

    @Override
    public boolean addVirtualMachine(VirtualMachine vm) {
        return allVMs.add(vm);
    }

    @Override
    public ExplodedSet<VirtualMachine> getVirtualMachines() {
        ExplodedSet<VirtualMachine> r = new ExplodedSet<VirtualMachine>(id());
        for (VJobElement<VirtualMachine> virElem : vmSets) {
            r.addAll(virElem.getElements());
        }
        for (VJobElement<VirtualMachine> virElem : multiVmSets) {
            r.addAll(virElem.getElements());
        }
        r.addAll(allVMs);
        return r;
    }

    @Override
    public ExplodedSet<Node> getNodes() {
        ExplodedSet<Node> r = new ExplodedSet<Node>();
        for (VJobElement<Node> phyElem : nodeSets) {
            r.addAll(phyElem.getElements());
        }

        for (VJobElement<Node> phyElem : multiNodeSets) {
            r.addAll(phyElem.getElements());
        }

        return r;
    }

    @Override
    public boolean addConstraint(PlacementConstraint c) {
        //FIXME: if sets of VMs does not belong to the defined sets, they should be declared
        return constraints.add(c);
    }

    @Override
    public boolean removeConstraint(PlacementConstraint c) {
        return constraints.remove(c);
    }

    @Override
    public List<PlacementConstraint> getConstraints() {
        return constraints;
    }

    @Override
    public List<VJobMultiSet<Node>> getMultiNodeSets() {
        return multiNodeSets;
    }

    @Override
    public VJobMultiSet<Node> getMultiNodeSet(String var) {
        for (VJobMultiSet<Node> m : multiNodeSets) {
            if (var.equals(m.getLabel())) {
                return m;
            }
        }
        return null;
    }


    @Override
    public List<VJobMultiSet<VirtualMachine>> getMultiVirtualMachineSets() {
        return multiVmSets;
    }

    @Override
    public VJobMultiSet<VirtualMachine> getMultiVirtualMachineSet(String var) {
        for (VJobMultiSet<VirtualMachine> m : multiVmSets) {
            if (var.equals(m.getLabel())) {
                return m;
            }
        }
        return null;
    }


    @Override
    public List<VJobSet<Node>> getNodeSets() {
        return nodeSets;
    }

    @Override
    public VJobSet<Node> getNodeSet(String var) {
        for (VJobSet<Node> m : nodeSets) {
            if (var.equals(m.getLabel())) {
                return m;
            }
        }
        return null;
    }


    @Override
    public List<VJobSet<VirtualMachine>> getVirtualMachineSets() {
        return vmSets;
    }

    @Override
    public VJobSet<VirtualMachine> getVirtualMachineSet(String var) {
        for (VJobSet<VirtualMachine> m : vmSets) {
            if (var.equals(m.getLabel())) {
                return m;
            }
        }
        return null;
    }

    @Override
    public void store(File f) throws IOException {
        if (!f.getParentFile().exists()) {
            if (!f.getParentFile().mkdirs()) {
                throw new IOException("Unable to create" + f.getName());
            }
        }
        PrintWriter out = new PrintWriter(new FileWriter(f));
        out.print(toString());
        out.close();
    }
}
