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

package gipad.plan.choco;

import gipad.configuration.CostFunction;
import gipad.configuration.configuration.Configuration;
import gipad.configuration.configuration.ConfigurationUtils;
import gipad.configuration.configuration.Node;
import gipad.configuration.configuration.VirtualMachine;
import gipad.exception.DurationEvaluationException;
import gipad.exception.MultipleResultingStateException;
import gipad.exception.NoAvailableTransitionException;
import gipad.exception.NonViableSourceConfigurationException;
import gipad.exception.PlanException;
import gipad.exception.UnknownResultingStateException;
import gipad.plan.Plan;
import gipad.plan.action.Action;
import gipad.plan.choco.actionmodel.MigratableActionModel;
import gipad.plan.choco.actionmodel.NodeActionModel;
import gipad.plan.choco.actionmodel.RunActionModel;
import gipad.plan.choco.actionmodel.StopActionModel;
import gipad.plan.choco.actionmodel.VirtualMachineActionModel;
import gipad.tools.DC;
import gipad.tools.ManagedElementList;
import gipad.tools.SimpleManagedElementList;
import gnu.trove.map.hash.TIntIntHashMap;
import gipad.plan.choco.actionmodel.slice.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import solver.Solver;
import solver.search.measure.IMeasures;
import solver.search.solution.Solution;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.VF;
import solver.variables.VariableFactory;
import gipad.configuration.*;
import gipad.configuration.configuration.*;
import gipad.configuration.configuration.Configuration;
import gipad.plan.*;
import gipad.plan.action.*;
import gnu.trove.map.hash.TIntIntHashMap;
import gipad.plan.action.Action;
import gipad.plan.choco.actionmodel.*;
import gipad.plan.choco.actionmodel.slice.*;
import gipad.plan.choco.constraints.CumulativeMultiDim;
import gipad.plan.choco.constraints.SatisfyDemandingSliceHeights;
import gipad.exception.*;
import gipad.tools.*;

import org.discovery.DiscoveryModel.model.Node;
import org.discovery.DiscoveryModel.model.VirtualMachine;

import solver.*;
import solver.constraints.ICF;
import solver.constraints.set.SCF;
import solver.variables.*;

/**
 * A CSP to model a reconfiguration plan composed of time bounded actions. In
 * this model, regarding to the current configuration and the sample destination
 * configuration, the model create the different actions that aims to perform
 * the transition to the destination configuration. In addition, several actions
 * acting on the placement of the virtual machines can be added.
 *
 * @author Fabien Hermenier
 */
public final class DefaultReconfigurationProblem implements ReconfigurationProblem {

    private ManagedElementList<VirtualMachine> manageable;

    /**
     * The maximum number of group of nodes.
     */
    public static final Integer MAX_NB_GRP = 1000;

    /**
     * The moment the reconfiguration starts. Equals to 0.
     */
    private IntVar<?> start;

    /**
     * The moment the reconfiguration ends. Variable.
     */
    private IntVar<?> end;

    /**
     * All the virtual machines' action to perform that implies regular actions.
     */
    private List<VirtualMachineActionModel> vmActions;

    /**
     * All the actions of the nodes that manage their state.
     */
    private List<NodeActionModel> nodesActions;

    /**
     * The source configuration.
     */
    private Configuration source;

    /**
     * The future running VMs.
     */
    private ManagedElementList<VirtualMachine> runnings;

    /**
     * The future waiting VMs.
     */
    private ManagedElementList<VirtualMachine> waitings;

    /**
     * The future sleeping VMs.
     */
    private ManagedElementList<VirtualMachine> sleepings;

    /**
     * The future terminated VMs.
     */
    private ManagedElementList<VirtualMachine> terminated;

    /**
     * The future online nodes.
     */
    private ManagedElementList<Node> onlines;

    /**
     * The future offline nodes.
     */
    private ManagedElementList<Node> offlines;

    /**
     * All the nodes managed by the model.
     */
    private Node[] nodes;

    private TIntIntHashMap revNodes;

    private Solver s;
    
    /**
     * A set model for each node.
     */
    private SetVar[] sets;

    /**
     * Cpu usage indexed by the index of the node.
     */
    private IntVar<?>[] cpuCapacities;
    

