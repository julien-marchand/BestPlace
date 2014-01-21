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

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import choco.kernel.common.logging.ChocoLogging;
import entropy.PropertiesHelper;
import entropy.TestHelper;
import entropy.configuration.Configuration;
import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.SimpleConfiguration;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.SimpleNode;
import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VirtualMachine;
import entropy.plan.CustomizablePlannerModule;
import entropy.plan.MultipleResultingStateException;
import entropy.plan.NoAvailableTransitionException;
import entropy.plan.NonViableSourceConfigurationException;
import entropy.plan.PlanException;
import entropy.plan.SolutionStatistics;
import entropy.plan.SolvingStatistics;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.UnknownResultingStateException;
import entropy.plan.action.Action;
import entropy.plan.action.Migration;
import entropy.plan.action.Shutdown;
import entropy.plan.durationEvaluator.MockDurationEvaluator;
import entropy.vjob.BasicVJob;
import entropy.vjob.ExplodedSet;
import entropy.vjob.LazySpread;
import entropy.vjob.VJob;
import entropy.vjob.builder.ConstraintsCatalogBuilderFromProperties;
import entropy.vjob.builder.MockVirtualMachineBuilder;
import entropy.vjob.builder.VJobBuilder;
import entropy.vjob.builder.VJobElementBuilder;

