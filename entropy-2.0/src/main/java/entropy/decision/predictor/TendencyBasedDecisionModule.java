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

package entropy.decision.predictor;

import java.util.HashMap;
import java.util.Map;

import entropy.configuration.Configuration;
import entropy.configuration.VirtualMachine;
import entropy.decision.AssignmentException;
import entropy.decision.DecisionModule;

/**
 * A Decision module that ask to always restart failed VMs and uses
 * a tendency based approach to estimate the CPU demand of the VMs.
 *
 * @author Fabien Hermenier
 */
public class TendencyBasedDecisionModule extends DecisionModule {

    public static final int DEFAULT_STEP = 50;

    private int step = DEFAULT_STEP;

    private Map<VirtualMachine, VMTendencyPredictor> preds;

    public TendencyBasedDecisionModule() {
        this.preds = new HashMap<VirtualMachine, VMTendencyPredictor>();
    }

    @Override
    public Configuration compute(Configuration cfg) throws AssignmentException {

        //Update the consumption of all the running VMs.
        for (VirtualMachine vm : cfg.getAllVirtualMachines()) {
            if (!preds.containsKey(vm)) {
                preds.put(vm, new VMTendencyPredictor(getStep()));
            }
            preds.get(vm).predictCPUDemand(vm);
            if (cfg.isRunning(vm) && vm.getCPUDemand() > cfg.getLocation(vm).getCPUCapacity()) {
                int d = vm.getCPUDemand();
                DecisionModule.getLogger().debug(vm.getName() + "' demand '" + d + "' decreased to '" + cfg.getLocation(vm).getCPUCapacity() + "'");
                vm.setCPUDemand(cfg.getLocation(vm).getCPUCapacity());
            }
        }

        return cfg;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int st) {
        this.step = st;
    }

}
