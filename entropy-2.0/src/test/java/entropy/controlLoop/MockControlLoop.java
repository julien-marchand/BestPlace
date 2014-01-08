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

package entropy.controlLoop;



/**
 * A Mock control loop for test purpose.
 * @author Fabien Hermenier
 *
 */
public class MockControlLoop extends ControlLoop {

	/**
	 * Make a new MockControlLoop.
	 */
	public MockControlLoop() {        
	}

	/**
	 * The number of iterations.
	 */
	private volatile int nb = 0;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean runLoop() {
			try {
				Thread.sleep(1000L);
			} catch (Exception e) {
				e.printStackTrace();
			}
			nb++;	
			if (mustExit()) {
                getLogger().debug("We have to leave!");
				return true;
			}		
		return false;
	}
	
	/**
	 * Get the number of iterations.
	 * @return a number
	 */
	public int getNb() {
		return nb;
	}

}