    /**
     * Mem usage indexed by the index of the node.
     */
    private IntVar<?>[] memCapacities;
    
    /**
     * Network input indexed by the index of the node.
     */
    private IntVar<?>[] netInCapacities;
    
    /**
     * Network output indexed by the index of the node.
     */
    private IntVar<?>[] netOutCapacities;

    /**
     * All the virtual machines managed by the model.
     */
    private VirtualMachine[] vms;

    private int[] currentLocation;

    private TIntIntHashMap revVMs;
    /**
     * The Cost Function
     */
    private CostFunction costFunc;

    /**
     * The group variable associated to each virtual machine.
     */
    private List<IntVar> vmGrp;

    /**
     * The group variable associated to each group of VMs.
     */
    private Map<ManagedElementList<VirtualMachine>, IntVar> vmsGrp;

    /**
     * The value associated to each group of nodes.
     */
    private Map<ManagedElementList<Node>, Integer> nodesGrp;

    /**
     * The groups associated to each node.
     */
    private List<List<Integer>> nodeGrps;

    /**
	 * The group of nodes associated to each identifier. To synchronize with
	 * nodesGrp.
     */
    private List<ManagedElementList<Node>> revNodesGrp;

    /**
     * The next value to use when creating a nodeGrp.
     */
    private int nextNodeGroupVal = 0;

    /**
     * All the consuming slices in the model.
     */
    private List<ConsumingSlice> consumingSlices;
    
    /**
     * All IncomingSlices
     */
    private List<IncomingSlice> incomingSlices;
    
    /**
     * All leavingSlice
     */
    
    private List<LeavingSlice> leavingSlices;
    
    /**
     * All the demanding slices in the model.
     */
    private List<DemandingSlice> demandingSlices;
    
    private SatisfyDemandingSliceHeights packing; //TODO 

    private int[] grpId; //The group ID of each node

