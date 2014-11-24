package com.oakonell.findx.model;

import java.util.List;

import junit.framework.TestCase;

import com.oakonell.findx.custom.model.AbstractEquationSolver.Solution;

public class TargetedEquationSolverTest extends TestCase {
	public void testSolverLevel0() {
		List<IMove> moves = null;
		try {
			ILevel level = Levels.get("1-1");
			TargetedEquationSolver solver = new TargetedEquationSolver();
			Solution solution = solver.solve(level.getEquation(), new Equation(
					new Expression(1, 0), new Expression(1)), level
					.getOperations(), level.getMinMoves());
			moves = solution.primaryMoves;
			assertEquals(1, solution.getNumMoves());
			assertEquals(level.getMinMoves(), solution.getNumMoves());
			assertEquals("Add 1", ((Move) moves.get(1)).getOperation()
					.toString());
		} catch (AssertionError e) {
			for (IMove move : moves) {
				System.out.println(move.toString());
			}
			throw e;
		}
	}

	/*
	 * public void testSolverLevel1() { List<Move> moves = null; try { Level
	 * level = Levels.get("1-2"); TargetedEquationSolver solver = new
	 * TargetedEquationSolver(); Solution solution =
	 * solver.solve(level.getEquation(), new Equation( new Expression(1, 0), new
	 * Expression(4)), level .getOperations(), level.getMinMoves()); moves =
	 * solution.moves; assertEquals(3, moves.size());
	 * assertEquals(level.getMinMoves(), moves.size());
	 * assertEquals("Subtract 1", moves.get(0).getOperation().toString());
	 * assertEquals("Subtract 1", moves.get(1).getOperation().toString());
	 * assertEquals("Subtract 1", moves.get(2).getOperation().toString()); }
	 * catch (AssertionError e) { for (Move move : moves) {
	 * System.out.println(move.toString()); } throw e; } }
	 * 
	 * public void testSolverLevel2() { List<Move> moves = null; try { Level
	 * level = Levels.get("1-3"); TargetedEquationSolver solver = new
	 * TargetedEquationSolver(); Solution solution =
	 * solver.solve(level.getEquation(), new Equation( new Expression(1, 0), new
	 * Expression(9)), level .getOperations(), level.getMinMoves()); moves =
	 * solution.moves; assertEquals(4, moves.size());
	 * assertEquals(level.getMinMoves(), moves.size()); Iterator<Move> iter =
	 * moves.iterator(); assertEquals("Subtract 1",
	 * iter.next().getOperation().toString()); assertEquals("Subtract 1",
	 * iter.next().getOperation().toString()); assertEquals("Add 3",
	 * iter.next().getOperation().toString()); assertEquals("Add 3",
	 * iter.next().getOperation().toString()); } catch (AssertionError e) { for
	 * (Move move : moves) { System.out.println(move.toString()); } throw e; } }
	 * 
	 * public void testSolverLevel3() {
	 * 
	 * List<Move> moves = null; try { Level level = Levels.get("1-4"); // // x +
	 * 1 = 9 TargetedEquationSolver solver = new TargetedEquationSolver();
	 * Solution solution = solver.solve(level.getEquation(), new Equation( new
	 * Expression(1, 0), new Expression(8)), level .getOperations(),
	 * level.getMinMoves()); moves = solution.moves; assertEquals(5,
	 * moves.size()); assertEquals(level.getMinMoves(), moves.size());
	 * Iterator<Move> iter = moves.iterator(); assertEquals("Add 3",
	 * iter.next().getOperation().toString()); assertEquals("Add 3",
	 * iter.next().getOperation().toString()); assertEquals("Add 3",
	 * iter.next().getOperation().toString()); assertEquals("Subtract 5",
	 * iter.next().getOperation().toString()); assertEquals("Subtract 5",
	 * iter.next().getOperation().toString()); } catch (AssertionError e) { for
	 * (Move move : moves) { System.out.println(move.toString()); } throw e; }
	 * 
	 * }
	 * 
	 * public void testSolverLevel4() { List<Move> moves = null; try { Level
	 * level = Levels.get("1-5"); TargetedEquationSolver solver = new
	 * TargetedEquationSolver(); Solution solution =
	 * solver.solve(level.getEquation(), new Equation( new Expression(1, 0), new
	 * Expression(8)), level .getOperations(), level.getMinMoves()); moves =
	 * solution.moves; assertEquals(3, moves.size());
	 * assertEquals(level.getMinMoves(), moves.size()); Iterator<Move> iter =
	 * moves.iterator(); assertEquals("Divide by 3",
	 * iter.next().getOperation().toString()); assertEquals("Divide by 2",
	 * iter.next().getOperation().toString()); assertEquals("Divide by 2",
	 * iter.next().getOperation().toString()); } catch (AssertionError e) { for
	 * (Move move : moves) { System.out.println(move.toString()); } throw e; } }
	 * 
	 * public void testSolverLevel5() { List<Move> moves = null; try { Level
	 * level = Levels.get("1-6"); TargetedEquationSolver solver = new
	 * TargetedEquationSolver(); Solution solution =
	 * solver.solve(level.getEquation(), new Equation( new Expression(1, 0), new
	 * Expression(2)), level .getOperations(), level.getMinMoves()); moves =
	 * solution.moves; assertEquals(5, moves.size());
	 * assertEquals(level.getMinMoves(), moves.size()); Iterator<Move> iter =
	 * moves.iterator(); assertEquals("Subtract 1",
	 * iter.next().getOperation().toString()); assertEquals("Divide by 2",
	 * iter.next().getOperation().toString()); assertEquals("Subtract 1",
	 * iter.next().getOperation().toString()); assertEquals("Divide by 2",
	 * iter.next().getOperation().toString()); assertEquals("Subtract 1",
	 * iter.next().getOperation().toString()); } catch (AssertionError e) { for
	 * (Move move : moves) { System.out.println(move.toString()); } throw e; } }
	 * 
	 * public void testSolverLevel6() { List<Move> moves = null; try { Level
	 * level = Levels.get("1-7"); TargetedEquationSolver solver = new
	 * TargetedEquationSolver(); Solution solution =
	 * solver.solve(level.getEquation(), new Equation( new Expression(1, 0), new
	 * Expression(10)), level .getOperations(), level.getMinMoves()); moves =
	 * solution.moves;
	 * 
	 * assertEquals(5, moves.size()); assertEquals(level.getMinMoves(),
	 * moves.size()); Iterator<Move> iter = moves.iterator();
	 * assertEquals("Multiply by 3", iter.next().getOperation().toString());
	 * assertEquals("Add 3", iter.next().getOperation().toString());
	 * assertEquals("Add 3", iter.next().getOperation().toString());
	 * assertEquals("Divide by 3", iter.next().getOperation().toString());
	 * assertEquals("Divide by 3", iter.next().getOperation().toString()); }
	 * catch (AssertionError e) { for (Move move : moves) {
	 * System.out.println(move.toString()); } throw e; } }
	 * 
	 * public void testSolverLevel7() { List<Move> moves = null; try { Level
	 * level = Levels.get("1-8"); TargetedEquationSolver solver = new
	 * TargetedEquationSolver(); Solution solution =
	 * solver.solve(level.getEquation(), new Equation( new Expression(1, 0), new
	 * Expression(-3)), level .getOperations(), level.getMinMoves() + 1); moves
	 * = solution.moves; assertEquals(4, moves.size());
	 * assertEquals(level.getMinMoves(), moves.size()); Iterator<Move> iter =
	 * moves.iterator(); assertEquals("Subtract 1",
	 * iter.next().getOperation().toString()); assertEquals("Divide by 3",
	 * iter.next().getOperation().toString()); assertEquals("Subtract 1",
	 * iter.next().getOperation().toString()); assertEquals("Multiply by -1",
	 * iter.next().getOperation() .toString()); } catch (AssertionError e) { for
	 * (Move move : moves) { System.out.println(move.toString()); } throw e; } }
	 * 
	 * public void testSolverLevel8() { List<Move> moves = null; try { Level
	 * level = Levels.get("1-9"); TargetedEquationSolver solver = new
	 * TargetedEquationSolver(); Solution solution =
	 * solver.solve(level.getEquation(), new Equation( new Expression(1, 0), new
	 * Expression(Fraction.ZERO, new Fraction(3))), level.getOperations(), level
	 * .getMinMoves()); moves = solution.moves; assertEquals(5, moves.size());
	 * assertEquals(level.getMinMoves(), moves.size()); Iterator<Move> iter =
	 * moves.iterator(); assertEquals("Subtract 1",
	 * iter.next().getOperation().toString()); assertEquals("Divide by 2",
	 * iter.next().getOperation().toString()); assertEquals("Subtract 1",
	 * iter.next().getOperation().toString()); assertEquals("Subtract x",
	 * iter.next().getOperation().toString()); assertEquals("Divide by 2",
	 * iter.next().getOperation().toString()); } catch (AssertionError e) { for
	 * (Move move : moves) { System.out.println(move.toString()); } throw e; } }
	 * 
	 * public void testSolverLevel9() { Level level = Levels.get("1-10");
	 * List<Move> moves = null; try { TargetedEquationSolver solver = new
	 * TargetedEquationSolver(); Solution solution =
	 * solver.solve(level.getEquation(), new Equation( new Expression(1, 0), new
	 * Expression(3)), level .getOperations(), level.getMinMoves() + 1); moves =
	 * solution.moves;
	 * 
	 * assertEquals(level.getMinMoves(), moves.size()); assertEquals(4,
	 * moves.size()); Iterator<Move> iter = moves.iterator();
	 * assertEquals("Subtract x", iter.next().getOperation().toString());
	 * assertEquals("Divide by 2", iter.next().getOperation().toString());
	 * assertEquals("Swap", iter.next().getOperation().toString());
	 * assertEquals("Add 3", iter.next().getOperation().toString()); } catch
	 * (AssertionError e) { for (Move move : moves) {
	 * System.out.println(move.toString()); } throw e; } }
	 * 
	 * public void testSolverLevel10() { Level level = Levels.get("1-11");
	 * 
	 * // Expression left = new Expression(-1, -2); // Expression right = new
	 * Expression(2, -8); // Equation eq = new Equation(left, right); //
	 * ArrayList<Operation> ops = new ArrayList<Operation>(); // ops.add(new
	 * Add(new Expression(3))); // ops.add(new Divide(3)); // ops.add(new
	 * Multiply(3)); // // ops.add(new Add(new Expression(3))); // ops.add(new
	 * Add(new Expression(9, 0))); // ops.add(new Swap()); // // min not
	 * correct? // Level level = new Level("10", "Swap", eq, ops, 15, 11);
	 * 
	 * for (int i = 0; i < 13; i++) { // int i = 11; long start =
	 * System.nanoTime(); TargetedEquationSolver solver = new
	 * TargetedEquationSolver(); Solution solution =
	 * solver.solve(level.getEquation(), new Equation( new Expression(1, 0), new
	 * Expression(6)), level .getOperations(), i); List<Move> moves =
	 * solution.moves; System.out.println("Depth " + i + ": " +
	 * TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms"); try {
	 * if (moves == null) { continue; } assertEquals(level.getMinMoves(),
	 * moves.size()); assertEquals(11, moves.size()); Iterator<Move> iter =
	 * moves.iterator(); assertEquals("Add 3",
	 * iter.next().getOperation().toString()); assertEquals("Add 3",
	 * iter.next().getOperation().toString()); assertEquals("Multiply by 3",
	 * iter.next().getOperation() .toString()); assertEquals("Add 3",
	 * iter.next().getOperation().toString()); assertEquals("Add 3",
	 * iter.next().getOperation().toString()); assertEquals("Multiply by 3",
	 * iter.next().getOperation() .toString()); assertEquals("Add 9x",
	 * iter.next().getOperation().toString()); assertEquals("Divide by 3",
	 * iter.next().getOperation() .toString()); assertEquals("Divide by 3",
	 * iter.next().getOperation() .toString()); assertEquals("Divide by 3",
	 * iter.next().getOperation() .toString()); assertEquals("Swap",
	 * iter.next().getOperation().toString()); break; } catch (AssertionError e)
	 * { for (Move move : moves) { System.out.println(move.toString()); } throw
	 * e; } } }
	 * 
	 * // TODO this takes too long to run as a test... // public void
	 * testSolverLevel12() { // Level level = Levels.get("12"); // // for (int i
	 * = 0; i < 28; i++) { // long start = System.nanoTime(); // EquationSolver
	 * solver = new EquationSolver(level.getEquation(), //
	 * level.getOperations(), i); // List<Move> moves = solver.solve(); //
	 * System.out.println("Depth " + i + ": " + //
	 * TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms"); // try
	 * { // if (moves == null) { // continue; // } //
	 * assertEquals(level.getMinMoves(), // moves.size()); // assertEquals(11,
	 * moves.size()); // Iterator<Move> iter = // moves.iterator(); //
	 * assertEquals("Subtract x", // iter.next().getOperation().toString()); //
	 * assertEquals("Add 3", // iter.next().getOperation().toString()); //
	 * assertEquals("Swap", // iter.next().getOperation().toString()); //
	 * assertEquals("Divide by 2", // iter.next().getOperation().toString()); //
	 * } catch (AssertionError e) { // for (Move move : moves) { //
	 * System.out.println(move.toString()); // } // throw e; // } // } // }
	 * 
	 * public void testSolverLevel11() { Level level = Levels.get("1-12");
	 * 
	 * for (int i = 0; i < 10; i++) { long start = System.nanoTime();
	 * TargetedEquationSolver solver = new TargetedEquationSolver(); Solution
	 * solution = solver.solve(level.getEquation(), new Equation( new
	 * Expression(1, 0), new Expression(-3)), level .getOperations(), i);
	 * List<Move> moves = solution.moves; System.out.println("Depth " + i + ": "
	 * + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms"); try
	 * { if (moves == null) { continue; } assertEquals(level.getMinMoves(),
	 * moves.size()); assertEquals(10, moves.size()); Iterator<Move> iter =
	 * moves.iterator(); assertEquals("Multiply by 3",
	 * iter.next().getOperation() .toString()); assertEquals("Add 3",
	 * iter.next().getOperation().toString()); assertEquals("Divide by 3",
	 * iter.next().getOperation() .toString()); assertEquals("Multiply by -1",
	 * iter.next().getOperation() .toString()); assertEquals("Add 3",
	 * iter.next().getOperation().toString()); assertEquals("Divide by 3",
	 * iter.next().getOperation() .toString()); assertEquals("Subtract x",
	 * iter.next().getOperation() .toString()); assertEquals("Subtract x",
	 * iter.next().getOperation() .toString()); assertEquals("Add 3",
	 * iter.next().getOperation().toString()); assertEquals("Multiply by -1",
	 * iter.next().getOperation() .toString()); } catch (AssertionError e) { for
	 * (Move move : moves) { System.out.println(move.toString()); } throw e; } }
	 * }
	 * 
	 * // public void testSolverLevel13() { // Level level = Levels.get("3-1");
	 * // // long start = System.nanoTime(); // TargetedEquationSolver solver =
	 * new TargetedEquationSolver(); // List<Move> moves =
	 * solver.solve(level.getEquation(), // new Equation(new Expression(1, 0),
	 * new Expression(-3)), // level.getOperations(), 22, new OnCalculateMove()
	 * { // @Override // public boolean shouldContinue() { // return true; // }
	 * // // @Override // public void calculated(SolverState currentState) { //
	 * if (currentState.getNumMovesVisited() % 5 == 0) { //
	 * System.out.println(currentState.getNumMovesVisited() + " / " // +
	 * currentState.getTotalMoveSpace() // + ": " // + (((double)
	 * currentState.getNumMovesVisited()) / //
	 * currentState.getTotalMoveSpace())); // } // } // }, new OnCalculateMove()
	 * { // @Override // public boolean shouldContinue() { // return true; // }
	 * // // @Override // public void calculated(SolverState currentState) { //
	 * if (currentState.getNumMovesVisited() % 5 == 0) { // System.out //
	 * .println("   backwards solver  " // + currentState.getNumMovesVisited()
	 * // + " / " // + currentState.getTotalMoveSpace() // + ": " // +
	 * (((double) currentState.getNumMovesVisited()) / currentState //
	 * .getTotalMoveSpace())); // } // } // }); // try { //
	 * assertEquals(level.getMinMoves(), moves.size()); // assertEquals(10,
	 * moves.size()); // Iterator<Move> iter = moves.iterator(); //
	 * assertEquals("Multiply by 3", iter.next().getOperation().toString()); //
	 * assertEquals("Add 3", iter.next().getOperation().toString()); //
	 * assertEquals("Divide by 3", iter.next().getOperation().toString()); //
	 * assertEquals("Multiply by -1", iter.next().getOperation().toString()); //
	 * assertEquals("Add 3", iter.next().getOperation().toString()); //
	 * assertEquals("Divide by 3", iter.next().getOperation().toString()); //
	 * assertEquals("Subtract x", iter.next().getOperation().toString()); //
	 * assertEquals("Subtract x", iter.next().getOperation().toString()); //
	 * assertEquals("Add 3", iter.next().getOperation().toString()); //
	 * assertEquals("Multiply by -1", iter.next().getOperation().toString()); //
	 * } catch (AssertionError e) { // for (Move move : moves) { //
	 * System.out.println(move.toString()); // } // throw e; // } // }
	 */
}
