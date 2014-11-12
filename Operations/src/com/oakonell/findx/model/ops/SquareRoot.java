package com.oakonell.findx.model.ops;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.OperationVisitor;

public class SquareRoot extends AbstractOperation {

	@Override
	public Expression apply(Expression expr) {
		Fraction aSquared = expr.getX2Coefficient();
		Fraction bSquared = expr.getConstant();
		Fraction ab2 = expr.getXCoefficient();

		Fraction a;
		try {
			a = squareRoot(aSquared);
		} catch (NotIntegerSquareRoot e) {
			throw new RuntimeException("Unable to take square root of "
					+ aSquared, e);
		}
		Fraction b;
		try {
			b = squareRoot(bSquared);
		} catch (NotIntegerSquareRoot e) {
			throw new RuntimeException("Unable to take square root of "
					+ bSquared, e);
		}
		// a or b may be negative
		Fraction possibleAb2 = a.multiply(b).multiply(2);

		if ((possibleAb2.compareTo(Fraction.ZERO) != 0)
				&& possibleAb2.compareTo(ab2.negate()) == 0) {
			// can't tell between the two a or b being negative?
			// choose this way- later operator handling will allow switching the
			// negative term
			return new Expression(a, b.negate());
		}

		if (possibleAb2.compareTo(ab2) != 0) {
			throw new RuntimeException("The expression " + expr
					+ " is not a perfect square (a*x + b)^2");
		}
		return new Expression(a, b);
	}

	@Override
	public boolean canApply(Expression expr) {
		Fraction aSquared = expr.getX2Coefficient();
		Fraction bSquared = expr.getConstant();
		Fraction ab2 = expr.getXCoefficient();

		try {
			Fraction a = squareRoot(aSquared);
			Fraction b = squareRoot(bSquared);

			Fraction possibleAb2 = a.multiply(b).multiply(2);

			if ((possibleAb2.compareTo(Fraction.ZERO) != 0)
					&& possibleAb2.compareTo(ab2.negate()) == 0) {
				return true;
			}

			return a.multiply(b).multiply(2).equals(ab2);
		} catch (NotIntegerSquareRoot e) {
			return false;
		}

	}

	@Override
	public boolean isInverse(Operation op) {
		return op instanceof Square;
	}

	@Override
	public Operation inverse() {
		return new Square();
	}

	@Override
	public OperationType type() {
		return OperationType.SQUARE_ROOT;
	}

	private Fraction squareRoot(Fraction aSquared) throws NotIntegerSquareRoot {
		int numIntRoot = integerRoot(aSquared.getNumerator());
		int denomIntRoot = integerRoot(aSquared.getDenominator());

		return new Fraction(numIntRoot, denomIntRoot);
	}

	private int integerRoot(int numerator) throws NotIntegerSquareRoot {
		double sqrt = Math.sqrt(numerator);
		int aInt = (int) sqrt;
		if (aInt * aInt != numerator)
			throw new NotIntegerSquareRoot("value " + numerator
					+ " does not have an integer square root");
		return aInt;
	}

	public static class NotIntegerSquareRoot extends Exception {
		public NotIntegerSquareRoot(String detailMessage) {
			super(detailMessage);
		}

	}

	@Override
	public void accept(OperationVisitor visitor) {
		visitor.visitSquareRoot(this);
	}

	@Override
	public String toString() {
		return "Square Root";
	}

	@Override
	public Operation afterUsed() {
		return Multiply.NEGATE;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return 11;
	}
}
