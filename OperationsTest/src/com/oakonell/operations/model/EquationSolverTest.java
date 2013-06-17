package com.oakonell.operations.model;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.oakonell.findx.custom.model.AbstractEquationSolver.OnCalculateMove;
import com.oakonell.findx.custom.model.AbstractEquationSolver.SolverState;
import com.oakonell.findx.custom.model.EquationSolver;
import com.oakonell.findx.model.Level;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Move;

public class EquationSolverTest extends TestCase {
    public void testSolverLevel0() {
        List<Move> moves = null;
        try {
            Level level = Levels.get("1-1");
            EquationSolver solver = new EquationSolver();
            moves = solver.solve(level.getEquation(), level.getOperations(), level.getMinMoves(), null);
            assertEquals(1, moves.size());
            assertEquals(level.getMinMoves(), moves.size());
            assertEquals("Add 1", moves.get(0).getOperation().toString());
        } catch (AssertionError e) {
            for (Move move : moves) {
                System.out.println(move.toString());
            }
            throw e;
        }
    }

    public void testSolverLevel1() {
        List<Move> moves = null;
        try {
            Level level = Levels.get("1-2");
            EquationSolver solver = new EquationSolver();
            moves = solver.solve(level.getEquation(), level.getOperations(), level.getMinMoves(), null);
            assertEquals(3, moves.size());
            assertEquals(level.getMinMoves(), moves.size());
            assertEquals("Subtract 1", moves.get(0).getOperation().toString());
            assertEquals("Subtract 1", moves.get(1).getOperation().toString());
            assertEquals("Subtract 1", moves.get(2).getOperation().toString());
        } catch (AssertionError e) {
            for (Move move : moves) {
                System.out.println(move.toString());
            }
            throw e;
        }
    }

    public void testSolverLevel2() {
        List<Move> moves = null;
        try {
            Level level = Levels.get("1-3");
            EquationSolver solver = new EquationSolver();
            moves = solver.solve(level.getEquation(), level.getOperations(), level.getMinMoves(), null);
            assertEquals(4, moves.size());
            assertEquals(level.getMinMoves(), moves.size());
            Iterator<Move> iter = moves.iterator();
            assertEquals("Subtract 1", iter.next().getOperation().toString());
            assertEquals("Subtract 1", iter.next().getOperation().toString());
            assertEquals("Add 3", iter.next().getOperation().toString());
            assertEquals("Add 3", iter.next().getOperation().toString());
        } catch (AssertionError e) {
            for (Move move : moves) {
                System.out.println(move.toString());
            }
            throw e;
        }
    }

    public void testSolverLevel3() {
        List<Move> moves = null;
        try {
            Level level = Levels.get("1-4");
            EquationSolver solver = new EquationSolver();
            moves = solver.solve(level.getEquation(), level.getOperations(), level.getMinMoves(), null);
            assertEquals(5, moves.size());
            assertEquals(level.getMinMoves(), moves.size());
            Iterator<Move> iter = moves.iterator();
            assertEquals("Add 3", iter.next().getOperation().toString());
            assertEquals("Add 3", iter.next().getOperation().toString());
            assertEquals("Add 3", iter.next().getOperation().toString());
            assertEquals("Subtract 5", iter.next().getOperation().toString());
            assertEquals("Subtract 5", iter.next().getOperation().toString());
        } catch (AssertionError e) {
            for (Move move : moves) {
                System.out.println(move.toString());
            }
            throw e;
        }
    }

    public void testSolverLevel4() {
        List<Move> moves = null;
        try {
            Level level = Levels.get("1-5");
            EquationSolver solver = new EquationSolver();
            moves = solver.solve(level.getEquation(), level.getOperations(), level.getMinMoves(), null);
            assertEquals(3, moves.size());
            assertEquals(level.getMinMoves(), moves.size());
            Iterator<Move> iter = moves.iterator();
            assertEquals("Divide by 3", iter.next().getOperation().toString());
            assertEquals("Divide by 2", iter.next().getOperation().toString());
            assertEquals("Divide by 2", iter.next().getOperation().toString());
        } catch (AssertionError e) {
            for (Move move : moves) {
                System.out.println(move.toString());
            }
            throw e;
        }
    }

    public void testSolverLevel5() {
        List<Move> moves = null;
        try {
            Level level = Levels.get("1-6");
            EquationSolver solver = new EquationSolver();
            moves = solver.solve(level.getEquation(), level.getOperations(), 5, null);
            assertEquals(5, moves.size());
            assertEquals(level.getMinMoves(), moves.size());
            Iterator<Move> iter = moves.iterator();
            assertEquals("Subtract 1", iter.next().getOperation().toString());
            assertEquals("Divide by 2", iter.next().getOperation().toString());
            assertEquals("Subtract 1", iter.next().getOperation().toString());
            assertEquals("Divide by 2", iter.next().getOperation().toString());
            assertEquals("Subtract 1", iter.next().getOperation().toString());
        } catch (AssertionError e) {
            for (Move move : moves) {
                System.out.println(move.toString());
            }
            throw e;
        }
    }

