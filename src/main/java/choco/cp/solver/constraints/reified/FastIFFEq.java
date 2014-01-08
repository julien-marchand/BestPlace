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
package choco.cp.solver.constraints.reified;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A fast implementation for BVAR => VAR = CSTE
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/06/11
 */
public class FastIFFEq extends AbstractBinIntSConstraint {

    private final int constante;

    public FastIFFEq(IntDomainVar b, IntDomainVar var, int constante) {
        super(b, var);
        if (!b.hasBooleanDomain()) {
            throw new SolverException(b.getName() + " is not a boolean variable");
        }
        this.constante = constante;
    }

    @Override
    public int getFilteredEventMask(int idx) {
        if (idx == 0) {
            return IntVarEvent.INSTINT_MASK;
        } else {
            if (v1.hasEnumeratedDomain()) {
                return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
            }
            return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
        }
    }

    @Override
    public void propagate() throws ContradictionException {
        if (v0.isInstantiated()) {
            int val = v0.getVal();
            if (val == 0) {
                if (v1.removeVal(constante, this, false)) {
                    this.setEntailed();
                }
            } else {
                v1.instantiate(constante, this, false);
                this.setEntailed();
            }
        }
        if (v1.isInstantiatedTo(constante)) {
            v0.instantiate(1, this, false);
        } else if (!v1.canBeInstantiatedTo(constante)) {
            v0.instantiate(0, this, false);
            this.setEntailed();
        }
    }

    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        if (idx == 0) {
            int val = v0.getVal();
            if (val == 0) {
                if (v1.removeVal(constante, this, false)) {
                    this.setEntailed();
                }
            } else {
                v1.instantiate(constante, this, false);
            }
        } else {
            if (v1.isInstantiatedTo(constante)) {
                v0.instantiate(1, this, false);
            } else {
                v0.instantiate(0, this, false);
            }
        }
    }

    @Override
    public void awakeOnRem(int varIdx, int val) throws ContradictionException {
        if (varIdx == 1 && val == constante) {
            v0.instantiate(0, this, false);
        }
    }

    @Override
    public void awakeOnInf(int varIdx) throws ContradictionException {
        if (varIdx == 1) {
            if (!v1.canBeInstantiatedTo(constante)) {
                v0.instantiate(0, this, false);
                this.setEntailed();
            }
        }
    }

    @Override
    public void awakeOnSup(int varIdx) throws ContradictionException {
        if (varIdx == 1) {
            if (!v1.canBeInstantiatedTo(constante)) {
                v0.instantiate(0, this, false);
                this.setEntailed();
            }
        }
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        return (tuple[0] == 1 && tuple[1] == constante)
                || (tuple[0] == 0 && tuple[1] != constante);
    }
}
