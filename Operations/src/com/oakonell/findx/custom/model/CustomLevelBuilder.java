package com.oakonell.findx.custom.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.fraction.Fraction;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.oakonell.findx.FindXApp;
import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.Stage;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class CustomLevelBuilder {
	// the DB id
	private long id;

	private boolean isOptimized = true;
	private boolean isImported;
	private String author;

	private List<Operation> operations = new ArrayList<Operation>();

	private List<Move> moves = new ArrayList<Move>();
	private Fraction solution;

	private String title;
	private int sequence;

	public CustomLevelBuilder() {
		setSolution(Fraction.ONE);
	}

	public long getId() {
		return id;
	}

	protected void setId(long id) {
		this.id = id;
	}

	public int getSequence() {
		return sequence;
	}

	protected void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public Fraction getSolution() {
		return solution;
	}

	public void setSolution(Fraction solution) {
		if (this.solution != null && this.solution.equals(solution)) {
			return;
		}
		this.solution = solution == null ? Fraction.ZERO : solution;
		// adjust the moves list
		Equation solvedEquation = new Equation(new Expression(1, 0),
				new Expression(Fraction.ZERO, solution));
		Move solvedMove = new Move(solvedEquation, null);
		if (moves.size() <= 1) {
			moves.clear();
			moves.add(solvedMove);
			return;
		}
		List<Move> oldMoves = new ArrayList<Move>(
				moves.subList(1, moves.size()));
		Collections.reverse(oldMoves);
		moves.clear();
		moves.add(solvedMove);
		for (Move each : oldMoves) {
			Operation op = each.getOperation();
			apply(op);
		}
	}

	public void replaceMoves(List<Move> result) {
		// TODO something doesn't seem right here...
		Move originalLastMove = moves.get(moves.size() - 1);
		moves.subList(1, moves.size()).clear();
		for (Move each : result) {
			moves.add(each);
		}
		Move newLastMove = moves.get(moves.size() - 1);
		if (!newLastMove.getEndEquation().equals(
				originalLastMove.getEndEquation())) {
			throw new RuntimeException(
					"The end equations should match upon replacing the moves!");
		}
	}

	public List<Move> getMoves() {
		return moves;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void apply(Operation op) {
		markAsOptimized(false);

		if (op == null || !operations.contains(op)) {
			throw new IllegalArgumentException("Operation " + op
					+ " is not one of the level's valid operations");
		}

		Move move = moves.remove(0);
		Operation inverse = op.inverse();

		// top move should have no operation, just a starting equation
		Equation newStartEquation = inverse.apply(move.getStartEquation());
		Move newMove = new Move(newStartEquation, op);
		moves.add(0, newMove);
		moves.add(0, new Move(newStartEquation, null));
	}

	public boolean usesOperation(Operation op) {
		for (Move each : getMoves()) {
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
		// if only one "move" it is the solution, with no operation
		if (moves.size() <= 1) {
			return;
		}

		Equation solvedEquation = new Equation(new Expression(1, 0),
				new Expression(Fraction.ZERO, solution));
		Move solvedMove = new Move(solvedEquation, null);

		List<Move> oldMoves = new ArrayList<Move>(
				moves.subList(1, moves.size()));
		Collections.reverse(oldMoves);
		moves.clear();
		moves.add(solvedMove);
		for (Move each : oldMoves) {
			Operation op = each.getOperation();
			if (op.equals(operation)) {
				continue;
			}
			apply(op);
		}

	}

	public void load(long id) {
		CustomLevelDBReader reader = new CustomLevelDBReader();
		reader.read(FindXApp.getContext(), this, id);
	}

	public void save() {
		saveToParse();
		CustomLevelDBWriter writer = new CustomLevelDBWriter();
		writer.write(FindXApp.getContext(), this);
	}

	private void saveToParse() {
		ParseUser parseUser = ParseUser.getCurrentUser();
		if (parseUser != null) {
			try {
				ParseObject level = new ParseObject("CustomLevel");
				level.put("title", title);
				level.put("author", parseUser.getUsername());
				level.put("solution", solution);
				level.put("start_equation", moves.get(0).getStartEquation().toString());
				level.put("numMoves", moves.size());
				level.save();
				Map<Operation, ParseObject> opToParseOp = new HashMap<Operation, ParseObject>();
				int i = 0;
				for (Operation each : operations) {
					ParseObject parseOp = new ParseObject("LevelOperation");
					parseOp.put("level", level);
					each.addToParseObject(parseOp);
					opToParseOp.put(each, parseOp);
					parseOp.save();
					i++;
				}
				i = 0;
				for (Move each : moves) {
					ParseObject parseMove = new ParseObject("LevelMove");
					parseMove.put("level", level);
					parseMove.put("sequence", i);
					if (each.getOperation() != null) {
						ParseObject parseOp = opToParseOp.get(each
								.getOperation());
						parseMove.put("operation", parseOp);
					}
					parseMove.save();
					i++;
				}
			} catch (ParseException e) {
				Toast.makeText(FindXApp.getContext(),
						"Error writing level data: " + e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public CustomLevel convertToLevel(Stage custom) {
		CustomLevel level = new CustomLevel(this, custom);
		return level;
	}

	public void defaultMaxSequence() {
		DataBaseHelper helper = new DataBaseHelper(FindXApp.getContext());
		SQLiteDatabase db = helper.getReadableDatabase();
		int max = 0;
		Cursor cursor = db.rawQuery(
				"select MAX(" + DataBaseHelper.CustomLevelTable.SEQ_NUM
						+ ") as max_seq from "
						+ DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME, null);
		if (cursor.moveToFirst()) {
			max = cursor.getInt(0);
		}
		cursor.close();
		db.close();

		setSequence(max + 1);
	}

	public void prepareAsCopy() {
		id = 0;
	}

	public void moveUp(Move item) {
		int index = moves.indexOf(item);
		List<Move> subList = moves.subList(1, index + 1);
		List<Move> toReapply = new ArrayList<Move>(subList.subList(0,
				subList.size() - 1));
		subList.clear();
		moves.remove(0);
		moves.add(0, new Move(item.getEndEquation(), null));

		Collections.reverse(toReapply);
		Move first = toReapply.remove(0);
		apply(first.getOperation());
		apply(item.getOperation());
		for (Move each : toReapply) {
			apply(each.getOperation());
		}
	}

	public void moveDown(Move item) {
		int index = moves.indexOf(item);
		// the sublist includes ONE more move past the one being moved down
		List<Move> subList = moves.subList(1, index + 2);
		List<Move> toReapply = new ArrayList<Move>(subList.subList(0,
				subList.size()));
		Move next = subList.get(subList.size() - 1);
		subList.clear();
		moves.remove(0);
		moves.add(0, new Move(next.getEndEquation(), null));

		Collections.reverse(toReapply);
		toReapply.remove(item);
		apply(item.getOperation());
		for (Move each : toReapply) {
			apply(each.getOperation());
		}
	}

	public void deleteMove(Move item) {
		int index = moves.indexOf(item);
		List<Move> subList = moves.subList(1, index + 1);
		List<Move> toReapply = new ArrayList<Move>(subList.subList(0,
				subList.size() - 1));
		Collections.reverse(toReapply);
		subList.clear();
		moves.remove(0);
		moves.add(0, new Move(item.getEndEquation(), null));
		for (Move each : toReapply) {
			apply(each.getOperation());
		}
	}

	public void replaceMove(Move item, Operation op) {
		int index = moves.indexOf(item);
		List<Move> subList = moves.subList(1, index + 1);
		List<Move> toReapply = new ArrayList<Move>(subList.subList(0,
				subList.size() - 1));
		Collections.reverse(toReapply);
		subList.clear();
		moves.remove(0);
		moves.add(0, new Move(item.getEndEquation(), null));
		apply(op);
		for (Move each : toReapply) {
			apply(each.getOperation());
		}
	}

	public void replaceOperation(Operation operation, Operation newOperation) {
		int index = operations.indexOf(operation);
		operations.add(index, newOperation);
		operations.remove(operation);

		if (moves.size() <= 1) {
			return;
		}

		// revisit all the moves
		Equation solvedEquation = new Equation(new Expression(1, 0),
				new Expression(Fraction.ZERO, solution));
		Move solvedMove = new Move(solvedEquation, null);

		List<Move> oldMoves = new ArrayList<Move>(
				moves.subList(1, moves.size()));
		Collections.reverse(oldMoves);
		moves.clear();
		moves.add(solvedMove);
		for (Move each : oldMoves) {
			Operation op = each.getOperation();
			if (!op.equals(operation)) {
				apply(op);
			} else {
				apply(newOperation);
			}
		}

	}

	public void markAsOptimized(boolean isOptimized2) {
		isOptimized = isOptimized2;
		if (listener != null) {
			listener.isOptimized(isOptimized);
		}
	}

	public void markAsOptimized() {
		markAsOptimized(true);
	}

	public boolean isOptimized() {
		return isOptimized;
	}

	public boolean isImported() {
		return isImported;
	}

	public void setIsImported(boolean isImported) {
		this.isImported = isImported;
	}

	public List<Integer> getMoveOperationIndices() {
		List<Integer> result = new ArrayList<Integer>();
		for (Move each : getMoves()) {
			if (each.getOperation() == null) {
				continue;
			}
			int indexOf = operations.indexOf(each.getOperation());
			if (indexOf == -1) {
				throw new RuntimeException("The custom level " + id
						+ " solution moves contains an invalid operation "
						+ each.getOperation());
			}
			result.add(indexOf);
		}
		return result;
	}

	private OptimizedListener listener;

	public interface OptimizedListener {
		void isOptimized(boolean optimized);
	}

	public void setOptimizedListener(OptimizedListener listener) {
		this.listener = listener;
	}

	public void addOperation(Operation operation) {
		getOperations().add(operation);
		markAsOptimized(false);
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthor() {
		return author;
	}

}
