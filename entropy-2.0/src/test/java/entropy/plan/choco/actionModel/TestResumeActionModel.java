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
import entropy.configuration.Configuration;
import entropy.configuration.DefaultVirtualMachine;
import entropy.configuration.Node;
import entropy.configuration.SimpleConfiguration;
import entropy.configuration.SimpleNode;
import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VirtualMachine;
import entropy.plan.action.Resume;
import entropy.plan.choco.ReconfigurationProblem;

/**
 * Unit tests for ResumeActionModel.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestResumeActionModel {

    /**
     * Test the creation of a plan composed with a resume action.
     */
    public void testResumeActionDetection() {
        Configuration src = new SimpleConfiguration();
        Configuration dst = new SimpleConfiguration();
        Node n1 = new SimpleNode("N1", 5, 5, 5);
        VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 1, 1, 1);
        src.addOnline(n1);
        dst.addOnline(n1);
        src.setSleepOn(vm1, n1);
        dst.setRunOn(vm1, n1);
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        ResumeActionModel a = (ResumeActionModel) model.getAssociatedAction(vm1);
        Assert.assertEquals(a.getVirtualMachine(), vm1);
        Assert.assertEquals(a.getDuration().getInf(), 4);
        Assert.assertEquals(a.getDuration().getSup(), 5);
        Assert.assertTrue(model.solve(false));
        Assert.assertNotNull(a.getDemandingSlice());
        Assert.assertEquals(a.getDemandingSlice().getCPUheight(), a.getVirtualMachine().getCPUDemand());
        Assert.assertEquals(a.getDemandingSlice().getMemoryheight(), a.getVirtualMachine().getMemoryDemand());
        Assert.assertNotNull(a.toString());
    }

    /**
     * Test solving with a local resume
     */
    public void testSolvingWithLocalResume() {
        Configuration src = new SimpleConfiguration();
        Configuration dst = new SimpleConfiguration();
        Node n1 = new SimpleNode("N1", 5, 6, 5);
        Node n2 = new SimpleNode("N2", 5, 5, 5);
        VirtualMachine vm = new DefaultVirtualMachine("VM1", 1, 1, 1);
        vm.setCPUDemand(6);
        src.addOnline(n1);
        src.addOnline(n2);
        dst.addOnline(n1);
        dst.addOnline(n2);
        src.setSleepOn(vm, n1);
        dst.setRunOn(vm, n1);

        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        Assert.assertTrue(model.solve(false));
        ResumeActionModel a = (ResumeActionModel) model.getAssociatedAction(vm);
        Resume s = a.getDefinedAction(model);
        Assert.assertEquals(s.getVirtualMachine(), vm);
        Assert.assertEquals(s.getHost(), n1);
        Assert.assertEquals(s.getDestination(), n1);
        Assert.assertEquals(s.getStartMoment(), 0);
        Assert.assertEquals(s.getFinishMoment(), 4);
        Assert.assertEquals(a.getDuration().getVal(), 4);
    }

    /**
     * Test solving with a remote resume.
     * The action is forced to be remote by increasing its CPU needs out of N1 capacity
     */
    public void testSolvingWithRemoteResume() {
        Configuration src = new SimpleConfiguration();
        Configuration dst = new SimpleConfiguration();
        Node n1 = new SimpleNode("N1", 5, 2, 2);
        Node n2 = new SimpleNode("N2", 5, 5, 5);
        VirtualMachine vm = new SimpleVirtualMachine("VM1", 1, 1, 1);
        vm.setCPUDemand(3);
        src.addOnline(n1);
        src.addOnline(n2);
        dst.addOnline(n1);
        dst.addOnline(n2);
        src.setSleepOn(vm, n1);
        dst.setRunOn(vm, n1);

        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        Assert.assertTrue(model.solve(false));
        ResumeActionModel a = (ResumeActionModel) model.getAssociatedAction(vm);
        Resume s = a.getDefinedAction(model);
        Assert.assertEquals(s.getVirtualMachine(), vm);
        Assert.assertEquals(s.getHost(), n1);
        Assert.assertEquals(s.getDestination(), n2);
        Assert.assertEquals(s.getStartMoment(), 0);
        Assert.assertEquals(s.getFinishMoment(), 5);
        Assert.assertEquals(a.getDuration().getVal(), 5);
    }

    /**
     * Test solving with a slice bigger than the duration of the action
     */
    public void testWithBiggerSlice() {
        Configuration src = new SimpleConfiguration();
        Configuration dst = new SimpleConfiguration();
        Node n1 = new SimpleNode("N1", 5, 5, 5);
        Node n2 = new SimpleNode("N2", 5, 5, 5);
        VirtualMachine vm = new SimpleVirtualMachine("VM1", 1, 1, 1);
        src.addOnline(n1);
        src.addOnline(n2);
        dst.addOnline(n1);
        dst.addOnline(n2);
        src.setSleepOn(vm, n1);
        dst.setRunOn(vm, n1);
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        ResumeActionModel a = (ResumeActionModel) model.getAssociatedAction(vm);
        try {
            a.getDemandingSlice().duration().setInf(20);
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage(), e);
        }
        Assert.assertTrue(model.solve(false));

        //Here, we increase the duration of the slice
        Assert.assertEquals(a.getVirtualMachine(), vm);
        Assert.assertNotNull(a.getDemandingSlice());
        Assert.assertNotNull(a.end());
        Resume s = a.getDefinedAction(model);
        Assert.assertEquals(s.getVirtualMachine(), vm);
        Assert.assertEquals(s.getHost(), n1);
        Assert.assertEquals(s.getDestination(), n1);
        Assert.assertEquals(s.getStartMoment(), 0);
        Assert.assertEquals(s.getFinishMoment(), 4);
        Assert.assertEquals(a.getDuration().getVal(), 4);
    }
}
