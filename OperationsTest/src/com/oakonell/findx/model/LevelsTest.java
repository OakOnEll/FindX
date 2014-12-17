package com.oakonell.findx.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.fraction.Fraction;

import junit.framework.TestCase;

import com.oakonell.findx.custom.model.AbstractEquationSolver.Solution;
import com.oakonell.findx.custom.model.EquationSolver;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Level;
import com.oakonell.findx.model.Level.LevelSolution;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.SquareRoot;
import com.oakonell.findx.model.ops.WildCard;
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
		for (ILevel each : stage.getLevels()) {
			System.out.println("-----------------  Level " + each.getName()
					+ " - " + each.getId());
			// test that stored solution solves the level\
			LevelSolution levelSolution = each.getLevelSolution();
			if (levelSolution.getSolutions().size() > 2) {
				fail(each + " has too many solutions");
			}
			Equation equation = each.getEquation();
			List<Integer> firstOperations = levelSolution.getFirstOperations();
			MoveResult moveResult = null;
			for (Integer index : firstOperations) {
				Operation op = each.getOperations().get(index);
				System.out.println(equation + ": " + op);
				moveResult = op.applyMove(equation, 0, each.getOperations(),
						null);
				equation = moveResult.getPrimaryEndEquation();
			}
			if (equation != null && equation.isSolved()) {
				if (levelSolution.getSolutions().size() > 1) {
					fail(each + " has too many solutions");
					assertTrue(equation.getRhs().getConstant()
							.compareTo(levelSolution.getSolutions().get(0)) == 0);
				}
				continue;
			}

			List<Operation> operations = new ArrayList<Operation>(
					each.getOperations());
			Multiply multiplyNegOne = Multiply.NEGATE;
			Collections
					.replaceAll(operations, new SquareRoot(), multiplyNegOne);
			for (Operation op : each.getOperations()) {
				if (!(op instanceof WildCard))
					continue;
				WildCard wild = (WildCard) op;
				if (wild.getActual() instanceof SquareRoot) {
					Collections.replaceAll(operations, op, multiplyNegOne);
				}
			}

			Equation secondEquation = moveResult.getSecondary1()
					.getStartEquation();
			equation = secondEquation;
			List<Integer> secondOps;
			List<Integer> ops;
			if (secondEquation.equals(levelSolution.getSecondaryEquation1())) {
				ops = levelSolution.getSecondaryOperations1();
				secondOps = levelSolution.getSecondaryOperations2();
				assertEquals("Level " + each.getId() + " " + each.getName()
						+ " can't find secondary solutions",
						levelSolution.getSecondaryEquation2(), moveResult
								.getSecondary2().getStartEquation());
			} else {
				ops = levelSolution.getSecondaryOperations2();
				secondOps = levelSolution.getSecondaryOperations1();
				assertEquals("Level " + each.getId() + " " + each.getName()
						+ " can't find secondary solutions",
						levelSolution.getSecondaryEquation1(), secondEquation);
			}
			for (Integer index : ops) {
				Operation op = operations.get(index);
				System.out.println(equation + ": " + op);
				equation = op.apply(equation);
			}
			assertTrue("Level " + each.getId() + " " + each.getName()
					+ " is not solved by stored solution - end equation "
					+ equation, equation.isSolved());
			assertSolutionInLevel(equation.getRhs().getConstant(),
					levelSolution);

			equation = moveResult.getSecondary2().getStartEquation();
			for (Integer index : secondOps) {
				Operation op = operations.get(index);
				System.out.println(equation + ": " + op);
				equation = op.apply(equation);
			}
			assertTrue("Level " + each.getId() + " " + each.getName()
					+ " is not solved by stored solution- end equation "
					+ equation, equation.isSolved());
			assertSolutionInLevel(equation.getRhs().getConstant(),
					levelSolution);

		}

	}

	private void assertSolutionInLevel(Fraction constant,
			LevelSolution levelSolution) {
		for (Fraction each : levelSolution.getSolutions()) {
			if (each.compareTo(constant) == 0)
				return;
		}
		fail("Solution " + constant + " is not in solutions: "
				+ levelSolution.getSolutions());
	}
}
