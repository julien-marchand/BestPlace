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

package entropy.plan.parser;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.Configuration;
import entropy.configuration.DefaultConfiguration;
import entropy.configuration.DefaultNode;
import entropy.configuration.DefaultVirtualMachine;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.DefaultTimedReconfigurationPlan;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.action.Migration;
import entropy.plan.action.Resume;
import entropy.plan.action.Run;
import entropy.plan.action.Shutdown;
import entropy.plan.action.Startup;
import entropy.plan.action.Stop;
import entropy.plan.action.Suspend;

/**
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestPlainTextTimedReconfigurationPlanSerializer {

    /**
     * Write a plan and read it. Original and readed plan must be identical
     */
    public void loopTest() {
        Configuration c = new DefaultConfiguration();
        Node[] nodes = new DefaultNode[10];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new DefaultNode("N" + (i + 1), 1, 1, 1);
            if (i < 8) {
                c.addOnline(nodes[i]);
            } else {
                c.addOffline(nodes[i]);
            }
        }
        VirtualMachine[] vms = new DefaultVirtualMachine[10];
        for (int i = 0; i < vms.length; i++) {
            vms[i] = new DefaultVirtualMachine("VM" + (i + 1), 1, 1, 1);
        }
        c.addWaiting(vms[0]);
        c.addWaiting(vms[1]);
        c.setRunOn(vms[2], nodes[0]);
        c.setRunOn(vms[3], nodes[1]);
        c.setRunOn(vms[6], nodes[1]);
        c.setSleepOn(vms[4], nodes[2]);

        TimedReconfigurationPlan p = new DefaultTimedReconfigurationPlan(c);
        p.add(new Startup(nodes[8], 0, 5));
        p.add(new Startup(nodes[9], 0, 5));
        p.add(new Shutdown(nodes[6], 0, 5));
        p.add(new Migration(vms[2], nodes[0], nodes[3], 5, 10));
        p.add(new Suspend(vms[3], nodes[1], nodes[1], 5, 10));
        p.add(new Resume(vms[4], nodes[2], nodes[5], 5, 10));
        p.add(new Run(vms[0], nodes[0], 2, 5));
        p.add(new Stop(vms[6], nodes[0], 2, 5));

        File f = null;
        try {
            f = new File("/Users/fhermeni/tmp.txt");//File.createTempFile("tmp", "tmp");
            PlainTextTimedReconfigurationPlanSerializer.getInstance().write(p, f.getAbsolutePath());
            TimedReconfigurationPlan plan2 = PlainTextTimedReconfigurationPlanSerializer.getInstance().read(f.getAbsolutePath());
            Assert.assertEquals(p, plan2);
            //f.deleteOnExit();
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        } finally {
            /*if (f != null) {
                f.delete();
            } */
        }


    }
}
