package gipad.plan.choco.constraints;

import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;

import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: Arnaud Letort
 * Date: 22/03/13
 * Time: 11:45
 *
 * Implementation of the k-dimensional sweep algorithm RING version (without precedence relations).
 */
@SuppressWarnings({"serial","unused","rawtypes"})
public class PropTTPCDynamicSweepLoads extends Propagator<IntVar> {
    private final Solver s;

    private final int nbTasks;
    private final int nbResources;
    private final int[] capacities;
    private final int[][] successors;
    private final int[][] predecessors;

    // ===== LOADS (begin) =====
    private final int[] interestingTimePoints;
    private final int[] interestingResources;
    private int nextIndexInterestingTP;
    private final IntVar[][] loads;
    
    // ===== LOADS (end) =====

    private DynamicSweepMinKDimPrecColRings sweepMin;
    private DynamicSweepMaxKDimPrecColRings sweepMax;
    private DynamicSweepGreedyKDimPrecColRings sweepGreedy;
    private final boolean aggregateMode; // Optimization
    private final boolean greedyMode;
    private final Rings ring;
    private int[] mapping; // mapping[relative id] = absolute id
    private int[] mappingRev; // mappingRev[absolute id] = relative id
    private int nbTasksInFilteringAlgo;
    private final int[] resourceTypes; // resourceTypes[r] give the type of the r^h resource, 0 = cumulative, 1 = colored
    public static final int CUMULATIVE = 0;
    public static final int COLORED = 1;
	private int nbEventsToAdd;
    private int[] datesAPEvents;
    private int[][] heightsAPEvents;

    private int[] ls;
    private int[] us;
    private int[] ld;
    private int[] le;
    private int[] ue;
    private int[][] c;
    // [COLORS]
    // Allow to have non-contigus colors on a given resource (+ scan the colors used (sparse))
    private int[][] cUsed; // [RESOURCE][INDEX]. cUsed[r][i] give the i(th) color used on resource r.
    // [COLORS]

    /**
     *
     * @param vars The variables in the following order :
     *  starts, durations, ends,
     *  list_of_consumptions_of_t0, list_of_consumptions_of_t1, ...
     *
     *  capacity_of_resource_r0, capacity_of_resource_r1, ...
     *
     *  load_of_interestingTimePoints_0_interestingResources_0, load_of_interestingTimePoints_0_interestingResources_1, ...
     *  load_of_interestingTimePoints_1_interestingResources_0, load_of_interestingTimePoints_1_interestingResources_1, ...
     *
     *
     *
     * @param nbTasks The number of tasks (items)
     * @param nbResources The number of resources (dimensions)
     * @param capacities The capacity of each resource
     * @param succ The successors matrix, where succ[t] gives the list of successors of task t
     * @param resourceTypes The type of each resource (possible values are : PropTTPCDynamicSweepLoads.CUMULATIVE and PropTTPCDynamicSweepLoads.COLORED)
     * @param interestingTimePoints The list of interesting time points
     * @param interestingResources The list of interesting resources
     */
    public PropTTPCDynamicSweepLoads(IntVar[] vars, int nbTasks, int nbResources, int[] capacities, int[][] succ, int[] resourceTypes, int[] interestingTimePoints, int[] interestingResources) {
        super(vars, PropagatorPriority.QUADRATIC,false);
        this.s = solver;
        this.nbTasks = nbTasks;
        this.nbResources = nbResources;
        this.capacities = capacities;
        this.resourceTypes = resourceTypes;
        this.aggregateMode = false; // TODO
        this.greedyMode = false;
        this.nbTasksInFilteringAlgo = nbTasks;
        this.nbEventsToAdd = 0;
        this.datesAPEvents = null;
        this.heightsAPEvents = null;
        this.ring = new Rings(nbResources,nbTasks);
        this.ls = new int[nbTasks];
        this.us = new int[nbTasks];
        this.ld = new int[nbTasks];
        this.le = new int[nbTasks];
        this.ue = new int[nbTasks];
        this.c = new int[nbTasks][nbResources];
        int[] nbpreds = new int[nbTasks];
        this.successors = new int[succ.length][];
        for (int i=0;i<nbTasks;i++) {
            this.successors[i] = new int[succ[i].length];
            for (int j=0;j<successors[i].length;j++) {
                this.successors[i][j] = succ[i][j];
                nbpreds[successors[i][j]]++;
            }
        }
        this.predecessors = new int[succ.length][];
        for (int i=0;i<nbTasks;i++) {
            this.predecessors[i] = new int[nbpreds[i]];
        }
        int tsuc;
        for (int i=0;i<nbTasks;i++) {
            if (successors[i] != null) {
                for (int j=0;j<successors[i].length;j++) {
                    tsuc = successors[i][j];
                    nbpreds[tsuc]--;
                    predecessors[tsuc][nbpreds[tsuc]] = i;
                }
            }
        }
        // [COLORS]
        int nbRead, i; // temp
        cUsed = new int[nbResources][];
        boolean[] colorsOnR = new boolean[nbTasks+1]; // colorsOnR[i] indicates whether or not color i is used on resource r.
        int nbColorsOnR;
        for (int r=0;r<nbResources;r++) {
            if (resourceTypes[r] == COLORED) {
                nbColorsOnR = 1; // neutral color
                for (int t=0;t<nbTasks;t++) {
                    if (colorsOnR[getColor(vars,t,r,nbTasks,nbResources)] == false) {
                        nbColorsOnR++;
                        colorsOnR[getColor(vars,t,r,nbTasks,nbResources)] = true;
                    }
                }
                cUsed[r] = new int[nbColorsOnR];
                nbRead = 1; // neutral color
                i = 0;
                while (nbRead != nbColorsOnR) {
                    if ( colorsOnR[i] ) {
                        cUsed[r][nbRead] = i;
                        nbRead++;
                    }
                    i++;
                }
            } else {
                assert (resourceTypes[r] == CUMULATIVE);
                cUsed[r] = null;
            }
        // [COLORS]
        }
        // ===== LOADS (begin) =====
        this.loads = new IntVar[interestingTimePoints.length][interestingResources.length];
        this.interestingTimePoints = interestingTimePoints;
        this.interestingResources = interestingResources;
        this.nextIndexInterestingTP = 0;
        int lIdx = 3*nbTasks+nbTasks*nbResources+nbResources;
        for (i=0;i<interestingTimePoints.length;i++) {
            for (int j=0;j<interestingResources.length;j++) {
                loads[i][j] = vars[lIdx+i*interestingResources.length+j];
            }
        }
        // ===== LOADS (end) =====
    }

    private int getColor(IntVar[] vars, int t, int r, int nbTasks, int nbResources) {
        return vars[3*nbTasks+t*nbResources+r].getValue();
    }


    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.BOUND.mask + EventType.INSTANTIATE.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        this.mainLoop();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        this.forcePropagate(EventType.FULL_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        int minStart = Integer.MAX_VALUE;
		int maxEnd = Integer.MIN_VALUE;
		// compute min start and max end
		for(int is=0, id=nbTasks, ie=2*nbTasks, ih=3*nbTasks;is<this.nbTasks;is++,id++,ie++,ih+=nbResources) { // is = start index, ..., ih = height index
			if (!vars[is].instantiated() || !vars[id].instantiated() || !vars[ie].instantiated()) return ESat.UNDEFINED;
            for(int r=0;r< nbResources;r++) {
                if (!vars[ih+r].instantiated()) return ESat.UNDEFINED;
            }
			if (vars[is].getValue() < minStart) minStart = vars[is].getValue();
			if (vars[ie].getValue() > maxEnd) maxEnd = vars[ie].getValue();
		}
		int[] sumHeight = new int[nbResources];
        int[][] countColors = new int[nbResources][];
        int[] nbDistinctColors = new int[nbResources];
        int ctr;
		// scan the time axis and check the heights and the colors
		for(int i=minStart;i<=maxEnd;i++) {
			for(int r=0;r< nbResources;r++) {
                if (resourceTypes[r] == CUMULATIVE) {
                    sumHeight[r] = 0;
                } else if (resourceTypes[r] == COLORED) {
                    countColors[r] = new int[nbTasks+1]; // +1 for the neutral color.
                    nbDistinctColors[r] = 0;
                } else {
//                    System.out.println(" ! unknown resource type ! ");
                    assert false;
                }

            }
			for(int is=0, ie=2*nbTasks, ih=3*nbTasks;is<this.nbTasks;is++,ie++,ih += nbResources) {
				if ( i >= vars[is].getValue() && i < vars[ie].getValue() ) { // the task overlap the time point.
                    for(int r=0;r< nbResources;r++) {
                        if (resourceTypes[r] == CUMULATIVE) {
                            sumHeight[r] += vars[ih+r].getValue();
                        } else if (resourceTypes[r] == COLORED) {
                            ctr = vars[ih+r].getValue();
                            if (countColors[r][ctr] == 0) {
                                nbDistinctColors[r]++;
                            }
                            countColors[r][ctr]++;
                        }
                    }
                }
			}
			for(int r=0;r< nbResources;r++) {
                if (resourceTypes[r] == CUMULATIVE) {
                    if (sumHeight[r] > capacities[r]) {
//                        System.out.println(" resource (cumulative) overflow r"+r+" i="+i+" sumHeight[r]="+sumHeight[r]+" > capacities[r]="+capacities[r]);
                        return ESat.FALSE;
                    }
                } else if (resourceTypes[r] == COLORED) {
                    if (nbDistinctColors[r] > capacities[r]) {
//                        System.out.println(" resource (colored) overflow r"+r+" i="+i+" nbDistinctColors[r]="+nbDistinctColors[r]+" > capacities[r]="+capacities[r]);
                        return ESat.FALSE;
                    }
                } else {
                    assert (false);
                }
            }
		}
        // scan precedence relations
        for (int is=0, ie=2*nbTasks;is<this.nbTasks;is++,ie++) {
            for (int j=0;j<successors[is].length;j++) {
                if ( vars[ie].getValue() > vars[successors[is][j]].getValue() ) {
                    return ESat.FALSE;
                }
            }
        }
		return ESat.TRUE;
    }

    public void mainLoop() throws ContradictionException {
        int state = 0;
		int[][] eventsToAdd;
		boolean succeed = false;
		boolean res, max;

        do {
//            for(int is=0;is<nbTasks;is++) {
//				vars[is].notifyMonitors(null, null);
//			}
            // copy variable bounds into arrays
            //copyAndAggregate(ls,us,ld,le,ue,h);
            copyAndAggregate();

            // ===== GREEDY MODE =====
            if (greedyMode == true && state == 0) { // greedy mode is ON and it is the first loop
                this.sweepGreedy = new DynamicSweepGreedyKDimPrecColRings();
                succeed = this.sweepGreedy.greedy();
                if (succeed) {
                    assert(this.sweepGreedy.allTasksAreFixed());
                    for(int is=0;is<this.nbTasksInFilteringAlgo;is++) { // update variables and stop !
					    vars[mapping[is]].updateLowerBound(sweepGreedy.ls(is), this);
						vars[mapping[is]].updateUpperBound(sweepGreedy.us(is), this);
						vars[mapping[is]+2*nbTasks].updateLowerBound(sweepGreedy.le(is), this);
						vars[mapping[is]+2*nbTasks].updateUpperBound(sweepGreedy.ue(is), this);
                    }
                }
            }

            // ===== NORMAL MODE =====
            if (greedyMode == false || !succeed) { // greedy mode is OFF or fails. Run the dynamic sweep
                this.sweepMin = new DynamicSweepMinKDimPrecColRings();
                this.sweepMax = new DynamicSweepMaxKDimPrecColRings();
                res = sweepMin.sweepMin();
                res = sweepMax.sweepMax();
                max = false;
                while (res) {
                    if (max) {
                        if (sweepMin.isSweepMaxNeeded()) { // check if sweep max should be run.
                            res = sweepMax.sweepMax();
                        } else {
                            res = false;
                            assert(false == sweepMax.sweepMax());
                        }
		            } else {
		                res = sweepMin.sweepMin();
		            }
		            max = !max;
                }
                state = 0;
                //update variable bounds
                boolean allFixed = true;
                for(int is=0;is<nbTasksInFilteringAlgo;is++) {
                    vars[mapping[is]].updateLowerBound(ls[is],this);
                    vars[mapping[is]].updateUpperBound(us[is],this);
                    vars[mapping[is]+nbTasks].updateLowerBound(ld[is],this);
                    vars[mapping[is]+2*nbTasks].updateLowerBound(le[is],this);
                    vars[mapping[is]+2*nbTasks].updateUpperBound(ue[is],this);
                    state = state + (us[is]-ls[is])+(ue[is]-le[is])+ld[is];
                    // ===== LOADS (begin) ===== allFixed is true iff all the tasks are fixed
                    if (allFixed && (us[is]-ls[is])+(ue[is]-le[is]) != 0) {
                        allFixed = false;
                    }
                    // ===== LOADS (end) =====
			    }
                // check !
                for(int is=0;is<nbTasksInFilteringAlgo;is++) {
                    state = state-(vars[mapping[is]].getUB()-vars[mapping[is]].getLB())
                                 -(vars[mapping[is]+2*nbTasks].getUB()-vars[mapping[is]+2*nbTasks].getLB())
                                 -vars[mapping[is]+nbTasks].getLB();
                }
                // ===== LOADS (begin) =====
                int lIdx = 3*nbTasks+nbTasks*nbResources+nbResources;
                if (allFixed && state == 0) {
                    for (int i=0;i<interestingTimePoints.length;i++) {
                        for (int j=0;j<interestingResources.length;j++) {
                            loads[i][j].updateUpperBound(loads[i][j].getLB(),this);
//System.out.println("load tp="+interestingTimePoints[i]+" res="+interestingResources[j]+" = "+loads[i][j].getValue());
                        }
                    }
                }
                // ===== LOADS (end) =====
            }

        } while (state!= 0);
    }

