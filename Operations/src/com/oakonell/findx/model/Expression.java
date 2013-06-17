package com.oakonell.findx.model;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.math3.fraction.Fraction;

@Immutable
public class Expression {
    private final Fraction xCoefficient;
    private final Fraction constant;

    public Expression(int constant) {
        this(Fraction.ZERO, new Fraction(constant));
    }

    public Expression(int coeff, int constant) {
        this(new Fraction(coeff), new Fraction(constant));
    }

    public Expression(Fraction coefficient, Fraction constant) {
        xCoefficient = coefficient == null ? Fraction.ZERO : coefficient;
        this.constant = constant == null ? Fraction.ZERO : constant;
    }

    public Fraction getXCoefficient() {
        return xCoefficient;
    }

    public Fraction getConstant() {
        return constant;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((constant == null) ? 0 : constant.hashCode());
        result = prime * result + ((xCoefficient == null) ? 0 : xCoefficient.hashCode());
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
        Expression other = (Expression) obj;
        if (constant == null) {
            if (other.constant != null) {
                return false;
            }
        } else if (!constant.equals(other.constant)) {
            return false;
        }
        if (xCoefficient == null) {
            if (other.xCoefficient != null) {
                return false;
            }
        } else if (!xCoefficient.equals(other.xCoefficient)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        boolean hadX = false;
        if (xCoefficient.compareTo(Fraction.ZERO) != 0) {
            if (xCoefficient.compareTo(Fraction.ONE) == 0) {
            } else if (xCoefficient.compareTo(Fraction.ONE.negate()) == 0) {
                builder.append("-");
            } else {
                builder.append(fractionToString(xCoefficient, true));
            }
            builder.append("x");
            hadX = true;
        }
        if (hadX) {
            if (constant.compareTo(Fraction.ZERO) > 0) {
                builder.append(" + ");
                builder.append(fractionToString(constant, false));
            } else if (constant.compareTo(Fraction.ZERO) < 0) {
                builder.append(" - ");
                builder.append(fractionToString(constant.negate(), false));
            }
        } else {
            builder.append(fractionToString(constant, false));
        }

        return builder.toString();
    }

    private String fractionToString(Fraction f, boolean paren) {
        String str = null;
        if (f.getDenominator() == 1) {
            str = Integer.toString(f.getNumerator());
        } else if (f.getNumerator() == 0) {
            str = "0";
        } else {
            String sign = f.getNumerator() > 0 ? "" : "-";
            int num = Math.abs(f.getNumerator());
            int den = f.getDenominator();
            if (paren) {
                str = sign + "(" + num + "/" + den + ")";
            } else {
                str = sign + num + "/" + den;
            }
        }
        return str;
    }

}
