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

package entropy.plan.choco.actionModel.slice;

import java.util.List;

import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A utility class to extract some variables from slices.
 *
 * @author Fabien Hermenier
 */
public final class Slices {

    /**
     * Utility class. No Instantiation
     */
    private Slices() {
    }

    /**
     * Extract all the hosters of an array of slices.
     *
     * @param slices the slices to consider
     * @return an array of assignement var, in an order similar to slices
     */
    public static IntDomainVar[] extractHosters(Slice[] slices) {
        IntDomainVar[] l = new IntDomainVar[slices.length];
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
    public static IntDomainVar[] extractHosters(List<? extends Slice> slices) {
        return extractHosters(slices.toArray(new Slice[slices.size()]));
    }

    /**
     * Extract all the CPU heights of an array of slices.
     *
     * @param slices the slices to consider
     * @return an array of integer with regards to the order of the slices
     */
    public static int[] extractCPUHeights(Slice[] slices) {
        int[] heights = new int[slices.length];
        for (int i = 0; i < slices.length; i++) {
            heights[i] = slices[i].getCPUheight();
        }
        return heights;
    }

    /**
     * Extract all the CPU heights of a list of slices.
     *
     * @param slices the slices to consider
     * @return <code>extractCPUHeights(slices.toArray(new Slice[slices.size()]))</code>
     */
    public static int[] extractCPUHeights(List<Slice> slices) {
        return extractCPUHeights(slices.toArray(new Slice[slices.size()]));
    }

    /**
     * Extract all the memory heights of an array of slices.
     *
     * @param slices the slices to consider
     * @return an array of integer with regards to the order of the slices
     */
    public static int[] extractMemoryHeights(Slice[] slices) {
        int[] heights = new int[slices.length];
        for (int i = 0; i < slices.length; i++) {
            heights[i] = slices[i].getMemoryheight();
        }
        return heights;
    }

    /**
     * Extract all the memory heights of a list of slices
     *
     * @param slices the slices to consider
     * @return <code>extractMemoryHeights(slices.toArray(new Slice[slices.size()]))</code>
     */
    public static int[] extractMemoryHeights(List<Slice> slices) {
        return extractMemoryHeights(slices.toArray(new Slice[slices.size()]));
    }

    /**
     * Extract all the end moment of an array of slices.
     *
     * @param slices the slices to consider
     * @return an array of variable with regards to the order of the slices.
     */
    public static IntDomainVar[] extractEnds(Slice[] slices) {
        IntDomainVar[] ends = new IntDomainVar[slices.length];
        for (int i = 0; i < slices.length; i++) {
            ends[i] = slices[i].end();
        }
        return ends;
    }

    /**
     * Extract all the end moment of a list of slices.
     *
     * @param slices the slices to consider
     * @return an array of variable with regards to the order of the slices.
     */
    public static IntDomainVar[] extractEnds(List<Slice> slices) {
        return extractEnds(slices.toArray(new Slice[slices.size()]));
    }

    /**
     * Extract all the start moment of an array of slices.
     *
     * @param slices the slices to consider
     * @return an array of variable with regards to the order of the slices.
     */
    public static IntDomainVar[] extractStarts(Slice[] slices) {
        IntDomainVar[] ends = new IntDomainVar[slices.length];
        for (int i = 0; i < slices.length; i++) {
            ends[i] = slices[i].start();
        }
        return ends;
    }

    /**
     * Extract all the start moment of a list of slices.
     *
     * @param slices the slices to consider
     * @return an array of variable with regards to the order of the slices.
     */
    public static IntDomainVar[] extractStarts(List<Slice> slices) {
        return extractStarts(slices.toArray(new Slice[slices.size()]));
    }

}
