package gipad.plan.choco.constraints;

import java.util.Arrays;

import solver.Solver;
import solver.constraints.Constraint;
import solver.variables.IntVar;
import solver.variables.Task;

public class CumulativeMultiDim extends Constraint{
    
    private static final long serialVersionUID = 2212908998958152173L;
    
	//************ Static variables 
	private final int CUMULATIVE = PropTTPCDynamicSweepLoads.CUMULATIVE;
	private final int COLORED = PropTTPCDynamicSweepLoads.COLORED;
	
	//*********all variables
	private IntVar[] allVars;
	private static IntVar[][] vLoads;// Pb si en static ?
	
	
	public CumulativeMultiDim( Task[] vTasks, IntVar[][] vHeights, IntVar[] vCapacities,  Solver solver, int[] interestingTimePoints) {
	    super(varsAggregator(vCapacities.length, vTasks, vHeights, vCapacities,interestingTimePoints.length), solver);
	    Constraint<?, PropTTPCDynamicSweepLoads> c = new Constraint(allVars, solver);
	    int[] resourceType= new int[vCapacities.length];
	    Arrays.fill(resourceType, CUMULATIVE);
	    int[] interestingResources  = new int[]{0};
	    int[] capacities=new int[vCapacities.length];
	    for(int i=0;i<vCapacities.length;i++){
		capacities[i]=vCapacities[i].getValue();
	    }
	    c.addPropagators(new PropTTPCDynamicSweepLoads(allVars,vTasks.length,vCapacities.length,capacities, new int[vTasks.length][0],resourceType,interestingTimePoints,interestingResources));	
	}
	
 
      private static IntVar[] varsAggregator(int nbResources, Task[] vTasks, IntVar[][] vHeights, IntVar[] vCapacities, int nbInterestingTimePoints) {
          int hIdx = 3*vTasks.length;
          IntVar[] allVars = new IntVar[hIdx+vTasks.length*nbResources+nbResources+nbInterestingTimePoints*nbResources];
          CumulativeMultiDim.vLoads = new IntVar[nbInterestingTimePoints][vCapacities.length];
          for (int t=0;t<vTasks.length;t++) {
              allVars[t] = vTasks[t].getStart();
              allVars[t+vTasks.length] = vTasks[t].getDuration();
              allVars[t+2*vTasks.length] = vTasks[t].getEnd();
              for (int r=0;r<nbResources;r++) {
                  allVars[hIdx+t*nbResources+r] = vHeights[t][r];
              }
          }
          int cIdx = hIdx+vTasks.length*nbResources;
          for (int r=0;r<nbResources;r++) {
              allVars[cIdx+r] = vCapacities[r];
          }
          int lIdx = cIdx+nbResources;
          for (int i=0;i<nbInterestingTimePoints;i++) {
              for (int j=0;j<nbResources;j++) {
                  allVars[lIdx+i*nbResources+j] = vLoads[i][j];
              }
          }
    	return allVars;
      }
}