    /**
	 * Copy bounds of rtasks into arrays.
	 * Build an aggregated cumulative profile with instantiated tasks.
	 *
	 */
	public void copyAndAggregate() throws ContradictionException {
		nbEventsToAdd = 0;
		mapping = new int[nbTasks];
        mappingRev = new int[nbTasks]; // absolute => relative
		if (aggregateMode == true) {
            boolean[] isFixed = new boolean[nbTasks];
			IncHeapEvents hEvents = new IncHeapEvents(2*nbTasks);
			datesAPEvents = null;
            heightsAPEvents = null;

            // FIRST LOOP: generate events related to fixed tasks; record fixed tasks (in isFixed)
			for(int is=0, id=nbTasks, ie=2*nbTasks, ih=3*nbTasks;is<nbTasks;is++,id++,ie++,ih+=nbResources) {
				if (vars[is].instantiated() && vars[id].instantiated() && vars[ie].instantiated()) {
				    // the task is instantiated.
                    isFixed[is] = true;
					hEvents.add(vars[is].getValue(), is, Event.FSCP, -1);
					hEvents.add(vars[ie].getValue(), is, Event.FECP, -1);
				}
			}
            // SECOND LOOP: update bounds of succ and pred of fixed tasks.
            int len;
            for(int is=0, ie=2*nbTasks; is<isFixed.length;is++,ie++) {
                if ( isFixed[is] )  {
                    for (int sid=0;sid<successors[is].length;sid++) { // update earliest start of successors.
                        vars[successors[is][sid]].updateLowerBound(vars[ie].getValue(), this);
                    }
                    for (int sid=0;sid<predecessors[is].length;sid++) { // update latest end of predecessors.
                        vars[2*nbTasks+predecessors[is][sid]].updateUpperBound(vars[ie].getValue(), this);
                    }
                }
            }
            // THIRD LOOP: copy of variable bounds.
            int copyIdx = 0;
            for(int is=0, id=nbTasks, ie=2*nbTasks, ih=3*nbTasks;is<nbTasks;is++,id++,ie++,ih+=nbResources) {
				if ( !isFixed[is] ) {
                    ls[copyIdx] = vars[is].getLB();
                    us[copyIdx] = vars[is].getUB();
                    ld[copyIdx] = vars[id].getLB();
                    le[copyIdx] = vars[ie].getLB();
                    ue[copyIdx] = vars[ie].getUB();
                    for (int r=0;r<nbResources;r++) {
                        c[copyIdx][r] = vars[ih+r].getLB();
                    }
                    mapping[copyIdx] = is;
                    mappingRev[is] = copyIdx;
                    copyIdx++;
                } else { // task 'is' is fixed => task 'is' has no mappingRev..
                    mappingRev[is] = -1;
                }
            }
			nbTasksInFilteringAlgo = copyIdx;
			// build the aggregated profile
			if (!hEvents.isEmpty()) { // there are instantiated tasks
                int hidx;
				datesAPEvents = new int[hEvents.nbEvents()];
                heightsAPEvents = new int[nbResources][hEvents.nbEvents()];
				int evtIdx = 0;
				int[] curEvt = hEvents.peek();
				int delta, date;
                int[] height, prevHeight;
				delta = curEvt[0];
				date = delta;
				prevHeight = new int[nbResources]; // init. to 0
				height = new int[nbResources]; // init. to 0
				while (!hEvents.isEmpty()) {
					if (date != delta) {
						if (differ(prevHeight, height)) { // variation of the consumption
							datesAPEvents[evtIdx] = delta; // event date
							for (int r=0;r<nbResources;r++) {
                                heightsAPEvents[r][evtIdx] = prevHeight[r]-height[r]; // decrement
                            }
                            evtIdx++;
							for (int r=0;r<nbResources;r++) { //prevHeight = height.clone();
                                prevHeight[r] = height[r];
                            }
						}
						delta = date;
					}
					curEvt = hEvents.poll();
                    hidx = 3 * nbTasks + curEvt[1] * nbResources;
					if (curEvt[2] == Event.FSCP) {
                        for (int r=0;r<nbResources;r++) {
                            height[r] += vars[hidx+r].getValue();
                        }
                    } else if (curEvt[2] == Event.FECP) {
                        for (int r=0;r<nbResources;r++) {
                            height[r] -= vars[hidx+r].getValue();
                        }
                    }

                    if (!hEvents.isEmpty()) date = hEvents.peekDate();
				}
				// creer le dernier !
				datesAPEvents[evtIdx] = date;
				for (int r=0;r<nbResources;r++) {
                    heightsAPEvents[r][evtIdx] = prevHeight[r]-height[r]; // decrement
                }
				nbEventsToAdd = evtIdx + 1;
			}

		} else {
			for(int is=0, id=nbTasks, ie=2*nbTasks, ih=3*nbTasks;is<nbTasks;is++,id++,ie++,ih+=nbResources) {
				ls[is] = vars[is].getLB();
				us[is] = vars[is].getUB();
				ld[is] = vars[id].getLB();
				le[is] = vars[ie].getLB();
				ue[is] = vars[ie].getUB();
                for(int r=0;r<nbResources;r++) {
				    c[is][r] = vars[ih+r].getValue();
                }
				mapping[is] = is;
                mappingRev[is] = is;
			}
		}
	}

    private boolean differ(int[] a, int[] b) {
        int i=0;
        while (i<a.length &&  (a[i] == b[i]) ) {
            i++;
        }
        return i<a.length;
    }

    public int getRelativeId(int absoluteId) {
        return mappingRev[absoluteId];
    }

    public int getAbsoluteId(int relativeId) {
        return mapping[relativeId];
    }

    /**
     * Circular double linked lists which record the status of the tasks.
     */
    class Rings {

        protected final int nbItems;

        public final int ptrNone;
        public final int ptrReady;
        public final int ptrCheck;
        public final int ptrConflict; // add the resource id to get the corresponding list of tasks in conflict

        public final static int NONE = -1;
        public final static int READY = -2;
        public final static int CHECK = -3;

        protected final int[] ring;
        protected final int[] backward;
        protected final int[] forward;

        public Rings(int k, int n) {
            this.nbItems = n;
            this.ring = new int[n+3+k];
            this.backward = new int[n+3+k];
            this.forward = new int[n+3+k];
            for (int i=0;i<n;i++) {
                this.ring[i] = -1;
                this.backward[i] = i;
                this.forward[i] = i;
            }
            this.ptrNone = n;
            this.ptrReady = n+1;
            this.ptrCheck = n+2;
            this.ptrConflict = n+3;
            for (int i=n;i<ring.length;i++) {
                ring[i] = -666;
                backward[i] = i;
                forward[i] = i;
            }
        }

        public void setNone(int t) {
            // supprime t du ring courant
            forward[backward[t]] = forward[t];  // le suivant du precedent de t = le suivant de t
            backward[forward[t]] = backward[t]; // le precedent du suivant de t = le precedent de t
            // pas de ring pour none en pratique. (car aucun parcours de cette liste)
            forward[t] = t;
            backward[t] = t;
            ring[t] = NONE;
        }

        public void setReady(int t) {
            // supprime t du ring courant
            forward[backward[t]] = forward[t];  // le suivant du precedent de t = le suivant de t
            backward[forward[t]] = backward[t]; // le precedent du suivant de t = le precedent de t
            // insertion dans le ring none.
            forward[t] = forward[ptrReady];
            backward[t] = ptrReady;
            forward[ptrReady] = t;
            backward[forward[t]] = t;
            ring[t] = READY;
        }

        public void setCheck(int t) {
            // supprime t du ring courant
            forward[backward[t]] = forward[t];  // le suivant du precedent de t = le suivant de t
            backward[forward[t]] = backward[t]; // le precedent du suivant de t = le precedent de t
            // insertion dans le ring check.
            forward[t] = forward[ptrCheck];
            backward[t] = ptrCheck;
            forward[ptrCheck] = t;
            backward[forward[t]] = t;
            ring[t] = CHECK;
        }

        public void setConflict(int t, int rc) {
            assert(rc >= 0);
            // supprime t du ring courant
            forward[backward[t]] = forward[t];  // le suivant du precedent de t = le suivant de t
            backward[forward[t]] = backward[t]; // le precedent du suivant de t = le precedent de t
            // insertion dans le ring conflict.
            forward[t] = forward[ptrConflict+rc];
            backward[t] = ptrConflict+rc;
            forward[ptrConflict+rc] = t;
            backward[forward[t]] = t;
            ring[t] = rc;
        }


        public int firstInCheck() {
            return forward[ptrCheck];
        }

        public int firstInConflict(int r) {
            return forward[ptrConflict+r];
        }

        public int firstInReady() {
            return forward[ptrReady];
        }

        public int next(int t) {
            return forward[t];
        }

        public boolean inConflict(int t) {
            return (ring[t] >= 0);
        }

        public boolean inNone(int t) {
            return (ring[t] == NONE);
        }

        public boolean inReady(int t) {
            return (ring[t] == READY);
        }

        public boolean inCheck(int t) {
            return (ring[t] == CHECK);
        }

        public boolean isEmptyReady() {
            return (ptrReady == forward[ptrReady]);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("NONE     : "+noneToString()+"\n")
              .append("READY    : "+readyToString()+"\n")
              .append("CHECK    : "+checkToString()+"\n");
    //          .append("CONFLICT : "+conflictToString()+"\n");
            return sb.toString();
        }

        public String noneToString() {
            StringBuffer sb = new StringBuffer("[ ");
            for (int i=0;i<nbItems;i++) {
                if (ring[i] == NONE) {
                    sb.append(" t"+i);
                }
            }
            sb.append(" ]");
            return sb.toString();
        }

        public String readyToString() {
            StringBuffer sb = new StringBuffer("[ ");
            int ptrCur = forward[ptrReady];
            while (ptrCur != ptrReady) {
                sb.append(" t"+ptrCur);
                ptrCur = forward[ptrCur];
            }
            sb.append(" ]");
            return sb.toString();
        }

        public String checkToString() {
            StringBuffer sb = new StringBuffer("[ ");
            int ptrCur = forward[ptrCheck];
            while (ptrCur != ptrCheck) {
                sb.append(" t"+ptrCur);
                ptrCur = forward[ptrCur];
            }
            sb.append(" ]");
            return sb.toString();
        }
    }


    /**
     * Heap recording the events by ascending order.
     */
    class IncHeapEvents {
        private int[] date; // Key !
        private int[] task;
        private int[] type;
        private int[] dec;
        private int size;
        private int nbEvents;

        public final int[][] bufferPoll;

        public IncHeapEvents(int _size) {
            this.nbEvents = 0;
            this.size = _size;
            date = new int[size];
            task = new int[size];
            type = new int[size];
            dec = new int[size];
            bufferPoll = new int[size][];
        }

        public void clear() {
            nbEvents = 0;
        }

        /**
         * Inserts the specified event into this heap
         * @param _date
         * @param _task
         * @param _type
         * @param _dec
         */
        public void add(int _date, int _task, int _type, int _dec) {
            int i = nbEvents;

            int parent = parent(i);
            while ( (i>0) && (date[parent] > _date) ) {
                date[i] = date[parent];
                task[i] = task[parent];
                type[i] = type[parent];
                dec[i] = dec[parent];
                i = parent;
                parent = parent(i);
            }
            date[i] = _date;
            task[i] = _task;
            type[i] = _type;
            dec[i] = _dec;
            nbEvents++;
        }

        /**
         * Retrieves and removes the top of this heap, or returns null if this queue is empty.
         * @return
         */
        public int[] poll() {
            if (nbEvents == 0) return null;
            int[] top = new int[4];
            top[0] = date[0];
            top[1] = task[0];
            top[2] = type[0];
            top[3] = dec[0];
            nbEvents--;
            date[0] = date[nbEvents];
            task[0] = task[nbEvents];
            type[0] = type[nbEvents];
            dec[0] = dec[nbEvents];
            int vdate = date[0];
            int vtask = task[0];
            int vtype = type[0];
            int vdec = dec[0];
            int i = 0;
            int j;
            while (!isLeaf(i)) {
                j = leftChild(i);
                if (hasRightChild(i) && date[rightChild(i)] < date[leftChild(i)]) {
                    j = rightChild(i);
                }
                if (vdate <= date[j]) break;
                date[i] = date[j];
                task[i] = task[j];
                type[i] = type[j];
                dec[i] = dec[j];
                i = j;
            }
            date[i] = vdate;
            task[i] = vtask;
            type[i] = vtype;
            dec[i] = vdec;
            return top;
        }

        public int pollAllTopItems() {
            if (nbEvents == 0) return 0;
            int firstDate = date[0];
            int nbExtractedItems = 0;
            while (!isEmpty() && date[0] == firstDate) {
                bufferPoll[nbExtractedItems] = poll();
                if (bufferPoll[nbExtractedItems] != null) { nbExtractedItems++; }
                else { break; }
            }
            return nbExtractedItems;
        }

        /**
         * Removes the top of this heap. (does not check if empty)
         * @return
         */
        public void remove() {
            nbEvents--;
            date[0] = date[nbEvents];
            task[0] = task[nbEvents];
            type[0] = type[nbEvents];
            dec[0] = dec[nbEvents];
            int vdate = date[0];
            int vtask = task[0];
            int vtype = type[0];
            int vdec = dec[0];
            int i = 0;
            int j;
            while (!isLeaf(i)) {
                j = leftChild(i);
                if (hasRightChild(i) && date[rightChild(i)] < date[leftChild(i)]) {
                    j = rightChild(i);
                }
                if (vdate <= date[j]) break;
                date[i] = date[j];
                task[i] = task[j];
                type[i] = type[j];
                dec[i] = dec[j];
                i = j;
            }
            date[i] = vdate;
            task[i] = vtask;
            type[i] = vtype;
            dec[i] = vdec;
        }

        /**
         * Retrieves, but does not remove, the top event of this heap.
         * @return
         */
        public int[] peek() {
            if (isEmpty()) return null;
            else {
                int[] res = new int[4];
                res[0] = date[0];
                res[1] = task[0];
                res[2] = type[0];
                res[3] = dec[0];
                return res;
            }
        }

        /**
         * Retrieves, but does not remove, the date of the top event of this heap. Doesn't check if the heap is empty.
         * @return
         */
        public int peekDate() {
            return date[0];
        }

