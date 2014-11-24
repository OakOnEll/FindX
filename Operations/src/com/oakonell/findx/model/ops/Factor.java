package com.oakonell.findx.model.ops;

import java.util.List;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.custom.model.AbstractEquationSolver.Solution;
import com.oakonell.findx.custom.model.EquationSolver;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.FormattedMove;
import com.oakonell.findx.model.MoveResult;
import com.oakonell.findx.model.MultipleSolutionMove;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.OperationVisitor;
import com.oakonell.findx.model.SecondaryEquationMove;

public class Factor extends AbstractOperation {
	private final Expression expr;

	public Factor(Expression exp) {
		expr = exp;
		// validate that expr is only a linear expression in x
		if (expr.hasX2Coefficient()) {
			throw new RuntimeException(
					"Can't have an x^2 coefficient in a Factor operation");
		}
	}

	public Expression getExpression() {
		return expr;
	}

	@Override
	public boolean isInverse(Operation op) {
		if (!(op instanceof Defactor))
			return false;
		Defactor defactor = (Defactor) op;
		return defactor.getExpression().equals(expr);
	}

	@Override
	public Operation inverse() {
		return new Defactor(expr);
	}

	@Override
	public OperationType type() {
		return OperationType.FACTOR;
	}

	@Override
	public void accept(OperationVisitor visitor) {
		visitor.visitFactor(this);
	}

	@Override
	public String toString() {
		return "Factor " + expr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Factor other = (Factor) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		return true;
	}

	@Override
	public boolean canApply(Equation equation) {
		return canApply(equation.getLhs()) || canApply(equation.getRhs());
	}

	@Override
	public MoveResult applyMove(Equation equation, int moveNum,
			List<Operation> operations, Operation appliedOrNull) {
		Expression lhs = equation.getLhs();
		Expression rhs = equation.getRhs();
		int num = 0;
		Expression lhsResult = factor(lhs);
		if (lhsResult != null) {
			num++;
		}
		Expression rhsResult = factor(rhs);
		if (rhsResult != null) {
			num++;
		}

		if (num == 1) {
			Expression zero = new Expression(0);
			if (lhs.isZero()) {
				String factored = "0 = (" + rhsResult + ")(" + expr + ")";
				return new MoveResult(//
						new MultipleSolutionMove(equation,
								(appliedOrNull == null ? this : appliedOrNull),
								factored, moveNum), //
						new SecondaryEquationMove(
								new Equation(zero, rhsResult), 1), //
						new SecondaryEquationMove(new Equation(zero, expr), 2));
			}
			if (rhs.isZero()) {
				String factored = "(" + lhsResult + ")(" + expr + ") = 0";
				return new MoveResult(//
						new MultipleSolutionMove(equation,
								(appliedOrNull == null ? this : appliedOrNull),
								factored, moveNum), //
						new SecondaryEquationMove(
								new Equation(lhsResult, zero), 1), //
						new SecondaryEquationMove(new Equation(expr, zero), 2));
			}
		}
		StringBuilder builder = new StringBuilder();
		if (lhsResult != null) {
			builder.append("(");
			builder.append(lhsResult);
			builder.append(")(");
			builder.append(expr);
			builder.append(")");
		} else {
			builder.append(lhs);
		}
		builder.append(" = ");
		if (rhsResult != null) {
			builder.append("(");
			builder.append(rhsResult);
			builder.append(")(");
			builder.append(expr);
			builder.append(")");
		} else {
			builder.append(rhs);
		}

		FormattedMove move = new FormattedMove(equation,
				(appliedOrNull == null ? this : appliedOrNull),
				builder.toString(), moveNum);
		return new MoveResult(move);
	}

	@Override
	protected Expression apply(Expression lhs) {
		throw new UnsupportedOperationException(
				"can't call apply factor on an expression- use applyMove");
	}

	private Expression factor(Expression otherExpr) {
		if (!otherExpr.hasX2Coefficient() || otherExpr.isZero())
			return null;

		Fraction x2 = otherExpr.getX2Coefficient();
		Fraction x1 = otherExpr.getXCoefficient();
		Fraction x0 = otherExpr.getConstant();

		Fraction a = expr.getXCoefficient();
		Fraction b = expr.getConstant();

		Fraction c = x2.divide(a);
		Fraction d = x0.divide(b);

		if (x1.equals(b.multiply(c).add(a.multiply(d)))) {
			return new Expression(c, d);
		}
		return null;
	}

	@Override
	public boolean canApply(Expression otherExpr) {
		Expression result = factor(otherExpr);
		return result != null;
	}

	@Override
	public boolean isAppliableWith(List<Operation> operations) {
		// Make sure that the current operations allow solving this factor's
		// expression itself
		EquationSolver solver = new EquationSolver();
		Solution solve = solver.solve(new Equation(expr, new Expression(0)),
				operations, 5, null);
		return solve != null;
	}

}
