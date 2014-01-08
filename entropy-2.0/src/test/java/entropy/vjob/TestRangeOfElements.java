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

package entropy.vjob;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.DefaultVirtualMachine;

/**
 * Unit tests for {@code RangeOfElements}.
 *
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestRangeOfElements {

    public void testWithoutLabel() {
        RangeOfElements<DefaultVirtualMachine> r = new RangeOfElements<DefaultVirtualMachine>("VM[1..3]");
        Assert.assertEquals(r.definition(), "VM[1..3]");
        Assert.assertEquals(r.pretty(), "VM[1..3]");
    }

    public void testWithLabel() {
        RangeOfElements<DefaultVirtualMachine> r = new RangeOfElements<DefaultVirtualMachine>("VM[1..3]", "$T1");
        Assert.assertEquals(r.definition(), "VM[1..3]");
        Assert.assertEquals(r.pretty(), "$T1");
    }
}
