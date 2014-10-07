package com.oakonell.findx.model;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.oakonell.findx.data.DataBaseHelper;

public class Puzzle {
	private Level level;
	private int numUndosLeft;
	private List<Move> moves = new ArrayList<Move>();

	public Puzzle(String puzzleId) {
		level = Levels.get(puzzleId);
		if (level == null) {
			throw new IllegalArgumentException("No level with id=" + puzzleId);
		}
		numUndosLeft = 2;
	}

	public void initialize(String puzzleId, List<Integer> opIndices,
			int numUndosUsed) {
		level = Levels.get(puzzleId);
		moves.clear();
		Equation startEquation = level.getEquation();
		List<Operation> operations = level.getOperations();
		Equation equation = startEquation;
		for (int index : opIndices) {
			if (index >= operations.size()) {
				throw new IllegalArgumentException(
						"Operator index out of bounds: " + index);
			}
			Move move = new Move(equation, operations.get(index));
			moves.add(move);
			equation = move.getEndEquation();
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

	public List<Move> getMoves() {
		return moves;
	}

	public List<Operation> getOperations() {
		return level.getOperations();
	}

	public boolean undo() {
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
		for (Move each : moves) {
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

	private int getOperatorIndex(Move move) {
		int index = 0;
		for (Operation each : level.getOperations()) {
			if (each.equals(move.getOperation())) {
				return index;
			}
			index++;
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
		level.possibilyUpdateRating(moves.size(), getUndosUsed());
	}

	public int getRating() {
		return level.calculateRating(moves.size(), getUndosUsed());
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

	public void apply(Operation operation) {
		if (!getOperations().contains(operation)) {
			throw new RuntimeException("Operation " + operation
					+ " is not a valid operation for puzzle " + getId());
		}
		Equation startEquation;
		if (moves.isEmpty()) {
			startEquation = level.getEquation();
		} else {
			startEquation = moves.get(moves.size() - 1).getEndEquation();
		}
		Move move = new Move(startEquation, operation);
		moves.add(move);
	}

	public boolean isSolved() {
		return !moves.isEmpty()
				&& moves.get(moves.size() - 1).getEndEquation().isSolved();
	}

}
