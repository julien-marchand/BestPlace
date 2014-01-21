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

import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.DefaultNode;

/**
 * Unit tests for {@code MultiSetsUnion}.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestMultiSetsUnion {

    public void testExpandAndAllElements() {
        ExplodedMultiSet<DefaultNode> ms1 = new ExplodedMultiSet<DefaultNode>();
        ExplodedMultiSet<DefaultNode> ms2 = new ExplodedMultiSet<DefaultNode>();
        List<ExplodedSet<DefaultNode>> ns = new LinkedList<ExplodedSet<DefaultNode>>();
        ExplodedSet<DefaultNode> all = new ExplodedSet<DefaultNode>();
        ExplodedSet<DefaultNode> s = new ExplodedSet<DefaultNode>();
        ExplodedSet<DefaultNode> e = new ExplodedSet<DefaultNode>();
        ExplodedMultiSet<DefaultNode> ms = ms1;

        for (int i = 1; i <= 20; i++) {
            DefaultNode n = new DefaultNode("N" + i, 1, 1, 1);
            all.add(n);
            s.add(n);
            e.add(n);
            if (i % 5 == 0) {
                ms.add(s);
                s = new ExplodedSet<DefaultNode>();
                ns.add(e);
                e = new ExplodedSet<DefaultNode>();
            }
            if (i == 10) {
                ms = ms2;
            }
        }
        MultiSetsUnion<DefaultNode> u = new MultiSetsUnion<DefaultNode>(ms1, ms2);
        Assert.assertEquals(u.expand(), ns);
        Assert.assertEquals(u.getElements(), all);
    }

    public void testPrettyAndDefinition() {
        ExplodedMultiSet<DefaultNode> ms1 = new ExplodedMultiSet<DefaultNode>();
        ExplodedMultiSet<DefaultNode> ms2 = new ExplodedMultiSet<DefaultNode>();

        ms1.add(new RangeOfElements<DefaultNode>("N[1..5]"));
        ms1.add(new RangeOfElements<DefaultNode>("N[6..10]", "$T2"));
        ms2.add(new RangeOfElements<DefaultNode>("N[11..15]"));
        ms2.add(new RangeOfElements<DefaultNode>("N[16..20]", "$T4"));
        MultiSetsUnion<DefaultNode> u = new MultiSetsUnion<DefaultNode>(ms1, ms2);
        String def = "{N[1..5], $T2} + {N[11..15], $T4}";
        Assert.assertEquals(u.definition(), def);
        Assert.assertEquals(u.pretty(), def);

        u.setLabel("$small");
        Assert.assertEquals(u.definition(), def);
        Assert.assertEquals(u.pretty(), "$small");

    }
}