    public void testSolverLevel6() {
        List<Move> moves = null;
        try {
            Level level = Levels.get("1-7");
            EquationSolver solver = new EquationSolver();
            moves = solver.solve(level.getEquation(), level.getOperations(), level.getMinMoves(), null);
            assertEquals(5, moves.size());
            assertEquals(level.getMinMoves(), moves.size());
            Iterator<Move> iter = moves.iterator();
            assertEquals("Multiply by 3", iter.next().getOperation().toString());
            assertEquals("Add 3", iter.next().getOperation().toString());
            assertEquals("Add 3", iter.next().getOperation().toString());
            assertEquals("Divide by 3", iter.next().getOperation().toString());
            assertEquals("Divide by 3", iter.next().getOperation().toString());
        } catch (AssertionError e) {
            for (Move move : moves) {
                System.out.println(move.toString());
            }
            throw e;
        }
    }

    public void testSolverLevel7() {
        List<Move> moves = null;
        try {
            Level level = Levels.get("1-8");
            EquationSolver solver = new EquationSolver();
            moves = solver.solve(level.getEquation(), level.getOperations(), level.getMinMoves(), null);
            assertEquals(4, moves.size());
            assertEquals(level.getMinMoves(), moves.size());
            Iterator<Move> iter = moves.iterator();
            assertEquals("Subtract 1", iter.next().getOperation().toString());
            assertEquals("Divide by 3", iter.next().getOperation().toString());
            assertEquals("Subtract 1", iter.next().getOperation().toString());
            assertEquals("Multiply by -1", iter.next().getOperation().toString());
        } catch (AssertionError e) {
            for (Move move : moves) {
                System.out.println(move.toString());
            }
            throw e;
        }
    }

    public void testSolverLevel8() {
        List<Move> moves = null;
        try {
            Level level = Levels.get("1-9");
            EquationSolver solver = new EquationSolver();
            moves = solver.solve(level.getEquation(), level.getOperations(), level.getMinMoves(), null);
            assertEquals(5, moves.size());
            assertEquals(level.getMinMoves(), moves.size());
            Iterator<Move> iter = moves.iterator();
            assertEquals("Subtract 1", iter.next().getOperation().toString());
            assertEquals("Divide by 2", iter.next().getOperation().toString());
            assertEquals("Subtract 1", iter.next().getOperation().toString());
            assertEquals("Subtract x", iter.next().getOperation().toString());
            assertEquals("Divide by 2", iter.next().getOperation().toString());
        } catch (AssertionError e) {
            for (Move move : moves) {
                System.out.println(move.toString());
            }
            throw e;
        }
    }

    public void testSolverLevel9() {
        Level level = Levels.get("1-10");
        EquationSolver solver = new EquationSolver();
        List<Move> moves = solver.solve(level.getEquation(), level.getOperations(), level.getMinMoves(), null);
        try {
            assertEquals(level.getMinMoves(), moves.size());
            assertEquals(4, moves.size());
            Iterator<Move> iter = moves.iterator();
            assertEquals("Subtract x", iter.next().getOperation().toString());
            assertEquals("Divide by 2", iter.next().getOperation().toString());
            assertEquals("Add 3", iter.next().getOperation().toString());
            assertEquals("Swap", iter.next().getOperation().toString());
        } catch (AssertionError e) {
            for (Move move : moves) {
                System.out.println(move.toString());
            }
            throw e;
        }
    }

