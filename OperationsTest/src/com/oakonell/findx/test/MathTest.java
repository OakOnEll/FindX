package com.oakonell.findx.test;

import junit.framework.TestCase;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionFormat;

import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.Divide;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.Subtract;

public class MathTest extends TestCase {

    public void testFractions() {
        Fraction fraction = new Fraction(1, 3);

        Expression l = new Expression(new Fraction(10), new Fraction(2));
        Expression r = new Expression(new Fraction(6), new Fraction(4));

        Equation e = new Equation(l, r);

        Operation add1 = new Add(new Expression(new Fraction(0), new Fraction(1)));
        Operation sub1 = new Subtract(new Expression(new Fraction(0), new Fraction(1)));
        Operation mult2 = new Multiply(new Fraction(2));
        Operation div2 = new Divide(new Fraction(2));

    }

    public void parseFraction() {
        FractionFormat format = new FractionFormat();
        Fraction fraction = format.parse("5a");
        System.out.println(fraction);

        fraction = format.parse("1.5");
        System.out.println(fraction);
    }
}