    /**
     * Make a new model.
     *
	 * @param src
	 *            The source configuration. It must be viable.
	 * @param run
	 *            The set of virtual machines that must be running at the end of
	 *            the process
	 * @param wait
	 *            The set of virtual machines that must be waiting at the end of
	 *            the process
	 * @param sleep
	 *            The set of virtual machines that must be sleeping at the end
	 *            of the process
	 * @param stop
	 *            The set of virtual machines that must be terminated at the end
	 *            of the process
	 * @param manageable
	 *            the set of virtual machines to consider as manageable in the
	 *            problem
	 * @param on
	 *            The set of nodes that must be online at the end of the process
	 * @param off
	 *            The set of nodes that must be offline at the end of the
	 *            process
	 * @param eval
	 *            the evaluator to estimate the duration of an action.
	 * @throws entropy.plan.PlanException
	 *             if an error occurred while building the model
     */
	public DefaultReconfigurationProblem(Configuration src, ManagedElementList<VirtualMachine> run,
			ManagedElementList<VirtualMachine> wait, ManagedElementList<VirtualMachine> sleep,
			ManagedElementList<VirtualMachine> stop, ManagedElementList<VirtualMachine> manageable,
			ManagedElementList<Node> on, ManagedElementList<Node> off, CostFunction costFunc)
			throws PlanException {
        this.source = src;
        this.manageable = manageable;
        runnings = run;
        waitings = wait;
        sleepings = sleep;
        terminated = stop;
        onlines = on;
        offlines = off;
        this.costFunc = costFunc;

        this.checkDisjointSet();
        if (Configurations.currentlyOverloadedNodes(this.source).size() > 0) {
			throw new NonViableSourceConfigurationException(source, Configurations
					.currentlyOverloadedNodes(source).get(0));
        }

        start = VF.fixed(0,s);
        end = VF.bounded("end", 0, MAX_TIME, s);
        s.post(ICF.arithm(start, "<=", end));

	this.vms = source.getAllVirtualMachines().toArray(
				new VirtualMachine[source.getAllVirtualMachines().size()]);
        this.revVMs = new TIntIntHashMap(vms.length);
        for (int i = 0; i < vms.length; i++) {
            revVMs.put(vms[i].hashCode(), i);
        }
        ManagedElementList<Node> ns = source.getAllNodes();
        this.nodes = ns.toArray(new Node[ns.size()]);
        this.grpId = new int[ns.size()];
        this.revNodes = new TIntIntHashMap(ns.size());
        for (int i = 0; i < nodes.length; i++) {
            revNodes.put(nodes[i].hashCode(), i);
        }
        try {
            this.makeBasicActions(); 	// creation des actions possible pour chaque VM
					// En fonction de l'état actuel de la VM et de l'action qui est
					// demandé à la VM
        } catch (DurationEvaluationException e) {
            throw new PlanException(e.getMessage(), e);
        }
        this.makeResourcesCapacities(); 
		// creation de toutes les variables qui représentent les sommes de
		// comsommations sur chaque noeud
        //TODO
        this.vmGrp = new ArrayList<IntVar>(this.vms.length);
        for (int i = 0; i < vms.length; i++) {
            this.vmGrp.add(i, null);
        }
        this.vmsGrp = new HashMap<ManagedElementList<VirtualMachine>, IntVar>();
        this.nodeGrps = new ArrayList<List<Integer>>(this.nodes.length);
        for (int i = 0; i < this.nodes.length; i++) {
            this.nodeGrps.add(i, new LinkedList<Integer>());
        }
        this.nodesGrp = new HashMap<ManagedElementList<Node>, Integer>();
        this.revNodesGrp = new ArrayList<ManagedElementList<Node>>(MAX_NB_GRP);

	
        //notre cumulative colorée ici
        packing = new SatisfyDemandingSliceHeights(this);// new  SatisfyDemandingSlicesHeightsSimpleBP();
        //TODO: Uncomment for capacity
		/*
		 * if (!this.demandingSlices.isEmpty()) { this.makeSetModel(); }
		 */
        new SlicesPlanner().add(this);
    }
	//TODO
	public DefaultReconfigurationProblem(Configuration src, ManagedElementList<VirtualMachine> vms,
			CostFunction costFunc) throws PlanException  {

    	 this.source = src;
         this.manageable = vms;
         runnings = src.getRunnings();
         waitings = new SimpleManagedElementList<VirtualMachine>(); // no vm  waiting
         sleepings = new SimpleManagedElementList<VirtualMachine>(); // no vm
         terminated = new SimpleManagedElementList<VirtualMachine>(); 
         onlines = src.getOnlines();
         offlines = new SimpleManagedElementList<Node>();
         this.costFunc = costFunc;
         
         this.checkDisjointSet();
         if (ConfigurationUtils.getOverloadedNodes(this.source).size() > 0) {
			throw new NonViableSourceConfigurationException(source, ConfigurationUtils
					.getOverloadedNodes(source).get(0));
         }
	}

   /**
     * Make a set model. On set per node, that indicates the VMs it will run
     */
    private void makeSetModel() {

        if (this.sets == null) {
            //A set variable for each future online nodes
            this.sets = new SetVar[nodes.length];

            for (int i = 0; i < sets.length; i++) {
                Node n = nodes[i];
                SetVar s = VF.set("host(" + n.name() + ")", 0, demandingSlices.size() - 1,this.s);
                sets[i] = s;
            }

            //Make the channeling with the assignment variable of all the d-slices

            IntVar<?>[] assigns = SliceUtils.extractHosters(demandingSlices.toArray(new Slice[demandingSlices.size()]));
            //TODO verify : post(new InverseSetInt(assigns, sets));
            s.post(SCF.int_channel(sets, assigns, 0, 0));
        }
    }

    public SatisfyDemandingSliceHeights getSatisfyDSlicesHeightConstraint() {
        return this.packing;
    }

