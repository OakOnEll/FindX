package com.oakonell.operations.model.ops;

import junit.framework.TestCase;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.ops.Subtract;

public class SubtractTest extends TestCase {
    public void testSubtractExpression() {
        Expression e1 = new Expression(new Fraction(2), new Fraction(3));
        Subtract sub = new Subtract(new Expression(new Fraction(3), new Fraction(1, 2)));
        assertEquals(new Expression(new Fraction(-1), new Fraction(5, 2)), sub.apply(e1));
    }

    public void testToString() {
        Operation sub = new Subtract(new Expression(new Fraction(3), new Fraction(1, 2)));
        assertEquals("Subtract 3x + 1/2", sub.toString());
    }

}
