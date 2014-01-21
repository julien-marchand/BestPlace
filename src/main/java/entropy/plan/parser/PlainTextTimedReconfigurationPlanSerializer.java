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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.configuration.parser.ConfigurationSerializerException;
import entropy.configuration.parser.PlainTextConfigurationSerializer;
import entropy.plan.DefaultTimedReconfigurationPlan;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.action.Action;
import entropy.plan.action.Migration;
import entropy.plan.action.Pause;
import entropy.plan.action.Resume;
import entropy.plan.action.Run;
import entropy.plan.action.Shutdown;
import entropy.plan.action.Startup;
import entropy.plan.action.Stop;
import entropy.plan.action.Suspend;
import entropy.plan.action.UnPause;

/**
 * A serializer to store a plan in a human readable plain text format.
 * TODO: Need some tests.
 *
 * @author Fabien Hermenier
 */
public class PlainTextTimedReconfigurationPlanSerializer extends FileTimedReconfigurationPlanSerializer {

    private final static PlainTextTimedReconfigurationPlanSerializer instance = new PlainTextTimedReconfigurationPlanSerializer();

    private static final String CONFIG_SEP = "#Reconfiguration Plan";

    private static final String MIGRATION = "migrate";

    private static final String RUN = "run";

    private static final String STOP = "stop";

    private static final String STARTUP = "startup";

    private static final String SHUTDOWN = "shutdown";

    private static final String SUSPEND = "suspend";

    private static final String RESUME = "resume";

    private static final String PAUSE = "pause";

    private static final String UNPAUSE = "unpause";

    private BufferedWriter out;

    private PlainTextTimedReconfigurationPlanSerializer() {
    }

    @Override
    public TimedReconfigurationPlan unSerialize(InputStream i) throws IOException, TimedReconfigurationPlanSerializerException {
        BufferedReader in = null;
        TimedReconfigurationPlan p = null;
        try {
            new StringBuilder();
            in = new BufferedReader(new InputStreamReader(i));
            Configuration cfg = PlainTextConfigurationSerializer.getInstance().unSerialize(in);
            p = new DefaultTimedReconfigurationPlan(cfg);

            String line = in.readLine();
            if (line.endsWith(CONFIG_SEP)) {
                line = in.readLine();
            }
            while (line != null) {
                if (line.length() == 0) {
                    continue;
                }
                String[] toks = line.split(" ");
                if (toks.length != 2) {
                    throw new TimedReconfigurationPlanSerializerException("Non viable action line:" + line);
                }
                String[] moments = toks[0].split(":");
                if (moments.length != 2) {
                    throw new TimedReconfigurationPlanSerializerException("Incorrect time interval:" + toks[0]);
                }
                try {
                    int st = Integer.parseInt(moments[0]);
                    int ed = Integer.parseInt(moments[1]);
                    if (!line.endsWith(")")) {
                        throw new TimedReconfigurationPlanSerializerException("Non viable action line:" + line);
                    }
                    line = toks[1].substring(0, toks[1].length() - 1);
                    String[] x = line.split("\\(");
                    if (x.length != 2) {
                        throw new TimedReconfigurationPlanSerializerException("Non viable action line:" + line);
                    }
                    String[] params = x[1].split(",");
                    String id = x[0];
                    //Go to the conversion
                    Action a = null;
                    ManagedElementSet<VirtualMachine> vms = cfg.getAllVirtualMachines();
                    ManagedElementSet<Node> ns = cfg.getAllNodes();
                    if (id.equals(MIGRATION)) {
                        a = new Migration(vms.get(params[0]),
                                ns.get(params[1]),
                                ns.get(params[2]), st, ed);
                    } else if (id.equals(STOP)) {
                        a = new Stop(vms.get(params[0]),
                                ns.get(params[1]),
                                st, ed);
                    } else if (id.equals(RUN)) {
                        a = new Run(vms.get(params[0]),
                                ns.get(params[1]),
                                st, ed);
                    } else if (id.equals(PAUSE)) {
                        a = new Pause(vms.get(params[0]),
                                ns.get(params[1]),
                                st, ed);
                    } else if (id.equals(UNPAUSE)) {
                        a = new UnPause(vms.get(params[0]),
                                ns.get(params[1]),
                                st, ed);
                    } else if (id.equals(SUSPEND)) {
                        a = new Suspend(vms.get(params[0]),
                                ns.get(params[1]),
                                ns.get(params[2]),
                                st, ed);
                    } else if (id.equals(RESUME)) {
                        a = new Resume(vms.get(params[0]),
                                ns.get(params[1]),
                                ns.get(params[2]),
                                st, ed);
                    } else if (id.equals(STARTUP)) {
                        a = new Startup(ns.get(params[0]),
                                st, ed);
                    } else if (id.equals(SHUTDOWN)) {
                        a = new Shutdown(ns.get(params[0]),
                                st, ed);
                    } else {
                        throw new TimedReconfigurationPlanSerializerException("Unsupported action: " + id);
                    }
                    if (!p.add(a)) {
                        throw new TimedReconfigurationPlanSerializerException("Unable to add action '" + a + "'");
                    }

                } catch (NumberFormatException e) {
                    throw new TimedReconfigurationPlanSerializerException("Incorrect time interval for " + toks[0] + ": " + e.getMessage());
                }
                line = in.readLine();
            }
        } catch (ConfigurationSerializerException e) {
            throw new TimedReconfigurationPlanSerializerException(e);
        } finally {
/*            if (in != null) {
                in.close();
            }*/
        }
        return p;
    }

