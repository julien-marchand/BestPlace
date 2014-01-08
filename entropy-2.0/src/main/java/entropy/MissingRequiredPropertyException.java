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

package entropy;

/**
 * An exception to signal a missing property that was required.
 * @author Fabien Hermenier
 */
public class MissingRequiredPropertyException extends PropertiesHelperException {

    /**
     * The identifier of the missing property.
     */
    private String prop;

    /**
     * Make a new exception.
     * @param property the identifier of the missing property.
     */
    public MissingRequiredPropertyException(String property) {
        super("Missing required property '" + property + "'");
        this.prop = property;
    }

    /**
     * Get the identifier of the property.
     * @return a String
     */
    public String getProperty() {
        return this.prop;
    }
}