        /**
         * Retrieves, but does not remove, the task of the top event of this heap. Doesn't check if the heap is empty.
         * @return
         */
        public int peekTask() {
            return task[0];
        }

        /**
         * Retrieves, but does not remove, the type of the top event of this heap. Doesn't check if the heap is empty.
         * @return
         */
        public int peekType() {
            return type[0];
        }

        /**
         * Retrieves, but does not remove, the decrement of the top event of this heap. Doesn't check if the heap is empty.
         * @return
         */
        public int peekDec() {
            return dec[0];
        }

        public boolean isEmpty() {
            return (nbEvents == 0);
        }

        private int parent(int _child) {
            return ((_child + 1) >> 1) - 1;
        }

        private int leftChild(int _parent) {
            return ((_parent + 1) << 1) - 1;
        }

        private int rightChild(int _parent) {
            return ((_parent + 1) << 1);
        }

        private boolean isLeaf(int i) {
            return ( (((i + 1) << 1) - 1) >= nbEvents);
        }

        private boolean hasRightChild(int i) {
            return ( ((i + 1) << 1) < nbEvents);
        }

        public String toString() {
            String res = "";
            int i;
            for(i=0;i<nbEvents;i++) {
                res += "<date="+date[i]+",task="+task[i]+",type=";
                switch(type[i]) {
                    case Event.SCP : res += "SCP"; break;
                    case Event.ECP : res += "ECP"; break;
                    case Event.PR : res += "PR"; break;
                    case Event.CCP : res += "CCP"; break;
                    case Event.FSCP : res += "FSCP"; break;
                    case Event.FECP : res += "FECP"; break;
                    default : res += "UNKNOWN EVENT"; assert(false); break;
            }
                res += ",dec="+dec[i]+">\n";
            }
            return res;
        }

        public String peekEvent() {
            String res = "";
            res += "<date="+date[0]+",task="+task[0]+",type=";
            switch(type[0]) {
                case Event.SCP : res += "SCP"; break;
                case Event.ECP : res += "ECP"; break;
                case Event.PR : res += "PR"; break;
                case Event.CCP : res += "CCP"; break;
                case Event.FSCP : res += "FSCP"; break;
                case Event.FECP : res += "FECP"; break;
                default : res += "UNKNOWN EVENT"; assert(false); break;
            }
            res += ",dec="+dec[0]+">\n";
            return res;
        }

        public int nbEvents() {
            return nbEvents;
        }
    }

    /**
     * Heap recording the events by descending order.
     */
    class DecHeapEvents {

        private int[] date; // Key !
        private int[] task;
        private int[] type;
        private int[] dec;
        private int size;
        private int nbEvents;

        public final int[][] bufferPoll;

        public DecHeapEvents(int _size) {

            this.nbEvents = 0;
            this.size = _size;
            date = new int[size];
            task = new int[size];
            type = new int[size];
            dec = new int[size];
            bufferPoll = new int[size][];
        }

        public void clear() {
            nbEvents = 0;
        }

        /**
         * Inserts the specified event into this heap
         * @param _date
         * @param _task
         * @param _type
         * @param _dec
         */
        public void add(int _date, int _task, int _type, int _dec) {
            int i = nbEvents;

            int parent = parent(i);
            while ( (i>0) && (date[parent] < _date) ) {
                date[i] = date[parent];
                task[i] = task[parent];
                type[i] = type[parent];
                dec[i] = dec[parent];
                i = parent;
                parent = parent(i);
            }
            date[i] = _date;
            task[i] = _task;
            type[i] = _type;
            dec[i] = _dec;
            nbEvents++;
        }

        /**
         * Retrieves and removes the top of this heap, or returns null if this queue is empty.
         * @return
         */
        public int[] poll() {
            if (nbEvents == 0) return null;
            int[] top = new int[4];
            top[0] = date[0];
            top[1] = task[0];
            top[2] = type[0];
            top[3] = dec[0];
            nbEvents--;
            date[0] = date[nbEvents];
            task[0] = task[nbEvents];
            type[0] = type[nbEvents];
            dec[0] = dec[nbEvents];
            int vdate = date[0];
            int vtask = task[0];
            int vtype = type[0];
            int vdec = dec[0];
            int i = 0;
            int j;
            while (!isLeaf(i)) {
                j = leftChild(i);
                if (hasRightChild(i) && date[rightChild(i)] > date[leftChild(i)]) {
                    j = rightChild(i);
                }
                if (vdate >= date[j]) break;
                date[i] = date[j];
                task[i] = task[j];
                type[i] = type[j];
                dec[i] = dec[j];
                i = j;
            }
            date[i] = vdate;
            task[i] = vtask;
            type[i] = vtype;
            dec[i] = vdec;
            return top;
        }

        public int pollAllTopItems() {
            if (nbEvents == 0) return 0;
            int firstDate = date[0];
            int nbExtractedItems = 0;
            while (!isEmpty() && date[0] == firstDate) {
                bufferPoll[nbExtractedItems] = poll();
                if (bufferPoll[nbExtractedItems] != null) { nbExtractedItems++; }
                else { break; }
            }
            return nbExtractedItems;
        }

        /**
         * Removes the top of this heap. (without any check)
         * @return
         */
        public void remove() {
            nbEvents--;
            date[0] = date[nbEvents];
            task[0] = task[nbEvents];
            type[0] = type[nbEvents];
            dec[0] = dec[nbEvents];
            int vdate = date[0];
            int vtask = task[0];
            int vtype = type[0];
            int vdec = dec[0];
            int i = 0;
            int j;
            while (!isLeaf(i)) {
                j = leftChild(i);
                if (hasRightChild(i) && date[rightChild(i)] > date[leftChild(i)]) {
                    j = rightChild(i);
                }
                if (vdate >= date[j]) break;
                date[i] = date[j];
                task[i] = task[j];
                type[i] = type[j];
                dec[i] = dec[j];
                i = j;
            }
            date[i] = vdate;
            task[i] = vtask;
            type[i] = vtype;
            dec[i] = vdec;
        }

        /**
         * Retrieves, but does not remove, the top event of this heap.
         * @return
         */
        public int[] peek() {
            if (isEmpty()) return null;
            else {
                int[] res = new int[4];
                res[0] = date[0];
                res[1] = task[0];
                res[2] = type[0];
                res[3] = dec[0];
                return res;
            }
        }

        /**
         * Retrieves, but does not remove, the date of the top event of this heap. Doesn't check if the heap is empty.
         * @return
         */
        public int peekDate() {
            return date[0];
        }

        /**
         * Retrieves, but does not remove, the task of the top event of this heap. Doesn't check if the heap is empty.
         * @return
         */
        public int peekTask() {
            return task[0];
        }

        /**
         * Retrieves, but does not remove, the type of the top event of this heap. Doesn't check if the heap is empty.
         * @return
         */
        public int peekType() {
            return type[0];
        }

        /**
         * Retrieves, but does not remove, the decrement of the top event of this heap. Doesn't check if the heap is empty.
         * @return
         */
        public int peekDec() {
            return dec[0];
        }

        public boolean isEmpty() {
            return (nbEvents == 0);
        }

        private int parent(int _child) {
            return ((_child + 1) >> 1) - 1;
        }

        private int leftChild(int _parent) {
            return ((_parent + 1) << 1) - 1;
        }

        private int rightChild(int _parent) {
            return ((_parent + 1) << 1);
        }

        private boolean isLeaf(int i) {
            return ( (((i + 1) << 1) - 1) >= nbEvents);
        }

        private boolean hasRightChild(int i) {
            return ( ((i + 1) << 1) < nbEvents);
        }

        public String toString() {
            String res = "";
            int i;
            for(i=0;i<nbEvents;i++) {
                res += "<date="+date[i]+",task="+task[i]+",type=";
                switch(type[i]) {
                    case Event.SCP : res += "SCP"; break;
                    case Event.ECP : res += "ECP"; break;
                    case Event.PR : res += "PR"; break;
                    case Event.CCP : res += "CCP"; break;
                    case Event.FSCP : res += "FSCP"; break;
                    case Event.FECP : res += "FECP"; break;
                    default : res += "UNKNOWN EVENT"; assert(false); break;
            }
                res += ",dec="+dec[i]+"> ";
            }
            return res;
        }

        public String peekEvent() {
            String res = "";
            res += "<date="+date[0]+",task="+task[0]+",type=";
            switch(type[0]) {
                case Event.SCP : res += "SCP"; break;
                case Event.ECP : res += "ECP"; break;
                case Event.PR : res += "PR"; break;
                case Event.CCP : res += "CCP"; break;
                case Event.FSCP : res += "FSCP"; break;
                case Event.FECP : res += "FECP"; break;
                default : res += "UNKNOWN EVENT"; assert(false); break;
            }
            res += ",dec="+dec[0]+"> ";
            return res;
        }

        public int nbEvents() {
            return nbEvents;
        }
    }

    /**
     * Event types
     */
    class Event {
        public final static int SCP = 0; // start of a compulsory part of a fixed task.
        public final static int ECP = 1; // end of a compulsory part of a non-fixed task.
        public final static int PR = 2; // earliest start of a task.
        public final static int CCP = 3; // Latest start of a task initially without compulsory part.
        public final static int RS = 7; // earliest end of a task, needed for precedences ("Release Successors")
        public final static int FSCP = 4; // Start of a compulsory part of a fixed task.
        public final static int FECP = 5; // End of a compulsory part of a fixed task.
        public final static int AP = 6; // Aggregation event
    }

    class DynamicSweepMinKDimPrecColRings {

    private final IncHeapEvents hEvents;

    private int delta;
    private int date;

    private final int[] bufferPR;
    private int nbItemsBufferPR;
    private final int[] bufferRS;
    private int nbItemsBufferRS;

    private boolean prunning;

    private final int[] nbDistinctColors; // [RESOURCE] # of distinct colours on the given resource
    private final int[][] countColors; // [RESOURCE][COLOR] give the number of tasks using a given color on a given resource.

    private final int[] gap;
    private final int[] gapi;
    private final int[] nbpreds;

	private int maxDate;
	private int minDate;


    // h[task][resource]
	public DynamicSweepMinKDimPrecColRings() {
		this.hEvents = new IncHeapEvents(4*nbTasksInFilteringAlgo+2*(nbTasks-nbTasksInFilteringAlgo));
        this.gap = new int[nbResources];
        this.gapi = new int[nbResources];
        this.nbpreds = new int[nbTasksInFilteringAlgo];
        // [COLORS]
        this.nbDistinctColors = new int[nbResources];
        this.countColors = new int[nbResources][];
        for (int r=0;r<nbResources;r++) {
            if ( resourceTypes[r] == COLORED ) {
                this.countColors[r] = new int[nbTasks+1]; // neutral color
            } else {
                assert (resourceTypes[r] == CUMULATIVE);
                this.countColors[r] = null;
            }
        }
        // [COLORS]
        this.bufferPR = new int[nbTasksInFilteringAlgo];
        this.nbItemsBufferPR = 0;
        this.bufferRS = new int[nbTasksInFilteringAlgo];
        this.nbItemsBufferRS = 0;
        this.prunning = false;
	}


	public void addAggregatedProfile() {
		for(int i=0;i<nbEventsToAdd;i++) {
			hEvents.add(datesAPEvents[i], -1, Event.AP, i);
			if (datesAPEvents[i] < this.minDate) minDate = datesAPEvents[i];
			if (datesAPEvents[i] > this.maxDate) maxDate = datesAPEvents[i];
		}
	}

    public void adjustMin(int t, int minStart, int minEnd) {
//System.out.println("adjust task="+t+" to "+minStart);
        assert(minStart < minEnd);
        assert(minStart + ld[t] == minEnd);
        if ( minStart > ls[t] ) {
            prunning = true;
            ls[t] = minStart;
            le[t] = minEnd;
        }
    }


	protected void generateMinEvents() {
		this.hEvents.clear();
        for (int r=0;r<nbResources;r++) {
            // [COLORS]
            if ( resourceTypes[r] == COLORED ) {
                assert (countColors.length >= 1); // at least the neutral color.
                countColors[r][0] = 1; // avoid the handle the specific case of the neutral color during the sweep.
                nbDistinctColors[r] = 0;
                for (int i=1;i<countColors[r].length;i++) {
                    countColors[r][i] = 0;
                }
            } else {
                assert (resourceTypes[r] == CUMULATIVE);
                gap[r] = capacities[r];
                gapi[r] = capacities[r];
            }
            // [COLORS]
        }
        int absoluteT, relativePred;
        for(int t=0;t<nbTasksInFilteringAlgo;t++) {
            absoluteT = getAbsoluteId(t);
			if ( t == 0 || ls[t] < minDate ) minDate = ls[t];
			if ( t == 0 || ue[t] > maxDate ) maxDate = ue[t];
             // the task has at least 1 predecessors.
             // scan pred, if the current pred was not fixed (copyAndAggregate(...)) then nbpred[t]++;
             for (int pred=0;pred<predecessors[absoluteT].length;pred++) {
                 relativePred = getRelativeId(predecessors[absoluteT][pred]);
                 if ( relativePred != -1 ) {
                     nbpreds[t]++;
                 }
             }
            if (nbpreds[t] == 0) {
                ring.setNone(t);
                hEvents.add(us[t],t, Event.SCP,-1);
                if ( us[t] < le[t] ) { // has a compulsory part
                    hEvents.add(le[t],t, Event.ECP,-1);
                }
                if ( successors[absoluteT].length != 0 ) {
                    hEvents.add(le[t],t, Event.RS,-1);
                }
                if ( ls[t] < us[t] ) { // not scheduled
                    hEvents.add(ls[t],t, Event.PR,-1);
                } else {
                    ring.setReady(t);
                }
            }
		}
	}


	public boolean sweepMin() throws ContradictionException {
        // ===== LOADS (begin) =====
        nextIndexInterestingTP = 0;
        // ===== LOADS (end) =====
//this.printTasks();
        prunning = false;
        generateMinEvents();
        addAggregatedProfile();
        while (!hEvents.isEmpty()) {
            processEvents();
            filter();
        }
        assert (minProperty());
        return prunning;
	}

