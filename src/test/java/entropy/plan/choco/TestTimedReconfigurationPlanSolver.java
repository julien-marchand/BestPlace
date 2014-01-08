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

import org.testng.Assert;
import org.testng.annotations.Test;

import choco.cp.solver.search.integer.valselector.MinVal;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.solver.ContradictionException;
import entropy.TestHelper;
import entropy.configuration.Configuration;
import entropy.configuration.Configurations;
import entropy.configuration.Node;
import entropy.configuration.SimpleConfiguration;
import entropy.configuration.SimpleNode;
import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VirtualMachine;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.action.Action;
import entropy.plan.action.Migration;
import entropy.plan.action.Shutdown;
import entropy.plan.action.Startup;
import entropy.plan.choco.actionModel.TimedReconfigurationPlanModelHelper;

/**
 * Unit tests for TimedReconfigurationPlanSolver.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestTimedReconfigurationPlanSolver {

    /**
     * Location of resources used for tests.
     */
    public static final String RESOURCES_LOCATION = "src/test/resources/entropy/plan/choco/TestTimedReconfigurationPlanSolver.";

    /**
     * Make a migration on a node previously offline.
     * The node must be booted before performing the migration.
     */
    public void testMigrateOnaBootingNode() {
        //ChocoLogging.setVerbosity(Verbosity.SEARCH);
        Node n1 = new SimpleNode("N1", 1, 2, 3);
        Node n2 = new SimpleNode("N2", 1, 4, 5);
        VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 1, 1, 2);
        vm1.setCPUDemand(4);
        vm1.setMemoryDemand(4);
        Configuration src = new SimpleConfiguration();
        src.addOnline(n1);
        src.addOffline(n2);
        src.setRunOn(vm1, n1);
        Configuration dst = src.clone();
        dst.addOnline(n2);
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        Assert.assertTrue(model.solve(false));
        TimedReconfigurationPlan plan = model.extractSolution();
        //System.err.println(plan);
        for (Action a : plan) {
            if (a instanceof Migration) {
                Migration m = (Migration) a;
                Assert.assertEquals(m.getVirtualMachine(), vm1);
                Assert.assertEquals(m.getFinishMoment(), 8);
                Assert.assertEquals(m.getStartMoment(), 7);
                Assert.assertEquals(m.getHost(), n1);
                Assert.assertEquals(m.getDestination(), n2);
            } else if (a instanceof Startup) {
                Startup s = (Startup) a;
                Assert.assertEquals(s.getNode(), n2);
                Assert.assertEquals(s.getStartMoment(), 0);
                Assert.assertEquals(s.getFinishMoment(), 7);
            } else {
                Assert.fail("Bad action: " + a.getClass());
            }
        }
        Configuration c = plan.getDestination();
        Assert.assertEquals(c.getLocation(vm1), n2);
        Assert.assertTrue(c.isOnline(n2));
    }

    /**
     * Test a plan where the destination of a migration has to be
     * booted first while the host is turned off after the migration.
     */
    public void testShutdownAnHostingNode() {
        Node n1 = new SimpleNode("N1", 1, 2, 3);
        Node n2 = new SimpleNode("N2", 1, 4, 5);
        VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 1, 1, 2);
        Configuration src = new SimpleConfiguration();
        src.addOnline(n1);
        src.addOnline(n2);
        src.setRunOn(vm1, n1);
        Configuration dst = new SimpleConfiguration();
        dst.addOnline(n2);
        dst.addOffline(n1);
        dst.setRunOn(vm1, n2);
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        /*try {
            model.getEnd().setSup(30);
        } catch (ContradictionException e) {
            Assert.fail(e.getMessage());
        } */
        Assert.assertTrue(model.minimize(model.getEnd(), false));
        TimedReconfigurationPlan plan = model.extractSolution();
        //Check the plan
        // System.err.println(plan);
        Assert.assertEquals(plan.size(), 2);
        for (Action a : plan) {
            if (a instanceof Shutdown) {
                Shutdown s = (Shutdown) a;
                Assert.assertEquals(s.getNode(), n1);
                Assert.assertEquals(s.getStartMoment(), 1);
                Assert.assertEquals(s.getFinishMoment(), 9);
            } else if (a instanceof Migration) {
                Migration m = (Migration) a;
                Assert.assertEquals(m.getHost(), n1);
                Assert.assertEquals(m.getDestination(), n2);
                Assert.assertEquals(m.getVirtualMachine(), vm1);
                Assert.assertEquals(m.getStartMoment(), 0);
                Assert.assertEquals(m.getFinishMoment(), 1);
            } else {
                Assert.fail("Unexpected action: " + a);
            }
        }
        Configuration c = plan.getDestination();
        Assert.assertTrue(c.isOffline(n1), c.toString());
        Assert.assertEquals(c.getLocation(vm1), n2);
    }

    /**
     * A migration on an offline node, plan to be booted.
     */
    public void testBootingAnHostingNode() {
        ChocoLogging.setVerbosity(Verbosity.SILENT);
        Node n1 = new SimpleNode("N1", 1, 2, 9);
        Node n2 = new SimpleNode("N2", 1, 7, 5);
        VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 1, 1, 2);
        vm1.setCPUDemand(4);
        Configuration src = new SimpleConfiguration();
        src.addOnline(n1);
        src.addOffline(n2);
        src.setRunOn(vm1, n1);
        Configuration dst = new SimpleConfiguration();
        dst.addOnline(n2);
        dst.addOnline(n1);
        dst.setRunOn(vm1, n1);
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        model.setTimeLimit(5000);

        Assert.assertTrue(model.solve(false));
        Assert.assertTrue(model.checkSolution());
        TimedReconfigurationPlan plan = model.extractSolution();
        Configuration c = plan.getDestination();
        Assert.assertEquals(Configurations.futureOverloadedNodes(c).size(), 0);
        //Check the plan
        Assert.assertEquals(plan.size(), 2);
        for (Action a : plan) {
            if (a instanceof Startup) {
                Startup s = (Startup) a;
                Assert.assertEquals(s.getNode(), n2);
                Assert.assertEquals(s.getStartMoment(), 0);
                Assert.assertEquals(s.getFinishMoment(), 7);
            } else if (a instanceof Migration) {
                Migration m = (Migration) a;
                Assert.assertEquals(m.getHost(), n1);
                Assert.assertEquals(m.getDestination(), n2);
                Assert.assertEquals(m.getVirtualMachine(), vm1);
                Assert.assertEquals(m.getStartMoment(), 7);
                Assert.assertEquals(m.getFinishMoment(), 8);
            } else {
                Assert.fail("Unexpected action: " + a);
            }
        }
        Assert.assertEquals(c.getLocation(vm1), n2);
        Assert.assertTrue(c.isOnline(n2));

    }


    /**
     * Test the startup action.
     */
    public void testBoot() {
        Node n1 = new SimpleNode("N1", 1, 1, 1);
        Configuration src = new SimpleConfiguration();
        src.addOffline(n1);
        Configuration dst = src.clone();
        dst.addOnline(n1);
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        Assert.assertEquals(Boolean.TRUE, model.solve());
        TimedReconfigurationPlan plan = model.extractSolution();
        Configuration c = plan.getDestination();
        Assert.assertEquals(plan.size(), 1);
        for (Action a : plan) {
            Assert.assertEquals(a.getClass(), Startup.class);
            Startup s = (Startup) a;
            Assert.assertEquals(s.getNode(), n1);
            Assert.assertEquals(s.getStartMoment(), 0);
            Assert.assertEquals(s.getFinishMoment(), 7);
        }
        Assert.assertTrue(c.isOnline(n1));
    }

    /**
     * Test a shutdown action.
     */
    public void testShutdown() {
        Node n1 = new SimpleNode("N1", 1, 1, 1);
        Configuration src = new SimpleConfiguration();
        src.addOnline(n1);
        Configuration dst = src.clone();
        dst.addOffline(n1);
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        Assert.assertTrue(model.solve(false));
        TimedReconfigurationPlan plan = model.extractSolution();
        Configuration c = plan.getDestination();
        Assert.assertEquals(plan.size(), 1);
        for (Action a : plan) {
            Assert.assertEquals(a.getClass(), Shutdown.class);
            Shutdown s = (Shutdown) a;
            Assert.assertEquals(s.getNode(), n1);
            Assert.assertEquals(s.getStartMoment(), 0);
            Assert.assertEquals(s.getFinishMoment(), 8);
        }
        Assert.assertTrue(c.isOffline(n1));
    }

    /**
     * Test  solve process when all resources requirements are satisfied.
     */
    public void testIdlePlan() {
        //ChocoLogging.setVerbosity(Verbosity.SEARCH);
        Configuration src = TestHelper.readConfiguration(RESOURCES_LOCATION + "smallSequencing_src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_LOCATION + "smallSequencing_dst.txt");
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        try {
            model.getEnd().setSup(50);
        } catch (ContradictionException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(model.solve(), Boolean.TRUE);
        TimedReconfigurationPlan plan = model.extractSolution();
        Configuration c = plan.getDestination();
        Assert.assertEquals(Configurations.currentlyOverloadedNodes(c).size(), 0);
    }

    /**
     * Test strictMaintainOfResources with a feasible problem.
     */
    public void testFeasibleStrictMaintainOfResources() {
        Configuration src = new SimpleConfiguration();
        Configuration dst = new SimpleConfiguration();

        Node n1 = new SimpleNode("N1", 1, 1, 5);
        Node n2 = new SimpleNode("N2", 1, 2, 5);
        src.addOnline(n1);
        src.addOnline(n2);
        dst.addOnline(n1);
        dst.addOnline(n2);
        VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 1, 1, 5);
        vm1.setCPUDemand(2);
        src.setRunOn(vm1, n1);
        dst.setRunOn(vm1, n1);
        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        try {
            model.getEnd().setSup(50);
        } catch (ContradictionException e) {
            Assert.fail(e.getMessage());
        }
        model.setValIntSelector(new MinVal());
        Assert.assertEquals(model.solve(), Boolean.TRUE);
        TimedReconfigurationPlan plan = model.extractSolution();
        for (Action a : plan) {
            Migration m = (Migration) a;
            Assert.assertEquals(m.getDestination(), n2);
        }
        Configuration c = plan.getDestination();
        Assert.assertEquals(c.getLocation(vm1), n2);
    }

    /**
     * Test strictMaintainOfResouces with a unfeasible problem.
     */
    public void testStrictMaintainOfResourcesWithUnFeasiblePb() {

        Node n1 = new SimpleNode("N1", 1, 1, 5);
        //   ChocoLogging.setVerbosity(Verbosity.SEARCH);
        VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 1, 1, 5);
        vm1.setCPUDemand(2);
        Configuration src = new SimpleConfiguration();
        Configuration dst = new SimpleConfiguration();
        src.addOnline(n1);
        dst.addOnline(n1);
        src.setRunOn(vm1, n1);
        dst.setRunOn(vm1, n1);

        ReconfigurationProblem model = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        try {
            model.getEnd().setSup(100);
        } catch (ContradictionException e) {
            Assert.fail(e.getMessage());
        }
        //model.setTimeLimit(10);
        Assert.assertEquals(model.solve(), Boolean.FALSE);
    }

    /**
     * Boot a node then start a VM on it.
     */
    public void testBootThenStart() {
        Node n = new SimpleNode("N1", 1, 1, 1);
        VirtualMachine vm = new SimpleVirtualMachine("VM1", 1, 1, 1);
        Configuration src = new SimpleConfiguration();
        src.addOffline(n);
        src.addWaiting(vm);
        Configuration dst = new SimpleConfiguration();
        dst.addOnline(n);
        dst.setRunOn(vm, n);
        ReconfigurationProblem m = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        Assert.assertTrue(m.solve());
        m.extractSolution();
    }

    public void testWithStayingOfflineNode() {
        Node n = new SimpleNode("N1", 1, 1, 1);
        Configuration src = new SimpleConfiguration();
        src.addOffline(n);
        Configuration dst = new SimpleConfiguration();
        dst.addOffline(n);
        ReconfigurationProblem m = TimedReconfigurationPlanModelHelper.makeBasicModel(src, dst);
        Assert.assertTrue(m.solve());
        m.extractSolution();
    }
}
