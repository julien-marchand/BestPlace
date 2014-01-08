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
import entropy.plan.action.Migration;

/**
 * A driver to perform a migration by executing a command on the source node.
 * The command is executed through a SSH Session.
 * 
 * <table>
 * <tr><td>Name of the property</td><td>Description</td><td>Default value</td></tr>
 * <tr>
 * 	<td>{@value #COMMAND_PROPERTY} (<i>String</i>)</td>
 *  <td>The command to execute on the hosting node. The Strings "$VM", "$SOURCE" and "$DESTINATION" will be replaced respectively
 *  by the name of the virtual machine, the name of the source node and the name of the destination node</td>
 *  <td>-</td>
 * </tr>
 * <tr><td>{@value entropy.PropertiesHelper#AUTH_USERNAME_PROPERTY} (<i>string</i>)</td><td> The username to use when logging</td><td> <i>current user</i></td></tr>
 * <tr><td>{@value entropy.PropertiesHelper#AUTH_PRIVATE_KEY_PROPERTY} (<i>string</i>)</td><td> The pathname to the SSH private key</td><td> - (required)</td></tr>

 * </table>
 * @author Fabien Hermenier
 *
 */
public class SSHMigration extends SSHDriver {

	/**
	 * The identifier that define {@link #command}.
	 */
	public static final String COMMAND_PROPERTY = "driver.sshMigration.command";
	
	/**
	 * The command to execute.
	 */
	private String command;

	/**
	 * The action to execute.
	 */
	private Migration action;
	
	/**
	 * The identifier of the source node.
	 */
	public static final String SOURCE_NODE_ID = "\\$SOURCE";
	
	/**
	 * The identifier of the destination node.
	 */
	public static final String DESTINATION_NODE_ID = "\\$DESTINATION";
	
	/**
	 * The identifier of the virtual machine.
	 */
	public static final String VM_ID = "\\$VM";
		
	
	/**
	 * Create and configure the driver to execute a migration action.
	 * @param a the action to execute
	 * @param properties the properties to configure the driver
     * @throws entropy.PropertiesHelperException if an error occurred while configuring the driver
     *
	 */
	public SSHMigration(Migration a, PropertiesHelper properties) throws PropertiesHelperException {
		super(a, properties);
		this.command = properties.getRequiredProperty(COMMAND_PROPERTY);	
		this.action = a;
	}
	
	@Override
	public String getRemoteHostname() {
		return this.action.getHost().getName();
	}

	@Override
	public String getCommandToExecute() {
		return command.replaceAll(VM_ID, this.action.getVirtualMachine().getName()).
		replaceAll(SOURCE_NODE_ID, action.getHost().getName()).
		replaceAll(DESTINATION_NODE_ID, action.getDestination().getName());
	}
}
