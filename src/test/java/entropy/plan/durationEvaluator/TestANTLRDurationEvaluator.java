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
package entropy.plan.durationEvaluator;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.PropertiesHelper;
import entropy.TestHelper;
import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;

/**
 * Unit tests for ANTLRDurationEvaluator.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestANTLRDurationEvaluator {

    /**
     * Root of the test resources.
     */
    public static final String RESOURCES_BASE = "src/test/resources/entropy/plan/durationEvaluator/TestANTLRDurationEvaluator.";

    /**
     * Make a default evaluator.
     *
     * @return a DurationEvaluator
     */
    private DurationEvaluator makeDurationEvaluator() {
        try {
            PropertiesHelper prop = TestHelper.readEntropyProperties(RESOURCES_BASE + "expressions.properties");
            return DurationEvaluatorFactory.readFromProperties(prop);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Test evaluateMigration() in various conditions.
     */
    public void testEvaluateMigration() {
        DurationEvaluator ev = makeDurationEvaluator();
        DefaultVirtualMachine vm = new DefaultVirtualMachine("VM1", 1, 2, 3);
        try {
            Assert.assertEquals(ev.evaluateMigration(vm), 3);
            Assert.assertEquals(ev.evaluateMigration(null), -1);
        } catch (DurationEvaluationException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test evaluateStop().
     */
    public void testEvaluateStop() {
        DurationEvaluator ev = makeDurationEvaluator();
        DefaultVirtualMachine vm = new DefaultVirtualMachine("VM1", 1, 2, 3);
        try {
            Assert.assertEquals(ev.evaluateStop(vm), 6);
        } catch (DurationEvaluationException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test evaluateRun().
     */
    public void testEvaluateRun() {
        DurationEvaluator ev = makeDurationEvaluator();
        DefaultVirtualMachine vm = new DefaultVirtualMachine("VM1", 1, 2, 3);
        try {
            Assert.assertEquals(ev.evaluateRun(vm), 4);
        } catch (DurationEvaluationException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test evaluateLocalSuspend().
     */
    public void testEvaluateLocalSuspend() {
        DurationEvaluator ev = makeDurationEvaluator();
        DefaultVirtualMachine vm = new DefaultVirtualMachine("VM1", 1, 2, 3);
        try {
            Assert.assertEquals(ev.evaluateLocalSuspend(vm), 7);
        } catch (DurationEvaluationException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test evaluateLocalResume().
     */
    public void testEvaluateLocalResume() {
        DurationEvaluator ev = makeDurationEvaluator();
        DefaultVirtualMachine vm = new DefaultVirtualMachine("VM1", 1, 2, 3);
        try {
            Assert.assertEquals(ev.evaluateLocalResume(vm), 10);
        } catch (DurationEvaluationException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test evaluateStartup().
     */
    public void testEvaluateStartup() {
        DurationEvaluator ev = makeDurationEvaluator();
        DefaultNode n = new DefaultNode("N1", 1, 1, 1500);
        try {
            Assert.assertEquals(ev.evaluateStartup(n), 3);
        } catch (DurationEvaluationException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test evaluateShutdown().
     */
    public void testEvaluateShutdown() {
        DurationEvaluator ev = makeDurationEvaluator();
        DefaultNode n = new DefaultNode("N1", 2, 1, 1);
        try {
            Assert.assertEquals(ev.evaluateShutdown(n), 11);
        } catch (DurationEvaluationException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test evaluateRemoteResume().
     */
    public void testEvaluateRemoteResume() {
        DurationEvaluator ev = makeDurationEvaluator();
        DefaultVirtualMachine vm = new DefaultVirtualMachine("VM1", 1, 2, 3);
        try {
            Assert.assertEquals(ev.evaluateRemoteResume(vm), 6);
        } catch (DurationEvaluationException e) {
            Assert.fail(e.getMessage());
        }
    }
}
