package com.oakonell.findx.custom.model;

import java.util.List;

import org.apache.commons.math3.fraction.Fraction;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.oakonell.findx.FindXApp;
import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.data.DataBaseHelper.CustomLevelTable;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.ILevel;
import com.oakonell.findx.model.Level.LevelSolution;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.Stage;
import com.oakonell.utils.StringUtils;

public class CustomLevelProxy implements ICustomLevel {
	private CustomLevel level;

	private final Stage stage;
	private final long dbId;
	private final String name;
	private final String hyphenId;
	private final boolean isImported;
	private final String author;
	private final int sequence;
	private String serverId;

	public CustomLevelProxy(CustomStage stage, String id, long dbId,
			String name, boolean isImported, String author, String serverId,
			int sequence) {
		this.dbId = dbId;
		this.stage = stage;
		this.name = name;
		this.hyphenId = id;
		this.isImported = isImported;
		this.author = author;
		this.serverId = serverId;
		this.sequence = sequence;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getId() {
		return hyphenId;
	}

	@Override
	public Stage getStage() {
		return stage;
	}

	@Override
	public ILevel getPreviousLevel() {
		return getLevel().getPreviousLevel();
	}

	@Override
	public ILevel getNextLevel() {
		return getLevel().getNextLevel();
	}

	@Override
	public String getMultilineDescription() {
		return getLevel().getMultilineDescription();
	}

	@Override
	public boolean isUnlocked() {
		return true;
	}

	@Override
	public int getMinMoves() {
		// TODO this causes a fault
		return getLevel().getMinMoves();
	}

	// --- Delegation methods

	private CustomLevel getLevel() {
		if (level == null) {
			DataBaseHelper helper = new DataBaseHelper(FindXApp.getContext());
			SQLiteDatabase db = helper.getReadableDatabase();

			Cursor query = db.query(DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME,
					null, BaseColumns._ID + " = ?", new String[] { dbId + "" },
					null, null, CustomLevelTable.SEQ_NUM);
			if (!query.moveToNext()) {
				return null;
			}
			CustomLevelDBReader reader = new CustomLevelDBReader();
			CustomLevelBuilder builder = reader.readFromCursor(db, query);
			level = new CustomLevel(builder, stage);
		}
		return level;
	}

	@Override
	public LevelSolution getLevelSolution() {
		return getLevel().getLevelSolution();
	}

	@Override
	public List<Fraction> getSolutions() {
		return getLevel().getSolutions();
	}

	@Override
	public void possibilyUpdateRating(int moves, int undosUsed) {
		getLevel().possibilyUpdateRating(moves, undosUsed);
	}

	@Override
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

	@Override
	public List<Operation> getOperations() {
		return getLevel().getOperations();
	}

	@Override
	public Equation getEquation() {
		return getLevel().getEquation();
	}

	@Override
	public void setServerId(String id) {
		if (level != null) {
			getLevel().setServerId(id);
		}
		serverId = id;
	}

	// ----------
	@Override
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

	private Cursor getExistingRatingRow(SQLiteDatabase db) {
		Cursor query = db.query(DataBaseHelper.LEVEL_PROGRESS_TABLE_NAME, null,
				DataBaseHelper.LevelProgressTable.LEVEL_ID + "=?",
				new String[] { dbId + "" }, null, null, null);
		return query;
	}

	public boolean savedToServer() {
		return !StringUtils.isEmpty(serverId);
	}

	@Override
	public String getServerId() {
		return serverId;
	}

	@Override
	public int getSequence() {
		return sequence;
	}

	@Override
	public String getAuthor() {
		return author;
	}

	@Override
	public boolean isImported() {
		return isImported;
	}

	@Override
	public long getDbId() {
		return dbId;
	}

}
