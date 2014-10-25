package com.oakonell.findx.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.fraction.Fraction;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.SquareRoot;

public class Puzzle {
	private Level level;
	private int numUndosLeft;
	private List<IMove> moves = new ArrayList<IMove>();
	private List<Operation> operations;

	private Equation equationInWaiting;
	private Equation currentEquation;
	private int numMoves = 0;

	public Puzzle(String puzzleId) {
		level = Levels.get(puzzleId);
		if (level == null) {
			throw new IllegalArgumentException("No level with id=" + puzzleId);
		}
		operations = new ArrayList<Operation>(level.getOperations());
		numUndosLeft = 2;
		currentEquation = level.getEquation();
	}

	public void initialize(String puzzleId, List<Integer> opIndices,
			int numUndosUsed) {
		level = Levels.get(puzzleId);
		moves.clear();
		Equation startEquation = level.getEquation();
		// TODO huh, the save state needs to include the wild card operations
		List<Operation> operations = new ArrayList<Operation>(
				level.getOperations());
		Equation equation = startEquation;
		for (int index : opIndices) {
			if (index >= operations.size()) {
				throw new IllegalArgumentException(
						"Operator index out of bounds: " + index);
			}
			numMoves++;
			apply(operations.get(index));
		}
		numUndosLeft = 2 - numUndosUsed;
	}

	public Level getLevel() {
		return level;
	}

	public String getName() {
		return level.getName();
	}

	public String getId() {
		return level.getId();
	}

	public int getMinMoves() {
		return level.getMinMoves();
	}

