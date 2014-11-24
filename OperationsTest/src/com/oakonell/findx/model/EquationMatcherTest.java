package com.oakonell.findx.model;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import com.oakonell.findx.custom.model.AbstractEquationSolver.Solution;

public class EquationMatcherTest extends TestCase {
	public void testLevel2ButOne() {
		ILevel level = Levels.get("1-2");
		// x + 3 = 7
		List<IMove> moves = null;
		try {

			EquationMatcher matcher = new EquationMatcher(new Equation(
					new Expression(1, 1), new Expression(5)));
			Solution solution = matcher.solve(level.getEquation(),
					level.getOperations(), level.getMinMoves(), null);
			moves = solution.primaryMoves;

			assertEquals(2, solution.getNumMoves());
			assertEquals("Subtract 1", ((Move) moves.get(1)).getOperation()
					.toString());
			assertEquals("Subtract 1", ((Move) moves.get(2)).getOperation()
					.toString());

		} catch (AssertionError e) {
			for (IMove move : moves) {
				System.out.println(move.toString());
			}
			throw e;
		}
	}

	public void testLevel3ButOne() {
		ILevel level = Levels.get("1-4");
		// x + 1 = 9
		List<IMove> moves = null;
		try {

			EquationMatcher matcher = new EquationMatcher(new Equation(
					new Expression(1, -33), new Expression(29)));
			Solution solution = matcher.solve(level.getEquation(),
					level.getOperations(), level.getMinMoves(), null);
			moves = solution.primaryMoves;

			assertEquals(1, solution.getNumMoves());
			Iterator<IMove> iter = moves.iterator();
			iter.next();
			assertEquals("Divide by 4", ((Move) iter.next()).getOperation()
					.toString());

		} catch (AssertionError e) {
			for (IMove move : moves) {
				System.out.println(move.toString());
			}
			throw e;
		}
	}
}