    private void addTask(int t) throws ContradictionException {
        if (delta > us[t]) {
            contradiction(null, "earliest task of t"+t+" introduce at "+delta+" (after its latest start)");
        }
        adjustMin(t,delta,delta+ld[t]);
        int absoluteT = getAbsoluteId(t);
        ring.setNone(t);
        if (ls[t] == delta) { // ls of task t is reintroduce at delta. (=> no PR)
            if (ls[t] == us[t]) { // task t is fixed (=> no SCP but directly decrease the gap)
                for (int r=0;r<nbResources;r++) {
                // [COLORS]
                    if ( resourceTypes[r] == COLORED ) {
                        int ctr = c[t][r];
                        if ( ctr != 0 ) { // is not the neutral color
                            if ( countColors[r][ctr] == 0 ) {
                                nbDistinctColors[r]++;
                            }
                            countColors[r][ctr]++;
                        }
                    } else {
                        assert (resourceTypes[r] == CUMULATIVE);
                        gap[r] -= c[t][r];
                    }
                // [COLORS]
                }
                ring.setReady(t);
            } else {
                bufferPR[nbItemsBufferPR] = t; nbItemsBufferPR++;
                hEvents.add(us[t],t, Event.SCP,-1);
            }
            if ( us[t] < le[t] ) { // has a compulsory part
                hEvents.add(le[t],t, Event.ECP,-1);
            }
            if ( successors[absoluteT].length != 0) {
                hEvents.add(le[t],t, Event.RS,-1);
            }
        } else { // ls of task t is reintroduce after delta. (=> SCP)
            hEvents.add(us[t],t, Event.SCP,-1);
            if ( us[t] < le[t] ) { // has a compulsory part
                hEvents.add(le[t],t, Event.ECP,-1);
            }
            if ( ls[t] < us[t] ) { // not scheduled
                hEvents.add(ls[t],t, Event.PR,-1);
            } else {
                ring.setReady(t);
            }
            if ( successors[absoluteT].length != 0) {
                hEvents.add(le[t],t, Event.RS,-1);
            }
        }
    }


    private void processEvents() throws ContradictionException {
        int t, tp, ecpi, rc, absoluteT, relativeSucc, ctr;
        int nbExtractedItems = hEvents.pollAllTopItems(); // result in hEvents.bufferPoll
        int[][] evts = hEvents.bufferPoll; // 0:date,1:task;2:type;3:dec
        nbItemsBufferPR = 0;
        nbItemsBufferRS = 0;
        assert(nbExtractedItems != 0);
        delta = evts[0][0];
        for (int i=0;i<nbExtractedItems;i++) {
            if ( evts[i][2] == Event.SCP ) {
                t = evts[i][1];
                ecpi = le[t];
                if (ring.inConflict(t)) { // = CONFLICT
                    adjustMin(t,us[t],ue[t]);
                    ring.setReady(t);
                } else if (ring.inCheck(t)) {
                    ring.setReady(t);
                }
                if (delta < le[t]) {
                    for (int r=0;r<nbResources;r++) {
                        // [COLORS]
                        if ( resourceTypes[r] == COLORED ) {
                            ctr = c[t][r];
                            if ( ctr != 0 ) { // is not the neutral color
                                if ( countColors[r][ctr] == 0 ) {
                                    nbDistinctColors[r]++;
                                }
                                countColors[r][ctr]++;
                            }
                        } else {
                            assert (resourceTypes[r] == CUMULATIVE);
                            gap[r] -= c[t][r];
                        }
                        // [COLORS]
                    }
                    if (ecpi <= delta) {
                        hEvents.add(le[t],t, Event.ECP,-1);
                    }
                }
            } else if ( evts[i][2] == Event.ECP ) {
                t = evts[i][1];
                if (ring.inConflict(t)) { // = CONFLICT
                    adjustMin(t,us[t],ue[t]);
                    ring.setReady(t);
                }
                if (le[t] > delta) {
                    hEvents.add(le[t],t, Event.ECP,-1);
                } else {
                    if (ring.inCheck(t)) {
                        ring.setReady(t);
                    }
                    for (int r=0;r<nbResources;r++) {
                        // [COLORS]
                        if ( resourceTypes[r] == COLORED ) {
                            ctr = c[t][r];
                            if ( ctr != 0 ) { // is not the neutral color
                                if ( countColors[r][ctr] == 1 ) {
                                    nbDistinctColors[r]--;
                                }
                                countColors[r][ctr]--;
                            }
                        } else {
                            assert (resourceTypes[r] == CUMULATIVE);
                            gap[r] += c[t][r];
                        }
                        // [COLORS]
                    }
                }
            } else if ( evts[i][2] == Event.PR ) { // PR event are processed later
                bufferPR[nbItemsBufferPR] = evts[i][1];
                nbItemsBufferPR++;
            } else if ( evts[i][2] == Event.RS ) {
                bufferRS[nbItemsBufferRS] = evts[i][1];
                nbItemsBufferRS++;
            } else if ( evts[i][2] == Event.AP ) { // <0 == SCP ; >0 == ECP
                assert false; // no AGGREGATE EVENTS for colors !
                for(int r=0;r<nbResources;r++) {
                    // [COLORS]
                    if ( resourceTypes[r] == COLORED ) {
                        ctr = heightsAPEvents[r][evts[i][3]];
                        if ( ctr <  0 ) { // start of fixed task
                            ctr = -ctr;
                            if ( countColors[r][ctr] == 0 ) {
                                nbDistinctColors[r]++;
                            }
                            countColors[r][ctr]++;
                        } else if ( ctr > 0 ) {
                            if ( countColors[r][ctr] == 1 ) {
                                nbDistinctColors[r]--;
                            }
                            countColors[r][ctr]--;
                        } else {
                            System.out.println(" AGGREGATED EVENT : NEUTRAL COLOR (TBD) "); // TODO do not generate such events.
                            assert (false);
                        }
                    } else {
                        assert (resourceTypes[r] == CUMULATIVE);
                        gap[r] += heightsAPEvents[r][evts[i][3]];
                    }
                    // [COLORS]
                }
            }
        }
        for (int i=0;i<nbItemsBufferRS;i++) {
            t = bufferRS[i];
            if ( delta == le[t] && !ring.inConflict(t) ) {
                absoluteT = getAbsoluteId(t);
                for (int s=0;s<successors[absoluteT].length;s++) {
                    relativeSucc = getRelativeId(successors[absoluteT][s]);
                    if (relativeSucc != -1) { // the successor was not fixed during copyAndAggregate(...)
                        nbpreds[relativeSucc]--;
                        assert (nbpreds[relativeSucc] >= 0);
                        if (nbpreds[relativeSucc] == 0) {
                            addTask(relativeSucc);
                        }
                    }
                }
            } else if ( ring.inConflict(t) ) { // t is in conflict
                hEvents.add(delta + ld[t], t, Event.RS, -1); // push the event to first possible date.
            } else {
                hEvents.add(le[t], t, Event.RS, -1); // synchronize the event with the current le[t].
            }

        }
        if (!hEvents.isEmpty()) { date = hEvents.peekDate(); }
        else { date = maxDate; }
        for (int i=0;i<nbItemsBufferPR;i++) { // PR event are processed now
            t = bufferPR[i];
            if ( ( rc = exceedGap(t)) != -1) { // one of the gap is exceeded
                ring.setConflict(t,rc);
            } else if (le[t] > date) {
                ring.setCheck(t);
            } else {
                ring.setReady(t);
            }
        }
    }

    private void filter() throws ContradictionException {
        int r,t,next,rc,ecpi;
        boolean b;
        for (r=0;r<nbResources;r++) {
            // [COLORS]
            if ( resourceTypes[r] == COLORED ) {
                if ( nbDistinctColors[r] > capacities[r] ) {
                    contradiction(null, "overload (colored) in ["+delta+","+date+") on resource r"+r);
                }
            } else {
                assert (resourceTypes[r] == CUMULATIVE);
                if (gap[r]<0) {
                    contradiction(null, "overload (cumulative) in ["+delta+","+date+") on resource r"+r);
                }
            }
            // [COLORS]
        }

        // ===== LOADS (begin) =====
        if ( nextIndexInterestingTP < interestingTimePoints.length ) {
            int iTP = interestingTimePoints[nextIndexInterestingTP];
            if ( iTP < date && iTP >= delta ) { // the interesting time point is in [delta,date)
                do { // update the load variables of the interesting time points in [delta,date)

                    for (int idr=0;idr<interestingResources.length;idr++) {
                        r = interestingResources[idr];
                        if ( resourceTypes[r] == COLORED ) {
                            loads[iTP][r].updateLowerBound(nbDistinctColors[r], aCause);
                        } else {
                            loads[iTP][r].updateLowerBound(capacities[r]-gap[r],aCause);
                        }
                    }

                    nextIndexInterestingTP++;
                    if (nextIndexInterestingTP >= interestingTimePoints.length) break;
                    iTP = interestingTimePoints[nextIndexInterestingTP];
                } while ( iTP < date );
            }
        }
        // ===== LOADS (end) =====

        for (r=0;r<nbResources;r++) {
            // [COLORS]
            if ( resourceTypes[r] == COLORED ) {
                if (nbDistinctColors[r] == capacities[r]) { // capacity is reached
                    next = ring.firstInCheck();
                    while (next != ring.ptrCheck) {
                        t = next;
                        next = ring.next(t);
                        if ( countColors[r][c[t][r]] == 0 ) {
                            if (le[t] > delta) {
                                ring.setConflict(t,r);
                            } else {
                                ring.setReady(t);
                            }
                        }
                    }
                }
            } else {
                assert (resourceTypes[r] == CUMULATIVE);
                if (gapi[r] > gap[r]) { // the gap decreases
                    next = ring.firstInCheck();
                    while (next != ring.ptrCheck) {
                        t = next;
                        next = ring.next(t);
                        if (c[t][r]>gap[r]) {
                            if (le[t] > delta) {
                                ring.setConflict(t,r);
                            } else {
                                ring.setReady(t);
                            }
                        }
                    }
                    gapi[r] = gap[r];
                }
            }
            // [COLORS]
        }
        for (r=0;r<nbResources;r++) {
            // [COLORS]
            if ( resourceTypes[r] == COLORED ) {
                b = nbDistinctColors[r] < capacities[r];
                next = ring.firstInConflict(r);
                while (next != ring.ptrConflict+r) {
                    t = next;
                    next = ring.next(t);
                    if ( b || countColors[r][c[t][r]] != 0 ) {
                        if ((rc=exceedGap(t)) != -1) { // one of the gapi is exceeded.
                            ring.setConflict(t,rc);
                        } else {
                            ecpi = le[t];
                            adjustMin(t,delta,delta+ld[t]);
                            if (le[t]>date) {ring.setCheck(t);} else {ring.setReady(t);}
                            if (us[t]>=ecpi && us[t]<le[t]) {
                                hEvents.add(le[t],t, Event.ECP,-1);
                            }
                        }
                    }
                }
            } else {
                assert (resourceTypes[r] == CUMULATIVE);
                if (gapi[r]<gap[r]) { // the gap increases
                    next = ring.firstInConflict(r);
                    while (next != ring.ptrConflict+r) {
                        t = next;
                        next = ring.next(t);
                        if (c[t][r]<=gap[r]) {
                            if ((rc=exceedGap(t)) != -1) { // one of the gapi is exceeded.
                                ring.setConflict(t,rc);
                            } else {
                                ecpi = le[t];
                                adjustMin(t,delta,delta+ld[t]);
                                if (le[t]>date) {ring.setCheck(t);} else {ring.setReady(t);}
                                if (us[t]>=ecpi && us[t]<le[t]) {
                                    hEvents.add(le[t],t, Event.ECP,-1);
                                }
                            }
                        }
                    }
                    gapi[r] = gap[r];
                }
            }
            // [COLORS]
        }
    }


    // It returns -1 is the task does not exceed any gap.
    // Otherwise, it returns the a resource where it exceeds.
    private int exceedGap(int t) {
        for (int r=0;r<nbResources;r++) {
            if ( resourceTypes[r] == COLORED ) {
                if ( nbDistinctColors[r] == capacities[r] && countColors[r][c[t][r]] == 0 ) return r;
            } else {
                assert (resourceTypes[r] == CUMULATIVE);
                if ( c[t][r] > gap[r] ) return r;
            }
        }
        return -1;
    }


    public boolean isSweepMaxNeeded() {
        int t, tMaxEcp = -1, maxEcp1 = Integer.MIN_VALUE, maxEcp2 = Integer.MIN_VALUE;
        boolean cp;
        int scp, ecp;

		// compute the 2 max values of ecp.
		for(t=0;t<nbTasksInFilteringAlgo;t++) {
            if ( us[t] < le[t] ) {
                cp = true;
                scp = us[t];
                ecp = le[t];
            } else {
                cp = false;
                scp = -1;
                ecp = -1;
            }
			if (cp && ecp >= maxEcp1) {
				maxEcp2 = maxEcp1;
				maxEcp1 = ecp;
				tMaxEcp = t;
			} else if (cp && ecp > maxEcp2) {
				maxEcp2 = ecp;
			}
		}
		if (tMaxEcp == -1) return false; // if no CP, stop saturation !
		for(t=0;t<nbTasksInFilteringAlgo;t++) {
			if ((ls[t] != us[t] && t != tMaxEcp && us[t] < maxEcp1) ||
				(ls[t] != us[t] && t == tMaxEcp && us[t] < maxEcp2)) {
				return true;
			}
		}
		return false;
	}

