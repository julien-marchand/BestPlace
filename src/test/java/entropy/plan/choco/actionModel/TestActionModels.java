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

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.DefaultConfiguration;
import entropy.configuration.DefaultNode;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.slice.ConsumingSlice;
import entropy.plan.choco.actionModel.slice.DemandingSlice;
import entropy.plan.choco.actionModel.slice.Slice;

/**
 * Unit tests for ActionModels.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestActionModels {

    private List<ActionModel> makeActionModels() {
        List<ActionModel> l = new ArrayList<ActionModel>();
        DefaultConfiguration src = new DefaultConfiguration();
        DefaultConfiguration dst = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("N1", 1000, 1000, 1000);
        src.addOnline(n1);
        dst.addOnline(n1);
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        for (int i = 0; i < 10; i++) {
            l.add(new MockActionModel(model, "A" + i, i % 3 == 0, i % 2 == 0));
        }
        return l;
    }

    public void testExtractStarts() {
        List<ActionModel> actions = makeActionModels();
        IntDomainVar[] starts = ActionModels.extractStarts(actions.toArray(new ActionModel[actions.size()]));
        for (int i = 0; i < starts.length; i++) {
            Assert.assertEquals(starts[i].getName(), "start(A" + i + ")");
        }
    }

    public void testExtractEnds() {
        List<ActionModel> actions = makeActionModels();
        IntDomainVar[] ends = ActionModels.extractEnds(actions.toArray(new ActionModel[actions.size()]));
        for (int i = 0; i < ends.length; i++) {
            Assert.assertEquals(ends[i].getName(), "end(A" + i + ")");
        }
    }

    public void testExtractSlices() {
        List<ActionModel> actions = makeActionModels();
        List<Slice> slices = ActionModels.extractSlices(actions);
        Assert.assertEquals(slices.size(), 9);
    }

    public void testExtractConsumingSlices() {
        List<ActionModel> actions = makeActionModels();
        List<ConsumingSlice> slices = ActionModels.extractConsumingSlices(actions);
        for (int i = 0; i < slices.size(); i++) {
            Assert.assertTrue(slices.get(i).getName().equals("c(A0)")
                    || slices.get(i).getName().equals("c(A3)")
                    || slices.get(i).getName().equals("c(A6)")
                    || slices.get(i).getName().equals("c(A9)")
            );
        }
    }

    public void testExtractDemandingSlices() {
        List<ActionModel> actions = makeActionModels();
        List<DemandingSlice> slices = ActionModels.extractDemandingSlices(actions);
        for (int i = 0; i < slices.size(); i++) {
            Assert.assertTrue(slices.get(i).getName().equals("d(A0)")
                    || slices.get(i).getName().equals("d(A2)")
                    || slices.get(i).getName().equals("d(A4)")
                    || slices.get(i).getName().equals("d(A6)")
                    || slices.get(i).getName().equals("d(A8)")

            );
        }
    }

    public void testExtractDurations() {
        IntDomainVar[] durations = ActionModels.extractDurations(makeActionModels());
        for (IntDomainVar i : durations) {
            Assert.assertEquals(i.getVal(), 10);
        }
    }
}
