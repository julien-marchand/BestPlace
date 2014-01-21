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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.vjob.VJob;
import entropy.vjob.VJobMultiSet;
import entropy.vjob.VJobSet;

/**
 * A table of symbols to store variables in a vjob description.
 * A prolog may be used to insert predefined variables.
 *
 * @author Fabien Hermenier
 */
public class SymbolsTable {

    /**
     * The declared variables.
     */
    private Map<String, Content> type;

    /**
     * Make a new table of symbols for a vjob.
     */
    public SymbolsTable() {
        this(null);
    }

    /**
     * Make a new table of symbols that use a prolog.
     * All the variables of the prolog will be inserted into the table
     *
     * @param prolog the prolog
     */
    public SymbolsTable(VJob prolog) {
        type = new Hashtable<String, Content>();

        if (prolog != null) {
            for (VJobMultiSet<Node> e : prolog.getMultiNodeSets()) {
                if (e.getLabel() != null) {
                    declare(e.getLabel(), new Content(Content.Type.multinodesets, e));
                }
            }
            for (VJobMultiSet<VirtualMachine> e : prolog.getMultiVirtualMachineSets()) {
                if (e.getLabel() != null) {
                    declare(e.getLabel(), new Content(Content.Type.multivmsets, e));
                }
            }

            for (VJobSet<Node> e : prolog.getNodeSets()) {
                if (e.getLabel() != null) {
                    declare(e.getLabel(), new Content(Content.Type.nodeset, e));
                }
            }

            for (VJobSet<VirtualMachine> e : prolog.getVirtualMachineSets()) {
                if (e.getLabel() != null) {
                    declare(e.getLabel(), new Content(Content.Type.vmset, e));
                }
            }
        }
    }

    /**
     * Declare a new variable.
     * The variable is inserted into the current vjob.
     *
     * @param label the label of the variable
     * @param t     the content of the variable
     * @return {@code true} if the declaration succeeds, {@code false} otherwise
     */
    public final boolean declare(String label, Content t) {
        if (type.containsKey(label)) {
            return false;
        }
        type.put(label, t);
        return true;
    }

    /**
     * Get the content associated to a variable label.
     *
     * @param label the label of the variable
     * @return the content of the variable if exists. {@code null} otherwise
     */
    public Content getSymbol(String label) {
        return type.get(label);
    }

    /**
     * Check wether a variable is declared.
     *
     * @param label the label of the variable
     * @return {@code true} if the variable is already declared, {@code false} otherwise
     */
    public boolean isDeclared(String label) {
        return type.containsKey(label);
    }

    /**
     * Textual representation of the table of symbol.
     *
     * @return all the variables and their content. One variable per line.
     */
    @Override
	public String toString() {
        StringBuilder b = new StringBuilder();
        for (Iterator<Map.Entry<String, Content>> ite = type.entrySet().iterator(); ite.hasNext();) {
            Map.Entry<String, Content> e = ite.next();
            b.append(e.getKey());
            b.append(": ");
            b.append(e.getValue().content());
            if (ite.hasNext()) {
                b.append("\n");
            }
        }
        return b.toString();
    }
}
