package gipad.plan.choco.actionmodel;

import solver.constraints.ICF;
import solver.constraints.LCF;
import solver.variables.IntVar;
import solver.variables.VF;
import gipad.configuration.configuration.*;
import gipad.plan.action.Migration;
import gipad.plan.choco.ReconfigurationProblem;
import gipad.plan.choco.actionmodel.slice.*;


public class MigratableActionModel extends VirtualMachineActionModel {

    /**
     * Make a new action.
     *
     * @param model    the model
     * @param vm       the virtual machine to make moveable
     * @param d        the duration of the migration if it is performed
     * @param moveable {@code true} to indicates the VM can be migrated
     */
   
    public MigratableActionModel(ReconfigurationProblem model, VirtualMachine vm, Configuration conf) {
    	super(vm);
        super.conf = conf;
        super.cSlice = new ConsumingSlice(model, "mig(" + vm.name() + ")", vm ,conf.getConsuming(vm), conf);
        super.lSlice = new LeavingSlice(model, "mig(" + vm.name() + ")", vm, conf.getLeaving(vm), conf);
        super.iSlice = new IncomingSlice(model, "mig(" + vm.name() + ")", conf.getIncoming(vm), conf);
        super.dSlice = new DemandingSlice(model, "mig(" + vm.name() + ")", conf.getDemanding(vm), conf);
        
        cSlice.addToModel(model);
        lSlice.addToModel(model);
        iSlice.addToModel(model);
        dSlice.addToModel(model);
        
        //les slices d'actions commencent où la phase de consommation initiale se termine
        lSlice.setStart(cSlice.getEnd());
        iSlice.setStart(cSlice.getEnd());
        //les slices d'actions se terminent là où la phase de consommation finale commence
        lSlice.setEnd(dSlice.getStart());
        iSlice.setEnd(dSlice.getStart());
        //la migration part de la machine hote initiale et arrive sur la machine finale
        cSlice.setHoster(lSlice.hoster());
        dSlice.setHoster(iSlice.hoster());        
        //Les deux machines initiales et finales communiquent uniquement entre elle pour réaliser l'action de migration.
        lSlice.setBwInput(iSlice.getBwOutput());
        iSlice.setBwInput(lSlice.getBwOutput());
        //La durée de migration en sortie est identique à celle en entrée des machines physiques
        lSlice.setDuration(iSlice.duration());       
        //La durée de la migration dépend de la taille de la VM et de la taille de la bande passante entre les deux noeuds
        //duration * bw = migrationCapacity 
        int currentNode = conf.getLocation(vm).getId();
        for(Node n : conf.getAllNodes()){
        	if(n.getId() == currentNode){
        	model.getSolver().post(LCF.ifThen(ICF.arithm(iSlice.hoster(), "=", currentNode), 
            			ICF.arithm(iSlice.duration(), "=", 0)));
        	}
        	else{
        	//TODO pas de prise en compte de la notion d'activité sur la machine
        	model.getSolver().post(LCF.ifThen(ICF.arithm(iSlice.hoster(), "=", n.getId()), 
        			ICF.times(iSlice.duration(), iSlice.getBwInput(), VF.fixed(conf.getMigrationCapacity(vm, n.getId()), model.getSolver()))));
        	}
        }
        
        //La durée de l'action globale sur cette VM est égale à la somme des durées de chaque action.
        super.duration = VF.enumerated("stop_dur(" + vm.name() + ")", 0, model.MAX_TIME ,model.getSolver());
        model.getSolver().post(ICF.sum(new IntVar[]{cSlice.duration(), lSlice.duration(), iSlice.duration(), dSlice.duration()}, super.duration));
    }

    /**
     * Get the moment the action ends. The action ends at the moment
     * the slice on the source node ends.
     *
     * @return <code>getConsumingSlice().end()</code>
     */
    @Override
    public final IntVar end() {
        return this.getConsumingSlice().getEnd();
    }

    /**
     * Get the moment the action starts. The action starts at the moment
     * the slice on the source node starts.
     *
     * @return <code>getDemandingSlice().start()</code>
     */
    @Override
    public final IntVar start() {
        return this.getDemandingSlice().getStart();
    }

    /**
     * Return the migration action if the VM have to move.
     *
     * @return a Migration if the source node and the destination node are different. null otherwise
     */
    @Override
    public Migration getDefinedAction(ReconfigurationProblem solver) {
        if (getConsumingSlice().hoster().getValue()
                != getDemandingSlice().hoster().getValue()) {
            return new Migration(getVirtualMachine(),
                    solver.getNode(getConsumingSlice().hoster().getValue()),
                    solver.getNode(getDemandingSlice().hoster().getValue()),
                    start().getValue(),
                    end().getValue());
        }
        return null;
    }


    @Override
    public String toString() {
        return "migration(" + getVirtualMachine().name() + ")";
    }

    @Override
    public IntVar getGlobalCost() {
        return iSlice.duration();
    }

	@Override
	public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
		cfg.setRunOn(getVirtualMachine(), solver.getNode(dSlice.hoster().getValue()));
		return true;
	}
}
