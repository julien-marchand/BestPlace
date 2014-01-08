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
import org.antlr.runtime.tree.CommonTreeAdaptor;

import entropy.vjob.VJob;

/**
 * An adapter to instantiate the right VJobTree depending on the token.
 *
 * @author Fabien Hermenier
 */
public class VJobTreeAdaptor extends CommonTreeAdaptor {

    private SemanticErrors errors;

    private SymbolsTable symbols;

    private VJobElementBuilder elemBuilder;

    private ConstraintsCatalog catalog;
    private VJob vjob;

    /**
     * Build a new adaptor.
     *
     * @param errs the errors to report
     * @param s    the symbol table to use
     * @param e    the builer to make elements
     * @param v    the vjob to fullfil
     * @param c    the catalog of available constraints
     */
    public VJobTreeAdaptor(SemanticErrors errs, SymbolsTable s, VJobElementBuilder e, VJob v, ConstraintsCatalog c) {
        this.errors = errs;
        this.symbols = s;
        this.elemBuilder = e;
        this.catalog = c;
        this.vjob = v;
    }

    @Override
    public Object create(Token payload) {
        if (payload == null) {
            return new VJobTree(payload, errors);
        }
        switch (payload.getType()) {
            case ANTLRVJob3Lexer.INT:
                return new IntTree(payload, errors);
            case ANTLRVJob3Lexer.EXPLODED_INTERVAL:
                return new ExplodedIntervalTree(payload, errors, elemBuilder);
            case ANTLRVJob3Lexer.INTERVAL:
                return new IntervalSetTree(payload, errors, elemBuilder);
            case ANTLRVJob3Lexer.EXPLODED_SET:
                return new ExplodedSetTree(payload, errors);
            case ANTLRVJob3Lexer.VAL:
                return new ValTree(payload, errors, elemBuilder);
            case ANTLRVJob3Lexer.EQUALS:
                return new EqualsTree(payload, errors, symbols, vjob);
            case ANTLRVJob3Lexer.UNION:
                return new UnionTree(payload, errors);
            case ANTLRVJob3Lexer.DIFF:
                return new DiffTree(payload, errors);
            case ANTLRVJob3Lexer.VAR:
                return new VarTree(payload, errors, symbols);
            case ANTLRVJob3Lexer.CNAME:
                return new ConstraintTree(payload, errors, catalog, vjob);
            default:
                return new VJobTree(payload, errors);
        }
    }
}
