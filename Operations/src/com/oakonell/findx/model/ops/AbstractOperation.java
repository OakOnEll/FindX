package com.oakonell.findx.model.ops;

import java.util.List;

import javax.annotation.concurrent.Immutable;

import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.MoveResult;
import com.oakonell.findx.model.Operation;

@Immutable
public abstract class AbstractOperation implements Operation {

	@Override
	public Equation apply(Equation equation) {
		Expression lhs = equation.getLhs();
		Expression rhs = equation.getRhs();
		return new Equation(apply(lhs), apply(rhs));
	}

	@Override
	public MoveResult applyMove(Equation equation, int moveNum, List<Operation> operations) {
		return new MoveResult(new Move(equation, this, moveNum));
	}

	abstract protected Expression apply(Expression lhs);

	@Override
	public boolean canApply(Equation equation) {
		return canApply(equation.getLhs()) && canApply(equation.getRhs());
	}

	public boolean canApply(Expression expr) {
		return true;
	}

	@Override
	public boolean isBuilt() {
		return true;
	}

	@Override
	public Operation afterUsed() {
		return this;
	}

}
