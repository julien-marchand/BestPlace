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

package entropy.configuration;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for ManagedElementSetComparator.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestVirtualMachinesSetComparator {

    public void test1() {
        VirtualMachine vm1 = new DefaultVirtualMachine("VM1", 1, 1, 4);
        VirtualMachine vm2 = new DefaultVirtualMachine("VM2", 1, 2, 3);
        VirtualMachine vm3 = new DefaultVirtualMachine("VM3", 1, 3, 2);
        VirtualMachine vm4 = new DefaultVirtualMachine("VM4", 1, 4, 1);

        ManagedElementSet<VirtualMachine> s1 = new DefaultManagedElementSet<VirtualMachine>(vm1);
        s1.add(vm2);
        ManagedElementSet<VirtualMachine> s2 = new DefaultManagedElementSet<VirtualMachine>(vm3);
        s2.add(vm4);

        VirtualMachinesSetComparator cmp = new VirtualMachinesSetComparator(true, ResourcePicker.VMRc.cpuConsumption);

        Assert.assertEquals(0, cmp.compare(s1, s1));
        Assert.assertEquals(-1, cmp.compare(s1, s2));
        Assert.assertEquals(1, cmp.compare(s2, s1));

        cmp = new VirtualMachinesSetComparator(false, ResourcePicker.VMRc.memoryConsumption);
        Assert.assertEquals(0, cmp.compare(s1, s1));
        Assert.assertEquals(-1, cmp.compare(s1, s2));
        Assert.assertEquals(1, cmp.compare(s2, s1));
    }
}
