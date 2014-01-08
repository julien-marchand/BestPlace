/*
 * Copyright (c) 2009 Ecole des Mines de Nantes.
 * 
 *     This file is part of Entropy.
 * 
 *     Entropy is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     Entropy is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 * 
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
*/
package entropy.monitoring.sampler;


import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.TestHelper;
import entropy.configuration.Configuration;

/**
 * Some unit tests for CPUBurningVMsSampler.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestCPUBurningVMsSampler {

    private final static String RESOURCES_DIR = "src/test/resources/entropy/monitoring/sampler/TestCPUBurningVMsSampler.";

    /**
     * Test instantiation and accessors.
     */
    public void testGets() {
        CPUBurningVMsSampler s = new CPUBurningVMsSampler(12);
        Assert.assertEquals(s.getThreshold(), 12);
    }

    @Test(dependsOnMethods = {"testGets"})
    public void test() {
        Configuration pure = TestHelper.readConfiguration(RESOURCES_DIR + "sample.txt");
        ConfigurationSampler sampler = new CPUBurningVMsSampler(500);
        Configuration sampled = sampler.sample(pure);
        Assert.assertEquals(sampled.getOnlines().get("N1").getCPUCapacity(), 2);
        Assert.assertEquals(sampled.getOnlines().get("N2").getCPUCapacity(), 2);
        Assert.assertEquals(sampled.getOnlines().get("N3").getCPUCapacity(), 2);
        Assert.assertEquals(sampled.getOfflines().get("N4").getCPUCapacity(), 2);
        Assert.assertEquals(sampled.getRunnings().get("VM1").getCPUConsumption(), 1);
        Assert.assertEquals(sampled.getRunnings().get("VM2").getCPUConsumption(), 0);
        Assert.assertEquals(sampled.getSleepings().get("VM3").getCPUConsumption(), 1);
        Assert.assertEquals(sampled.getWaitings().get("VM4").getCPUConsumption(), 0);
    }
}
