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

package entropy.plan.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import entropy.configuration.Configuration;
import entropy.configuration.parser.ProtobufConfigurationSerializer;
import entropy.plan.DefaultTimedReconfigurationPlan;
import entropy.plan.TimedReconfigurationPlan;
import entropy.plan.action.Action;
import entropy.plan.action.Migration;
import entropy.plan.action.Pause;
import entropy.plan.action.Resume;
import entropy.plan.action.Run;
import entropy.plan.action.Shutdown;
import entropy.plan.action.Startup;
import entropy.plan.action.Stop;
import entropy.plan.action.Suspend;
import entropy.plan.action.UnPause;

/**
 * @author Fabien Hermenier
 */
public class ProtobufTimedReconfigurationPlanSerializer extends FileTimedReconfigurationPlanSerializer {

    private final static ProtobufTimedReconfigurationPlanSerializer instance = new ProtobufTimedReconfigurationPlanSerializer();

    private PBTimedReconfigurationPlan.TimedReconfigurationPlan.Builder planBuilder;

    private ProtobufTimedReconfigurationPlanSerializer() {
    }

    @Override
    public TimedReconfigurationPlan unSerialize(InputStream in) throws IOException, TimedReconfigurationPlanSerializerException {
        TimedReconfigurationPlan plan;
        try {
            PBTimedReconfigurationPlan.TimedReconfigurationPlan p = PBTimedReconfigurationPlan.TimedReconfigurationPlan.parseFrom(in);
            Configuration cfg = ProtobufConfigurationSerializer.getInstance().convert(p.getSource());
            plan = new DefaultTimedReconfigurationPlan(cfg);
            for (PBTimedReconfigurationPlan.TimedReconfigurationPlan.Migration m : p.getMigrationsList()) {
                plan.add(convert(cfg, m));
            }

            for (PBTimedReconfigurationPlan.TimedReconfigurationPlan.Stop m : p.getStopsList()) {
                plan.add(convert(cfg, m));
            }

            for (PBTimedReconfigurationPlan.TimedReconfigurationPlan.Startup m : p.getStartupsList()) {
                plan.add(convert(cfg, m));
            }

            for (PBTimedReconfigurationPlan.TimedReconfigurationPlan.Run m : p.getRunsList()) {
                plan.add(convert(cfg, m));
            }

            for (PBTimedReconfigurationPlan.TimedReconfigurationPlan.Resume m : p.getResumesList()) {
                plan.add(convert(cfg, m));
            }

            for (PBTimedReconfigurationPlan.TimedReconfigurationPlan.Suspend m : p.getSuspendsList()) {
                plan.add(convert(cfg, m));
            }

            for (PBTimedReconfigurationPlan.TimedReconfigurationPlan.Pause m : p.getPausesList()) {
                plan.add(convert(cfg, m));
            }

            for (PBTimedReconfigurationPlan.TimedReconfigurationPlan.Unpause m : p.getUnpausesList()) {
                plan.add(convert(cfg, m));
            }

            //Ladt to prevent application failures due to a node currently hosting a VM
            for (PBTimedReconfigurationPlan.TimedReconfigurationPlan.Shutdown m : p.getShutdownsList()) {
                plan.add(convert(cfg, m));
            }

        } catch (Exception e) {
            throw new TimedReconfigurationPlanSerializerException(e);
        }
        return plan;
    }

    @Override
    public void serialize(TimedReconfigurationPlan plan, OutputStream out) throws IOException {
        planBuilder = PBTimedReconfigurationPlan.TimedReconfigurationPlan.newBuilder();
        planBuilder.setSource(ProtobufConfigurationSerializer.getInstance().convert(plan.getSource()));

        for (Action a : plan) {
            a.serialize(this);
        }
        planBuilder.build().writeTo(out);
    }

    private Migration convert(Configuration cfg, PBTimedReconfigurationPlan.TimedReconfigurationPlan.Migration a) {
        return new Migration(cfg.getAllVirtualMachines().get(a.getIdVm()),
                cfg.getAllNodes().get(a.getIdSrc()),
                cfg.getAllNodes().get(a.getIdDst()), a.getStart(), a.getEnd());
    }

    private Run convert(Configuration cfg, PBTimedReconfigurationPlan.TimedReconfigurationPlan.Run a) {
        return new Run(cfg.getAllVirtualMachines().get(a.getIdVm()),
                cfg.getAllNodes().get(a.getIdN()), a.getStart(), a.getEnd());
    }

    private Stop convert(Configuration cfg, PBTimedReconfigurationPlan.TimedReconfigurationPlan.Stop a) {
        return new Stop(cfg.getAllVirtualMachines().get(a.getIdVm()),
                cfg.getAllNodes().get(a.getIdN()), a.getStart(), a.getEnd());
    }

    private Startup convert(Configuration cfg, PBTimedReconfigurationPlan.TimedReconfigurationPlan.Startup a) {
        return new Startup(cfg.getAllNodes().get(a.getIdN()), a.getStart(), a.getEnd());
    }

    private Shutdown convert(Configuration cfg, PBTimedReconfigurationPlan.TimedReconfigurationPlan.Shutdown a) {
        return new Shutdown(cfg.getAllNodes().get(a.getIdN()), a.getStart(), a.getEnd());
    }