    /**
     * Set the resources capacity of the nodes.
     */
    private void makeResourcesCapacities() {
		this.cpuCapacities = new IntVar[nodes.length];
		this.memCapacities = new IntVar[nodes.length];
		this.netInCapacities = new IntVar[nodes.length];
		this.netOutCapacities = new IntVar[nodes.length];

		ManagedElementList<Node> involvedNodes = new SimpleManagedElementList<Node>();
		for (Node n : getFutureOfflines()) {
			NodeActionModel action = getAssociatedAction(n);
			if (action != null) {
				involvedNodes.add(n);
			}
		}
		involvedNodes.addAll(getFutureOnlines());
		for (Node n : involvedNodes) {
			cpuCapacities[getNode(n)] = VF.bounded(n.name()+"#cpuCapacity", 0, DC.getSumCPu(n), getSolver());
			memCapacities[getNode(n)] = VF.bounded(n.name()+"#memCapacity", 0, (int)n.hardwareSpecification().memory().capacity(), getSolver());
			netInCapacities[getNode(n)] = VF.bounded(n.name()+"#netInCapacity", 0, n.hardwareSpecification().networkInterfaces().get(0), s);//FIXME : getCapa
			netOutCapacities[getNode(n)] = VF.bounded(n.name()+"#netOutCapacity", 0, n.hardwareSpecification().networkInterfaces().get(0), s);//FIXME : getCapa

		}
    }

    /**
     * Check all the nodes belong to only one set.
     *
     * @throws gipad.exception.UnknownResultingStateException
     *          if the state of an element is not defined
     * @throws gipad.exception.MultipleResultingStateException
     *          if an element has two state
     */
	private void checkDisjointSet() throws UnknownResultingStateException,
			MultipleResultingStateException {
        for (Node n : getSourceConfiguration().getAllNodes()) {
            boolean inOnlines = this.getFutureOnlines().contains(n);
            boolean inOfflines = this.getFutureOfflines().contains(n);
            if (inOnlines && inOfflines) {
                throw new MultipleResultingStateException(n, getFutureOnlines(), getFutureOfflines());
            } else if (!inOnlines && !inOfflines) {
                throw new UnknownResultingStateException(n);
            }
        }

        for (VirtualMachine vm : getSourceConfiguration().getAllVirtualMachines()) {
            int nbIn = this.getFutureRunnings().contains(vm) ? 1 : 0;
			if (this.getFutureWaitings().contains(vm) || this.getFutureSleepings().contains(vm)
                    || this.getFutureTerminated().contains(vm)) {
                nbIn++;
            }
            if (nbIn == 0) {
                throw new UnknownResultingStateException(vm);
            } else if (nbIn > 1) {
				throw new MultipleResultingStateException(vm, getFutureRunnings(), getFutureSleepings(),
						getFutureWaitings(), getFutureTerminated());
            }
        }
    }

    @Override
    public Node[] getNodes() {
        return nodes;
    }

    @Override
    public VirtualMachine[] getVirtualMachines() {
        return vms;
    }

    @Override
    public Configuration getSourceConfiguration() {
        return this.source;
    }

    @Override
    public ManagedElementList<VirtualMachine> getFutureRunnings() {
        return this.runnings;
    }

    @Override
    public ManagedElementList<VirtualMachine> getFutureWaitings() {
        return this.waitings;
    }

    @Override
    public ManagedElementList<VirtualMachine> getFutureSleepings() {
        return this.sleepings;
    }

    @Override
    public ManagedElementList<VirtualMachine> getFutureTerminated() {
        return this.terminated;
    }

    @Override
    public ManagedElementList<Node> getFutureOnlines() {
        return this.onlines;
    }

    @Override
    public ManagedElementList<Node> getFutureOfflines() {
        return this.offlines;
    }

    @Override
    public IntVar<?> getStart() {
        return this.start;
    }

    @Override
    public IntVar<?> getEnd() {
        return this.end;
    }

    @Override
    public int getVirtualMachine(VirtualMachine vm) {
        int h = vm.hashCode();
        if (!revVMs.containsKey(h)) {
            return -1;
        }
        return revVMs.get(h);
    }

    @Override
    public VirtualMachine getVirtualMachine(int idx) {
        if (idx < vms.length && idx >= 0) {
            return vms[idx];
        }
        return null;
    }

    @Override
    public int getNode(Node n) {
        int h = n.hashCode();
        if (!revNodes.containsKey(h)) {
            return -1;
        }
        return revNodes.get(h);
    }

    @Override
    public Node getNode(int idx) {
        if (idx < nodes.length && idx >= 0) {
            return nodes[idx];
        }
        return null;
    }

