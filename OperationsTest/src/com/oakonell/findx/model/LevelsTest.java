package com.oakonell.findx.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import com.oakonell.findx.custom.model.AbstractEquationSolver.Solution;
import com.oakonell.findx.custom.model.EquationSolver;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Level;
import com.oakonell.findx.model.Level.LevelSolution;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.SquareRoot;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.Stage;

public class LevelsTest extends TestCase {

	public void testAllLevelsSolutions() {
		List<Stage> stages = Levels.getStages();
		for (Stage each : stages) {
			privateTestLevels(each);
		}
	}

	private void privateTestLevels(Stage stage) {
		for (Level each : stage.getLevels()) {
			System.out.println("-----------------  Level " + each.getName()
					+ " - " + each.getId());
			// test that stored solution solves the level\
			LevelSolution levelSolution = each.getLevelSolution();
			Equation equation = each.getEquation();
			List<Integer> firstOperations = levelSolution.getFirstOperations();
			boolean lastOpWasSquareRoot = false;
			for (Integer index : firstOperations) {
				if (lastOpWasSquareRoot) {
					throw new RuntimeException(
							"Performed a Square Root with more primary moves remaining?");
				}
				Operation op = each.getOperations().get(index);
				equation = op.apply(equation);
				lastOpWasSquareRoot = op instanceof SquareRoot;
			}
			if (!lastOpWasSquareRoot ||  equation.isSolved()) {
				assertTrue("Level " + each.getId() + " " + each.getName()
						+ " is not solved by stored solution ",
						equation.isSolved());
				continue;
			}

			List<Operation> operations = new ArrayList<Operation>(
					each.getOperations());
			Multiply multiplyNegOne = Multiply.NEGATE;
			Collections
					.replaceAll(operations, new SquareRoot(), multiplyNegOne);
			Equation secondEquation = new Equation(equation.getLhs(),
					multiplyNegOne.apply(equation.getRhs()));
			List<Integer> secondOps;
			List<Integer> ops;
			if (equation.equals(levelSolution.getSecondaryEquation1())) {
				ops = levelSolution.getSecondaryOperations1();
				secondOps = levelSolution.getSecondaryOperations2();
				assertEquals("Level " + each.getId() + " " + each.getName()
						+ " can't find secondary solutions",
						levelSolution.getSecondaryEquation2(), secondEquation);
			} else {
				ops = levelSolution.getSecondaryOperations2();
				secondOps = levelSolution.getSecondaryOperations1();
				assertEquals("Level " + each.getId() + " " + each.getName()
						+ " can't find secondary solutions",
						levelSolution.getSecondaryEquation1(), secondEquation);
			}
			for (Integer index : ops) {
				Operation op = each.getOperations().get(index);
				equation = op.apply(equation);
			}
			assertTrue("Level " + each.getId() + " " + each.getName()
					+ " is not solved by stored solution ", equation.isSolved());

			equation = secondEquation;
			for (Integer index : secondOps) {
				Operation op = each.getOperations().get(index);
				equation = op.apply(equation);
			}
			assertTrue("Level " + each.getId() + " " + each.getName()
					+ " is not solved by stored solution ", equation.isSolved());

		}

	}

}