    private Suspend convert(Configuration cfg, PBTimedReconfigurationPlan.TimedReconfigurationPlan.Suspend a) {
        return new Suspend(cfg.getAllVirtualMachines().get(a.getIdVm()),
                cfg.getAllNodes().get(a.getIdSrc()),
                cfg.getAllNodes().get(a.getIdDst()), a.getStart(), a.getEnd());
    }

    private Resume convert(Configuration cfg, PBTimedReconfigurationPlan.TimedReconfigurationPlan.Resume a) {
        return new Resume(cfg.getAllVirtualMachines().get(a.getIdVm()),
                cfg.getAllNodes().get(a.getIdSrc()),
                cfg.getAllNodes().get(a.getIdDst()), a.getStart(), a.getEnd());
    }

    private Pause convert(Configuration cfg, PBTimedReconfigurationPlan.TimedReconfigurationPlan.Pause a) {
        return new Pause(cfg.getAllVirtualMachines().get(a.getIdVm()), cfg.getAllNodes().get(a.getIdN()), a.getStart(), a.getEnd());
    }

    private UnPause convert(Configuration cfg, PBTimedReconfigurationPlan.TimedReconfigurationPlan.Unpause a) {
        return new UnPause(cfg.getAllVirtualMachines().get(a.getIdVm()), cfg.getAllNodes().get(a.getIdN()), a.getStart(), a.getEnd());
    }

    public static ProtobufTimedReconfigurationPlanSerializer getInstance() {
        return instance;
    }

    @Override
    public void serialize(Migration a) throws IOException {
        PBTimedReconfigurationPlan.TimedReconfigurationPlan.Migration.Builder b = PBTimedReconfigurationPlan.TimedReconfigurationPlan.Migration.newBuilder();
        b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
                .setIdVm(a.getVirtualMachine().getName())
                .setIdSrc(a.getHost().getName())
                .setIdDst(a.getDestination().getName());
        planBuilder.addMigrations(b.build());
    }

    @Override
    public void serialize(Run a) throws IOException {
        PBTimedReconfigurationPlan.TimedReconfigurationPlan.Run.Builder b = PBTimedReconfigurationPlan.TimedReconfigurationPlan.Run.newBuilder();
        b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
                .setIdVm(a.getVirtualMachine().getName())
                .setIdN(a.getHost().getName());
        planBuilder.addRuns(b.build());
    }

    @Override
    public void serialize(Stop a) throws IOException {
        PBTimedReconfigurationPlan.TimedReconfigurationPlan.Stop.Builder b = PBTimedReconfigurationPlan.TimedReconfigurationPlan.Stop.newBuilder();
        b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
                .setIdVm(a.getVirtualMachine().getName())
                .setIdN(a.getHost().getName());
        planBuilder.addStops(b.build());
    }

    @Override
    public void serialize(Suspend a) throws IOException {
        PBTimedReconfigurationPlan.TimedReconfigurationPlan.Suspend.Builder b = PBTimedReconfigurationPlan.TimedReconfigurationPlan.Suspend.newBuilder();
        b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
                .setIdVm(a.getVirtualMachine().getName())
                .setIdSrc(a.getHost().getName())
                .setIdDst(a.getDestination().getName());
        planBuilder.addSuspends(b.build());
    }

    @Override
    public void serialize(Resume a) throws IOException {
        PBTimedReconfigurationPlan.TimedReconfigurationPlan.Resume.Builder b = PBTimedReconfigurationPlan.TimedReconfigurationPlan.Resume.newBuilder();
        b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
                .setIdVm(a.getVirtualMachine().getName())
                .setIdSrc(a.getHost().getName())
                .setIdDst(a.getDestination().getName());
        planBuilder.addResumes(b.build());
    }

    @Override
    public void serialize(Startup a) throws IOException {
        PBTimedReconfigurationPlan.TimedReconfigurationPlan.Startup.Builder b = PBTimedReconfigurationPlan.TimedReconfigurationPlan.Startup.newBuilder();
        b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
                .setIdN(a.getNode().getName());
        planBuilder.addStartups(b.build());
    }

    @Override
    public void serialize(Shutdown a) throws IOException {
        PBTimedReconfigurationPlan.TimedReconfigurationPlan.Shutdown.Builder b = PBTimedReconfigurationPlan.TimedReconfigurationPlan.Shutdown.newBuilder();
        b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
                .setIdN(a.getNode().getName());
        planBuilder.addShutdowns(b.build());
    }

    @Override
    public void serialize(Pause a) throws IOException {
        PBTimedReconfigurationPlan.TimedReconfigurationPlan.Pause.Builder b = PBTimedReconfigurationPlan.TimedReconfigurationPlan.Pause.newBuilder();
        b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
                .setIdVm(a.getVirtualMachine().getName())
                .setIdN(a.getHost().getName());
        planBuilder.addPauses(b.build());
    }

    @Override
    public void serialize(UnPause a) throws IOException {
        PBTimedReconfigurationPlan.TimedReconfigurationPlan.Unpause.Builder b = PBTimedReconfigurationPlan.TimedReconfigurationPlan.Unpause.newBuilder();
        b.setStart(a.getStartMoment()).setEnd(a.getFinishMoment())
                .setIdVm(a.getVirtualMachine().getName())
                .setIdN(a.getHost().getName());
        planBuilder.addUnpauses(b.build());
    }
}
