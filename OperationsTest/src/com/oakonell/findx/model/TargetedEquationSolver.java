package com.oakonell.findx.model;

import java.util.ArrayList;
import java.util.List;

import com.oakonell.findx.custom.model.AbstractEquationSolver.OnCalculateMove;
import com.oakonell.findx.custom.model.AbstractEquationSolver.Solution;
import com.oakonell.findx.custom.model.EquationSolver;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Operation;

public class TargetedEquationSolver {

	public Solution solve(Equation eq, Equation solution,
			List<Operation> operations, int maxDepth) {
		return solve(eq, solution, operations, maxDepth, null, null);
	}

	public Solution solve(Equation eq, Equation solution,
			List<Operation> operations, int maxDepth,
			OnCalculateMove onCalculateMove,
			OnCalculateMove onBackwardsCalculateMove) {

		List<Operation> inverseOperations = new ArrayList<Operation>();
		for (Operation each : operations) {
			inverseOperations.add(each.inverse());
		}

		ForwardSolver forwardSolver = new ForwardSolver(solution,
				inverseOperations, onBackwardsCalculateMove);

		return forwardSolver.solve(eq, operations, maxDepth / 2 + 1,
				onCalculateMove);

	}

	private static class ForwardSolver extends EquationSolver {
		private Equation solution;
		private List<Operation> inverseOperations;
		private OnCalculateMove onBackwardsCalculateMove;

		public ForwardSolver(Equation solution,
				List<Operation> inverseOperations,
				OnCalculateMove onBackwardsCalculateMove) {
			this.inverseOperations = inverseOperations;
			this.solution = solution;
			this.onBackwardsCalculateMove = onBackwardsCalculateMove;
		}

		// @Override
		// protected void handleMaxDepthReached(SolverState state) {
		// EquationMatcher backwardSolver = new EquationMatcher(state.equation);
		// List<Move> solve = backwardSolver.solve(solution, inverseOperations,
		// state.maxDepth,
		// onBackwardsCalculateMove);
		// if (solve != null) {
		//
		// if (state.solution == null || (state.moves.size() + solve.size()) <
		// state.solution.size()) {
		// state.solution = new ArrayList<Operation>(state.moves);
		// ListIterator<Move> listIterator = solve.listIterator(solve.size());
		// while (listIterator.hasPrevious()) {
		// Move inverseMove = listIterator.previous();
		// Operation inverseOperation = inverseMove.getOperation();
		// Operation operation = inverseOperation.inverse();
		// state.solution.add(operation);
		// }
		// state.maxDepth = state.solution.size() / 2;
		// return;
		// }
		// }
		// }
	}
}
