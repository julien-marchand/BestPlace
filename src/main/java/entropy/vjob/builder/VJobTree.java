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
import org.antlr.runtime.tree.CommonTree;


/**
 * An abstract parser for a tree.
 *
 * @author Fabien Hermenier
 */
public class VJobTree extends CommonTree {

    /**
     * All the errors to report.
     */
    protected SemanticErrors errors;

    /**
     * Make a new tree.
     *
     * @param t    the token to handle. The root of this tree
     * @param errs the errors to report
     */
    public VJobTree(Token t, SemanticErrors errs) {
        super(t);
        errors = errs;
    }

    /**
     * Parse the root of the tree.
     *
     * @param parent the parent of the root
     * @return a content
     */
    public Content go(VJobTree parent) {
        throw new UnsupportedOperationException("Unhandled token: " + token.getText());
    }

    /**
     * Report an error for the current token and generate a content to ignore.
     *
     * @param msg the error message
     * @return an empty content
     */
    public Content ignoreError(String msg) {
        errors.append(token, msg);
        return Content.ignore;
    }

    /**
     * Report an error for the current token.
     *
     * @param msg the error message
     */
    public void error(String msg) {
        errors.append(token, msg);
    }
}
