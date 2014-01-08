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

import java.io.File;
import java.io.IOException;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import entropy.vjob.BasicVJob;
import entropy.vjob.VJob;

/**
 * Build VJobs from textual descriptions.
 * A prolog vjob may be used to consider predefined variables.
 *
 * @author Fabien Hermenier
 */
public class VJobBuilder {

    /**
     * An optional prolog where to get variables.
     */
    private VJob prolog;

    /**
     * The builder to instantiate new elements.
     */
    private VJobElementBuilder elemBuilder;

    /**
     * The catalog of available constraints.
     */
    private ConstraintsCatalog catalog;

    /**
     * Make a new builder.
     *
     * @param eBuilder the builder to instantiate new elements
     * @param c        the catalog of available constraints
     */
    public VJobBuilder(VJobElementBuilder eBuilder, ConstraintsCatalog c) {
        elemBuilder = eBuilder;
        catalog = c;
        prolog = null;
    }

    /**
     * Set the prolog to use.
     *
     * @param p the vjob to use as prolog
     */
    public void setProlog(VJob p) {
        prolog = p;
    }

    /**
     * Get the prolog.
     *
     * @return the vjob used as a prolog if defined, {@code null} otherwise
     */
    public VJob getProlog() {
        return prolog;
    }

    /**
     * Get the builder to make managed elements.
     *
     * @return the builder
     */
    public VJobElementBuilder getElementBuilder() {
        return elemBuilder;
    }

    /**
     * Build a VJob from a file containing the description.
     *
     * @param id the identifier of the vjob
     * @param f  the file to read
     * @return the builded vjob
     * @throws VJobBuilderException if an error occurred while building the vjob
     * @throws IOException          if an error occurred while reading the file
     */
    public VJob build(String id, File f) throws VJobBuilderException, IOException {
        try {
            return build(id, new ANTLRFileStream(f.getAbsolutePath()));
        } catch (IOException e) {
            throw new VJobBuilderException(e.getMessage(), e);
        }
    }

    /**
     * Build a VJob from a String.
     *
     * @param id          the identifier of the vjob
     * @param description the description of the vjob
     * @return the builded vjob
     * @throws VJobBuilderException if an error occurred while buildeing the vjob
     */
    public VJob build(String id, String description) throws VJobBuilderException {
        return build(id, new ANTLRStringStream(description));
    }

    /**
     * Internal method to build a vjob from a stream.
     *
     * @param id the identifier of the vjob
     * @param cs the stream to analyze
     * @return the  builded vjob
     * @throws VJobBuilderException in an error occurred while building the vjob
     */
    private VJob build(String id, CharStream cs) throws VJobBuilderException {
        ANTLRVJob3Lexer lexer = new ANTLRVJob3Lexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ANTLRVJob3Parser parser = new ANTLRVJob3Parser(tokens);
        final VJob v = new BasicVJob(id);

        final SemanticErrors errs = new SemanticErrors();

        parser.setTreeAdaptor(new VJobTreeAdaptor(errs, new SymbolsTable(getProlog()), elemBuilder, v, catalog));
        try {
            VJobTree tree = (VJobTree) parser.vjob_decl().getTree();
            if (tree == null) { //No tokens, empty VJob
                return v;
            }
            if (tree.token != null) {
                tree.go(tree); //Single instruction
            } else {
                for (int i = 0; i < tree.getChildCount(); i++) {
                    ((VJobTree) tree.getChild(i)).go(tree);
                }
            }
        } catch (RecognitionException e) {
            throw new VJobBuilderException(e.getMessage(), e);
        }
        if (errs.size() > 0) {
            throw new VJobBuilderException(errs.toString());
        }
        return v;
    }

    /**
     * Get the used constraint catalog.
     *
     * @return a catalog, may be null if no catalog was specified as instantiation.
     */
    public ConstraintsCatalog getCatalog() {
        return catalog;
    }
}
