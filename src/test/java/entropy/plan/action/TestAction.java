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
package entropy.plan.action;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.plan.MockAction;

/**
 * Dummy Tests for TimeActions.
 * @author Fabien Hermenier
 *
 */
@Test(groups={"unit"})
public class TestAction {

	/**
	 * Test the instantiation and the getters.
	 */
	public void testGets() {
		Action ta = new MockAction(10, 5);
		Assert.assertEquals(ta.getFinishMoment(), 5);
		Assert.assertEquals(ta.getStartMoment(), 10);
//		Assert.assertEquals(ta.getSourceNode(), src);
//		Assert.assertEquals(ta.getDestinationNode(), dst);
//		Assert.assertEquals(ta.getVirtualMachine(), vm);
	}
}
