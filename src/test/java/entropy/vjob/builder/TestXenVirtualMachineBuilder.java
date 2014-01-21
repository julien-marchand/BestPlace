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
package entropy.vjob.builder;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.VirtualMachine;

/**
 * Some unit tests for XenVirtualMachineBuilder.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestXenVirtualMachineBuilder {


    private static final String RESOURCES_LOCATION = "src/test/resources/entropy/vjob/builder/xenCfgs";

    /**
     * Test the instantation.
     */
    public void testInstantiation() {
        XenVirtualMachineBuilder b = new XenVirtualMachineBuilder(RESOURCES_LOCATION);
        Assert.assertEquals(b.getConfigDir(), RESOURCES_LOCATION);
    }

    /**
     * Test the parsing of a valid file.
     */
    public void testGoodRetrieve() {
        XenVirtualMachineBuilder b = new XenVirtualMachineBuilder(RESOURCES_LOCATION);
        try {
            VirtualMachine vm = b.buildVirtualMachine("vm1");
            Assert.assertEquals(vm.getName(), "vm1");
            Assert.assertEquals(vm.getMemoryDemand(), 1024);
            Assert.assertEquals(vm.getNbOfCPUs(), 4);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the parsing of a file without VCPU (by default = 1).
     */
    public void testRetrieveWithoutVCPU() {
        XenVirtualMachineBuilder b = new XenVirtualMachineBuilder(RESOURCES_LOCATION);
        try {
            VirtualMachine vm = b.buildVirtualMachine("noVCPU");
            Assert.assertEquals(vm.getMemoryDemand(), 2048);
            Assert.assertEquals(vm.getNbOfCPUs(), 1);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test the parsing without memory.
     *
     * @throws VJobBuilderException the exception we expect
     */
    @Test(expectedExceptions = {VirtualMachineBuilderException.class})
    public void testRetrieveWithoutMem() throws VirtualMachineBuilderException {
        XenVirtualMachineBuilder b = new XenVirtualMachineBuilder(RESOURCES_LOCATION);
        b.buildVirtualMachine("noMem");
    }
}
