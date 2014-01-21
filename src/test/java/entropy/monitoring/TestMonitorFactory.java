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
package entropy.monitoring;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.PropertiesHelperException;
import entropy.TestHelper;
import entropy.monitoring.ganglia.GangliaConfigurationAdapter;
import entropy.monitoring.sampler.CPUBurningVMsSampler;
import entropy.monitoring.sampler.CPUSampler;
import entropy.monitoring.sampler.ConfigurationSampler;
import entropy.monitoring.sampler.MemorySampler;

/**
 * Some unit tests about MonitorFactory.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestMonitorFactory {

    private static final String RESOURCES_DIR = "src/test/resources/entropy/monitoring/TestMonitorFactory.";

    /**
     * Test the creation of a MockConfigurationAdapter.
     */
    public void testMockCreation() {
        try {
            MonitorFactory f = new MonitorFactory(TestHelper.readEntropyProperties(RESOURCES_DIR + "createMockImpl.txt"));
            Monitor m = f.createMonitor();
            Assert.assertTrue(m.getConfigurationAdapter() instanceof MockConfigurationAdapter);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the detection of a unknown adapter implementation.
     *
     * @throws MonitorFactoryException the exception we expect
     */
    @Test(expectedExceptions = {MonitorFactoryException.class})
    public void testBadImplCreation() throws MonitorFactoryException {
        try {
            MonitorFactory f = new MonitorFactory(TestHelper.readEntropyProperties(RESOURCES_DIR + "createBadImpl.txt"));
            f.createMonitor();
        } catch (PropertiesHelperException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test with the default properties.
     */
    @Test(dependsOnMethods = {"testLoadingMemorySampler", "testLoadingCPUSampler"})
    public void testDefaultProperties() {
        try {
            MonitorFactory f = new MonitorFactory(TestHelper.readDefaultEntropyProperties());
            Monitor m = f.createMonitor();
            Assert.assertTrue(m.getConfigurationAdapter() instanceof GangliaConfigurationAdapter);

            //TODO: Check the default samplers
            Assert.assertEquals(m.getAttachedSamplers().size(), 2);

            Assert.assertTrue(m.getAttachedSamplers().get(0) instanceof MemorySampler);
            Assert.assertEquals(((MemorySampler) m.getAttachedSamplers().get(0)).getDivider(), 1024);

            Assert.assertTrue(m.getAttachedSamplers().get(1) instanceof CPUSampler);
            Assert.assertEquals(((CPUSampler) m.getAttachedSamplers().get(1)).getDivider(), 250);


        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the load of the blacklist.
     */
    public void testBlackListLoading() {
        MonitorFactory f = new MonitorFactory(TestHelper.readEntropyProperties(RESOURCES_DIR + "testBLloading.txt"));
        try {
            Monitor m = f.createMonitor();
            Assert.assertEquals(m.getConfigurationAdapter().getNodesBlackList().size(), 3);
            Assert.assertTrue(m.getConfigurationAdapter().getNodesBlackList().contains("node1"));
            Assert.assertTrue(m.getConfigurationAdapter().getNodesBlackList().contains("node5"));
            Assert.assertTrue(m.getConfigurationAdapter().getNodesBlackList().contains("node2"));
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    @Test(dependsOnMethods = {"testBlackListLoading"}, expectedExceptions = {MonitorFactoryException.class})
    public void testBlackListLoadingWithBadFile() throws MonitorFactoryException {
        MonitorFactory f = new MonitorFactory(TestHelper.readEntropyProperties(RESOURCES_DIR + "testBadBLloading.txt"));
        try {
            f.createMonitor();
        } catch (PropertiesHelperException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the load of the white list.
     */
    public void testWhiteListLoading() {
        MonitorFactory f = new MonitorFactory(TestHelper.readEntropyProperties(RESOURCES_DIR + "testWLloading.txt"));
        try {
            Monitor m = f.createMonitor();
            Assert.assertEquals(m.getConfigurationAdapter().getNodesWhiteList().size(), 3);
            Assert.assertTrue(m.getConfigurationAdapter().getNodesWhiteList().contains("node1"));
            Assert.assertTrue(m.getConfigurationAdapter().getNodesWhiteList().contains("node5"));
            Assert.assertTrue(m.getConfigurationAdapter().getNodesWhiteList().contains("node2"));
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the detection of an error when both a black and a white lists are defined.
     *
     * @throws MonitorFactoryException The exception we expect
     */
    @Test(dependsOnMethods = {"testWhiteListLoading", "testBlackListLoading"}, expectedExceptions = {MonitorFactoryException.class})
    public void testAvoidingBlackAndWhiteList() throws MonitorFactoryException {
        MonitorFactory m = new MonitorFactory(TestHelper.readEntropyProperties(RESOURCES_DIR + "bothBLandWL.txt"));
        try {
            m.createMonitor();
        } catch (PropertiesHelperException e) {
            Assert.fail(e.getMessage(), e);
        }

    }

    /**
     * Test the addition of a MemorySampler.
     */
    public void testLoadingMemorySampler() {
        MonitorFactory m = new MonitorFactory(TestHelper.readEntropyProperties(RESOURCES_DIR + "testMemorySampler.txt"));
        try {
            Monitor mon = m.createMonitor();
            Assert.assertEquals(mon.getAttachedSamplers().size(), 1);
            ConfigurationSampler s = mon.getAttachedSamplers().get(0);
            Assert.assertTrue(s instanceof MemorySampler);
            Assert.assertEquals(((MemorySampler) s).getDivider(), 1000);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the specification of a bad memory sampler implementation.
     *
     * @throws MonitorFactoryException The exception we expect
     */
    @Test(expectedExceptions = {MonitorFactoryException.class})
    public void testBadMemSamplerImpl() throws MonitorFactoryException {
        MonitorFactory m = new MonitorFactory(TestHelper.readEntropyProperties(RESOURCES_DIR + "testBadMemSamplerImpl.txt"));
        try {
            m.createMonitor();
        } catch (PropertiesHelperException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * The the addition of a CPUSampler.
     */
    public void testLoadingCPUSampler() {
        MonitorFactory m = new MonitorFactory(TestHelper.readEntropyProperties(RESOURCES_DIR + "testCPUSampler.txt"));
        try {
            Monitor mon = m.createMonitor();
            Assert.assertEquals(mon.getAttachedSamplers().size(), 1);
            ConfigurationSampler s = mon.getAttachedSamplers().get(0);
            Assert.assertTrue(s instanceof CPUSampler);
            Assert.assertEquals(((CPUSampler) s).getDivider(), 1000);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the addition of a CPUBurning sampler
     */
    public void testLoadingCPUBurningSampler() {
        MonitorFactory m = new MonitorFactory(TestHelper.readEntropyProperties(RESOURCES_DIR + "testCPUBurnSampler.txt"));
        try {
            Monitor mon = m.createMonitor();
            Assert.assertEquals(mon.getAttachedSamplers().size(), 1);
            ConfigurationSampler s = mon.getAttachedSamplers().get(0);
            Assert.assertTrue(s instanceof CPUBurningVMsSampler);
            Assert.assertEquals(((CPUBurningVMsSampler) s).getThreshold(), 1000);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the specification of a bad CPU sampler implementation.
     *
     * @throws MonitorFactoryException The exception we expect
     */
    @Test(expectedExceptions = {MonitorFactoryException.class})
    public void testBadCPUSamplerImpl() throws MonitorFactoryException {
        MonitorFactory m = new MonitorFactory(TestHelper.readEntropyProperties(RESOURCES_DIR + "testBadMemSamplerImpl.txt"));
        try {
            m.createMonitor();
        } catch (PropertiesHelperException e) {
            Assert.fail(e.getMessage(), e);
        }
    }
}
