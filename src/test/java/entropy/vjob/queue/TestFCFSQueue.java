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
package entropy.vjob.queue;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.vjob.BasicVJob;
import entropy.vjob.VJob;

/**
 * Some unit tests for FCFSFolderQueue.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestFCFSQueue {

    /**
     * The base of the test resources.
     */
    public static final String RESOURCES_DIR = "src/test/resources/entropy/vjob/queue/TestFCFSFolderQueue.";

    /**
     * Test append() and getRunningPriorities().
     */
    public void testAppending() {
        FCFSPool pool = new FCFSPool();
        VJob l1 = new BasicVJob("l1");
        VJob l2 = new BasicVJob("l2");
        VJob l3 = new BasicVJob("l3");

        pool.add(l1);
        Assert.assertEquals(pool.getRunningPriorities().size(), 1);
        Assert.assertEquals(pool.getRunningPriorities().get(0), l1);


        pool.add(l2);
        Assert.assertEquals(pool.getRunningPriorities().size(), 2);
        Assert.assertEquals(pool.getRunningPriorities().get(1), l2);

        pool.add(l3);
        Assert.assertEquals(pool.getRunningPriorities().size(), 3);
        Assert.assertEquals(pool.getRunningPriorities().get(2), l3);

    }

    /**
     * Test removal detection and shifting.
     */
    @Test(dependsOnMethods = {"testAppending"})
    public void testShiftingWhenRemove() {
        FCFSPool pool = new FCFSPool();
        VJob l1 = new BasicVJob("l1");
        VJob l2 = new BasicVJob("l2");
        VJob l3 = new BasicVJob("l3");
        pool.add(l1);
        pool.add(l2);
        pool.add(l3);
        Assert.assertEquals(pool.getRunningPriorities().size(), 3);
        pool.remove(l2);
        Assert.assertEquals(pool.getRunningPriorities().size(), 2);
        Assert.assertEquals(pool.getRunningPriorities().get(1), l3);
    }

}
