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

package entropy.monitoring.ganglia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import entropy.configuration.Configuration;
import entropy.monitoring.ConfigurationAdapter;
import entropy.monitoring.MonitoringException;

/**
 * Adapter that extract a configuration from a Ganglia meta daemon.
 * The adapter create a connection to the ganglia meta daemon and retrieve an XML output
 * of the current monitoring metrics.
 *
 * @author Fabien Hermenier
 * @see GangliaMetaXMLParser for metrics specifications.
 */
public class GangliaConfigurationAdapter extends ConfigurationAdapter {

    /**
     * Default port of the ganglia meta server.
     */
    public static final int DEFAULT_PORT = 8651;

    /**
     * The hostname to connect to.
     */
    private String host;

    /**
     * The port to connect to.
     */
    private int port;

    /**
     * Make a new adapter that request a ganglia meta daemon on the default port.
     *
     * @param hostname the hostname of the ganglia meta daemon
     */
    public GangliaConfigurationAdapter(String hostname) {
        this(hostname, DEFAULT_PORT);
    }

    /**
     * Make a new adapter that request a ganglia meta daemon on a specific port.
     *
     * @param hostname the hostname of the ganglia meta daemon
     * @param p        the port
     */
    public GangliaConfigurationAdapter(String hostname, int p) {
        this.port = p;
        this.host = hostname;
    }

    /**
     * Get a XML dump from a ganglia meta daemon.
     *
     * @return A String that contains all the dump
     * @throws MonitoringException if an error occurred during the read.
     */
    public String readXMLDump() throws MonitoringException {
        StringBuilder buffer = new StringBuilder();

        try {
            Socket s = new Socket(this.host, this.port);
            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (UnknownHostException e) {
            throw new MonitoringException("Unknown host: " + this.host + ":" + this.port, e);
        } catch (IOException e) {
            throw new MonitoringException("Unable to get the monitoring report from the GMeta daemon", e);
        }

        return buffer.toString().trim();
    }

    /**
     * Parse a configuration from a XML output of a Ganglia meta daemon.
     *
     * @param buffer the XML buffer
     * @return a Configuration
     * @throws MonitoringException if an error occured
     */
    public Configuration parseConfiguration(String buffer) throws MonitoringException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        javax.xml.parsers.SAXParser parser;
        GangliaMetaXMLParser gangliaXMLParser;
        try {
            parser = factory.newSAXParser();
            gangliaXMLParser = new GangliaMetaXMLParser(this);
            parser.parse(new InputSource(new StringReader(buffer)), gangliaXMLParser);

        } catch (ParserConfigurationException e) {
            throw new MonitoringException("Error while parsing the configuration", e);
        } catch (SAXException e) {
            throw new MonitoringException("Error while parsing the XML stream of GMetad", e);
        } catch (IOException e) {
            throw new MonitoringException("I/O error", e);
        }
        return gangliaXMLParser.getConfiguration();
    }

    @Override
    public Configuration extractConfiguration() throws MonitoringException {
        return parseConfiguration(this.readXMLDump());
    }


    /**
     * Get the hostname of the Ganglia meta daemon.
     *
     * @return the hostname
     */
    public String getHostname() {
        return this.host;
    }

    /**
     * Get the port used by the Ganglia meta daemon.
     *
     * @return the port
     */
	public int getPort() {
		return this.port;
	}

}
