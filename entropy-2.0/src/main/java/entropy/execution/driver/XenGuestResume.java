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

import java.io.IOException;

import org.apache.xmlrpc.XmlRpcException;

import entropy.PropertiesHelper;
import entropy.PropertiesHelperException;
import entropy.plan.action.Resume;

/**
 * A driver to restore a state file of a Xen guest image and run it. This 
 * driver can be configured with properties available with {@link entropy.PropertiesHelper}.
 * The following table sum the available properties.
 * 
 * <table>
 * <tr><td>Name of the property</td><td>Description</td><td>Default value</td></tr>
 * <tr>
 * 	<td>{@value entropy.execution.driver.XenGuestSuspend#STATE_FILE_LOCATION_PROPERTY} (<i>string</i>)</td>
 *  <td>The absolute pathname of the state file location. This location <b>must be</b>
 *      available for the whole cluster if you want to restore the virtual machine directly
 *      on another node.
 *  </td>
 *  <td>-</td>
 * </tr>
 * </table>
 * 
 * @see XenGuestSuspend
 * @see XenAPIDriver
 * 
 * @author Fabien Hermenier
 *
 */
public class XenGuestResume extends XenAPIDriver {
	
	/**
	 * The current action.
	 */
	private Resume rs;
	
	/**
	 * The current location of the state file.
	 */
	private String stateFilesLocation;
			
	/**
	 * Create and configure the driver to execute a resume action.
	 * @param action the action to execute
	 * @param props the properties to configure the driver
     * @throws entropy.PropertiesHelperException if an error occurred while configuring the driver
	 */
	public XenGuestResume(Resume action, PropertiesHelper props) throws PropertiesHelperException {
		super(action, props);
		this.rs = action;
		
		this.stateFilesLocation = props.getRequiredProperty(XenGuestSuspend.STATE_FILE_LOCATION_PROPERTY);
	}

	@Override
    public void execute() throws DriverException {
		try {
			XenRpcClient client = new XenRpcClient(this.rs.getHost().getName(), this.getPort());
			client.login(this.getUsername(), this.getPassword());
			client.restore(this.stateFilesLocation + this.rs.getVirtualMachine().getName() + XenGuestSuspend.STATE_FILE_PREFIX, false);
			client.logout();
		} catch (IOException e) {
			throw new DriverException(this, e.getMessage(), e);
		} catch (XenClientException e) {
            throw new DriverException(this, e.getMessage(), e);
        } catch (XmlRpcException e) {
            throw new DriverException(this, e.getMessage(), e);
        }
    }
	
	/**
	 * Get the location of the state files.
	 * @return a path
	 */
	public String getStateFilesLocation() {
		return this.stateFilesLocation;
	}

}
