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

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.Configuration;
import entropy.configuration.parser.PlainTextConfigurationSerializer;

/**
 * Prepare the unit tests and provides some tools to read resources for tests purpose.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestHelper {

    /**
     * The location of the properties file of the distribution.
     */
    public static final String DISTRIBUTION_PROPERTIES = "src/main/config/entropy.properties";

    /**
     * Read a configuration file. The test fails in an error occurred.
     *
     * @param file the file to read
     * @return the configuration
     */
    public static Configuration readConfiguration(String file) {
        Configuration c = null;
        try {
            c = PlainTextConfigurationSerializer.getInstance().read(file);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        return c;
    }

    /**
     * Read a properties file. The test fails in an error occurred.
     *
     * @param file the file to read
     * @return the properties
     */
    public static PropertiesHelper readEntropyProperties(String file) {
        PropertiesHelper p = null;
        try {
            p = new PropertiesHelper(file);
        } catch (IOException e) {
            Assert.fail(e.getMessage(), e);
        }
        return p;
    }

    /**
     * Read the properties provided in the distribution. The test fails in an error occurred.
     *
     * @return the properties
     */
    public static PropertiesHelper readDefaultEntropyProperties() {
        PropertiesHelper p = null;
        try {
            p = new PropertiesHelper(DISTRIBUTION_PROPERTIES);
        } catch (IOException e) {
            Assert.fail(e.getMessage(), e);
        }
        return p;
    }
}
