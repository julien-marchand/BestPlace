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
package entropy.plan.choco;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import choco.kernel.solver.search.ISolutionPool;
import choco.kernel.solver.search.SolutionPoolFactory;
import entropy.TestHelper;
import entropy.configuration.Configuration;
import entropy.configuration.DefaultConfiguration;
import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.DefaultNode;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.SimpleConfiguration;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.SimpleNode;
import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VirtualMachine;
import entropy.plan.MultipleResultingStateException;
import entropy.plan.NoAvailableTransitionException;
import entropy.plan.NonViableSourceConfigurationException;
import entropy.plan.PlanException;
import entropy.plan.SolutionStatistics;
import entropy.plan.SolvingStatistics;
import entropy.plan.UnknownResultingStateException;
import entropy.plan.choco.actionModel.MigratableActionModel;
import entropy.plan.choco.actionModel.ResumeActionModel;
import entropy.plan.choco.actionModel.RunActionModel;
import entropy.plan.choco.actionModel.StayOfflineNodeActionModel;
import entropy.plan.choco.actionModel.SuspendActionModel;
import entropy.plan.choco.actionModel.TimedReconfigurationPlanModelHelper;
import entropy.plan.choco.actionModel.VirtualMachineActionModel;
import entropy.plan.durationEvaluator.MockDurationEvaluator;

