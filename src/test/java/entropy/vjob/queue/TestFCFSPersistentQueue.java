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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.vjob.BasicVJob;
import entropy.vjob.VJob;
import entropy.vjob.builder.MockVirtualMachineBuilder;
import entropy.vjob.builder.VJobBuilder;
import entropy.vjob.builder.VJobElementBuilder;

/**
 * Simple unit tests for FCFSPersistentQueue.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestFCFSPersistentQueue {

    /**
     * The base of the test resources.
     */
    public static final String RESOURCES_DIR = "src/test/resources/entropy/vjob/queue/TestFCFSPersistentQueue.";

    private void putLeaseIntoQueue(File queue, String file) throws Exception {
        File f1 = new File(queue + File.separator + file);
        BufferedReader in = new BufferedReader(new FileReader("src/test/resources/entropy/vjob/queue/" + file));
        BufferedWriter out = new BufferedWriter(new FileWriter(f1));
        String line = in.readLine();
        while (line != null) {
            out.write(line);
            out.write("\n");
            line = in.readLine();
        }
        in.close();
        out.close();
    }

    /**
     * Test with an unexistant directory.
     * The directory is created after the first scan
     */
    public void testWithUnexistantDirectory() {
        File queueDir = new File(System.getProperty("java.io.tmpdir") + "/queue1/");
        queueDir.delete();
        Assert.assertFalse(queueDir.exists());
        try {
            FCFSPersistentQueue queue = new FCFSPersistentQueue(new VJobBuilder(new VJobElementBuilder(new MockVirtualMachineBuilder()), null), queueDir);
            Assert.assertEquals(queue.getFolder(), queueDir);
            Assert.assertTrue(queueDir.isDirectory());
            Assert.assertEquals(queue.getRunningPriorities().size(), 0);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        for (File f : queueDir.listFiles()) {
            f.delete();
        }
        queueDir.delete();

    }

    /**
     * Test with an existant directory.
     */
    public void testWithExistantDirectory() {
        File queueDir = new File(System.getProperty("java.io.tmpdir") + "/queue2/");
        queueDir.mkdirs();
        for (File f : queueDir.listFiles()) {
            f.delete();
        }
        Assert.assertTrue(queueDir.exists());
        try {
            FCFSPersistentQueue queue = new FCFSPersistentQueue(new VJobBuilder(new VJobElementBuilder(new MockVirtualMachineBuilder()), null), queueDir);
            Assert.assertEquals(queue.getFolder(), queueDir);
            Assert.assertTrue(queueDir.isDirectory());
            Assert.assertEquals(queue.getRunningPriorities().size(), 0);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        } finally {
            for (File f : queueDir.listFiles()) {
                f.delete();
            }
            queueDir.delete();
        }
    }

    /**
     * Test with an existant directory.
     */
    public void testWithNonEmptyDirectory() {
        File queueDir = new File("src/test/resources/entropy/vjob/queue");
        Assert.assertTrue(queueDir.exists());
        Assert.assertTrue(queueDir.isDirectory());
        MockVirtualMachineBuilder mvb = new MockVirtualMachineBuilder();
        mvb.farm.add("VM1");
        mvb.farm.add("VM4");
        mvb.farm.add("TOTO");
        mvb.farm.add("tinkieWinky");
        mvb.farm.add("po");
        mvb.farm.add("nala");
        try {
            FCFSPersistentQueue queue = new FCFSPersistentQueue(new VJobBuilder(new VJobElementBuilder(mvb), null), queueDir);
            Assert.assertEquals(queue.getFolder(), queueDir);
            Assert.assertEquals(queue.getRunningPriorities().size(), 4);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    /**
     * Test with an addition of a vjob.
     */
    //@Test(dependsOnMethods = {"testWithUnexistantDirectory"})
    public void testAdditionIntoFolder() {
        File queueDir = new File(System.getProperty("java.io.tmpdir") + "/queue3/");
        queueDir.mkdirs();
        for (File f : queueDir.listFiles()) {
            f.delete();
        }
        try {
            MockVirtualMachineBuilder vb = new MockVirtualMachineBuilder();
            vb.farm.add("VM1");
            FCFSPersistentQueue queue = new FCFSPersistentQueue(new VJobBuilder(new VJobElementBuilder(vb), null), queueDir);
            Assert.assertEquals(queue.getFolder(), queueDir);
            this.putLeaseIntoQueue(queueDir, "lease1.btrp");
            Assert.assertEquals(queue.getRunningPriorities().size(), 1);
            Assert.assertEquals(queue.getRunningPriorities().get(0).id(), "lease1");
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        } finally {
            for (File f : queueDir.listFiles()) {
                f.delete();
            }
            queueDir.delete();
        }
    }

    public void testAddFromMethod() {
        File queueDir = new File(System.getProperty("java.io.tmpdir") + "/queue4/");
        queueDir.mkdirs();
        for (File f : queueDir.listFiles()) {
            f.delete();
        }
        try {
            MockVirtualMachineBuilder vb = new MockVirtualMachineBuilder();
            vb.farm.add("VM1");
            VJobBuilder b = new VJobBuilder(new VJobElementBuilder(vb), null);
            VJob v = new BasicVJob("V1");
            FCFSPersistentQueue queue = new FCFSPersistentQueue(b, queueDir);
            Assert.assertTrue(queue.add(v));
            Assert.assertEquals(queue.getFolder(), queueDir);
            Assert.assertEquals(queue.getRunningPriorities().size(), 1);
            Assert.assertEquals(queue.getRunningPriorities().get(0).id(), "V1");
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        } finally {
            for (File f : queueDir.listFiles()) {
                f.delete();
            }
            queueDir.delete();
        }
    }

    /**
     * Test with the removal of a vjob.
     */
    public void testRemovalFromDir() {
        File queueDir = new File(System.getProperty("java.io.tmpdir") + "/queue4/");
        try {
            MockVirtualMachineBuilder mvb = new MockVirtualMachineBuilder();
            mvb.farm.add("VM1");
            FCFSPersistentQueue queue = new FCFSPersistentQueue(new VJobBuilder(new VJobElementBuilder(mvb), null), queueDir);
            Assert.assertEquals(queue.getFolder(), queueDir);
            this.putLeaseIntoQueue(queueDir, "lease1.btrp");
            Assert.assertEquals(queue.getRunningPriorities().size(), 1);
            Assert.assertEquals(queue.getRunningPriorities().get(0).id(), "lease1");
            File f = new File(queueDir.getAbsolutePath() + File.separator + "lease1.btrp");
            Assert.assertTrue(f.exists());
            Assert.assertTrue(f.delete());
            Assert.assertEquals(queue.getRunningPriorities().size(), 0);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        } finally {
            for (File f : queueDir.listFiles()) {
                f.delete();
            }
            queueDir.delete();
        }
    }

    /**
     * Test with the removal of a vjob.
     */
    public void testRemovalFromMethod() {
        File queueDir = new File(System.getProperty("java.io.tmpdir") + "/queue5/");
        try {
            MockVirtualMachineBuilder mvb = new MockVirtualMachineBuilder();
            mvb.farm.add("VM1");
            FCFSPersistentQueue queue = new FCFSPersistentQueue(new VJobBuilder(new VJobElementBuilder(mvb), null), queueDir);
            Assert.assertEquals(queue.getFolder(), queueDir);
            this.putLeaseIntoQueue(queueDir, "lease1.btrp");
            List<VJob> q = queue.getRunningPriorities();
            Assert.assertEquals(q.size(), 1);
            Assert.assertEquals(q.get(0).id(), "lease1");
            File f = new File(queueDir.getAbsolutePath() + File.separator + "lease1.btrp");
            Assert.assertTrue(f.exists());
            queue.remove(q.get(0));
            Assert.assertEquals(queue.getRunningPriorities().size(), 0);
            Assert.assertFalse(f.exists());
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        } finally {
            for (File f : queueDir.listFiles()) {
                f.delete();
            }
            queueDir.delete();
        }
    }
}
