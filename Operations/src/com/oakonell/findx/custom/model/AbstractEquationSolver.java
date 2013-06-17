package com.oakonell.findx.custom.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ReferenceMap;

import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.Operation;

public abstract class AbstractEquationSolver {

    public static interface OnCalculateMove {
        public boolean shouldContinue();

        public void calculated(SolverState currentState);
    }

    abstract protected boolean isSolution(Equation eq);

    public static class SolverState {
        // Apache collections are NOT generic? support it by casting here
        @SuppressWarnings("unchecked")
        private Map<Equation, Integer> equations = new ReferenceMap(ReferenceMap.SOFT, ReferenceMap.HARD);

        private List<Operation> operations;
        protected List<Operation> moves = new ArrayList<Operation>();
        protected Equation startEquation;
        protected Equation equation;

        protected List<Operation> solution;

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
            return solution != null;
        }

        public int getCurrentSolutionDepth() {
            return solution == null ? -1 : solution.size();
        }

        public List<Move> getCurrentSolution() {
            if (solution == null) {
                return null;
            }
            List<Move> moves = new ArrayList<Move>();
            Equation eq = startEquation;
            for (Operation each : solution) {
                Move move = new Move(eq, each);
                moves.add(move);
                eq = move.getEndEquation();
            }
            return moves;
        }
    }

    public List<Move> solve(Equation start, List<Operation> operations, int maxDepth, OnCalculateMove onCalculateMove) {
        return solve(start, operations, maxDepth, onCalculateMove, null);
    }

    protected List<Move> solve(Equation start, List<Operation> operations, int maxDepth,
            OnCalculateMove onCalculateMove, Map<Equation, Integer> alreadyVisited) {
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

        if (state.solution == null) {
            return null;
        }

        return state.getCurrentSolution();
    }

    private void depthSearch(SolverState state) {
        int depth = state.moves.size();
        if (state.onCalculateMove != null) {
            state.numMovesVisited++;
            state.onCalculateMove.calculated(state);
        }
        if (isSolution(state.equation)) {
            if (state.solution == null || depth < state.solution.size()) {
                state.solution = new ArrayList<Operation>(state.moves);
                state.maxDepth = state.solution.size();
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
        if (state.onCalculateMove != null && !state.onCalculateMove.shouldContinue()) {
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

            Equation nextEquation = each.apply(state.equation);
            Integer depthEquationEncountered = state.equations.get(nextEquation);
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

    private void pruneMoveCount(SolverState state, int depth) {
        long newMovesVisited = state.numMovesVisited
                + (long) Math.pow(state.operations.size(), state.initialMaxDepth - depth);
        if (newMovesVisited < state.numMovesVisited) {
            throw new RuntimeException("Long overflow occurred");
        }
        state.numMovesVisited = newMovesVisited;
    }

    protected void handleMaxDepthReached(SolverState state) {

    }

}
