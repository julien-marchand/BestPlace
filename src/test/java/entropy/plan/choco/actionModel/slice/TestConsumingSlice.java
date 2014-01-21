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

package entropy.plan.choco.actionModel.slice;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.DefaultConfiguration;
import entropy.configuration.DefaultNode;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.TimedReconfigurationPlanModelHelper;

/**
 * Unit tests for ConsumingSlice.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestConsumingSlice {

    public void testInstantiation() {
        DefaultConfiguration src = new DefaultConfiguration();
        DefaultNode n = new DefaultNode("n1", 1, 1, 1);
        src.addOnline(n);
        ReconfigurationProblem m = TimedReconfigurationPlanModelHelper.makeBasicModel(src, src);
        ConsumingSlice slice = new ConsumingSlice(m, "myName", n, 1, 2);
        Assert.assertEquals(slice.start(), m.getStart());
        Assert.assertNotNull(slice.end());
        Assert.assertNotNull(slice.duration());
        Assert.assertEquals(slice.hoster().getDomainSize(), 1);
        Assert.assertEquals(slice.hoster().getInf(), m.getNode(n));
    }

    public void testFixEnd() {
        DefaultConfiguration src = new DefaultConfiguration();
        DefaultNode n = new DefaultNode("n1", 1, 1, 1);
        src.addOnline(n);
        ReconfigurationProblem m = TimedReconfigurationPlanModelHelper.makeBasicModel(src, src);
        ConsumingSlice slice = new ConsumingSlice(m, "myName", n, 1, 2);
        slice.fixEnd(2);
        Assert.assertEquals(slice.end().getDomainSize(), 1);
        Assert.assertEquals(slice.end().getInf(), 2);
        Assert.assertEquals(slice.end().getSup(), 2);
    }
}
