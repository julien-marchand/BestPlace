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

import java.security.Permission;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test the launcher to avoid bad users.
 * @author Fabien Hermenier
 *
 */
@Test(groups = {"unit" }, sequential = true)
public class TestEntropyLauncher {
	
	/**
	 * An exception thrown when an System.exit() occurred.
	 * @author Fabien Hermenier
	 *
	 */
	class ExitException extends SecurityException {
		
		/**
		 * The exit status.
		 */
		private int status;
		
		/**
		 * A new exception.
		 * @param st the exit status
		 */
		public ExitException(int st) {
			super("There is no escape");
			this.status = st;
		}
		
		/**
		 * Return the error message.
		 * @return a String!
		 */
        @Override
		public String getMessage() {
			return "Application execute a 'System.exit(" + this.status + ")'";
		}
	}
	
	/**
	 * A Mock security manager to "transform" a System.exit() into
	 * a ExitException.
	 * @author Fabien Hermenier
	 *
	 */
	class NoExitSecurityManager extends SecurityManager {

		@Override
		public void checkPermission(Permission perm, Object ctx) {
			
		}

		@Override
		public void checkPermission(Permission perm) {
			
		}

		@Override
		public void checkExit(int st) {
			super.checkExit(st);
			throw new ExitException(st);
		}
	}
	
	/**
	 * Test with no arguments.
	 */
	public void testWithNoArgs() {
		System.setSecurityManager(new NoExitSecurityManager());
		try {
		EntropyLauncher.main(new String [] {});
		} catch (ExitException e) {
			Assert.assertEquals(e.status, 1);
		} finally {
			System.setSecurityManager(null);
		}
	}
	
	/**
	 * Test with wrong arguments.
	 */
	public void testWithBadArgs() {
		System.setSecurityManager(new NoExitSecurityManager());
		try {
			EntropyLauncher.main(new String [] {"bad"});
		} catch (ExitException e) {            
			Assert.assertEquals(e.status, 1);
		} finally {
			System.setSecurityManager(null);
		}
	}
	
	/**
	 * Test with an unreachable default properties
	 */
	public void testWithBadProperties() {
		System.setSecurityManager(new NoExitSecurityManager());
		try {
			EntropyLauncher.main(new String [] {"stop"});
		} catch (ExitException e) {        
			Assert.assertEquals(e.status, 1);
		} finally {
			System.setSecurityManager(null);
		}
	}

    	/**
	 * Test with an unreachable custom propertie
	 */
	public void testWithUnknownCustomProperties() {
		System.setSecurityManager(new NoExitSecurityManager());
		try {
			EntropyLauncher.main(new String [] {"startup", "baad"});
		} catch (ExitException e) {            
			Assert.assertEquals(e.status, 1);
		} finally {
			System.setSecurityManager(null);
		}
	}

    /**
	 * Test remote startup and shutdown
	 */
	/*public void testRemoteShutdown() {
       System.setSecurityManager(new NoExitSecurityManager());
		try {
			final MockControlLoop mock = new MockControlLoop();
            Entropy e = new Entropy(mock);
			e.setRegistryPort(4500);
            e.setSleepDelay(1);
            e.startup();
            EntropyLauncher.main(new String [] {"stop", "src/test/resources/entropy/TestEntropyLauncher.entropy_properties.txt"});
		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);
		} finally {
			System.setSecurityManager(null);
		}
	}   */
    
}
