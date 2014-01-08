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

import java.util.LinkedList;
import java.util.List;

import entropy.configuration.DefaultVirtualMachine;

/**
 * A mock for VirtualMachineBuilder
 *
 * @author Fabien Hermenier
 */
public class MockVirtualMachineBuilder implements VirtualMachineBuilder {


    public List<String> farm;

    public MockVirtualMachineBuilder(List<String> retrievables) {
        this.farm = retrievables;
    }

    public MockVirtualMachineBuilder() {
        this(new LinkedList<String>());
    }

    /**
     * Return a new virtual machine with a specified Id
     */
    @Override
    public DefaultVirtualMachine buildVirtualMachine(String id) {
        for (int i = 0; i < farm.size(); i++) {
            if (farm.get(i).equals(id)) {
                return new DefaultVirtualMachine(id, 1, 1, 1);
            }
        }
        return null;
    }


}