/**
 * Unit tests for BasicTimedReconfigurationPlanModel.
 * TODO: tests duration bounds
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestDefaultReconfigurationProblem {

    /**
     * Location of resources used for tests.
     */
    public static final String RESOURCES_LOCATION = "src/test/resources/entropy/plan/choco/TestTimedReconfigurationPlanModel.";

    /**
     * Just check the instantiation, access to the source configuration and start and end moment.
     */
    public void testInstantiation() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        Assert.assertTrue(model.getStart().isInstantiated());
        Assert.assertEquals(model.getStart().getVal(), 0);
        Assert.assertEquals(model.getEnd().getName(), "end");
        Assert.assertEquals(model.getVirtualMachineActions().size(), 7);
        for (VirtualMachineActionModel vma : model.getVirtualMachineActions()) {
            String vmName = vma.getVirtualMachine().getName();
            if (vmName.equals("VM1") || vmName.equals("VM2") || vmName.equals("VM3")
                    || vmName.equals("VM4") || vmName.equals("VM5") || vmName.equals("VM6")
                    || vmName.equals("VM7")) {
                Assert.assertEquals(vma.getClass(), MigratableActionModel.class);
            }
        }


    }

    /**
     * Test the creation and the cache of group of VMs in various conditions.
     */
    /*public void testGetGroupOfVMs() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);

        Assert.assertEquals(model.getVMGroups().size(), 0);
        ManagedElementSet<VirtualMachine> t1 = new SimpleManagedElementSet<VirtualMachine>();
        t1.add(src.getRunnings().get("VM1"));
        t1.add(src.getRunnings().get("VM2"));
        IntDomainVar v1 = model.getVMGroup(t1);

        //One group was created
        Assert.assertEquals(model.getVMGroups().size(), 1);
        for (VirtualMachine vm : src.getAllVirtualMachines()) {
            IntDomainVar v = model.getAssociatedGroup(vm);
            if (t1.contains(vm)) {
                Assert.assertNotNull(v);
            } else {
                Assert.assertNull(v);
            }
        }
        //No group was created
        IntDomainVar v2 = model.getVMGroup(t1);
        Assert.assertEquals(model.getVMGroups().size(), 1);
        Assert.assertEquals(v1, v2);

        for (VirtualMachine vm : t1) {
            Assert.assertEquals(model.getAssociatedGroup(vm), v1);
        }

        //new group, without overlapping
        ManagedElementSet<VirtualMachine> t2 = new SimpleManagedElementSet<VirtualMachine>();
        t2.add(src.getRunnings().get("VM3"));
        t2.add(src.getRunnings().get("VM4"));
        v2 = model.getVMGroup(t2);
        for (VirtualMachine vm : src.getAllVirtualMachines()) {
            IntDomainVar v = model.getAssociatedGroup(vm);
            if (t1.contains(vm) || t2.contains(vm)) {
                Assert.assertNotNull(v);
            } else {
                Assert.assertNull(v);
            }
        }

        //New group was created
        Assert.assertEquals(model.getVMGroups().size(), 2);
        Assert.assertNotSame(v1, v2);

        //Last, a group with a VM already associated to a group
        ManagedElementSet<VirtualMachine> t3 = new SimpleManagedElementSet<VirtualMachine>();
        t3.add(src.getRunnings().get("VM5"));
        t3.add(src.getRunnings().get("VM6"));
        t3.add(src.getRunnings().get("VM4"));
        Assert.assertNull(model.getVMGroup(t3));
        //No change
        Assert.assertEquals(model.getVMGroups().size(), 2);
        Assert.assertNull(model.getAssociatedGroup(src.getRunnings().get("VM5")));
        Assert.assertNull(model.getAssociatedGroup(src.getRunnings().get("VM6")));
        Assert.assertEquals(v2, model.getAssociatedGroup(src.getRunnings().get("VM4")));
    }    */

    /**
     * Test the creation and the cache of group of nodes.
     */
    public void testGetGroupOfNodes() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);

        Assert.assertEquals(model.getNodesGroups().size(), 0);

        //Create a group
        ManagedElementSet<Node> grp = new SimpleManagedElementSet<Node>();
        grp.add(src.getOnlines().get("N1"));
        grp.add(src.getOnlines().get("N2"));
        int v = model.getGroup(grp);
        Assert.assertEquals(v, 0);
        Assert.assertEquals(model.getNodesGroups().size(), 1);
        for (Node n : grp) {
            Assert.assertTrue(model.getAssociatedGroups(n).contains(0));
            Assert.assertEquals(model.getAssociatedGroups(n).size(), 1);
        }

        //Try to create an already defined group
        grp = new SimpleManagedElementSet<Node>();
        grp.add(src.getOnlines().get("N1"));
        grp.add(src.getOnlines().get("N2"));
        int v2 = model.getGroup(grp);
        Assert.assertEquals(v2, v);
        Assert.assertEquals(model.getNodesGroups().size(), 1);
        for (Node n : grp) {
            Assert.assertEquals(model.getAssociatedGroups(n).size(), 1);
        }

        //A new group with overlapping
        grp = new SimpleManagedElementSet<Node>();
        grp.add(src.getOnlines().get("N1"));
        grp.add(src.getOnlines().get("N3"));
        int v3 = model.getGroup(grp);
        Assert.assertEquals(v3, 1);
        Assert.assertEquals(model.getNodesGroups().size(), 2);
        Assert.assertEquals(model.getAssociatedGroups(src.getOnlines().get("N1")).size(), 2);
        Assert.assertEquals((int) model.getAssociatedGroups(src.getOnlines().get("N1")).get(0), 0);
        Assert.assertEquals((int) model.getAssociatedGroups(src.getOnlines().get("N1")).get(1), 1);

    }

    /**
     * Check an instantiation with a non viable source configuration
     *
     * @throws NonViableSourceConfigurationException
     *          the expected exception
     */
    @Test(expectedExceptions = {NonViableSourceConfigurationException.class})
    public void testInstantiationWithNonViableConfiguration() throws PlanException {
        try {
            Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "bad_src.txt");
            Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
            new DefaultReconfigurationProblem(src,
                    dst.getRunnings(),
                    dst.getWaitings(),
                    dst.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    dst.getOnlines(),
                    dst.getOfflines(),
                    new MockDurationEvaluator(1, 2, 3, 4, 5, 6, 7, 8));

        } catch (NoAvailableTransitionException e) {
            Assert.fail(e.getMessage());
        } catch (MultipleResultingStateException e) {
            Assert.fail(e.getMessage());
        } catch (UnknownResultingStateException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test identify(*) in various situations.
     */
    public void testGetNode() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        Configuration source = model.getSourceConfiguration();
        //Check values with an unexistant node
        Node n = new SimpleNode("fake", 1, 2, 3);
        Assert.assertEquals(model.getNode(n), -1);
        Assert.assertNull(model.getNode(Integer.MAX_VALUE), null);

        //Check the conversion from & to index
        for (Node n2 : source.getOnlines()) {
            int idx = model.getNode(n2);
            Assert.assertNotSame(idx, -1);
            Node rev = model.getNode(idx);
            Assert.assertNotNull(rev);
            Assert.assertEquals(rev, n2);
        }

        for (Node n2 : source.getOfflines()) {
            int idx = model.getNode(n2);
            Assert.assertNotSame(idx, -1);
            Node rev = model.getNode(idx);
            Assert.assertNotNull(rev);
            Assert.assertEquals(rev, n2);
        }
    }

    /**
     * Test getVirtualMachine(*) in various situations.
     */
    public void testGetVirtualMachine() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);

        //Check values with an unexistant virtual machine
        VirtualMachine vm = new SimpleVirtualMachine("fake", 1, 1, 1);
        Assert.assertEquals(model.getVirtualMachine(vm), -1);
        Assert.assertEquals(model.getVirtualMachine(Integer.MAX_VALUE), null);
    }

    /**
     * Test mustBeRunning on a VM already running.
     * We must observe the addition of a migration.
     */
    public void testMustBeRunningWithRunning() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");

        VirtualMachine vm = src.getRunnings().get("VM1");
        dst.setRunOn(vm, dst.getOnlines().get(0));
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        Assert.assertEquals(model.getAssociatedAction(vm).getClass(), MigratableActionModel.class);
    }

    /**
     * Test mustBeRunning on a VM currently sleeping.
     * We must observe the addition of a resume action.
     */
    public void testMustBeRunningWithSleeping() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");

        VirtualMachine vm = src.getSleepings().get("VM10");
        dst.setRunOn(vm, dst.getOnlines().get(0));
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        Assert.assertEquals(model.getAssociatedAction(vm).getClass(), ResumeActionModel.class);
    }

    /**
     * Test mustBeRunning on a VM currently waiting.
     * We must observe the addition of a waiting action.
     */
    public void testMustBeRunningWithWaiting() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");

        VirtualMachine vm = src.getWaitings().get("VM14");
        dst.setRunOn(vm, dst.getOnlines().get(0));

        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        Assert.assertEquals(model.getAssociatedAction(vm).getClass(), RunActionModel.class);
    }

    /**
     * Test mustBeRunning with a VM in an incorrect state.
     *
     * @throws NoAvailableTransitionException the exception we expect
     */
    @Test(expectedExceptions = {NoAvailableTransitionException.class})
    public void testMustbeRunningWithUnknownVM() throws PlanException {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");

        VirtualMachine vm = new SimpleVirtualMachine("fake", 1, 1, 1);
        dst.setRunOn(vm, dst.getOnlines().get(0));
        try {
            new DefaultReconfigurationProblem(src,
                    dst.getRunnings(),
                    dst.getWaitings(),
                    dst.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    dst.getOnlines(),
                    dst.getOfflines(),
                    new MockDurationEvaluator(1, 2, 3, 4, 5, 6, 7, 8));
        } catch (MultipleResultingStateException e) {
            Assert.fail(e.getMessage());
        } catch (NonViableSourceConfigurationException e) {
            Assert.fail(e.getMessage());
        } catch (UnknownResultingStateException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test mustBeSleeping with a running VM.
     * Check the addition of a suspend action.
     */
    public void testMustbeSleepingWithRunning() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");

        VirtualMachine vm = src.getRunnings().get("VM1");
        dst.setSleepOn(vm, dst.getLocation(vm));

        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        Assert.assertEquals(model.getVirtualMachineActions().size(), 7);
        Assert.assertEquals(model.getAssociatedAction(vm).getClass(), SuspendActionModel.class);
    }

    /**
     * Test mustBeSleeping with a VM in a incorrect state.
     *
     * @throws NoAvailableTransitionException the exception we expect
     */
    @Test(expectedExceptions = {NoAvailableTransitionException.class})
    public void testMustBeSleepingWithIncorrectState() throws PlanException {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");

        VirtualMachine vm = new SimpleVirtualMachine("fake", 1, 1, 1);
        dst.setSleepOn(vm, dst.getOnlines().get(0));
        try {
            new DefaultReconfigurationProblem(src,
                    dst.getRunnings(),
                    dst.getWaitings(),
                    dst.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    dst.getOnlines(),
                    dst.getOfflines(),
                    new MockDurationEvaluator(1, 2, 3, 4, 5, 6, 7, 8));
        } catch (MultipleResultingStateException e) {
            Assert.fail(e.getMessage());
        } catch (NonViableSourceConfigurationException e) {
            Assert.fail(e.getMessage());
        } catch (UnknownResultingStateException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test mustBeWaiting with a VM in an incorrect state.
     * must return false
     *
     * @throws NoAvailableTransitionException the error we expect
     */
    @Test(expectedExceptions = {NoAvailableTransitionException.class})
    public void tetsMustBeWaitingWithIncorrectState() throws PlanException {
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "sample_src.txt");
        dst.addWaiting(src.getRunnings().get("VM1"));
        try {
            new DefaultReconfigurationProblem(src,
                    dst.getRunnings(),
                    dst.getWaitings(),
                    dst.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    dst.getOnlines(),
                    dst.getOfflines(),
                    new MockDurationEvaluator(1, 2, 3, 4, 5, 6, 7, 8));
        } catch (MultipleResultingStateException e) {
            Assert.fail(e.getMessage());
        } catch (NonViableSourceConfigurationException e) {
            Assert.fail(e.getMessage());
        } catch (UnknownResultingStateException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void testWithStayOfflines() {
        Node n = new DefaultNode("N1", 1, 1, 1);
        Configuration src = new DefaultConfiguration();
        src.addOffline(n);
        try {
            ReconfigurationProblem m = new DefaultReconfigurationProblem(
                    src,
                    src.getRunnings(),
                    src.getWaitings(),
                    src.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    src.getOnlines(),
                    src.getOfflines(),
                    new MockDurationEvaluator(1, 2, 3, 4, 5, 6, 7, 8));
            Assert.assertEquals(m.getAssociatedAction(n).getClass(), StayOfflineNodeActionModel.class);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    public void testStatistics() {
        Configuration src = new SimpleConfiguration();
        Node n1 = new SimpleNode("N1", 3, 3, 3);
        Node n2 = new SimpleNode("N1", 3, 3, 3);
        src.addOnline(n1);
        src.addOnline(n2);
        VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 1, 1, 2);
        VirtualMachine vm2 = new SimpleVirtualMachine("VM2", 1, 1, 1);
        src.setRunOn(vm1, src.getOnlines().get("N1"));
        src.setRunOn(vm2, src.getOnlines().get("N1"));
        try {
            ReconfigurationProblem m = new DefaultReconfigurationProblem(
                    src,
                    src.getRunnings(),
                    src.getWaitings(),
                    src.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    src.getOnlines(),
                    src.getOfflines(),
                    new MockDurationEvaluator(1, 2, 3, 4, 5, 6, 7, 8));
            m.post(m.leq(m.getEnd(), 2));
            m.setFirstSolution(false);
            m.generateSearchStrategy();

            SolvingStatistics st = m.getSolvingStatistics();
            Assert.assertTrue(st.getNbBacktracks() == 0 && st.getNbNodes() == 0 && st.getTimeCount() == 0);
            Assert.assertEquals(m.getSolutionsStatistics().size(), 0);
            ISolutionPool sp = SolutionPoolFactory.makeInfiniteSolutionPool(m.getSearchStrategy());
            m.getSearchStrategy().setSolutionPool(sp);
            m.launch();
            Boolean ret = m.isFeasible();
            Assert.assertTrue(ret);

            List<SolutionStatistics> sols = m.getSolutionsStatistics();

            //Every solutions, asc sorted wrt. solving duration
            System.err.println(sols);
            for (int i = 1; i < sols.size(); i++) {
                SolutionStatistics s2 = sols.get(i);
                SolutionStatistics s1 = sols.get(i - 1);
                Assert.assertTrue(s2.getNbBacktracks() >= s1.getNbBacktracks());
                Assert.assertTrue(s2.getNbNodes() >= s1.getNbNodes());
                Assert.assertTrue(s2.getTimeCount() >= s1.getTimeCount());
                Assert.assertTrue(s1.getObjective() >= s2.getObjective());
            }
            //Solving statistics at least equals to the last solution
            SolutionStatistics s = sols.get(sols.size() - 1);
            st = m.getSolvingStatistics();
            System.err.println(st);
            Assert.assertTrue(st.getNbBacktracks() >= s.getNbBacktracks());
            Assert.assertTrue(st.getNbNodes() >= s.getNbNodes());
            Assert.assertTrue(st.getTimeCount() >= s.getTimeCount());
            System.err.flush();
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }

    }
}
