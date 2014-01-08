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

package entropy.plan.choco.actionModel;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.DefaultConfiguration;
import entropy.configuration.DefaultNode;
import entropy.plan.action.Shutdown;
import entropy.plan.choco.ReconfigurationProblem;

/**
 * Unit tests for ShutdownActionModel.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestShutdownNodeActionModel {

    public void testActionDetectionAndCreation() {
        DefaultConfiguration src = new DefaultConfiguration();
        DefaultConfiguration dst = new DefaultConfiguration();
        DefaultNode n = new DefaultNode("N1", 1, 1, 1);
        src.addOnline(n);
        dst.addOffline(n);
        ReconfigurationProblem m = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        ShutdownNodeActionModel a = (ShutdownNodeActionModel) m.getAssociatedAction(n);
        Assert.assertEquals(a.getNode(), n);
        Assert.assertEquals(a.getDuration().getVal(), 8);
        Assert.assertNotNull(a.getDemandingSlice());
        Assert.assertEquals(a.getDemandingSlice().getCPUheight(), a.getNode().getCPUCapacity());
        Assert.assertEquals(a.getDemandingSlice().getMemoryheight(), a.getNode().getMemoryCapacity());
        Assert.assertTrue(m.solve());
        Shutdown st = a.getDefinedAction(m);
        Assert.assertEquals(st.getNode(), n);
        Assert.assertEquals(st.getStartMoment(), 0);
        Assert.assertEquals(st.getFinishMoment(), 8);
        Assert.assertEquals(a.getDuration().getVal(), 8);
    }
}
