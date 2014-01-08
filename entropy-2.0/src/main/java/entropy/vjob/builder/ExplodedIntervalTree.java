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
import entropy.vjob.RangeOfElements;

/**
 * A Tree to parse an exploded range of elements.
 *
 * @author Fabien Hermenier
 */
public class ExplodedIntervalTree extends VJobTree {

    /**
     * The builder to make the element.
     */
    private VJobElementBuilder elemBuilder;

    /**
     * Make a new parser.
     *
     * @param t    the root node
     * @param errs the errors to report
     * @param eb   the builder to make the element
     */
    public ExplodedIntervalTree(Token t, SemanticErrors errs, VJobElementBuilder eb) {
        super(t, errs);
        this.elemBuilder = eb;
    }

    @Override
    public Content go(VJobTree parent) {
        Integer start = (Integer) ((VJobTree) getChild(1)).go(this).content();

        String head = getChild(0).getText().substring(0, getChild(0).getText().length() - 1);
        String tail = getChild(getChildCount() - 1).getText().substring(1);

        Token firstTok = ((VJobTree) getChild(0)).token;
        String first = head + start + tail;

        RangeOfElements ex;

        StringBuilder range = new StringBuilder(head);
        range.append("[");
        for (int i = 1; i < getChildCount() - 1; i++) {
            range.append(getChild(i).getText());
            if (i != getChildCount() - 2) {
                range.append(", ");
            }
        }
        range.append("]");
        range.append(tail);
        VirtualMachine vm = elemBuilder.matchAsVirtualMachine(first);
        if (vm != null) {
            ex = new RangeOfElements<VirtualMachine>(range.toString(), range.toString());
            for (int i = 1; i <= getChildCount() - 2; i++) {
                vm = elemBuilder.matchAsVirtualMachine(head + getChild(i).getText() + tail);
                if (vm == null) {
                    errors.append(firstTok, "element " + head + getChild(i).getText() + tail + " is not a virtual machine");
                    return Content.ignore;
                }
                ex.add(vm);
            }
            return new Content(Content.Type.vmset, ex);
        }
        Node n = elemBuilder.matchAsNode(first);
        if (n != null) {
            ex = new RangeOfElements<Node>(range.toString(), range.toString());
            for (int i = 1; i <= getChildCount() - 2; i++) {
                n = elemBuilder.matchAsNode(head + getChild(i).getText() + tail);
                if (n == null) {
                    errors.append(firstTok, "element " + head + getChild(i).getText() + tail + " is not a node");
                    return Content.ignore;
                }
                ex.add(n);
            }
            return new Content(Content.Type.nodeset, ex);
        } else {
            errors.append(firstTok, "element " + first + " is not a VM nor a node");
            return Content.ignore;
        }
    }
}
