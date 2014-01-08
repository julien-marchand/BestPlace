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

package entropy.execution.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Tool to execute remote shell command on a SSH server.
 * The tool support authentification through password or identity file.
 *
 * @author Fabien Hermenier
 */
public class SSHExec {

    /**
     * Default listening port of the ssh server.
     */
    public static final int DEFAULT_PORT = 22;

    /**
     * The port of the ssh server.
     */
    private int port;

    /**
     * The user used to log on the node.
     */
    private String user;

    /**
     * The hostname of the server.
     */
    private String hostname;

    /**
     * The current SSH session.
     */
    private Session session;

    /**
     * The buffer that contains all the standard output.
     */
    private StringBuilder outputBuffer;

    /**
     * The buffer that contains all the standard error.
     */
    private StringBuilder errorBuffer;

    /**
     * The SSH toolkit.
     */
    private JSch jsch;

    /**
     * Get the port of the server.
     *
     * @return the port
     */
    public final int getPort() {
        return this.port;
    }


    /**
     * Get the username on the server.
     *
     * @return the login of the user
     */
    public final String getUsername() {
        return this.user;
    }

    /**
     * Make a new instance with the current user.
     *
     * @param host the hostname of the SSH server
     * @throws JSchException If an error occurs
     */
    public SSHExec(String host) throws JSchException {
        this(host, DEFAULT_PORT, System.getenv("user.name"));

    }

    /**
     * Make a new instance.
     *
     * @param host     the hostname of the server
     * @param username the login of the user
     * @param p        the listening port
     * @throws JSchException If an error occurs
     */
    public SSHExec(String host, int p, String username) throws JSchException {
        this.hostname = host;
        this.user = username;
        this.port = p;

        this.outputBuffer = new StringBuilder();
        this.errorBuffer = new StringBuilder();

        jsch = new JSch();
        session = jsch.getSession(this.getUsername(), this.getHostname(), this.getPort());
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

    }

    /**
     * Use an identity file as authentification.
     *
     * @param filename The absolute pathname of the private key
     * @throws JSchException if an error occurs.
     */
    public void useIdentity(String filename) throws JSchException {
        this.jsch.addIdentity(filename);
    }

    /**
     * Use an identity file and a passphrase as authentification.
     *
     * @param filename   The absolute pathname of the private key
     * @param passPhrase the passphrase
     * @throws JSchException if an error occurs
     */
    public void useIdentity(String filename, String passPhrase) throws JSchException {
        this.jsch.addIdentity(filename, passPhrase);
    }

    /**
     * Use a password as authentification.
     *
     * @param password the password
     */
    public void usePassword(String password) {
        this.session.setPassword(password);
    }

    /**
     * Get the hostname of the server.
     *
     * @return the hostname
     */
    public final String getHostname() {
        return this.hostname;
    }

    /**
     * Return the standard output after the execution of the command.
     *
     * @return the content of the standard output, may be empty
     */
    public String getStandartOutput() {
        return this.outputBuffer.toString();
    }

    /**
     * Return the standard error output after the execution of the command.
     *
     * @return the content of the standard error output, may be empty
     */
    public String getStandardError() {
        return this.errorBuffer.toString();
    }

    /**
     * Execute a command through a SSH session.
     *
     * @param command the command to execute
     * @return the exit code
     * @throws JSchException if an error occurs
     * @throws IOException   if an error occurs
     */
    public int executeCommand(String command) throws JSchException, IOException {

        session.connect();

        int exitCode = -1;
        ChannelExec channel = null;
        BufferedReader stdoutReader = null;
        BufferedReader stderrReader = null;


        outputBuffer = new StringBuilder();
        errorBuffer = new StringBuilder();
        try {
            channel = (ChannelExec) session.openChannel("exec");

            channel.setCommand(command + "\n");

            InputStream stdout = channel.getInputStream();
            InputStream stderr = channel.getErrStream();

            channel.connect();

            stdoutReader = new BufferedReader(new InputStreamReader(stdout));
            stderrReader = new BufferedReader(new InputStreamReader(stderr));

            while (stdoutReader.read() != -1) {
                outputBuffer.append(stdoutReader.readLine());
            }

            while (stderrReader.read() != -1) {
                errorBuffer.append(stderrReader.readLine());
            }

            exitCode = channel.getExitStatus();

            return exitCode;
        } finally {
            if (stdoutReader != null) {
                stdoutReader.close();
            }
            if (stderrReader != null) {
                stderrReader.close();
            }
            if (channel != null) {
                channel.disconnect();
                session.disconnect();
            }
        }        
    }    
}
