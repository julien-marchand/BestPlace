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

import entropy.TestHelper;
import entropy.configuration.Configuration;
import entropy.monitoring.sampler.CPUBurningVMsSampler;
import entropy.monitoring.sampler.CPUSampler;
import entropy.monitoring.sampler.MemorySampler;

/**
 * Some unit tests about Monitor.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestMonitor {

    private static final String RESOURCES_DIR = "src/test/resources/entropy/monitoring/TestMonitor.";

    /**
     * Test the initialization.
     */
    public void testInstantation() {
        MockConfigurationAdapter mock = new MockConfigurationAdapter();
        Monitor m = new Monitor(mock);
        Assert.assertEquals(m.getConfigurationAdapter(), mock);
    }

    /**
     * Test the attachment of several sampler and the associated getter.
     */
    public void testAttachSampler() {
        MockConfigurationAdapter mock = new MockConfigurationAdapter();
        Monitor m = new Monitor(mock);
        CPUSampler c = new CPUSampler(12);
        MemorySampler mem = new MemorySampler(12);
        CPUBurningVMsSampler b = new CPUBurningVMsSampler(7);

        m.attach(c);
        m.attach(mem);
        m.attach(b);
        Assert.assertEquals(m.getAttachedSamplers().size(), 3);
        Assert.assertEquals(m.getAttachedSamplers().get(0), c);
        Assert.assertEquals(m.getAttachedSamplers().get(1), mem);
        Assert.assertEquals(m.getAttachedSamplers().get(2), b);
    }

    /**
     * Test getConfiguration with no sampler.
     */
    public void testGetConfigurationWithoutSamplers() {
        MockConfigurationAdapter mock = new MockConfigurationAdapter();
        try {
            Configuration toUse = TestHelper.readConfiguration(RESOURCES_DIR + "sample.txt");
            mock.useConfiguration(toUse);
            Monitor m = new Monitor(mock);
            Assert.assertEquals(m.getConfiguration(), toUse);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test getConfiguration with several sampler to alter the original
     * configuration.
     * Warn: ensure the samplers are rights.
     */
    public void testGetConfigurationWithSamplers() {
        MockConfigurationAdapter mock = new MockConfigurationAdapter();
        try {
            Configuration toUse = TestHelper.readConfiguration(RESOURCES_DIR + "sample.txt");
            mock.useConfiguration(toUse);
            Monitor m = new Monitor(mock);
            m.attach(new CPUSampler(1000));
            m.attach(new MemorySampler(1024));

            Configuration res = m.getConfiguration();
            //A basic check to see if the samplers where used
            Assert.assertEquals(res.getOnlines().get("N1").getMemoryCapacity(), 1);
            Assert.assertEquals(res.getOnlines().get("N1").getCPUCapacity(), 2);
            Assert.assertEquals(res.getOfflines().get("N4").getMemoryCapacity(), 4);
            Assert.assertEquals(res.getOfflines().get("N4").getCPUCapacity(), 2);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }
}
