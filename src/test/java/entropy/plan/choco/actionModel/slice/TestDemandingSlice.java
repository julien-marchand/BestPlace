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

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.DefaultConfiguration;
import entropy.configuration.DefaultNode;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.TimedReconfigurationPlanModelHelper;

/**
 * Unit tests for DemandingSlice.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestDemandingSlice {

    public void testInstantiation() {
        DefaultConfiguration src = new DefaultConfiguration();
        DefaultNode n = new DefaultNode("n1", 1, 1, 1);
        src.addOnline(n);
        ReconfigurationProblem m = TimedReconfigurationPlanModelHelper.makeBasicModel(src, src);
        DemandingSlice slice = new DemandingSlice(m, "myName", 1, 2);
        Assert.assertEquals(slice.end(), m.getEnd());
        Assert.assertNotNull(slice.start());
        Assert.assertNotNull(slice.duration());
        Assert.assertNotNull(slice.hoster());
    }

    public void testFixStart() {
        DefaultConfiguration src = new DefaultConfiguration();
        DefaultNode n = new DefaultNode("n1", 1, 1, 1);
        src.addOnline(n);
        ReconfigurationProblem m = TimedReconfigurationPlanModelHelper.makeBasicModel(src, src);
        DemandingSlice slice = new DemandingSlice(m, "myName", 1, 2);
        slice.fixStart(0);
        Assert.assertTrue(slice.start().isInstantiated());
        Assert.assertEquals(slice.start().getVal(), 0);
    }

    public void testFixHoster() {
        DefaultConfiguration src = new DefaultConfiguration();
        DefaultNode n = new DefaultNode("n1", 1, 1, 1);
        src.addOnline(n);
        ReconfigurationProblem m = TimedReconfigurationPlanModelHelper.makeBasicModel(src, src);
        DemandingSlice slice = new DemandingSlice(m, "myName", 1, 2);
        slice.fixHoster(0);
        Assert.assertTrue(slice.hoster().isInstantiated());
        Assert.assertEquals(slice.hoster().getVal(), 0);
    }

    public void testHosterBounds() {
        DefaultConfiguration src = new DefaultConfiguration();
        for (int i = 0; i < 21; i++) {
            if (i % 3 == 0) {
                src.addOnline(new DefaultNode("N" + i, 1, 1, 1));
            } else {
                src.addOffline(new DefaultNode("N" + i, 1, 1, 1));
            }
        }
        ReconfigurationProblem m = TimedReconfigurationPlanModelHelper.makeBasicModel(src, src);
        DemandingSlice slice = new DemandingSlice(m, "myName", 1, 2);
        IntDomainVar v = slice.hoster();
        DisposableIntIterator ite = v.getDomain().getIterator();
        while (ite.hasNext()) {
            int idx = ite.next();
            Assert.assertNotNull(m.getNode(idx));
        }
        ite.dispose();

    }
}