	public List<IMove> getMoves() {
		return moves;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public boolean undo() {
		// TODO deal with undoing square root
		if (moves.size() > 0) {
			moves.remove(moves.size() - 1);
			numUndosLeft--;
			return true;
		}
		return false;
	}

	public int getUndosLeft() {
		return numUndosLeft;
	}

	public boolean canUndo() {
		return moves.size() > 0 && numUndosLeft > 0;
	}

	public Level getNextLevel() {
		return level.getNextLevel();
	}

	public void writeState(SQLiteDatabase db) {
		// clear any existing records
		clearState(db);

		ContentValues levelInfo = new ContentValues();
		levelInfo.put(DataBaseHelper.CurrentLevelTable.LEVEL_ID, getId());
		levelInfo.put(DataBaseHelper.CurrentLevelTable.NUM_UNDOS_USED,
				getUndosUsed());
		db.insert(DataBaseHelper.CURRENT_LEVEL_STATE_TABLE_NAME, null,
				levelInfo);

		int seq = 1;
		for (IMove each : moves) {
			// TODO save wild card
			ContentValues moveInfo = new ContentValues();
			moveInfo.put(DataBaseHelper.CurrentLevelMovesTable.LEVEL_ID,
					getId());
			moveInfo.put(DataBaseHelper.CurrentLevelMovesTable.SEQUENCE, seq);
			int opIndex = getOperatorIndex(each);
			moveInfo.put(DataBaseHelper.CurrentLevelMovesTable.OP_INDEX,
					opIndex);
			db.insert(DataBaseHelper.CURRENT_LEVEL_MOVES_TABLE_NAME, null,
					moveInfo);
			seq++;
		}
	}

	private int getOperatorIndex(IMove move) {
		int index = 0;
		for (Operation each : getOperations()) {
			throw new RuntimeException("Implement me!");
			// if (each.equals(move.getOperation())) {
			// return index;
			// }
			// index++;
		}
		return -1;
	}

	private static void clearState(SQLiteDatabase db) {
		db.delete(DataBaseHelper.CURRENT_LEVEL_STATE_TABLE_NAME, null, null);
		db.delete(DataBaseHelper.CURRENT_LEVEL_MOVES_TABLE_NAME, null, null);
	}

	public static String readPendingLevel(SQLiteDatabase db) {
		try {
			Cursor query = db.query(
					DataBaseHelper.CURRENT_LEVEL_STATE_TABLE_NAME, null, null,
					null, null, null, null);
			if (!query.moveToFirst()) {
				query.close();
				return null;
			}

			int idIndex = query
					.getColumnIndex(DataBaseHelper.CurrentLevelTable.LEVEL_ID);
			return query.getString(idIndex);
		} finally {
			clearState(db);
		}
	}

	public static void readState(SQLiteDatabase db, String inLevelId,
			Puzzle puzzle) {
		try {
			Cursor query = db.query(
					DataBaseHelper.CURRENT_LEVEL_STATE_TABLE_NAME, null, null,
					null, null, null, null);
			if (!query.moveToFirst()) {
				query.close();
				return;
			}

			int idIndex = query
					.getColumnIndex(DataBaseHelper.CurrentLevelTable.LEVEL_ID);
			String levelId = query.getString(idIndex);
			if (inLevelId != null && !inLevelId.equals(levelId)) {
				query.close();
				clearState(db);
				return;
			}

			int numUndosIndex = query
					.getColumnIndex(DataBaseHelper.CurrentLevelTable.NUM_UNDOS_USED);
			int numUndosUsed = query.getInt(numUndosIndex);
			query.close();

			List<Integer> opIndices = new ArrayList<Integer>();
			Cursor moveQuery = db.query(
					DataBaseHelper.CURRENT_LEVEL_MOVES_TABLE_NAME, null, null,
					null, null, null,
					DataBaseHelper.CurrentLevelMovesTable.SEQUENCE);
			while (moveQuery.moveToNext()) {
				// moveQuery.getColumnIndex(DataBaseHelper.CurrentLevelMovesTable.LEVEL_ID);
				int opIndexIndex = moveQuery
						.getColumnIndex(DataBaseHelper.CurrentLevelMovesTable.OP_INDEX);
				int opIndex = moveQuery.getInt(opIndexIndex);
				if (opIndex < 0) {
					throw new IllegalArgumentException(
							"Operator index out of bounds: " + opIndex);
				}
				opIndices.add(opIndex);
			}
			moveQuery.close();

			puzzle.initialize(inLevelId, opIndices, numUndosUsed);
		} finally {
			clearState(db);
		}

	}

	public boolean hasAnyMoves() {
		return moves.size() > 0;
	}

	public void updateRating() {
		level.possibilyUpdateRating(getNumMoves(), getUndosUsed());
	}

	public int getRating() {
		return level.calculateRating(getNumMoves(), getUndosUsed());
	}

	public int getExistingRating() {
		return level.getRating();
	}

	public int getUndosUsed() {
		return 2 - numUndosLeft;
	}

	public String getMultilineDescription() {
		return level.getMultilineDescription();
	}

	public Stage getStage() {
		return level.getStage();
	}

	public Equation getStartEquation() {
		return level.getEquation();
	}

	public Equation getCurrentEquation() {
		return currentEquation;
	}

	public void apply(Operation operation) {
		if (!getOperations().contains(operation)) {
			throw new RuntimeException("Operation " + operation
					+ " is not a valid operation for puzzle " + getId());
		}
		Equation startEquation = currentEquation;

		numMoves++;
		Move move = new Move(startEquation, operation, numMoves);
		currentEquation = move.getEndEquation();
		if (operation instanceof SquareRoot) {
			moves.add(new MultipleSolutionMove(move.getDescriptiontext(),
					currentEquation.getLhs().toString() + " = ± ( "
							+ currentEquation.getRhs().toString() + " )",
					numMoves));
			moves.add(new SecondaryEquationMove(move.getEndEquation(), 1));
			// conditionally switch this no longer useful operator to a
			// multiply(-1) if one is not already present...
			boolean hasMutiplyNeg1 = false;
			for (Operation each : operations) {
				if (each instanceof Multiply) {
					if (((Multiply) each).getFactor().compareTo(Fraction.ONE) != 0) {
						hasMutiplyNeg1 = true;
					}
				}
			}
			Multiply negateOp = new Multiply(-1);
			if (!hasMutiplyNeg1) {
				Collections.replaceAll(operations, operation, negateOp);
			}
			equationInWaiting = new Equation(move.getEndEquation().getLhs(),
					negateOp.apply(move.getEndEquation().getRhs()));
		} else {
			moves.add(move);
		}
		if (move.getEndEquation().isSolved() && equationInWaiting != null) {
			moves.add(new SecondaryEquationMove(equationInWaiting, 2));
			currentEquation = equationInWaiting;
			equationInWaiting = null;
		}
	}

	public boolean isSolved() {
		return equationInWaiting == null && !moves.isEmpty()
				&& moves.get(moves.size() - 1).isSolved();
	}

	public int getNumMoves() {
		return numMoves;
	}

}
