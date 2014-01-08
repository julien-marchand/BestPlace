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
package entropy.plan;


import entropy.configuration.VirtualMachine;

/**
 * An exception dedicated to describe an incompatible
 * transition between two states of a virtual machine.
 *
 * @author Fabien Hermenier
 */
public class NoAvailableTransitionException extends PlanException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Make a new exception.
     *
     * @param vm  the Virtual machine involved in the exception
     * @param src the initial state
     * @param dst the destination state
     */
    public NoAvailableTransitionException(VirtualMachine vm, String src, String dst) {
        super("No available transition for " + vm.getName() + " between the state '" + src + "' and '" + dst + "'");
    }
}
