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


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import entropy.vjob.VJob;

/**
 * An absract pool of vjobs.
 *
 * @author Fabien Hermenier
 */
public abstract class VJobsPool {

    /**
     * The current logger for the class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("Queue");

    /**
     * An ordered list of vjobs. First have an higher priority.
     *
     * @return the list of vjobs
     */
    public abstract List<VJob> getRunningPriorities();

    /**
     * Get the logger
     *
     * @return an initialized logger
     */
    public static Logger getLogger() {
        return LOGGER;
    }

    /**
     * Add a vjob into the pool.
     *
     * @param v the vjob to add
     * @return true if the vjob is added.
     */
    public abstract boolean add(VJob v);

    /**
     * Remove the vjob from the pool.
     *
     * @param v the vjob to remove
     * @return true if the vjob was removed
     */
    public abstract boolean remove(VJob v);

    /**
     * Textual representation of the queue.
     *
     * @return a String
     */
    @Override
	public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("{");
        List<VJob> jobs = this.getRunningPriorities();
        for (int i = 0; i < jobs.size(); i++) {
            buffer.append(jobs.get(i).id());
            if (i < jobs.size() - 1) {
                buffer.append(", ");
            }
        }
        buffer.append("}");
        return buffer.toString();
    }

    /**
     * Get a VJob from its identifier.
     *
     * @param id the identifier of the vjob.
     * @return the vjob if exists, null otherwise
     */
    public abstract VJob get(String id);
}
