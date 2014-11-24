package com.oakonell.findx.custom.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.IMove;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.MoveResult;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.ops.Multiply;

public abstract class AbstractEquationSolver {

	public static interface OnCalculateMove {
		public boolean shouldContinue();

		public void calculated(SolverState currentState);
	}

	public static class Solution {
		public Fraction solution;
		public Fraction solution2;
		public List<IMove> primaryMoves;
		public List<IMove> secondary1Moves;
		public List<IMove> secondary2Moves;

		public Solution(SolverState state, List<Operation> moves) {
			this(state, moves, null, null);
		}

		public Solution(SolverState state, List<Operation> moves,
				Solution solve, Solution solve2) {
			Equation equation = state.startEquation;
			primaryMoves = new ArrayList<IMove>();
			primaryMoves.add(new Move(equation, null, 0));
			int moveNum = 1;
			MoveResult applyMove = null;
			for (Operation each : moves) {
				applyMove = each.applyMove(equation, moveNum, null, null);
				primaryMoves.add(applyMove.getPrimaryMove());
				equation = applyMove.getPrimaryEndEquation();
				moveNum++;
			}

			if (applyMove != null && applyMove.hasMultiple()) {
				secondary1Moves = new ArrayList<IMove>();
				secondary2Moves = new ArrayList<IMove>();

				equation = applyMove.getSecondary1().getStartEquation();
				secondary1Moves.add(applyMove.getSecondary1());
				for (IMove each : solve.primaryMoves) {
					if (!(each instanceof Move)) {
						continue;
					}
					if (((Move) each).getOperation() == null) continue;
					secondary1Moves.add(new Move(each.getStartEquation(),
							((Move) each).getOperation(), moveNum++));
					equation = ((Move) each).getEndEquation();
				}
				solution = equation.getRhs().getConstant();

				equation = applyMove.getSecondary2().getStartEquation();
				secondary2Moves.add(applyMove.getSecondary2());
				for (IMove each : solve2.primaryMoves) {
					if (!(each instanceof Move)) {
						continue;
					}
					if (((Move) each).getOperation() == null) continue;
					secondary2Moves.add(new Move(each.getStartEquation(),
							((Move) each).getOperation(), moveNum++));
					equation = ((Move) each).getEndEquation();
				}
				solution2 = equation.getRhs().getConstant();
			} else {
				if (equation.isSolved()) {
					solution = equation.getRhs().getConstant();
				}
			}

		}

		public int getNumMoves() {
			int size = primaryMoves.size() - 1;
			if (secondary1Moves != null) {
				size += secondary1Moves.size() + secondary2Moves.size() - 2;
			}
			return size;
		}
	}

	abstract protected boolean isSolution(Equation eq);

	public static class SolverState {
		// Apache collections are NOT generic? support it by casting here
		@SuppressWarnings("unchecked")
		private Map<Equation, Integer> equations = new ReferenceMap(
				ReferenceMap.SOFT, ReferenceMap.HARD);

		private List<Operation> operations;
		protected List<Operation> moves = new ArrayList<Operation>();
		protected Equation startEquation;
		protected Equation equation;

		private Solution currentSolution;

		protected int maxDepth;
		private int initialMaxDepth;

		private long totalMoveSpace;
		// good for 5 operations up to ~27 moves
		private long numMovesVisited;
		private OnCalculateMove onCalculateMove;

		public int getMaxDepth() {
			return maxDepth;
		}

		public long getTotalMoveSpace() {
			return totalMoveSpace;
		}

		public long getNumMovesVisited() {
			return numMovesVisited;
		}

		public boolean hasCurrentSolution() {
			return currentSolution != null;
		}

		public int getCurrentSolutionDepth() {
			return currentSolution == null ? -1 : currentSolution.getNumMoves();
		}

		// public List<Move> getCurrentSolution() {
		// if (solution == null) {
		// return null;
		// }
		// List<Move> moves = new ArrayList<Move>();
		// Equation eq = startEquation;
		// for (Operation each : solution) {
		// Move move = new Move(eq, each);
		// moves.add(move);
		// eq = move.getEndEquation();
		// }
		// return moves;
		// }

		public Solution getSolution() {
			return currentSolution;
		}
	}

