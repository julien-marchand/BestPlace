package gipad.configuration;

import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;

public class ConstFunc implements CostFunction {

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
     * @param i 
     */
    public ConstFunc(int m, int st, int r, int lres, int rres, int su, int start, int shut) {
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

    public int evaluateLocalResume(VirtualMachine vm) {
        return this.costs[IDX_LOCAL_RESUME];
    }

    public int evaluateLocalSuspend(VirtualMachine vm) {
        return this.costs[IDX_SUSPEND];
    }

    public int evaluateMigration(VirtualMachine vm) {
        return this.costs[IDX_MIGRATE];
    }

    public int evaluateRemoteResume(VirtualMachine vm) {
        return this.costs[IDX_REMOTE_RESUME];
    }

    public int evaluateRun(VirtualMachine vm) {
        return this.costs[IDX_RUN];
    }

    public int evaluateStop(VirtualMachine vm) {
        return this.costs[IDX_STOP];
    }

    public int evaluateShutdown(Node node) {
        return this.costs[IDX_SHUTDOWN];
    }

    public int evaluateStartup(Node node) {
        return this.costs[IDX_START];
    }

	public int getCout() {
		return 0;
	}

}