    @Override
    public void serialize(TimedReconfigurationPlan plan, OutputStream o) throws IOException {
        try {
            out = new BufferedWriter(new OutputStreamWriter(o));
            PlainTextConfigurationSerializer.getInstance().serialize(plan.getSource(), o);
            //Separator
            out.write(CONFIG_SEP);
            out.write("\n");
            //Actions are sorted by starting date
            Map<Integer, List<Action>> planning = new HashMap<Integer, List<Action>>();
            TreeSet<Integer> times = new TreeSet<Integer>();
            for (Action action : plan.getActions()) {
                int st = action.getStartMoment();
                if (!planning.containsKey(st)) {
                    planning.put(st, new LinkedList<Action>());
                    times.add(st);
                }
                planning.get(st).add(action);
            }
            for (Map.Entry<Integer, List<Action>> e : planning.entrySet()) {
                for (Action a : e.getValue()) {
                    out.write("" + a.getStartMoment());
                    out.write(":");
                    out.write("" + a.getFinishMoment());
                    out.write(" ");
                    a.serialize(this);
                    out.write("\n");
                }
            }

        } finally {
            if (out != null) {
                out.flush();
            }
        }
    }

    public static PlainTextTimedReconfigurationPlanSerializer getInstance() {
        return instance;
    }

    @Override
    public void serialize(Migration a) throws IOException {
        out.write(MIGRATION);
        out.write("(");
        out.write(a.getVirtualMachine().getName());
        out.write(",");
        out.write(a.getHost().getName());
        out.write(",");
        out.write(a.getDestination().getName());
        out.write(")");
    }

    @Override
    public void serialize(Run a) throws IOException {
        out.write(RUN);
        out.write("(");
        out.write(a.getVirtualMachine().getName());
        out.write(",");
        out.write(a.getHost().getName());
        out.write(")");
    }

    @Override
    public void serialize(Stop a) throws IOException {
        out.write(STOP);
        out.write("(");
        out.write(a.getVirtualMachine().getName());
        out.write(",");
        out.write(a.getHost().getName());
        out.write(")");
    }

    @Override
    public void serialize(Suspend a) throws IOException {
        out.write(SUSPEND);
        out.write("(");
        out.write(a.getVirtualMachine().getName());
        out.write(",");
        out.write(a.getHost().getName());
        out.write(",");
        out.write(a.getDestination().getName());
        out.write(")");

    }

    @Override
    public void serialize(Resume a) throws IOException {
        out.write(RESUME);
        out.write("(");
        out.write(a.getVirtualMachine().getName());
        out.write(",");
        out.write(a.getHost().getName());
        out.write(",");
        out.write(a.getDestination().getName());
        out.write(")");
    }

    @Override
    public void serialize(Startup a) throws IOException {
        out.write(STARTUP);
        out.write("(");
        out.write(a.getNode().getName());
        out.write(")");
    }

    @Override
    public void serialize(Shutdown a) throws IOException {
        out.write(SHUTDOWN);
        out.write("(");
        out.write(a.getNode().getName());
        out.write(")");
    }

    @Override
    public void serialize(Pause a) throws IOException {
        out.write(PAUSE);
        out.write("(");
        out.write(a.getVirtualMachine().getName());
        out.write(")");
    }

    @Override
    public void serialize(UnPause a) throws IOException {
        out.write(UNPAUSE);
        out.write("(");
        out.write(a.getVirtualMachine().getName());
        out.write(")");
    }
}
