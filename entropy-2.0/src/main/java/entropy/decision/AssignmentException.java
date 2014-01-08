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

package entropy.decision;

import entropy.configuration.Configuration;
import entropy.configuration.VirtualMachine;

/**
 * Exception throws when a virtual machine is not assignable to any node of a configuration.
 *
 * @author Fabien Hermenier
 */
public class AssignmentException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The configuration involved in the assignment process.
     */
    private Configuration c;

    /**
     * The virtual machine that can not be assigned.
     */
    private VirtualMachine vm;

    /**
     * An exception with a basic message.
     *
     * @param msg the error message
     */
    public AssignmentException(String msg) {
        super(msg);
        vm = null;
        c = null;
    }

    /**
     * New exception.
     *
     * @param conf the configuration involved in the assignment process.
     */
    public AssignmentException(Configuration conf) {
        this(conf, null);
    }

    /**
     * New exception.
     *
     * @param conf the configuration involved in the assignment process.
     * @param v    the virtual machine that can not be assigned, may be null
     */
    public AssignmentException(Configuration conf, VirtualMachine v) {
        this.c = conf;
        this.vm = v;
    }

    /**
     * Get a textual cause for this exception.
     *
     * @return a String
     */
    @Override
    public String getMessage() {
        if (this.vm != null) {
            return "Unable to assign the virtual machine " + this.vm + "to a node of the configuration:\n" + this.c;
        } else if (this.c != null) {
            return "Unable to find a viable assignment for the configuration:\n" + this.c;
        }
        return super.getMessage();
    }
}
