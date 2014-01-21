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
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.SimpleConfiguration;
import entropy.configuration.SimpleNode;
import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VirtualMachine;
import entropy.plan.DefaultTimedReconfigurationPlan;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.action.Migration;
import entropy.plan.action.Run;
import entropy.plan.action.Shutdown;
import entropy.plan.action.Startup;
import entropy.plan.action.Stop;
import entropy.plan.action.Suspend;

/**
 * Unit tests for ProtobufTimedReconfigurationPlanSerializer.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestProtobufTimedReconfigurationPlanSerializer {

    public void test() {

        Configuration cfg = new SimpleConfiguration();
        Node n1 = new SimpleNode("N1", 1, 2, 3);
        Node n2 = new SimpleNode("N2", 1, 2, 3);
        Node n3 = new SimpleNode("N3", 1, 2, 3);
        Node n4 = new SimpleNode("N4", 1, 2, 3);
        cfg.addOnline(n1);
        cfg.addOnline(n2);
        cfg.addOnline(n3);
        cfg.addOffline(n4);

        VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 1, 2, 3);
        VirtualMachine vm2 = new SimpleVirtualMachine("VM2", 1, 2, 3);
        VirtualMachine vm3 = new SimpleVirtualMachine("VM3", 1, 2, 3);
        VirtualMachine vm4 = new SimpleVirtualMachine("VM4", 1, 2, 3);
        VirtualMachine vm5 = new SimpleVirtualMachine("VM5", 1, 2, 3);
        VirtualMachine vm6 = new SimpleVirtualMachine("VM6", 1, 2, 3);
        VirtualMachine vm7 = new SimpleVirtualMachine("VM7", 1, 2, 3);

        cfg.setRunOn(vm1, n1);
        cfg.setRunOn(vm2, n1);
        cfg.setSleepOn(vm3, n2);
        cfg.setRunOn(vm4, n2);
        cfg.addWaiting(vm5);
        cfg.setRunOn(vm6, n3);
        cfg.setRunOn(vm7, n3);

        TimedReconfigurationPlan p = new DefaultTimedReconfigurationPlan(cfg);
        Assert.assertTrue(p.add(new Migration(vm1, n1, n2, 0, 5)));
        Assert.assertTrue(p.add(new Startup(n4, 0, 3)));
        Assert.assertTrue(p.add(new Migration(vm6, n3, n2, 2, 5)));
        Assert.assertTrue(p.add(new Suspend(vm7, n3, n4, 7, 10)));
        Assert.assertTrue(p.add(new Run(vm5, n4, 0, 1)));
        //Assert.assertTrue(p.add(new Pause(vm2, n1, 0, 5)));
        //Assert.assertTrue(p.add(new UnPause(vm3, n1, 10, 15)));
        Assert.assertTrue(p.add(new Stop(vm4, n2, 1, 2)));
        Assert.assertTrue(p.add(new Shutdown(n3, 10, 15)));

        ProtobufTimedReconfigurationPlanSerializer s = ProtobufTimedReconfigurationPlanSerializer.getInstance();
        File tmpF = null;
        try {
            tmpF = File.createTempFile("out", "out");
            s.write(p, tmpF.getAbsolutePath());
            TimedReconfigurationPlan r = s.read(tmpF.getAbsolutePath());
            Assert.assertEquals(r, p);
        } catch (IOException e) {
            Assert.fail(e.getMessage(), e);
        } catch (TimedReconfigurationPlanSerializerException e) {
            Assert.fail(e.getMessage(), e);
        } finally {
            if (tmpF != null && tmpF.exists()) {
                tmpF.delete();
            }
        }
    }
}
