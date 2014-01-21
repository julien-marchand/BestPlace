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

import choco.kernel.solver.ContradictionException;
import entropy.configuration.DefaultConfiguration;
import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.plan.action.Suspend;
import entropy.plan.choco.ReconfigurationProblem;

/**
 * Unit tests for SuspendActionModel.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestSuspendActionModel {

    /**
     * Check the creation of a SuspendActionModel
     * solve the problem and get the resulting action
     */
    public void testSolvingWithSuspendActionCreation() {
        DefaultConfiguration src = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("N1", 1, 1, 1);
        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("VM1", 1, 1, 1);
        src.addOnline(n1);
        src.setRunOn(vm1, n1);
        DefaultConfiguration dst = new DefaultConfiguration();
        dst.addOnline(n1);
        dst.setSleepOn(vm1, n1);
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        VirtualMachineActionModel a = model.getAssociatedAction(vm1);
        Assert.assertEquals(a.getClass(), SuspendActionModel.class);
        SuspendActionModel vma = (SuspendActionModel) a;
        Assert.assertEquals(a.getDuration().getVal(), 6);
        Assert.assertNotNull(vma.getConsumingSlice());
        Assert.assertEquals(vma.getConsumingSlice().getCPUheight(), vm1.getCPUConsumption());
        Assert.assertEquals(vma.getConsumingSlice().getMemoryheight(), vm1.getMemoryConsumption());
        Assert.assertTrue(model.solve(false));

        Suspend s = vma.getDefinedAction(model);
        Assert.assertEquals(s.getVirtualMachine(), vm1);
        Assert.assertEquals(s.getHost(), n1);
        Assert.assertEquals(s.getDestination(), n1);
        Assert.assertEquals(s.getStartMoment(), 0);
        Assert.assertEquals(s.getFinishMoment(), 6);
        Assert.assertEquals(a.getDuration().getVal(), 6);
    }

    /**
     * Test with a slice bigger than the duration of the action.
     */
    public void testSolvingWithBiggerSlice() {
        DefaultConfiguration src = new DefaultConfiguration();
        DefaultNode n1 = new DefaultNode("N1", 1, 1, 1);
        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("VM1", 1, 1, 1);
        src.addOnline(n1);
        src.setRunOn(vm1, n1);
        DefaultConfiguration dst = new DefaultConfiguration();
        dst.addOnline(n1);
        dst.setSleepOn(vm1, n1);
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        VirtualMachineActionModel a = model.getAssociatedAction(vm1);
        //Increase the duration of the slice
        try {
            a.getConsumingSlice().duration().setInf(20);
        } catch (ContradictionException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(a.getClass(), SuspendActionModel.class);
        SuspendActionModel vma = (SuspendActionModel) a;
        Assert.assertNotNull(vma.getConsumingSlice());

        Assert.assertTrue(model.solve(false));

        Suspend s = vma.getDefinedAction(model);
        Assert.assertEquals(s.getVirtualMachine(), vm1);
        Assert.assertEquals(s.getHost(), n1);
        Assert.assertEquals(s.getDestination(), n1);
        Assert.assertEquals(s.getStartMoment(), 0);
        Assert.assertEquals(s.getFinishMoment(), 6);
    }
}
