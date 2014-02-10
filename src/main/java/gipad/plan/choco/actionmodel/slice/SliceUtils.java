package gipad.plan.choco.actionmodel.slice;

import java.util.List;

import solver.variables.IntVar;

public class SliceUtils {

    /**
     * Extract all the hosters of an array of slices.
     *
     * @param slices the slices to consider
     * @return an array of assignement var, in an order similar to slices
     */
    public static IntVar<?>[] extractHosters(Slice[] slices) {
        IntVar<?>[] l = new IntVar[slices.length];
        for (int i = 0; i < slices.length; i++) {
            l[i] = slices[i].hoster();
        }
        return l;
    }
    
    /**
     * Extract all the hosters of a list of slices
     *
     * @param slices the slices to consider
     * @return <code>extractHosters(slices.toArray(new Slice[slices.size()]))</code>
     */
    public static IntVar<?>[] extractHosters(List<? extends Slice> slices) {
        return extractHosters(slices.toArray(new Slice[slices.size()]));
    }
}
