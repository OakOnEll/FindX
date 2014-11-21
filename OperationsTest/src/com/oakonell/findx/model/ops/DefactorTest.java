package com.oakonell.findx.model.ops;

import junit.framework.TestCase;

import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;

public class DefactorTest extends TestCase {
	public void testCanApplyExpr() {
		Defactor defactor = new Defactor(new Expression(1, 1));
		Expression expr = new Expression(1, -1);
		assertTrue(defactor.canApply(expr));
		assertFalse(defactor.canApply(new Expression(1, 1, 1)));
	}

	public void testAppyExpr() {
		Defactor defactor = new Defactor(new Expression(1, 1));
		Expression expr = new Expression(1, -1);
		Expression result = defactor.apply(expr);
		assertEquals(new Expression(1, 0, -1), result);

		Expression result2 = defactor.apply(new Expression(1, 1));
		assertEquals(new Expression(1, 2, 1), result2);

		Expression zero = new Expression(0);
		Expression result3 = defactor.apply(zero);
		assertEquals(zero, result3);
	}

	public void testCanApplyEquation() {
		Defactor defactor = new Defactor(new Expression(1, 1));
		Expression expr = new Expression(1, -1);
		Expression expr2 = new Expression(1, 1);
		Equation equation = new Equation(expr, expr2);
		assertFalse(defactor.canApply(equation));
		Equation equation2 = new Equation(expr, new Expression(0));
		assertTrue(defactor.canApply(equation2));

		Equation equation3 = new Equation(new Expression(1, 1, 1),
				new Expression(0));
		assertFalse(defactor.canApply(equation3));

		Equation equation4 = new Equation(new Expression(0), new Expression(1,
				1, 1));
		assertFalse(defactor.canApply(equation4));
	}
	
	public void testApplyEquation() {
		Defactor defactor = new Defactor(new Expression(1, 1));
		Expression expr = new Expression(1, -1);
		Expression expr2 = new Expression(1, 1);
		Equation equation = new Equation(expr, expr2);

		Equation expectedResult = new Equation(new Expression(1,0,-1), new Expression(1,2,1));
		assertEquals(expectedResult, defactor.apply(equation));
		
		Expression zero = new Expression(0);
		Equation equation2 = new Equation(expr, zero);
		Equation expectedResult2 = new Equation(new Expression(1,0,-1), zero);
		assertEquals(expectedResult2, defactor.apply(equation2));

		Equation equation3 = new Equation(zero, expr);
		Equation expectedResult3 = new Equation(zero, new Expression(1,0,-1));
		assertEquals(expectedResult3, defactor.apply(equation3));
}
}
