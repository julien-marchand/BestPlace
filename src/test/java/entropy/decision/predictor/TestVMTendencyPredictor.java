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

package entropy.decision.predictor;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.DefaultVirtualMachine;

/**
 * Unit tests for VMTendencyPredictor
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestVMTendencyPredictor {

    public void regularInc() {
        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("VM1", 1, 1, 1);
        VMTendencyPredictor p1 = new VMTendencyPredictor(5);
        p1.predictCPUDemand(vm1);
        Assert.assertEquals(vm1.getCPUDemand(), vm1.getCPUConsumption());
        for (int i = 2; i <= 5; i++) {
            vm1 = new DefaultVirtualMachine("VM1", 1, i, 1);
            p1.predictCPUDemand(vm1);
            //System.err.println(vm1 + " " + p1);
            System.err.println(vm1.getCPUConsumption() + " " + vm1.getCPUDemand());
            Assert.assertEquals(vm1.getCPUDemand(), i + 1);
        }
    }

    public void testWithProbalityCurve() {
        //BasicConfigurator.configure();
        DefaultVirtualMachine vm1 = new DefaultVirtualMachine("VM1", 1, 1, 1);
        VMTendencyPredictor p1 = new VMTendencyPredictor(50);
        int wasDemanded = -1;
        int t = 0;
        for (double i = 0.0; i < Math.PI * 2; i += 0.1, t++) {
            int x = (int) (Math.sin(i) * 1000) + 1000;
            //System.err.println((int)(Math.sin(i) * 1000));
            vm1 = new DefaultVirtualMachine("VM1", 1, x, 1);
            p1.predictCPUDemand(vm1);
            System.err.print(t + " " + vm1.getCPUConsumption() + " ");
            if (wasDemanded != -1) {
                System.err.println(wasDemanded);
            } else {
                System.err.println("-");
            }
            wasDemanded = vm1.getCPUDemand();
            Assert.assertTrue(wasDemanded >= vm1.getCPUDemand());
            //System.err.println(vm1.getCPUConsumption() + " " + vm1.getCPUDemand());
        }
    }
}
