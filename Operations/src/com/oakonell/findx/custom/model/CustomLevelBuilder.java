package com.oakonell.findx.custom.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.FindXApp;
import com.oakonell.findx.custom.model.AbstractEquationSolver.Solution;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.IMove;
import com.oakonell.findx.model.IMoveWithOperation;
import com.oakonell.findx.model.Level.LevelSolution;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.MultipleSolutionMove;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.SecondaryEquationMove;
import com.oakonell.findx.model.Stage;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.SquareRoot;

public class CustomLevelBuilder extends TempCorrectLevelBuilder {

	public CustomLevelBuilder() {
		setSolution(Fraction.ONE);
	}

	public List<IMove> getPrimaryMoves() {
		return primaryMoves;
	}
	public List<IMove> getSecondary1Moves() {
		return secondary1Moves;
	}
	public List<IMove> getSecondary2Moves() {
		return secondary2Moves;
	}
	
	public boolean canReplaceOperation(Operation operation) {
		// the same logistics apply to delete/edit, as an edit is effectively a
		// delete and apply the new operation
		return canDeleteOperation(operation);
	}

	public boolean canDeleteOperation(Operation op) {
		if (op instanceof SquareRoot) {
			// can't delete a square root operation if it is used
			IMove last = primaryMoves.get(primaryMoves.size() - 1);
			return !(last instanceof MultipleSolutionMove && ((MultipleSolutionMove) last)
					.getOperation().equals(op));
		}
		// otherwise, can't delete it if it is used in the secondary solutions
		return !usesOperationInSecondaries(op);
	}

	public boolean usesOperation(Operation operation) {
		return usesOperation(operation, primaryMoves)
				|| usesOperationInSecondaries(operation);
	}

	private boolean usesOperationInSecondaries(Operation op) {
		return usesOperation(op, secondary1Moves)
				|| usesOperation(op, secondary2Moves);
	}

	private boolean usesOperation(Operation op, List<IMove> moves) {
		for (IMove iEach : moves) {
			if (iEach instanceof MultipleSolutionMove) {
				if (((MultipleSolutionMove) iEach).getOperation().equals(op)) {
					return true;
				}
			}
			if (!(iEach instanceof Move))
				continue;

			Move each = (Move) iEach;

			if (each.getOperation() != null && each.getOperation().equals(op)) {
				return true;
			}
		}
		return false;
	}

	public void removeOperation(Operation operation) {
		if (!operations.remove(operation)) {
			return;
		}

		// An earlier check canDeleteOperation should have been called, and this
		// can only be called after
		// we only allow deleting an operation if used in the "primary" moves
		if (usesOperationInSecondaries(operation)) {
			throw new RuntimeException(
					"Can't delete an operation used in a secondary solution");
		}

		// we can't simply re-apply all the moves
		// find the latest index of the operation in primaryMoves
		int index = primaryMoves.size() - 1;
		for (; index >= 0; index--) {
			IMove iMove = primaryMoves.get(index);
			if (!(iMove instanceof Move))
				continue;
			Move move = (Move) iMove;
			Operation op = move.getOperation();
			if (op == null)
				continue;
			if (op.equals(operation))
				break;
		}
		if (index <= 0)
			return;

		Move item = (Move) primaryMoves.get(index);

		List<IMove> subList = primaryMoves.subList(1, index + 1);
		List<IMove> toReapply = new ArrayList<IMove>(subList.subList(0,
				subList.size() - 1));
		int numDeleted = subList.size();
		subList.clear();
		primaryMoves.remove(0);
		for (int i = 0; i < numDeleted; i++) {
			decrementMoveNumbers();
		}
		primaryMoves.add(0, new Move(item.getEndEquation(), null, 0));

		Collections.reverse(toReapply);
		for (IMove iEach : toReapply) {
			Move each = (Move) iEach;
			Operation eachOperation = each.getOperation();
			if (eachOperation.equals(operation))
				continue;
			apply(eachOperation, true);
		}
	}

	public void setSolution2(Fraction secondarySolution) {
		this.secondarySolution = secondarySolution;
	}

	public void setSolution(Fraction solution) {
		if (this.solution != null && this.solution.equals(solution)) {
			return;
		}
		// can't set the solution if a multiple solution operation has been
		// applied
		if (secondarySolution != null) {
			throw new RuntimeException(
					"Can't change the solution when there's a square root applied");
		}

		// Alternatively test that the new solution is valid with the
		// current moves
		// eg, if there is a square root, it may not be a rational square root
		// and should not be allowed

		this.solution = solution == null ? Fraction.ZERO : solution;
		// adjust the moves list
		Equation solvedEquation = new Equation(new Expression(1, 0),
				new Expression(Fraction.ZERO, solution));
		Move solvedMove = new Move(solvedEquation, null, 0);
		if (primaryMoves.size() <= 1) {
			primaryMoves.clear();
			primaryMoves.add(solvedMove);
			return;
		}
		List<IMove> oldMoves = new ArrayList<IMove>(primaryMoves.subList(1,
				primaryMoves.size()));
		Collections.reverse(oldMoves);
		primaryMoves.clear();
		primaryMoves.add(solvedMove);
		for (IMove iEach : oldMoves) {
			Move each = (Move) iEach;
			Operation op = each.getOperation();
			apply(op);
		}
	}

