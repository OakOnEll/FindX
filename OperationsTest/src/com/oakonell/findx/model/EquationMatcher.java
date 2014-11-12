package com.oakonell.findx.model;

import com.oakonell.findx.custom.model.AbstractEquationSolver;
import com.oakonell.findx.model.Equation;

public class EquationMatcher extends AbstractEquationSolver {
    private final Equation solution;

    public EquationMatcher(Equation solution) {
        this.solution = solution;
    }

    @Override
    protected boolean isSolution(Equation eq) {
        return eq.equals(solution);
    }

}
