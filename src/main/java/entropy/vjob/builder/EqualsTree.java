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

import org.antlr.runtime.Token;

import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.vjob.VJob;
import entropy.vjob.VJobElement;
import entropy.vjob.VJobMultiSet;
import entropy.vjob.VJobSet;

/**
 * A parser to declare a variable.
 *
 * @author Fabien Hermenier
 */
public class EqualsTree extends VJobTree {

    /**
     * The table of symbols to use;
     */
    private SymbolsTable symbols;

    /**
     * The vjob to fullfil with variables.
     */
    private VJob vjob;

    /**
     * Make a new parser
     *
     * @param t    the root token
     * @param errs the errors to report
     * @param syms the table of symbols to use
     * @param v    the vjob to fullfil with variables
     */
    public EqualsTree(Token t, SemanticErrors errs, SymbolsTable syms, VJob v) {
        super(t, errs);
        symbols = syms;
        vjob = v;
    }

    @Override
    public Content go(VJobTree parent) {
        if (symbols.isDeclared(getChild(0).getText())) {
            errors.append(((VJobTree) getChild(0)).token, "Variable " + getChild(0).getText() + " is already defined");
        } else {
            //We declare the variable even if the right operand has to be ignored
            //to reduce annoying error message
            Content res = ((VJobTree) getChild(1)).go(this);
            symbols.declare(getChild(0).getText(), res);
            if (res.equals(Content.ignore)) {
                return Content.ignore;
            }
            ((VJobElement) res.content()).setLabel(getChild(0).getText());
            switch (res.type()) {
                case vmset:
                    vjob.addVirtualMachines((VJobSet<VirtualMachine>) res.content());
                    break;
                case multivmsets:
                    vjob.addVirtualMachines((VJobMultiSet<VirtualMachine>) res.content());
                    break;
                case nodeset:
                    vjob.addNodes((VJobSet<Node>) res.content());
                    break;
                case multinodesets:
                    vjob.addNodes((VJobMultiSet<Node>) res.content());
                    break;
                default:
                    return ignoreError("Unsupported type of variable: " + res.type());
            }
        }
        return Content.empty;
    }
}
