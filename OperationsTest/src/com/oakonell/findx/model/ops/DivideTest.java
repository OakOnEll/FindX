package com.oakonell.findx.model.ops;

import junit.framework.TestCase;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.ops.Divide;

public class DivideTest extends TestCase {
    public void testSubtractExpression() {
        Expression e1 = new Expression(new Fraction(2), new Fraction(3));
        Divide div = new Divide(new Fraction(3));
        assertEquals(new Expression(new Fraction(2, 3), new Fraction(1)), div.apply(e1));
    }

    public void testToString() {
        Operation div = new Divide(new Fraction(3));
        assertEquals("Divide by 3", div.toString());
    }
}