	public void apply(Operation op) {
		apply(op, true);
	}

	protected void apply(Operation op, boolean adjustMoveNumbers) {
		markAsOptimized(false);

		if (op == null || !operations.contains(op)) {
			throw new IllegalArgumentException("Operation " + op
					+ " is not one of the level's valid operations");
		}

		// top move should have no operation, just a starting equation
		Move move = (Move) primaryMoves.remove(0);

		Operation inverse = op.inverse();
		Equation newStartEquation = inverse.apply(move.getStartEquation());

		// here, need to solve the TWO equations if there was a branch
		Move newMove = new Move(newStartEquation, op, 1);
		if (op instanceof SquareRoot
				&& !(newStartEquation.getRhs().isZero() || newStartEquation
						.getLhs().isZero())) {
			// TODO put up a progress dialog for resolving roots
			// completely replace the moves list with these two solution moves
			int numMoves = primaryMoves.size();
			primaryMoves.clear();

			Equation rootEquation1 = op.apply(newStartEquation);
			Multiply negateOp = Multiply.NEGATE;
			Equation rootEquation2 = new Equation(rootEquation1.getLhs(),
					negateOp.apply(rootEquation1.getRhs()));
			primaryMoves.add(new MultipleSolutionMove(newStartEquation, op,
					rootEquation1.getLhs().toString() + " = � ( "
							+ rootEquation1.getRhs().toString() + " )", 1));
			EquationSolver solver = new EquationSolver();
			List<Operation> modOps = new ArrayList<Operation>(operations);
			modOps.remove(op);
			modOps.add(Multiply.NEGATE);
			Solution solution1 = solver.solve(rootEquation1, modOps, numMoves,
					null);
			Solution solution2 = solver.solve(rootEquation2, modOps, numMoves,
					null);
			if (solution1.solution.compareTo(solution) == 0) {
				secondarySolution = solution2.solution;
			} else if (solution2.solution.compareTo(solution) == 0) {
				secondarySolution = solution1.solution;
			} else {
				throw new RuntimeException(
						"Unexpected state- root solutions do not contain original solution.");
			}

			secondary1Moves.add(new SecondaryEquationMove(rootEquation1, 1));
			int i = 2;
			for (Move each : solution1.moves) {
				secondary1Moves.add(new Move(each.getStartEquation(), each
						.getOperation(), i++));
			}
			secondary2Moves.add(new SecondaryEquationMove(rootEquation2, 2));
			for (Move each : solution2.moves) {
				secondary2Moves.add(new Move(each.getStartEquation(), each
						.getOperation(), i++));
			}
		} else {
			if (adjustMoveNumbers) {
				// adjust each move's move Number
				incrementMoveNumbers();
			}
			primaryMoves.add(0, newMove);
		}

		primaryMoves.add(0, new Move(newStartEquation, null, 0));
	}

	public void deleteMove(IMove iMove) {
		// can't delete a secondary move
		if (secondary1Moves.contains(iMove) || secondary2Moves.contains(iMove)) {
			throw new RuntimeException(
					"Can't replace move in the secondary solutions");
		}
		if (!(iMove instanceof Move)) {
			throw new RuntimeException("Can't delete a non-move " + iMove);
		}
		Move item = (Move) iMove;
		int index = primaryMoves.indexOf(item);
		List<IMove> subList = primaryMoves.subList(1, index + 1);
		List<IMove> toReapply = new ArrayList<IMove>(subList.subList(0,
				subList.size() - 1));
		Collections.reverse(toReapply);
		subList.clear();
		primaryMoves.remove(0);
		decrementMoveNumbers();
		primaryMoves.add(0, new Move(item.getEndEquation(), null, 0));
		for (IMove iEach : toReapply) {
			Move each = (Move) iEach;
			apply(each.getOperation(), false);
		}
	}

	public void replaceMove(Move item, Operation op) {
		// can't delete a secondary move
		if (secondary1Moves.contains(item) || secondary2Moves.contains(item)) {
			throw new RuntimeException(
					"Can't replace move in the secondary solutions");
		}
		int index = primaryMoves.indexOf(item);
		List<IMove> subList = primaryMoves.subList(1, index + 1);
		List<IMove> toReapply = new ArrayList<IMove>(subList.subList(0,
				subList.size() - 1));
		Collections.reverse(toReapply);
		subList.clear();
		primaryMoves.remove(0);
		primaryMoves.add(0, new Move(item.getEndEquation(), null, 0));
		apply(op);
		for (IMove iEach : toReapply) {
			Move each = (Move) iEach;
			apply(each.getOperation());
		}
	}

