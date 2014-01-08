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
package entropy.decision.vjobScheduler;

import java.util.ArrayList;
import java.util.List;

import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.SimpleConfiguration;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.decision.AssignmentException;
import entropy.decision.DecisionModule;
import entropy.vjob.VJob;
import entropy.vjob.queue.VJobsPool;

/**
 * A VJob scheduler based on the algorithm First Fit.
 * The scheduler browse the queue. For each vjob, it tests
 * if a viable configuration exists. If it suceeds at assigning
 * all the VMs composing the vjob, then the VJobs will be in the running
 * state. Otherwise, depending on the current state of the job, it will
 * be in the waiting state or in the sleeping state.
 * All the VMs that do not belong to a VJobs will be stopped.
 *
 * @author Fabien Hermenier
 */
public class FirstFitVJobScheduler extends DecisionModule {

    /**
     * The queue.
     */
    private VJobsPool queue;

    /**
     * Make a new scheduler.
     *
     * @param q the queue of VJobs.
     */
    public FirstFitVJobScheduler(VJobsPool q) {
        this.queue = q;
    }

    /**
     * Compute a new configuration using the FirstFit algorithm
     *
     * @param current the current configuration
     * @return a new viable configuration that indicated the states of the VMs
     * @throws AssignmentException if an error occurs.
     */
    @Override
    public Configuration compute(Configuration current) throws AssignmentException {
        // TODO Auto-generated method stub

        //Scan the submitted vjobs
        List<VJob> declaredVJobs = queue.getRunningPriorities();

        ManagedElementSet<Node> tmpNodes = new SimpleManagedElementSet<Node>();
        Configuration newConf = new SimpleConfiguration();
        for (Node n : current.getOnlines()) {
            Node copy = n.clone();
            tmpNodes.add(copy);
            newConf.addOnline(copy);
        }
        List<VirtualMachine> toAssign = new ArrayList<VirtualMachine>();
        for (VJob l2 : declaredVJobs) {
            for (VirtualMachine vm : l2.getVirtualMachines()) {
                if (current.isRunning(vm)) {
                    toAssign.add(current.getRunnings().get(vm.getName()));
                } else if (current.isWaiting(vm)) {
                    toAssign.add(current.getWaitings().get(vm.getName()));
                } else if (current.isSleeping(vm)) {
                    toAssign.add(current.getSleepings().get(vm.getName()));
                } else {
                    throw new AssignmentException("Unable to get the VM '" + vm.getName() + "' from the configuration");
                }
            }
            Configuration c = new SimpleConfiguration();
            tmpNodes = new SimpleManagedElementSet<Node>();
            for (Node n : current.getOnlines()) {
                Node copy = n.clone();
                tmpNodes.add(copy);
                c.addOnline(copy);
            }
            boolean success = true;
            for (VirtualMachine vm : toAssign) {
                for (Node node : tmpNodes) {
                    if (vm.getCPUConsumption() <= node.getCPUCapacity()
                            && vm.getMemoryConsumption() <= node.getMemoryCapacity()) {
                        c.setRunOn(vm.clone(), newConf.getOnlines().get(node.getName()));
                        node.setCPUCapacity(node.getCPUCapacity() - vm.getCPUConsumption());
                        node.setMemoryCapacity(node.getMemoryCapacity() - vm.getMemoryConsumption());
                        break;
                    }
                }
                if (!c.getRunnings().contains(vm)) {
                    success = false;
                    break;
                }
            }
            if (!success) {
                getLogger().debug("Fail at assigning '" + l2.id() + "'");
                toAssign.removeAll(l2.getVirtualMachines());
                for (VirtualMachine vm : l2.getVirtualMachines()) {
                    Node node = c.getLocation(vm);
                    //Old state ?

                    //Was running
                    if (current.getRunnings().contains(vm)) {
                        //Go to sleep on the local node.
                        newConf.setSleepOn(vm, current.getLocation(vm));
                    } else if (current.getSleepings().contains(vm)) {
                        //Still sleeping on its old location
                        newConf.setSleepOn(vm, current.getLocation(vm));
                    } else {
                        //Still waiting
                        newConf.addWaiting(vm);
                    }
                    if (node != null) {
                        tmpNodes.get(node.getName()).setCPUCapacity(node.getCPUCapacity() + vm.getCPUConsumption());
                        tmpNodes.get(node.getName()).setMemoryCapacity(node.getMemoryCapacity() + vm.getMemoryConsumption());
                    }
                }
            } else {
                getLogger().debug("Succeed at assigning '" + l2.id() + "'");
                for (VirtualMachine vm : l2.getVirtualMachines()) {
                    newConf.setRunOn(vm, c.getLocation(vm));
                }
            }

        }

        return newConf;
    }

    /*private Configuration reduceWithCP(Configuration c) {
         VirtualMachineAssignmentModel model = new VirtualMachineAssignmentModel(c);
         for (VirtualMachine vm : c.getRunnings()) {
             model.mustBeRunning(vm);
         }
         for (VirtualMachine vm : c.getWaitings()) {
             model.mustBeOnFarm(vm);
             //Store informations about their consumption
             this.suspendedInfos.remove(vm);
             this.suspendedInfos.add(vm);
         }

         for (VirtualMachine vm : c.getSleepings()) {
             model.mustBeOnFarm(vm);
         }

         model.lazyMaintainResourcesAccessibility();

         VirtualMachineAssignmentSolver solver = new VirtualMachineAssignmentSolver();
         solver.read(model);
         //solver.setVirtualMachineSelector(new BiggestUnassignedVirtualMachine(solver, solver.getVar(nodes), VirtualMachine.CPU_CONSUMPTION));
         //solver.setNodeSelector(new BusiestNode(solver, solver.getVar(nodes), Node.CPU_CAPACITY, true));
         solver.setVirtualMachineSelector(new BiggestVirtualMachinesFirst(solver, VirtualMachine.CPU_CONSUMPTION));
         solver.setNodeSelector(new StayFirstNodeSelector(solver));
         if (solver.solve(this.getTimeout())) {
             return solver.getResultingConfiguration();
         }
         return solver.getResultingConfiguration();
     }   */
}
