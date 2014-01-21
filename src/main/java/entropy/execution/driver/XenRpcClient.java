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
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

/**
 * A Client to execute actions on the virtual machines hosted
 * on a Xen Hypervisor. This client uses the xen-api 1.0.6
 * 
 * In order to execute methods, you have to log in first using an username
 * and a password. If the authentification succeed, a session is opened and you
 * can execute methods. Then, you have to close your session using the method logout.
 * 
 * @author Fabien Hermenier
 */
public class XenRpcClient {

	/**
	 * The xmlrpc client.
	 */
	private XmlRpcClient client;
		
	/**
	 * The current session Identifier.
	 */
	private String sessionID;
	
	/**
	 * The default relocation port.
	 */
	public static final int DEFAULT_RELOCATION_PORT = 8002;
		
	/**
	 * The default xen-api port.
	 */
	public static final int DEFAULT_XEN_API_PORT = 9363;
	
	/**
	 * The default username to log in.
	 */
	public static final String DEFAULT_USERNAME = "";
	
	/**
	 * The default password of the user to log in.
	 */
	public static final String DEFAULT_PASSWORD = "";
	

	/**
	 * The remote port.
	 */
	private int port;
	
	/**
	 * The remote hostname.
	 */
	private String hostname;
	
	
	/**
	 * Make a new client and connect it to the port {@value #DEFAULT_XEN_API_PORT} of a remote 
	 * host.
	 * @param host the xend hostname.
	 * @throws MalformedURLException if an error occurred 
	 */
	public XenRpcClient(String host) throws MalformedURLException {
		this(host, DEFAULT_XEN_API_PORT);
	}
		
	/**
	 * Make a new client, connected to the specified port of a remote host.
	 * @param host the xend hostname.
	 * @param p the port of the xen-api
	 * @throws MalformedURLException if an error occurred 
	 */
	public XenRpcClient(String host, int p) throws MalformedURLException {
		this.hostname = host;
		this.port = p;
		URL url = new URL("http://" + hostname + ":" + port);
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(url);
		config.setEnabledForExtensions(true);
		this.client = new XmlRpcClient();
		this.client.setConfig(config);
	}
	
	/**
	 * login onto the client.
	 * @param username the username
	 * @param password the password
	 * @return true if the operation succeed
	 * @throws XenClientException if an error related to the operations occurred.
	 * @throws XmlRpcException if an error occurred.
	 */
	public boolean login(String username, String password) throws XenClientException, XmlRpcException {
		this.sessionID = (String) this.execute("session.login_with_password", new Object [] {username, password});
		return this.sessionID != null;
	}
	
	/**
	 * Logout on the client.
	 * @return true if the operation succeed
	 * @throws XenClientException if an error related to the operations occurred.
	 * @throws XmlRpcException if an error occurred
	 */
	public boolean logout() throws XenClientException, XmlRpcException {
		return this.execute("session.logout", new Object[] {this.sessionID}) != null;
	}
	
	/**
	 * Migrate a virtual machine on a destination node.
	 * The remote relocation port must be {@value #DEFAULT_RELOCATION_PORT}
	 * @param name the name of the virtual machine
	 * @param dest the hostname of the destination node
	 * @param live set to true if migration is performed in live
	 * @return true if the operation succeed
	 * @throws XmlRpcException if an error occurred
	 * @throws XenClientException if an error related to the operations occurred.
	 */
	public boolean migrate(String name, String dest, boolean live) throws XenClientException, XmlRpcException {
		return this.migrate(name, dest, live, DEFAULT_RELOCATION_PORT);
	}
	
	/**
	 * Migrate a virtual machine on a destination node.
	 * The remote relocation port must be {@value #DEFAULT_RELOCATION_PORT}
	 * @param name the name of the virtual machine
	 * @param dest the hostname of the destination node
	 * @param live set to true if migration is performed in live
	 * @param dstPort the remote relocation port
	 * @return true if the operation succeed
	 * @throws XmlRpcException if an error occurred
	 * @throws XenClientException if an error related to the operations occurred.
	 */
	public boolean migrate(String name, String dest, boolean live, int dstPort) throws XenClientException, XmlRpcException {
		Map <String, Object>m  = new HashMap<String, Object>();
		m.put("port", dstPort);
		return this.execute("VM.migrate", new Object[]{this.sessionID, this.getUUID(name), dest, live, m}) != null;
	}
	
	/**
	 * Save the virtual machine in a state file and free associated
	 * CPU and memory resources.
	 * @param name the name of the virtual machine
	 * @param stateFile the file to store the state of the virtual machine
	 * @param checkpoint run the VM  after creating the checkpoint ?
	 * @return true if the operation succeed
	 * @throws XmlRpcException if an error occurred
	 * @throws XenClientException if an error related to the operations occurred.
	 */
	public boolean save(String name, String stateFile, boolean checkpoint) throws XenClientException, XmlRpcException {
		String uuid = this.getUUID(name);
		if (uuid != null) {
			return this.execute("VM.save", new Object []{this.sessionID, uuid, stateFile, checkpoint}) != null;
		}
		return false;
	}
		
