package com.oakonell.findx.model.ops;

import org.apache.commons.math3.fraction.Fraction;

public final class OptimizedFractionUtils {
    public static Fraction add(Fraction a, Fraction b) {
        if (a.getDenominator() == 1 && b.getDenominator() == 1) {
            return new Fraction(a.getNumerator() + b.getNumerator());
        }
        return a.add(b);
    }

    public static Fraction subtract(Fraction a, Fraction b) {
        if (a.getDenominator() == 1 && b.getDenominator() == 1) {
            return new Fraction(a.getNumerator() - b.getNumerator());
        }
        return a.subtract(b);
    }

    public static Fraction divide(Fraction a, Fraction b) {
        if (a.getDenominator() == 1 && b.getDenominator() == 1) {
            return new Fraction(a.getNumerator(), b.getNumerator());
        }
        return a.divide(b);
    }

    public static Fraction multiply(Fraction a, Fraction b) {
        if (a.getDenominator() == 1 && b.getDenominator() == 1) {
            return new Fraction(a.getNumerator() * b.getNumerator());
        }
        return a.multiply(b);
    }

}
