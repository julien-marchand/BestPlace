/*
 * Copyright (c) Fabien Hermenier
 *
 * This file is part of Entropy.
 *
 * Entropy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Entropy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.configuration;

import static entropy.configuration.ResourcePicker.VMRc.cpuConsumption;
import static entropy.configuration.ResourcePicker.VMRc.memoryConsumption;
import static entropy.configuration.ResourcePicker.VMRc.nbOfCPUs;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for VirtualMachineComparator.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestVirtualMachineComparator {

    /**
     * Make default elements for test purpose.
     *
     * @return an array of 2 elements.
     */
    private static ManagedElementSet<VirtualMachine> makeDefaultElements() {
        ManagedElementSet<VirtualMachine> elmts = new SimpleManagedElementSet<VirtualMachine>();
        elmts.add(new SimpleVirtualMachine("VM1", 2, 1, 4));
        elmts.add(new SimpleVirtualMachine("VM2", 1, 1, 4));
        elmts.add(new SimpleVirtualMachine("VM3", 1, 5, 9));
        elmts.add(new SimpleVirtualMachine("VM4", 3, 8, 6));
        return elmts;
    }

    /**
     * Test the comparison between equivalents elements.
     */
    public void testEquivalent() {
        ManagedElementSet<VirtualMachine> elmts = makeDefaultElements();
        VirtualMachineComparator cmp = new VirtualMachineComparator(true, cpuConsumption);
        cmp.appendCriteria(true, memoryConsumption);
        Assert.assertEquals(cmp.compare(elmts.get(0), elmts.get(1)), 0);

        cmp = new VirtualMachineComparator(false, cpuConsumption);
        cmp.appendCriteria(false, memoryConsumption);
        Assert.assertEquals(cmp.compare(elmts.get(0), elmts.get(1)), 0);
    }

    /**
     * Test the comparison between non equivalent values.
     */
    public void testNonEquivalent() {
        ManagedElementSet<VirtualMachine> elmts = makeDefaultElements();
        VirtualMachineComparator cmp = new VirtualMachineComparator(true, cpuConsumption);
        cmp.appendCriteria(true, nbOfCPUs);
        Assert.assertTrue(cmp.compare(elmts.get(0), elmts.get(1)) > 0);

        cmp = new VirtualMachineComparator(false, cpuConsumption);
        cmp.appendCriteria(false, nbOfCPUs);
        Assert.assertTrue(cmp.compare(elmts.get(0), elmts.get(1)) < 0);
    }

    /**
     * Test sorting the list using the comparator.
     */
    public void testSort() {
        ManagedElementSet<VirtualMachine> elmts = makeDefaultElements();
        VirtualMachineComparator cmp = new VirtualMachineComparator(true, cpuConsumption);
        cmp.appendCriteria(true, nbOfCPUs);
        Collections.sort(elmts, cmp);
        Assert.assertEquals(elmts.get(0).getName(), "VM2");
        Assert.assertEquals(elmts.get(1).getName(), "VM1");
        Assert.assertEquals(elmts.get(2).getName(), "VM3");
        Assert.assertEquals(elmts.get(3).getName(), "VM4");
    }
}
