package com.oakonell.operations.model;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.Subtract;

public class CustomLevelBuilderTest extends TestCase {

    public void testFromStart() {
        CustomLevelBuilder builder = new CustomLevelBuilder();
        List<Operation> operations = builder.getOperations();

        List<Move> moves = builder.getMoves();
        assertEquals(1, moves.size());
        assertNull(moves.get(0).getOperation());

        Add add = new Add(new Expression(5));
        operations.add(add);

        builder.apply(add);

        assertEquals(2, moves.size());
        assertNull(moves.get(0).getOperation());

        builder.apply(add);

        assertEquals(3, moves.size());

        Move first = moves.get(0);
        assertEquals("x - 10 = -9", first.getStartEquation().toString());
        assertNull(first.getOperation());

        Move second = moves.get(1);
        assertEquals("x - 10 = -9", second.getStartEquation().toString());
        assertEquals(add, second.getOperation());

        Move third = moves.get(2);
        assertEquals("x - 5 = -4", third.getStartEquation().toString());
        assertEquals(add, third.getOperation());

    }

    public void testChangeSolution() {
        CustomLevelBuilder builder = new CustomLevelBuilder();
        List<Operation> operations = builder.getOperations();

        List<Move> moves = builder.getMoves();
        assertEquals(1, moves.size());
        assertNull(moves.get(0).getOperation());

        Operation add = new Add(new Expression(5));
        operations.add(add);
        Operation subtract = new Subtract(new Expression(1));
        operations.add(subtract);

        builder.apply(add);
        builder.apply(subtract);

        assertEquals(3, moves.size());

        Move zeroth = moves.get(0);
        assertEquals("x - 4 = -3", zeroth.getStartEquation().toString());
        assertNull(zeroth.getOperation());

        Move first = moves.get(1);
        assertEquals("x - 4 = -3", first.getStartEquation().toString());
        assertEquals(subtract, first.getOperation());

        Move second = moves.get(2);
        assertEquals("x - 5 = -4", second.getStartEquation().toString());
        assertEquals(add, second.getOperation());

        builder.setSolution(new Fraction(5));

        assertEquals(3, moves.size());
        zeroth = moves.get(0);
        assertEquals("x - 4 = 1", zeroth.getStartEquation().toString());
        assertNull(zeroth.getOperation());

        first = moves.get(1);
        assertEquals("x - 4 = 1", first.getStartEquation().toString());
        assertEquals(subtract, first.getOperation());

        second = moves.get(2);
        assertEquals("x - 5 = 0", second.getStartEquation().toString());
        assertEquals(add, second.getOperation());
    }

}