	public Solution solve(Equation start, List<Operation> operations,
			int maxDepth, OnCalculateMove onCalculateMove) {
		return solve(start, operations, maxDepth, onCalculateMove, null);
	}

	protected Solution solve(Equation start, List<Operation> operations,
			int maxDepth, OnCalculateMove onCalculateMove,
			Map<Equation, Integer> alreadyVisited) {
		SolverState state = new SolverState();
		state.equation = start;
		state.startEquation = start;
		state.operations = operations;
		state.maxDepth = maxDepth;
		state.initialMaxDepth = maxDepth;
		// TODO worry about overflow?
		state.totalMoveSpace = (long) Math.pow(operations.size(), maxDepth);
		state.onCalculateMove = onCalculateMove;

		if (alreadyVisited != null) {
			state.equations = alreadyVisited;
		}

		depthSearch(state);

		return state.getSolution();
	}

	private void depthSearch(SolverState state) {
		int depth = state.moves.size();
		if (state.onCalculateMove != null) {
			state.numMovesVisited++;
			state.onCalculateMove.calculated(state);
		}
		if (isSolution(state.equation)) {
			if (state.currentSolution == null
					|| depth < state.currentSolution.getNumMoves()) {
				state.currentSolution = new Solution(state, state.moves);

				// how to update number of moves visited for all remaining
				// branches
				pruneMoveCount(state, depth);
				return;
			}
			return;
		}
		if (depth >= state.maxDepth) {
			handleMaxDepthReached(state);
			pruneMoveCount(state, depth + 1);
			return;
		}
		if (state.onCalculateMove != null
				&& !state.onCalculateMove.shouldContinue()) {
			return;
		}

		for (Operation each : state.operations) {
			// don't just undo previous operation if there are reversible
			// operations
			if (depth >= 1) {
				if (state.moves.get(depth - 1).isInverse(each)) {
					pruneMoveCount(state, depth + 1);
					continue;
				}
			}

			if (!each.canApply(state.equation)) {
				pruneMoveCount(state, depth + 1);
				continue;
			}

			MoveResult moveResult = each.applyMove(state.equation, depth, null, null);
			if (moveResult.hasMultiple()) {
				// this has trouble matching an equation, assume only "solved"
				// solutions for now
				ArrayList<Operation> subOperations = new ArrayList<Operation>(
						state.operations);
				subOperations.remove(each);
				subOperations.add(Multiply.NEGATE);
				// TODO adjust move counts
				Solution solve = solve(moveResult.getSecondary1()
						.getStartEquation(), subOperations, state.maxDepth
						- depth, state.onCalculateMove, state.equations);
				Solution solve2 = solve(moveResult.getSecondary2()
						.getStartEquation(), subOperations, state.maxDepth
						- depth, state.onCalculateMove, state.equations);

				if (solve != null && solve2 != null) {
					int totalNumMoves = depth + solve.getNumMoves()
							+ solve2.getNumMoves();
					if (state.currentSolution == null
							|| totalNumMoves < state.currentSolution
									.getNumMoves()) {
						state.moves.add(each);
						state.currentSolution = new Solution(state,
								state.moves, solve, solve2);
						// update number of moves visited for all remaining
						// branches
						pruneMoveCount(state, totalNumMoves);
						state.moves.remove(depth);
						return;
					}
				}

			} else {
				Equation nextEquation = moveResult.getPrimaryEndEquation();
				Integer depthEquationEncountered = state.equations
						.get(nextEquation);
				if (depthEquationEncountered != null) {
					if (depthEquationEncountered <= depth) {
						pruneMoveCount(state, depth + 1);
						continue;
					}
				}
				Equation origEquation = state.equation;
				state.equation = nextEquation;
				state.equations.put(nextEquation, depth);
				state.moves.add(each);
				depthSearch(state);
				state.moves.remove(depth);
				state.equation = origEquation;
			}

		}
	}

	private void pruneMoveCount(SolverState state, int depth) {
		long newMovesVisited = state.numMovesVisited
				+ (long) Math.pow(state.operations.size(),
						state.initialMaxDepth - depth);
		if (newMovesVisited < state.numMovesVisited) {
			throw new RuntimeException("Long overflow occurred");
		}
		state.numMovesVisited = newMovesVisited;
	}

	protected void handleMaxDepthReached(SolverState state) {

	}

}
