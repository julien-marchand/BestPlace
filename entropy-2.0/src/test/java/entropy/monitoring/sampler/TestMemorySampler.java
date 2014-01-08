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
package entropy.monitoring.sampler;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.TestHelper;
import entropy.configuration.Configuration;

/**
 * Some unit tests about MemorySampler.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestMemorySampler {

    public static final String RESOURCES_DIR = "src/test/resources/entropy/monitoring/sampler/TestMemorySampler.";

    /**
     * Test the instantiation and getters.
     */
    public void testGets() {
        MemorySampler s = new MemorySampler(12);
        Assert.assertEquals(s.getDivider(), 12);
    }

    public void test() {
        Configuration cfg = TestHelper.readConfiguration(RESOURCES_DIR + "sample.txt");
        ConfigurationSampler s = new MemorySampler(1024);
        Configuration res = s.sample(cfg);
        Assert.assertEquals(res.getOnlines().get("N1").getMemoryCapacity(), 1);
        Assert.assertEquals(res.getOnlines().get("N2").getMemoryCapacity(), 2);
        Assert.assertEquals(res.getOnlines().get("N3").getMemoryCapacity(), 3);
        Assert.assertEquals(res.getOfflines().get("N4").getMemoryCapacity(), 4);

        Assert.assertEquals(res.getRunnings().get("VM1").getMemoryConsumption(), 1);
        Assert.assertEquals(res.getRunnings().get("VM2").getMemoryConsumption(), 2);
        Assert.assertEquals(res.getSleepings().get("VM3").getMemoryConsumption(), 3);
        Assert.assertEquals(res.getWaitings().get("VM4").getMemoryConsumption(), 4);
    }
}
