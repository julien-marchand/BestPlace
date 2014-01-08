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

/**
 * Specify the content of a VJobTree
 *
 * @author Fabien Hermenier
 */
public class Content {

    /**
     * Ignore the content.
     */
    public static Content ignore = new Content(Type.ignore);

    /**
     * An empty content.
     */
    public static Content empty = new Content(Type.empty);

    /**
     * Type of the content.
     */
    public static enum Type {
        empty, node, vm, nodeset, vmset, multinodesets, multivmsets, integer, ignore
    }

    /**
     * The current content.
     */
    private Object content;

    /**
     * The contents type
     */
    private Type type;

    /**
     * Build an empty content.
     */
    public Content() {
    }

    /**
     * Build a content with a specified type.
     * @param t the contents type
     * @param c the content
     */
    public Content(Type t, Object c) {
        this.type = t;
        this.content = c;
    }

    /**
     * Build a typed content.
     * @param t the contents type
     */
    public Content(Type t) {
        this.type = t;
    }

    /**
     * Get the content.
     * @return the content
     */
    public Object content() {
        return content;
    }

    /**
     * Get the contents type.
     * @return a type
     */
    public Type type() {
        return this.type;
    }
}