    /**	FIXME duration evaluator
	 * Create all the basic action that manipulate the state of the virtual
	 * machine and the nodes. creation de l'arraylist qui contient l'ensemble
	 * des actions possibles pour chaque VM
	 * 
     * @throws entropy.plan.NoAvailableTransitionException
     *          if the VM can not be running regarding to its current state
     */
    private void makeBasicActions() throws DurationEvaluationException, NoAvailableTransitionException {

        //make the actions for the VMs
        this.vmActions = new ArrayList<VirtualMachineActionModel>(vms.length);
        for (int i = 0; i < vms.length; i++) {
            this.vmActions.add(i, null);
        }

        //for (VirtualMachine vm : getFutureRunnings()) {
        this.currentLocation = new int[vms.length];
        for (int i = 0; i < runnings.size(); i++) {
            VirtualMachine vm = runnings.get(i);
            boolean dyn = manageable.contains(vm);
            VirtualMachineActionModel a;
            if (this.source.isRunning(vm)) {
                currentLocation[getVirtualMachine(vm)] = getNode(source.getLocation(vm));
                a = new MigratableActionModel(this, vm, durationEval.evaluateMigration(vm), dyn);
            } else if (this.source.isSleeping(vm)) {
				a = new ResumeActionModel(this, vm, durationEval.evaluateLocalResume(vm),
						durationEval.evaluateRemoteResume(vm));
                currentLocation[getVirtualMachine(vm)] = getNode(source.getLocation(vm));
            } else if (this.source.isWaiting(vm)) {
                currentLocation[getVirtualMachine(vm)] = -1;
                a = new RunActionModel(this, vm, durationEval.evaluateRun(vm));
            } else {
                throw new NoAvailableTransitionException(vm, "terminated", "running");
            }
            vmActions.set(getVirtualMachine(vm), a);
        }
        for (int i = 0; i < waitings.size(); i++) {
            VirtualMachine vm = waitings.get(i);
//        for (VirtualMachine vm : getFutureWaitings()) {
            if (!this.source.isWaiting(vm)) {
                if (this.source.isRunning(vm)) {
                    throw new NoAvailableTransitionException(vm, "running", "waiting");
                } else if (this.source.isSleeping(vm)) {
                    throw new NoAvailableTransitionException(vm, "sleeping", "waiting");
                } else {
                    throw new NoAvailableTransitionException(vm, "terminated", "waiting");
                }
            }
        }
        for (int i = 0; i < sleepings.size(); i++) {
            VirtualMachine vm = sleepings.get(i);
//        for (VirtualMachine vm : getFutureSleepings()) {
            if (this.source.isRunning(vm)) {
				VirtualMachineActionModel a = new SuspendActionModel(this, vm,durationEval.evaluateLocalSuspend(vm));
                vmActions.set(getVirtualMachine(vm), a);
            } else if (this.source.isWaiting(vm)) {
                throw new NoAvailableTransitionException(vm, "waiting", "sleeping");
            } else if (!this.source.isSleeping(vm)) {
                throw new NoAvailableTransitionException(vm, "terminated", "sleeping");
            }
        }
        for (int i = 0; i < terminated.size(); i++) {
            VirtualMachine vm = terminated.get(i);
            //for (VirtualMachine vm : getFutureTerminated()) {
            if (this.source.isRunning(vm)) {
				VirtualMachineActionModel a = new StopActionModel(this, vm,durationEval.evaluateStop(vm));
                vmActions.set(getVirtualMachine(vm), a);
            } else if (this.source.isSleeping(vm)) {
                throw new NoAvailableTransitionException(vm, "sleeping", "terminated");
            } else if (this.source.isWaiting(vm)) {
                throw new NoAvailableTransitionException(vm, "sleeping", "waiting");
            }
        }

        //Make the actions for the nodes

        this.nodesActions = new ArrayList<NodeActionModel>(nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            this.nodesActions.add(i, null);
        }

        for (Node n : getFutureOnlines()) {
            if (getSourceConfiguration().getOfflines().contains(n)) {
                BootNodeActionModel a = new BootNodeActionModel(this, n, durationEval.evaluateStartup(n));
                nodesActions.set(getNode(a.getNode()), a);
            }
        }
        for (Node n : getFutureOfflines()) {
            if (getSourceConfiguration().getOnlines().contains(n)) {
				ShutdownNodeActionModel a = new ShutdownNodeActionModel(this, n,
						durationEval.evaluateShutdown(n));
                nodesActions.set(getNode(a.getNode()), a);
            } else {
                StayOfflineNodeActionModel a = new StayOfflineNodeActionModel(this, n);
				/*
				 * ShutdownNodeActionModel a = new ShutdownNodeActionModel(this,
				 * n, durationEval.evaluateShutdown(n)); try {
				 * a.start().setVal(0); } catch (Exception e) {
				 * Plan.logger.error(e.getMessage(), e); }
				 */
                //a.start().setLowB(0);
                //a.start().setUppB(0);
                nodesActions.set(getNode(a.getNode()), a);
            }
        }

        //Get all the slices

        this.demandingSlices = new ArrayList<DemandingSlice>();
        this.demandingSlices.addAll(ActionModelUtils.extractDemandingSlices(getVirtualMachineActions()));
        this.demandingSlices.addAll(ActionModelUtils.extractDemandingSlices(getNodeMachineActions()));

        this.consumingSlices = new ArrayList<ConsumingSlice>();
        this.consumingSlices.addAll(ActionModelUtils.extractConsumingSlices(getVirtualMachineActions()));
        this.consumingSlices.addAll(ActionModelUtils.extractConsumingSlices(getNodeMachineActions()));
    }

