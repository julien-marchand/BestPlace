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
package gipad.plan.choco.actionmodel;

import gipad.plan.action.Migration;
import gipad.plan.choco.ReconfigurationProblem;
import gipad.plan.choco.actionmodel.slice.*;


/**
 * Model a action that may potentially migrate a VM.
 * The action is modeled with one consuming slice and one demanding slice.
 * If the demanding slice is hosted on a different node than the consuming slice
 * it will result in a migration. In this case, the action starts at the beginning of
 * the demanding slice and ends at the end of the consuming slice. Otherwise,
 * the VM will stay on the node.
 *
 * @author Fabien Hermenier
 */
public class MigratableActionModel extends VirtualMachineActionModel {

    /**
     * The global cost of the action.
     */
    private IntVar cost;

    /**
     * Make a new action.
     *
     * @param model    the model
     * @param vm       the virtual machine to make moveable
     * @param d        the duration of the migration if it is performed
     * @param moveable {@code true} to indicates the VM can be migrated
     */
    /*  public MigratableActionModel(ReconfigurationProblem model, VirtualMachine vm, int d, boolean moveable) {
   super(vm);

   cSlice = new ConsumingSlice(model, "migS(" + vm.getName() + ")", model.getSourceConfiguration().getLocation(vm), vm.getCPUConsumption(), vm.getMemoryConsumption());
   dSlice = null;

   if (moveable) {
       dSlice = new DemandingSlice(model, "migD(" + vm.getName() + ")", vm.getCPUDemand(), vm.getMemoryDemand());
       duration = model.createEnumIntVar("d(migrate(" + getVirtualMachine().getName() + "))", new int[]{0, d});
       this.cost = model.createBoundIntVar("k(migrate(" + getVirtualMachine().getName() + "))", 0, ReconfigurationProblem.MAX_TIME);
   } else {
       dSlice = new DemandingSlice(model, "migD(" + vm.getName() + ")", model.getNode(model.getSourceConfiguration().getLocation(vm)), vm.getCPUDemand(), vm.getMemoryDemand());
       duration = model.createIntegerConstant("d(migrate(" + getVirtualMachine().getName() + "))", 0);
       this.cost = model.createIntegerConstant("c(migrate(" + getVirtualMachine().getName() + "))", 0);
   }


   if (moveable) {
       BooleanVarImpl move = (BooleanVarImpl) model.createBooleanVar("mv(" + getVirtualMachine().getName() + ")");
       model.post(ReifiedFactory.builder(move, model.neq(cSlice.hoster(), dSlice.hoster()), model));

       BooleanVarImpl stay = (BooleanVarImpl) model.createBooleanVar("rt(" + getVirtualMachine().getName() + ")");
       model.post(model.neq(move, stay));

       //IntDomainVar stay = ((DefaultReconfigurationProblem)model).createNotBooleanVar("", move);

       model.post(new TimesXYZ(move, cSlice.end(), cost));

       model.post(model.ifOnlyIf(stay, model.eq(duration, 0)));
       //model.post(new FastIFFEq(stay, duration, 0));

       if (dSlice.getCPUheight() <= cSlice.getCPUheight()) {
           //model.post(new FastImpliesEq(stay, cSlice.duration(), 0));
           model.post(model.implies(stay, model.eq(cSlice.duration(), 0)));
       }
       if (dSlice.getCPUheight() > cSlice.getCPUheight()) {
           //model.post(new FastImpliesEq(stay, dSlice.duration(), 0));
           model.post(model.implies(stay, model.eq(dSlice.duration(), 0)));
       }

       model.post(model.leq(duration, cSlice.duration()));
       model.post(model.leq(duration, dSlice.duration()));
       model.post(model.eq(this.end(), model.plus(this.start(), duration)));
       //model.post(model.eq(this.end(), new IntDomainVarAddCste(model,"", this.start(), duration));

   } else {
       if (dSlice.getCPUheight() <= cSlice.getCPUheight()) {
           cSlice.fixDuration(0);
       }
       if (dSlice.getCPUheight() > cSlice.getCPUheight()) {
           dSlice.fixDuration(0);
       }
       model.post(model.eq(this.end(), this.start()));
   }
   cSlice.addToModel(model);
   dSlice.addToModel(model);
}     */
    public MigratableActionModel(ReconfigurationProblem model, VirtualMachine vm, int d, boolean moveable) {
        super(vm);

        assert d > 0 : "The cost of migration for " + vm + " equals 0 !";
        //
        //    cSlice: default: 0 + var(duration) = var(end)
        //    dSlice: default: var(start) + var(duration) = var(end)
        //
        //    !moveable:
        //        si cpu increase: dSlice: var(start) + 0 = var(end) -> Slice(end, 0, end) -> pas de contraintes
        //        else            cSlice: 0 + 0 = var(end)  -> Slice(0,0,0), pas de contraintes
        //
        //    moveable: cSlice -> Slice(0, var(end), var(end)), pas de plus
        //
        //

        if (moveable) {
            this.cost = model.createBoundIntVar("k(migrate(" + getVirtualMachine().getName() + "))", 0, ReconfigurationProblem.MAX_TIME);
            duration = model.createEnumIntVar("d(migrate(" + getVirtualMachine().getName() + "))", new int[]{0, d});
            cSlice = new ConsumingSlice(model, "migS(" + vm.getName() + ")", model.getSourceConfiguration().getLocation(vm), vm.getCPUConsumption(), vm.getMemoryConsumption());
            dSlice = new DemandingSlice(model, "migD(" + vm.getName() + ")", vm.getCPUDemand(), vm.getMemoryDemand());

            IntDomainVar move = model.createBooleanVar("mv(" + getVirtualMachine().getName() + ")");
            model.post(ReifiedFactory.builder(move, model.neq(cSlice.hoster(), dSlice.hoster()), model));

            IntDomainVar stay = new BoolVarNot(model, "", move);//(DefaultReconfigurationProblem) model).createNotBooleanVar("", move);

            model.post(new TimesXYZ(move, cSlice.end(), cost));

            model.post(new FastIFFEq(stay, duration, 0));

            if (dSlice.getCPUheight() <= cSlice.getCPUheight()) {
                model.post(new FastImpliesEq(stay, cSlice.duration(), 0));
            }
            if (dSlice.getCPUheight() > cSlice.getCPUheight()) {
                model.post(new FastImpliesEq(stay, dSlice.duration(), 0));
            }
            model.post(model.eq(dSlice.end(), model.plus(dSlice.start(), dSlice.duration())));
            model.post(model.eq(cSlice.end(), model.plus(cSlice.start(), cSlice.duration())));
            model.post(model.leq(duration, cSlice.duration()));
            model.post(model.leq(duration, dSlice.duration()));
            model.post(model.eq(this.end(), model.plus(this.start(), duration)));
        } else {
            boolean neadIncrease = vm.getCPUConsumption() <= vm.getCPUDemand();
            this.cost = model.createIntegerConstant("c(migrate(" + getVirtualMachine().getName() + "))", 0);
            if (neadIncrease) {
                cSlice = new ConsumingSlice("",
                        model.createIntegerConstant("", model.getNode(model.getSourceConfiguration().getLocation(vm))),
                        model.createTaskVar("", model.getStart(), model.getEnd(), model.getEnd()),
                        vm.getCPUConsumption(),
                        vm.getMemoryConsumption()
                );

                dSlice = new DemandingSlice("migD(" + vm.getName() + ")",
                        model.createIntegerConstant("", model.getNode(model.getSourceConfiguration().getLocation(vm))),
                        model.createTaskVar("", model.getEnd(), model.getEnd(), model.createIntegerConstant("", 0)),
                        vm.getCPUDemand(),
                        vm.getMemoryDemand()

                );
            } else {
                cSlice = new ConsumingSlice("",
                        model.createIntegerConstant("", model.getNode(model.getSourceConfiguration().getLocation(vm))),
                        model.createTaskVar("", model.getStart(), model.getStart(), model.getStart()),
                        vm.getCPUConsumption(),
                        vm.getMemoryConsumption()
                );

                dSlice = new DemandingSlice("",
                        model.createIntegerConstant("", model.getNode(model.getSourceConfiguration().getLocation(vm))),
                        model.createTaskVar("", model.getStart(), model.getEnd(), model.getEnd()),
                        vm.getCPUDemand(),
                        vm.getMemoryDemand()

                );
            }
            model.post(model.eq(this.end(), this.start()));
        }

        model.post(model.leq(cSlice.duration(), model.getEnd()));
        model.post(model.leq(dSlice.duration(), model.getEnd()));
    }

