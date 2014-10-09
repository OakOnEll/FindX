package com.oakonell.operations.model.ops;

import junit.framework.TestCase;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.ops.SquareRoot;

public class SquareRootTest extends TestCase {
	public void testSimpleSquareRoot() {
		SquareRoot op = new SquareRoot();
		Expression expr = new Expression(0, 4);
		Expression result = op.apply(expr);
		assertEquals(0, Fraction.ZERO.compareTo(result.getX2Coefficient()));
		assertEquals(0, Fraction.ZERO.compareTo(result.getXCoefficient()));
		assertEquals(0, Fraction.TWO.compareTo(result.getConstant()));
	}

	public void testComplexSquareRootNegativeAb() {
		SquareRoot op = new SquareRoot();
		Expression expr = new Expression(Fraction.ONE, new Fraction(-2),
				Fraction.ONE);
		Expression result = op.apply(expr);
		assertEquals(0, Fraction.ZERO.compareTo(result.getX2Coefficient()));
		assertEquals(0, Fraction.ONE.compareTo(result.getXCoefficient()));
		assertEquals(0, Fraction.MINUS_ONE.compareTo(result.getConstant()));
	}

	public void testComplexSquareRootPositiveAb() {
		SquareRoot op = new SquareRoot();
		Expression expr = new Expression(Fraction.ONE, new Fraction(2),
				Fraction.ONE);
		Expression result = op.apply(expr);
		assertEquals(0, Fraction.ZERO.compareTo(result.getX2Coefficient()));
		assertEquals(0, Fraction.ONE.compareTo(result.getXCoefficient()));
		assertEquals(0, Fraction.ONE.compareTo(result.getConstant()));
	}

	public void testCanApplyComplexSquareRoot() {
		SquareRoot op = new SquareRoot();
		Expression expr = new Expression(Fraction.ONE, new Fraction(-2),
				Fraction.ONE);
		assertTrue(op.canApply(expr));
	}

}
