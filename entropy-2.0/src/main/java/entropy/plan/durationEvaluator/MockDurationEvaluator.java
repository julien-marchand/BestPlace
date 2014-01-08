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
package entropy.plan.durationEvaluator;


import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;

/**
 * A mock object to get various duration.
 * Each duration is a constant given during instantiation
 *
 * @author Fabien Hermenier
 */
public class MockDurationEvaluator implements DurationEvaluator {

    private static final int IDX_MIGRATE = 0;

    private static final int IDX_STOP = 1;

    private static final int IDX_RUN = 2;

    private static final int IDX_LOCAL_RESUME = 3;

    private static final int IDX_REMOTE_RESUME = 4;

    private static final int IDX_SUSPEND = 5;

    private static final int IDX_START = 6;

    private static final int IDX_SHUTDOWN = 7;

    private int[] costs;

    /**
     * Make a new mock with some specific costs.
     *
     * @param m     the cost of a migration
     * @param st    the cost of a stop
     * @param r     the cost of a run
     * @param lres  the cost of a local resume
     * @param rres  the cost of a remote resume
     * @param su    the cost of a suspend
     * @param start the cost of a startup
     * @param shut  the cost of a shutdown
     */
    public MockDurationEvaluator(int m, int st, int r, int lres, int rres, int su, int start, int shut) {
        costs = new int[8];
        costs[IDX_MIGRATE] = m;
        costs[IDX_STOP] = st;
        costs[IDX_RUN] = r;
        costs[IDX_LOCAL_RESUME] = lres;
        costs[IDX_REMOTE_RESUME] = rres;
        costs[IDX_SUSPEND] = su;
        costs[IDX_START] = start;
        costs[IDX_SHUTDOWN] = shut;
    }

    @Override
    public int evaluateLocalResume(VirtualMachine vm) {
        return this.costs[IDX_LOCAL_RESUME];
    }

    @Override
    public int evaluateLocalSuspend(VirtualMachine vm) {
        return this.costs[IDX_SUSPEND];
    }

    @Override
    public int evaluateMigration(VirtualMachine vm) {
        return this.costs[IDX_MIGRATE];
    }

    @Override
    public int evaluateRemoteResume(VirtualMachine vm) {
        return this.costs[IDX_REMOTE_RESUME];
    }

    @Override
    public int evaluateRun(VirtualMachine vm) {
        return this.costs[IDX_RUN];
    }

    @Override
    public int evaluateStop(VirtualMachine vm) {
        return this.costs[IDX_STOP];
    }

    @Override
    public int evaluateShutdown(Node node) {
        return this.costs[IDX_SHUTDOWN];
    }

    @Override
    public int evaluateStartup(Node node) {
        return this.costs[IDX_START];
    }

    /**
     * Set a constant duration for an action
     *
     * @param type the type of the action.
     * @param val  the duration
     * @return (@code true} if the action duration has been updated
     */
    public boolean set(int type, int val) {
        if (type >= 0 && type < costs.length) {
            costs[type] = val;
            return true;
        }
        return false;
    }

}
