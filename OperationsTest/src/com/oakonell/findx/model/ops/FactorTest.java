package com.oakonell.findx.model.ops;

import junit.framework.TestCase;

import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.MoveResult;

public class FactorTest extends TestCase {
	public void testCanApplyExpr() {
		Factor factor = new Factor(new Expression(1, -1));
		Expression expr = new Expression(1, 0, -1);
		assertTrue(factor.canApply(expr));
	}
	
	public void testApplyMoveEquationFactored() {
		Factor factor = new Factor(new Expression(1, -1));
		Equation equation = new Equation(new Expression(1,0,-1),new Expression(0));
		MoveResult moveResult= factor.applyMove(equation,1 , null, null);
		assertTrue(moveResult.hasMultiple());
		
		assertEquals("(x + 1)(x - 1) = 0", moveResult.getPrimaryMove().getEndEquationString());		
		assertEquals("x + 1 = 0", moveResult.getSecondary1().getEndEquationString());
		assertEquals("x - 1 = 0", moveResult.getSecondary2().getEndEquationString());

	
		equation = new Equation(new Expression(0), new Expression(1,0,-1));
		moveResult= factor.applyMove(equation,1 , null, null);
		assertTrue(moveResult.hasMultiple());
		
		assertEquals("0 = (x + 1)(x - 1)", moveResult.getPrimaryMove().getEndEquationString());		
		assertEquals("0 = x + 1", moveResult.getSecondary1().getEndEquationString());
		assertEquals("0 = x - 1", moveResult.getSecondary2().getEndEquationString());

	}

	public void testApplyMoveEquationNoSolutions() {
		Factor factor = new Factor(new Expression(1, -1));
		Equation equation = new Equation(new Expression(1,0,-1),new Expression(1,1,-2));
		MoveResult moveResult= factor.applyMove(equation,1 , null, null);
		assertFalse(moveResult.hasMultiple());		
		assertEquals("(x + 1)(x - 1) = (x + 2)(x - 1)", moveResult.getPrimaryMove().getEndEquationString());		

	
		equation = new Equation(new Expression(1,0,-1),new Expression(12));
		moveResult= factor.applyMove(equation,1 , null, null);
		assertFalse(moveResult.hasMultiple());		
		assertEquals("(x + 1)(x - 1) = 12", moveResult.getPrimaryMove().getEndEquationString());		

		
		equation = new Equation(new Expression(12), new Expression(1,0,-1));
		moveResult= factor.applyMove(equation,1 , null, null);
		assertFalse(moveResult.hasMultiple());		
		assertEquals("12 = (x + 1)(x - 1)", moveResult.getPrimaryMove().getEndEquationString());		

		equation = new Equation(new Expression(1,0,-1),new Expression(1, 0, 12));
		moveResult= factor.applyMove(equation,1 , null, null);
		assertFalse(moveResult.hasMultiple());		
		assertEquals("(x + 1)(x - 1) = x<sup><small>2</small></sup> + 12", moveResult.getPrimaryMove().getEndEquationString());		

	}
}
