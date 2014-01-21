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

package entropy.plan.visualization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import entropy.plan.Plan;
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
 * Visualize a reconfiguration plan as a dot multigraph, indicating
 * the movement of the VMs.
 *
 * @author Fabien Hermenier
 */
public class DotVisualizer implements PlanVisualizer {

    /**
     * The output file.
     */
    private String out;

    /**
     * The current buffer.
     */
    private StringBuilder buffer;

    /**
     * Make a new visualizer.
     *
     * @param path the path name to the output file
     */
    public DotVisualizer(String path) {
        this.out = path;
    }

    /**
     * Get the path of the output file.
     *
     * @return a path
     */
    public String getOutputFile() {
        return out;
    }

    /**
     * Set the output file.
     *
     * @param path the path to the output file
     */
    public void setOutputFile(String path) {
        this.out = path;
    }

    /**
     * Build the dot graph.
     *
     * @param plan the plan to visualize
     * @return {@code true} if the generation succeeds
     */
    @Override
    public boolean buildVisualization(TimedReconfigurationPlan plan) {
        buffer = new StringBuilder("digraph TimedExecutionGraph{\n");

        File parent = new File(getOutputFile()).getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            Plan.logger.error("Unable to create '" + parent.getName() + "'");
            return false;
        }

        buffer.append("rankdir=LR;\n");
        for (Action a : plan) {
            a.injectToVisualizer(this);
        }

        buffer.append("}");
        FileWriter out = null;
        try {
            out = new FileWriter(getOutputFile());
            out.write(buffer.toString());
        } catch (IOException e) {
            Plan.logger.error(e.getMessage(), e);
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Plan.logger.error(e.getMessage(), e);
                }
            }
        }

        return true;
    }

    private void setTimesLabel(Action a, StringBuilder b) {
        if (a.getStartMoment() >= 0) {
            b.append(", headlabel=\"").append(a.getStartMoment()).append("\",");
            b.append("taillabel=\" ").append(a.getFinishMoment()).append("\"");
        }
    }

    @Override
    public void inject(Migration a) {
        buffer.append(a.getHost().getName());
        buffer.append(" -> ");
        buffer.append(a.getDestination().getName());
        buffer.append(" [label=\"migrate(");
        buffer.append(a.getVirtualMachine().getName());
        buffer.append(")\"");
        setTimesLabel(a, buffer);
        buffer.append(",color=\"green\"]\n");
    }

    @Override
    public void inject(Run a) {
        buffer.append("r");
        buffer.append(a.getVirtualMachine().getName());
        buffer.append(" -> ");
        buffer.append(a.getHost().getName());
        buffer.append(" [label=\"run(");
        buffer.append(a.getVirtualMachine().getName());
        buffer.append(")\"");
        setTimesLabel(a, buffer);
        buffer.append(", color=\"red\"]\n");
        buffer.append("r");
        buffer.append(a.getVirtualMachine().getName());
        buffer.append(" [label=\"\", shape=none]\n");
    }

    @Override
    public void inject(Stop a) {
        buffer.append(a.getHost().getName());
        buffer.append(" -> ");
        buffer.append("s");
        buffer.append(a.getVirtualMachine().getName());
        buffer.append(" [label=\"stop(");
        buffer.append(a.getVirtualMachine().getName());
        buffer.append(")\"");
        setTimesLabel(a, buffer);
        buffer.append(", color=\"blue\"]\n");
        buffer.append("s");
        buffer.append(a.getVirtualMachine().getName());
        buffer.append(" [label=\"\", shape=none]\n");
    }

    @Override
    public void inject(Startup a) {
        buffer.append("b");
        buffer.append(a.getNode().getName());
        buffer.append(" -> ");
        buffer.append(a.getNode().getName());
        buffer.append(" [label=\"boot\"");
        setTimesLabel(a, buffer);
        buffer.append(",color=\"blue\"]\n");
        buffer.append("b");
        buffer.append(a.getNode().getName());
        buffer.append(" [label=\"\", shape=none]\n");
    }

    @Override
    public void inject(Shutdown a) {
        buffer.append(a.getNode().getName());
        buffer.append(" -> ");
        buffer.append(a.getNode().getName());
        buffer.append(" [label=\"halt\"");
        setTimesLabel(a, buffer);
        buffer.append(", color=\"red\"]\n");
    }

    @Override
    public void inject(Resume a) {
        buffer.append(a.getHost().getName());
        buffer.append(" -> ");
        buffer.append(a.getDestination().getName());
        buffer.append(" [label=\"resume(");
        buffer.append(a.getVirtualMachine().getName()).append(")\"");
        setTimesLabel(a, buffer);
        buffer.append(", color=\"red\"]\n");
    }

    @Override
    public void inject(Suspend a) {
        buffer.append(a.getHost().getName());
        buffer.append(" -> ");
        buffer.append(a.getHost().getName());
        buffer.append(" [label=\"suspend(");
        buffer.append(a.getVirtualMachine().getName());
        buffer.append(")\"");
        setTimesLabel(a, buffer);
        buffer.append(", color=\"blue\"]\n");
    }

    @Override
    public void inject(Pause a) {
        buffer.append("[label=\"pause(").append(a.getVirtualMachine().getName()).append(")\", color=\"blue\"]\n");
    }

    @Override
    public void inject(UnPause a) {
        buffer.append("[label=\"unpause(").append(a.getVirtualMachine().getName()).append(")\", color=\"red\"]\n");
    }
}
