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
import entropy.plan.action.Stop;

/**
 * A driver to perform a stop action with an SSH command.
 * 
 * <table>
 * <caption>Properties relative to SSHXenStop</caption>
 * <tr><td>Identifier (<i>type</i>)</td><td>Description</td><td>Default value</td></tr>
 * <tr><td>{@value entropy.PropertiesHelper#AUTH_USERNAME_PROPERTY} (<i>string</i>)</td><td> The username to use when logging</td><td> <i>current user</i></td></tr>
 * <tr><td>{@value entropy.PropertiesHelper#AUTH_PRIVATE_KEY_PROPERTY} (<i>string</i>)</td><td> The pathname to the SSH private key</td><td> - (required)</td></tr>
 * </table>
 * @author Fabien Hermenier
 *
 */
public class SSHStop extends SSHDriver {

	/**
	 * The identifier that define {@link #command}.
	 */
	public static final String COMMAND_PROPERTY = "driver.sshStop.command";
		
	/**
	 * The command to execute.
	 */
	private String command;

	/**
	 * The identifier of the hosting node.
	 */
	public static final String HOST_NODE_ID = "\\$HOST";
	
	/**
	 * The identifier of the virtual machine.
	 */
	public static final String VM_ID = "\\$VM";

	/**
	 * THe action to execute.
	 */
	private Stop action;

	
	/**
	 * Create and configure the driver to execute a stop action.
	 * @param a the action to execute
	 * @param properties the properties to configure the driver
     * @throws entropy.PropertiesHelperException if an error occurred while reading the properties of the driver
	 */

	public SSHStop(Stop a, PropertiesHelper properties) throws PropertiesHelperException {
		super(a, properties);
		this.action = a;
		this.command = properties.getRequiredProperty(COMMAND_PROPERTY);
	}
			
	@Override
	public String getCommandToExecute() {
		return command.replaceAll(VM_ID, this.action.getVirtualMachine().getName()).		
		replaceAll(HOST_NODE_ID, action.getHost().getName());
	}

	@Override
	public String getRemoteHostname() {
		return this.action.getHost().getName();
	}

}
