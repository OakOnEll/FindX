package com.oakonell.findx.model.ops;

import javax.annotation.concurrent.Immutable;

import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;

@Immutable
public abstract class AbstractOperation implements Operation {

    @Override
    public Equation apply(Equation equation) {
        Expression lhs = equation.getLhs();
        Expression rhs = equation.getRhs();
        return new Equation(apply(lhs), apply(rhs));
    }

    abstract protected Expression apply(Expression lhs);

}
