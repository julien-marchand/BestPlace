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

import entropy.PropertiesHelper;
import entropy.PropertiesHelperException;
import entropy.plan.action.Shutdown;


/**
 * Driver to perform the shutdown of a node through a SSH connection on the node.
 * The SSH connection is based on a key infrastructure.
 * 
 * <table>
 * <caption>Properties relative to SSHShutdown</caption>
 * <tr><td>Identifier (<i>type</i>)</td><td>Description</td><td>Default value</td></tr>
 * <tr><td>{@value entropy.PropertiesHelper#AUTH_USERNAME_PROPERTY} (<i>string</i>)</td><td> The username to use when logging</td><td> - (required)</td></tr>
 * <tr><td>{@value entropy.PropertiesHelper#AUTH_PRIVATE_KEY_PROPERTY} (<i>string</i>)</td><td> The pathname to the SSH private key/td><td> - (required)</td></tr>
 * </table>
 * @author Fabien Hermenier
 * 
 *  FIXME: Up to date as non really used
 */
public class SSHShutdown extends SSHDriver {
			
	/**
	 * The identifier that define {@link #command}.
	 */
	public static final String COMMAND_PROPERTY = "driver.sshShutdown.command";

	
	/**
	 * The command to execute.
	 */
	private String command;

	/**
	 * The identifier of the hosting node.
	 */
	public static final String HOST_NODE_ID = "\\$HOST";

	
	/**
	 * New action with default parameters.
	 * The server is listening on SSHAction.DEFAULT_PORT and the user to log with
	 * is the current user
	 * @param action the shutdown action
	 * @param props the properties to configure the driver
     * @throws entropy.PropertiesHelperException if an error occurred while configuring the driver
     *
	 */
	public SSHShutdown(Shutdown action, PropertiesHelper props) throws PropertiesHelperException {
		super(action, props);        
		this.command = props.getRequiredProperty(COMMAND_PROPERTY);		
	}

	@Override
	public String getCommandToExecute() {
		return command.replaceAll(HOST_NODE_ID, getRemoteHostname());
	}

	@Override
	public String getRemoteHostname() {
		return ((Shutdown)getAction()).getNode().getName();
	}
}
