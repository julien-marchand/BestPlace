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

package entropy.monitoring.ganglia;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import entropy.configuration.Configuration;
import entropy.configuration.DefaultConfiguration;
import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.monitoring.ConfigurationAdapter;
import entropy.monitoring.Monitor;

/**
 * A SAX2 Parser to create a configuration from a XML Stream. The XML schema corresponds to
 * a GMetad XML output. The GMeta XML must contains some specific metrics in order to identify
 * containers (nodes that host vitual machines) and virtual machines.
 * <p/>
 * <p/>
 * A HOST is considered as a container if all the following metrics are defined. The CPU_CAPACITY
 * of a node by computing the frequency with the number of physical CPUs. The result is rounded using ConfigurationAdapter.round(...).
 * <p/>
 * <table>
 * <tr>
 * <th>Identifier</th>
 * <th>Description</th>
 * <th>Type</th>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_CONTAINER_NB_CPU}</td>
 * <td>Number of physical CPUs</td>
 * <td>uint16</td>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_CONTAINER_TYPE}</td>
 * <td>The type of the container (see <i>supported hypervisors</i>)</td>
 * <td>string</td>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_CONTAINER_MEMORY_TOTAL}</td>
 * <td>total of memory of the container in KB</td>
 * <td>float</td>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_CONTAINER_LIST_VMS}</td>
 * <td>List of each virtual machine name, separated by a space character</td>
 * <td>string</td>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_CONTAINER_STARTUP_DRIVER}</td>
 * <td>The identifier of the driver to use to boot the node</td>
 * <td>string</td>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_CONTAINER_SHUTDOWN_DRIVER}</td>
 * <td>The identifier of the driver to use to shutdown the node</td>
 * <td>string</td>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_CONTAINER_MIGRATION_DRIVER}</td>
 * <td>The identifier of the driver to use to migrate a virtual machine</td>
 * <td>string</td>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_CONTAINER_RUN_DRIVER}</td>
 * <td>The identifier of the driver to use to run a virtual machine</td>
 * <td>string</td>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_CONTAINER_STOP_DRIVER}</td>
 * <td>The identifier of the driver to use to stop a virtual machine</td>
 * <td>string</td>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_CONTAINER_RESUME_DRIVER}</td>
 * <td>The identifier of the driver to use to resume a virtual machine</td>
 * <td>string</td>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_CONTAINER_SUSPEND_DRIVER}</td>
 * <td>The identifier of the driver to use to suspend a virtual machine</td>
 * <td>string</td>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_CPU_FREQUENCY} (<i>standard gmond metric</i>)</td>
 * <td>The frequency of each CPU of the container (in MHz)</td>
 * <td>uint32</td>
 * </tr>
 * <caption>Metrics required by a container</caption>
 * </table>
 * <br/><br/>
 * In order to identify a supposed virtual machine, the HOST must contains the following metrics. All of them
 * are standard gmond metrics.
 * <table>
 * <tr>
 * <th>Identifier</th>
 * <th>Description</th>
 * <th>Type</th>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_NB_CPU}</td>
 * <td>Number of VCPUs used by the virtual machine</td>
 * <td>uint16</td>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_VM_MEMORY_CONSUMPTION}</td>
 * <td>The memory requirement of the virtual machine in KB</td>
 * <td>float</td>
 * </tr>
 * <tr>
 * <td>{@value #METRIC_CPU_FREQUENCY}</td>
 * <td>The frequency of each CPU allocated to the host (in MHz)</td>
 * <td>uint32</td>
 * </tr>
 * <caption>List of metrics required by a virtual machine</caption>
 * </table>
 * <p/>
 * In order to be added into the configuration, each HOST considered as a virtual machine
 * must be hosted on one host that is considered as a container.
 *
 * @author Fabien Hermenier
 */
public class GangliaMetaXMLParser extends DefaultHandler {

    /**
     * Indicates the beginning of the description of a sleeping VM.
     */
    public static final String BEGIN_SLEEPING = "(";

    /**
     * Indicates the end of the description of a sleeping VM.
     */
    public static final String END_SLEEPING = ")";

    /**
     * Metric that indicates the identifier of the driver used to migrate virtual machines.
     */
    public static final String METRIC_CONTAINER_MIGRATION_DRIVER = "container.driver.migration";

    /**
     * Metric that indicates the identifier of the driver used to resume virtual machines.
     */
    public static final String METRIC_CONTAINER_RESUME_DRIVER = "container.driver.resume";

    /**
     * Metric that indicates the identifier of the driver used to suspend virtual machines.
     */
    public static final String METRIC_CONTAINER_SUSPEND_DRIVER = "container.driver.suspend";

