package com.oakonell.operations.model.ops;

import junit.framework.TestCase;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.ops.Add;

public class AddTest extends TestCase {
    public void testAddExpression() {
        Expression e1 = new Expression(new Fraction(2), new Fraction(3));
        Add add = new Add(new Expression(new Fraction(3), new Fraction(1, 2)));
        assertEquals(new Expression(new Fraction(5), new Fraction(7, 2)), add.apply(e1));
    }

    public void testToString() {
        Operation add = new Add(new Expression(new Fraction(3), new Fraction(1, 2)));
        assertEquals("Add 3x + 1/2", add.toString());
    }

}
