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

package entropy.controlLoop;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import entropy.configuration.Configuration;
import entropy.configuration.Configurations;
import entropy.configuration.DefaultManagedElementSet;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.decision.DecisionModule;
import entropy.vjob.Ban;
import entropy.vjob.PlacementConstraint;
import entropy.vjob.Spread;
import entropy.vjob.VJob;

/**
 * Analyse continuously the state of the infrastructure and reveals the
 * number of inconsistency
 *
 * @author Fabien Hermenier
 */
public class StateAnalyzer {

    private String output;

    private long start;


    public StateAnalyzer(String file) {
        this.output = file;
        start = System.currentTimeMillis();
    }

    public void analyze(Configuration cfg, List<VJob> vjobs, boolean isReconfiguring) {

        int nbVMs = cfg.getAllVirtualMachines().size();

        ManagedElementSet<VirtualMachine> in = new DefaultManagedElementSet<VirtualMachine>();
        //Amount of VMs that have resources contention issues
        //ie current host does not has a sufficient amount of ressources to
        //satisfy all the VMs
        int nbWithContentions = 0;
        ManagedElementSet<Node> overloaded = Configurations.futureOverloadedNodes(cfg);
        for (Node n : overloaded) {
            nbWithContentions += cfg.getRunnings(n).size();
            in.addAll(cfg.getRunnings(n));
        }

        //Amount of waiting VMs (we consider this as a constraint violation)
        int nbOfflines = cfg.getWaitings().size();

        //Amount of VMs involved in unsatisfied placement constraints
        ManagedElementSet<VirtualMachine> vms = new DefaultManagedElementSet<VirtualMachine>();
        for (VJob vjob : vjobs) {
            for (PlacementConstraint c : vjob.getConstraints()) {
                if (!c.isSatisfied(cfg)) {
                    if (c.getClass().equals(Ban.class)) {
                        for (VirtualMachine vm : ((Ban) c).getMisPlaced(cfg)) {
                            if (!in.contains(vm)) {
                                vms.add(vm);
                            }
                        }
                    } else if (c instanceof Spread) {
                        for (VirtualMachine vm : ((Spread) c).getMisPlaced(cfg)) {
                            if (!in.contains(vm)) {
                                vms.add(vm);
                            }
                        }
                    } else {
                        DecisionModule.getLogger().info(c + " is not satisfied, but ignored by StateAnalyzer");
                    }
                }
            }
        }
        int nbMisPlaced = vms.size();
        try {
            this.store(nbVMs, nbWithContentions, nbMisPlaced, nbOfflines, isReconfiguring);
        } catch (IOException e) {
            DecisionModule.getLogger().error("Unable to store analyse into '" + output + "'");
        }
    }


    private void store(int nbVMs, int nbWithContentions, int nbMisPlaced, int nbOfflines, boolean isReconfiguring) throws IOException {
        BufferedWriter out = null;
        try {
            long current = System.currentTimeMillis();

            out = new BufferedWriter(new FileWriter(output, true));
            long time = (current - start) / 1000;
            out.write(time + " "
                    + nbVMs + " "
                    + nbWithContentions + " "
                    + nbMisPlaced + " "
                    + nbOfflines + " "
                    + (isReconfiguring ? "1" : "0")
                    + "\n"
            );
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
