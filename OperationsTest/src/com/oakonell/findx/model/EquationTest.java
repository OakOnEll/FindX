package com.oakonell.findx.model;

import junit.framework.TestCase;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;

public class EquationTest extends TestCase {

    public void testToString() {
        Expression l = new Expression(new Fraction(4, 6), new Fraction(1, 5));
        Expression r = new Expression(new Fraction(1, 6), new Fraction(3, 5));

        Equation eq = new Equation(l, r);

        assertEquals("(2/3)x + 1/5 = (1/6)x + 3/5", eq.toString());
    }
}
