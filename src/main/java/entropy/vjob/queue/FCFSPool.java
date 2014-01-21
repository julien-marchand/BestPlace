package entropy.vjob.queue;
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


import java.util.LinkedList;
import java.util.List;

import entropy.vjob.VJob;

/**
 * A Queue based on the algorithm FCFS (First Come, First serve).
 * The queue relies on a storable queue to manage them.
 *
 * @author Fabien Hermenier
 */
public class FCFSPool extends VJobsPool {

    /**
     * The queue.
     */
    private List<VJob> vjobs;

    /**
     * Make a new queue.
     */
    public FCFSPool() {
        this.vjobs = new LinkedList<VJob>();
    }

    /**
     * Get the VJobs.
     *
     * @return a list of vjobs, may be empty. Earlier jobs are the first
     */
    @Override
    public List<VJob> getRunningPriorities() {
        return this.vjobs;
    }

    @Override
    public boolean add(VJob v) {
        return vjobs.add(v);
    }

    @Override
    public boolean remove(VJob v) {
        return vjobs.remove(v);
    }

    @Override
    public VJob get(String id) {
        for (VJob v : vjobs) {
            if (v.id().equals(id)) {
                return v;
            }
        }
        return null;
    }
}
