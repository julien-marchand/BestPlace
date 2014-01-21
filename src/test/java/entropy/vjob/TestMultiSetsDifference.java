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
 * Unit tests for {@code MultiSetsDifference}
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestMultiSetsDifference {

    /*public void testExpandAndAllElements() {
        ExplodedMultiSet<DefaultNode> ms1 = new ExplodedMultiSet<DefaultNode>();
        ExplodedMultiSet<DefaultNode> ms2 = new ExplodedMultiSet<DefaultNode>();
        List<ExplodedList<DefaultNode>> ns = new LinkedList<ExplodedList<DefaultNode>>();
        ExplodedList<DefaultNode> all = new ExplodedList<DefaultNode>();
        ExplodedList<DefaultNode> s = new ExplodedList<DefaultNode>();
        ExplodedList<DefaultNode> e = new ExplodedList<DefaultNode>();
        ExplodedMultiSet<DefaultNode> ms = ms1;

        for (int i = 1; i <= 20; i++) {
            DefaultNode n = new DefaultNode("N" + i, 1, 1, 1);
            all.add(n);
            s.add(n);
            e.add(n);
            if (i % 5 == 0) {
                ms.add(s);
                s = new ExplodedList<DefaultNode>();
                ns.add(e);
                e = new ExplodedList<DefaultNode>();
            }
            if (i == 10) {
                ms = ms2;
            }
        }
        MultiSetsDifference<DefaultNode> u = new MultiSetsDifference<DefaultNode>(ms1, ms2);
        Assert.assertEquals(u.expand(), ns);
        Assert.assertEquals(u.getElements(), all);
    }           */

    public void testPrettyAndDefinition() {
        ExplodedMultiSet<DefaultNode> ms1 = new ExplodedMultiSet<DefaultNode>();
        ExplodedMultiSet<DefaultNode> ms2 = new ExplodedMultiSet<DefaultNode>();

        ms1.add(new RangeOfElements<DefaultNode>("N[1..5]"));
        ms1.add(new RangeOfElements<DefaultNode>("N[6..10]", "$T2"));
        ms2.add(new RangeOfElements<DefaultNode>("N[11..15]"));
        ms2.add(new RangeOfElements<DefaultNode>("N[16..20]", "$T4"));
        MultiSetsDifference<DefaultNode> u = new MultiSetsDifference<DefaultNode>(ms1, ms2);
        String def = "{N[1..5], $T2} - {N[11..15], $T4}";
        Assert.assertEquals(u.definition(), def);
        Assert.assertEquals(u.pretty(), def);

        u.setLabel("$small");
        Assert.assertEquals(u.definition(), def);
        Assert.assertEquals(u.pretty(), "$small");

    }
}
