/*
 * Copyright (c) Fabien Hermenier
 *
 *        This file is part of Entropy.
 *
 *        Entropy is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU Lesser General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        Entropy is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU Lesser General Public License for more details.
 *
 *        You should have received a copy of the GNU Lesser General Public License
 *        along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.plan.choco;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import entropy.PropertiesHelper;
import entropy.TestHelper;
import entropy.configuration.Configuration;
import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.plan.SolutionStatistics;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.durationEvaluator.MockDurationEvaluator;
import entropy.vjob.VJob;
import entropy.vjob.builder.ConstraintsCatalogBuilderFromProperties;
import entropy.vjob.builder.MockVirtualMachineBuilder;
import entropy.vjob.builder.VJobBuilder;
import entropy.vjob.builder.VJobElementBuilder;

/**
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestCustomizableSplitablePlannerModule {

    private static final String RESOURCES_DIR = "src/test/resources/entropy/plan/TestCustomizableSplitablePlannerModule.";

    private CustomizableSplitablePlannerModule makeModule() {
        return new CustomizableSplitablePlannerModule(new MockDurationEvaluator(5, 1, 1, 7, 14, 7, 2, 4));
    }

    public void testBasics() {
        CustomizableSplitablePlannerModule planner = makeModule();
        Assert.assertEquals(planner.getPartitioningMode(), CustomizableSplitablePlannerModule.PartitioningMode.none);
        planner.setPartitioningMode(CustomizableSplitablePlannerModule.PartitioningMode.sequential);
        Assert.assertEquals(planner.getPartitioningMode(), CustomizableSplitablePlannerModule.PartitioningMode.sequential);

        Assert.assertEquals(planner.isRepairModeUsed(), true);
        planner.setRepairMode(false);
        Assert.assertEquals(planner.isRepairModeUsed(), false);

    }

    /**
     * Basic test with not that much action
     */
    public void test1() {
        CustomizableSplitablePlannerModule planner = makeModule();
        ChocoLogging.setVerbosity(Verbosity.SILENT);
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "splitted_cfg.txt");

        try {
            MockVirtualMachineBuilder builder = new MockVirtualMachineBuilder();
            VJobBuilder b = new VJobBuilder(new VJobElementBuilder(builder), new ConstraintsCatalogBuilderFromProperties(new PropertiesHelper("src/main/config/entropy.properties")).build());
            b.getElementBuilder().useConfiguration(src);
            VJob v = b.build("m", new File(RESOURCES_DIR + "splitted.txt"));
            List<VJob> vjobs = new ArrayList<VJob>();
            vjobs.add(v);
            planner.setRepairMode(false);
            planner.setPartitioningMode(CustomizableSplitablePlannerModule.PartitioningMode.none);
            TimedReconfigurationPlan plan = planner.compute(src,
                    src.getRunnings(),
                    src.getWaitings(),
                    src.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    src.getOnlines(),
                    src.getOfflines(),
                    vjobs);
            System.err.println(plan);
            Assert.assertEquals(plan.size(), 2);
            Assert.assertEquals(plan.getDuration(), 5);
            List<SolutionStatistics> stats = planner.getSolutionsStatistics();
            Assert.assertEquals(stats.get(0).getObjective(), 10);

        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Basic test with not that much action
     */
    /*public void test2() {
        //Assert.fail();
        CustomizableSplitablePlannerModule planner = makeModule();
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "splitted_cfg2.txt");

        try {
            MockVirtualMachineBuilder builder = new MockVirtualMachineBuilder();
            VJobBuilder b = new VJobBuilder(new VJobElementBuilder(builder), new ConstraintsCatalogBuilderFromProperties(new PropertiesHelper("src/main/config/entropy.properties")).build());
            b.getElementBuilder().useConfiguration(src);
            VJob v = b.build("m", new File(RESOURCES_DIR + "splitted2.txt"));
            List<VJob> vjobs = new ArrayList<VJob>();
            vjobs.add(v);
            planner.setRepairMode(false);
            planner.setPartitioningMode(CustomizableSplitablePlannerModule.PartitioningMode.sequential);
            planner.setTimeLimit(200);
            TimedReconfigurationPlan plan = planner.compute(src,
                    src.getRunnings(),
                    src.getWaitings(),
                    src.getSleepings(),
                    new DefaultManagedElementSet<VirtualMachine>(),
                    src.getOnlines(),
                    src.getOfflines(),
                    vjobs);
            System.err.println(plan);
            System.err.println(planner.getSolvingStatistics());
            System.err.println(planner.getSolutionsStatistics());
            System.err.flush();
            //Assert.fail();
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }    */
}
