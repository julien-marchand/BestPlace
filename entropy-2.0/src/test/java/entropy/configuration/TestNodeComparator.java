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

import static entropy.configuration.ResourcePicker.NodeRc.cpuCapacity;
import static entropy.configuration.ResourcePicker.NodeRc.memoryCapacity;
import static entropy.configuration.ResourcePicker.NodeRc.nbOfCPUs;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for NodeComparator.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestNodeComparator {

    /**
     * Make default elements for test purpose.
     *
     * @return an array of 2 elements.
     */
    private static ManagedElementSet<Node> makeDefaultElements() {
        ManagedElementSet<Node> elmts = new SimpleManagedElementSet<Node>();
        elmts.add(new SimpleNode("N1", 2, 1, 4));
        elmts.add(new SimpleNode("N2", 1, 1, 4));
        elmts.add(new SimpleNode("N3", 1, 5, 9));
        elmts.add(new SimpleNode("N4", 3, 8, 6));
        return elmts;
    }

    /**
     * Test the comparison between equivalents elements.
     */
    public void testEquivalent() {
        ManagedElementSet<Node> elmts = makeDefaultElements();
        NodeComparator cmp = new NodeComparator(true, cpuCapacity);
        cmp.appendCriteria(true, memoryCapacity);
        Assert.assertEquals(cmp.compare(elmts.get(0), elmts.get(1)), 0);

        cmp = new NodeComparator(false, cpuCapacity);
        cmp.appendCriteria(false, memoryCapacity);
        Assert.assertEquals(cmp.compare(elmts.get(0), elmts.get(1)), 0);
    }

    /**
     * Test the comparison between non equivalent values.
     */
    public void testNonEquivalent() {
        ManagedElementSet<Node> elmts = makeDefaultElements();
        NodeComparator cmp = new NodeComparator(true, cpuCapacity);
        cmp.appendCriteria(true, nbOfCPUs);
        Assert.assertTrue(cmp.compare(elmts.get(0), elmts.get(1)) > 0);

        cmp = new NodeComparator(false, cpuCapacity);
        cmp.appendCriteria(false, nbOfCPUs);
        Assert.assertTrue(cmp.compare(elmts.get(0), elmts.get(1)) < 0);
    }

    /**
     * Test sorting the list using the comparator.
     */
    public void testSort() {
        ManagedElementSet<Node> elmts = makeDefaultElements();
        NodeComparator cmp = new NodeComparator(true, cpuCapacity);
        cmp.appendCriteria(true, nbOfCPUs);
        Collections.sort(elmts, cmp);
        Assert.assertEquals(elmts.get(0).getName(), "N2");
        Assert.assertEquals(elmts.get(1).getName(), "N1");
        Assert.assertEquals(elmts.get(2).getName(), "N3");
        Assert.assertEquals(elmts.get(3).getName(), "N4");
    }
}
