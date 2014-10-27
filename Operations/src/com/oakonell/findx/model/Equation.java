package com.oakonell.findx.model;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.math3.fraction.Fraction;

@Immutable
public class Equation {
	private final Expression lhs;
	private final Expression rhs;

	public Equation(Expression left, Expression right) {
		lhs = left;
		rhs = right;
	}

	public Expression getLhs() {
		return lhs;
	}

	public Expression getRhs() {
		return rhs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lhs == null) ? 0 : lhs.hashCode());
		result = prime * result + ((rhs == null) ? 0 : rhs.hashCode());
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
		Equation other = (Equation) obj;
		if (lhs == null) {
			if (other.lhs != null) {
				return false;
			}
		} else if (!lhs.equals(other.lhs)) {
			return false;
		}
		if (rhs == null) {
			if (other.rhs != null) {
				return false;
			}
		} else if (!rhs.equals(other.rhs)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(lhs);
		builder.append(" = ");
		builder.append(rhs);

		return builder.toString();
	}

	public boolean isSolved() {
		return lhs.getXCoefficient().equals(Fraction.ONE)
				&& lhs.getConstant().equals(Fraction.ZERO)
				&& lhs.getX2Coefficient().equals(Fraction.ZERO)
				&& rhs.isConstantOnly();
	}

}
