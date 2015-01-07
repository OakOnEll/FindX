package com.oakonell.findx.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.data.DataBaseHelper.CurrentLevelMovesTable;
import com.oakonell.findx.data.DataBaseHelper.CustomLevelMovesTable;

public class DbUpgradeTest extends AndroidTestCase {
	private DataBaseHelper dbHelper;

	public void setUp() {
		RenamingDelegatingContext context = new RenamingDelegatingContext(
				getContext(), "test_");
		dbHelper = new DataBaseHelper(context);
	}

	public void testDB() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		addSimpleLevel(db);
		addComplexLevel(db);

		displayMoves(db);

		dbHelper.updateOpIds(db);
		Log.i("DB upgrade test", "After upgrade");

		displayMoves(db);

	}

	private void addSimpleLevel(SQLiteDatabase db) {
		ContentValues level = new ContentValues();
		level.put(DataBaseHelper.CustomLevelTable.AUTHOR, "Rob");
		level.put(DataBaseHelper.CustomLevelTable.LHS_CONST, -1);
		level.put(DataBaseHelper.CustomLevelTable.LHS_X_COEFF, 1);
		level.put(DataBaseHelper.CustomLevelTable.MAX_MOVES, 10);
		level.put(DataBaseHelper.CustomLevelTable.MIN_MOVES, 2);
		level.put(DataBaseHelper.CustomLevelTable.NAME, "Test");
		level.put(DataBaseHelper.CustomLevelTable.RHS_CONST, 0);
		level.put(DataBaseHelper.CustomLevelTable.SOLUTION, 1);
		long levelId = db.insert(DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME, null,
				level);
		assertFalse(levelId < 0);

		ContentValues op = new ContentValues();
		op.put(DataBaseHelper.CustomLevelOperationsTable.SEQ_NUM, 1);
		op.put(DataBaseHelper.CustomLevelOperationsTable.CONST, 1);
		op.put(DataBaseHelper.CustomLevelOperationsTable.CUSTOM_LEVEL_ID,
				levelId);
		op.put(DataBaseHelper.CustomLevelOperationsTable.TYPE, "ADD");
		long opId = db.insert(
				DataBaseHelper.CUSTOM_LEVEL_OPERATIONS_TABLE_NAME, null, op);
		assertFalse(opId < 0);

		ContentValues move = new ContentValues();
		move.put(DataBaseHelper.CustomLevelMovesTable.CUSTOM_LEVEL_ID, levelId);
		move.put(DataBaseHelper.CustomLevelMovesTable.SEQ_NUM, 1);
		move.put(DataBaseHelper.CustomLevelMovesTable.OPERATION_ID, opId);
		move.put(DataBaseHelper.CustomLevelMovesTable.MOVE_TYPE, 0);
		long moveId = db.insert(DataBaseHelper.CUSTOM_LEVEL_MOVES_TABLE_NAME,
				null, move);
		assertFalse(moveId < 0);
	}

	private void addComplexLevel(SQLiteDatabase db) {
		ContentValues level = new ContentValues();
		level.put(DataBaseHelper.CustomLevelTable.AUTHOR, "Rob");
		level.put(DataBaseHelper.CustomLevelTable.LHS_CONST, -1);
		level.put(DataBaseHelper.CustomLevelTable.LHS_X_COEFF, 1);
		level.put(DataBaseHelper.CustomLevelTable.MAX_MOVES, 10);
		level.put(DataBaseHelper.CustomLevelTable.MIN_MOVES, 2);
		level.put(DataBaseHelper.CustomLevelTable.NAME, "Test");
		level.put(DataBaseHelper.CustomLevelTable.RHS_CONST, 0);
		level.put(DataBaseHelper.CustomLevelTable.SOLUTION, 1);
		long levelId = db.insert(DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME, null,
				level);
		assertFalse(levelId < 0);

		ContentValues op = new ContentValues();
		op.put(DataBaseHelper.CustomLevelOperationsTable.SEQ_NUM, 1);
		op.put(DataBaseHelper.CustomLevelOperationsTable.CONST, 1);
		op.put(DataBaseHelper.CustomLevelOperationsTable.CUSTOM_LEVEL_ID,
				levelId);
		op.put(DataBaseHelper.CustomLevelOperationsTable.TYPE, "ADD");
		long opId = db.insert(
				DataBaseHelper.CUSTOM_LEVEL_OPERATIONS_TABLE_NAME, null, op);
		assertFalse(opId < 0);

		op = new ContentValues();
		op.put(DataBaseHelper.CustomLevelOperationsTable.SEQ_NUM, 2);
		op.put(DataBaseHelper.CustomLevelOperationsTable.CONST, 3);
		op.put(DataBaseHelper.CustomLevelOperationsTable.CUSTOM_LEVEL_ID,
				levelId);
		op.put(DataBaseHelper.CustomLevelOperationsTable.TYPE, "ADD");
		long opId2 = db.insert(
				DataBaseHelper.CUSTOM_LEVEL_OPERATIONS_TABLE_NAME, null, op);
		assertFalse(opId < 0);

		ContentValues move = new ContentValues();
		move.put(DataBaseHelper.CustomLevelMovesTable.CUSTOM_LEVEL_ID, levelId);
		move.put(DataBaseHelper.CustomLevelMovesTable.SEQ_NUM, 1);
		move.put(DataBaseHelper.CustomLevelMovesTable.OPERATION_ID, opId);
		move.put(DataBaseHelper.CustomLevelMovesTable.MOVE_TYPE, 0);
		long moveId = db.insert(DataBaseHelper.CUSTOM_LEVEL_MOVES_TABLE_NAME,
				null, move);
		assertFalse(moveId < 0);

		move = new ContentValues();
		move.put(DataBaseHelper.CustomLevelMovesTable.CUSTOM_LEVEL_ID, levelId);
		move.put(DataBaseHelper.CustomLevelMovesTable.SEQ_NUM, 2);
		move.put(DataBaseHelper.CustomLevelMovesTable.OPERATION_ID, opId2);
		move.put(DataBaseHelper.CustomLevelMovesTable.MOVE_TYPE, 0);
		moveId = db.insert(DataBaseHelper.CUSTOM_LEVEL_MOVES_TABLE_NAME, null,
				move);
		assertFalse(moveId < 0);

	}

	private void displayMoves(SQLiteDatabase db) {
		Cursor query = db.query(DataBaseHelper.CUSTOM_LEVEL_MOVES_TABLE_NAME,
				null, null, null, null, null,
				CustomLevelMovesTable.CUSTOM_LEVEL_ID + ", "
						+ CustomLevelMovesTable.SEQ_NUM);
		query.moveToFirst();
		while (!query.isAfterLast()) {
			Log.i("DB upgrade test",
					query.getString(query
							.getColumnIndex(DataBaseHelper.CustomLevelMovesTable.CUSTOM_LEVEL_ID))
							+ ", "
							+ query.getString(query
									.getColumnIndex(DataBaseHelper.CustomLevelMovesTable.OPERATION_ID))
							+ ", "
							+ query.getString(query
									.getColumnIndex(DataBaseHelper.CustomLevelMovesTable.SEQ_NUM)));
			query.moveToNext();
		}
		query.close();
	}

	public void tearDown() throws Exception {
		dbHelper.close();
		super.tearDown();
	}
}
