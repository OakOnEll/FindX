package com.oakonell.findx.custom.model;

import com.oakonell.findx.model.Equation;

public class EquationSolver extends AbstractEquationSolver {

    @Override
    protected boolean isSolution(Equation eq) {
        return eq.isSolved();
    }

}
