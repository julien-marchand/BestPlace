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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A tool to retrieve properties used by entropy.
 *
 * @author Fabien Hermenier
 */
public final class PropertiesHelper {

    /**
     * The filename that contains the properties.
     */
    public static final String DEFAULT_PROP_FILE = "config/entropy.properties";

    /**
     * All the properties.
     */
    private Properties props;

    //Some common properties

    /**
     * The username to use to perform operations.
     */
    public static final String AUTH_USERNAME_PROPERTY = "auth.username";


    /**
     * The private key to use to perform some authentification.
     */
    public static final String AUTH_PRIVATE_KEY_PROPERTY = "auth.privateKey";

    /**
     * Make a new PropertiesHelper using {@value #DEFAULT_PROP_FILE}.
     *
     * @throws IOException if an error occurs while reading the file
     */
    public PropertiesHelper() throws IOException {
        this(DEFAULT_PROP_FILE);
    }


    /**
     * Make a new PropertiesHelper.
     *
     * @param file The property file
     * @throws IOException if an error occurs while reading the file
     */
    public PropertiesHelper(String file) throws IOException {
        this.props = new Properties();
        InputStream reader = null;
        try {
            reader = new FileInputStream(file);
            this.props.load(reader);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public PropertiesHelper(InputStream in) throws IOException {
        this.props = new Properties();
        this.props.load(in);
    }

    /**
     * Get a required property value as a String.
     *
     * @param key the identifier of the property
     * @return the value of the property
     * @throws MissingRequiredPropertyException
     *          if the property is unknown.
     */
    public String getRequiredProperty(String key) throws MissingRequiredPropertyException {
        if (!this.props.containsKey(key)) {
            throw new MissingRequiredPropertyException(key);
        }
        return this.props.getProperty(key);
    }

    /**
     * Get a required property value as an Integer.
     *
     * @param key the identifier of the property
     * @return the integer value of the property
     * @throws MissingRequiredPropertyException
     *                                    if the property is unknown.
     * @throws WrongPropertyTypeException if its value can not be parsed as an Integer
     */
    public int getRequiredPropertyAsInt(String key) throws MissingRequiredPropertyException, WrongPropertyTypeException {
        int v;
        String value = null;
        try {
            value = this.getRequiredProperty(key);
            v = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new WrongPropertyTypeException(key, Integer.class, value);
        }
        return v;
    }

    /**
     * Get a required property value as a Boolean.
     *
     * @param key the identifier of the property
     * @return the boolean value of the property
     * @throws MissingRequiredPropertyException
     *                                    if the property is unknown.
     * @throws WrongPropertyTypeException if its value can not be parsed as an Integer
     */
    public boolean getRequiredPropertyAsBoolean(String key) throws MissingRequiredPropertyException, WrongPropertyTypeException {
        String value = null;
        boolean b;
        try {
            value = this.getRequiredProperty(key);
            b = Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            throw new WrongPropertyTypeException(key, Boolean.class, value);
        }
        return b;
    }

    /**
     * Get the value of an optional property.
     *
     * @param key          the identifier of the property
     * @param defaultValue the default value
     * @return the value of the property or the default value if the property is not set
     */
    public String getOptionalProperty(String key, String defaultValue) {
        return this.props.getProperty(key, defaultValue);
    }

    /**
     * Get the integer value of an optional property.
     *
     * @param key          the identifier of the property
     * @param defaultValue the default value
     * @return the value of the property or the default value if the property is not set
     * @throws WrongPropertyTypeException if its value can not be parsed as an Integer
     */
    public int getOptionalProperty(String key, int defaultValue) throws WrongPropertyTypeException {
        String rep = this.props.getProperty(key);
        if (rep != null) {
            try {
                return Integer.parseInt(rep);
            } catch (NumberFormatException e) {
                throw new WrongPropertyTypeException(key, Integer.class, rep);
            }
        }
        return defaultValue;
    }

    /**
     * Get the boolean value of an optional property.
     *
     * @param key          the identifier of the property
     * @param defaultValue the default value
     * @return the value of the property or the default value if the property is not set
     * @throws WrongPropertyTypeException if its value can not be parsed as an Integer
     */
    public boolean getOptionalProperty(String key, boolean defaultValue) throws WrongPropertyTypeException {
        String rep = this.props.getProperty(key);
        if (rep != null) {
            try {
                return Boolean.parseBoolean(rep);
            } catch (NumberFormatException e) {
                throw new WrongPropertyTypeException(key, Boolean.class, rep);
            }
        }
        return defaultValue;
    }

    /**
     * Check if a property is defined.
     *
     * @param key the key of the property.
     * @return true if the property is defined
     */
    public boolean isDefined(String key) {
        return this.props.containsKey(key);
    }

    @Override
    public String toString() {
        return props.toString();
    }
}
