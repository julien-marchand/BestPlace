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

package entropy.execution;

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.plan.action.Migration;
import entropy.plan.action.Run;

/**
 * Unit tests for TimedExecutionGraph.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestTimedExecutionGraph {


    /**
     * Test the extraction of dependencies.
     */
    public void testExtractDependencies() {
        TimedExecutionGraph g = new TimedExecutionGraph();
        DefaultVirtualMachine[] vms = new DefaultVirtualMachine[6];
        for (int i = 0; i < vms.length; i++) {
            vms[i] = new DefaultVirtualMachine("VM" + (i + 1), 1, 1, 1);
        }
        DefaultNode[] ns = new DefaultNode[5];
        for (int i = 0; i < ns.length; i++) {
            ns[i] = new DefaultNode("N" + (i + 1), 1000, 1000, 1000);
        }
        Migration m3 = new Migration(vms[2], ns[0], ns[1], 7, 9);
        Migration m1 = new Migration(vms[0], ns[0], ns[2], 0, 10);
        Migration m2 = new Migration(vms[1], ns[1], ns[2], 0, 5);
        Migration m4 = new Migration(vms[3], ns[1], ns[3], 3, 7);
        Migration m5 = new Migration(vms[4], ns[3], ns[2], 0, 3);
        Run r6 = new Run(vms[5], ns[3], 0, 5);
        m1.insertIntoGraph(g);
        m2.insertIntoGraph(g);
        m3.insertIntoGraph(g);
        m4.insertIntoGraph(g);
        m5.insertIntoGraph(g);
        r6.insertIntoGraph(g);
        Set<Dependencies> deps = g.extractDependencies();
        Dependencies d1 = new Dependencies(m4);
        d1.addDependency(m5);
        Dependencies d2 = new Dependencies(m3);
        d2.addDependency(m2);
        d2.addDependency(m4);
        Assert.assertTrue(deps.contains(d1));
        Assert.assertTrue(deps.contains(d2));
        Assert.assertEquals(deps.size(), 6);
    }

    public void testToEventAgenda() {
        TimedExecutionGraph g = new TimedExecutionGraph();
        DefaultVirtualMachine[] vms = new DefaultVirtualMachine[6];
        for (int i = 0; i < vms.length; i++) {
            vms[i] = new DefaultVirtualMachine("VM" + (i + 1), 1, 1, 1);
        }
        DefaultNode[] ns = new DefaultNode[5];
        for (int i = 0; i < ns.length; i++) {
            ns[i] = new DefaultNode("N" + (i + 1), 1000, 1000, 1000);
        }
        Migration m3 = new Migration(vms[2], ns[0], ns[1], 7, 9);
        Migration m1 = new Migration(vms[0], ns[0], ns[2], 0, 10);
        Migration m2 = new Migration(vms[1], ns[1], ns[2], 0, 5);
        Migration m4 = new Migration(vms[3], ns[1], ns[3], 3, 7);
        Migration m5 = new Migration(vms[4], ns[3], ns[2], 0, 3);
        Run r6 = new Run(vms[5], ns[3], 0, 5);
        m1.insertIntoGraph(g);
        m2.insertIntoGraph(g);
        m3.insertIntoGraph(g);
        m4.insertIntoGraph(g);
        m5.insertIntoGraph(g);
        r6.insertIntoGraph(g);
        System.err.println(g.toEventAgenda());
        Assert.assertNotNull(g.toEventAgenda());
    }

}
