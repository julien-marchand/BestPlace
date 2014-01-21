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

import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.DefaultNode;

/**
 * Unit tests for {@code ExplodedMultiSet}
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestExplodedMultiSet {

    public void testExpandAndAllElements() {
        ExplodedMultiSet<DefaultNode> ms = new ExplodedMultiSet<DefaultNode>();
        DefaultManagedElementSet<DefaultNode> all = new DefaultManagedElementSet<DefaultNode>();
        List<ExplodedSet<DefaultNode>> expanded = new LinkedList<ExplodedSet<DefaultNode>>();

        for (int i = 0; i < 3; i++) {
            ExplodedSet<DefaultNode> s = new ExplodedSet<DefaultNode>();
            ExplodedSet<DefaultNode> ns = new ExplodedSet<DefaultNode>();
            for (int j = 0; j < 2; j++) {
                DefaultNode n = new DefaultNode("N" + i + j, 1, 1, 1);
                s.add(n);
                all.add(n);
                ns.add(n);
            }
            ms.add(s);
            expanded.add(ns);
        }
        Assert.assertEquals(ms.getElements(), all);
        Assert.assertEquals(ms.expand(), expanded);

    }

    public void testPrettyAndDefinition() {
        ExplodedMultiSet<DefaultNode> ms = new ExplodedMultiSet<DefaultNode>();
        ms.add(new ExplodedSet<DefaultNode>("$T1"));
        ms.add(new RangeOfElements<DefaultNode>("N[2..5]"));
        ms.add(new RangeOfElements<DefaultNode>("N[20..50]", "$T3"));
        ms.add(new ExplodedSet<DefaultNode>());
        String def = "{$T1, N[2..5], $T3, {}}";
        Assert.assertEquals(ms.pretty(), def);
        Assert.assertEquals(ms.definition(), def);

        ms.setLabel("$small");
        Assert.assertEquals(ms.pretty(), "$small");
        Assert.assertEquals(ms.definition(), def);
    }
}
