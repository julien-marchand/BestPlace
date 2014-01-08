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
 * Some unit tests about CPUSampler.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestCPUSampler {

    public static final String RESOURCES_DIR = "src/test/resources/entropy/monitoring/sampler/TestCPUSampler.";

    /**
     * Test instantiation and getters.
     */
    public void testGets() {
        CPUSampler s = new CPUSampler(12);
        Assert.assertEquals(s.getDivider(), 12);
    }

    public void test() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "sample.txt");
        ConfigurationSampler s = new CPUSampler(1000);
        Configuration res = s.sample(src);
        Assert.assertEquals(res.getOnlines().get("N1").getCPUCapacity(), 1);
        Assert.assertEquals(res.getOnlines().get("N2").getCPUCapacity(), 2);
        Assert.assertEquals(res.getOnlines().get("N3").getCPUCapacity(), 3);
        Assert.assertEquals(res.getOfflines().get("N4").getCPUCapacity(), 4);

        Assert.assertEquals(res.getRunnings().get("VM1").getCPUConsumption(), 1);
        Assert.assertEquals(res.getRunnings().get("VM2").getCPUConsumption(), 2);
        Assert.assertEquals(res.getSleepings().get("VM3").getCPUConsumption(), 3);
        Assert.assertEquals(res.getWaitings().get("VM4").getCPUConsumption(), 4);
    }
}