	private boolean minProperty() {
		int[] sum = new int[nbResources];
        int[][] count = new int[nbResources][];
        int[] nbDistinct = new int[nbResources];
        int t, tp, i, r, absoluteT, relativeSucc;
		for(t=0;t<nbTasksInFilteringAlgo;t++) { // for each task t ...
            absoluteT = getAbsoluteId(t);
            assert (absoluteT != -1);
			for (int succ=0;succ<successors[absoluteT].length;succ++) { // (check precedences relations)
                relativeSucc = getRelativeId(successors[absoluteT][succ]);
                if (relativeSucc != -1 && le[t]>ls[relativeSucc]) return false;
            }
            for(i=ls[t];i<le[t];i++) { // ... scheduled to its earliest position ...
                for (r=0;r<nbResources;r++) {
                    if (resourceTypes[r] == CUMULATIVE) {
                        sum[r] = c[t][r];
                    } else if (resourceTypes[r] == COLORED) {
                        count[r] = new int[nbTasks+1]; // alloc + init to 0
                        if (c[t][r] != 0) {  // // iff this is not the neutral color
                            nbDistinct[r] = 1;
                            count[r][c[t][r]] = 1;
                        }
                    } else {
                        System.out.println(" ! unknown resource type ! ");
                        return false;
                    }
                }
                for(tp=0;tp<nbTasksInFilteringAlgo;tp++) { // ... + the aggregated profile (without t) ...
                    if ((t != tp) && (us[tp] <= i) && (i<le[tp])) {
                        for (r=0;r<nbResources;r++) {
                            if (resourceTypes[r] == CUMULATIVE) {
                                sum[r] += c[tp][r];
                            } else { // COLORED
                                if (c[tp][r] != 0) {
                                    if (count[r][c[t][r]] == 0) {
                                        nbDistinct[r]++;
                                    }
                                    count[r][c[t][r]]++;
                                }
                            }
                        }
                    }
                }
                for (r=0;r<nbResources;r++) { // ... check that the limit is never exceeded
                    if (resourceTypes[r] == CUMULATIVE) {
                        if (sum[r] > capacities[r]) {
                            return false;
                        }
                    } else {
                        if (nbDistinct[r] > capacities[r]) {
                            return false;
                        }
                    }
                }
			}
		}
		return true;
	}

	// ====================================================
	// ====================================================

	public void printEvent(int[] evt) {
		System.out.print("<date="+evt[0]+",task="+evt[1]+",type=");
		switch (evt[2]) {
			case Event.SCP : System.out.print("SCP"); break;
			case Event.ECP : System.out.print("ECP"); break;
			case Event.PR : System.out.print("PR"); break;
			case Event.CCP : System.out.print("CCP"); break;
			case Event.FSCP : System.out.print("FSCP"); break;
			case Event.FECP : System.out.print("FECP"); break;
			case Event.AP : System.out.print("AP"); break;
            case Event.RS : System.out.print("RS"); break;
		}
		System.out.println(",dec="+evt[3]+">");
	}

    public void printTasks() {
        for (int t=0;t<nbTasksInFilteringAlgo;t++) {
            printTask(t);
        }
    }

	public void printTask(int i) {
        int absoluteT = getAbsoluteId(i);
		System.out.print("Task:"+i+"("+absoluteT+"): s:["+ls[i]+".."+us[i]+"] d:["+ld[i]+".."+ld[i]+"] e:["+le[i]+".."+ue[i]+"]");
		if (us[i] < le[i]) System.out.println("| scp:"+us[i]+" ecp:"+le[i]); else System.out.println();;
	}


    }

    class DynamicSweepMaxKDimPrecColRings {

    private final DecHeapEvents hEvents;

    private int delta;
    private int date;

    private final int[] bufferPR;
    private int nbItemsBufferPR;
    private final int[] bufferRS;
    private int nbItemsBufferRS;

    private boolean prunning;

	private final int[] nbDistinctColors; // [RESOURCE] # of distinct colours on the given resource
    private final int[][] countColors; // [RESOURCE][COLOR] give the number of tasks using a given color on a given resource.

    private final int[] gap;
    private final int[] gapi;
    private final int[] nbsuccs;

	private int maxDate;
	private int minDate;

    // h[task][resource]
	public DynamicSweepMaxKDimPrecColRings() {
		this.hEvents = new DecHeapEvents(4*nbTasksInFilteringAlgo+2*(nbTasks-nbTasksInFilteringAlgo));
        this.gap = new int[nbResources];
        this.gapi = new int[nbResources];
        this.nbsuccs = new int[nbTasksInFilteringAlgo];
        // [COLORS]
        this.nbDistinctColors = new int[nbResources];
        this.countColors = new int[nbResources][];
        for (int r=0;r<nbResources;r++) {
            if ( resourceTypes[r] == COLORED ) {
                this.countColors[r] = new int[nbTasks+1]; // neutral color
            } else {
                assert (resourceTypes[r] == CUMULATIVE);
                this.countColors[r] = null;
            }
        }
        // [COLORS]
        this.bufferPR = new int[nbTasksInFilteringAlgo];
        this.nbItemsBufferPR = 0;
        this.bufferRS = new int[nbTasksInFilteringAlgo];
        this.nbItemsBufferRS = 0;
        this.prunning = false;
	}

    public void addAggregatedProfile() {
		for(int i=0;i<nbEventsToAdd;i++) {
			hEvents.add(datesAPEvents[i]-1, -1, Event.AP, i);
			if (datesAPEvents[i] < this.minDate) minDate = datesAPEvents[i];
			if (datesAPEvents[i] > this.maxDate) maxDate = datesAPEvents[i];
		}
	}

    public void adjustMax(int t, int maxStart, int maxEnd) {
        assert(maxStart < maxEnd);
        assert(maxStart + ld[t] == maxEnd);
        if ( maxEnd < ue[t] ) {
            prunning = true;
            us[t] = maxStart;
            ue[t] = maxEnd;
        }
    }

    protected void generateMaxEvents() {
        this.hEvents.clear();
        for (int r=0;r<nbResources;r++) {
            // [COLORS]
            if ( resourceTypes[r] == COLORED ) {
                assert (countColors.length >= 1); // at least the neutral color.
                countColors[r][0] = 1; // avoid the handle the specific case of the neutral color during the sweep.
                nbDistinctColors[r] = 0;
                for (int i=1;i<countColors[r].length;i++) {
                    countColors[r][i] = 0;
                }
            } else {
                assert (resourceTypes[r] == CUMULATIVE);
                gap[r] = capacities[r];
                gapi[r] = capacities[r];
            }
            // [COLORS]
        }
        int absoluteT, relativeSucc;
		for(int t=0;t<nbTasksInFilteringAlgo;t++) {
            absoluteT = getAbsoluteId(t);
			if ( t == 0 || ls[t] < minDate ) minDate = ls[t];
			if ( t == 0 || ue[t] > maxDate ) maxDate = ue[t];
            for (int succ=0;succ<successors[absoluteT].length;succ++) {
                relativeSucc = getRelativeId(successors[absoluteT][succ]);
                if ( relativeSucc != -1 ) {
                    nbsuccs[t]++;
                }
            }
            if (nbsuccs[t] == 0) {
                ring.setNone(t);
                hEvents.add(le[t]-1,t, Event.SCP,-1);
                if ( us[t] < le[t] ) { // has a compulsory part
                    hEvents.add(us[t]-1,t, Event.ECP,-1);
                }
                if ( predecessors[absoluteT].length != 0 ) {
                    hEvents.add(us[t]-1,t, Event.RS,-1);
                }
                if ( ls[t] < us[t] ) { // not scheduled
                    hEvents.add(ue[t]-1,t, Event.PR,-1);
                } else {
                    ring.setReady(t);
                }
            }
		}
	}

    public boolean sweepMax() throws ContradictionException {
        // ===== LOADS (begin) =====
        nextIndexInterestingTP = interestingTimePoints.length-1;
        // ===== LOADS (end) =====
        prunning = false;
        generateMaxEvents();
        addAggregatedProfile();
        while (!hEvents.isEmpty()) {
            processEvents();
            filter();
        }
        assert (maxProperty());
        return prunning;
	}

    private void addTask(int t) throws ContradictionException {
        if (delta < le[t]-1 ) {
            contradiction(null, "latest end of task t"+t+" introduce at "+delta+ "(+1) (before its earliest end)");
        }
        adjustMax(t,delta-ld[t]+1,delta+1);
        int absoluteT = getAbsoluteId(t);
        ring.setNone(t);
        if (ue[t] == delta+1) { // ue of task t is reintroduce at delta+1. (=> no PR)
            if (ls[t] == us[t]) { // task t is fixed (=> no SCP but directly decrease the gap)
                for (int r=0;r<nbResources;r++) {
                    // [COLORS]
                    if ( resourceTypes[r] == COLORED ) {
                        int ctr = c[t][r];
                        if ( ctr != 0 ) { // is not the neutral color
                            if ( countColors[r][ctr] == 0 ) {
                                nbDistinctColors[r]++;
                            }
                            countColors[r][ctr]++;
                        }
                    } else {
                        assert (resourceTypes[r] == CUMULATIVE);
                        gap[r] -= c[t][r];
                    }
                    // [COLORS]
                }
                ring.setReady(t);
            } else { // add the PR event in the buffer (because it needs to be processed now)
                /*hEvents.add(ls[t],t,Event.PR,-1);*/  bufferPR[nbItemsBufferPR] = t; nbItemsBufferPR++;
                hEvents.add(le[t]-1,t, Event.SCP,-1);
            }
            if ( us[t] < le[t] ) { // has a compulsory part
                hEvents.add(us[t]-1,t, Event.ECP,-1);
            }
            if ( successors[absoluteT].length != 0) {
                hEvents.add(us[t]-1,t, Event.RS,-1);
            }
        } else { // ls of task t is reintroduce after delta. (=> SCP)
            hEvents.add(le[t]-1,t, Event.SCP,-1);
            if ( us[t] < le[t] ) { // has a compulsory part
                hEvents.add(us[t]-1,t, Event.ECP,-1);
            }
            if ( ls[t] < us[t] ) { // not scheduled
                hEvents.add(ue[t]-1,t, Event.PR,-1);
            } else {
                ring.setReady(t);
            }
            if ( successors[absoluteT].length != 0) {
                hEvents.add(us[t]-1,t, Event.RS,-1);
            }
        }
    }

    private void processEvents() throws ContradictionException {
        int t, tp, ecpi, rc, absoluteT, relativePred, ctr;
        int nbExtractedItems = hEvents.pollAllTopItems(); // result in hEvents.bufferPoll
        int[][] evts = hEvents.bufferPoll; // 0:date,1:task;2:type;3:dec
        nbItemsBufferPR = 0;
        nbItemsBufferRS = 0;
        assert(nbExtractedItems != 0);
        delta = evts[0][0];
        for (int i=0;i<nbExtractedItems;i++) {
//System.out.print("Event: "); printEvent(evts[i]);
            if ( evts[i][2] == Event.SCP ) {
                t = evts[i][1];
                ecpi = us[t]-1;
                if (ring.inConflict(t)) { // = CONFLICT
                    adjustMax(t,ls[t],le[t]);
                    ring.setReady(t);
                } else if (ring.inCheck(t)) {
                    ring.setReady(t);
                }
                if (delta > us[t]-1) {
                    for (int r=0;r<nbResources;r++) {
                        // [COLORS]
                        if ( resourceTypes[r] == COLORED ) {
                            ctr = c[t][r];
                            if ( ctr != 0 ) { // is not the neutral color
                                if ( countColors[r][ctr] == 0 ) {
                                    nbDistinctColors[r]++;
                                }
                                countColors[r][ctr]++;
                            }
                        } else {
                            assert (resourceTypes[r] == CUMULATIVE);
                            gap[r] -= c[t][r];
                        }
                        // [COLORS]
                    }
                    if (ecpi >= delta) {
                        hEvents.add(us[t]-1,t, Event.ECP,-1);
                    }
                }
            } else if ( evts[i][2] == Event.ECP ) {
                t = evts[i][1];
                if (ring.inConflict(t)) { // = CONFLICT
                    adjustMax(t,ls[t],le[t]);
                    ring.setReady(t);
                }
                if (us[t]-1 < delta) {
                    hEvents.add(us[t]-1,t, Event.ECP,-1);
                } else {
                    if (ring.inCheck(t)) {
                        ring.setReady(t);
                    }
                    for (int r=0;r<nbResources;r++) {
                        // [COLORS]
                        if ( resourceTypes[r] == COLORED ) {
                            ctr = c[t][r];
                            if ( ctr != 0 ) { // is not the neutral color
                                if ( countColors[r][ctr] == 1 ) {
                                    nbDistinctColors[r]--;
                                }
                                countColors[r][ctr]--;
                            }
                        } else {
                            assert (resourceTypes[r] == CUMULATIVE);
                            gap[r] += c[t][r];
                        }
                        // [COLORS]
                    }
                }
            } else if ( evts[i][2] == Event.PR ) { // PR event are processed later
                bufferPR[nbItemsBufferPR] = evts[i][1];
                nbItemsBufferPR++;
            } else if (evts[i][2] == Event.RS) {
                bufferRS[nbItemsBufferRS] = evts[i][1];
                nbItemsBufferRS++;
            } else if ( evts[i][2] == Event.AP ) {
                for(int r=0;r<nbResources;r++) {
                    //gap[k] -= heightsAPEvents[k][evts[i][3]];
                    // [COLORS]
                    if ( resourceTypes[r] == COLORED ) {
                        ctr = heightsAPEvents[r][evts[i][3]];
                        if ( ctr >  0 ) { // start of fixed task
                            if ( countColors[r][ctr] == 0 ) {
                                nbDistinctColors[r]++;
                            }
                            countColors[r][ctr]++;
                        } else if ( ctr < 0 ) {
                            ctr = -ctr;
                            if ( countColors[r][ctr] == 1 ) {
                                nbDistinctColors[r]--;
                            }
                            countColors[r][ctr]--;
                        } else {
                            System.out.println(" AGGREGATED EVENT : NEUTRAL COLOR (TBD) "); // TODO do not generate such events.
                        }
                    } else {
                        assert (resourceTypes[r] == CUMULATIVE);
                        gap[r] -= heightsAPEvents[r][evts[i][3]];
                    }
                    // [COLORS]
                }
            }
        }
        for (int i=0;i<nbItemsBufferRS;i++) {
            t = bufferRS[i];
            if ( delta == us[t]-1 && !ring.inConflict(t) ) { // (SYNC && !CONFLICT) RS is at its final position
                absoluteT = getAbsoluteId(t);
                for (int p=0;p<predecessors[absoluteT].length;p++) {
                    relativePred = getRelativeId(predecessors[absoluteT][p]);
                    if ( relativePred != -1 ) {
                        nbsuccs[relativePred]--;
                        if (nbsuccs[relativePred] == 0) {
                            addTask(relativePred);
                        }
                    }
                }
            } else if ( ring.inConflict(t) /*delta == us[t]-1*/ ) { // ( (SYNC || !SYNC) && !CONFLICT) )
                hEvents.add(delta - ld[t], t, Event.RS, -1);
            } else {
                hEvents.add(us[t]-1, t, Event.RS, -1);
            }
        }
        if (!hEvents.isEmpty()) { date = hEvents.peekDate(); }
        else { date = minDate; }
        for (int i=0;i<nbItemsBufferPR;i++) { // PR event are processed now
            t = bufferPR[i];
            if ( ( rc = exceedGap(t)) != -1) { // one of the gap is exceeded
                ring.setConflict(t,rc);
            } else if (us[t]-1 < date) {
                ring.setCheck(t);
            } else {
                ring.setReady(t);
            }
        }
    }

