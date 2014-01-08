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

package entropy.configuration.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.SimpleConfiguration;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.SimpleNode;
import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VirtualMachine;


/**
 * Serialize and un-serialize a configuration from/to a plain text format, human readable.
 * Mostly the original format of a configuration.
 *
 * @author Fabien Hermenier
 */
public final class PlainTextConfigurationSerializer extends FileConfigurationSerializer {

    private static final PlainTextConfigurationSerializer INSTANCE = new PlainTextConfigurationSerializer();

    /**
     * Default separator that appears before the nodes declaration.
     */
    public static final String LIST_NODES = "#list of nodes";

    /**
     * Default separator that appears before the virtual machines' declaration.
     */
    public static final String LIST_VMS = "#list of VMs";

    /**
     * Default separator that appears before the configuration declaration.
     */
    public static final String CONFIG = "#initial configuration";

    public static final String END_CONFIG = "#end of configuration";

    //Hack for plan without end mark for the configuration.
    public static final String START_PLAN = "#Reconfiguration";

    /**
     * Default field separator.
     */
    public static final String FIELD_SEP = " ";

    /**
     * Indicate the farm of the Virtual machines in waiting state.
     */
    public static final String FARM = "FARM";

    //VirtualMachine Parsing part

    /**
     * Index of the name of the virtual machine.
     */
    public static final int VM_NAME_IDX = 0;

    /**
     * Index of the nb of CPUs value.
     */
    public static final int VM_NB_CPU_IDX = 1;

    /**
     * Index of the cpu consumption value.
     */
    public static final int VM_CONSO_CPU_IDX = 2;

    /**
     * Index of the memory consumption value.
     */
    public static final int VM_CONSO_MEM_IDX = 3;

    /**
     * Number minimum token in the line.
     */
    public static final int VM_NB_MIN_TOKENS = 4;

    /**
     * Index of the lease identifier in the line (optional information).
     */
    public static final int VM_LEASE_IDX = 4;

    /**
     * Field separator to indicate a resource demand.
     */
    public static final String VM_DEMAND_SEP = "->";

    //Node part

    /**
     * Index of the name of the node.
     */
    public static final int NODE_NAME_IDX = 0;

    /**
     * Index of the nb of CPUs value.
     */
    public static final int NODE_NB_CPU_IDX = 1;

    /**
     * Index of the cpu capacity value.
     */
    public static final int NODE_CAPA_CPU_IDX = 2;

    /**
     * Index of the memory capacity value.
     */
    public static final int NODE_CAPA_MEM_IDX = 3;

    /**
     * Number of tokens in a line.
     */
    public static final int NODE_NB_TOKENS = 4;

    private PlainTextConfigurationSerializer() {

    }

    public static PlainTextConfigurationSerializer getInstance() {
        return INSTANCE;
    }

    public Configuration unSerialize(BufferedReader reader) throws IOException, ConfigurationSerializerException {
        Configuration conf = new SimpleConfiguration();

        ManagedElementSet<VirtualMachine> vms = new SimpleManagedElementSet<VirtualMachine>();
        ManagedElementSet<Node> nodes = new SimpleManagedElementSet<Node>();

        String state = null;
        String line = reader.readLine();
        while (line != null && !line.equals(END_CONFIG) && !line.startsWith(START_PLAN)) {
            if (line.startsWith(LIST_NODES)) {
                state = LIST_NODES;
            } else if (line.startsWith(LIST_VMS)) {
                state = LIST_VMS;
            } else if (line.startsWith(CONFIG)) {
                state = CONFIG;
            } else if (line.length() > 0) {
                if (state == null) {
                    throw new ConfigurationSerializerException("Unrecognized characters: " + line);
                } else if (state.equals(LIST_NODES)) {
                    nodes.add(readNode(line));
                } else if (state.equals(LIST_VMS)) {
                    vms.add(readVirtualMachine(line));
                } else if (state.equals(CONFIG)) {
                    String[] toks = line.split(FIELD_SEP);
                    if (toks[0].equals(FARM)) {
                        for (int i = 1; i < toks.length; i++) {
                            VirtualMachine vm = vms.get(toks[i]);
                            if (vm != null) {
                                conf.addWaiting(vm);
                            } else {
                                throw new ConfigurationSerializerException("VirtualMachine '" + toks[i] + "' is unknown");
                            }
                        }
                    } else if (toks[0].startsWith("(")) {
                        String id = toks[0].substring(1, toks[0].indexOf(')'));
                        Node n = nodes.get(id);
                        if (n != null) {
                            conf.addOffline(n);
                        } else {
                            throw new ConfigurationSerializerException("Node '" + id + "' is unknown");
                        }
                    } else {
                        Node n = nodes.get(toks[0]);
                        if (n == null) {
                            throw new ConfigurationSerializerException("Node '" + toks[0] + "' is unknown");
                        }
                        conf.addOnline(n);
                        for (int i = 1; i < toks.length; i++) {
                            if (toks[i].startsWith("(")) {
                                String id = toks[i].substring(1, toks[i].indexOf(')'));
                                VirtualMachine vm = vms.get(id);
                                if (vm == null) {
                                    throw new ConfigurationSerializerException("VirtualMachine '" + id + "' is unknow");
                                } else {
                                    conf.setSleepOn(vm, n);
                                }
                            } else {
                                VirtualMachine vm = vms.get(toks[i]);
                                if (vm == null) {
                                    throw new ConfigurationSerializerException("VirtualMachine '" + toks[i] + "' is unknow");
                                } else {
                                    conf.setRunOn(vm, n);
                                }
                            }
                        }
                    }
                } else {
                    throw new ConfigurationSerializerException("Unrecognized characters: " + line);
                }
            }
            line = reader.readLine();
        }
        return conf;
    }