    @Override
    public List<VirtualMachineActionModel> getVirtualMachineActions() {
        List<VirtualMachineActionModel> actions = new ArrayList<VirtualMachineActionModel>();
        for (VirtualMachineActionModel a : vmActions) {
            if (a != null) {
                actions.add(a);
            }
        }
        return actions;
    }

    public VirtualMachineActionModel getAssociatedVirtualMachineAction(int idxVM) {
        return vmActions.get(idxVM);
    }

    @Override
    public VirtualMachineActionModel getAssociatedAction(VirtualMachine vm) {
        int idx = getVirtualMachine(vm);
        return vmActions.get(idx);
    }

    @Override
    public List<NodeActionModel> getNodeMachineActions() {
        List<NodeActionModel> actions = new ArrayList<NodeActionModel>();
        for (NodeActionModel a : nodesActions) {
            if (a != null) {
                actions.add(a);
            }
        }
        return actions;
    }

    @Override
    public NodeActionModel getAssociatedAction(Node n) {
        return this.nodesActions.get(getNode(n));
    }

    @Override
    public IntVar<?> getFreeCPU(Node n) {
        return this.cpuCapacities[getNode(n)];
    }

    @Override
    public IntVar<?> getFreeMem(Node n) {
        return this.memCapacities[getNode(n)];
    }

    @Override
    public IntVar<?> getVMGroup(ManagedElementList<VirtualMachine> vms) {
        IntVar<?> v = this.vmsGrp.get(vms);
        if (v != null) {
            return v;
        }
        v = VF.enumerated("vmset" + vms.toString(), 0, MAX_NB_GRP, s);
        for (VirtualMachine vm : vms) {
            this.vmGrp.set(getVirtualMachine(vm), v);
        }
        this.vmsGrp.put(vms, v);
        return v;
    }

    @Override
    public IntVar<?> makeGroup(ManagedElementList<VirtualMachine> vms, List<ManagedElementList<Node>> nodes) {
        int[] values = new int[nodes.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = getGroup(nodes.get(i));
        }
        IntVar<?> v = VF.enumerated("vmset" + vms.toString(), /*0, MAX_NB_GRP*/values,s);
        this.vmsGrp.put(vms, v);
        //System.err.println(vmsGrp.size());
        return v;
    }

    @Override
    public IntVar<?> getAssociatedGroup(VirtualMachine vm) {
        return this.vmGrp.get(getVirtualMachine(vm));
    }

    @Override
    public Set<ManagedElementList<VirtualMachine>> getVMGroups() {
        return this.vmsGrp.keySet();
    }

