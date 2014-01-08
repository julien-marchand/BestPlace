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

/**
 * A Parser to get a variable.
 *
 * @author Fabien Hermenier
 */
public class VarTree extends VJobTree {

    /**
     * The table of symbols to use.
     */
    private SymbolsTable symbols;

    /**
     * Make a new parser.
     *
     * @param t    the root of the tree
     * @param errs the errors to report
     * @param syms the symbols' table to use
     */
    public VarTree(Token t, SemanticErrors errs, SymbolsTable syms) {
        super(t, errs);
        symbols = syms;
    }

    @Override
    public Content go(VJobTree parent) {
        String lbl = token.getText();
        if (!symbols.isDeclared(lbl)) {
            return ignoreError("Unkown variable " + lbl);
        }
        return symbols.getSymbol(lbl);
    }
}
