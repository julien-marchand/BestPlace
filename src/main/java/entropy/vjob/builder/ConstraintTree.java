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

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.Token;

import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJob;
import entropy.vjob.VJobElement;

/**
 * A tree to build a constraint. Root
 * of the tree is the constraint identifier while childs are the parameters.
 *
 * @author Fabien Hermenier
 */
public class ConstraintTree extends VJobTree {

    private VJob vjob;

    /**
     * The constraint catalog that contains the available constraints.
     */
    private ConstraintsCatalog catalog;

    /**
     * Make a new Tree parser.
     *
     * @param t    the root symbol
     * @param errs the errors to report
     * @param c    the catalog of available constraints
     * @param v    the VJob to fullfil with the parsed constraints.
     */
    public ConstraintTree(Token t, SemanticErrors errs, ConstraintsCatalog c, VJob v) {
        super(t, errs);
        this.catalog = c;
        this.vjob = v;
    }

    /**
     * Build the constraint.
     * The constraint is builded if it exists in the catalog and if the parameters
     * are compatible with the constraint signature.
     *
     * @param parent the parent of the root
     * @return {@code Content.empty} if the constraint is successfully builded.
     *         {@code Content.ignore} if an error occured (the error is already reported)
     */
    @Override
    public Content go(VJobTree parent) {
        String cname = getText().substring(0, getText().length() - 1);
        //Get the params
        List<VJobElement> params = new ArrayList<VJobElement>();
        for (int i = 0; i < getChildCount(); i++) {
            Content c = ((VJobTree) getChild(i)).go(this);
            if (c == Content.ignore) {
                return Content.ignore;
            } else if (c.type() == Content.Type.integer) {
                params.add(new NumberElement((Integer) c.content()));
            } else {
                params.add(((VJobElement) c.content()));
            }

        }
        try {
            PlacementConstraint c = catalog.buildConstraint(cname, params);
            if (c == null) {
                return ignoreError("Constraint '" + cname + "' is not in the catalog");
            }
            vjob.addConstraint(c);
        } catch (ConstraintBuilderException e) {
            ignoreError(e.getMessage());
        }
        return Content.empty;
    }
}
