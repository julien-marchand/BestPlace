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
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;

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
 * Visualize a reconfiguration plan as a agenda.
 * Image can be stored as a JPEG or a PNG file.
 *
 * @author Fabien Hermenier
 */
public class GanttVisualizer implements PlanVisualizer {

    /**
     * Available image format.
     */
    public static enum Format {
        jpg, png
    }

    /**
     * The format used.
     */
    private Format fmt;

    /**
     * The output file.
     */
    private String out;

    /**
     * Make a new visualizer. Image is stored as a PNG file
     *
     * @param path the path name to the output file
     */
    public GanttVisualizer(String path) {
        this(path, Format.png);
    }

    /**
     * Make a new visualizer.
     *
     * @param path   the path name to the output file
     * @param format the format of the output image
     */
    public GanttVisualizer(String path, Format format) {
        this.out = path;
        this.fmt = format;
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
     * Set the output path of the visualization
     *
     * @param path the path to the output file
     */
    public void setOutputFile(String path) {
        this.out = path;
    }

    /**
     * Set the output file format.
     *
     * @param f the format of the output image
     */
    public void setOutputFormat(Format f) {
        this.fmt = f;
    }

    /**
     * Get the format of the output image.
     *
     * @return a format
     */
    public Format getOutputFormat() {
        return this.fmt;
    }

    /**
     * Build the plan agenda
     *
     * @param plan the plan to visualize
     * @return {@code true} if the generation succeeds
     */
    @Override
    public boolean buildVisualization(TimedReconfigurationPlan plan) {
        File parent = new File(out).getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            Plan.logger.error("Unable to create '" + getOutputFile() + "'");
            return false;
        }
        final TaskSeriesCollection collection = new TaskSeriesCollection();
        TaskSeries ts = new TaskSeries("actions");
        for (Action action : plan) {
            Task t = new Task(action.toString(), new SimpleTimePeriod(action.getStartMoment(), action.getFinishMoment()));
            ts.add(t);
        }
        collection.add(ts);
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());

        final JFreeChart chart = ChartFactory.createGanttChart(
                null,  // chart title
                "Actions",              // domain axis label
                "Time",              // range axis label
                collection,             // data
                false,                // include legend
                true,                // tooltips
                false                // urls
        );
        CategoryPlot plot = chart.getCategoryPlot();
        DateAxis da = (DateAxis) plot.getRangeAxis();
        SimpleDateFormat sdfmt = new SimpleDateFormat();
        sdfmt.applyPattern("S");
        da.setDateFormatOverride(sdfmt);
        ((GanttRenderer) plot.getRenderer()).setShadowVisible(false);
        int width = 500 + 10 * plan.getDuration();
        int height = 50 + 20 * plan.size();
        try {
            switch (fmt) {
                case png:
                    ChartUtilities.saveChartAsPNG(new File(getOutputFile()), chart, width, height);
                    break;
                case jpg:
                    ChartUtilities.saveChartAsJPEG(new File(getOutputFile()), chart, width, height);
                    break;
            }
        } catch (IOException e) {
            Plan.logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * Unused.
     *
     * @param a the action to add
     */
    @Override
    public void inject(Migration a) {
    }

    /**
     * Unused.
     *
     * @param a the action to add
     */
    @Override
    public void inject(Run a) {
    }

    /**
     * Unused.
     *
     * @param a the action to add
     */
    @Override
    public void inject(Stop a) {
    }

    /**
     * Unused.
     *
     * @param a the action to add
     */
    @Override
    public void inject(Startup a) {
    }

    /**
     * Unused.
     *
     * @param a the action to add
     */
    @Override
    public void inject(Shutdown a) {
    }

    /**
     * Unused.
     *
     * @param a the action to add
     */
    @Override
    public void inject(Resume a) {
    }

    /**
     * Unused.
     *
     * @param a the action to add
     */
    @Override
    public void inject(Suspend a) {
    }

    /**
     * Unused.
     *
     * @param a the action to add
     */
    @Override
    public void inject(Pause a) {
    }

    /**
     * Unused.
     *
     * @param a the action to add
     */
    @Override
    public void inject(UnPause a) {
    }
}
