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
package entropy.controlLoop;


import java.io.File;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.TestHelper;
import entropy.configuration.Configuration;
import entropy.configuration.Configurations;
import entropy.execution.TimedReconfigurationExecuter;
import entropy.execution.driver.MockDriverFactory;
import entropy.monitoring.MockConfigurationAdapter;
import entropy.monitoring.Monitor;
import entropy.plan.action.Action;
import entropy.plan.durationEvaluator.DurationEvaluator;
import entropy.plan.durationEvaluator.MockDurationEvaluator;
import entropy.vjob.builder.MockVirtualMachineBuilder;
import entropy.vjob.builder.VJobBuilder;
import entropy.vjob.builder.VJobElementBuilder;
import entropy.vjob.queue.FCFSPersistentQueue;
import entropy.vjob.queue.VJobsPool;

/**
 * Unit tests for CustomizableControlLoop.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestCustomizableControlLoop {

    private static final String RESOURCES_DIR = "src/test/resources/entropy/controlLoop/TestCustomizableControlLoop.";

    /*public void testsWithNoVJobs() {
        try {
            Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "emptyConf.txt");
            MockConfigurationAdapter mockConfAdapter = new MockConfigurationAdapter();
            mockConfAdapter.useConfiguration(src);
            MockVirtualMachineBuilder vmBuilder = new MockVirtualMachineBuilder();
            DurationEvaluator eval = new MockDurationEvaluator(5, 2, 2, 3, 6, 3, 1, 1);
            MockDriverFactory mockDrvFactory = new MockDriverFactory();
            VJobBuilder b = new VJobBuilder(new VJobElementBuilder(vmBuilder), null);
            VJobsPool queue = new FCFSPersistentQueue(b, new File(RESOURCES_DIR + "emptyQueue/"));
            CustomizableControlLoop loop = new CustomizableControlLoop(new Monitor(mockConfAdapter), queue, b, eval, new TimedReconfigurationExecuter(mockDrvFactory));
            loop.setAssignTimeout(30);
            loop.setPlanTimeout(30);
            loop.setMasterVJobFile(RESOURCES_DIR + "parts.txt");
            loop.runLoop();
            Assert.assertEquals(mockDrvFactory.getNbActions(), 0);
            //Thread.sleep(30000);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }       */

    /**
     * 2 VJobs without constraints. Just ensure the configuration will be maintained as viable.
     */
    public void testWithNoConstraintedVJobs() {
        try {
            Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "2VJobsStart.txt");
            MockConfigurationAdapter mockConfAdapter = new MockConfigurationAdapter();
            mockConfAdapter.useConfiguration(src);
            MockVirtualMachineBuilder vmBuilder = new MockVirtualMachineBuilder();
            DurationEvaluator eval = new MockDurationEvaluator(5, 2, 2, 3, 6, 3, 1, 1);
            MockDriverFactory mockDrvFactory = new MockDriverFactory();
            VJobBuilder b = new VJobBuilder(new VJobElementBuilder(vmBuilder), null);
            VJobsPool queue = new FCFSPersistentQueue(b, new File(RESOURCES_DIR + "2VJobs/"));
            CustomizableControlLoop loop = new CustomizableControlLoop(new Monitor(mockConfAdapter), queue, b, eval, new TimedReconfigurationExecuter(mockDrvFactory));
            loop.setAssignTimeout(30);
            loop.setPlanTimeout(30);
            loop.setMasterVJobFile(RESOURCES_DIR + "parts.txt");
            loop.runLoop();
            List<Action> actions = mockDrvFactory.getPerformed();
            Assert.assertEquals(actions.size(), 2);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    public void testConfLighter() {
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "confLighter.txt");
        CustomizableControlLoop.lightConfiguration(src);
        Assert.assertEquals(Configurations.currentlyOverloadedNodes(src).size(), 0);
    }

}
