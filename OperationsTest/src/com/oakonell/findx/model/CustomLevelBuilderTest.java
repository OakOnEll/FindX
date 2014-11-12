package com.oakonell.findx.model;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.IMove;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.SquareRoot;
import com.oakonell.findx.model.ops.Subtract;

public class CustomLevelBuilderTest extends TestCase {

	public void testRemoveOperation() {
		CustomLevelBuilder builder = new CustomLevelBuilder();
		List<Operation> operations = builder.getOperations();

		List<IMove> moves = builder.getMoves();
		assertEquals(1, moves.size());
		assertNull(((Move) moves.get(0)).getOperation());
		assertEquals(0, builder.getNumMoves());

		Add add = new Add(new Expression(5));
		operations.add(add);

		Add add2 = new Add(new Expression(7));
		operations.add(add2);

		builder.apply(add);
		assertTrue(builder.usesOperation(add));
		assertFalse(builder.usesOperation(add2));
		builder.canDeleteOperation(add2);
		builder.removeOperation(add2);

		builder.removeOperation(new Add(new Expression(1)));
	}

	public void testFromStart() {
		CustomLevelBuilder builder = new CustomLevelBuilder();
		List<Operation> operations = builder.getOperations();

		List<IMove> moves = builder.getMoves();
		assertEquals(1, moves.size());
		assertNull(((Move) moves.get(0)).getOperation());
		assertEquals(0, builder.getNumMoves());

		Add add = new Add(new Expression(5));
		operations.add(add);

		builder.apply(add);

		assertEquals(2, moves.size());
		assertNull(((Move) moves.get(0)).getOperation());
		assertEquals(1, builder.getNumMoves());

		builder.apply(add);

		assertEquals(3, moves.size());
		assertEquals(2, builder.getNumMoves());

		Move first = ((Move) moves.get(0));
		assertEquals("x - 10 = -9", first.getStartEquation().toString());
		assertNull(first.getOperation());

		Move second = ((Move) moves.get(1));
		assertEquals("x - 10 = -9", second.getStartEquation().toString());
		assertEquals(add, second.getOperation());

		Move third = ((Move) moves.get(2));
		assertEquals("x - 5 = -4", third.getStartEquation().toString());
		assertEquals(add, third.getOperation());

		assertTrue(builder.canSetSolution());
	}

	public void testChangeSolution() {
		CustomLevelBuilder builder = new CustomLevelBuilder();
		List<Operation> operations = builder.getOperations();

		List<IMove> moves = builder.getMoves();
		assertEquals(1, moves.size());
		assertNull(((Move) moves.get(0)).getOperation());

		Operation add = new Add(new Expression(5));
		operations.add(add);
		Operation subtract = new Subtract(new Expression(1));
		operations.add(subtract);

		builder.apply(add);
		builder.apply(subtract);

		assertEquals(3, moves.size());

		Move zeroth = ((Move) moves.get(0));
		assertEquals("x - 4 = -3", zeroth.getStartEquation().toString());
		assertNull(zeroth.getOperation());

		Move first = ((Move) moves.get(1));
		assertEquals("x - 4 = -3", first.getStartEquation().toString());
		assertEquals(subtract, first.getOperation());

		Move second = ((Move) moves.get(2));
		assertEquals("x - 5 = -4", second.getStartEquation().toString());
		assertEquals(add, second.getOperation());

		builder.setSolution(new Fraction(5));

		assertEquals(3, moves.size());
		zeroth = ((Move) moves.get(0));
		assertEquals("x - 4 = 1", zeroth.getStartEquation().toString());
		assertNull(zeroth.getOperation());

		first = ((Move) moves.get(1));
		assertEquals("x - 4 = 1", first.getStartEquation().toString());
		assertEquals(subtract, first.getOperation());

		second = ((Move) moves.get(2));
		assertEquals("x - 5 = 0", second.getStartEquation().toString());
		assertEquals(add, second.getOperation());
	}

