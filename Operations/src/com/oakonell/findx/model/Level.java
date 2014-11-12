package com.oakonell.findx.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.math3.fraction.Fraction;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.oakonell.findx.FindXApp;
import com.oakonell.findx.data.DataBaseHelper;

@Immutable
public class Level {
	private final Stage stage;
	private final String id;
	private final String name;
	private final Equation equation;
	private final List<Operation> operations;
	private final LevelSolution solution;

	public Level(Stage stage, String name, Equation equation,
			List<Operation> operations, List<Integer> solutionOpIndices) {
		this(stage, name, equation, operations, new LevelSolution(
				solutionOpIndices, equation, operations));
	}

	public Level(Stage stage, String name, Equation equation,
			List<Operation> operations, LevelSolution levelSolution) {
		this.stage = stage;
		id = stage.getId() + "-" + (stage.getLevels().size() + 1);
		this.name = name;
		this.equation = equation;
		this.operations = operations;

		solution = levelSolution;
	}

	public String getId() {
		return id;
	}

	public Level getNextLevel() {
		return stage.getNextLevel(this);
	}

	public Level getPreviousLevel() {
		return stage.getPreviousLevel(this);
	}

	public String getName() {
		return name;
	}

	public Equation getEquation() {
		return equation;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public int getMinMoves() {
		return solution.getNumMoves();
	}

	public int getRating() {
		DataBaseHelper helper = new DataBaseHelper(FindXApp.getContext());
		SQLiteDatabase db = helper.getReadableDatabase();
		int rating = getExistingRating(db);
		db.close();

		return rating;
	}

	private int getExistingRating(SQLiteDatabase db) {
		Cursor query = getExistingRatingRow(db);
		int rating = 0;

		if (query.moveToFirst()) {
			rating = 3;
			int movesIndex = query
					.getColumnIndex(DataBaseHelper.LevelProgressTable.MIN_MOVES);
			int numMoves = query.getInt(movesIndex);

			int numUndosIndex = query
					.getColumnIndex(DataBaseHelper.LevelProgressTable.NUM_UNDOS);
			int numUndos = query.getInt(numUndosIndex);
			rating = calculateRating(numMoves, numUndos);
		}
		query.close();
		return rating;
	}

	public int calculateRating(int numMoves, int undosUsed) {
		int rating = 3;
		if (undosUsed > 0) {
			rating--;
		}
		if (numMoves > getMinMoves()) {
			rating--;
		}
		return rating;
	}

	private Cursor getExistingRatingRow(SQLiteDatabase db) {
		Cursor query = db.query(DataBaseHelper.LEVEL_PROGRESS_TABLE_NAME, null,
				DataBaseHelper.LevelProgressTable.LEVEL_ID + "=?",
				new String[] { id }, null, null, null);
		return query;
	}

	public void possibilyUpdateRating(int moves, int undosUsed) {
		DataBaseHelper helper = new DataBaseHelper(FindXApp.getContext());
		SQLiteDatabase db = helper.getWritableDatabase();

		int newRating = calculateRating(moves, undosUsed);

		int existingRating = getExistingRating(db);
		if (existingRating >= newRating) {
			db.close();
			return;
		}

		ContentValues values = new ContentValues();
		values.put(DataBaseHelper.LevelProgressTable.LEVEL_ID, id);
		values.put(DataBaseHelper.LevelProgressTable.MIN_MOVES, moves);
		values.put(DataBaseHelper.LevelProgressTable.NUM_UNDOS, undosUsed);

		if (existingRating > 0) {
			// update
			db.update(DataBaseHelper.LEVEL_PROGRESS_TABLE_NAME, values,
					DataBaseHelper.LevelProgressTable.LEVEL_ID + "=?",
					new String[] { id });
		} else {
			// insert
			db.insert(DataBaseHelper.LEVEL_PROGRESS_TABLE_NAME, null, values);
		}

		db.close();
	}

	public static void resetLevelProgress() {
		DataBaseHelper helper = new DataBaseHelper(FindXApp.getContext());
		SQLiteDatabase db = helper.getWritableDatabase();

		db.delete(DataBaseHelper.LEVEL_PROGRESS_TABLE_NAME, null, null);
		db.close();
	}

	public boolean isUnlocked() {
		Level previous = getPreviousLevel();
		if (previous == null) {
			return true;
		}
		return previous.getRating() > 0;
	}

	public String getMultilineDescription() {
		StringBuilder builder = new StringBuilder();
		Equation equation = getEquation();
		builder.append(equation.toString());
		builder.append("\n");
		builder.append("with operators: ");
		for (Iterator<Operation> iter = getOperations().iterator(); iter
				.hasNext();) {
			Operation each = iter.next();
			builder.append(each.toString());
			if (iter.hasNext()) {
				builder.append(", ");
			}
		}
		return builder.toString();
	}

	public Stage getStage() {
		return stage;
	}

	public List<Fraction> getSolutions() {
		return solution.getSolutions();
	}

	public LevelSolution getLevelSolution() {
		return solution;
	}

	public static class LevelSolution {
		private final List<Integer> firstOperations;

		private final Equation secondaryEquation1;
		private final List<Integer> secondaryOperations1;

		private final Equation secondaryEquation2;
		private final List<Integer> secondaryOperations2;

		private final List<Fraction> solutions;

		public LevelSolution(List<Integer> solutionOpIndices,
				Equation startEquation, List<Operation> operations) {
			this.firstOperations = solutionOpIndices;
			Equation equation = startEquation;
			for (Integer index : solutionOpIndices) {
				equation = operations.get(index).apply(equation);
			}
			solutions = new ArrayList<Fraction>();
			solutions.add(equation.getRhs().getConstant());
			this.secondaryEquation1 = null;
			this.secondaryOperations1 = null;
			this.secondaryEquation2 = null;
			this.secondaryOperations2 = null;
		}

		public LevelSolution(List<Fraction> solutions, List<Integer> first,
				Equation secondaryEquation1,
				List<Integer> secondaryOperations1, //
				Equation secondaryEquation2, List<Integer> secondaryOperations2) {
			this.firstOperations = first;
			this.secondaryEquation1 = secondaryEquation1;
			this.secondaryOperations1 = secondaryOperations1;
			this.secondaryEquation2 = secondaryEquation2;
			this.secondaryOperations2 = secondaryOperations2;
			this.solutions = solutions;
		}

		public List<Fraction> getSolutions() {
			return solutions;
		}

		public List<Integer> getFirstOperations() {
			return firstOperations;
		}

		public Equation getSecondaryEquation1() {
			return secondaryEquation1;
		}

		public List<Integer> getSecondaryOperations1() {
			return secondaryOperations1;
		}

		public Equation getSecondaryEquation2() {
			return secondaryEquation2;
		}

		public List<Integer> getSecondaryOperations2() {
			return secondaryOperations2;
		}

		public int getNumMoves() {
			int numMoves = firstOperations.size();
			if (secondaryOperations1 != null) {
				return numMoves + secondaryOperations1.size()
						+ secondaryOperations2.size();
			}
			return numMoves;
		}
	}
}