    @Override
    public int getGroup(ManagedElementList<Node> nodes) {
        if (this.nodesGrp.get(nodes) != null) {
            return this.nodesGrp.get(nodes);
        } else {
            if (nextNodeGroupVal > MAX_NB_GRP) {
                return -1;
            }
            int v = nextNodeGroupVal++;
            this.nodesGrp.put(nodes, v);
            this.revNodesGrp.add(v, nodes);
            for (Node n : nodes) {
                List<Integer> l = this.nodeGrps.get(getNode(n));
                l.add(v);

				/*
				 * if (grpId[getNode(n)] != 0) {
				 * Plan.logger.error("Node group has changed"); return -1; }
				 */
                grpId[getNode(n)] = v;
            }
            //Set the group of the nodes
            return v;
        }
    }

    @Override
    public Set<ManagedElementList<Node>> getNodesGroups() {
        return this.nodesGrp.keySet();
    }

    @Override
    public List<Integer> getAssociatedGroups(Node n) {
        return this.nodeGrps.get(getNode(n));
    }

    @Override
	public int[] getNodesGroupId() {
        return this.grpId;
    }

    @Override
    public ManagedElementList<Node> getNodeGroup(int idx) {
        return this.revNodesGrp.get(idx);
    }

    @Override
    public DurationEvaluator getDurationEvaluator() {
        return this.durationEval;
    }

    @Override
    public List<DemandingSlice> getDemandingSlices() {
        return this.demandingSlices;
    }

    @Override
    public List<ConsumingSlice> getConsumingSlice() {
        return this.consumingSlices;
    }

    @Override
    public List<VirtualMachineActionModel> getAssociatedActions(ManagedElementList<VirtualMachine> vms) {
        List<VirtualMachineActionModel> l = new LinkedList<VirtualMachineActionModel>();
        for (VirtualMachine vm : vms) {
            VirtualMachineActionModel a = getAssociatedAction(vm);
            if (a != null) {
                l.add(a);
            }
        }
        return l;
    }

    public int getLocation(int vmIdx) {
        return currentLocation[vmIdx];
    }

    @Override
    public SetVar[] getSetModels() {
		return new SetVar[0]; // To change body of implemented methods use File
								// | Settings | File Templates.
    }

    @Override
    public SetVar getSetModel(Node n) {
        if (sets == null) {
            makeSetModel();
        }

        int idx = getNode(n);
        if (idx < 0) {
            return null;
        }
        return sets[idx];
    }

    @Override
    public SConstraint<IntVar> implies(SConstraint<IntVar> c1, SConstraint<IntVar> c2) {
        //implies: or(not(c1),c2)

        IntVar bC1 = createBooleanVar("bC1");
        post(ReifiedFactory.builder(bC1, c1, this));

        IntVar bC2 = createBooleanVar("bC2");
        post(ReifiedFactory.builder(bC2, c2, this));

        SConstraint cNotC1 = BooleanFactory.not(bC1);
        IntVar bNotC1 = createBooleanVar("!c1");
        post(ReifiedFactory.builder(bNotC1, cNotC1, this));

        return BooleanFactory.or(getEnvironment(), bNotC1, bC2);
    }

    @Override
    public SConstraint<IntVar> implies(IntVar b1, SConstraint<IntVar> c2) {
        //implies: or(not(c1),c2)

        IntVar bC2 = createBooleanVar("bC2");
        post(ReifiedFactory.builder(bC2, c2, this));

        IntVar notB1 = createBooleanVar("!b1");
        post(neq(b1, notB1));

        return BooleanFactory.or(getEnvironment(), notB1, bC2);
    }

    @Override
    public SConstraint ifOnlyIf(IntVar b1, SConstraint c2) {

        //and(or(b1, non b2), or(non b1, b2))
        IntVar notBC1 = createBooleanVar("!(" + b1.pretty() + ")");
        post(neq(b1, notBC1));

        IntVar bC2 = createBooleanVar("boolean(" + c2.pretty() + ")");
        post(ReifiedFactory.builder(bC2, c2, this));

        IntVar notBC2 = createBooleanVar("!(" + c2.pretty() + ")");
        post(neq(notBC2, bC2));

        IntVar or1 = createBooleanVar("or1");
        post(ReifiedFactory.builder(or1, BooleanFactory.or(getEnvironment(), b1, notBC2), this));

        IntVar or2 = createBooleanVar("or2");
        post(ReifiedFactory.builder(or2, BooleanFactory.or(getEnvironment(), notBC1, bC2), this));

        return BooleanFactory.and(or1, or2);
    }

