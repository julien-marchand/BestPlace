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

import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.vjob.BasicVJob;
import entropy.vjob.ExplodedMultiSet;
import entropy.vjob.ExplodedSet;
import entropy.vjob.VJob;
import entropy.vjob.VJobElement;

/**
 * Unit tests for {@link SymbolsTable}.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestSymbolsTable {

    /**
     * Test the inclusion of variables in the prolog.
     */
    public void testPrologUsage() {
        VJob p = new BasicVJob("prolog");

        ExplodedSet<Node> e1 = new ExplodedSet<Node>("$R1");
        ExplodedSet<VirtualMachine> e2 = new ExplodedSet<VirtualMachine>("$T1");
        ExplodedMultiSet<Node> e3 = new ExplodedMultiSet<Node>("$small");
        ExplodedMultiSet<VirtualMachine> e4 = new ExplodedMultiSet<VirtualMachine>("$multiVM");
        p.addNodes(e1);
        p.addNodes(e3);
        p.addVirtualMachines(e2);
        p.addVirtualMachines(e4);

        SymbolsTable s = new SymbolsTable(p);
        Assert.assertEquals(s.getSymbol("$R1").content(), e1);
        Assert.assertEquals(s.getSymbol("$T1").content(), e2);
        Assert.assertEquals(s.getSymbol("$small").content(), e3);
        Assert.assertEquals(s.getSymbol("$multiVM").content(), e4);
    }


    /**
     * Test the declaration of variables in various case.
     */
    public void testDeclare() {
        SymbolsTable s = new SymbolsTable();

        VJobElement[] es = new VJobElement[4];
        Content[] cs = new Content[4];


        es[0] = new ExplodedSet<DefaultNode>("$R1");
        cs[0] = new Content(Content.Type.nodeset, es[0]);

        es[1] = new ExplodedSet<DefaultVirtualMachine>("$T1");
        cs[1] = new Content(Content.Type.vmset, es[1]);

        es[2] = new ExplodedMultiSet<DefaultNode>("$small");
        cs[2] = new Content(Content.Type.multinodesets, es[2]);

        es[3] = new ExplodedMultiSet<DefaultVirtualMachine>("$multiVM");
        cs[3] = new Content(Content.Type.multivmsets, es[3]);

        for (int i = 0; i < cs.length; i++) {
            Assert.assertTrue(s.declare(es[i].getLabel(), cs[i]));
            Assert.assertTrue(s.isDeclared(es[i].getLabel()));
            Assert.assertEquals(s.getSymbol(es[i].getLabel()), cs[i]);
        }
    }

    /**
     * Test bad declaration.
     */
    public void testBadDeclares() {
        SymbolsTable s = new SymbolsTable();

        s.declare("$R1", new Content(Content.Type.nodeset, new ExplodedSet<DefaultNode>("$R1")));
        //Label already exists
        Assert.assertFalse(s.declare("$R1", new Content(Content.Type.nodeset, new ExplodedSet<DefaultNode>("$R1"))));
    }
}
