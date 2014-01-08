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
 * An exception to signal that the value of a property do not have the good type
 * @author Fabien Hermenier
 */
public class WrongPropertyTypeException extends PropertiesHelperException {

    /**
     * The identifier of the property.
     */
    private String prop;

    /**
     * The expected type of the property.
     */
    private Class type;

    /**
     * The current value of the property.
     */
    private String value;

    /**
     * Make a new exception.
     * @param key the identifier of the property.
     * @param t the expected type of the value
     * @param v the current value.
     */
    public WrongPropertyTypeException(String key, Class t, String v) {
        super("Wrong type for property '" + key + "'. Unable to convert '" + v + "' into a " + t.getSimpleName());
        this.prop = key;
        this.type = t;
        this.value = v;
    }

    /**
     * Get the identifier of the property.
     * @return a String
     */
    public String getProperty() {
        return this.prop;
    }

    /**
     * Get the current value of the property.
     * @return a String
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Get the expected class of the property.
     * @return a Class
     */
    public Class getExpectedClass() {
        return this.type;
    }
}
