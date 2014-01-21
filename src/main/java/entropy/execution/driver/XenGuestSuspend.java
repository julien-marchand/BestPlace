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
import entropy.plan.action.Suspend;

/**
 * A driver to perform suspend of Xen virtual machines. This driver can be configured
 * using the properties loaded with {@link entropy.PropertiesHelper}. The following table sum the
 * available propertie.
 
 * <table>
 * <tr><td>Name of the property</td><td>Description</td><td>Default value</td></tr>
 * <tr>
 * 	<td>{@value #STATE_FILE_LOCATION_PROPERTY} (<i>string</i>)</td>
 *  <td>The absolute pathname of the state file location. This location <b>must be</b>
 *      available for the whole cluster if you want to restore the virtual machine directly
 *      on another node.
 *  </td>
 *  <td>-</td>
 * </tr>
 * <tr>
 * 	<td>{@value #STATE_FILE_PREFIX_PROPERTY} (<i>String</i>)</td>
 *  <td>The prefix for each state file</td>
 *  <td>-</td>
 * </tr>
 * </table>
 * 
 * @see XenGuestResume
 * @see XenRpcClient 
 * @author Fabien Hermenier
 *
 */
public class XenGuestSuspend extends XenAPIDriver {

	/**
	 * The default prefix for state files.
	 */
	public static final String DEFAULT_STATE_FILES_PREFIX = ".chk";
			
	/**
	 * The current action.
	 */
	private Suspend sp;
	
	/**
	 * The current location of the state files.
	 */
	private String stateFilesLocation;
		
	/**
	 * The property that define the location of the state file.
	 */
	public static final String STATE_FILE_LOCATION_PROPERTY = "driver.xenapi.stateFilesLocation";
	
	/**
	 * The prefix for the state files.
	 */
	public static final String STATE_FILE_PREFIX = ".chk";
	
	
	/**
	 * Create and configure the driver to execute a suspend action.
	 * @param action the action to execute
	 * @param props the properties to configure the driver
     * @throws entropy.PropertiesHelperException if an error occurred while configuring the driver
	 */
	public XenGuestSuspend(Suspend action, PropertiesHelper props) throws PropertiesHelperException {
		super(action, props);
		this.stateFilesLocation = props.getRequiredProperty(STATE_FILE_LOCATION_PROPERTY);
		this.sp = action;
	}

	@Override
    public void execute() throws DriverException {
		try {
			XenRpcClient client = new XenRpcClient(this.sp.getHost().getName(), this.getPort());
			client.login(this.getUsername(), this.getPassword());
			client.save(this.sp.getVirtualMachine().getName(), this.getStateFilesLocation() + "/" + this.sp.getVirtualMachine().getName() + STATE_FILE_PREFIX, false);
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