	/**
	 * Execute a command and return the result if the method is executed.
	 * @param method the method call
	 * @param params the parameters of the method call
	 * @return the return value
	 * @throws XmlRpcException if an error occurred
	 * @throws XenClientException if an error related to the operations occurred.
	 */
	private Object execute(String method, Object [] params) throws XmlRpcException, XenClientException {
		HashMap <String, Object> result = (HashMap <String, Object>) this.client.execute(method, params);
		if (result.get("Status").equals("Success")) {
			return result.get("Value");
		} else  {
			Object [] toks = (Object[]) result.get("ErrorDescription");
			if (toks[0].equals("PERMISSION_DENIED")) {
				throw new XenClientException(method, toks[0].toString(), "You do not have the permission to perform the operation");
			} else if (toks[0].equals("OPERATION_NOT_ALLOWED")) {
				throw new XenClientException(method, toks[0].toString(), "You attempted an operation that was not allowed");
			} else if (toks[0].equals("INTERNAL_ERROR")
					   || toks[0].equals("MESSAGE_METHOD_UNKNOWN")
					   || toks[0].equals("VM_HVM_REQUIRED")) {
				throw new XenClientException(method, toks[0].toString(), toks[1].toString());
			} else if (toks[0].equals("INVALID_HANDLE")) {
				throw new XenClientException(method, toks[0].toString(), "Invalid handle: class=" + toks[1].toString() + " but value given=" + toks[2].toString());
			} else if (toks[0].equals("MESSAGE_PARAMETER_COUNT_MISMATCH")) {
				throw new XenClientException(method, toks[0].toString(), "Incorrect number of parameters, expected " + toks[1].toString() + " but given " + toks[2].toString());
			} else if (toks[0].equals("SESSION_AUTHENTIFICATION_FAILED")) {
				throw new XenClientException(method, toks[0].toString(), "The given session ID '" + params[0] + "' is incorrect");
			} else if (toks[0].equals("SESSION_INVALID")) {
				throw new XenClientException(method, toks[0].toString(), "The given session ID '" + params[0] + "' is invalid (server restart or ID timed out)");
			} else if (toks[0].equals("VALUE_NOT_SUPPORTED")) {
				throw new XenClientException(method, toks[0].toString(), "The value '" + toks[2].toString() + " of the field '" + toks[1].toString() + " is not supported : " + toks[3].toString());
			} else if (toks[0].equals("VM_BAD_POWER_STATE")) {
				throw new XenClientException(method, toks[0].toString(), "The virtual machine '" + toks[1].toString() + "' is not in the good state to perform the operation. Expected '" + toks[2] + "' but was '" + toks[3] + "'");
			} else if (toks[0].equals("SECURITY_ERROR")) {
				throw new XenClientException(method, toks[0].toString(), "Security error'" + toks[1].toString() + " - " + toks[2].toString());
			} else {
				throw new XenClientException(method, toks[0].toString(), "");
			}
		}		
	}
	
	/**
	 * Restore a virtual machine, previously saved into a file.
	 * @param stateFile the file that contain the state of the virtual machine
	 * @param run run the VM after the restart ?
	 * @return true if the operation succeed
	 * @throws XmlRpcException if an error occurred
	 * @throws XenClientException if an error related to the operations occurred.
	 */
	public boolean restore(String stateFile, boolean run) throws XenClientException, XmlRpcException {		
		return this.execute("VM.restore", new Object []{this.sessionID, stateFile, run}) != null;			
	}
	
	/**
	 * Get the UUID associated to the name of the virtual machine.
	 * The name must be unique
	 * @param name the name of the virtual machine
	 * @return the UUID associated to the name
	 * @throws XmlRpcException if an error occurred
	 * @throws XenClientException if an error related to the operations occurred.
	 */
	public String getUUID(String name) throws XenClientException, XmlRpcException {
		Object [] res = (Object []) this.execute("VM.get_by_name_label", new Object[]{this.sessionID, name});
		if (res.length > 0) {
			return res[0].toString();
		}
		return null;
	}
	
	/**
	* Get the name of a virtual machine from its UUID.
	* @param uuid the UUID of the virtual machine
	* @return the name
	* @throws XmlRpcException if an error occurred
	 * @throws XenClientException if an error related to the operations occurred.
	*/
	public String getNameFromUUID(String uuid) throws XenClientException, XmlRpcException {
			return (String) this.execute("VM.get_name_label", new Object[]{this.sessionID, uuid});
	}
	/**
	 * Shutdown the virtual machine.
	 * @param name the name of the virtual machine
	 * @return true if the operation succeed
	 * @throws XmlRpcException if an error occurred
	 * @throws XenClientException if an error related to the operations occurred.
	 */
	public boolean shutdown(String name) throws XenClientException, XmlRpcException {
		return this.execute("VM.clean_shutdown", new Object[] {this.sessionID, this.getUUID(name)}) != null;
	}
		
	/**
	 * Hard shutdown of a virtual machine.
	 * @param name the name of the virtual machine
	 * @throws XmlRpcException if an error occurred
	 * @return true if the operation succeed
	 * @throws XenClientException if an error related to the operations occurred.
	 */
	public boolean destroy(String name) throws XenClientException, XmlRpcException {
		return this.execute("VML.hard_shutdown", new Object[] {this.sessionID, this.getUUID(name)}) != null;
	}
	
	/**
	 * List the VMs.
	 * @return a list of VMs, may be empty
	 * @throws XmlRpcException if an error occurred
	 * @throws XenClientException if an error related to the operations occurred.
	 */
	public List<String> listVMs() throws XenClientException, XmlRpcException {
		LinkedList<String> vms = new LinkedList<String>();
		Object [] list = (Object []) this.execute("VM.get_all", new Object[] {this.sessionID});
		for (Object o : list) {
			vms.add(this.getNameFromUUID(o.toString()));
		}
		return vms;
	}
	
	/**
	 * Get the port of the xen api server.
	 * @return the port
	 */
	public int getRemotePort() {
		return this.port;
	}
	
	/**
	 * Get the name of the remote host.
	 * @return an hostname
	 */
	public String getRemoteHostname() {
		return this.hostname;
	}
}
