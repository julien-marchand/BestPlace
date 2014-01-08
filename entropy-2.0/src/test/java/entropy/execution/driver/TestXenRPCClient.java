/*
 * Copyright (c) 2009 Ecole des Mines de Nantes.
 * 
 *     This file is part of Entropy.
 * 
 *     Entropy is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     Entropy is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 * 
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
*/
package entropy.execution.driver;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests XenRPCClient.
 * At this moment, tests require an hypervisor :/
 * @author Fabien Hermenier
 *
 */
public class TestXenRPCClient {

	/**
	 * Test login and logout method.
	 */
	@Test(groups = {"integration:hypervisor" }, sequential = true)
	public void testAuth() {
        Assert.fail();
		try {
			XenRpcClient client = new XenRpcClient("b217.info.emn.fr", 9363);
			client.login("", "");
			client.logout();
		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);
		}
	}
	
	/**
	 * Test getUUID() in various contexts.
	 */
	@Test(groups = {"integration:hypervisor" }, sequential = true, dependsOnMethods = {"testAuth" })
	public void testGetUUID() {
		try {
			XenRpcClient client = new XenRpcClient("b217.info.emn.fr", 9363);
			client.login("root", "");
			Assert.assertEquals(client.getUUID("Domain-0"), "00000000-0000-0000-0000-000000000000");					
			Assert.assertNull(client.getUUID("toto"));
			client.logout();
		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);
		}		
	}
	
	/**
	 * Test getNameFromUUID().
	 */
	@Test(groups = {"integration:hypervisor" }, sequential = true, dependsOnMethods = {"testAuth" })
	public void testGetNameFromUUID() {
		try {
			XenRpcClient client = new XenRpcClient("b217.info.emn.fr", 9363);
			client.login("root", "");
			Assert.assertEquals(client.getNameFromUUID("00000000-0000-0000-0000-000000000000"), "Domain-0");							
			client.logout();
		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);
		}		
	}
	/**
	 * Test the save and restore methods.
	 */
	@Test(groups = {"integration:hypervisor" }, sequential = true, dependsOnMethods = {"testAuth" })
	public void testSaveAndRestore() {
		try {
			XenRpcClient client = new XenRpcClient("b217.info.emn.fr", 9363);
			client.login("root", "");
			//Save and restore of an existant VMs
			Assert.assertTrue(client.save("tinyLenny1", "/tmp/tinyLenny1.state", false));			
			Assert.assertTrue(client.restore("/tmp/tinyLenny1.state", false));
			client.logout();
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}	
	}
	
	/**
	 * Test listVMs().
	 */
	@Test(groups = {"integration:hypervisor" }, sequential = true, dependsOnMethods = {"testAuth" })
	public void testList() {
		try {
			XenRpcClient client = new XenRpcClient("b217.info.emn.fr", 9363);
			client.login("root", "");
			List<String> vms = client.listVMs();
			Assert.assertTrue(vms.size() > 0);
			Assert.assertTrue(vms.contains("Domain-0"));
			client.logout();
		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);			
		}			
	}
	
	/**
	 * Test the migrate method.
	 */
	@Test(groups = {"integration:hypervisor" }, sequential = true, dependsOnMethods = {"testAuth" })
	public void testMigration() {
		try {
			XenRpcClient client = new XenRpcClient("b217.info.emn.fr", 9363);
			client.login("root", "");
			Assert.assertTrue(client.migrate("tinyLenny2", "pastel-2", true));
			Assert.assertFalse(client.listVMs().contains("tinyLenny2"));
			//TODO: Check the presence on the destination machine
			client.logout();
		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);
		}	
	}
	
	/**
	 * Test gets().
	 */
	@Test(groups = {"unit" })
	public void testGets() {
		try {
			XenRpcClient client = new XenRpcClient("myHost");
			Assert.assertEquals(client.getRemoteHostname(), "myHost");
			Assert.assertEquals(client.getRemotePort(), XenRpcClient.DEFAULT_XEN_API_PORT);
		
			client = new XenRpcClient("myHost", 8000);
			Assert.assertEquals(client.getRemoteHostname(), "myHost");
			Assert.assertEquals(client.getRemotePort(), 8000);
		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);
		}

	}
}
