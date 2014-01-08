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
import entropy.plan.action.Action;

/**
 * Abstract class to provide a Driver that execute an action using the xenapi on a remote host.
 * 
 * <table>
 * <tr><td>Name of the property</td><td>Description</td><td>Default value</td></tr>
 * <tr>
 * 	<td>{@value #USERNAME_PROPERTY} (<i>String</i>)</td>
 *  <td>The username used to log in</td>
 *  <td>-</td>
 * </tr>
 * <tr>
 * 	<td>{@value #PASSWORD_PROPERTY} (<i>String</i>)</td>
 *  <td>The password associated to the username</td>
 *  <td>-</td>
 * </tr>
 * <tr>
 * 	<td>{@value #PORT_PROPERTY} (<i>int</i>)</td>
 *  <td>The remote port of the xen-api server</td>
 *  <td>-</td>
 * </tr>
 * </table>
 * @author Fabien Hermenier
 */
public abstract class XenAPIDriver extends Driver {

	/**
	 * The remote port.
	 */
	private int port;
		
	/**
	 * The password to use.
	 */
	private String password;
	
	/**
	 * The current username.
	 */
	private String username;

	/**
	 * The property that define the username used to login on the xenapi server.
	 */
	public static final String USERNAME_PROPERTY = "driver.xenapi.username";

	/**
	 * The property that define the password used to login on the xenapi server.
	 */
	public static final String PASSWORD_PROPERTY = "driver.xenapi.password";
	
	/**
	 * The property that define the port of the xenapi server.
	 */
	public static final String PORT_PROPERTY = "driver.xenapi.port";
	
	/**
	 * Abstract class to provide a Driver that execute an action using the XenAPI.
	 * @param a the action to execute
	 * @param properties the properties to customize the driver
     * @throws entropy.PropertiesHelperException if an error occurred while configuring the driver
	 */
	public XenAPIDriver(Action a, PropertiesHelper properties) throws PropertiesHelperException {
		super(a);
		
		this.port = properties.getRequiredPropertyAsInt(PORT_PROPERTY);
		this.username = properties.getRequiredProperty(USERNAME_PROPERTY);
		this.password = properties.getRequiredProperty(PASSWORD_PROPERTY);
	}

	@Override
	public String toString() {
		return ("xenapi(" + this.getAction().toString() + ")");
	}
	
	/**
	 * Get the password used for the action.
	 * @return a password
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * Get the login used to authentificate on the remote host.
	 * @return a username
	 */

	public String getUsername() {
		return this.username;
	}
	
	/**
	 * Get the listening port of the remote host.
	 * @return a port
	 */
	public int getPort() {
		return this.port;
	}
}