    @Override
    public Configuration unSerialize(InputStream in) throws IOException, ConfigurationSerializerException {
        return unSerialize(new BufferedReader(new InputStreamReader(in)));
    }

    @Override
    public void serialize(Configuration c, OutputStream o) throws IOException {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(o));
            out.write(LIST_NODES);
            out.write("\n");
            for (Node n : c.getOnlines()) {
                out.write(writeNode(n));
                out.write("\n");
            }
            for (Node n : c.getOfflines()) {
                out.write(writeNode(n));
                out.write("\n");
            }

            out.write(LIST_VMS);
            out.write("\n");
            for (VirtualMachine vm : c.getRunnings()) {
                out.write(writeVirtualMachine(vm));
                out.write("\n");
            }
            for (VirtualMachine vm : c.getSleepings()) {
                out.write(writeVirtualMachine(vm));
                out.write("\n");
            }

            for (VirtualMachine vm : c.getWaitings()) {
                out.write(writeVirtualMachine(vm));
                out.write("\n");
            }

            out.write(CONFIG);
            out.write("\n");
            for (Node n : c.getOnlines()) {
                out.write(n.getName());
                for (VirtualMachine vm : c.getRunnings(n)) {
                    out.write(" " + vm.getName());
                }
                for (VirtualMachine vm : c.getSleepings(n)) {
                    out.write(" (" + vm.getName());
                    out.write(")");
                }
                out.write("\n");
            }
            for (Node n : c.getOfflines()) {
                out.write("(" + n.getName() + ")");
                out.write("\n");
            }
            out.write(FARM);
            for (VirtualMachine vm : c.getWaitings()) {
                out.write(" " + vm.getName());
            }
            out.write("\n");
            out.write(END_CONFIG);
            out.write("\n");
            out.flush();
        } finally {
            /*if (out != null) {
                out.close();
            } */
        }
    }

    private String writeVirtualMachine(VirtualMachine vm) {
        StringBuilder buffer = new StringBuilder(50);
        buffer.append(vm.getName());
        buffer.append(" ");
        buffer.append(vm.getNbOfCPUs());
        buffer.append(" ");
        buffer.append(vm.getCPUConsumption());
        if (vm.getCPUDemand() != vm.getCPUConsumption()) {
            buffer.append(VM_DEMAND_SEP);
            buffer.append(vm.getCPUDemand());
        }
        buffer.append(" ");
        buffer.append(vm.getMemoryConsumption());
        if (vm.getMemoryConsumption() != vm.getMemoryDemand()) {
            buffer.append(VM_DEMAND_SEP);
            buffer.append(vm.getMemoryDemand());
        }

        if (vm.getVJobId() != null) {
            buffer.append(" ");
            buffer.append(vm.getVJobId());
        }
        return buffer.toString();
    }

    private VirtualMachine readVirtualMachine(String line) throws ConfigurationSerializerException {
        String[] toks = line.split(FIELD_SEP);
        if (toks.length < VM_NB_MIN_TOKENS) {
            throw new ConfigurationSerializerException("'" + line + "' should have at lease " + VM_NB_MIN_TOKENS + " tokens");
        }
        String name = toks[VM_NAME_IDX];
        int consoCPU;
        int consoMem;
        int needCPU = -1;
        int needMem = -1;
        int nbCPU = Integer.parseInt(toks[VM_NB_CPU_IDX]);
        if (toks[VM_CONSO_CPU_IDX].contains(VM_DEMAND_SEP)) {
            //String[] ss = toks[VM_CONSO_CPU_IDX].split(VM_DEMAND_SEP);
            //System.err.println(toks[VM_CONSO_CPU_IDX]);
            consoCPU = Integer.parseInt(toks[VM_CONSO_CPU_IDX].substring(0, toks[VM_CONSO_CPU_IDX].indexOf(VM_DEMAND_SEP)));
            needCPU = Integer.parseInt(toks[VM_CONSO_CPU_IDX].substring(toks[VM_CONSO_CPU_IDX].indexOf(VM_DEMAND_SEP) + 2, toks[VM_CONSO_CPU_IDX].length()));
            //System.err.println(toks[VM_CONSO_CPU_IDX] + " " + "|" + consoCPU + "| |" + needCPU+"|");
            /*if ((ss.length != 2) && (ss[0].length() == 0 || ss[1].length() == 0)) {
                throw new ConfigurationSerializerException("Bad syntax for '" + toks[VM_CONSO_CPU_IDX] + "'. '" + VM_DEMAND_SEP + "' must be between 2 integers, whitout spaces");
            } */
            //consoCPU = Integer.parseInt(ss[0]);
            //needCPU = Integer.parseInt(ss[1]);
        } else {
            consoCPU = Integer.parseInt(toks[VM_CONSO_CPU_IDX]);
        }

        if (toks[VM_CONSO_MEM_IDX].contains(VM_DEMAND_SEP)) {
            String[] ss = toks[VM_CONSO_MEM_IDX].split(VM_DEMAND_SEP);
            if ((ss.length != 2) && (ss[0].length() == 0 || ss[1].length() == 0)) {
                throw new ConfigurationSerializerException("Bad syntax for '" + toks[VM_CONSO_MEM_IDX] + "'. '" + VM_DEMAND_SEP + "' must be between 2 integers, whitout spaces");
            }
            consoMem = Integer.parseInt(ss[0]);
            needMem = Integer.parseInt(ss[1]);
        } else {
            consoMem = Integer.parseInt(toks[VM_CONSO_MEM_IDX]);
        }

        if (nbCPU <= 0 || consoCPU < 0 || consoMem < 0) {
            throw new ConfigurationSerializerException("Incorrect value for '" + line + "'. all numbers must be positive");
        }
        VirtualMachine vm = new SimpleVirtualMachine(name, nbCPU, consoCPU, consoMem);
        if (needCPU >= 0) {
            vm.setCPUDemand(needCPU);
        } else {
            vm.setCPUDemand(consoCPU);
        }
        if (needMem >= 0) {
            vm.setMemoryDemand(needMem);
        } else {
            vm.setMemoryDemand(consoMem);
        }
        /*if (toks.length == 5) {
            vm.updateValue(DefaultVirtualMachine.VJOB_ID, toks[VM_LEASE_IDX]);
        } */
        return vm;
    }


    private Node readNode(String line) throws ConfigurationSerializerException {
        String[] toks = line.split(FIELD_SEP);
        if (toks.length != NODE_NB_TOKENS) {
            throw new ConfigurationSerializerException("'" + line + "' should have 4 tokens");
        }
        String name = toks[NODE_NAME_IDX];
        try {
            int nbCPU = Integer.parseInt(toks[NODE_NB_CPU_IDX]);
            int capaCPU = Integer.parseInt(toks[NODE_CAPA_CPU_IDX]);
            int capaMem = Integer.parseInt(toks[NODE_CAPA_MEM_IDX]);
            if (nbCPU <= 0 || capaCPU < 0 || capaMem < 0) {
                throw new ConfigurationSerializerException("Incorrect value for '" + line + "'. all numbers must be positive");
            }
            return new SimpleNode(name, nbCPU, capaCPU, capaMem);
        } catch (NumberFormatException e) {
            throw new ConfigurationSerializerException("Unable to create a Node from '" + line + "':" + e.getMessage());
        }
    }

    private String writeNode(Node node) {
        StringBuilder buffer = new StringBuilder(100);
        buffer.append(node.getName());
        buffer.append(" ");
        buffer.append(node.getNbOfCPUs());
        buffer.append(" ");
        buffer.append(node.getCPUCapacity());
        buffer.append(" ");
        buffer.append(node.getMemoryCapacity());
        return buffer.toString();
    }
}
