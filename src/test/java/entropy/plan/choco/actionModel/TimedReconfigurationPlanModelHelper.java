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

import entropy.configuration.Configuration;
import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.plan.choco.DefaultReconfigurationProblem;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.durationEvaluator.DurationEvaluator;
import entropy.plan.durationEvaluator.MockDurationEvaluator;

/**
 * A helper class to create simple BasicTimedReconfigurationPlanModel.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TimedReconfigurationPlanModelHelper {

    /**
     * Make a model that aims to pass from a source to a destination configuration.
     *
     * @param src The source configuration
     * @param dst The destination configuration
     * @return the model
     */
    public static ReconfigurationProblem makeBasicModel(Configuration src, Configuration dst) {
        DurationEvaluator e;
        try {
            e = new MockDurationEvaluator(1, 2, 3, 4, 5, 6, 7, 8);
            ManagedElementSet<VirtualMachine> toTerminate = new DefaultManagedElementSet<VirtualMachine>(src.getAllVirtualMachines());
            toTerminate.removeAll(dst.getAllVirtualMachines());
            return new DefaultReconfigurationProblem(src,
                    dst.getRunnings(),
                    dst.getWaitings(),
                    dst.getSleepings(),
                    toTerminate,
                    src.getAllVirtualMachines(),
                    dst.getOnlines(),
                    dst.getOfflines(),
                    e);
        } catch (Exception e2) {
            Assert.fail(e2.getMessage(), e2);
        }
        return null;
    }
}
