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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import entropy.configuration.Configuration;
import entropy.configuration.DefaultNode;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.monitoring.ConfigurationAdapter;
import entropy.monitoring.MockConfigurationAdapter;

/**
 * Unit tests related to GangliaMetaXMLParser.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestGangliaMetaXMLParser {

    /**
     * Base dir of the resources.
     */
    public static final String RESOURCES_DIR = "src/test/resources/entropy/monitoring/ganglia/TestGangliaMetaXMLParser.";

    /**
     * Get the content of a file.
     *
     * @param filename the path of the file
     * @return the content
     * @throws IOException if an error occurs
     */
    private String getFileContent(String filename) throws IOException {
        StringBuilder buffer = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line = br.readLine();
        while (line != null) {
            buffer.append(line);
            line = br.readLine();
        }
        br.close();
        return buffer.toString();
    }

    /**
     * Get a configuration from a XML file.
     *
     * @param filename the pathname of the file
     * @return a configuration
     */
    private Configuration parse(String filename) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        javax.xml.parsers.SAXParser parser = factory.newSAXParser();
        GangliaMetaXMLParser gangliaXMLParser = new GangliaMetaXMLParser(new MockConfigurationAdapter());
        parser.parse(new InputSource(new StringReader(getFileContent(filename))), gangliaXMLParser);
        return gangliaXMLParser.getConfiguration();
    }

    /**
     * Test the parse of common node metrics.
     * TODO: Check MAC Address
     */
    public void checkNodeMetricsParsing() {
        try {
            Configuration c = this.parse(RESOURCES_DIR + "checkNodeMetricsParsing.xml");

            //3 nodes
            Assert.assertEquals(c.getOnlines().size(), 3);
            Assert.assertNotNull(c.getOnlines().get("N1"));
            Assert.assertNotNull(c.getOnlines().get("N2"));
            Assert.assertNotNull(c.getOnlines().get("N3"));
            Node n1 = c.getOnlines().get("N1");

            //Check all the metrics of the first node
            Assert.assertEquals(n1.getCPUCapacity(), 4000);
            Assert.assertEquals(n1.getMemoryCapacity(), 4000);
            Assert.assertEquals(n1.getNbOfCPUs(), 2);
            Assert.assertEquals(n1.getHypervisorID(), "xen-3.3");
            Assert.assertEquals(n1.getMigrationDriverID(), "xen");
            Assert.assertEquals(n1.getStartupDriverID(), "wol");
            Assert.assertEquals(n1.getShutdownDriverID(), "ssh");
            //Assert.assertEquals(n1.getMACAddress(), "00:00:11:11:22:22");
            Assert.assertEquals(n1.getIPAddress(), "127.0.0.1");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the parse of virtual machine metrics.
     * TODO: Check CPU consumption.
     */
    public void checkVirtualMachineMetricsParsing() {
        try {
            Configuration c = this.parse(RESOURCES_DIR + "checkVirtualMachinesMetricsParsing.xml");

            //3 VMs
            Assert.assertEquals(c.getRunnings().size(), 3);
            Assert.assertNotNull(c.getRunnings().get("lenny3"));
            Assert.assertNotNull(c.getRunnings().get("lenny4"));
            Assert.assertNotNull(c.getRunnings().get("lenny5"));
            VirtualMachine vm = c.getRunnings().get("lenny3");

            //Check all the metrics of the fist VM
            Assert.assertEquals(vm.getMemoryConsumption(), 512);
            Assert.assertEquals(vm.getNbOfCPUs(), 1);
            //Assert.assertEquals(vm.getCPUheight(), 1);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the assignment of the virtual machines.
     */
    public void checkAssignmentsParsing() {
        try {
            Configuration c = this.parse(RESOURCES_DIR + "checkAssignmentsParsing.xml");
            VirtualMachine lenny3 = c.getRunnings().get("lenny3");
            VirtualMachine lenny4 = c.getRunnings().get("lenny4");
            VirtualMachine lenny5 = c.getRunnings().get("lenny5");

            Node p10 = c.getOnlines().get("pastel-10.b217.home");
            Node p11 = c.getOnlines().get("pastel-11.b217.home");
            Assert.assertEquals(c.getLocation(lenny3), p10);
            Assert.assertEquals(c.getLocation(lenny4), p11);
            Assert.assertEquals(c.getLocation(lenny5), p10);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the computation of CPU consumption for each virtual machine.
     */
    public void checkCPUConsumptionComputation() {
        try {
            Configuration c = this.parse(RESOURCES_DIR + "checkCPUConsumptionComputation.xml");
            VirtualMachine lenny3 = c.getRunnings().get("lenny3");
            VirtualMachine lenny4 = c.getRunnings().get("lenny4");
            VirtualMachine lenny5 = c.getRunnings().get("lenny5");
            Assert.assertEquals(lenny3.getCPUConsumption(), 1800);
            Assert.assertEquals(lenny4.getCPUConsumption(), 0);
            Assert.assertEquals(lenny5.getCPUConsumption(), 2000);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the assignment of the virtual machines with a invalid virtual machine.
     * A node declare an assignment that does not correspond to a known virtual machine.
     * The virtual machine must be skipped
     *
     * @throws SAXException the Exception we expect.
     */
    @Test(expectedExceptions = {SAXException.class})
    public void checkAssignmentsParsingWithInvalidAssignemnt() throws SAXException {
        try {
            this.parse(RESOURCES_DIR + "checkAssignmentsParsingWithInvalidAssignment.xml");
        } catch (ParserConfigurationException e) {
            Assert.fail(e.getMessage(), e);
        } catch (IOException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the extraction of a configuration with no virtual machines.
     */
    public void testExtractionWithNoVirtualMachines() {
        try {
            Configuration c = this.parse(RESOURCES_DIR + "testWithNoVMs.xml");
            Assert.assertEquals(c.getRunnings().size(), 0);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the extraction of a configuration with no nodes.
     */
    public void testExtractionWithNoNodes() {
        try {
            Configuration c = this.parse(RESOURCES_DIR + "testWithNoNodes.xml");
            Assert.assertEquals(c.getOnlines().size(), 0);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test that the parser skip nodes in the black list.
     */
    public void testWithNodesBlackList() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            javax.xml.parsers.SAXParser parser = factory.newSAXParser();
            ConfigurationAdapter mock = new MockConfigurationAdapter();
            LinkedList<String> l = new LinkedList<String>();
            l.add("pastel-5.b217.home");
            l.add("pastel-8.b217.home");
            l.add("pastel-2.b217.home");
            mock.setNodesBlackList(l);
            GangliaMetaXMLParser gangliaXMLParser = new GangliaMetaXMLParser(mock);
            parser.parse(new InputSource(new StringReader(getFileContent(RESOURCES_DIR + "sample.xml"))), gangliaXMLParser);
            Configuration c = gangliaXMLParser.getConfiguration();
            Assert.assertFalse(c.getOnlines().contains(new DefaultNode("pastel-2", 1, 2, 3)));
            Assert.assertFalse(c.getOnlines().contains(new DefaultNode("pastel-5", 1, 2, 3)));
            Assert.assertFalse(c.getOnlines().contains(new DefaultNode("pastel-8", 1, 2, 3)));
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the detection of a sleeping but online VM.
     */
    public void testOnlineSleepingVMsDetection() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            javax.xml.parsers.SAXParser parser = factory.newSAXParser();
            ConfigurationAdapter mock = new MockConfigurationAdapter();
            GangliaMetaXMLParser gangliaXMLParser = new GangliaMetaXMLParser(mock);
            parser.parse(new InputSource(new StringReader(getFileContent(RESOURCES_DIR + "onlineSleepingVMsDetection.xml"))), gangliaXMLParser);
            Configuration c = gangliaXMLParser.getConfiguration();
            VirtualMachine vm = c.getSleepings().get("lenny3");
            Assert.assertNotNull(vm);
            Node n = c.getOnlines().get("pastel-10.b217.home");
            Assert.assertEquals(c.getSleepingLocation(vm), n);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the detection of a sleeping but online VM.
     */
    public void testOfflineSleepingVMsDetection() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            javax.xml.parsers.SAXParser parser = factory.newSAXParser();
            ConfigurationAdapter mock = new MockConfigurationAdapter();
            GangliaMetaXMLParser gangliaXMLParser = new GangliaMetaXMLParser(mock);
            parser.parse(new InputSource(new StringReader(getFileContent(RESOURCES_DIR + "onlineSleepingVMsDetection.xml"))), gangliaXMLParser);
            Configuration c = gangliaXMLParser.getConfiguration();
            VirtualMachine vm = c.getSleepings().get("lenny3");
            Assert.assertNotNull(vm);
            Node n = c.getOnlines().get("pastel-10.b217.home");
            Assert.assertEquals(c.getSleepingLocation(vm), n);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the detection of a offline node.
     */
    public void testOfflineNodesDetection() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            javax.xml.parsers.SAXParser parser = factory.newSAXParser();
            ConfigurationAdapter mock = new MockConfigurationAdapter();
            GangliaMetaXMLParser gangliaXMLParser = new GangliaMetaXMLParser(mock);
            parser.parse(new InputSource(new StringReader(getFileContent(RESOURCES_DIR + "offlineNodesDetection.xml"))), gangliaXMLParser);
            Configuration c = gangliaXMLParser.getConfiguration();
            Assert.assertNotNull(c.getOfflines().get("N4"));
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

}
