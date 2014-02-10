package gipad.plan.choco.constraints;

import solver.Solver;
import solver.constraints.Constraint;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.selectors.values.InDomainMin;
import solver.search.strategy.selectors.variables.Smallest;
import solver.search.strategy.strategy.Assignment;
import solver.variables.IntVar;
import solver.variables.Task;
import solver.variables.VariableFactory;

import org.junit.*;


@SuppressWarnings("rawtypes") 
public class CumulativeMultiDimTest {
    	
    	// ===== DATA =====
        
	 Task[] vTasks;
	 IntVar[] vStarts;
	 IntVar[] vDurations;
	 IntVar[] vEnds ;
	 IntVar[][] vHeights ;
	 IntVar[] vCapacities ;
	 IntVar[][] vLoads ;
    
	//************ Static variables 
	private final int CUMULATIVE = PropTTPCDynamicSweepLoads.CUMULATIVE;
	private final int COLORED = PropTTPCDynamicSweepLoads.COLORED;
    
    @SuppressWarnings("unchecked")
    @Test
    public void testMe() {
	
        // ===== BUILD TASKS/ITEMS =====
        Solver solver = new Solver();
	int[] startsLB = new int[]{0, 0, 0, 0, 0,   0,1,2,3};
        int[] endsUB = new int[]{4, 4, 4, 4, 4,     1,2,3,4};
        int[] durations = new int[]{1, 1, 1, 1, 1,  1,1,1,1};
        int[][] heights = new int[][]{ {2,2,1}, {2,2,2}, {1,1,3}, {1,1,4}, {2,2,5},    {5,5,0},{5,5,0},{5,5,0},{5,5,0}};
        int[][] successors = new int[startsLB.length][0];
        int[] capacities = new int[]{10,10,2};
        int[] resourceType = new int[]{CUMULATIVE,CUMULATIVE,COLORED};
        int nbTasks = startsLB.length;
        int nbResources = capacities.length;

        int[] interestingTimePoints = new int[]{0,1,2,3};
        int[] interestingResources  = new int[]{0};
        int nbInterestingTimePoints = interestingTimePoints.length;
        int nbInterestingResources = interestingResources.length;
        
        //Convert to IntVar
        intToIntVar(solver, nbTasks, nbResources, nbInterestingTimePoints, nbInterestingResources,  startsLB, endsUB,  durations,heights,  interestingTimePoints, capacities,interestingResources);
        
        
        // ===== BUILD AND POST CONSTRAINT =====
        IntVar[] allVars = varsAggregator( solver,  nbTasks,  nbResources,  nbInterestingTimePoints,  nbInterestingResources, capacities);
        Constraint c = new Constraint(allVars, solver);
        c.addPropagators(new PropTTPCDynamicSweepLoads(allVars,nbTasks,nbResources,capacities,successors,resourceType,interestingTimePoints,interestingResources));
        solver.post(c);

        // ===== POST ADDITIONAL CONSTRAINTS ? =====


        // search configuration
        solver.set(new Assignment(new Smallest(vStarts), new InDomainMin())); //OK (default)

        // log configuration
        SearchMonitorFactory.log(solver, true, true);
        SearchMonitorFactory.limitTime(solver, 10000);
        // solve
        solver.findSolution();
        // Pretty out
        prettyOut(solver,nbTasks,nbResources,resourceType,capacities);
        
    }
    

    private void prettyOut(Solver solver, int nbTasks,int nbResources, int[] resourceType,  int[] capacities) {
	System.out.println(solver);
        int horizon = 4;
        for (int i = 0; i < nbResources; i++) {
		System.out.println("Res Type " + resourceType[i]);
		System.out.println("Capacity " + capacities[i]);
		int[] cumul = new int[horizon + 1];
		for (int j = 0; j < nbTasks; j++) {
			System.out.print("Task n°" + j + " start : " + vTasks[j].getStart().getValue() + " end : " +  vTasks[j].getEnd().getValue() +"    ");
			for (int k = 0; k < horizon + 1; k++) {
				if (k < vTasks[j].getStart().getValue() || k >= vTasks[j].getEnd().getValue()) {
					System.out.print("_");
				} else {
					System.out.print(vHeights[j][i].getValue());
					cumul[k] += vHeights[j][i].getValue();
				}
			}
			System.out.println();
		}
		System.out.print("                              ");
		for (int k = 0; k < horizon + 1; k++) {
			System.out.print(cumul[k]);
		}
		System.out.println();
		System.out.println();
		}
    }

    private IntVar[] varsAggregator(Solver solver, int nbTasks, int nbResources, int nbInterestingTimePoints, int nbInterestingResources, int[] capacities) {
	int hIdx = 3*nbTasks;
        IntVar[] allVars = new IntVar[hIdx+nbTasks*nbResources+nbResources+nbInterestingTimePoints*nbInterestingResources];
        for (int t=0;t<nbTasks;t++) {
            allVars[t] = vStarts[t];
            allVars[t+nbTasks] = vDurations[t];
            allVars[t+2*nbTasks] = vEnds[t];
            for (int r=0;r<nbResources;r++) {
                allVars[hIdx+t*nbResources+r] = vHeights[t][r];
            }
        }
        int cIdx = hIdx+nbTasks*nbResources;
        for (int r=0;r<nbResources;r++) {
            vCapacities[r] = VariableFactory.bounded("limit_" + r, capacities[r], capacities[r], solver);
            allVars[cIdx+r] = vCapacities[r];
        }
        int lIdx = cIdx+nbResources;
        for (int i=0;i<nbInterestingTimePoints;i++) {
            for (int j=0;j<nbInterestingResources;j++) {
                allVars[lIdx+i*nbInterestingResources+j] = vLoads[i][j];
            }
        }
	return allVars;
    }

    private void intToIntVar(Solver solver, int nbTasks, int nbResources, int nbInterestingTimePoints, int nbInterestingResources, int[] startsLB, int[] endsUB, int[] durations, int[][] heights, int[] interestingTimePoints, int[] capacities, int[] interestingResources) {
	vTasks = new Task[nbTasks];
        vStarts = new IntVar[nbTasks];
        vDurations = new IntVar[nbTasks];
        vEnds = new IntVar[nbTasks];
        vHeights = new IntVar[nbTasks][nbResources];
        vCapacities = new IntVar[nbResources];
        vLoads = new IntVar[nbInterestingTimePoints][nbInterestingResources];

        for(int i=0;i< nbTasks;i++){
            vStarts[i] = VariableFactory.bounded("s_"+i, startsLB[i], endsUB[i]-durations[i], solver);
            vDurations[i] = VariableFactory.bounded("d_"+i, durations[i], durations[i], solver);
            vEnds[i] = VariableFactory.bounded("e_"+i, startsLB[i]+durations[i], endsUB[i], solver);
            vTasks[i] = VariableFactory.task(vStarts[i], vDurations[i], vEnds[i]);
            for (int r=0;r<nbResources;r++) {
                vHeights[i][r] = VariableFactory.bounded("t"+i+"_height_r"+r,heights[i][r],heights[i][r], solver);
            }
        }

        for (int i=0;i<nbInterestingTimePoints;i++) {
            for (int j=0;j<nbInterestingResources;j++) {
                vLoads[i][j] = VariableFactory.bounded("load_"+interestingTimePoints[i],0,capacities[interestingResources[j]],solver);
            }
        }
    }
}
