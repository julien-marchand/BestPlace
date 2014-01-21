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

package entropy.decision;

import java.util.EnumSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.TestHelper;
import entropy.configuration.Configuration;
import entropy.configuration.Configurations;
import entropy.configuration.DefaultVirtualMachine;
import entropy.configuration.Node;
import entropy.configuration.ResourcePicker;
import entropy.configuration.VirtualMachineComparator;

/**
 * Unit tests for the FirstFitPlacement class.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestFirstFitPlacement {

    /**
     * Root dir of tests resources.
     */
    public static final String RESOURCES_DIR = "src/test/resources/entropy/decision/TestFirstFitPlacement.";

    /**
     * Test the standard FirstFitPlacement.
     */
    public void testStandard() {
        try {
            FirstFitPlacement p = new FirstFitPlacement();
            Configuration dst = p.compute(TestHelper.readConfiguration(RESOURCES_DIR + "testStandard.txt"));
            /*for (VirtualMachine vm : dst.getRunnings()) {
                   System.out.println(vm);
               }
               for (Node n : dst.getOnlines()) {
                   System.out.println(n);
               }
               System.out.println(dst);*/
            Assert.assertEquals(Configurations.currentlyOverloadedNodes(dst).size(), 0);
            Assert.assertEquals(Configurations.usedNodes(dst, EnumSet.of(Configurations.State.Runnings)).size(), 3);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test a placement similar to the FirstFitDecrease algorithm.
     */
    public void testFirstFitDecrease() {
        try {
            VirtualMachineComparator cmp = new VirtualMachineComparator(false, ResourcePicker.VMRc.cpuConsumption);
            cmp.appendCriteria(false, ResourcePicker.VMRc.memoryConsumption);
            Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "testFirstFitDecrease.txt");
            FirstFitPlacement p = new FirstFitPlacement(cmp, null);
            Configuration dst = p.compute(src);
            Assert.assertEquals(Configurations.currentlyOverloadedNodes(dst).size(), 0);
            Assert.assertEquals(Configurations.usedNodes(dst, EnumSet.of(Configurations.State.Runnings)).size(), 2);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test that an exception is thrown when an assignment is not possible.
     *
     * @throws AssignmentException the exception we except
     */
    @Test(expectedExceptions = {AssignmentException.class})
    public void testWithNoSolution() throws AssignmentException {
        Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "testWithNoSolution.txt");
        src.setRunOn(new DefaultVirtualMachine("VM5", 1, 0, 3000), src.getOnlines().get(0));
        FirstFitPlacement p = new FirstFitPlacement();
        p.compute(src);
    }

    /**
     * Test the coherence between the source and the destination configuration.
     */
    public void testConfigurationCoherence() {
        try {
            Configuration src = TestHelper.readConfiguration(RESOURCES_DIR + "testFirstFitDecrease.txt");
            VirtualMachineComparator cmp = new VirtualMachineComparator(false, ResourcePicker.VMRc.cpuConsumption);
            cmp.appendCriteria(false, ResourcePicker.VMRc.memoryConsumption);
            FirstFitPlacement p = new FirstFitPlacement(cmp, null);
            Configuration dst = p.compute(src);
            Assert.assertEquals(Configurations.usedNodes(dst, EnumSet.of(Configurations.State.Runnings)).size(), 2);

            for (Node n : dst.getOnlines()) {
                Assert.assertEquals(n.getNbOfCPUs(), 1);
                Assert.assertEquals(n.getMemoryCapacity(), 2048);
            }
            Assert.assertEquals(dst.getOnlines().get("N1").getCPUCapacity(), 100);
            Assert.assertEquals(dst.getOnlines().get("N2").getCPUCapacity(), 200);
            Assert.assertEquals(dst.getOnlines().get("N3").getCPUCapacity(), 400);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage(), e);
        }
    }
}