    /**
     * Metric that indicates the identifier of the driver used to run virtual machines.
     */
    public static final String METRIC_CONTAINER_RUN_DRIVER = "container.driver.run";

    /**
     * Metric that indicates the identifier of the driver used to migrate virtual machines.
     */
    public static final String METRIC_CONTAINER_STOP_DRIVER = "container.driver.stop";


    /**
     * Metric that indicates the identifier of the driver to use to boot the node.
     */
    public static final String METRIC_CONTAINER_STARTUP_DRIVER = "container.driver.startup";

    /**
     * Metric that indicates the identifier of the driver to use to shutdown the node.
     */
    public static final String METRIC_CONTAINER_SHUTDOWN_DRIVER = "container.driver.shutdown";

    /**
     * Metric that indicates the number of physical CPUs of the host.
     */
    public static final String METRIC_CONTAINER_NB_CPU = "container.cpu_num";

    /**
     * Metric that indicates the amount of memory (in KB) of the host.
     */
    public static final String METRIC_CONTAINER_MEMORY_TOTAL = "container.mem_total";

    /**
     * Metric that indicates the virtual machines hosted by the host.
     */
    public static final String METRIC_CONTAINER_LIST_VMS = "container.vms";

    /**
     * Metric that indicates the type of the container.
     */
    public static final String METRIC_CONTAINER_TYPE = "container.type";

    /**
     * Metric that indicates the frequency of the CPUs allocated to a host.
     */
    public static final String METRIC_CPU_FREQUENCY = "cpu_speed";

    /**
     * Metric that indicates the memory requirement of a host.
     */
    public static final String METRIC_VM_MEMORY_CONSUMPTION = "mem_total";

    /**
     * Metric that indicates the number of CPUs allocated to a host.
     */
    public static final String METRIC_NB_CPU = "cpu_num";

    /**
     * Metric that indicates the percentage of CPU consumed at user level.
     */
    public static final String METRIC_CPU_PCT_USER = "cpu_user";

    /**
     * Metric that indicates the percentage of CPU consumed at a nice level.
     */

    public static final String METRIC_CPU_PCT_NICE = "cpu_nice";

    /**
     * Metric that indicates the percentage of CPU consumed at system level.
     */
    public static final String METRIC_CPU_PCT_SYSTEM = "cpu_system";

    /**
     * The list of all the virtual machines.
     */
    private ManagedElementSet<VirtualMachine> allVMs;

    /**
     * The current configuration that is build.
     */
    private Configuration currentConfiguration;

    /**
     * The IP of the current host.
     */
    private String currentIp;

    /**
     * The hostname of the current host.
     */
    private String currentHostname;

    /**
     * The metrics of the current host.
     */
    private Map<String, String> currentMetrics;

    /**
     * The list of VMs assigned to each node.
     */
    private Map<Node, String> assigns;

    /**
     * The percentage of CPUs used by all supposed VMs.
     */
    private Map<VirtualMachine, Float> cpuPcts;

    /**
     * Contains the identifier of each online host.
     */
    private Set<String> onlines;

    /**
     * The associated configuration adapter.
     */
    private ConfigurationAdapter parent;

    /**
     * Make a new parser.
     *
     * @param cfgAdapter the associated configuration adapter
     */
    public GangliaMetaXMLParser(ConfigurationAdapter cfgAdapter) {
        this.parent = cfgAdapter;

    }

    /**
     * Test is a String describes a sleeping VM
     *
     * @param str the String to analyze
     * @return true if the described VM is sleeping
     */
    private boolean isSleeping(String str) {
        return str.startsWith(BEGIN_SLEEPING) && str.endsWith(END_SLEEPING);
    }

    /**
     * Extract the name of a sleeping VM.
     *
     * @param str the string that identify the VM.
     * @return the name of the VM.
     */
    private String extractVMFromSleeping(String str) {
        return str.substring(1, str.length() - 1);
    }

