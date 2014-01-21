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
package entropy.plan.durationEvaluator;


import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;

/**
 * A duration evaluator based on ANTLR.
 *
 * @author Fabien Hermenier
 */
public class ANTLRDurationEvaluator implements DurationEvaluator {

    /**
     * Index for the migration.
     */
    private static final int MIGRATE_STREAM = 0;

    /**
     * Index for the run.
     */
    private static final int RUN_STREAM = 1;

    /**
     * Index for the stop.
     */
    private static final int STOP_STREAM = 2;

    /**
     * Index for the local suspend.
     */
    private static final int LOCAL_SUSPEND_STREAM = 3;

    /**
     * Index for the local resume.
     */
    private static final int LOCAL_RESUME_STREAM = 4;

    /**
     * Index for the remote resume.
     */
    private static final int REMOTE_RESUME_STREAM = 5;

    /**
     * Index for the startup.
     */
    private static final int STARTUP_STREAM = 6;

    /**
     * Index for the shutdown.
     */
    private static final int SHUTDOWN_STREAM = 7;

    /**
     * the different expressions. Use the different indexes to manipulate it.
     */
    private String[] exprs;

    /**
     * Make a new evaluator that evaluate different expressions.
     *
     * @param migrationExpr    the expression to define the duration of a migration
     * @param stopExpr         the expression to define the duration of a stop
     * @param runExpr          the expression to define the duration of a run
     * @param localSuspendExpr the expression to define the duration of a local suspend
     * @param localResumeExpr  the expression to define the duration of a local resume
     * @param remoteResumeExpr the expression to define the duration of a remote resume
     * @param startupExpr      the expression to define the duration of a startup
     * @param shutdownExpr     the expression to define the duration of a shutdown
     */
    public ANTLRDurationEvaluator(String migrationExpr,
                                  String stopExpr,
                                  String runExpr,
                                  String localSuspendExpr,
                                  String localResumeExpr,
                                  String remoteResumeExpr,
                                  String startupExpr,
                                  String shutdownExpr
    ) {
        this.exprs = new String[8];
        this.exprs[RUN_STREAM] = runExpr;
        this.exprs[MIGRATE_STREAM] = migrationExpr;
        this.exprs[STOP_STREAM] = stopExpr;
        this.exprs[REMOTE_RESUME_STREAM] = remoteResumeExpr;
        this.exprs[LOCAL_RESUME_STREAM] = localResumeExpr;
        this.exprs[LOCAL_SUSPEND_STREAM] = localSuspendExpr;
        this.exprs[STARTUP_STREAM] = startupExpr;
        this.exprs[SHUTDOWN_STREAM] = shutdownExpr;
    }

    /**
     * Read an expression with the lexer.
     *
     * @param idx the index of the expression to read.
     * @return a stream of token
     */
    private CommonTokenStream read(int idx) {
        CharStream cs = new ANTLRStringStream(this.exprs[idx]);
        ANTLRDurationEvaluatorLexer lexer = new ANTLRDurationEvaluatorLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream();
        tokens.setTokenSource(lexer);
        return tokens;
    }

    /**
     * Evaluate the expression of a specific duration.
     *
     * @param idx the index of the expression
     * @param vm  the virtual machine
     * @return the estimation of the expression
     * @throws RecognitionException if an error occurs during the evaluation
     */
    private int evaluate(int idx, VirtualMachine vm) throws RecognitionException {
        ANTLRDurationEvaluatorParser p = new ANTLRDurationEvaluatorParser(this.read(idx));
        return p.evaluate(vm);
    }

    /**
     * Evaluate the expression of a specific duration.
     *
     * @param idx the index of the expression
     * @param n   the node
     * @return the estimation of the expression
     * @throws RecognitionException if an error occurs during the evaluation
     */
    private int evaluate(int idx, Node n) throws RecognitionException {
        ANTLRDurationEvaluatorParser p = new ANTLRDurationEvaluatorParser(this.read(idx));
        return p.evaluate(n);
    }

    @Override
    public int evaluateMigration(VirtualMachine vm) throws DurationEvaluationException {
        try {
            return evaluate(MIGRATE_STREAM, vm);
        } catch (RecognitionException e) {
            throw new DurationEvaluationException(exprs[MIGRATE_STREAM], e, vm);
        }
    }

    @Override
    public int evaluateRun(VirtualMachine vm) throws DurationEvaluationException {
        try {
            return evaluate(RUN_STREAM, vm);
        } catch (RecognitionException e) {
            throw new DurationEvaluationException(exprs[RUN_STREAM], e, vm);
        }

    }

    @Override
    public int evaluateStop(VirtualMachine vm) throws DurationEvaluationException {
        try {
            return evaluate(STOP_STREAM, vm);
        } catch (RecognitionException e) {
            throw new DurationEvaluationException(exprs[STOP_STREAM], e, vm);
        }
    }

    @Override
    public int evaluateLocalSuspend(VirtualMachine vm) throws DurationEvaluationException {
        try {
            return evaluate(LOCAL_SUSPEND_STREAM, vm);
        } catch (RecognitionException e) {
            throw new DurationEvaluationException(exprs[LOCAL_SUSPEND_STREAM], e, vm);
        }

    }

    @Override
    public int evaluateLocalResume(VirtualMachine vm) throws DurationEvaluationException {
        try {
            return evaluate(LOCAL_RESUME_STREAM, vm);
        } catch (RecognitionException e) {
            throw new DurationEvaluationException(exprs[LOCAL_RESUME_STREAM], e, vm);
        }
    }

    @Override
    public int evaluateRemoteResume(VirtualMachine vm) throws DurationEvaluationException {
        try {
            return evaluate(REMOTE_RESUME_STREAM, vm);
        } catch (RecognitionException e) {
            throw new DurationEvaluationException(exprs[REMOTE_RESUME_STREAM], e, vm);
        }
    }

    @Override
    public int evaluateShutdown(Node node) throws DurationEvaluationException {
        try {
            return evaluate(SHUTDOWN_STREAM, node);
        } catch (RecognitionException e) {
            throw new DurationEvaluationException(exprs[SHUTDOWN_STREAM], e, node);
        }
    }

    @Override
    public int evaluateStartup(Node node) throws DurationEvaluationException {
        try {
            return evaluate(STARTUP_STREAM, node);
        } catch (RecognitionException e) {
            throw new DurationEvaluationException(exprs[STARTUP_STREAM], e, node);
        }
    }
}
