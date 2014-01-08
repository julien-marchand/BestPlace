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
package entropy.decision;

import entropy.configuration.Configuration;

/**
 * A customizable decision module. We user has to specify the state of each VM belonging to
 * the configuration. By default, all the state stay untouched.
 *
 * @author Fabien Hermenier
 */
public class CustomizableDecisionModule extends DecisionModule {

    /**
     * Compute the sample configuration.
     *
     * @return the sample configuration. Should be viable
     * @throws AssignmentException if an error occurred.
     */
    @Override
    public Configuration compute(Configuration currentConf) throws AssignmentException {
        return currentConf;
    }
}
