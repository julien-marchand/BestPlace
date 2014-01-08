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

import entropy.vjob.MultiSetsUnion;
import entropy.vjob.SetsUnion;
import entropy.vjob.VJobMultiSet;
import entropy.vjob.VJobSet;

/**
 * A parser to make the union between two sets or multisets.
 *
 * @author Fabien Hermenier
 */
public class UnionTree extends VJobTree {

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param errs the errors to report
     */
    public UnionTree(Token t, SemanticErrors errs) {
        super(t, errs);
    }

    @Override
    public Content go(VJobTree parent) {
        //Type checking, right child must have the same type that left type
        Content l = ((VJobTree) getChild(0)).go(this);
        Content r = ((VJobTree) getChild(1)).go(this);
        //error
        if (l.equals(Content.ignore) || r.equals(Content.ignore)) {
            return Content.ignore;
        }
        switch (l.type()) {
            case vm:

        }
        if (l.type() != r.type()) {
            return ignoreError("Type mismatch: rigth operand is not a " + l.type().toString());
        } else {
            if (l.type() == Content.Type.vmset || l.type() == Content.Type.nodeset) {
                return new Content(l.type(), new SetsUnion((VJobSet) l.content(), (VJobSet) r.content()));
            } else if (l.type() == Content.Type.multinodesets || l.type() == Content.Type.multivmsets) {
                return new Content(l.type(), new MultiSetsUnion((VJobMultiSet) l.content(), (VJobMultiSet) r.content()));
            }
            return ignoreError("Unsupported union for " + l.type());
        }
    }

}