	public void testRemoveOperator() {
		CustomLevelBuilder builder = new CustomLevelBuilder();
		Add add3 = new Add(new Expression(3));
		Add add5 = new Add(new Expression(5));
		builder.addOperation(add3);
		builder.addOperation(add5);

		builder.apply(add3);
		assertEquals(1, builder.getNumMoves());
		builder.apply(add3);
		assertEquals(2, builder.getNumMoves());
		builder.apply(add5);
		assertEquals(3, builder.getNumMoves());

		builder.canDeleteOperation(add3);
		builder.removeOperation(add3);

		assertEquals(1, builder.getNumMoves());
		assertEquals("x - 5 = -4", builder.getCurrentStartEquation().toString());
	}

	public void testDeleteMove() {
		CustomLevelBuilder builder = new CustomLevelBuilder();
		Add add3 = new Add(new Expression(3));
		Add add5 = new Add(new Expression(5));
		builder.addOperation(add3);
		builder.addOperation(add5);

		builder.apply(add3);
		assertEquals(1, builder.getNumMoves());
		builder.apply(add3);
		assertEquals(2, builder.getNumMoves());
		builder.apply(add5);
		assertEquals(3, builder.getNumMoves());

		List<IMove> moves = builder.getMoves();
		assertEquals(4, moves.size());
		Move start = (Move) moves.get(0);
		Move moveAdd5 = (Move) moves.get(1);
		Move moveAdd3 = (Move) moves.get(2);
		Move move2Add3 = (Move) moves.get(3);

		builder.canDeleteMove(moveAdd3);
		builder.deleteMove(moveAdd3);

		assertEquals(2, builder.getNumMoves());
		assertEquals("x - 8 = -7", builder.getCurrentStartEquation().toString());
	}

