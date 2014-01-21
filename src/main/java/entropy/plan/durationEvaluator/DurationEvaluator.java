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


import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;

/**
 * An interface to specify a evaluator of duration expression.
 *
 * @author Fabien Hermenier
 */
public interface DurationEvaluator {

    /**
     * Evaluate the duration of a migration action for a specific virtual machine.
     *
     * @param vm the involved virtual machine
     * @return the estimated duration or -1 in case of errors
     * @throws DurationEvaluationException if an error occurred during evaluation
     */
    int evaluateMigration(VirtualMachine vm) throws DurationEvaluationException;

    /**
     * Evaluate the duration of a run action for a specific virtual machine.
     *
     * @param vm the involved virtual machine
     * @return the estimated duration or -1 in case of errors
     * @throws DurationEvaluationException if an error occurred during evaluation
     */
    int evaluateRun(VirtualMachine vm) throws DurationEvaluationException;

    /**
     * Evaluate the duration of a stop action for a specific virtual machine.
     *
     * @param vm the involved virtual machine
     * @return the estimated duration or -1 in case of errors
     * @throws DurationEvaluationException if an error occurred during evaluation
     */
    int evaluateStop(VirtualMachine vm) throws DurationEvaluationException;

    /**
     * Evaluate the duration of a local suspend action for a specific virtual machine.
     *
     * @param vm the involved virtual machine
     * @return the estimated duration or -1 in case of errors
     * @throws DurationEvaluationException if an error occurred during evaluation
     */
    int evaluateLocalSuspend(VirtualMachine vm) throws DurationEvaluationException;

    /**
     * Evaluate the duration of a local resume action for a specific virtual machine.
     *
     * @param vm the involved virtual machine
     * @return the estimated duration or -1 in case of errors
     * @throws DurationEvaluationException if an error occurred during evaluation
     */
    int evaluateLocalResume(VirtualMachine vm) throws DurationEvaluationException;

    /**
     * Evaluate the duration of a remote resume action for a specific virtual machine.
     *
     * @param vm the involved virtual machine
     * @return the estimated duration or -1 in case of errors
     * @throws DurationEvaluationException if an error occurred during evaluation
     */
    int evaluateRemoteResume(VirtualMachine vm) throws DurationEvaluationException;

    /**
     * Evaluate the duration of the startup of a node.
     *
     * @param node the involved node
     * @return the estimated duration or -1 in case of errors
     * @throws DurationEvaluationException if the evaluation of the duration fails
     */
    int evaluateStartup(Node node) throws DurationEvaluationException;

    /**
     * Evaluate the duration of the shutdown of a node.
     *
     * @param node the involved node
     * @return the estimated duration or -1 in case of errors
     * @throws DurationEvaluationException if the evaluation of the duration fails
     */
    int evaluateShutdown(Node node) throws DurationEvaluationException;

}
