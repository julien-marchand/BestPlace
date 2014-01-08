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

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import entropy.controlLoop.ControlLoopFactoryException;
import entropy.monitoring.MonitorFactoryException;

/**
 * The launcher of Entropy.
 *
 * @author Fabien Hermenier
 */
public final class EntropyLauncher {

    /**
     * The default properties file to managed Entropy.
     */
    public static final String DEFAULT_PROPERTIES = "config/entropy.properties";


    /**
     * No instantiation please.
     */
    private EntropyLauncher() {

    }

    /**
     * Print the command line usage on the standard error and exit.
     */
    public static void usage() {
        System.out.println("Usage: entropy.Entropy start|stop [config_file]");
        System.out.println("config_file: the path to the configuration file of entropy (default is '" + DEFAULT_PROPERTIES + "')");
    }

    /**
     * Launcher of Entropy.
     *
     * @param args first argument must be start or stop. Secong argument is optional and denotes the configuration file
     *             of Entropy
     */
    public static void main(String[] args) {
        if (args.length != 1 && args.length != 2) {
            usage();
            System.exit(1);
        }

        PropertiesHelper properties = null;
        try {
            if (args.length == 1) {
                properties = new PropertiesHelper();
            } else {
                properties = new PropertiesHelper(args[1]);
            }
        } catch (IOException e) {
            Entropy.getLogger().error("Unable to read the configuration file: " + e.getMessage());
            System.exit(1);
        }


        if (args[0].equals("start")) {
            try {
                Entropy e = EntropyBuilder.buildFromProperties(properties);
                e.startup();
            } catch (IOException e) {
                Entropy.getLogger().error("Unable to start entropy: " + e.getMessage());
                System.exit(1);
            } catch (EntropyException e) {
                Entropy.getLogger().error("Unable to start entropy: " + e.getMessage());
                System.exit(1);
            } catch (MonitorFactoryException e) {
                Entropy.getLogger().error("Unable to start entropy: " + e.getMessage());
                System.exit(1);
            } catch (PropertiesHelperException e) {
                Entropy.getLogger().error("Unable to start entropy: " + e.getMessage());
                System.exit(1);
            } catch (ControlLoopFactoryException e) {
                Entropy.getLogger().error("Unable to start entropy: " + e.getMessage());
            }
        } else if (args[0].equals("stop")) {
            try {
                int port = properties.getRequiredPropertyAsInt(EntropyBuilder.REGISTRY_PORT_KEY);
                Entropy.getLogger().info("Waiting for shutdown.");
                JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + port + "/" + Entropy.MBEAN_NAME);
                JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
                MBeanServerConnection conn = jmxc.getMBeanServerConnection();
                conn.invoke(new ObjectName(Entropy.MBEAN_NAME), "shutdown", new Object[]{}, new String[]{});
            } catch (IOException e) {
                Entropy.getLogger().error("Unable to shutdown entropy: " + e.getMessage());
                System.exit(1);
            } catch (MalformedObjectNameException e) {
                Entropy.getLogger().error("Unable to shutdown entropy: " + e.getMessage());
                System.exit(1);
            } catch (PropertiesHelperException e) {
                Entropy.getLogger().error("Unable to shutdown entropy: " + e.getMessage());
                System.exit(1);
            } catch (MBeanException e) {
                Entropy.getLogger().error("Unable to shutdown entropy: " + e.getMessage());
                System.exit(1);
            } catch (ReflectionException e) {
                Entropy.getLogger().error("Unable to shutdown entropy: " + e.getMessage());
                System.exit(1);
            } catch (InstanceNotFoundException e) {
                Entropy.getLogger().error("Unable to shutdown entropy: " + e.getMessage());
                System.exit(1);
            }
        } else {
            usage();
        }
    }

}
