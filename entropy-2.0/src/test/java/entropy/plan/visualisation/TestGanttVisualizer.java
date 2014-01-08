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

package entropy.plan.visualisation;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.DefaultConfiguration;
import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.plan.DefaultTimedReconfigurationPlan;
import entropy.plan.action.Migration;
import entropy.plan.visualization.GanttVisualizer;
import entropy.plan.visualization.PlanVisualizer;

/**
 * Unit tests for GanttVisualizer.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestGanttVisualizer {

    /**
     * Test accessors.
     */
    public void testGets() {
        GanttVisualizer vis = new GanttVisualizer("out.png");
        Assert.assertEquals(vis.getOutputFile(), "out.png");
        Assert.assertEquals(vis.getOutputFormat(), GanttVisualizer.Format.png);
        vis.setOutputFile("out.jpg");
        vis.setOutputFormat(GanttVisualizer.Format.jpg);
        Assert.assertEquals(vis.getOutputFile(), "out.jpg");
        Assert.assertEquals(vis.getOutputFormat(), GanttVisualizer.Format.jpg);
        vis = new GanttVisualizer("out.jpg", GanttVisualizer.Format.jpg);
        Assert.assertEquals(vis.getOutputFile(), "out.jpg");
        Assert.assertEquals(vis.getOutputFormat(), GanttVisualizer.Format.jpg);
    }

    /**
     * Test generation.
     */
    public void testStorePlanAsGantt() {
        DefaultConfiguration cfg = new DefaultConfiguration();
        DefaultVirtualMachine[] vms = new DefaultVirtualMachine[6];
        DefaultNode[] ns = new DefaultNode[6];
        for (int i = 0; i < vms.length; i++) {
            vms[i] = new DefaultVirtualMachine("vm" + (i + 1), 1, 1, 1);
            ns[i] = new DefaultNode("n" + (i + 1), 1, 1, 1);
            cfg.addOnline(ns[i]);
        }
        Migration m1 = new Migration(vms[0], ns[0], ns[1], 3, 6);
        Migration m2 = new Migration(vms[1], ns[1], ns[2], 0, 3);
        Migration m3 = new Migration(vms[2], ns[0], ns[2], 5, 10);
        Migration m4 = new Migration(vms[3], ns[2], ns[3], 0, 3);
        Migration m5 = new Migration(vms[4], ns[2], ns[4], 0, 10);
        Migration m6 = new Migration(vms[5], ns[2], ns[3], 0, 5);

        DefaultTimedReconfigurationPlan plan = new DefaultTimedReconfigurationPlan(cfg);
        plan.add(m1);
        plan.add(m2);
        plan.add(m3);
        plan.add(m4);
        plan.add(m5);
        plan.add(m6);

        try {
            File f = File.createTempFile("plan", ".png");
            PlanVisualizer visu = new GanttVisualizer(f.getAbsolutePath());
            visu.buildVisualization(plan);
            Assert.assertTrue(f.exists());
            Assert.assertTrue(f.length() > 50);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
    }
}
