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

import java.util.LinkedList;
import java.util.List;

import entropy.plan.action.Action;

/**
 * A Mock factory to create drivers for test purpose.
 * @author Fabien Hermenier
 *
 */
public class MockDriverFactory extends DriverFactory {

	/**
	 * The list of actions that has been transformed.
	 */
	private List<Action> performed;
			
	/**
	 * Make a new factory.
	 */
	public MockDriverFactory() {
		super(null);		
		this.performed = new LinkedList<Action>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Driver transform(Action action) throws DriverInstantiationException {
		performed.add(action);	
		return new MockDriver(action, false);		
	}
	
	/**
	 * Get the total number of transformed actions.
	 * @return a positive integer
	 */
	public int getNbActions() {
		return this.performed.size();
	}
	
	public void reset() {
		this.performed = new LinkedList<Action>();
	}
	
	public List<Action> getPerformed() {
		return this.performed;
	}
}
