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

import com.jcraft.jsch.JSchException;

import entropy.PropertiesHelper;
import entropy.PropertiesHelperException;
import entropy.plan.action.Action;

/**
 * Abstract class to provide a Driver that execute an action with a SSH command on a remote host.
 * @author Fabien Hermenier
 *
 */
public abstract class SSHDriver extends Driver {
			
	/**
	 * The pathname of the identity file.
	 */
	private String identityFile;
	
	/**
	 * The login used to log on the remote host.
	 */
	private String username;
	

	/**
	 * Create and configure the driver to execute an action.
	 * @param a the action to execute
	 * @param props the properties to configure the driver
     * @throws PropertiesHelperException if an error occurred while configuring the driver
	 */
	public SSHDriver(Action a, PropertiesHelper props) throws PropertiesHelperException {
		super(a);
		this.username = props.getOptionalProperty(PropertiesHelper.AUTH_USERNAME_PROPERTY, System.getProperty("user.name"));
		this.identityFile = props.getRequiredProperty(PropertiesHelper.AUTH_PRIVATE_KEY_PROPERTY);
	}
	
	/**
	 * Get the login used to log on the remote host.
	 * @return a user name
	 */
	public String getUsername() {
		return this.username;
	}
	
	/**
	 * Get the path of the private key used for authentification.
	 * @return a pathname
	 */
	public String getIdentityFile() {
		return this.identityFile;
	}
	
	/**
	 * Get the name of the remote host.
	 * @return a host name
	 */
	public abstract String getRemoteHostname();
	
	@Override
	public void execute() throws DriverException {
		try {
			SSHExec ssh = new SSHExec(getRemoteHostname(), SSHExec.DEFAULT_PORT, this.getUsername());
						
			ssh.useIdentity(this.getIdentityFile());
			String realCmd = this.getCommandToExecute();
			int ret = ssh.executeCommand(realCmd); 
			if (ret != 0) {
				throw new DriverException(this, "Error while execute the command '" + realCmd + "': return " + ret);
			}

			} catch (JSchException e) {
				throw new DriverException(this, e.getMessage(), e);
			} catch (IOException e) {
                throw new DriverException(this, e.getMessage(), e);
            }        
	}
	
	/**
	 * Get the command to execute on the remote host.
	 * @return a shell command
	 */
	public abstract String getCommandToExecute();
	
	@Override
	public String toString() {
		return "ssh(" + this.getAction().toString() + ")";
	}
}