    private void filter() throws ContradictionException {
        int r,t,next,rc,ecpi;
        boolean b;
        for (r=0;r<nbResources;r++) {
            // [COLORS]
            if ( resourceTypes[r] == COLORED ) {
                if ( nbDistinctColors[r] > capacities[r] ) {
                    contradiction(null, "overload (colored) in ["+delta+","+date+") on resource r"+r);
                }
            } else {
                assert (resourceTypes[r] == CUMULATIVE);
                if (gap[r]<0) {
                    contradiction(null, "overload (cumulative) in ["+delta+","+date+") on resource r"+r);
                }
            }
            // [COLORS]
        }

         // ===== LOADS (begin) =====
        if (nextIndexInterestingTP >= 0 ) {
            int iTP = interestingTimePoints[nextIndexInterestingTP];
            if ( iTP > date && iTP <= delta) { // the interesting time point is in [delta,date)
                do { // update the load variables of the interesting time points in (date,delta]

                    for (int idr=0;idr<interestingResources.length;idr++) {
                        r = interestingResources[idr];
                        if ( resourceTypes[r] == COLORED ) {
                            loads[iTP][r].updateLowerBound(nbDistinctColors[r],aCause);
                        } else {
                            loads[iTP][r].updateLowerBound(capacities[r]-gap[r],aCause);
                        }
                    }

                    nextIndexInterestingTP--;
                    if (nextIndexInterestingTP < 0) break;
                    iTP = interestingTimePoints[nextIndexInterestingTP];
                } while ( iTP < date );
            }
        }
        // ===== LOADS (end) =====

        for (r=0;r<nbResources;r++) {
            // [COLORS]
            if ( resourceTypes[r] == COLORED ) {
                if (nbDistinctColors[r] == capacities[r]) { // capacity is reached
                    next = ring.firstInCheck();
                    while (next != ring.ptrCheck) {
                        t = next;
                        next = ring.next(t);
                        if ( countColors[r][c[t][r]] == 0 ) {
                            if (us[t]-1 < delta) {
                                ring.setConflict(t,r);
                            } else {
                                ring.setReady(t);
                            }
                        }
                    }
                }
            } else {
                // TODO HERE
                assert (resourceTypes[r] == CUMULATIVE);
                if (gapi[r] > gap[r]) { // the gap decreases
                    next = ring.firstInCheck();
                    while (next != ring.ptrCheck) {
                        t = next;
                        next = ring.next(t);
                        if (c[t][r]>gap[r]) {
                            if (us[t]-1 < delta) {
                                ring.setConflict(t,r);
                            } else {
                                ring.setReady(t);
                            }
                        }
                    }
                    gapi[r] = gap[r];
                }
            }
            // [COLORS]
        }
        for (r=0;r<nbResources;r++) {
            // [COLORS]
            if ( resourceTypes[r] == COLORED ) {
                b = nbDistinctColors[r] < capacities[r];
                next = ring.firstInConflict(r);
                while (next != ring.ptrConflict+r) {
                    t = next;
                    next = ring.next(t);
                    if ( b || countColors[r][c[t][r]] != 0 ) {
                        if ((rc=exceedGap(t)) != -1) { // one of the gapi is exceeded.
                            ring.setConflict(t,rc);
                        } else {
                            ecpi = us[t]-1;
                            adjustMax(t,delta-ld[t]+1,delta+1);
                            if (us[t]-1<date) {ring.setCheck(t);} else {ring.setReady(t);}
                            if (le[t]-1<=ecpi && us[t]<le[t]) {
                                hEvents.add(us[t]-1,t, Event.ECP,-1);
                            }
                        }
                    }
                }
            } else {
                assert (resourceTypes[r] == CUMULATIVE);
                if (gapi[r]<gap[r]) {
                    next = ring.firstInConflict(r);
                    while (next != ring.ptrConflict+r) {
                        t = next;
                        next = ring.next(t);
                        if (c[t][r]<=gap[r]) {
                            if ((rc=exceedGap(t)) != -1) { // one of the gapi is exceeded.
                                ring.setConflict(t,rc);
                            } else {
                                ecpi = us[t]-1;
                                adjustMax(t,delta-ld[t]+1,delta+1);
                                if (us[t]-1<date) {ring.setCheck(t);} else {ring.setReady(t);}
                                if (le[t]-1<=ecpi && us[t]<le[t]) {
                                    hEvents.add(us[t]-1,t, Event.ECP,-1);
                                }
                            }
                        }
                    }
                    gapi[r] = gap[r];
                }
            }
            // [COLORS]
        }
    }

    // It returns -1 is the task does not exceed any gap.
    // Otherwise, it returns the a resource where it exceeds.
    private int exceedGap(int t) {
        for (int r=0;r<nbResources;r++) {
            if ( resourceTypes[r] == COLORED ) {
                if ( nbDistinctColors[r] == capacities[r] && countColors[r][c[t][r]] == 0 ) return r;
            } else {
                assert (resourceTypes[r] == CUMULATIVE);
                if ( c[t][r] > gap[r] ) return r;
            }
        }
        return -1;
    }


	private boolean maxProperty() {
////        System.out.println("    END");
////        for(int i=0;i<n;i++) printTask(i);
//		int[] sum = new int[nbResources];
//        int t, tp, i, r, len, absoluteT, relativePred;
//		for(t=0;t<nbTasksInFilteringAlgo;t++) { // for each task t ...
//            absoluteT = getAbsoluteId(t);
//            for (int pred=0;pred<predecessors[absoluteT].length;pred++) { // (check precedences relations)
//                relativePred = getRelativeId(predecessors[absoluteT][pred]);
//                if (relativePred != -1 && us[t]<ue[relativePred]) return false;
//            }
//			for(i=us[t];i<ue[t];i++) { // ... scheduled to its latest position
//                for (r=0;r<nbResources;r++) {
//                    sum[r] = c[t][r];
//                }
//                for(tp=0;tp<nbTasksInFilteringAlgo;tp++) { // compute the
//                    if ((t != tp) && (us[tp] <= i) && (i<le[tp])) {
//                        for (r=0;r<nbResources;r++) {
//                            sum[r] += c[tp][r];
//                        }
//                    }
//                }
//                for (r=0;r<nbResources;r++) {
//                    if (sum[r] > capacities[r]) {
//                        return false;
//                    }
//                }
//			}
//		}
//		return true;
        int[] sum = new int[nbResources];
        int[][] count = new int[nbResources][];
        int[] nbDistinct = new int[nbResources];
        int t, tp, i, r, absoluteT, relativePred;
		for(t=0;t<nbTasksInFilteringAlgo;t++) { // for each task t ...
            absoluteT = getAbsoluteId(t);
            assert (absoluteT != -1);
			for (int pred=0;pred<successors[absoluteT].length;pred++) { // (check precedences relations)
                relativePred = getRelativeId(successors[absoluteT][pred]);
                if (relativePred != -1 && us[t]<ue[relativePred]) return false;
            }
            for(i=us[t];i<ue[t];i++) { // ... scheduled to its earliest position ...
                for (r=0;r<nbResources;r++) {
                    if (resourceTypes[r] == CUMULATIVE) {
                        sum[r] = c[t][r];
                    } else if (resourceTypes[r] == COLORED) {
                        count[r] = new int[nbTasks+1]; // alloc + init to 0
                        if (c[t][r] != 0) {  // // iff this is not the neutral color
                            nbDistinct[r] = 1;
                            count[r][c[t][r]] = 1;
                        }
                    } else {
                        System.out.println(" ! unknown resource type ! ");
                        return false;
                    }
                }
                for(tp=0;tp<nbTasksInFilteringAlgo;tp++) { // ... + the aggregated profile (without t) ...
                    if ((t != tp) && (us[tp] <= i) && (i<le[tp])) {
                        for (r=0;r<nbResources;r++) {
                            if (resourceTypes[r] == CUMULATIVE) {
                                sum[r] += c[tp][r];
                            } else { // COLORED
                                if (c[tp][r] != 0) {
                                    if (count[r][c[t][r]] == 0) {
                                        nbDistinct[r]++;
                                    }
                                    count[r][c[t][r]]++;
                                }
                            }
                        }
                    }
                }
                for (r=0;r<nbResources;r++) { // ... check that the limit is never exceeded
                    if (resourceTypes[r] == CUMULATIVE) {
                        if (sum[r] > capacities[r]) {
                            return false;
                        }
                    } else {
                        if (nbDistinct[r] > capacities[r]) {
                            return false;
                        }
                    }
                }
			}
		}
		return true;
	}



	public void printEvent(int[] evt) {
		System.out.print("<date="+evt[0]+",task="+evt[1]+",type=");
		switch (evt[2]) {
			case Event.SCP : System.out.print("SCP"); break;
			case Event.ECP : System.out.print("ECP"); break;
			case Event.PR : System.out.print("PR"); break;
			case Event.CCP : System.out.print("CCP"); break;
			case Event.FSCP : System.out.print("FSCP"); break;
			case Event.FECP : System.out.print("FECP"); break;
			case Event.AP : System.out.print("AP"); break;
            case Event.RS : System.out.print("RS"); break;
		}
		System.out.println(",dec="+evt[3]+">");
	}

    public void printTasks() {
        for (int t=0;t<nbTasksInFilteringAlgo;t++) {
            printTask(t);
        }
    }

	public void printTask(int i) {
		System.out.print("Task:"+i+" : s:["+ls[i]+".."+us[i]+"] d:["+ld[i]+".."+ld[i]+"] e:["+le[i]+".."+ue[i]+"] h["+i+"][0]="+c[i][0]+" h["+i+"][1]="+c[i][1]+" ");
		if (us[i] < le[i]) System.out.println("| scp:"+us[i]+" ecp:"+le[i]); else System.out.println();;
	}

    }

    /**
     * Greedy Mode
     */
    class DynamicSweepGreedyKDimPrecColRings {

        private final Rings ringGreedy;

        private final int[] lsGreedy; // earliest start
        private final int[] usGreedy; // latest start
        private final int[] leGreedy; // earliest end
        private final int[] ueGreedy; // latest end

        private final int[] nbpreds;
        private final BitSet isFECPRead;

        private final IncHeapEvents hEvents;

        private int delta;
        private int date;
        private final BitSet toBeFixed;
        private final Trail trail;

        private final int[] bufferPR;
        private int nbItemsBufferPR;

        private final int[] nbDistinctColors; // [RESOURCE] # of distinct colours on the given resource
        private final int[][] countColors; // [RESOURCE][COLOR] give the number of tasks using a given color on a given resource.
        private final int[] gap;
        private final int[] gapi;


        //private int[][] heightsAPEvents;


        private int maxDate;
        private int minDate;


        // h[task][resource]
        public DynamicSweepGreedyKDimPrecColRings() {
            this.hEvents = new IncHeapEvents(4*nbTasksInFilteringAlgo+2*(nbTasks-nbTasksInFilteringAlgo)); // TODO check the size
            this.lsGreedy = ls.clone();
            this.usGreedy = us.clone();
            this.leGreedy = le.clone();
            this.ueGreedy = ue.clone();
            //this.hGreedy = h;
            //this.ldGreedy = ld;
            this.nbpreds = new int[nbTasksInFilteringAlgo];
            this.isFECPRead = new BitSet(nbTasksInFilteringAlgo);
            this.gap = new int[nbResources];
            this.gapi = new int[nbResources];
            // [COLORS]
            this.nbDistinctColors = new int[nbResources];
            this.countColors = new int[nbResources][];
            for (int r=0;r<nbResources;r++) {
                if ( resourceTypes[r] == COLORED ) {
                    this.countColors[r] = new int[nbTasks+1]; // neutral color
                } else {
                    assert (resourceTypes[r] == CUMULATIVE);
                    this.countColors[r] = null;
                }
            }
            // [COLORS]
            this.toBeFixed = new BitSet(nbTasksInFilteringAlgo);
            this.trail = new Trail((int) Math.pow(nbTasksInFilteringAlgo,1.5) + 5*nbTasksInFilteringAlgo*nbResources); // TODO find a 'good' value.
            this.ringGreedy = new Rings(nbResources,nbTasksInFilteringAlgo);
            this.bufferPR = new int[nbTasksInFilteringAlgo];
            this.nbItemsBufferPR = 0;
        }

        public void addAggregatedProfile() {
            for(int i=0;i<nbEventsToAdd;i++) {
                hEvents.add(datesAPEvents[i], -1, Event.AP, i);
                if (datesAPEvents[i] < this.minDate) minDate = datesAPEvents[i];
                if (datesAPEvents[i] > this.maxDate) maxDate = datesAPEvents[i];
            }
        }

        public void adjustMin(int t, int minStart, int minEnd) {
    //System.out.println(" CALL adjust_min(t="+t+", ls="+minStart+")");
            assert(minStart < minEnd);
            lsGreedy[t] = minStart;
            leGreedy[t] = minEnd;
        }

        public void adjustMax(int t, int maxStart, int maxEnd) {
    //System.out.println(" CALL adjust_max(t="+t+", us="+maxStart+")");
            assert(maxStart < maxEnd);
            usGreedy[t] = maxStart;
            ueGreedy[t] = maxEnd;
        }

