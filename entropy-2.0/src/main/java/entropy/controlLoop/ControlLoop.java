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

package entropy.controlLoop;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import entropy.configuration.Configuration;
import entropy.configuration.parser.PlainTextConfigurationSerializer;

/**
 * Abstract class that define a control loop.
 *
 * @author Fabien Hermenier
 */
public abstract class ControlLoop {

    /**
     * The logger for the current class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("Loop");

    /**
     * The root directory to log configurations.
     */
    private String logDir;

    /**
     * THe date format for configuration logging.
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * The hour format for configuration logging.
     */
    public static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("kk:mm:ss");

    private boolean mustExit = false;

    /**
     * Indicates wether the loop is asking for exiting.
     *
     * @return true if the loop has to terminate ASAP.
     */
    public boolean mustExit() {
        return mustExit;
    }

    /**
     * Set the exit status of the control loop.
     *
     * @param b true to ask the loop to termintate ASAP.
     */
    public void setExit(boolean b) {
        mustExit = b;
    }

    /**
     * Get the logger.
     *
     * @return an initialized logger
     */
    public static Logger getLogger() {
        return LOGGER;
    }

    /**
     * Execute an iteration of the loop.
     *
     * @return true if we have to leave the loop.
     */
    public abstract boolean runLoop();


    /**
     * Log a configuration into a file.
     * If an error occurs, it is logged at the error level
     *
     * @param c         the configuration the log.
     * @param timeStamp the timeStamp for the configuration
     * @param suffix    the suffix of the log file
     * @return the pathname of the log file
     */
    public String logConfiguration(Configuration c, Date timeStamp, String suffix) {
        if (logDir != null) {
            String filename = this.logDir + "/" + DATE_FORMAT.format(timeStamp) + "/"
                    + HOUR_FORMAT.format(timeStamp) + "-"
                    + suffix + ".txt";
            try {
                PlainTextConfigurationSerializer.getInstance().write(c, filename);
            } catch (Exception e) {
                getLogger().warn("Unable to store the configuration: " + e.getMessage());
            }
            return filename;
        }
        return null;
    }

    /**
     * Set the directory where logged configuration are stored.
     *
     * @param path the pathname
     */
    public void setLogsDir(String path) {
        this.logDir = path;
    }

    /**
     * Get the logs dir.
     *
     * @return the pathname of the logs dir
     */
    public String getLogsDir() {
        return this.logDir;
    }

    public void destroy() {

    }
}

