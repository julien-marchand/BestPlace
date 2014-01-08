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
 * A parser to make integer.
 *
 * @author Fabien Hermenier
 */
public class IntTree extends VJobTree {

    /**
     * Make a new parser.
     *
     * @param t    the root token
     * @param errs the errors to report
     */
    public IntTree(Token t, SemanticErrors errs) {
        super(t, errs);
    }

    @Override
    public Content go(VJobTree parent) {
        try {
            int i = Integer.parseInt(token.getText());
            return new Content(Content.Type.integer, i);
        } catch (NumberFormatException e) {
            return ignoreError("Malformed integer: " + token.getText());
        }
    }
}
