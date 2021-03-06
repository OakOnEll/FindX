package com.oakonell.findx.model;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.math3.fraction.Fraction;

@Immutable
public class Expression {
	private final Fraction x2Coefficient;
	private final Fraction xCoefficient;
	private final Fraction constant;

	public enum UseParenthesis {
		FORCE, DEFAULT, NO
	}

	public Expression(int constant) {
		this(Fraction.ZERO, Fraction.ZERO, new Fraction(constant));
	}

	public Expression(int coeff, int constant) {
		this(0, coeff, constant);
	}

	public Expression(int x2coeff, int coeff, int constant) {
		this(new Fraction(x2coeff), new Fraction(coeff), new Fraction(constant));
	}

	public Expression(Fraction coefficient, Fraction constant) {
		this(Fraction.ZERO, coefficient, constant);
	}

	public Expression(Fraction x2Coefficient, Fraction coefficient,
			Fraction constant) {
		this.x2Coefficient = x2Coefficient == null ? Fraction.ZERO
				: x2Coefficient;
		xCoefficient = coefficient == null ? Fraction.ZERO : coefficient;
		this.constant = constant == null ? Fraction.ZERO : constant;
	}

	public Fraction getX2Coefficient() {
		return x2Coefficient;
	}

	public Fraction getXCoefficient() {
		return xCoefficient;
	}

	public Fraction getConstant() {
		return constant;
	}

	public boolean hasXCoefficient() {
		return xCoefficient.compareTo(Fraction.ZERO) != 0;
	}

	public boolean hasX2Coefficient() {
		return x2Coefficient.compareTo(Fraction.ZERO) != 0;
	}

	public boolean isConstantOnly() {
		return !hasX2Coefficient() && !hasXCoefficient();
	}

	public boolean hasConstant() {
		return constant.compareTo(Fraction.ZERO) != 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((constant == null) ? 0 : constant.hashCode());
		result = prime * result
				+ ((x2Coefficient == null) ? 0 : x2Coefficient.hashCode());
		result = prime * result
				+ ((xCoefficient == null) ? 0 : xCoefficient.hashCode());
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
		Expression other = (Expression) obj;
		if (constant == null) {
			if (other.constant != null)
				return false;
		} else if (!constant.equals(other.constant))
			return false;
		if (x2Coefficient == null) {
			if (other.x2Coefficient != null)
				return false;
		} else if (!x2Coefficient.equals(other.x2Coefficient))
			return false;
		if (xCoefficient == null) {
			if (other.xCoefficient != null)
				return false;
		} else if (!xCoefficient.equals(other.xCoefficient))
			return false;
		return true;
	}

	private boolean appendCoeffPart(StringBuilder builder, Fraction value,
			String suffix, boolean hasPrevious) {
		if (value.compareTo(Fraction.ZERO) == 0) {
			return false;
		}
		if (value.compareTo(Fraction.ONE) == 0) {
			// a coeff of 1 can be elided
			if (hasPrevious) {
				builder.append(" + ");
			}
		} else if (value.compareTo(Fraction.ONE.negate()) == 0) {
			if (hasPrevious) {
				builder.append(" - ");
			} else {
				// a coeff of -1 simply becomes a prefixed '-'
				builder.append("-");
			}
		} else {
			if (hasPrevious) {
				if (value.compareTo(Fraction.ZERO) < 0) {
					builder.append(" - ");
				} else {
					builder.append(" + ");
				}
				builder.append(fractionToString(value.abs(),
						UseParenthesis.DEFAULT));
			} else {
				builder.append(fractionToString(value, UseParenthesis.DEFAULT));
			}
		}

		builder.append(suffix);
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		boolean hadX = false;
		hadX |= appendCoeffPart(builder, x2Coefficient,
				"x<sup><small>2</small></sup>", hadX);
		hadX |= appendCoeffPart(builder, xCoefficient, "x", hadX);

		// constant is treated specially
		if (hadX) {
			if (constant.compareTo(Fraction.ZERO) > 0) {
				builder.append(" + ");
				builder.append(fractionToString(constant, UseParenthesis.NO));
			} else if (constant.compareTo(Fraction.ZERO) < 0) {
				builder.append(" - ");
				builder.append(fractionToString(constant.negate(),
						UseParenthesis.NO));
			}
		} else {
			builder.append(fractionToString(constant, UseParenthesis.NO));
		}

		return builder.toString();
	}

	public static String fractionToString(Fraction f, UseParenthesis useParen) {
		return fractionToString(f, useParen, false);
	}

	public static String fractionToString(Fraction f, UseParenthesis useParen,
			boolean signInsideParen) {
		String str = null;
		if (f.getDenominator() == 1) {
			if (useParen == UseParenthesis.FORCE) {
				str = "(" + Integer.toString(f.getNumerator()) + ")";
			} else {
				str = Integer.toString(f.getNumerator());
			}
		} else if (f.getNumerator() == 0) {
			if (useParen == UseParenthesis.FORCE) {
				str = "(0)";
			} else {
				str = "0";
			}
		} else {
			String sign = f.getNumerator() > 0 ? "" : "-";
			int num = Math.abs(f.getNumerator());
			int den = f.getDenominator();
			if (useParen != UseParenthesis.NO) {
				if (signInsideParen) {
					// huh, when used with x replacement from level solved
					// dialog, the sign should be inside?
					str = "(" + sign + num + "/" + den + ")";
				} else {
					str = sign + "(" + num + "/" + den + ")";
				}
			} else {
				str = sign + num + "/" + den;
			}
		}
		return str;
	}

	public boolean isZero() {
		return !hasX2Coefficient() && !hasXCoefficient() && !hasConstant();
	}

}
