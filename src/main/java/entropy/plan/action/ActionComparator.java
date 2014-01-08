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
package entropy.plan.action;

import java.io.Serializable;
import java.util.Comparator;


/**
 * A comparator for Action.
 * Can be used to compare Action regarding to their start moment or their finish moment.
 * @author Fabien Hermenier
 *
 */
public class ActionComparator implements Comparator<Action>, Serializable {

    /**
     * Default serial UID.
     */
    private static final long serialVersionUID = 1L;
    
	/**
	 * The comparison mode.
	 */
	public static enum Type {
        /**
         *  A comparison based on the start moment
         */
        start,
        /**
         *  comparison based on the finish moment.
         */
        finish}
		
	/**
	 * The comparison mode.
	 */
	private Type type;
	
	/**
	 * Make a new comparator.
	 * @param t The comparison mode.
	 * on the finish moment
	 */
	public ActionComparator(Type t) {
		this.type = t;
	}
	
	
	/**
	 * Compare two actions regarding to the comparison mode.
	 * @param o1 the first action
	 * @param o2 the second action
	 * @return a negative number, if o1 is inferior to o2. A positive number of o1 is greater than o2. If both action are 
	 * equals, returns 0.
	 */
	@Override
    public int compare(Action o1, Action o2) {
		if (this.type == Type.start) {
			return o1.getStartMoment() - o2.getStartMoment();
		}
		return o1.getFinishMoment() - o2.getFinishMoment(); 	
	}

}