    /**
     * Get the moment the action ends. The action ends at the moment
     * the slice on the source node ends.
     *
     * @return <code>getConsumingSlice().end()</code>
     */
    @Override
    public final IntVar end() {
        return this.getConsumingSlice().end();
    }

    /**
     * Get the moment the action starts. The action starts at the moment
     * the slice on the source node starts.
     *
     * @return <code>getDemandingSlice().start()</code>
     */
    @Override
    public final IntVar start() {
        return this.getDemandingSlice().start();
    }

    /**
     * Return the migration action if the VM have to move.
     *
     * @return a Migration if the source node and the destination node are different. null otherwise
     */
    @Override
    public Migration getDefinedAction(ReconfigurationProblem solver) {
        if (getConsumingSlice().hoster().getVal()
                != getDemandingSlice().hoster().getVal()) {
            return new Migration(getVirtualMachine(),
                    solver.getNode(getConsumingSlice().hoster().getVal()),
                    solver.getNode(getDemandingSlice().hoster().getVal()),
                    start().getVal(),
                    end().getVal());
        }
        return null;
    }

    @Override
    public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
        cfg.addOnline(solver.getNode(getDemandingSlice().hoster().getVal()));
        return cfg.setRunOn(getVirtualMachine(), solver.getNode(getDemandingSlice().hoster().getVal()));

    }

    @Override
    public String toString() {
        return "migration(" + getVirtualMachine().getName() + ")";
    }

    @Override
    public IntVar getGlobalCost() {
        return this.cost;
    }
}
