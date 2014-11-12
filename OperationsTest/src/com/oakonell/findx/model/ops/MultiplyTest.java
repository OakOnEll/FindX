package com.oakonell.findx.model.ops;

import junit.framework.TestCase;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.ops.Multiply;

public class MultiplyTest extends TestCase {
    public void testSubtractExpression() {
        Expression e1 = new Expression(new Fraction(2), new Fraction(3));
        Multiply mult = new Multiply(new Fraction(3));
        assertEquals(new Expression(new Fraction(6), new Fraction(9)), mult.apply(e1));
    }

    public void testToString() {
        Operation mult = new Multiply(new Fraction(3));
        assertEquals("Multiply by 3", mult.toString());
    }

}
