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

package entropy.plan.parser;

import java.io.IOException;

import entropy.plan.TimedReconfigurationPlan;

/**
 * A Factory to select the serializer to use when reading or writing
 * a configuration file. The serializer is selected using the file extension.
 * <p/>
 * Supported Serializer:
 * <ul>
 * <li>{@link entropy.configuration.parser.PlainTextConfigurationSerializer}: File extension must be {@value #PLAIN_TEXT_EXTENSION}. </li>
 * <li>{@link entropy.configuration.parser.ProtobufConfigurationSerializer}: File extension must be {@value #PROTOBUF_EXTENSION}. </li>
 * </ul>
 *
 * @author Fabien Hermenier
 */
public final class FileTimedReconfigurationPlanSerializerFactory {

    /**
     * Singleton.
     */
    private static final FileTimedReconfigurationPlanSerializerFactory INSTANCE = new FileTimedReconfigurationPlanSerializerFactory();

    /**
     * File extension to specify a storage using protobuf.
     */
    public static final String PROTOBUF_EXTENSION = ".pbd";

    /**
     * File extension to specify a storage using plain text.
     */
    public static final String PLAIN_TEXT_EXTENSION = ".txt";

    /**
     * Singleton, so private instantiation.
     */
    private FileTimedReconfigurationPlanSerializerFactory() {
    }

    /**
     * Get the instance of the factory.
     *
     * @return the instance
     */
    public static FileTimedReconfigurationPlanSerializerFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Read a plan from a file.
     * The serializer to use is inferred using the file extension.
     *
     * @param path the path to the file.
     * @return the parsed plan or {@code null} if no serializer is associated to the file extension
     * @throws java.io.IOException if an error occurred while reading the file
     * @throws TimedReconfigurationPlanSerializerException
     *                             if an error occurred while parsing the file
     */
    public TimedReconfigurationPlan read(String path) throws IOException, TimedReconfigurationPlanSerializerException {
        FileTimedReconfigurationPlanSerializer s = getSerializer(path);
        if (s == null) {
            return null;
        }
        return getSerializer(path).read(path);
    }

    /**
     * Write a plan to a file.
     * The serializer to use is inferred from the file extension.
     *
     * @param plan the configuration to write
     * @param path the path to the file
     * @return {@code true} if the operation succeed, {@code false} if no serializer is associated to the file extension
     * @throws java.io.IOException if an error occurred while writing the file
     */
    public boolean write(TimedReconfigurationPlan plan, String path) throws IOException {
        FileTimedReconfigurationPlanSerializer s = getSerializer(path);
        if (s == null) {
            return false;
        }
        s.write(plan, path);
        return true;
    }

    /**
     * Get the serialized that fit with the given filename.
     *
     * @param path the path to the filename
     * @return a compatible serializer or {@code null}
     */
    public FileTimedReconfigurationPlanSerializer getSerializer(String path) {
        if (path.endsWith(PLAIN_TEXT_EXTENSION)) {
            return PlainTextTimedReconfigurationPlanSerializer.getInstance();
        } else if (path.endsWith(PROTOBUF_EXTENSION)) {
            return ProtobufTimedReconfigurationPlanSerializer.getInstance();
        }
        return null;
    }
}
