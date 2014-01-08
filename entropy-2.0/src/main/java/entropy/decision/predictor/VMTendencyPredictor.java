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

import entropy.configuration.VirtualMachine;
import entropy.decision.DecisionModule;

/**
 * @author Fabien Hermenier
 */
public class VMTendencyPredictor implements VMDemandPredictor {

    private double incValue;

    private double adaptDegree = 0.5;

    private int wasDemanded = -1;

    private int lastConsumption = -1;

    private int step;

    public VMTendencyPredictor(int st) {
        this.step = st;
    }

    @Override
    public void predictCPUDemand(VirtualMachine vm) {
        int current = vm.getCPUConsumption();
        int demand = 0;
        if (lastConsumption != -1) {
            if (current - lastConsumption < 0) { //Decreasing tendency
                vm.setCPUDemand(sample(current));
            } else if (lastConsumption - current < 0) { //Increasing tendency
                if (wasDemanded != -1) {
                    int theoricalInc = wasDemanded - lastConsumption;
                    int realIncVal = current - lastConsumption;
                    incValue = theoricalInc + (realIncVal - theoricalInc) * adaptDegree;
                } else {
                    incValue = current - lastConsumption;
                }
                demand = (int) (current + incValue);
                vm.setCPUDemand(sample(demand));

                wasDemanded = demand;
            }
        }
        //No demand = 0.
        if (vm.getCPUDemand() == 0) {
            vm.setCPUDemand(step);
        }
        DecisionModule.getLogger().debug("cpuDemand of '" + vm.getName()
                + " rounded to " + vm.getCPUDemand()
                + " (pure= " + demand + ", consumption=" + vm.getCPUConsumption() + ", step= " + step + ")");

        lastConsumption = vm.getCPUConsumption();
    }

    private int sample(int v) {
        return (v / step + 1) * step;
    }
}
