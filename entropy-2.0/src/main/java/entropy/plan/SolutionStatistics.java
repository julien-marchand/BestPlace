/*
 * Copyright (c) Fabien Hermenier
 *
 *        This file is part of Entropy.
 *
 *        Entropy is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU Lesser General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        Entropy is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU Lesser General Public License for more details.
 *
 *        You should have received a copy of the GNU Lesser General Public License
 *        along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.plan;

/**
 * Statistic about a solution.
 *
 * @author Fabien Hermenier
 */
public class SolutionStatistics extends SolvingStatistics {

    /**
     * The objective value of the solution.
     */
    private int objective;

    /**
     * Indicates whether the solution denotes a solution of an optimization problem or not.
     */
    public boolean isOptimizationProblem;

    /**
     * Make a new statistics for a given solution of an optimization problem.
     *
     * @param nbNodes      the number of opened node to get the solution
     * @param nbBacktracks the number of backtracks to get the solution
     * @param timeCount    the duration of the solving process to get the solution in ms
     * @param objective    the value of the objective
     */
    public SolutionStatistics(int nbNodes, int nbBacktracks, int timeCount, boolean timeout, int objective) {
        super(nbNodes, nbBacktracks, timeCount, timeout);
        isOptimizationProblem = true;
        this.objective = objective;
    }

    /**
     * Make a new statistics for a given solution of a satisfaction problem.
     *
     * @param nbNodes      the number of opened node to get the solution
     * @param nbBacktracks the number of backtracks to get the solution
     * @param timeCount    the duration of the solving process to get the solution in ms
     */
    public SolutionStatistics(int nbNodes, int nbBacktracks, int timeCount, boolean timeout) {
        super(nbNodes, nbBacktracks, timeCount, timeout);
        isOptimizationProblem = false;
    }

    /**
     * Indicates whether the solution is a solution of an optimization problem
     *
     * @return {@code true} is the solution denotes a solution for an optimization problem
     */
    public boolean isOptimizationProblem() {
        return this.isOptimizationProblem;
    }

    /**
     * @return get the objective value of the solution.
     */
    public int getObjective() {
        return objective;
    }


    @Override
    public String toString() {
        if (isOptimizationProblem) {
            return new StringBuilder("obj=").append(objective).append(", ").append(super.toString()).toString();
        }
        return super.toString();
    }

    /**
     * Export data to a String that sump up the solution. Parseable data
     * Fields are separated by space. First is the objective, second the number of
     * opened nodes, third is the number of backtracks. Last is the time
     *
     * @return a String
     */
    @Override
    public String toRawData() {
        return new StringBuilder().append(isOptimizationProblem ? objective : '-').append(' ').append(super.toRawData()).toString();
    }

    @Override
    public SolutionStatistics clone() {
        SolutionStatistics c = new SolutionStatistics(getNbNodes(), getNbBacktracks(), getTimeCount(), hasReachedTimeout());
        c.isOptimizationProblem = isOptimizationProblem;
        c.objective = objective;
        return c;
    }
}
