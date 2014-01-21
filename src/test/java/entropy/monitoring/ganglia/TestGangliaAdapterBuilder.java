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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for GangliaAdapterBuilder.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestGangliaAdapterBuilder {

    /**
     * Test the creation of a GangliaConfigurationAdapter.
     */
    public void testBuild() {
        try {
            GangliaAdapterBuilder b = new GangliaAdapterBuilder();
            GangliaConfigurationAdapter g = b.build("src/test/resources/entropy/monitoring/ganglia/TestGangliaAdapterBuilder.props.txt");
            Assert.assertEquals(g.getHostname(), "localhost");
            Assert.assertEquals(g.getPort(), 6667);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the creation of the adapter configured in the distribution
     */
    public void testDistributionBuild() {
        try {
            GangliaAdapterBuilder b = new GangliaAdapterBuilder();
            GangliaConfigurationAdapter g = b.build("src/main/config/ganglia.properties");
            Assert.assertEquals(g.getHostname(), "localhost");
            Assert.assertEquals(g.getPort(), 8651);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }
}