	public void replaceOperation(Operation operation, Operation newOperation) {
		// can't delete a secondary move
		if (usesOperationInSecondaries(operation)) {
			throw new RuntimeException(
					"Can't replace operation used in secondary solutions");
		}
		int index = operations.indexOf(operation);
		operations.add(index, newOperation);
		operations.remove(operation);

		if (primaryMoves.size() <= 1) {
			return;
		}

		// revisit all the moves after the first use of the operation being replaced
		Equation solvedEquation = new Equation(new Expression(1, 0),
				new Expression(Fraction.ZERO, solution));
		Move solvedMove = new Move(solvedEquation, null, 0);

		List<IMove> oldMoves = new ArrayList<IMove>(primaryMoves.subList(1,
				primaryMoves.size()));
		Collections.reverse(oldMoves);
		primaryMoves.clear();
		primaryMoves.add(solvedMove);
		for (IMove iEach : oldMoves) {
			Move each = (Move) iEach;
			Operation op = each.getOperation();
			if (!op.equals(operation)) {
				apply(op);
			} else {
				apply(newOperation);
			}
		}

	}

	protected LevelSolution getLevelSolution() {
		List<Integer> first = new ArrayList<Integer>();
		for (IMove iEach : primaryMoves) {
			if (!(iEach instanceof IMoveWithOperation))
				continue;

			IMoveWithOperation each = (IMoveWithOperation) iEach;
			if (each.getOperation() == null) {
				continue;
			}
			int indexOf = operations.indexOf(each.getOperation());
			if (indexOf == -1) {
				throw new RuntimeException("The custom level " + getId()
						+ " solution moves contains an invalid operation "
						+ each.getOperation());
			}
			first.add(indexOf);
		}

		if (secondarySolution == null || secondary1Moves.isEmpty()) {
			return new LevelSolution(first, getCurrentStartEquation(),
					getOperations());
		}
		List<Integer> secondary1 = new ArrayList<Integer>();
		for (IMove iEach : secondary1Moves) {
			if (!(iEach instanceof IMoveWithOperation))
				continue;

			IMoveWithOperation each = (IMoveWithOperation) iEach;
			if (each.getOperation() == null) {
				continue;
			}
			int indexOf = operations.indexOf(each.getOperation());
			if (indexOf == -1) {
				throw new RuntimeException("The custom level " + getId()
						+ " solution moves contains an invalid operation "
						+ each.getOperation());
			}
			secondary1.add(indexOf);
		}
		List<Integer> secondary2 = new ArrayList<Integer>();
		for (IMove iEach : secondary1Moves) {
			if (!(iEach instanceof IMoveWithOperation))
				continue;

			IMoveWithOperation each = (IMoveWithOperation) iEach;
			if (each.getOperation() == null) {
				continue;
			}
			int indexOf = operations.indexOf(each.getOperation());
			if (indexOf == -1) {
				throw new RuntimeException("The custom level " + getId()
						+ " solution moves contains an invalid operation "
						+ each.getOperation());
			}
			secondary2.add(indexOf);
		}
		List<Fraction> solutions = new ArrayList<Fraction>();
		solutions.add(solution);
		solutions.add(secondarySolution);
		return new LevelSolution(solutions, first, secondary1Moves.get(0)
				.getStartEquation(), secondary1, secondary2Moves.get(0)
				.getStartEquation(), secondary2);

	}

	public void replaceMoves(Solution result) {
		// TODO validate that the moves are valid for eg square root
		// TODO something doesn't seem right here...
		Move originalLastMove = (Move) primaryMoves
				.get(primaryMoves.size() - 1);
		primaryMoves.subList(1, primaryMoves.size()).clear();
		for (Move each : result.moves) {
			primaryMoves.add(each);
		}
		Move newLastMove = (Move) primaryMoves.get(primaryMoves.size() - 1);
		if (!newLastMove.getEndEquation().equals(
				originalLastMove.getEndEquation())) {
			throw new RuntimeException(
					"The end equations should match upon replacing the moves!");
		}
	}

	public void load(long id) {
		CustomLevelDBReader reader = new CustomLevelDBReader();
		reader.read(FindXApp.getContext(), this, id);
	}

	public void save() {
		CustomLevelDBWriter writer = new CustomLevelDBWriter();
		writer.write(FindXApp.getContext(), this);
	}

	public CustomLevel convertToLevel(Stage custom) {
		CustomLevel level = new CustomLevel(this, custom);
		return level;
	}

	public List<IMove> getRawMoves() {
		return primaryMoves;
	}

}