    public void testSolverLevel10() {
        Level level = Levels.get("1-11");

        // Expression left = new Expression(-1, -2);
        // Expression right = new Expression(2, -8);
        // Equation eq = new Equation(left, right);
        // ArrayList<Operation> ops = new ArrayList<Operation>();
        // ops.add(new Add(new Expression(3)));
        // ops.add(new Divide(3));
        // ops.add(new Multiply(3));
        // // ops.add(new Add(new Expression(3)));
        // ops.add(new Add(new Expression(9, 0)));
        // ops.add(new Swap());
        // // min not correct?
        // Level level = new Level("10", "Swap", eq, ops, 15, 11);

        for (int i = 0; i < 13; i++) {
            // int i = 11;
            long start = System.nanoTime();
            EquationSolver solver = new EquationSolver();
            List<Move> moves = solver.solve(level.getEquation(), level.getOperations(), i, null);
            System.out.println("Depth " + i + ": " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms");
            try {
                if (moves == null) {
                    continue;
                }
                assertEquals(level.getMinMoves(), moves.size());
                assertEquals(11, moves.size());
                Iterator<Move> iter = moves.iterator();
                assertEquals("Add 3", iter.next().getOperation().toString());
                assertEquals("Add 3", iter.next().getOperation().toString());
                assertEquals("Multiply by 3", iter.next().getOperation().toString());
                assertEquals("Add 3", iter.next().getOperation().toString());
                assertEquals("Add 3", iter.next().getOperation().toString());
                assertEquals("Multiply by 3", iter.next().getOperation().toString());
                assertEquals("Add 9x", iter.next().getOperation().toString());
                assertEquals("Divide by 3",
                        iter.next().getOperation().toString());
                assertEquals("Divide by 3",
                        iter.next().getOperation().toString());
                assertEquals("Divide by 3",
                        iter.next().getOperation().toString());
                assertEquals("Swap", iter.next().getOperation().toString());
            } catch (AssertionError e) {
                for (Move move : moves) {
                    System.out.println(move.toString());
                }
                throw e;
            }
        }
    }

    // TODO this takes too long to run as a test...
    // public void testSolverLevel12() {
    // Level level = Levels.get("12");
    //
    // for (int i = 0; i < 28; i++) {
    // long start = System.nanoTime();
    // EquationSolver solver = new EquationSolver(level.getEquation(),
    // level.getOperations(), i);
    // List<Move> moves = solver.solve();
    // System.out.println("Depth " + i + ": " +
    // TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms");
    // try {
    // if (moves == null) {
    // continue;
    // }
    // assertEquals(level.getMinMoves(),
    // moves.size());
    // assertEquals(11, moves.size());
    // Iterator<Move> iter =
    // moves.iterator();
    // assertEquals("Subtract x",
    // iter.next().getOperation().toString());
    // assertEquals("Add 3",
    // iter.next().getOperation().toString());
    // assertEquals("Swap",
    // iter.next().getOperation().toString());
    // assertEquals("Divide by 2",
    // iter.next().getOperation().toString());
    // } catch (AssertionError e) {
    // for (Move move : moves) {
    // System.out.println(move.toString());
    // }
    // throw e;
    // }
    // }
    // }

    public void testSolverLevel11() {
        Level level = Levels.get("1-12");

        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            EquationSolver solver = new EquationSolver();
            List<Move> moves = solver.solve(level.getEquation(), level.getOperations(), i, null);
            System.out.println("Depth " + i + ": " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms");
            try {
                if (moves == null) {
                    continue;
                }
                assertEquals(level.getMinMoves(), moves.size());
                assertEquals(10, moves.size());
                Iterator<Move> iter = moves.iterator();
                assertEquals("Multiply by 3", iter.next().getOperation().toString());
                assertEquals("Add 3", iter.next().getOperation().toString());
                assertEquals("Divide by 3", iter.next().getOperation().toString());
                assertEquals("Multiply by -1", iter.next().getOperation().toString());
                assertEquals("Add 3", iter.next().getOperation().toString());
                assertEquals("Divide by 3", iter.next().getOperation().toString());
                assertEquals("Add 3", iter.next().getOperation().toString());
                assertEquals("Subtract x", iter.next().getOperation().toString());
                assertEquals("Subtract x", iter.next().getOperation().toString());
                assertEquals("Multiply by -1", iter.next().getOperation().toString());
            } catch (AssertionError e) {
                for (Move move : moves) {
                    System.out.println(move.toString());
                }
                throw e;
            }
        }
    }

//    public void testSolverLevel12() {
//        Level level = Levels.get("2-13");
//
//        long start = System.nanoTime();
//        EquationSolver solver = new EquationSolver();
//        List<Move> moves = solver.solve(level.getEquation(), level.getOperations(), 22, new OnCalculateMove() {
//            @Override
//            public boolean shouldContinue() {
//                return true;
//            }
//
//            @Override
//            public void calculated(SolverState currentState) {
//                if (currentState.getNumMovesVisited() % 5000 == 0) {
//                    System.out.println(currentState.getNumMovesVisited() + " / " + currentState.getTotalMoveSpace()
//                            + ": " + (((double) currentState.getNumMovesVisited()) / currentState.getTotalMoveSpace()));
//                }
//            }
//        });
//        try {
//            assertEquals(level.getMinMoves(), moves.size());
//            assertEquals(10, moves.size());
//            Iterator<Move> iter = moves.iterator();
//            assertEquals("Multiply by 3", iter.next().getOperation().toString());
//            assertEquals("Add 3", iter.next().getOperation().toString());
//            assertEquals("Divide by 3", iter.next().getOperation().toString());
//            assertEquals("Multiply by -1", iter.next().getOperation().toString());
//            assertEquals("Add 3", iter.next().getOperation().toString());
//            assertEquals("Divide by 3", iter.next().getOperation().toString());
//            assertEquals("Add 3", iter.next().getOperation().toString());
//            assertEquals("Subtract x", iter.next().getOperation().toString());
//            assertEquals("Subtract x", iter.next().getOperation().toString());
//            assertEquals("Multiply by -1", iter.next().getOperation().toString());
//        } catch (AssertionError e) {
//            for (Move move : moves) {
//                System.out.println(move.toString());
//            }
//            throw e;
//        }
//    }
}
