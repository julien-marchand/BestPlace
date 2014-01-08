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
 * Unit tests for TimedActionComparator.
 * @author Fabien Hermenier
 *
 */
@Test(groups = {"unit"})
public class TestActionComparator {

	/**
	 * Make 2 TimedAction.
	 * t[0].getFinishMoment() < t[1].getFinishMoment() && t[0].getStartMoment() > t[1].getEndMoment()
	 * @return an array of 2 TimedAction.
	 */
	private Action [] makeActions() {
		Action ts [] = new MockAction[2];
		ts[0] = new MockAction(10, 5);		
		ts[1] = new MockAction(11, 4);
		return ts;
	}
	
	/**
	 * Test a comparison based on finish moment. 
	 */
	public void testFinishMomentComparison() {
		Action [] ts = makeActions();
		ActionComparator cmp = new ActionComparator(ActionComparator.Type.finish);
		Assert.assertTrue(cmp.compare(ts[0], ts[1]) > 0);
		Assert.assertTrue(cmp.compare(ts[1], ts[0]) < 0);
	}
	
	/**
	 * Test a comparison based on starting moment.
	 */
	public void testStartMomentComparison() {
		Action [] ts = makeActions();
		ActionComparator cmp = new ActionComparator(ActionComparator.Type.start);
		Assert.assertTrue(cmp.compare(ts[0], ts[1]) < 0);
		Assert.assertTrue(cmp.compare(ts[1], ts[0]) > 0);
	}

}
