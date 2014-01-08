/*
 * Copyright (c) Fabien Hermenier
 *
 *        This file is part of Entropy.
 *
 *        Entropy is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU Lesser General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        Entropy is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU Lesser General Public License for more details.
 *
 *        You should have received a copy of the GNU Lesser General Public License
 *        along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.plan.partitioner;

import java.util.List;

import entropy.vjob.Ban;
import entropy.vjob.Fence;
import entropy.vjob.OneOf;
import entropy.vjob.Spread;

/**
 * @author Fabien Hermenier
 */
public interface PlanPartitioner {

    void part(Fence f) throws PartitioningException;

    void part(Ban b) throws PartitioningException;

    void part(Spread s) throws PartitioningException;

    void part(OneOf s) throws PartitioningException;

    List<Partition> getResultingPartitions();

}
