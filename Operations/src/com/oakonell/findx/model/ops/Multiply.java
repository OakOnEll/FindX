package com.oakonell.findx.model.ops;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.OperationVisitor;
import com.parse.ParseObject;

@Immutable
public class Multiply extends AbstractOperation {
	private final Fraction factor;

	public Multiply(int factor) {
		this(new Fraction(factor));
	}

	public Multiply(Fraction factor) {
		this.factor = factor;
	}

	public Fraction getFactor() {
		return factor;
	}

	@Override
	public Expression apply(Expression expression) {
		Fraction xCoeff = OptimizedFractionUtils.multiply(
				expression.getXCoefficient(), factor);
		Fraction constant = OptimizedFractionUtils.multiply(
				expression.getConstant(), factor);
		return new Expression(xCoeff, constant);
	}

	@Override
	public String toString() {
		return "Multiply by " + factor.toString();
	}

	@Override
	public boolean isInverse(Operation op) {
		if (op instanceof Divide) {
			Divide other = (Divide) op;
			return other.getFactor().equals(factor);
		} else if (op instanceof Multiply) {
			Multiply other = (Multiply) op;
			return other.getFactor().reciprocal().equals(factor);
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((factor == null) ? 0 : factor.hashCode());
		return result;
	}

	@Override
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
		Multiply other = (Multiply) obj;
		if (factor == null) {
			if (other.factor != null) {
				return false;
			}
		} else if (!factor.equals(other.factor)) {
			return false;
		}
		return true;
	}

	@Override
	public OperationType type() {
		return OperationType.MULTIPLY;
	}

	@Override
	public Operation inverse() {
		return new Divide(factor);
	}

	@Override
	public void accept(OperationVisitor visitor) {
		visitor.visitMultiply(this);
	}


}
