package com.oakonell.findx.model.ops;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.OperationVisitor;

public class Square extends AbstractOperation {

	@Override
	protected Expression apply(Expression lhs) {
		// we don't allow squaring an already quadratic, so only
		// (ax+b)^2 = a^2x^2 + 2abx + b^2
		Fraction a = lhs.getXCoefficient();
		Fraction b = lhs.getConstant();
		return new Expression(a.multiply(a), a.multiply(b).multiply(2),
				b.multiply(b));
	}

	@Override
	public boolean isInverse(Operation op) {
		return op instanceof SquareRoot;
	}

	@Override
	public Operation inverse() {
		return new SquareRoot();
	}

	@Override
	public OperationType type() {
		return OperationType.SQUARE;
	}

	@Override
	public boolean canApply(Expression expr) {
		return expr.getX2Coefficient().equals(Fraction.ZERO);
	}

	@Override
	public void accept(OperationVisitor visitor) {
		visitor.visitSquare(this);
	}


	@Override
	public String toString() {
		return "Square";
	}
}
