package com.oakonell.findx.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import com.oakonell.findx.custom.model.AbstractEquationSolver.Solution;
import com.oakonell.findx.custom.model.EquationSolver;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.SquareRoot;

public class EquationSolverTest extends TestCase {

	public void testBetterSolution() {
		EquationSolver solver = new EquationSolver();
		List<Operation> operations = new ArrayList<Operation>();
		SquareRoot squareRoot = new SquareRoot();
		Add addOne = new Add(new Expression(1));
		Add addTwo = new Add(new Expression(2));
		operations.add(squareRoot);
		operations.add(addOne);
		operations.add(addTwo);
		
		Solution solve = solver.solve(new Equation(new Expression(1, 0, -1),
				new Expression(0, 0, 0)), operations, 2, null);
		
		
	}
	
	public void testSolveSquareRoot() {
		EquationSolver solver = new EquationSolver();
		List<Operation> operations = new ArrayList<Operation>();
		SquareRoot squareRoot = new SquareRoot();
		operations.add(squareRoot);
		Add addOne = new Add(new Expression(1));
		operations.add(addOne);

		Solution solve = solver.solve(new Equation(new Expression(1, 0, -1),
				new Expression(0, 0, 0)), operations, 2, null);
		try {
			assertEquals(addOne,
					((Move) solve.primaryMoves.get(1)).getOperation());
			assertEquals(squareRoot,
					((MultipleSolutionMove) solve.primaryMoves.get(2)).getOperation());
		} catch (AssertionError e) {
			for (IMove move : solve.primaryMoves) {
				System.out.println(move.toString());
			}
			throw e;
		}
	}

	public void testSolveAllLevels() {
		List<Stage> stages = Levels.getStages();
		for (Stage each : stages) {
			privateTestLevels(each);
		}
	}

	private void privateTestLevels(Stage stage) {
		EquationSolver solver = new EquationSolver();
		for (Level each : stage.getLevels()) {
			System.out.println("-----------------  Level " + each.getName()
					+ " - " + each.getId());
			if (each.getMinMoves() <= 20) {
				// Test that can be solved
				Solution solution = solver.solve(each.getEquation(),
						each.getOperations(), each.getMinMoves(), null);
				List<IMove> solve = solution.primaryMoves;
				for (IMove move : solve) {
					System.out.println(move.toString());
				}
				assertNotNull("Level " + each.getId()
						+ " didn't solve in min moves " + each.getMinMoves(),
						solve);
			}
		}
	}

