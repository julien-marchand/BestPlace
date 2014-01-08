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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import entropy.plan.TimedReconfigurationPlan;

/**
 * A wrapper to provide serialization and de-serialization to and from a file.
 *
 * @author Fabien Hermenier
 */
public abstract class FileTimedReconfigurationPlanSerializer implements TimedReconfigurationPlanSerializer {

    /**
     * Read a plan from a file.
     *
     * @param path the file
     * @return the plan
     * @throws java.io.IOException if an error occurred while reading the file
     * @throws TimedReconfigurationPlanSerializerException
     *                             if an error occurred while parsing the file
     */
    public final TimedReconfigurationPlan read(String path) throws IOException, TimedReconfigurationPlanSerializerException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(path);
            return unSerialize(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Write a configuration to a file.
     * If the parent folder does not exists, it is created.
     *
     * @param plan the plan to write
     * @param path of the file
     * @throws java.io.IOException if an error occurred while writing the file
     */
    public final void write(TimedReconfigurationPlan plan, String path) throws IOException {
        File f = new File(path);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Unable to create folder '" + parent.getAbsolutePath() + "'");
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            serialize(plan, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