    @Override
    public TimedReconfigurationPlan extractSolution() {
        //TODO: check if solution is found
        //Configuration dst = extractConfiguration();
		DefaultTimedReconfigurationPlan plan = new DefaultTimedReconfigurationPlan(
				getSourceConfiguration());
        for (NodeActionModel action : getNodeMachineActions()) {
            if (action instanceof BootNodeActionModel) {
                Action a = action.getDefinedAction(this);
                if (a != null) {
                    if (!plan.add(action.getDefinedAction(this))) {
                        Plan.logger.warn("Action " + a + " is not added into the plan");
                    }
                } else {
                    Plan.logger.debug("No resulting action for " + action);
                }
            }
        }
        for (VirtualMachineActionModel action : getVirtualMachineActions()) {
            Action a = action.getDefinedAction(this);
            if (a != null) {
                plan.add(a);
            }
        }
        for (Action a : plan) {
            if (a.getStartMoment() == a.getFinishMoment()) {
                Plan.logger.warn("Action " + a + " has a duration equals to 0");
            }
        }

        for (NodeActionModel action : getNodeMachineActions()) {
            if (action instanceof ShutdownNodeActionModel) {
                Action a = action.getDefinedAction(this);
                if (a != null) {
                    if (!plan.add(action.getDefinedAction(this))) {
                        Plan.logger.warn("Action " + a + " is not added into the plan");
                    }
				}/*
				 * else { Plan.logger.debug("No resulting action for " +
				 * action); }
				 */
            }
        }
        if (plan.getDuration() != end.getValue()) {
			Plan.logger.error("Theoretical duration (" + getEnd().getValue() + ") and plan duration "
					+ plan.getDuration() + " mismatch");
            return null;
        }
        return plan;
    }

//    @Override
//    public List<SolutionStatistics> getSolutionsStatistics() {
//        List<SolutionStatistics> stats = new LinkedList<SolutionStatistics>();
//        for (Solution s : this.getSearchStrategy().getStoredSolutions()) {
//            IMeasures m = s.getMeasures();
//            SolutionStatistics st;
//            if (m.getObjectiveValue() != null) {
//				st = new SolutionStatistics(m.getNodeCount(), m.getBackTrackCount(), m.getTimeCount(),
//						this.isEncounteredLimit(), m.getObjectiveValue().intValue());
//            } else {
//				st = new SolutionStatistics(m.getNodeCount(), m.getBackTrackCount(), m.getTimeCount(),
//                        this.isEncounteredLimit());
//            }
//            stats.add(st);
//        }
//        Collections.sort(stats, SolutionStatisticsComparator);
//        return stats;
//    }

    /*FIXME @Override
     public SolvingStatistics getSolvingStatistics() {
		return new SolvingStatistics(this.getNodeCount(), this.getBackTrackCount(), this.getTimeCount(),
                this.isEncounteredLimit());
    }*/

//    private static Comparator SolutionStatisticsComparator = new Comparator<SolutionStatistics>() {
//
//        @Override
//        public int compare(SolutionStatistics sol1, SolutionStatistics sol2) {
//            if (sol1.getTimeCount() == sol2.getTimeCount()) {
//                //Compare wrt. the number of nodes or backtracks
//                if (sol1.getNbNodes() == sol2.getTimeCount()) {
//                    return sol1.getNbBacktracks() - sol2.getNbBacktracks();
//                }
//                return sol1.getNbNodes() - sol2.getNbNodes();
//            }
//            return sol1.getTimeCount() - sol2.getTimeCount();
//        }
//    };

	@Override
	public Solver getSolver() {
		return s;
	}
	
	public IntVar[] getCpuCapacities() {
	     return cpuCapacities;
	}
	
	public IntVar[] getMemCapacities() {
	    return memCapacities;
	}
	public IntVar[] getNetInCapacities() {
	    return netInCapacities;
	}

	public IntVar[] getNetOutCapacities() {
	    return netOutCapacities;
	}
}