	public void testSolverLevel0() {
		List<IMove> moves = null;
		try {
			Level level = Levels.get("1-1");
			EquationSolver solver = new EquationSolver();
			Solution solution = solver.solve(level.getEquation(),
					level.getOperations(), level.getMinMoves(), null);
			moves = solution.primaryMoves;
			assertEquals(2, moves.size());
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

	public void testSolverLevel1() {
		List<IMove> moves = null;
		try {
			Level level = Levels.get("1-2");
			EquationSolver solver = new EquationSolver();
			Solution solution = solver.solve(level.getEquation(),
					level.getOperations(), level.getMinMoves(), null);
			moves = solution.primaryMoves;
			assertEquals(3, solution.getNumMoves());
			assertEquals(level.getMinMoves(), solution.getNumMoves());
			assertEquals("Subtract 1", ((Move) moves.get(1)).getOperation()
					.toString());
			assertEquals("Subtract 1", ((Move) moves.get(2)).getOperation()
					.toString());
			assertEquals("Subtract 1", ((Move) moves.get(3)).getOperation()
					.toString());
		} catch (AssertionError e) {
			for (IMove move : moves) {
				System.out.println(move.toString());
			}
			throw e;
		}
	}

	public void testSolverLevel2() {
		List<IMove> moves = null;
		try {
			Level level = Levels.get("1-3");
			EquationSolver solver = new EquationSolver();
			Solution solution = solver.solve(level.getEquation(),
					level.getOperations(), level.getMinMoves(), null);
			moves = solution.primaryMoves;
			assertEquals(level.getMinMoves(), solution.getNumMoves());
			Iterator<IMove> iter = moves.iterator();
			iter.next();
			assertEquals("Divide by 3", ((Move) iter.next()).getOperation()
					.toString());
			assertEquals("Add 6", ((Move) iter.next()).getOperation()
					.toString());
		} catch (AssertionError e) {
			for (IMove move : moves) {
				System.out.println(move.toString());
			}
			throw e;
		}
	}

	public void testSolverLevel3() {
		List<IMove> moves = null;
		try {
			Level level = Levels.get("1-4");
			EquationSolver solver = new EquationSolver();
			Solution solution = solver.solve(level.getEquation(),
					level.getOperations(), level.getMinMoves(), null);
			moves = solution.primaryMoves;
			assertEquals(level.getMinMoves(), solution.getNumMoves());
			Iterator<IMove> iter = moves.iterator();
			iter.next();
			assertEquals("Divide by 4", ((Move) iter.next()).getOperation()
					.toString());
			assertEquals("Add 33", ((Move) iter.next()).getOperation()
					.toString());
		} catch (AssertionError e) {
			for (IMove move : moves) {
				System.out.println(move.toString());
			}
			throw e;
		}
	}

	public void testSolverLevel4() {
		List<IMove> moves = null;
		try {
			Level level = Levels.get("1-5");
			EquationSolver solver = new EquationSolver();
			Solution solution = solver.solve(level.getEquation(),
					level.getOperations(), level.getMinMoves(), null);
			moves = solution.primaryMoves;
			assertEquals(level.getMinMoves(), solution.getNumMoves());
			Iterator<IMove> iter = moves.iterator();
			iter.next();
			assertEquals("Divide by 7 / 2", ((Move) iter.next()).getOperation()
					.toString());
			assertEquals("Add 2/3", ((Move) iter.next()).getOperation()
					.toString());
		} catch (AssertionError e) {
			for (IMove move : moves) {
				System.out.println(move.toString());
			}
			throw e;
		}
	}

	public void testSolverLevel5() {
		List<IMove> moves = null;
		try {
			Level level = Levels.get("1-6");
			EquationSolver solver = new EquationSolver();
			Solution solution = solver.solve(level.getEquation(),
					level.getOperations(), 5, null);
			moves = solution.primaryMoves;
			assertEquals(level.getMinMoves(), solution.getNumMoves());
			Iterator<IMove> iter = moves.iterator();
			iter.next();
			assertEquals("Subtract 3/5", ((Move) iter.next()).getOperation()
					.toString());
			assertEquals("Multiply by 4 / 5", ((Move) iter.next())
					.getOperation().toString());
		} catch (AssertionError e) {
			for (IMove move : moves) {
				System.out.println(move.toString());
			}
			throw e;
		}
	}

	public void testSolverLevel6() {
		List<IMove> moves = null;
		try {
			Level level = Levels.get("1-7");
			EquationSolver solver = new EquationSolver();
			Solution solution = solver.solve(level.getEquation(),
					level.getOperations(), level.getMinMoves(), null);
			moves = solution.primaryMoves;
			assertEquals(level.getMinMoves(), solution.getNumMoves());
			Iterator<IMove> iter = moves.iterator();
			iter.next();
			assertEquals("Multiply by 3 / 5", ((Move) iter.next())
					.getOperation().toString());
			assertEquals("Subtract 12", ((Move) iter.next()).getOperation()
					.toString());
		} catch (AssertionError e) {
			for (IMove move : moves) {
				System.out.println(move.toString());
			}
			throw e;
		}
	}

	public void testSolverLevel7() {
		List<IMove> moves = null;
		try {
			Level level = Levels.get("1-8");
			EquationSolver solver = new EquationSolver();
			Solution solution = solver.solve(level.getEquation(),
					level.getOperations(), level.getMinMoves(), null);
			moves = solution.primaryMoves;
			assertEquals(level.getMinMoves(), solution.getNumMoves());
			Iterator<IMove> iter = moves.iterator();
			iter.next();
			assertEquals("Subtract 1", ((Move) iter.next()).getOperation()
					.toString());
			assertEquals("Subtract 1", ((Move) iter.next()).getOperation()
					.toString());
			assertEquals("Add 3", ((Move) iter.next()).getOperation()
					.toString());
			assertEquals("Add 3", ((Move) iter.next()).getOperation()
					.toString());
		} catch (AssertionError e) {
			for (IMove move : moves) {
				System.out.println(move.toString());
			}
			throw e;
		}
	}

	public void testSolverLevel8() {
		List<IMove> moves = null;
		try {
			Level level = Levels.get("1-9");
			EquationSolver solver = new EquationSolver();
			Solution solution = solver.solve(level.getEquation(),
					level.getOperations(), level.getMinMoves(), null);
			moves = solution.primaryMoves;
			assertEquals(level.getMinMoves(), solution.getNumMoves());
			Iterator<IMove> iter = moves.iterator();
			iter.next();
			assertEquals("Add 3", ((Move) iter.next()).getOperation()
					.toString());
			assertEquals("Add 3", ((Move) iter.next()).getOperation()
					.toString());
			assertEquals("Add 3", ((Move) iter.next()).getOperation()
					.toString());
			assertEquals("Subtract 5", ((Move) iter.next()).getOperation()
					.toString());
			assertEquals("Subtract 5", ((Move) iter.next()).getOperation()
					.toString());
		} catch (AssertionError e) {
			for (IMove move : moves) {
				System.out.println(move.toString());
			}
			throw e;
		}
	}

	public void testSolverLevel9() {
		Level level = Levels.get("1-10");
		EquationSolver solver = new EquationSolver();
		Solution solution = solver.solve(level.getEquation(),
				level.getOperations(), level.getMinMoves(), null);
		List<IMove> moves = solution.primaryMoves;
		try {
			assertEquals(level.getMinMoves(), solution.getNumMoves());
			Iterator<IMove> iter = moves.iterator();
			iter.next();
			assertEquals("Divide by 3", ((Move) iter.next()).getOperation()
					.toString());
			assertEquals("Divide by 2", ((Move) iter.next()).getOperation()
					.toString());
			assertEquals("Divide by 2", ((Move) iter.next()).getOperation()
					.toString());
		} catch (AssertionError e) {
			for (IMove move : moves) {
				System.out.println(move.toString());
			}
			throw e;
		}
	}

	/*
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
	 * System.nanoTime(); EquationSolver solver = new EquationSolver(); Solution
	 * solution = solver.solve(level.getEquation(), level.getOperations(), i,
	 * null); List<Move> moves = solution == null ? null : solution.moves;
	 * System.out.println("Depth " + i + ": " +
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
	 * iter.next().getOperation().toString()); } catch (AssertionError e) { for
	 * (Move move : moves) { System.out.println(move.toString()); } throw e; } }
	 * }
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
	 * EquationSolver solver = new EquationSolver(); Solution solution =
	 * solver.solve(level.getEquation(), level.getOperations(), i, null);
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
	 * iter.next().getOperation() .toString()); assertEquals("Add 3",
	 * iter.next().getOperation().toString()); assertEquals("Subtract x",
	 * iter.next().getOperation() .toString()); assertEquals("Subtract x",
	 * iter.next().getOperation() .toString()); assertEquals("Multiply by -1",
	 * iter.next().getOperation() .toString()); } catch (AssertionError e) { for
	 * (Move move : moves) { System.out.println(move.toString()); } throw e; } }
	 * }
	 */
	// public void testSolverLevel12() {
	// Level level = Levels.get("2-13");
	//
	// long start = System.nanoTime();
	// EquationSolver solver = new EquationSolver();
	// List<Move> moves = solver.solve(level.getEquation(),
	// level.getOperations(), 22, new OnCalculateMove() {
	// @Override
	// public boolean shouldContinue() {
	// return true;
	// }
	//
	// @Override
	// public void calculated(SolverState currentState) {
	// if (currentState.getNumMovesVisited() % 5000 == 0) {
	// System.out.println(currentState.getNumMovesVisited() + " / " +
	// currentState.getTotalMoveSpace()
	// + ": " + (((double) currentState.getNumMovesVisited()) /
	// currentState.getTotalMoveSpace()));
	// }
	// }
	// });
	// try {
	// assertEquals(level.getMinMoves(), moves.size());
	// assertEquals(10, moves.size());
	// Iterator<Move> iter = moves.iterator();
	// assertEquals("Multiply by 3", iter.next().getOperation().toString());
	// assertEquals("Add 3", iter.next().getOperation().toString());
	// assertEquals("Divide by 3", iter.next().getOperation().toString());
	// assertEquals("Multiply by -1", iter.next().getOperation().toString());
	// assertEquals("Add 3", iter.next().getOperation().toString());
	// assertEquals("Divide by 3", iter.next().getOperation().toString());
	// assertEquals("Add 3", iter.next().getOperation().toString());
	// assertEquals("Subtract x", iter.next().getOperation().toString());
	// assertEquals("Subtract x", iter.next().getOperation().toString());
	// assertEquals("Multiply by -1", iter.next().getOperation().toString());
	// } catch (AssertionError e) {
	// for (Move move : moves) {
	// System.out.println(move.toString());
	// }
	// throw e;
	// }
	// }
}
