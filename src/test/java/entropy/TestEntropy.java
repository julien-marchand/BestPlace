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

package entropy;

import java.rmi.registry.LocateRegistry;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.controlLoop.MockControlLoop;

/**
 * Unit tests for Entropy.
 * @author Fabien Hermenier
 *
 */
@Test(groups = {"unit"}, sequential = true)
public class TestEntropy {
		
	/**
	 * Test the registration to a existing registry.
	 */
	public void testWithExistantRegistry() {        
        final MockControlLoop mock = new MockControlLoop();
        Entropy e = new Entropy(mock);
		try {
			LocateRegistry.createRegistry(4567);			
			e.setRegistryPort(4567);
            Assert.assertFalse(e.isRunning());
            e.startup();
            Assert.assertTrue(e.isRunning());
            e.shutdown();
            Assert.assertFalse(e.isRunning());            
		} catch (Exception e2) {
			Assert.fail(e2.getMessage(), e2);
		}

	}

	/**
	 * Test the registration to a inexisting registry.
	 */
	public void testUnregisterWithInexistantRegistry() {		
		try {
			MockControlLoop mock = new MockControlLoop();
            Entropy e = new Entropy(mock);
			e.setRegistryPort(4568);
            Assert.assertEquals(e.isRunning(), false);
            e.startup();
            Assert.assertTrue(e.isRunning());
            e.shutdown();
            Assert.assertFalse(e.isRunning());              
		} catch (Exception e2) {
			Assert.fail(e2.getMessage(), e2);
		} 
	}
	
	
    @Test(expectedExceptions = {EntropyException.class})
    public void testAvoidingMultipleStartup() throws EntropyException {
			final MockControlLoop mock = new MockControlLoop();
            Entropy e = new Entropy(mock);
        try {
			e.setRegistryPort(4572);
            e.startup();
            Assert.assertTrue(e.isRunning());
            e.startup();
            Assert.assertFalse(e.isRunning());
        } finally {
            e.shutdown();
        }        
    }

    @Test(expectedExceptions = {EntropyException.class})
    public void testAvoidingMultipleShutdown() throws EntropyException {
			final MockControlLoop mock = new MockControlLoop();
            Entropy e = new Entropy(mock);
			e.setRegistryPort(4573);
            e.startup();
            Assert.assertTrue(e.isRunning());
			e.shutdown();
            e.shutdown();
    }
}