        protected void generateGreedyEvents() {
            this.hEvents.clear();
            this.toBeFixed.clear(); // TODO del
            this.isFECPRead.clear(); // TODO del
            for (int r=0;r<nbResources;r++) {
                // [COLORS]
                if ( resourceTypes[r] == COLORED ) {
                    assert (countColors.length >= 1); // at least the neutral color.
                    countColors[r][0] = 1; // avoid the handle the specific case of the neutral color during the sweep.
                    nbDistinctColors[r] = 0;
                    for (int i=1;i<countColors[r].length;i++) {
                        countColors[r][i] = 0;
                    }
                } else {
                    assert (resourceTypes[r] == CUMULATIVE);
                    gap[r] = capacities[r];
                    gapi[r] = capacities[r];
                }
                // [COLORS]
            }
            int absoluteT, relativePred;
            for(int t=0;t<nbTasksInFilteringAlgo;t++) {
                absoluteT = getAbsoluteId(t);
                if ( t == 0 || lsGreedy[t] < minDate ) minDate = lsGreedy[t];
                if ( t == 0 || ueGreedy[t] > maxDate ) maxDate = ueGreedy[t];
                // the task has at least 1 predecessors.
                // scan pred, if the current pred was not fixed (copyAndAggregate(...)) then nbpred[t]++;
                for (int pred=0;pred<predecessors[absoluteT].length;pred++) {
                     relativePred = getRelativeId(predecessors[absoluteT][pred]);
                     if ( relativePred != -1 ) {
                         nbpreds[t]++;
                     }
                }
                if ( nbpreds[t] == 0 ) { // iff no predecessor, t is added in the process
                    ringGreedy.setNone(t);
                    if (lsGreedy[t] == usGreedy[t] && ld[t] > 0) {
                        hEvents.add(usGreedy[t],t, Event.FSCP,-1);
                        hEvents.add(leGreedy[t],t, Event.FECP,-1);
                    } else if ( ld[t] > 0 ) {
                        toBeFixed.set(t,true);
                        hEvents.add(lsGreedy[t],t, Event.PR,-1);
                        hEvents.add(usGreedy[t],t, Event.SCP,-1);
                        if ( usGreedy[t] < leGreedy[t] ) {
                            hEvents.add(leGreedy[t],t, Event.ECP,-1);
                        }
                    } else { // zero duration -- can fix right now
                        adjustMax(t,lsGreedy[t],leGreedy[t]);
                    }
                }
            }
        }

        public boolean greedy() {
            generateGreedyEvents();
            addAggregatedProfile();
            this.delta = minDate;
            trail.push(Trail.DELTA,minDate-1);
            while ( !hEvents.isEmpty() || !ringGreedy.isEmptyReady()  ) {
                if ( !ringGreedy.isEmptyReady() ) {
                    greedyAssign();
                } else {
                    if (!greedyPhase1()) { return false; }
                }
                if ( !greedyPhase2() ) {
                    return false;
                }
                trail.push(Trail.DELTA,delta);
            }
//            assert (greedyProperty());
            return true;
        }

        // ?
        private boolean greedyProperty() {   // TODO new property due to colors
            if ( !toBeFixed.isEmpty() ) {
                return false;
            }
            int[] sum = new int[nbResources];
            for (int t=0;t<nbTasksInFilteringAlgo;t++) {
                if ( lsGreedy[t] < leGreedy[t] ) {
                    for (int r=0;r<nbResources;r++) {
                        sum[r] = c[t][r];
                    }
                    for (int tp=0;tp<nbTasksInFilteringAlgo;tp++) {
                        if ( tp != t && usGreedy[tp] <= lsGreedy[t] && lsGreedy[t] < leGreedy[tp]) {
                            for (int r=0;r<nbResources;r++) {
                                sum[r] += c[tp][r];
                            }
                        }
                    }
                    for (int r=0;r<nbResources;r++) {
                        if ( sum[r] > capacities[r] ) { return false; }
                    }
                }
            }
            return true;
        }

