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

package entropy.vjob;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.DefaultNode;

/**
 * Unit tests for {@link ExplodedList}
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestExplodedSet {

    public void testEmpty() {
        VJobSet<DefaultNode> ns = new ExplodedSet<DefaultNode>();
        Assert.assertEquals(ns.pretty(), "{}");
        Assert.assertEquals(ns.definition(), "{}");
        Assert.assertEquals(ns.getLabel(), null);
        Assert.assertEquals(ns.flatten().size(), 0);
    }

    public void testLabel() {
        VJobSet<DefaultNode> ns = new ExplodedSet<DefaultNode>("$T1");
        Assert.assertEquals(ns.pretty(), "$T1");
        Assert.assertEquals(ns.getLabel(), "$T1");
        Assert.assertEquals(ns.definition(), "{}");
    }

    public void testNonEmpty() {
        ExplodedSet<DefaultNode> ns = new ExplodedSet<DefaultNode>("$T1");
        ns.add(new DefaultNode("N1", 1, 1, 1));
        ns.add(new DefaultNode("N2", 1, 1, 1));
        Assert.assertEquals(ns.pretty(), "$T1");
        Assert.assertEquals(ns.definition(), "{N1, N2}");
        Assert.assertEquals(ns.flatten(), ns);
    }
}