	public void testSquareRootUse() {
		CustomLevelBuilder builder = new CustomLevelBuilder();
		SquareRoot squareRoot = new SquareRoot();
		Add add3 = new Add(new Expression(3));
		Add add5 = new Add(new Expression(5));
		builder.addOperation(squareRoot);
		builder.addOperation(add3);
		builder.addOperation(add5);

		builder.apply(add3);
		assertEquals(1, builder.getNumMoves());
		builder.apply(squareRoot);
		assertTrue(builder.usesOperation(add3));
		assertTrue(builder.usesOperation(squareRoot));
		assertFalse(builder.usesOperation(add5));
		assertEquals(3, builder.getNumMoves());
		builder.apply(add3);
		assertEquals(4, builder.getNumMoves());
		builder.apply(add5);
		assertEquals(5, builder.getNumMoves());
		assertTrue(builder.usesOperation(add5));

		Equation equation = builder.getCurrentStartEquation();
		assertEquals(new Fraction(5), builder.getSecondarySolution());

		assertEquals(5, builder.getNumMoves());

		List<IMove> moves = builder.getMoves();
		assertEquals(8, moves.size());
		Move start = (Move) moves.get(0);
		Move moveAdd5 = (Move) moves.get(1);
		Move moveAdd3 = (Move) moves.get(2);
		IMove moveSquareRoot = moves.get(3);
		IMove sol1 = moves.get(4);
		Move move2Add3 = (Move) moves.get(5);
		IMove sol2 = moves.get(6);
		Move move3Add3 = (Move) moves.get(7);

		assertEquals(add3, moveAdd3.getOperation());
		assertEquals(add5, moveAdd5.getOperation());
		assertEquals(add3, moveAdd3.getOperation());
		assertEquals(add3, moveAdd3.getOperation());

		assertEquals("0", start.getMoveNumText());
		assertEquals("1", moveAdd5.getMoveNumText());
		assertEquals("2", moveAdd3.getMoveNumText());
		assertEquals("3", moveSquareRoot.getMoveNumText());
		assertEquals("", sol1.getMoveNumText());
		assertEquals("4", move2Add3.getMoveNumText());
		assertEquals("", sol2.getMoveNumText());
		assertEquals("5", move3Add3.getMoveNumText());

		assertTrue(builder.canDeleteMove(moveAdd5));
		assertTrue(builder.canDeleteMove(moveAdd3));
		assertFalse(builder.canDeleteMove(moveSquareRoot));
		assertFalse(builder.canDeleteMove(move2Add3));
		assertFalse(builder.canDeleteMove(move3Add3));

		assertTrue(builder.canEditMove(moveAdd5));
		assertTrue(builder.canEditMove(moveAdd3));
		assertFalse(builder.canEditMove(moveSquareRoot));
		assertFalse(builder.canEditMove(move2Add3));
		assertFalse(builder.canEditMove(move3Add3));

		assertFalse(builder.canDeleteOperation(add3));
		assertFalse(builder.canDeleteOperation(squareRoot));
		assertTrue(builder.canDeleteOperation(add5));

		assertFalse(builder.canSetSolution());

		assertFalse(builder.canReplaceOperation(add3));
		assertFalse(builder.canReplaceOperation(squareRoot));
		assertTrue(builder.canReplaceOperation(add5));

		assertFalse(builder.canMoveUp(start));
		assertFalse(builder.canMoveUp(moveAdd5));
		assertTrue(builder.canMoveUp(moveAdd3));
		assertFalse(builder.canMoveUp(moveSquareRoot));
		assertFalse(builder.canMoveUp(sol1));
		assertFalse(builder.canMoveUp(move2Add3));
		assertFalse(builder.canMoveUp(sol2));
		assertFalse(builder.canMoveUp(move3Add3));

		assertFalse(builder.canMoveDown(start));
		assertTrue(builder.canMoveDown(moveAdd5));
		assertFalse(builder.canMoveDown(moveAdd3));
		assertFalse(builder.canMoveDown(moveSquareRoot));
		assertFalse(builder.canMoveDown(sol1));
		assertFalse(builder.canMoveDown(move2Add3));
		assertFalse(builder.canMoveDown(sol2));
		assertFalse(builder.canMoveDown(move3Add3));

		builder.moveDown(moveAdd5);
		assertEquals(8, moves.size());
		start = (Move) moves.get(0);
		moveAdd3 = (Move) moves.get(1);
		moveAdd5 = (Move) moves.get(2);
		moveSquareRoot = moves.get(3);
		sol1 = moves.get(4);
		move2Add3 = (Move) moves.get(5);
		sol2 = moves.get(6);
		move3Add3 = (Move) moves.get(7);

		assertEquals(add3, moveAdd3.getOperation());
		assertEquals(add5, moveAdd5.getOperation());
		assertEquals(add3, moveAdd3.getOperation());
		assertEquals(add3, moveAdd3.getOperation());

		assertEquals("0", start.getMoveNumText());
		assertEquals("1", moveAdd3.getMoveNumText());
		assertEquals("2", moveAdd5.getMoveNumText());
		assertEquals("3", moveSquareRoot.getMoveNumText());
		assertEquals("", sol1.getMoveNumText());
		assertEquals("4", move2Add3.getMoveNumText());
		assertEquals("", sol2.getMoveNumText());
		assertEquals("5", move3Add3.getMoveNumText());

		builder.moveUp(moveAdd5);
		assertEquals(8, moves.size());
		start = (Move) moves.get(0);
		moveAdd5 = (Move) moves.get(1);
		moveAdd3 = (Move) moves.get(2);
		moveSquareRoot = moves.get(3);
		sol1 = moves.get(4);
		move2Add3 = (Move) moves.get(5);
		sol2 = moves.get(6);
		move3Add3 = (Move) moves.get(7);

		assertEquals(add3, moveAdd3.getOperation());
		assertEquals(add5, moveAdd5.getOperation());
		assertEquals(add3, moveAdd3.getOperation());
		assertEquals(add3, moveAdd3.getOperation());

		assertEquals("0", start.getMoveNumText());
		assertEquals("1", moveAdd5.getMoveNumText());
		assertEquals("2", moveAdd3.getMoveNumText());
		assertEquals("3", moveSquareRoot.getMoveNumText());
		assertEquals("", sol1.getMoveNumText());
		assertEquals("4", move2Add3.getMoveNumText());
		assertEquals("", sol2.getMoveNumText());
		assertEquals("5", move3Add3.getMoveNumText());

		builder.removeOperation(add5);
		assertEquals(7, moves.size());
		assertEquals(4, builder.getNumMoves());
		start = (Move) moves.get(0);
		moveAdd3 = (Move) moves.get(1);
		moveSquareRoot = moves.get(2);
		sol1 = moves.get(3);
		move2Add3 = (Move) moves.get(4);
		sol2 = moves.get(5);
		move3Add3 = (Move) moves.get(6);

		assertEquals(add3, moveAdd3.getOperation());
		assertEquals(add3, moveAdd3.getOperation());
		assertEquals(add3, moveAdd3.getOperation());

		assertEquals("0", start.getMoveNumText());
		assertEquals("1", moveAdd3.getMoveNumText());
		assertEquals("2", moveSquareRoot.getMoveNumText());
		assertEquals("", sol1.getMoveNumText());
		assertEquals("3", move2Add3.getMoveNumText());
		assertEquals("", sol2.getMoveNumText());
		assertEquals("4", move3Add3.getMoveNumText());

		builder.addOperation(add5);
		builder.apply(add5);
		assertEquals(8, moves.size());
		start = (Move) moves.get(0);
		moveAdd5 = (Move) moves.get(1);
		moveAdd3 = (Move) moves.get(2);
		moveSquareRoot = moves.get(3);
		sol1 = moves.get(4);
		move2Add3 = (Move) moves.get(5);
		sol2 = moves.get(6);
		move3Add3 = (Move) moves.get(7);

		assertEquals(add3, moveAdd3.getOperation());
		assertEquals(add5, moveAdd5.getOperation());
		assertEquals(add3, moveAdd3.getOperation());
		assertEquals(add3, moveAdd3.getOperation());

		assertEquals("0", start.getMoveNumText());
		assertEquals("1", moveAdd5.getMoveNumText());
		assertEquals("2", moveAdd3.getMoveNumText());
		assertEquals("3", moveSquareRoot.getMoveNumText());
		assertEquals("", sol1.getMoveNumText());
		assertEquals("4", move2Add3.getMoveNumText());
		assertEquals("", sol2.getMoveNumText());
		assertEquals("5", move3Add3.getMoveNumText());

		assertTrue(builder.canDeleteMove(moveAdd5));
		builder.deleteMove(moveAdd5);

		assertEquals(7, moves.size());
		assertEquals(4, builder.getNumMoves());
		start = (Move) moves.get(0);
		moveAdd3 = (Move) moves.get(1);
		moveSquareRoot = moves.get(2);
		sol1 = moves.get(3);
		move2Add3 = (Move) moves.get(4);
		sol2 = moves.get(5);
		move3Add3 = (Move) moves.get(6);

		assertEquals(add3, moveAdd3.getOperation());
		assertEquals(add3, moveAdd3.getOperation());
		assertEquals(add3, moveAdd3.getOperation());

		assertEquals("0", start.getMoveNumText());
		assertEquals("1", moveAdd3.getMoveNumText());
		assertEquals("2", moveSquareRoot.getMoveNumText());
		assertEquals("", sol1.getMoveNumText());
		assertEquals("3", move2Add3.getMoveNumText());
		assertEquals("", sol2.getMoveNumText());
		assertEquals("4", move3Add3.getMoveNumText());

	}

}
