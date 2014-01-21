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

package entropy.plan.choco.constraint.platform;

import java.util.List;

import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.plan.choco.ReconfigurationProblem;
import entropy.plan.choco.actionModel.ActionModels;
import entropy.plan.choco.actionModel.slice.DemandingSlice;
import entropy.plan.choco.constraint.GlobalConstraint;
import entropy.vjob.ExplodedSet;
import gnu.trove.TIntObjectHashMap;

/**
 * @author Fabien Hermenier
 */
public class PlatformConstraint implements GlobalConstraint {


    private List<String> platforms;

    private IntDomainVar freeInfra;

    private TIntObjectHashMap objs;

    public PlatformConstraint(List<String> pls) {
        this.platforms = pls;
    }

    public List<String> getPlatforms() {
        return platforms;
    }


    @Override
    public void add(ReconfigurationProblem rp) {
        //For each node, we specify a 'platform' variable
        Node[] ns = rp.getNodes();
        IntDomainVar[] vs = new IntDomainVar[ns.length];
        for (int i = 0; i < vs.length; i++) {
            Node n = ns[i];
            vs[i] = rp.createEnumIntVar(n.getName() + "#platform", 0, platforms.size() - 1);
        }
        freeInfra = rp.createEnumIntVar("free", 0, platforms.size());

        IntDomainVar[] v = new IntDomainVar[rp.getVirtualMachines().length];

        //A constant to indicate the required platform
        int i = 0;
        for (VirtualMachine vm : rp.getVirtualMachines()) {
            if (!objs.containsKey(vm.hashCode())) {
                v[i] = freeInfra;
            } else {
                v[i] = rp.createIntegerConstant(objs.get(hashCode()).toString(), 0);
            }
            i++;
        }

        //Last, we enforce a node will have the good platform when it host VMs
        for (DemandingSlice sl : ActionModels.extractDemandingSlices(rp.getVirtualMachineActions())) {
            //blabla
        }


    }

    public boolean setPlatform(VirtualMachine vm, String p) {
        objs.put(vm.hashCode(), p);
        return true;
    }

    @Override
    public boolean isSatisfied(Configuration cfg) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ExplodedSet<VirtualMachine> getAllVirtualMachines() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ExplodedSet<VirtualMachine> getMisPlaced(Configuration cfg) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
