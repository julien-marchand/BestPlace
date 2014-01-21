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

import entropy.vjob.MultiSetsDifference;
import entropy.vjob.SetsDifference;
import entropy.vjob.VJobMultiSet;
import entropy.vjob.VJobSet;

/**
 * A parser to make the difference between two sets or multisets.
 *
 * @author Fabien Hermenier
 */
public class DiffTree extends VJobTree {

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param errs the errors to report
     */
    public DiffTree(Token t, SemanticErrors errs) {
        super(t, errs);
    }

    @Override
    public Content go(VJobTree parent) {
        //Type checking, right child must have the same type that left type        
        Content l = ((VJobTree) getChild(0)).go(this);
        Content r = ((VJobTree) getChild(1)).go(this);

        //error
        if (l.type() == Content.Type.ignore || r.type() == Content.Type.ignore) {
            return Content.ignore;
        }

        if (l.type() != r.type()) {
            return ignoreError("Type mismatch: right operand is not a " + l.type().toString());
        } else {
            if (l.type() == Content.Type.vmset || l.type() == Content.Type.nodeset) {
                return new Content(l.type(), new SetsDifference((VJobSet) l.content(), (VJobSet) r.content()));
            } else if (l.type() == Content.Type.multinodesets || l.type() == Content.Type.multivmsets) {
                return new Content(l.type(), new MultiSetsDifference((VJobMultiSet) l.content(), (VJobMultiSet) r.content()));
            }
            return ignoreError("Unsupported union for " + l.type());
        }
    }
}
