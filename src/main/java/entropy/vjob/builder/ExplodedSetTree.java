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

import entropy.configuration.DefaultManagedElement;
import entropy.configuration.ManagedElement;
import entropy.vjob.ExplodedMultiSet;
import entropy.vjob.ExplodedSet;
import entropy.vjob.VJobSet;

/**
 * A parser to make exploded sets.
 *
 * @author Fabien Hermenier
 */
public class ExplodedSetTree extends VJobTree {

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param errs the errors to report
     */
    public ExplodedSetTree(Token t, SemanticErrors errs) {
        super(t, errs);
    }

    @Override
    public Content go(VJobTree parent) {
        if (getChildCount() > 0) {
            Content t0 = ((VJobTree) getChild(0)).go(this);
            switch (t0.type()) {
                case ignore:
                    return Content.ignore;
                case vm:
                    return makeSet(t0.type(), Content.Type.vmset);
                case node:
                    return makeSet(t0.type(), Content.Type.nodeset);
                case nodeset:
                    return makeMultiSet(t0.type(), Content.Type.multinodesets);
                case vmset:
                    return makeMultiSet(t0.type(), Content.Type.multivmsets);
                default:
                    errors.append(((VJobTree) getChild(0)).token, "a set cannot be composed of " + t0.type().toString());
                    return Content.ignore;
            }
        } else {
            return Content.empty;
        }

    }

    private Content makeMultiSet(Content.Type expect, Content.Type produce) {
        ExplodedMultiSet set = new ExplodedMultiSet();
        for (int i = 0; i < getChildCount(); i++) {
            Content tx = ((VJobTree) getChild(i)).go(this);
            if (!tx.type().equals(expect)) {
                return ignoreError("Type mismatch for "
                        + ((DefaultManagedElement) tx.content()).getName() + " expected " + expect + " but was " + tx.type());
            } else {
                set.add((VJobSet) tx.content());
            }
        }
        return new Content(produce, set);
    }

    private Content makeSet(Content.Type expect, Content.Type produce) {
        ExplodedSet set = new ExplodedSet();
        for (int i = 0; i < getChildCount(); i++) {
            Content tx = ((VJobTree) getChild(i)).go(this);
            if (!tx.type().equals(expect)) {
                return ignoreError("Type mismatch for " + ((ManagedElement) tx.content()).getName() + " expected " + expect + " but was " + tx.type());
            } else {
                set.add((ManagedElement) tx.content());
            }
        }
        return new Content(produce, set);
    }
}
