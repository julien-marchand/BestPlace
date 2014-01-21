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
package entropy.plan;

import java.io.IOException;

import entropy.configuration.Configuration;
import entropy.execution.TimedExecutionGraph;
import entropy.plan.action.Action;
import entropy.plan.parser.TimedReconfigurationPlanSerializer;
import entropy.plan.visualization.PlanVisualizer;

public class MockAction extends Action {

    public MockAction(int s, int f) {
        super(s, f);
    }

    @Override
    public boolean insertIntoGraph(TimedExecutionGraph g) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean apply(Configuration c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCompatibleWith(Configuration src) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean isCompatibleWith(Configuration c, Configuration dst) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public void injectToVisualizer(PlanVisualizer vis) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void serialize(TimedReconfigurationPlanSerializer s) throws IOException {
        throw new UnsupportedOperationException();
    }

}
