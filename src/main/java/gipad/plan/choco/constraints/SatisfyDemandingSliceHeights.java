
package gipad.plan.choco.constraints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.discovery.DiscoveryModel.model.Node;

import solver.constraints.Constraint;
import solver.variables.IntVar;
import gipad.plan.choco.DefaultReconfigurationProblem;
import gipad.plan.choco.ReconfigurationProblem;
import gipad.plan.choco.actionmodel.slice.DemandingSlice;
import gipad.plan.choco.actionmodel.slice.Slice;

public class SatisfyDemandingSliceHeights{

    public SatisfyDemandingSliceHeights(DefaultReconfigurationProblem rp) {
        List<Slice> dSlices = new ArrayList<Slice>(rp.getDemandingSlices()));
        //Consuming et leaving sur lnoeud
        //Collections.sort(dSlices, new SliceComparator(false, SliceComparator.ResourceType.cpuConsumption));

        //VM actions 
        //Puis selon l'action associée on regarde les cas !
        int[][] sizes = new int[2][];
        sizes[0] = new int[dSlices.size()];
        sizes[1] = new int[dSlices.size()];

        IntVar<?>[] assigns = new IntVar[dSlices.size()];
        for (int i = 0; i < dSlices.size(); i++) {
            sizes[0][i] = dSlices.get(i).getCPUheight()[0];
            sizes[1][i] = dSlices.get(i).getMemoryheight();
            sizes[2][i] = dSlices.get(i).getBwInput();
            sizes[3][i] = dSlices.get(i).getBwOutput();
            assigns[i] = dSlices.get(i).hoster();
        }

        Node[] ns = rp.getNodes();
        IntVar<?>[][] capas = new IntVar[2][];
        capas[0] = new IntVar[ns.length];
        capas[1] = new IntVar[ns.length];

        for (int i = 0; i < ns.length; i++) {
            capas[0][i] = rp.getFreeCPU(ns[i]);
            capas[1][i] = rp.getFreeMem(ns[i]);
            capas[2][i] = rp.getF.getBwInput();
            capas[3][i] = dSlices.get(i).getBwOutput();
        }

        Constraint c = new CumulativeMultiDim(vTasks, vHeights, rp.getC, rp.getSolver(), interestingTimePoints);
        rp.getSolver().post(c);
    }
    
    public IntVar<?>[] getCapacities(){
	return null;
	
    }
}