        // OK
        private boolean greedyPhase1() {
    //System.out.println(" CALL greedy_phase1()");
            int t, ecpi, rc, ctr;
            int nbExtractedItems = hEvents.pollAllTopItems(); // result in hEvents.bufferPoll
            int[][] evts = hEvents.bufferPoll; // 0:date,1:task;2:type;3:dec
            nbItemsBufferPR = 0;
            assert(nbExtractedItems != 0);
            delta = evts[0][0];
            for (int i=0;i<nbExtractedItems;i++) {
    //printEvent(evts[i]);
                if ( evts[i][2] == Event.FSCP ) {
                    t = evts[i][1];
                    trail.push(Trail.FSCP,t);
                    for (int r=0;r<nbResources;r++) {
                        // [COLORS]
                        if ( resourceTypes[r] == COLORED ) {
                            ctr = c[t][r];
                            if ( ctr != 0 ) { // is not the neutral color
                                if ( countColors[r][ctr] == 0 ) {
                                    nbDistinctColors[r]++;
                                }
                                countColors[r][ctr]++;
                            }
                        } else {
                            assert (resourceTypes[r] == CUMULATIVE);
                            gap[r] -= c[t][r];
                        }
                        // [COLORS]
                    }
                } else if ( evts[i][2] == Event.FECP ) {
                    t = evts[i][1];
                    trail.push(Trail.FECP,t);
                    for (int r=0;r<nbResources;r++) {
                        // [COLORS]
                        if ( resourceTypes[r] == COLORED ) {
                            ctr = c[t][r];
                            if ( ctr != 0 ) { // is not the neutral color
                                if ( countColors[r][ctr] == 1 ) {
                                    nbDistinctColors[r]--;
                                }
                                countColors[r][ctr]--;
                            }
                        } else {
                            assert (resourceTypes[r] == CUMULATIVE);
                            gap[r] += c[t][r];
                        }
                        // [COLORS]
                    }
                    // -------------------------
                    // MODIF FOR PRECEDENCES
                    int relativeSucc, absoluteT;
                    absoluteT = getAbsoluteId(t);
                    if ( isFECPRead.get(t) == false ) { // never read before
                        isFECPRead.set(t,true);
                        for (int succ=0;succ<successors[absoluteT].length;succ++) {
                            relativeSucc = getRelativeId(successors[absoluteT][succ]);
                            if ( relativeSucc != -1 ) {
                                nbpreds[relativeSucc]--;
                                assert (nbpreds[relativeSucc] >= 0);
                                if ( lsGreedy[relativeSucc] < delta ) { // update the lower bounds.
                                    if ( delta > usGreedy[relativeSucc] ) { return false; }
                                    lsGreedy[relativeSucc] = delta;
                                    leGreedy[relativeSucc] = delta+ld[relativeSucc];
                                }
                                if ( nbpreds[relativeSucc] == 0 ) { // we can now add the task in the process.
                                    ringGreedy.setNone(relativeSucc);
                                    if ( lsGreedy[relativeSucc] == delta ) { // the PR can be added at the current sweep-line position
                                        if ( lsGreedy[relativeSucc] == usGreedy[relativeSucc] ) { // task relativeSucc is fixed (=> no SCP but directly decrease the gap)
                                            toBeFixed.set(relativeSucc,false);
                                            trail.push(Trail.FSCP,relativeSucc);
                                            for (int r=0;r<nbResources;r++) {
                                                // [COLORS]
                                                if ( resourceTypes[r] == COLORED ) {
                                                    ctr = c[relativeSucc][r];
                                                    if ( ctr != 0 ) { // is not the neutral color
                                                        if ( countColors[r][ctr] == 0 ) {
                                                            nbDistinctColors[r]++;
                                                        }
                                                        countColors[r][ctr]++;
                                                    }
                                                } else {
                                                    assert (resourceTypes[r] == CUMULATIVE);
                                                    gap[r] -= c[relativeSucc][r];
                                                }
                                                // [COLORS]
                                            }
                                            hEvents.add(leGreedy[relativeSucc],relativeSucc, Event.FECP,-1);
                                        } else { // task is not yet fixed
                                            toBeFixed.set(relativeSucc,true);
                                            bufferPR[nbItemsBufferPR] = relativeSucc; nbItemsBufferPR++;
                                            hEvents.add(usGreedy[relativeSucc],relativeSucc, Event.SCP,-1);
                                            if ( usGreedy[relativeSucc] < leGreedy[relativeSucc] ) { // has a compulsory part
                                                hEvents.add(leGreedy[relativeSucc],relativeSucc, Event.ECP,-1);
                                            }
                                        }
                                    } else { // ls of task t is reintroduce after delta. (=> SCP)
                                        assert ( lsGreedy[relativeSucc]>delta );
                                        if ( lsGreedy[relativeSucc] == usGreedy[relativeSucc] ) { // task relativeSucc is fixed (=> no SCP but directly decrease the gap)
                                            toBeFixed.set(relativeSucc,false);
                                            hEvents.add(lsGreedy[relativeSucc],relativeSucc, Event.FSCP,-1);
                                            hEvents.add(leGreedy[relativeSucc],relativeSucc, Event.FECP,-1);
                                        } else { // task is not yet fixed
                                            toBeFixed.set(relativeSucc,true);
                                            hEvents.add(lsGreedy[relativeSucc],relativeSucc, Event.PR,-1);
                                            hEvents.add(usGreedy[relativeSucc],relativeSucc, Event.SCP,-1);
                                            if ( usGreedy[relativeSucc] < leGreedy[relativeSucc] ) { // has a compulsory part
                                                hEvents.add(leGreedy[relativeSucc],relativeSucc, Event.ECP,-1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // END MODIF FOR PRECEDENCES
                    // -------------------------
                } else if ( evts[i][2] == Event.SCP && toBeFixed.get(evts[i][1]) == true ) {
                    t = evts[i][1];
                    ecpi = leGreedy[t];
                    if (ringGreedy.inConflict(t)) { // = CONFLICT
                        adjustMin(t,usGreedy[t],ueGreedy[t]);
                        if ( greedyFix(t) ) { continue; }
                    } else if (ringGreedy.inCheck(t)) {
                        greedyFix(t);
                    }
                    trail.push(Trail.SCP,t);
                    if (delta < leGreedy[t]) {
                        for (int r=0;r<nbResources;r++) {
                            // [COLORS]
                            if ( resourceTypes[r] == COLORED ) {
                                ctr = c[t][r];
                                if ( ctr != 0 ) { // is not the neutral color
                                    if ( countColors[r][ctr] == 0 ) {
                                        nbDistinctColors[r]++;
                                    }
                                    countColors[r][ctr]++;
                                }
                            } else {
                                assert (resourceTypes[r] == CUMULATIVE);
                                gap[r] -= c[t][r];
                            }
                            // [COLORS]
                        }
                        if (ecpi <= delta) {
                            hEvents.add(leGreedy[t],t, Event.ECP,-1);
                        }
                    }
                } else if ( evts[i][2] == Event.ECP && toBeFixed.get(evts[i][1]) == true ) {
                    t = evts[i][1];
                    if (ringGreedy.inConflict(t)) { // = CONFLICT
                        adjustMin(t,usGreedy[t],ueGreedy[t]);
                        if ( greedyFix(t) ) { continue; } // BUG HERE
                    }
                    if (leGreedy[t] > delta) {
                        hEvents.add(leGreedy[t],t, Event.ECP,-1);
                    } else {
                        trail.push(Trail.ECP,t);
                        if (ringGreedy.inCheck(t)) {
                            greedyFix(t);
                        }
                        for (int r=0;r<nbResources;r++) {
                            // [COLORS]
                            if ( resourceTypes[r] == COLORED ) {
                                ctr = c[t][r];
                                if ( ctr != 0 ) { // is not the neutral color
                                    if ( countColors[r][ctr] == 1 ) {
                                        nbDistinctColors[r]--;
                                    }
                                    countColors[r][ctr]--;
                                }
                            } else {
                                assert (resourceTypes[r] == CUMULATIVE);
                                gap[r] += c[t][r];
                            }
                            // [COLORS]
                        }
                    }
                } else if ( evts[i][2] == Event.PR && toBeFixed.get(evts[i][1]) == true ) { // PR event are processed later
                    bufferPR[nbItemsBufferPR] = evts[i][1];
                    nbItemsBufferPR++;
                } else if ( evts[i][2] == Event.AP ) {
                    for(int r=0;r<nbResources;r++) {
                        // [COLORS]
                        if ( resourceTypes[r] == COLORED ) {
                            ctr = heightsAPEvents[r][evts[i][3]];
                            if ( ctr <  0 ) { // start of fixed task
                                ctr = -ctr;
                                if ( countColors[r][ctr] == 0 ) {
                                    nbDistinctColors[r]++;
                                }
                                countColors[r][ctr]++;
                            } else if ( ctr > 0 ) {
                                if ( countColors[r][ctr] == 1 ) {
                                    nbDistinctColors[r]--;
                                }
                                countColors[r][ctr]--;
                            } else {
                                System.out.println(" AGGREGATED EVENT WITH A NEUTRAL COLOR "); // TODO do not generate such events.
                            }
                        } else {
                            assert (resourceTypes[r] == CUMULATIVE);
                            gap[r] += heightsAPEvents[r][evts[i][3]];
                        }
                        // [COLORS]
                    }
                }
            }
            if (!hEvents.isEmpty()) { date = hEvents.peekDate(); }
            else { date = maxDate; }
            for (int i=0;i<nbItemsBufferPR;i++) {
                t = bufferPR[i];
                if ( exceedGap(t) == -1 && leGreedy[t] <= date ) {
                    greedyFix(t);
                }
            }
            for (int i=0;i<nbItemsBufferPR;i++) { // PR event are processed now
                t = bufferPR[i];
                if ( toBeFixed.get(t) ) {
                    trail.push(Trail.PR,t);
                    if ( (rc = exceedGap(t)) != -1) { // one of the gap is exceeded
                        ringGreedy.setConflict(t,rc);
                    } else {
                        ringGreedy.setCheck(t);
                    }
                }
            }
            return true;
    //System.out.println(" END greedy_phase1(): delta="+delta+" delta'="+date);
        }

        // OK
        private boolean greedyFix(int t) {
    //System.out.println(" CALL greedy_fix(t="+t+",delta="+delta+")");
            int ctr;
            if ( lsGreedy[t] < delta ) {
    //System.out.println(" -> SET READY t="+t+" ls="+ls[t]);
                ringGreedy.setReady(t);
                return false;
    //System.out.println(" END greedy_fix ");
            } else {
                toBeFixed.set(t,false);
                ringGreedy.setNone(t);
                adjustMax(t,lsGreedy[t],leGreedy[t]);
                trail.push(Trail.FSCP,t);
                hEvents.add(leGreedy[t],t, Event.FECP,-1);
                for (int r=0;r<nbResources;r++) {
                    // [COLORS]
                    if ( resourceTypes[r] == COLORED ) {
                        ctr = c[t][r];
                        if ( ctr != 0 ) { // is not the neutral color
                            if ( countColors[r][ctr] == 0 ) {
                                nbDistinctColors[r]++;
                            }
                            countColors[r][ctr]++;
                        }
                    } else {
                        assert (resourceTypes[r] == CUMULATIVE);
                        gap[r] -= c[t][r];
                    }
                    // [COLORS]
                }
    //System.out.println(" END greedy_fix ");
                return true;
            }
        }

        // OK
        private boolean greedyPhase2() {
            int r,t,next,rc,ecpi;
            boolean fixed = false;
            for (r=0;r<nbResources;r++) {
                // [COLORS]
                if ( resourceTypes[r] == COLORED ) {
                    if ( nbDistinctColors[r] > capacities[r] ) {
                        return false;
                    }
                } else {
                    assert (resourceTypes[r] == CUMULATIVE);
                    if (gap[r]<0) {
                        return false;
                    }
                }
                // [COLORS]
            }
            for (r=0;r<nbResources;r++) {
                next = ringGreedy.firstInConflict(r);
                while (next != ringGreedy.ptrConflict+r) {
                    t = next;
                    next = ringGreedy.next(t);
                    // [COLORS]
                    if ((resourceTypes[r] == CUMULATIVE && gapi[r] < gap[r] && c[t][r] <= gap[r] && delta+ld[t] <= date) ||      // TODO sortir le test sur le gap (ne depend pas de la tache)
                        (resourceTypes[r] == COLORED && (nbDistinctColors[r] < capacities[r] || countColors[r][c[t][r]] != 0) && delta+ld[t] <= date)) {
                    // [COLORS]
                        if ( ( rc = exceedGap(t)) != -1) { // one of the gap is exceeded
                            ringGreedy.setConflict(t,rc);
                        } else {
                            adjustMin(t,delta,delta+ld[t]);
                            fixed |= greedyFix(t);
                        }
                    }
                }
            }
            for (r=0;r<nbResources;r++) {
                // [COLORS]
                if (resourceTypes[r] == COLORED) {
                    next = ringGreedy.firstInConflict(r);
                        while (next != ringGreedy.ptrConflict+r) {
                            t = next;
                            next = ringGreedy.next(t);
                            if ( nbDistinctColors[r] < capacities[r] || countColors[r][c[t][r]] != 0 ) {
                                if ((rc=exceedGap(t)) != -1) { // one of the gapi is exceeded.
                                    ringGreedy.setConflict(t,rc);
                                } else {
                                    trail.push(Trail.PR,t);
                                    ecpi = leGreedy[t];
                                    adjustMin(t,delta,delta+ld[t]);
                                    ringGreedy.setCheck(t);
                                    if (usGreedy[t]>=ecpi && usGreedy[t]<leGreedy[t]) {
                                        hEvents.add(leGreedy[t],t, Event.ECP,-1);
                                    }
                                }
                            }
                        }
                } else {
                    assert (resourceTypes[r] == CUMULATIVE);
                    if (gapi[r]<gap[r]) { // the gap increases
                        next = ringGreedy.firstInConflict(r);
                        while (next != ringGreedy.ptrConflict+r) {
                            t = next;
                            next = ringGreedy.next(t);
                            if (c[t][r]<=gap[r]) {
                                if ((rc=exceedGap(t)) != -1) { // one of the gapi is exceeded.
                                    ringGreedy.setConflict(t,rc);
                                } else {
                                    trail.push(Trail.PR,t);
                                    ecpi = leGreedy[t];
                                    adjustMin(t,delta,delta+ld[t]);
                                    ringGreedy.setCheck(t);
                                    if (usGreedy[t]>=ecpi && usGreedy[t]<leGreedy[t]) {
                                        hEvents.add(leGreedy[t],t, Event.ECP,-1);
                                    }
                                }
                            }
                        }
                        gapi[r] = gap[r];
                    }
                }
                // [COLORS]
            }
            for (r=0;r<nbResources;r++) {
                // [COLORS]
                if (resourceTypes[r] == COLORED) {
                    next = ringGreedy.firstInCheck();
                    while (next != ringGreedy.ptrCheck) {
                        t = next;
                        next = ringGreedy.next(t);
                        if ( nbDistinctColors[r] == capacities[r] && countColors[r][c[t][r]] == 0 ) {
                            if (leGreedy[t] > delta) {
                                ringGreedy.setConflict(t,r);
                            } else {
                                greedyFix(t);
                            }
                        }
                    }
                } else {
                    if (gapi[r] > gap[r] || fixed) { // the gap decreases
                        next = ringGreedy.firstInCheck();
                        while (next != ringGreedy.ptrCheck) {
                            t = next;
                            next = ringGreedy.next(t);
                            if ( c[t][r]>gap[r]  ) {
                                if (leGreedy[t] > delta) {
                                    ringGreedy.setConflict(t,r);
                                } else {
                                    greedyFix(t);
                                }
                            }
                        }
                        gapi[r] = gap[r];
                    }
                }
                // [COLORS]
            }
            return true;
        }

        // OK
        private void greedyAssign() {
            int t = -1, next, tag, item[];
            int tp = -1, ctr;
            next = ringGreedy.firstInReady();
            while (next != ringGreedy.ptrReady) {
                t = next;
                next = ringGreedy.next(t);
                if ( tp == -1 || lsGreedy[t] < lsGreedy[tp] ) { tp = t; }
            }
            ringGreedy.setNone(tp);
            while ( delta >= lsGreedy[tp] ) {
                item = trail.pop(); // 0:tag, 1:data
                tag = item[0];
                t = item[1];
                if ( tag == Trail.FSCP ) {
                    assert ( t >= 0 && t < nbTasksInFilteringAlgo);
                    hEvents.add(delta,t, Event.FSCP,-1);
                    for (int r=0;r<nbResources;r++) {
                        // [COLORS]
                        if ( resourceTypes[r] == COLORED ) {
                            ctr = c[t][r];
                            if ( ctr != 0 ) { // is not the neutral color
                                if ( countColors[r][ctr] == 1 ) {
                                    nbDistinctColors[r]--;
                                }
                                countColors[r][ctr]--;
                            }
                        } else {
                            assert (resourceTypes[r] == CUMULATIVE);
                            gap[r] += c[t][r];
                        }
                        // [COLORS]
                    }
                } else if ( tag == Trail.FECP ) {
                    hEvents.add(delta,t, Event.FECP,-1);
                    for (int r=0;r<nbResources;r++) {
                        // [COLORS]
                        if ( resourceTypes[r] == COLORED ) {
                            ctr = c[t][r];
                            if ( ctr != 0 ) { // is not the neutral color
                                if ( countColors[r][ctr] == 0 ) {
                                    nbDistinctColors[r]++;
                                }
                                countColors[r][ctr]++;
                            }
                        } else {
                            assert (resourceTypes[r] == CUMULATIVE);
                            gap[r] -= c[t][r];
                        }
                        // [COLORS]
                    }
                } else if ( tag == Trail.SCP && toBeFixed.get(t) ) {
                    hEvents.add(delta,t, Event.SCP,-1);
                    if ( delta < leGreedy[t] ) {
                        for (int r=0;r<nbResources;r++) {
                            // [COLORS]
                            if ( resourceTypes[r] == COLORED ) {
                                ctr = c[t][r];
                                if ( ctr != 0 ) { // is not the neutral color
                                    if ( countColors[r][ctr] == 1 ) {
                                        nbDistinctColors[r]--;
                                    }
                                    countColors[r][ctr]--;
                                }
                            } else {
                                assert (resourceTypes[r] == CUMULATIVE);
                                gap[r] += c[t][r];
                            }
                            // [COLORS]
                        }
                    }
                } else if ( tag == Trail.ECP && toBeFixed.get(t) ) {
                    hEvents.add(delta,t, Event.ECP,-1);
                    for (int r=0;r<nbResources;r++) {
                        // [COLORS]
                        if ( resourceTypes[r] == COLORED ) {
                            ctr = c[t][r];
                            if ( ctr != 0 ) { // is not the neutral color
                                if ( countColors[r][ctr] == 0 ) {
                                    nbDistinctColors[r]++;
                                }
                                countColors[r][ctr]++;
                            }
                        } else {
                            assert (resourceTypes[r] == CUMULATIVE);
                            gap[r] -= c[t][r];
                        }
                        // [COLORS]
                    }
                } else if ( tag == Trail.PR && toBeFixed.get(t) && !ringGreedy.inNone(t) ) {
                    hEvents.add(lsGreedy[t],t, Event.PR,-1);
                    ringGreedy.setNone(t);
                } else if ( tag == Trail.DELTA && delta > t ) { // here 't' is a timepoint (not a task)
                    date = delta;
                    delta = t;
                }
            }
            toBeFixed.set(tp,false);
            adjustMax(tp,lsGreedy[tp],leGreedy[tp]);
            hEvents.add(lsGreedy[tp],tp, Event.FSCP,-1);
            hEvents.add(leGreedy[tp],tp, Event.FECP,-1);
            assert ( ringGreedy.isEmptyReady() );
        }

        // It returns -1 if the task does not exceed any gap.
        // Otherwise, it returns the a resource where it exceeds.
        private int exceedGap(int t) {
            for (int r=0;r<nbResources;r++) {
                if ( resourceTypes[r] == COLORED ) {
                    if ( nbDistinctColors[r] == capacities[r] && countColors[r][c[t][r]] == 0 ) return r;
                } else {
                    assert (resourceTypes[r] == CUMULATIVE);
                    if ( c[t][r] > gap[r] ) return r;
                }
            }
            return -1;
        }

        public int ls(int t) {
            return lsGreedy[t];
        }

        public int us(int t) {
            return usGreedy[t];
        }

        public int le(int t) {
            return leGreedy[t];
        }

        public int ue(int t) {
            return ueGreedy[t];
        }

        public int ld(int t) {
            return ld[t];
        }

        public boolean allTasksAreFixed() {
            for (int t=0;t<nbTasksInFilteringAlgo;t++) {
                if ( lsGreedy[t] != usGreedy[t] ) return false;
            }
            return true;
        }

        // ====================================================
        // ====================================================

        public void printEvent(int[] evt) {
            System.out.print("\t<date="+evt[0]+",task="+evt[1]+",type=");
            switch (evt[2]) {
                case Event.SCP : System.out.print("SCP"); break;
                case Event.ECP : System.out.print("ECP"); break;
                case Event.PR : System.out.print("PR"); break;
                case Event.CCP : System.out.print("CCP"); break;
                case Event.FSCP : System.out.print("FSCP"); break;
                case Event.FECP : System.out.print("FECP"); break;
                case Event.AP : System.out.print("AP"); break;
            }
            System.out.println(",dec="+evt[3]+">");
        }

        public void printTasks() {
            for (int t=0;t<nbTasksInFilteringAlgo;t++) {
                printTask(t);
            }
        }

        public void printTask(int i) {
            System.out.print("Task:"+i+" : s:["+lsGreedy[i]+".."+usGreedy[i]+"] d:["+ld[i]+".."+ld[i]+"] e:["+leGreedy[i]+".."+ueGreedy[i]+"]");
            if (usGreedy[i] < leGreedy[i]) System.out.println("| scp:"+usGreedy[i]+" ecp:"+leGreedy[i]); else System.out.println();;
        }

    }

    class Trail {

        private int size;
        private int current;

        private int[] tag;
        private int[] data;

        private final int[] buffer;

        public final static int FSCP = 1;
        public final static int FECP = 2;
        public final static int SCP = 3;
        public final static int ECP = 4;
        public final static int PR = 5;
        public final static int DELTA = 6;



        public Trail(int _size) {
            this.size = _size;
            this.current = 0;
            this.tag = new int[size];
            this.data = new int[size];
            this.buffer = new int[2];
        }

        public void push(int _tag, int _data) {
            //System.out.println(" <- PUSH "+this);
            if ( current >= size ) {
                increaseSize();
            }
            assert (current < size);
            assert (tag.length == size);
            tag[current] = _tag;
            data[current] = _data;
            current++;
        }

        public int[] pop() {
            //System.out.println(" -> POP  "+this);
            current--;
            buffer[0] = tag[current];
            buffer[1] = data[current];
            return buffer;
        }

        public void increaseSize() {
            //System.out.print(" -> INCREASE TRAIL SIZE  ");
            //long start, duration, end;
            //        start = System.currentTimeMillis();
            size = (int)(size * 1.5);
            int[] _tag = new int[size];
            int[] _data = new int[size];
            for (int i=0;i<current;i++) {
                _tag[i] = tag[i];
                _data[i] = data[i];
            }
            tag = _tag;
            data = _data;
            //        end = System.currentTimeMillis();
            //        duration = end - start;
            //System.out.println("IN "+duration+" ms");
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i=0;i<current;i++) {
                sb.append("<");
                switch (tag[i]) {
                    case 1:
                        sb.append("FSCP");
                        break;
                    case 2:
                        sb.append("FECP");
                        break;
                    case 3:
                        sb.append("SCP");
                        break;
                    case 4:
                        sb.append("ECP");
                        break;
                    case 5:
                        sb.append("PR");
                        break;
                    case 6:
                        sb.append("DELTA");
                        break;
                }
                sb.append(","+data[i]+">,");
            }
            return sb.toString();
        }

    }


}
