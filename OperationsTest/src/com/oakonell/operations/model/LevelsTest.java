package com.oakonell.operations.model;

import java.util.List;

import junit.framework.TestCase;

import com.oakonell.findx.custom.model.EquationSolver;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Level;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.Stage;

public class LevelsTest extends TestCase {

    public void testAllLevelsMinMoves() {
        List<Stage> stages = Levels.getStages();
        for (Stage each : stages) {
            privateTestLevels(each);
        }
    }

    private void privateTestLevels(Stage stage) {
        EquationSolver solver = new EquationSolver();
        for (Level each : stage.getLevels()) {
            System.out.println("-----------------  Level " + each.getName() + " - " + each.getId());
            if (each.getMinMoves() <= 20) {
                // Test that can be solved
                List<Move> solve = solver.solve(each.getEquation(), each.getOperations(), each.getMinMoves(), null);
                for (Move move : solve) {
                    System.out.println(move.toString());
                }
                assertNotNull("Level " + each.getId() + " didn't solve in min moves " + each.getMinMoves(), solve);
            }
            // also test that stored solution solves the level
            List<Operation> solution = each.getSolutionOperations();
            Equation equation = each.getEquation();
            for (Operation op : solution) {
                equation = op.apply(equation);
            }
            assertTrue("Level " + each.getId() + " " + each.getName() + " is not solved by stored solution ",
                    equation.isSolved());
        }

    }

}
