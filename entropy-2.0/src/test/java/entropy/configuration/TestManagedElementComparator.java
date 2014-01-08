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
 * Unit tests for ManagedElementComparator.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestManagedElementComparator {

    /**
     * Make default elements for test purpose.
     *
     * @return an array of 2 elements.
     */
    private static MockDefaultManagedElement[] makeDefaultElements() {
        MockDefaultManagedElement[] elmts = new MockDefaultManagedElement[2];
        elmts[0] = new MockDefaultManagedElement("e1");
        elmts[1] = new MockDefaultManagedElement("e2");

        elmts[0].updateValue("a", 5);
        elmts[0].updateValue("b", "toto");
        elmts[0].updateValue("c", 12.4);
        elmts[0].updateValue("d", -1);

        elmts[1].updateValue("a", 5);
        elmts[1].updateValue("b", "toto");
        elmts[1].updateValue("c", 7.4);

        return elmts;
    }

    /**
     * Test the comparison between equivalents elements.
     */
    public void testEquivalent() {
        MockDefaultManagedElement[] elmts = makeDefaultElements();
        ManagedElementComparator<MockDefaultManagedElement> cmp = new ManagedElementComparator<MockDefaultManagedElement>(true, "a");
        cmp.appendCriteria(true, "b");
        Assert.assertEquals(cmp.compare(elmts[0], elmts[1]), 0);

        cmp = new ManagedElementComparator<MockDefaultManagedElement>(false, "a");
        cmp.appendCriteria(false, "b");
        Assert.assertEquals(cmp.compare(elmts[0], elmts[1]), 0);
    }

    /**
     * Test the comparison between non equivalent values.
     */
    public void testNonEquivalent() {
        MockDefaultManagedElement[] elmts = makeDefaultElements();
        ManagedElementComparator<MockDefaultManagedElement> cmp = new ManagedElementComparator<MockDefaultManagedElement>(true, "a");
        cmp.appendCriteria(true, "c");
        Assert.assertTrue(cmp.compare(elmts[0], elmts[1]) > 0);

        cmp = new ManagedElementComparator<MockDefaultManagedElement>(false, "a");
        cmp.appendCriteria(false, "c");
        Assert.assertTrue(cmp.compare(elmts[0], elmts[1]) < 0);
    }

    /**
     * Test the comparison betweens criterias that are null.
     */
    public void testWithNulls() {
        MockDefaultManagedElement[] elmts = makeDefaultElements();
        ManagedElementComparator<MockDefaultManagedElement> cmp = new ManagedElementComparator<MockDefaultManagedElement>(false, "a");
        cmp.appendCriteria(false, "d");
        Assert.assertEquals(cmp.compare(elmts[0], elmts[1]), 0);
        cmp = new ManagedElementComparator<MockDefaultManagedElement>(true, "a");
        cmp.appendCriteria(true, "d");
        Assert.assertEquals(cmp.compare(elmts[0], elmts[1]), 0);

        cmp = new ManagedElementComparator<MockDefaultManagedElement>(true, "g");
        cmp.appendCriteria(true, "b");
        Assert.assertEquals(cmp.compare(elmts[0], elmts[1]), 0);

    }

}
