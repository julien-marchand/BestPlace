/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package choco.cp.solver.variables.integer;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A bijective boolean variable B :
 * <br/>B = not(A)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/06/11
 */
public class BoolVarNot extends AbstractBijectiveVar {

    /**
     * Build a variable Y such as Y = X op c.
     *
     * @param solver   The model this variable belongs to
     * @param name     The name of the variable
     * @param variable constraints stored specific structure
     */
    public BoolVarNot(final Solver solver, String name, IntDomainVar variable) {
        super(solver, name, variable);

    }


    @Override
    public void remVal(int x) throws ContradictionException {
        if (x == 0) {
            variable.remVal(1);
        } else if (x == 1) {
            variable.remVal(0);
        }
    }

    @Override
    public void setInf(int x) throws ContradictionException {
        if (x == 1) {
            variable.setSup(0);
        }
    }

    @Override
    public void setMin(int x) throws ContradictionException {
        if (x == 1) {
            variable.setMax(0);
        }
    }

    @Override
    public void setSup(int x) throws ContradictionException {
        if (x == 0) {
            variable.setInf(1);
        }
    }

    @Override
    public void setMax(int x) throws ContradictionException {
        if (x == 0) {
            variable.setMin(1);
        }
    }

    @Override
    public boolean canBeInstantiatedTo(int x) {
        if (x == 0) {
            variable.canBeInstantiatedTo(1);
        } else if (x == 1) {
            variable.canBeInstantiatedTo(0);
        }
        return false;
    }

    @Override
    public boolean fastCanBeInstantiatedTo(int x) {
        if (x == 0) {
            variable.fastCanBeInstantiatedTo(1);
        } else if (x == 1) {
            variable.fastCanBeInstantiatedTo(0);
        }
        return false;
    }

    @Override
    public int getRandomDomainValue() {
        if (variable.isInstantiated()) {
            return getVal();
        } else {
            return variable.getRandomDomainValue();
        }
    }

    @Override
    public int getNextDomainValue(int i) {
        int inf = getInf();
        if (i < inf) return inf;
        int sup = getSup();
        if (i < sup) return sup;
        return Integer.MAX_VALUE;
    }

    @Override
    public int getPrevDomainValue(int i) {
        int sup = getSup();
        if (i > sup) return sup;
        int inf = getInf();
        if (i > inf) return inf;
        return Integer.MIN_VALUE;
    }

    @Override
    public int getInf() {
        return variable.getSup();
    }

    @Override
    public int getSup() {
        return variable.getInf();
    }

    @Override
    public int getValue() {
        return Math.abs(variable.getValue() - 1);
    }

    @Override
    public boolean updateInf(int x, SConstraint cause, boolean forceAwake) throws ContradictionException {
        return x == 1 && variable.instantiate(0, cause, forceAwake);
    }

    @Override
    public boolean updateInf(int x, int idx) throws ContradictionException {
        return x == 1 && variable.instantiate(0, idx);
    }

    @Override
    public boolean updateSup(int x, SConstraint cause, boolean forceAwake) throws ContradictionException {
        return x == 0 && variable.instantiate(1, cause, forceAwake);
    }

    @Override
    public boolean updateSup(int x, int idx) throws ContradictionException {
        return x == 0 && variable.instantiate(1, idx);
    }

    @Override
    public boolean removeVal(int x, SConstraint cause, boolean forceAwake) throws ContradictionException {
        if (x == 0) {
            return variable.instantiate(0, cause, forceAwake);
        } else if (x == 1) {
            return variable.instantiate(1, cause, forceAwake);
        }
        return false;
    }

    @Override
    public boolean removeVal(int x, int idx) throws ContradictionException {
        if (x == 0) {
            return variable.instantiate(0, idx);
        } else if (x == 1) {
            return variable.instantiate(1, idx);
        }
        return false;
    }

    @Override
    public boolean removeInterval(int a, int b, SConstraint cause, boolean forceAwake) throws ContradictionException {
        if (a <= getInf())
            return updateInf(b + 1, cause, forceAwake);
        else if (getSup() <= b)
            return updateSup(a - 1, cause, forceAwake);
        return false;
    }

    @Override
    public boolean removeInterval(int a, int b, int idx) throws ContradictionException {
        if (a <= getInf())
            return updateInf(b + 1, idx);
        else if (getSup() <= b)
            return updateSup(a - 1, idx);
        return false;
    }

    @Override
    public boolean instantiate(int x, SConstraint cause, boolean forceAwake) throws ContradictionException {
        if (x == 0) {
            return variable.instantiate(1, cause, forceAwake);
        } else if (x == 1) {
            return variable.instantiate(0, cause, forceAwake);
        }
        return false;
    }

    @Override
    public boolean instantiate(int x, int idx) throws ContradictionException {
        if (x == 0) {
            return variable.instantiate(1, idx);
        } else if (x == 1) {
            return variable.instantiate(0, idx);
        }
        return false;
    }

    @Override
    public void setVal(int x) throws ContradictionException {
        if (x == 0) {
            variable.setVal(1);
        } else if (x == 1) {
            variable.setVal(0);
        }
        variable.setVal(x);
    }

    @Override
    public int getVal() {
        return Math.abs(variable.getVal() - 1);
    }

    @Override
    public boolean isInstantiatedTo(int x) {
        if (x == 0) return variable.isInstantiatedTo(1);
        return x == 1 && variable.isInstantiatedTo(0);
    }

    @Override
    public String pretty() {
        return String.format("not (%s)", variable.getName());
    }
}
