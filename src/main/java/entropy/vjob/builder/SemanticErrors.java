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

import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.Token;

/**
 * A structure to report all the errors detected when parsing a VJob.
 *
 * @author Fabien Hermenier
 */
public class SemanticErrors {

    /**
     * The error messages.
     */
    private List<String> errors;

    /**
     * Make a new instance.
     */
    public SemanticErrors() {
        errors = new LinkedList<String>();
    }

    /**
     * Append an error related to a token
     *
     * @param t   the token responsible for the error
     * @param msg the error message
     */
    public void append(Token t, String msg) {
        StringBuilder b = new StringBuilder();
        b.append("Error(");
        b.append(t.getLine());
        b.append(":");
        b.append(t.getCharPositionInLine());
        b.append(") ");
        b.append(msg);
        errors.add(b.toString());
    }

    /**
     * Report an error.
     *
     * @param msg the error message
     */
    public void append(String msg) {
        errors.add(msg);
    }

    /**
     * Get the number of errors
     *
     * @return an integer
     */
    public int size() {
        return errors.size();
    }

    /**
     * Print all the errors, one per line.
     *
     * @return all the reported errors
     */
    @Override
	public String toString() {
        StringBuilder b = new StringBuilder();
        for (String l : errors) {
            b.append(l);
            b.append("\n");
        }
        return b.toString();
    }
}
