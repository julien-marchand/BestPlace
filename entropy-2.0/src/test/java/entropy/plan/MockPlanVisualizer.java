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

package entropy.plan;

import java.util.LinkedList;
import java.util.List;

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
import entropy.plan.visualization.PlanVisualizer;

/**
 * @author Fabien Hermenier
 */
public class MockPlanVisualizer implements PlanVisualizer {

    private List<Action> injected;

    public MockPlanVisualizer() {
        injected = new LinkedList<Action>();
    }

    @Override
    public boolean buildVisualization(TimedReconfigurationPlan plan) {
        return false;
    }

    public boolean isInjected(Action a) {
        return injected.contains(a);
    }

    @Override
    public void inject(Migration a) {
        injected.add(a);
    }

    @Override
    public void inject(Run a) {
        injected.add(a);
    }

    @Override
    public void inject(Stop a) {
        injected.add(a);
    }

    @Override
    public void inject(Startup a) {
        injected.add(a);
    }

    @Override
    public void inject(Shutdown a) {
        injected.add(a);
    }

    @Override
    public void inject(Resume a) {
        injected.add(a);
    }

    @Override
    public void inject(Suspend a) {
        injected.add(a);
    }

    @Override
    public void inject(Pause a) {
        injected.add(a);
    }

    @Override
    public void inject(UnPause a) {
        injected.add(a);
    }
}
