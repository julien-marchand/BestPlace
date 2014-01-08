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

package entropy.vjob.builder;

import entropy.configuration.VirtualMachine;

/**
 * An interface to specify a builder for VirtualMachine.
 * This builder is required to instantiate VirtualMachine objects when they are
 * not known by the system but specified in a VJob file.
 * Using the Identifier of the virtual machine, the builder get its basic parameters
 * such as its memory requirements.
 *
 * @author Fabien Hermenier
 */
public interface VirtualMachineBuilder {

    /**
     * Build a {@link entropy.configuration.VirtualMachine} from its identifier.
     *
     * @param name the identifier of the virtual machine
     * @return a {@link entropy.configuration.VirtualMachine} if exists, or {@code null} if no virtual machines are associated to the identifier
     * @throws VirtualMachineBuilderException if an error occurred
     */
    VirtualMachine buildVirtualMachine(String name) throws VirtualMachineBuilderException;
}
