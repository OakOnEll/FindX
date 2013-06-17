package com.oakonell.operations.model;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import com.oakonell.findx.custom.model.EquationMatcher;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Level;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Move;

public class EquationMatcherTest extends TestCase {
    public void testLevel2ButOne() {
        Level level = Levels.get("1-2");
        // x + 3 = 7
        List<Move> moves = null;
        try {

            EquationMatcher matcher = new EquationMatcher(new Equation(new Expression(1, 1), new Expression(5)));
            moves = matcher.solve(level.getEquation(), level.getOperations(), level.getMinMoves(), null);

            assertEquals(2, moves.size());
            assertEquals("Subtract 1", moves.get(0).getOperation().toString());
            assertEquals("Subtract 1", moves.get(1).getOperation().toString());

        } catch (AssertionError e) {
            for (Move move : moves) {
                System.out.println(move.toString());
            }
            throw e;
        }
    }

    public void testLevel3ButOne() {
        Level level = Levels.get("1-4");
        // x + 1 = 9
        List<Move> moves = null;
        try {

            EquationMatcher matcher = new EquationMatcher(new Equation(new Expression(1, 5), new Expression(13)));
            moves = matcher.solve(level.getEquation(), level.getOperations(), level.getMinMoves(), null);

            assertEquals(4, moves.size());
            Iterator<Move> iter = moves.iterator();
            assertEquals("Add 3", iter.next().getOperation().toString());
            assertEquals("Add 3", iter.next().getOperation().toString());
            assertEquals("Add 3", iter.next().getOperation().toString());
            assertEquals("Subtract 5", iter.next().getOperation().toString());

        } catch (AssertionError e) {
            for (Move move : moves) {
                System.out.println(move.toString());
            }
            throw e;
        }
    }
}
