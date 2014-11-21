package com.oakonell.findx.model.ops;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.OperationVisitor;

public class Defactor extends AbstractOperation {
	private final Expression expr;

	public Defactor(Expression expr) {
		this.expr = expr;
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
		if (!(op instanceof Factor))
			return false;
		Factor factor = (Factor) op;
		return factor.getExpression().equals(expr);
	}

	@Override
	public Operation inverse() {
		return new Factor(expr);
	}

	@Override
	public void accept(OperationVisitor visitor) {
		visitor.visitDefactor(this);
	}

	@Override
	public OperationType type() {
		return OperationType.DEFACTOR;
	}

	@Override
	public boolean canApply(Equation equation) {
		return (equation.getLhs().isZero() || equation.getRhs().isZero())
				&& super.canApply(equation);
	}

	@Override
	protected Expression apply(Expression lhs) {
		if (lhs.isZero())
			return lhs;
		Fraction a = expr.getXCoefficient();
		Fraction b = expr.getConstant();
		Fraction c = lhs.getXCoefficient();
		Fraction d = lhs.getConstant();

		Fraction crossterm = b.multiply(c).add(a.multiply(d));
		return new Expression(a.multiply(c), crossterm, b.multiply(d));
	}

	@Override
	public boolean canApply(Expression expr) {
		// Can have no x^2 terms
		return !expr.hasX2Coefficient();
	}

}
