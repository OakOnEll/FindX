package com.oakonell.findx.custom.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.fraction.Fraction;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.oakonell.findx.FindXApp;
import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.IMove;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.MultipleSolutionMove;
import com.oakonell.findx.model.Operation;

public abstract class TempCorrectLevelBuilder {
	// the DB id
	private long id;

	private boolean isOptimized = true;
	private boolean isImported;
	private String author;
	private String serverId;

	protected List<Operation> operations = new ArrayList<Operation>();

	protected Fraction solution;

	private String title;
	private int sequence;

	protected Fraction secondarySolution;
	protected List<IMove> primaryMoves = new ArrayList<IMove>();
	protected List<IMove> secondary1Moves = new ArrayList<IMove>();
	protected List<IMove> secondary2Moves = new ArrayList<IMove>();
	// a "live" updated combined list of the moves, typically for UI
	// presentation
	protected List<IMove> combinedMoves = new CombinedList<IMove>(primaryMoves,
			secondary1Moves, secondary2Moves);

	public TempCorrectLevelBuilder() {
	}

	public boolean hasMoves() {
		return primaryMoves.size() > 1;
	}

	public List<IMove> getMoves() {
		return combinedMoves;
	}

	protected abstract void setSolution(Fraction one);

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Fraction getSolution() {
		return solution;
	}

	public int getNumMoves() {
		if (secondarySolution == null) {
			return primaryMoves.size() - 1;
		}
		return primaryMoves.size() - 3 + secondary1Moves.size()
				+ secondary2Moves.size();
	}

	public void markAsOptimized(boolean isOptimized2) {
		isOptimized = isOptimized2;
		if (listener != null) {
			listener.isOptimized(isOptimized);
		}
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

	public void setServerId(String id) {
		serverId = id;
	}

	public String getServerId() {
		return serverId;
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

	private OptimizedListener listener;

	public interface OptimizedListener {
		void isOptimized(boolean optimized);
	}

	public void setOptimizedListener(OptimizedListener listener) {
		this.listener = listener;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public void prepareAsCopy() {
		id = 0;
		serverId = null;
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

	public boolean canMoveUp(IMove item) {
		if (item instanceof MultipleSolutionMove)
			return false;
		int index = primaryMoves.indexOf(item);
		return index > 1;
	}

	public boolean canMoveDown(IMove item) {
		int index = primaryMoves.indexOf(item);
		if (index == 0)
			return false;
		if (index == primaryMoves.size() - 2
				&& primaryMoves.get(primaryMoves.size() - 1) instanceof MultipleSolutionMove)
			return false;
		return index >= 0 && index < primaryMoves.size() - 1;
	}

	public Equation getCurrentStartEquation() {
		return ((Move) primaryMoves.get(0)).getStartEquation();
	}

	public Fraction getSecondarySolution() {
		return secondarySolution;
	}

	public boolean canSetSolution() {
		// Can't change solution once a multiple solution branch occurred
		if (secondarySolution != null) {
			return false;
		}
		return true;
	}

	public boolean canDeleteMove(IMove item) {
		if (!(item instanceof Move)) {
			return false;
		}
		return primaryMoves.contains(item);
	}

	public boolean canEditMove(IMove item) {
		if (!(item instanceof Move)) {
			return false;
		}
		return primaryMoves.contains(item);
	}

	public void moveUp(IMove move) {
		Move item = (Move) move;
		int index = primaryMoves.indexOf(item);

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
		Move first = (Move) toReapply.remove(0);
		apply(first.getOperation(), true);
		apply(item.getOperation(), true);
		for (IMove iEach : toReapply) {
			Move each = (Move) iEach;
			apply(each.getOperation(), true);
		}
	}

	public void moveDown(IMove move) {
		Move item = (Move) move;
		int index = primaryMoves.indexOf(item);
		// the sublist includes ONE more move past the one being moved down
		List<IMove> subList = primaryMoves.subList(1, index + 2);
		List<IMove> toReapply = new ArrayList<IMove>(subList.subList(0,
				subList.size()));
		Move next = (Move) subList.get(subList.size() - 1);
		int numDeleted = subList.size();
		subList.clear();
		primaryMoves.remove(0);
		for (int i = 0; i < numDeleted; i++) {
			decrementMoveNumbers();
		}
		primaryMoves.add(0, new Move(next.getEndEquation(), null, 0));

		Collections.reverse(toReapply);
		toReapply.remove(item);
		apply(item.getOperation(), true);
		for (IMove iEach : toReapply) {
			Move each = (Move) iEach;
			apply(each.getOperation(), true);
		}
	}

	abstract protected void apply(Operation operation, boolean b);

	protected void incrementMoveNumbers() {
		for (IMove each : primaryMoves) {
			each.incrementMoveNum();
		}
		for (IMove each : secondary1Moves) {
			each.incrementMoveNum();
		}
		for (IMove each : secondary2Moves) {
			each.incrementMoveNum();
		}
	}

	protected void decrementMoveNumbers() {
		for (IMove each : primaryMoves) {
			each.decrementMoveNum();
		}
		for (IMove each : secondary1Moves) {
			each.decrementMoveNum();
		}
		for (IMove each : secondary2Moves) {
			each.decrementMoveNum();
		}
	}

}
