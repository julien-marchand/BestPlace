/*
 * Copyright (c) Fabien Hermenier
 *
 * This file is part of Entropy.
 *
 * Entropy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Entropy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.configuration.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import entropy.configuration.Configuration;

/**
 * Basic interface to specify a serializer of a Configuration.
 *
 * @author Fabien Hermenier
 */
public interface ConfigurationSerializer {

    /**
     * Un-serialize a Configuration.
     *
     * @param in the stream to read.
     * @return the configuration
     * @throws IOException if an error occurred while reading the stream
     * @throws ConfigurationSerializerException
     *                     if an error occurred while parsing datas
     */
    Configuration unSerialize(InputStream in) throws IOException, ConfigurationSerializerException;

    /**
     * Serialize a Configuration into a stream.
     *
     * @param cfg the configuration to serialize
     * @param out the stream to write to
     * @throws IOException if an error occurred while writing to the stream
     */
    void serialize(Configuration cfg, OutputStream out) throws IOException;
}
