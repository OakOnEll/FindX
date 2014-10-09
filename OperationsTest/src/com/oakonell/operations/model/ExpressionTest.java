package com.oakonell.operations.model;

import junit.framework.TestCase;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Expression;

public class ExpressionTest extends TestCase {

    public void testToString() {
        assertEquals("x", new Expression(new Fraction(1), new Fraction(0)).toString());
        assertEquals("1", new Expression(new Fraction(0), new Fraction(1)).toString());
        assertEquals("0", new Expression(new Fraction(0), new Fraction(0)).toString());

        assertEquals("x + 1", new Expression(new Fraction(1), new Fraction(1)).toString());
        assertEquals("x + 2", new Expression(new Fraction(1), new Fraction(2)).toString());
        assertEquals("2x", new Expression(new Fraction(2), new Fraction(0)).toString());
        assertEquals("2x + 3", new Expression(new Fraction(2), new Fraction(3)).toString());

        assertEquals("(1/2)x", new Expression(new Fraction(1, 2), new Fraction(0)).toString());
        assertEquals("(2/3)x + 1/5", new Expression(new Fraction(4, 6), new Fraction(1, 5)).toString());

        assertEquals("x - 1", new Expression(new Fraction(1), new Fraction(-1)).toString());
        assertEquals("x - 1/2", new Expression(new Fraction(1), new Fraction(-1, 2)).toString());
        assertEquals("-(2/3)x - 1/5", new Expression(new Fraction(-4, 6), new Fraction(-1, 5)).toString());

        assertEquals("-2/3", new Expression(new Fraction(0), new Fraction(-2, 3)).toString());

        assertEquals("-2/3", new Expression(null, new Fraction(-2, 3)).toString());
        assertEquals("2x", new Expression(new Fraction(2), null).toString());

        assertEquals("-x", new Expression(new Fraction(-1), null).toString());

		assertEquals("x<sup><small>2</small></sup> - 2x + 1",new Expression(Fraction.ONE, new Fraction(-2), Fraction.ONE).toString());
		assertEquals("x<sup><small>2</small></sup> + 2x + 1",new Expression(Fraction.ONE, new Fraction(2), Fraction.ONE).toString());
		assertEquals("x<sup><small>2</small></sup> + 1",new Expression(Fraction.ONE, new Fraction(0), Fraction.ONE).toString());
		assertEquals("x<sup><small>2</small></sup> - 1",new Expression(Fraction.ONE, new Fraction(0), Fraction.MINUS_ONE).toString());
		assertEquals("-x<sup><small>2</small></sup> - 1",new Expression(Fraction.MINUS_ONE, new Fraction(0), Fraction.MINUS_ONE).toString());
		assertEquals("-2x<sup><small>2</small></sup> - 1",new Expression(new Fraction(-2), new Fraction(0), Fraction.MINUS_ONE).toString());
		assertEquals("-(1/2)x<sup><small>2</small></sup> - 1",new Expression(new Fraction(-1,2), new Fraction(0), Fraction.MINUS_ONE).toString());

        
    }
}
