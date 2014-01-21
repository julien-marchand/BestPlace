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

import java.net.MalformedURLException;

import org.apache.xmlrpc.XmlRpcException;

import entropy.PropertiesHelper;
import entropy.PropertiesHelperException;
import entropy.plan.action.Migration;

/**
 * Driver to perform the migration of a VirtualMachine managed by a Xen Hypervisor. This driver
 * can be configured using properties loaded into {@link entropy.PropertiesHelper}. The following table
 * sum the available properties for this driver.
 * 
 * <table>
 * <tr><td>Name of the property</td><td>Description</td><td>Default value</td></tr>
 * <tr>
 * 	<td>{@value #RELOCATION_PORT_PROPERTY} (<i>int</i>)</td>
 *  <td>The remote relocation port used for migration</td>
 *  <td>-</td>
 * </tr>
 * </table>
 * 
 * @see XenAPIDriver
 * @author Fabien Hermenier
 */
public class XenGuestMigration extends XenAPIDriver {
		
	/**
	 * The current action to execute.
	 */
	private Migration mig;
		
	/**
	 * The current remote relocation port.
	 */
	private int relocationPort;
	
	
	/**
	 * The property that define the remote relocation port.
	 */
	public static final String RELOCATION_PORT_PROPERTY = "driver.xenapi.relocationPort";

	
	/**
	 * Create and configure the driver to execute a migration action.
	 * @param action the action to execute
	 * @param props the properties to configure the driver
     * @throws entropy.PropertiesHelperException if an error occurred while configuring the driver
	 */
	public XenGuestMigration(Migration action, PropertiesHelper props) throws PropertiesHelperException {
		super(action, props);
		this.mig = action;
		this.relocationPort = props.getRequiredPropertyAsInt(RELOCATION_PORT_PROPERTY);
	}

	/**
	 * Execute the migration.
	 * @throws DriverException if a error occurred during the migration
	 */
	@Override
    public void execute() throws DriverException {
		XenRpcClient client;
		try {
			client = new XenRpcClient(this.mig.getHost().getName(), this.getPort());
			client.login(this.getUsername(), this.getPassword());
			client.migrate(this.mig.getVirtualMachine().getName(), this.mig.getDestination().getName(), true, this.relocationPort);
			client.logout();
		} catch (MalformedURLException e) {
			throw new DriverException(this, e.getMessage(), e);
		} catch (XmlRpcException e) {
			throw new DriverException(this, e.getMessage(), e);
		} catch (XenClientException e) {
			throw new DriverException(this, e.getMessage(), e);
		}
		
	}
	
	/**
	 * Get the relocation port of the destination node.
	 * @return a port
	 */
	public int getRelocationPort() {
		return this.relocationPort;
	}	
}