/**
 * Unit tests for CustomizablePlannerModule.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestChocoCustomRP {

    private static final String RESOURCES_DIR = "src/test/resources/entropy/plan/choco/TestChocoCustomizablePlannerModule.";

    private ChocoCustomRP makeModule() {
        return new ChocoCustomRP(new MockDurationEvaluator(5, 1, 1, 7, 14, 7, 2, 4));
    }

    @Test(expectedExceptions = {PlanException.class})
    public void testWithoutConstraintsAndSolution() throws PlanException {
        ChocoCustomRP planner = makeModule();
        planner.setTimeLimit(10);
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "noSolution.txt");
        List<VJob> vjobs = new ArrayList<VJob>();
        VJob v = new BasicVJob("v1");
        v.addVirtualMachines(new ExplodedSet<VirtualMachine>(src.getRunnings()));
        new MockVirtualMachineBuilder();
        vjobs.add(v);
        try {
            planner.compute(src,
                    src.getRunnings(),
                    src.getWaitings(),
                    src.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    src.getOnlines(),
                    src.getOfflines(),
                    vjobs);

        } catch (MultipleResultingStateException e) {
            Assert.fail(e.getMessage(), e);
        } catch (NoAvailableTransitionException e) {
            Assert.fail(e.getMessage(), e);
        } catch (NonViableSourceConfigurationException e) {
            Assert.fail(e.getMessage(), e);
        } catch (UnknownResultingStateException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void testWithoutVJobs() {
        CustomizablePlannerModule planner = makeModule();
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "empty.txt");
        List<VJob> vjobs = new ArrayList<VJob>();
        new MockVirtualMachineBuilder();
        try {
            TimedReconfigurationPlan plan = planner.compute(src,
                    src.getRunnings(),
                    src.getWaitings(),
                    src.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    src.getOnlines(),
                    src.getOfflines(),
                    vjobs);
            Assert.assertEquals(plan.size(), 0);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    public void testViableWithoutConstraints() {
        //BasicConfigurator.configure();
        CustomizablePlannerModule planner = makeModule();
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "simple.txt");
        List<VJob> vjobs = new ArrayList<VJob>();
        VJob v = new BasicVJob("v1");
        v.addVirtualMachines(new ExplodedSet<VirtualMachine>(src.getRunnings()));
        new MockVirtualMachineBuilder();
        vjobs.add(v);
        try {
            TimedReconfigurationPlan plan = planner.compute(src,
                    src.getRunnings(),
                    src.getWaitings(),
                    src.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    src.getOnlines(),
                    src.getOfflines(),
                    vjobs);
            //System.out.println(plan);
            Assert.assertEquals(plan.size(), 0);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    public void testWithSequences() {
        //ChocoLogging.setVerbosity(Verbosity.SEARCH);
        ChocoCustomRP planner = makeModule();
        planner.setRepairMode(false); //required to have a solution
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "sequencing_src.txt");
        Configuration dst = TestHelper.readConfiguration(RESOURCES_DIR + "sequencing_dst.txt");
        List<VJob> vjobs = new ArrayList<VJob>();
        VJob v = new BasicVJob("v1");
        v.addVirtualMachines(new ExplodedSet<VirtualMachine>(src.getRunnings()));
        new MockVirtualMachineBuilder();
        vjobs.add(v);
        try {
            TimedReconfigurationPlan plan = planner.compute(src,
                    dst.getRunnings(),
                    dst.getWaitings(),
                    dst.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    dst.getOnlines(),
                    dst.getOfflines(),
                    vjobs);
            Assert.assertEquals(plan.size(), 5);
            Shutdown s = null;
            Migration m1 = null, m2 = null, m3 = null, m4 = null;
            for (Action a : plan.getActions()) {
                if (a instanceof Shutdown) {
                    s = (Shutdown) a;
                } else if (a instanceof Migration) {
                    Migration m = (Migration) a;
                    if (m.getVirtualMachine().getName().equals("VM1")) m1 = m;
                    else if (m.getVirtualMachine().getName().equals("VM2")) m2 = m;
                    else if (m.getVirtualMachine().getName().equals("VM3")) m3 = m;
                    else if (m.getVirtualMachine().getName().equals("VM4")) m4 = m;
                }
            }
            Assert.assertTrue(s.getStartMoment() >= m1.getFinishMoment());
            Assert.assertTrue(s.getStartMoment() >= m2.getFinishMoment());
            Assert.assertTrue(m1.getStartMoment() >= m3.getFinishMoment());
            Assert.assertTrue(m2.getStartMoment() >= m4.getFinishMoment());

        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    public void testNonViableWithoutConstraints() {
        //ChocoLogging.setVerbosity(Verbosity.SEARCH);
        CustomizablePlannerModule planner = makeModule();
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "nonViable.txt");
        //System.err.println(src.getCurrentlyOverloadedNodes());     
        List<VJob> vjobs = new ArrayList<VJob>();
        VJob v = new BasicVJob("v1");
        v.addVirtualMachines(new ExplodedSet<VirtualMachine>(src.getRunnings()));
        new MockVirtualMachineBuilder();
        vjobs.add(v);
        try {
            TimedReconfigurationPlan plan = planner.compute(src,
                    src.getRunnings(),
                    src.getWaitings(),
                    src.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    src.getOnlines(),
                    src.getOfflines(),
                    vjobs);
            System.out.println(plan);
            Assert.assertEquals(plan.getDuration(), 5);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test solving with basic constraints.
     */
    public void testWithSatisfiableConstraints() {
        CustomizablePlannerModule planner = makeModule();
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "nonViable.txt");
        List<VJob> vjobs = new ArrayList<VJob>();
        VJob v = new BasicVJob("v1");
        v.addVirtualMachines(new ExplodedSet<VirtualMachine>(src.getRunnings()));
        try {
            v.addConstraint(new LazySpread(new ExplodedSet<VirtualMachine>(v.getVirtualMachines())));
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        new MockVirtualMachineBuilder();
        vjobs.add(v);
        try {
            TimedReconfigurationPlan plan = planner.compute(src,
                    src.getRunnings(),
                    src.getWaitings(),
                    src.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    src.getOnlines(),
                    src.getOfflines(),
                    vjobs);
            System.err.println(plan);
            Assert.assertEquals(plan.size(), 3);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    public void testWithBootAndShutdownActions() {
        //ChocoLogging.setVerbosity(Verbosity.SEARCH);
        ChocoCustomRP planner = makeModule();
        //Check solving statistics
        Assert.assertTrue(planner.getSolvingStatistics() == SolvingStatistics.getStatisticsForNotSolvingProcess());
        Assert.assertEquals(planner.getSolutionsStatistics().size(), 0);

        planner.setRepairMode(false);
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "nonViable.txt");
        src.addOffline(src.getOnlines().get("N9"));
        List<VJob> vjobs = new ArrayList<VJob>();

        ManagedElementSet<Node> offs = new DefaultManagedElementSet<Node>();
        ManagedElementSet<Node> ons = new DefaultManagedElementSet<Node>();
        ons.addAll(src.getOnlines());

        ons.remove(src.getOnlines().get("N3"));
        offs.add(src.getOnlines().get("N3"));

        ons.add(src.getAllNodes().get("N9"));

        VJob v = new BasicVJob("v1");
        v.addVirtualMachines(new ExplodedSet<VirtualMachine>(src.getRunnings()));
        vjobs.add(v);
        try {
            TimedReconfigurationPlan plan = planner.compute(src,
                    src.getRunnings(),
                    src.getWaitings(),
                    src.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    ons,
                    offs,
                    vjobs);
            System.err.println(plan);
            Assert.assertEquals(plan.getActions().size(), 6);

        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        SolvingStatistics st = planner.getSolvingStatistics();
        List<SolutionStatistics> sols = planner.getSolutionsStatistics();

        //Every solutions, asc sorted wrt. solving duration
        Assert.assertTrue(sols.size() > 1);
        for (int i = 1; i < sols.size(); i++) {
            SolutionStatistics s2 = sols.get(i);
            SolutionStatistics s1 = sols.get(i - 1);
            Assert.assertTrue(s2.getNbBacktracks() >= s1.getNbBacktracks() &&
                    s2.getNbNodes() >= s1.getNbNodes() &&
                    s2.getTimeCount() >= s1.getTimeCount() &&
                    s1.getObjective() > s2.getObjective());
        }
        //Solvin statistics at least equals to the last solution
        SolutionStatistics s = sols.get(sols.size() - 1);
        st = planner.getSolvingStatistics();
        Assert.assertTrue(st.getNbBacktracks() >= s.getNbBacktracks() &&
                st.getNbNodes() >= s.getNbNodes() &&
                st.getTimeCount() >= s.getTimeCount());


    }

    /**
     * Solve a plan with some slice with no requirements.
     */
    public void testWithNoRequirements() {
        CustomizablePlannerModule planner = makeModule();
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "noRequirements.txt");
        List<VJob> vjobs = new ArrayList<VJob>();
        VJob v = new BasicVJob("v1");
        v.addVirtualMachines(new ExplodedSet<VirtualMachine>(src.getRunnings()));
        new MockVirtualMachineBuilder();
        vjobs.add(v);
        try {
            TimedReconfigurationPlan plan = planner.compute(src,
                    src.getRunnings(),
                    src.getWaitings(),
                    src.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    src.getOnlines(),
                    src.getOfflines(),
                    vjobs);
            Assert.assertEquals(plan.size(), 0);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    public void testCosts() {
        //ChocoLogging.setVerbosity(Verbosity.SEARCH);
        CustomizablePlannerModule planner = makeModule();
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "testCost.txt");
        List<VJob> vjobs = new ArrayList<VJob>();
        VJob v = new BasicVJob("v1");
        v.addVirtualMachines(new ExplodedSet<VirtualMachine>(src.getRunnings()));
        new MockVirtualMachineBuilder();
        vjobs.add(v);
        try {
            TimedReconfigurationPlan plan = planner.compute(src,
                    src.getRunnings(),
                    src.getWaitings(),
                    src.getSleepings(),
                    new SimpleManagedElementSet<VirtualMachine>(),
                    src.getOnlines(),
                    src.getOfflines(),
                    vjobs);
            System.out.println(plan);
            Assert.assertEquals(plan.size(), 1);
            Assert.assertEquals(plan.getDuration(), 5);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /*public void dummy() {
        ChocoCustomRP planner = makeModule();
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "multipleOneOf_cfg.txt");
        try {
            MockVirtualMachineBuilder builder = new MockVirtualMachineBuilder();
            VJobBuilder b = new VJobBuilder(new VJobElementBuilder(builder), new ConstraintsCatalogBuilderFromProperties(new PropertiesHelper("src/main/config/entropy.properties")).build());
            b.getElementBuilder().useConfiguration(src);
            VJob v = b.build("m", new File(RESOURCES_DIR + "multipleOneOf.txt"));
            List<VJob> vjobs = new ArrayList<VJob>();
            vjobs.add(v);
            TimedReconfigurationPlan plan = planner.compute(src,
                    src.getRunnings(),
                    src.getWaitings(),
                    src.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    src.getOnlines(),
                    src.getOfflines(),
                    vjobs);
            System.out.println(plan);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        } finally {
            ChocoLogging.flushLogs();
        }
    }   */

    public void testMigrateThenRun() {
        //ChocoLogging.setVerbosity(Verbosity.SILENT);
        Node n1 = new SimpleNode("N1", 5, 5, 5);
        Node n2 = new SimpleNode("N2", 4, 4, 4);
        Node n3 = new SimpleNode("N3", 4, 4, 4);

        VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 2, 2, 2);
        VirtualMachine vm2 = new SimpleVirtualMachine("VM2", 2, 5, 5);
        Configuration src = new SimpleConfiguration();
        src.addOnline(n1);
        src.addOnline(n2);
        src.addOnline(n3);
        src.setRunOn(vm1, n1);
        src.addWaiting(vm2);
        ChocoCustomRP planner = makeModule();

        try {
            MockVirtualMachineBuilder builder = new MockVirtualMachineBuilder();
            VJobBuilder b = new VJobBuilder(new VJobElementBuilder(builder), new ConstraintsCatalogBuilderFromProperties(new PropertiesHelper("src/main/config/entropy.properties")).build());
            b.getElementBuilder().useConfiguration(src);
            VJob v = new BasicVJob("V1");
            List<VJob> vjobs = new ArrayList<VJob>();
            vjobs.add(v);
            planner.setRepairMode(false);
            TimedReconfigurationPlan plan = planner.compute(src,
                    src.getAllVirtualMachines(),
                    new SimpleManagedElementSet<VirtualMachine>(),
                    new SimpleManagedElementSet<VirtualMachine>(),
                    new SimpleManagedElementSet<VirtualMachine>(),
                    src.getOnlines(),
                    src.getOfflines(),
                    vjobs);
            System.out.println(plan);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        } finally {
            ChocoLogging.flushLogs();
        }
    }
}