    @Override
    public void endDocument() throws SAXException {
        /**
         * We make the assignment.
         */
        for (Node n : this.assigns.keySet()) {
            String vms = this.assigns.get(n);
            StringTokenizer st = new StringTokenizer(vms, " ");
            while (st.hasMoreElements()) {

                String buf = st.nextToken();
                if (isSleeping(buf)) {
                    String name = extractVMFromSleeping(buf);
                    VirtualMachine vm = allVMs.get(name);
                    if (vm == null) {
                        //The configuration is incoherent and skipping the virtual machine is not a viable solution.
                        throw new SAXException("Unknown virtual machine '" + name + "'");
                    } else {
                        vm.setCPUConsumption(ConfigurationAdapter.getCPUConsumption(cpuPcts.get(vm), n));
                        //We make the assignment
                        this.currentConfiguration.setSleepOn(vm, n);
                    }
                } else {
                    String name = buf;
                    VirtualMachine vm = allVMs.get(name);
                    if (vm == null) {
                        //The configuration is incoherent and skipping the virtual machine is not a viable solution.
                        throw new SAXException("Unknown virtual machine '" + name + "'");
                    } else {
                        //	We set the CPU consumption
                        vm.setCPUConsumption(ConfigurationAdapter.getCPUConsumption(cpuPcts.get(vm), n));
                        //We make the assignment
                        this.currentConfiguration.setRunOn(vm, n);
                    }
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (name.equals("HOST")) {
            if (isContainer(currentMetrics.keySet())) {
                if (this.parent.getNodesBlackList().contains(this.currentHostname)) {
                    Monitor.getLogger().debug("Ignoring '" + this.currentHostname + "': belong to the nodes black list");
                } else if (this.parent.getNodesWhiteList().size() > 0 && !this.parent.getNodesWhiteList().contains(this.currentHostname)) {
                    Monitor.getLogger().debug("Ignoring '" + this.currentHostname + "': do not belong to the nodes white list");
                } else if (onlines.contains(this.currentHostname)) {
                    Monitor.getLogger().debug(currentHostname + " is considered as a container");
                    float memKb = Integer.parseInt(currentMetrics.get(METRIC_CONTAINER_MEMORY_TOTAL));
                    int cpuCapacity = ConfigurationAdapter.getCPUCapacity(Integer.parseInt(currentMetrics.get(METRIC_CPU_FREQUENCY)),
                            Integer.parseInt(currentMetrics.get(METRIC_CONTAINER_NB_CPU)));
                    Node n = new DefaultNode(this.currentHostname, Integer.parseInt(currentMetrics.get(METRIC_CONTAINER_NB_CPU)),
                            cpuCapacity,
                            (int) memKb / 1000); //Memory in MB

                    n.setHypervisorID(currentMetrics.get(METRIC_CONTAINER_TYPE));
                    n.setIPAddress(this.currentIp);
                    n.setStartupDriverID(currentMetrics.get(METRIC_CONTAINER_STARTUP_DRIVER));
                    n.setShutdownDriverID(currentMetrics.get(METRIC_CONTAINER_SHUTDOWN_DRIVER));
                    n.setMigrationDriverID(currentMetrics.get(METRIC_CONTAINER_MIGRATION_DRIVER));
                    n.setRunDriverID(currentMetrics.get(METRIC_CONTAINER_RUN_DRIVER));
                    n.setResumeDriverID(currentMetrics.get(METRIC_CONTAINER_RESUME_DRIVER));
                    n.setSuspendDriverID(currentMetrics.get(METRIC_CONTAINER_SUSPEND_DRIVER));
                    n.setStopDriverID(currentMetrics.get(METRIC_CONTAINER_STOP_DRIVER));
                    this.currentConfiguration.addOnline(n);
                    this.assigns.put(n, currentMetrics.get(METRIC_CONTAINER_LIST_VMS));
                } else {
                    float memKb = Integer.parseInt(currentMetrics.get(METRIC_CONTAINER_MEMORY_TOTAL));
                    int cpuCapacity = ConfigurationAdapter.getCPUCapacity(Integer.parseInt(currentMetrics.get(METRIC_CPU_FREQUENCY)),
                            Integer.parseInt(currentMetrics.get(METRIC_CONTAINER_NB_CPU)));
                    Node n = new DefaultNode(this.currentHostname, Integer.parseInt(currentMetrics.get(METRIC_CONTAINER_NB_CPU)),
                            cpuCapacity,
                            (int) memKb / 1000); //Memory in MB
                    n.setHypervisorID(currentMetrics.get(METRIC_CONTAINER_TYPE));
                    n.setIPAddress(this.currentIp);
                    n.setStartupDriverID(currentMetrics.get(METRIC_CONTAINER_STARTUP_DRIVER));
                    n.setShutdownDriverID(currentMetrics.get(METRIC_CONTAINER_SHUTDOWN_DRIVER));
                    n.setMigrationDriverID(currentMetrics.get(METRIC_CONTAINER_MIGRATION_DRIVER));
                    n.setRunDriverID(currentMetrics.get(METRIC_CONTAINER_RUN_DRIVER));
                    n.setResumeDriverID(currentMetrics.get(METRIC_CONTAINER_RESUME_DRIVER));
                    n.setSuspendDriverID(currentMetrics.get(METRIC_CONTAINER_SUSPEND_DRIVER));
                    n.setStopDriverID(currentMetrics.get(METRIC_CONTAINER_STOP_DRIVER));
                    this.currentConfiguration.addOffline(n);
                }
            } else if (isVirtualMachine(currentMetrics.keySet())) {
                Monitor.getLogger().debug(currentHostname + " is considered as a virtual machine");
                //We just compute the pct of CPU consumption of the virtual machine and store it
                //We compute the exact value during the assignment
                float cpuPct = Float.parseFloat(this.currentMetrics.get(METRIC_CPU_PCT_NICE))
                        + Float.parseFloat(this.currentMetrics.get(METRIC_CPU_PCT_SYSTEM))
                        + Float.parseFloat(this.currentMetrics.get(METRIC_CPU_PCT_USER));
                VirtualMachine vm = new DefaultVirtualMachine(this.currentHostname, Integer.parseInt(this.currentMetrics.get(METRIC_NB_CPU)),
                        0,
                        Math.round(Float.parseFloat(this.currentMetrics.get(METRIC_VM_MEMORY_CONSUMPTION)) / 1000));
                this.cpuPcts.put(vm, cpuPct);
                this.allVMs.add(vm);
            } else {
                Monitor.getLogger().debug(currentHostname + " is ignored");
            }
        }/* else if (name.equals("HOST") && !this.isHostOnline) {
			logger.debug(" Host '" + this.currentHostname + "' is considered offline");
		}*/
    }

    /**
     * Test if the metrics related to a host correspond to a container.
     *
     * @param metrics the metrics
     * @return true if the host is a container
     */
    private boolean isContainer(Set<String> metrics) {
        return metrics.contains(METRIC_CONTAINER_TYPE)
                && metrics.contains(METRIC_CONTAINER_LIST_VMS)
                && metrics.contains(METRIC_CONTAINER_MEMORY_TOTAL)
                && metrics.contains(METRIC_CONTAINER_NB_CPU)
                && metrics.contains(METRIC_CONTAINER_MIGRATION_DRIVER)
/*			&& metrics.contains(METRIC_CONTAINER_SUSPEND_DRIVER)
			&& metrics.contains(METRIC_CONTAINER_RESUME_DRIVER)
			&& metrics.contains(METRIC_CONTAINER_RUN_DRIVER)
			&& metrics.contains(METRIC_CONTAINER_STOP_DRIVER)*/
                /*
                 * No usuable at the moment, will be used in futur release
                && metrics.contains(METRIC_CONTAINER_STARTUP_DRIVER)
                && metrics.contains(METRIC_CONTAINER_SHUTDOWN_DRIVER)*/;
    }

    /**
     * Test if the metrics related to a host correspond to a virtual machine.
     *
     * @param metrics the metrics
     * @return true if the host is a virtual machine
     */
    private boolean isVirtualMachine(Set<String> metrics) {
        return metrics.contains(METRIC_VM_MEMORY_CONSUMPTION)
                && metrics.contains(METRIC_NB_CPU);
    }

    @Override
    public void startDocument() throws SAXException {
        this.allVMs = new DefaultManagedElementSet<VirtualMachine>();
        this.assigns = new HashMap<Node, String>();
        this.currentConfiguration = new DefaultConfiguration();
        this.cpuPcts = new HashMap<VirtualMachine, Float>();
        this.onlines = new HashSet<String>();
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
        if (name.equals("HOST")) {
            this.currentMetrics = new HashMap<String, String>();
            this.currentHostname = atts.getValue("NAME");
            this.currentIp = atts.getValue("IP");
            int tn = Integer.parseInt(atts.getValue("TN"));
            int tmax = Integer.parseInt(atts.getValue("TMAX"));
            if (isMetricViable(tn, tmax)) {
                this.onlines.add(this.currentHostname);
            }
        } else if (name.equals("METRIC")) {
            this.currentMetrics.put(atts.getValue("NAME"), atts.getValue("VAL"));
        }
    }

    /**
     * Get the configuration after the parsing of the XML Stream.
     *
     * @return a valid configuration
     */
    public Configuration getConfiguration() {
        return this.currentConfiguration;
    }

    /**
     * Indicates wheter a metric if viable or not, given a tolerance and the delay since the last heartbeat.
     *
     * @param tn   the delay since the last hearbeat
     * @param tmax the maximum theorical delay between heartbeat.
     * @return true if the metric is not viable (or up-to-date)
     */
    private boolean isMetricViable(int tn, int tmax) {
        return tn < 4 * tmax;
    }
}
