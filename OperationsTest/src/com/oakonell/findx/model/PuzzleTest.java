package com.oakonell.findx.model;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.math3.fraction.Fraction;

public class PuzzleTest extends TestCase {
	public void testLevel1() {
		Puzzle puzzle = new Puzzle("1-1");
		Equation currentEquation = puzzle.getCurrentEquation();
		assertEquals("x - 1 = 0", currentEquation.toString());
		assertEquals(0, puzzle.getNumMoves());
		assertEquals(1, puzzle.getMinMoves());
		Fraction[] solutions = puzzle.getSolutions();
		assertEquals(1, solutions.length);
		assertEquals(Fraction.ONE, solutions[0]);
		assertFalse(puzzle.canUndo());
		assertFalse(puzzle.isSolved());
		List<IMove> moves = puzzle.getMoves();
		assertEquals(0, moves.size());

		puzzle.apply(puzzle.getOperations().get(0));
		assertTrue(puzzle.isSolved());
		assertEquals(1, puzzle.getNumMoves());
		assertEquals(1, moves.size());
		IMove startMove = moves.get(0);
		assertTrue(startMove instanceof Move);
		assertEquals(
				"x = 1 <font color=\"#32cd32\"><big><bold>\u2713</bold></big></font>",
				((Move) startMove).getEndEquationString());
	}

	public void testLevel3() {
		Puzzle puzzle = new Puzzle("1-3");
		Equation currentEquation = puzzle.getCurrentEquation();
		assertEquals("3x - 18 = 15", currentEquation.toString());
		assertEquals(0, puzzle.getNumMoves());
		assertEquals(2, puzzle.getMinMoves());
		Fraction[] solutions = puzzle.getSolutions();
		assertEquals(1, solutions.length);
		assertEquals(new Fraction(11), solutions[0]);
		assertFalse(puzzle.canUndo());
		assertFalse(puzzle.isSolved());
		List<IMove> moves = puzzle.getMoves();
		assertEquals(0, moves.size());

		int startUndos = puzzle.getUndosLeft();

		puzzle.apply(puzzle.getOperations().get(0));
		assertFalse(puzzle.isSolved());
		assertEquals(1, puzzle.getNumMoves());
		assertEquals(1, moves.size());
		assertTrue(puzzle.canUndo());

		assertTrue(puzzle.undo());
		assertEquals(1, puzzle.getUndosUsed());
		assertEquals(startUndos - 1, puzzle.getUndosLeft());
		assertFalse(puzzle.isSolved());
		assertEquals(0, puzzle.getNumMoves());
		assertEquals(0, moves.size());
		assertFalse(puzzle.canUndo());
	}

	public void testLevel4_undos() {
		Puzzle puzzle = new Puzzle("1-4");
		Equation currentEquation = puzzle.getCurrentEquation();
		assertEquals("4x - 132 = 116", currentEquation.toString());
		puzzle.apply(puzzle.getOperations().get(0));
		puzzle.apply(puzzle.getOperations().get(0));
		puzzle.apply(puzzle.getOperations().get(0));
		puzzle.undo();
		puzzle.undo();
		assertFalse(puzzle.canUndo());
	}

	public void testTrivialSquareRoot() {
		Puzzle puzzle = new Puzzle("4-1");
		Equation currentEquation = puzzle.getCurrentEquation();
		assertEquals("x<sup><small>2</small></sup> = 0",
				currentEquation.toString());
		puzzle.apply(puzzle.getOperations().get(0));
		assertTrue(puzzle.isSolved());
	}

	public void testSquareRoot() {
		Puzzle puzzle = new Puzzle("4-2");
		Equation currentEquation = puzzle.getCurrentEquation();
		assertEquals("x<sup><small>2</small></sup> - 1 = 0",
				currentEquation.toString());
		puzzle.apply(puzzle.getOperations().get(0));
		assertFalse(puzzle.isSolved());

		puzzle.apply(puzzle.getOperations().get(1));
		assertTrue(puzzle.isSolved());
	}
}
