package com.oakonell.findx.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "findx.db";
	private static final int DATABASE_VERSION = 14;

	public static final String CUSTOM_LEVEL_TABLE_NAME = "custom_level";

	public static class CustomLevelTable {
		public static final String NAME = "name";
		public static final String MIN_MOVES = "min_moves";
		public static final String MAX_MOVES = "max_moves";
		public static final String SEQ_NUM = "sequence";

		// equation columns
		public static final String LHS_X2_COEFF = "lhs_x2_coeff";
		public static final String LHS_X_COEFF = "lhs_x_coeff";
		public static final String LHS_CONST = "lhs_const";

		public static final String RHS_X2_COEFF = "rhs_x2_coeff";
		public static final String RHS_X_COEFF = "rhs_x_coeff";
		public static final String RHS_CONST = "rhs_const";

		// solution
		public static final String SOLUTION = "solution";
		public static final String SOLUTION2 = "solution2";

		public static final String IS_OPTIMAL = "is_optimal";

		public static final String IS_IMPORTED = "is_imported";
		public static final String AUTHOR = "author";
		public static final String SERVER_ID = "server_id";
		public static final String TO_DELETE = "to_delete";

	}

	public static final String CUSTOM_LEVEL_OPERATIONS_TABLE_NAME = "custom_level_operations";

	public static class CustomLevelOperationsTable {
		public static final String CUSTOM_LEVEL_ID = "level_id";
		public static final String TYPE = "type";
		public static final String WILD_TYPE = "wild_type";
		public static final String SEQ_NUM = "sequence";

		// expression columns
		public static final String X2_COEFF = "lhs_x2_coeff";
		public static final String X_COEFF = "lhs_x_coeff";
		public static final String CONST = "lhs_const";
	}

	public static final String CUSTOM_LEVEL_MOVES_TABLE_NAME = "custom_level_moves";

	public static class CustomLevelMovesTable {
		public static final String CUSTOM_LEVEL_ID = "level_id";
		public static final String MOVE_TYPE = "move_type";
		public static final String SEQ_NUM = "sequence";
		public static final String OPERATION_ID = "op_id";
	}

	public static final String LEVEL_PROGRESS_TABLE_NAME = "level_progress";

	public static class LevelProgressTable {
		public static final String LEVEL_ID = "level_id";
		public static final String MIN_MOVES = "min_moves";
		public static final String NUM_UNDOS = "undos_used";
	}

	public static final String CURRENT_LEVEL_STATE_TABLE_NAME = "current_level_state";

	public static class CurrentLevelTable {
		public static final String LEVEL_ID = "level_id";
		public static final String NUM_UNDOS_USED = "num_undos_used";
	}

	public static final String CURRENT_LEVEL_WILD_TABLE_NAME = "current_level_wilds";

	public static class CurrentLevelWildTable {
		public static final String LEVEL_ID = "level_id";
		public static final String INDEX = "op_index";
		public static final String TYPE = "type";
		public static final String X2_COEFF = "x2_coeff";
		public static final String X_COEFF = "x_coeff";
		public static final String CONST = "const";
	}

	public static final String CURRENT_LEVEL_MOVES_TABLE_NAME = "current_level_moves";

	public static class CurrentLevelMovesTable {
		public static final String LEVEL_ID = "level_id";
		public static final String SEQUENCE = "sequence";
		public static final String OP_INDEX = "op_index";
	}

	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		String createTableString = "CREATE TABLE " + CUSTOM_LEVEL_TABLE_NAME
				+ " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CustomLevelTable.NAME + " TEXT, "
				+ CustomLevelTable.MIN_MOVES + " INTEGER, "
				+ CustomLevelTable.MAX_MOVES + " INTEGER, "
				+ CustomLevelTable.SEQ_NUM + " INTEGER, " +

				CustomLevelTable.LHS_X2_COEFF + " TEXT, "
				+ CustomLevelTable.LHS_X_COEFF + " TEXT, "
				+ CustomLevelTable.LHS_CONST + " TEXT, "
				+ CustomLevelTable.RHS_X2_COEFF + " TEXT, "
				+ CustomLevelTable.RHS_X_COEFF + " TEXT, "
				+ CustomLevelTable.RHS_CONST + " TEXT, " +

				CustomLevelTable.SOLUTION + " TEXT, "
				+ CustomLevelTable.SOLUTION2 + " TEXT, " +

				CustomLevelTable.IS_IMPORTED + " INTEGER, "
				+ CustomLevelTable.AUTHOR + " STRING, "
				+ CustomLevelTable.SERVER_ID + " STRING, "
				+ CustomLevelTable.TO_DELETE + " INTEGER, "
				+ CustomLevelTable.IS_OPTIMAL + " INTEGER " + ");";
		sqLiteDatabase.execSQL(createTableString);

		createTableString = "CREATE TABLE "
				+ CUSTOM_LEVEL_OPERATIONS_TABLE_NAME + " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CustomLevelOperationsTable.CUSTOM_LEVEL_ID + " INTEGER, "
				+ CustomLevelOperationsTable.TYPE + " TEXT, "
				+ CustomLevelOperationsTable.WILD_TYPE + " TEXT, "
				+ CustomLevelOperationsTable.SEQ_NUM + " INTEGER, "
				+ CustomLevelOperationsTable.X2_COEFF + " TEXT, "
				+ CustomLevelOperationsTable.X_COEFF + " TEXT, "
				+ CustomLevelOperationsTable.CONST + " TEXT " + ");";
		sqLiteDatabase.execSQL(createTableString);

		createTableString = "CREATE TABLE " + CUSTOM_LEVEL_MOVES_TABLE_NAME
				+ " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CustomLevelMovesTable.CUSTOM_LEVEL_ID + " INTEGER, "
				+ CustomLevelMovesTable.MOVE_TYPE + " INTEGER, "
				+ CustomLevelMovesTable.OPERATION_ID + " INTEGER, "
				+ CustomLevelMovesTable.SEQ_NUM + " INTEGER " + ");";
		sqLiteDatabase.execSQL(createTableString);

		createTableString = "CREATE TABLE " + LEVEL_PROGRESS_TABLE_NAME + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ LevelProgressTable.LEVEL_ID + " TEXT, "
				+ LevelProgressTable.NUM_UNDOS + " INTEGER, "
				+ LevelProgressTable.MIN_MOVES + " INTEGER " + ");";
		sqLiteDatabase.execSQL(createTableString);

		createTableString = "CREATE TABLE " + CURRENT_LEVEL_STATE_TABLE_NAME
				+ " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CurrentLevelTable.LEVEL_ID + " TEXT, "
				+ CurrentLevelTable.NUM_UNDOS_USED + " INTEGER " + ");";
		sqLiteDatabase.execSQL(createTableString);

		createTableString = "CREATE TABLE " + CURRENT_LEVEL_MOVES_TABLE_NAME
				+ " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CurrentLevelMovesTable.LEVEL_ID + " TEXT, "
				+ CurrentLevelMovesTable.SEQUENCE + " INTEGER, "
				+ CurrentLevelMovesTable.OP_INDEX + " INTEGER " + ");";
		sqLiteDatabase.execSQL(createTableString);

		createWildTable(sqLiteDatabase);
	}

	private void createWildTable(SQLiteDatabase sqLiteDatabase) {
		String createTableString;
		createTableString = "CREATE TABLE " + CURRENT_LEVEL_WILD_TABLE_NAME
				+ " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CurrentLevelWildTable.LEVEL_ID + " TEXT, "
				+ CurrentLevelWildTable.INDEX + " INTEGER, "
				+ CurrentLevelWildTable.TYPE + " TEXT, "
				+ CurrentLevelWildTable.X2_COEFF + " TEXT, "
				+ CurrentLevelWildTable.X_COEFF + " TEXT, "
				+ CurrentLevelWildTable.CONST + " TEXT " + ");";
		sqLiteDatabase.execSQL(createTableString);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 7 && newVersion >= 7) {
			db.execSQL("ALTER TABLE " + CUSTOM_LEVEL_TABLE_NAME
					+ " ADD COLUMN " + CustomLevelTable.AUTHOR + " STRING;");
		}
		if (oldVersion < 8 && newVersion >= 8) {
			db.execSQL("ALTER TABLE " + CUSTOM_LEVEL_TABLE_NAME
					+ " ADD COLUMN " + CustomLevelTable.SERVER_ID + " STRING;");
		}
		if (oldVersion < 9 && newVersion >= 9) {
			db.execSQL("ALTER TABLE " + CUSTOM_LEVEL_TABLE_NAME
					+ " ADD COLUMN " + CustomLevelTable.TO_DELETE + " INTEGER;");
		}
		if (oldVersion < 10 && newVersion >= 10) {
			db.execSQL("ALTER TABLE " + CUSTOM_LEVEL_TABLE_NAME
					+ " ADD COLUMN " + CustomLevelTable.LHS_X2_COEFF + " TEXT;");
			db.execSQL("ALTER TABLE " + CUSTOM_LEVEL_TABLE_NAME
					+ " ADD COLUMN " + CustomLevelTable.RHS_X2_COEFF + " TEXT;");

			db.execSQL("ALTER TABLE " + CUSTOM_LEVEL_OPERATIONS_TABLE_NAME
					+ " ADD COLUMN " + CustomLevelOperationsTable.X2_COEFF
					+ " TEXT;");
		}
		if (oldVersion < 11 && newVersion >= 11) {
			db.execSQL("ALTER TABLE " + CUSTOM_LEVEL_OPERATIONS_TABLE_NAME
					+ " ADD COLUMN " + CustomLevelOperationsTable.WILD_TYPE
					+ " TEXT;");
		}
		if (oldVersion < 13 && newVersion >= 13) {
			db.execSQL("DROP TABLE IF EXISTS " + CURRENT_LEVEL_WILD_TABLE_NAME
					+ ";");
			createWildTable(db);
		}

		if (oldVersion < 14 && newVersion >= 14) {
			db.execSQL("ALTER TABLE " + CUSTOM_LEVEL_MOVES_TABLE_NAME
					+ " ADD COLUMN " + CustomLevelMovesTable.MOVE_TYPE
					+ " INTEGER;");
			db.execSQL("UPDATE " + CUSTOM_LEVEL_MOVES_TABLE_NAME + " SET "
					+ CustomLevelMovesTable.MOVE_TYPE + " = 0 ;");
			db.execSQL("ALTER TABLE " + CUSTOM_LEVEL_TABLE_NAME
					+ " ADD COLUMN " + CustomLevelTable.SOLUTION2 + " TEXT;");

		}

		// the operation indexing scheme on solution moves was changed at some
		// point in here...
		if (oldVersion < 14 && newVersion >= 14) {
			updateOpIds(db);
		}
	}

	protected void updateOpIds(SQLiteDatabase db) {
		// CUSTOM_LEVEL_OPERATIONS_TABLE_NAME
		// public static final String CUSTOM_LEVEL_ID = "level_id";
		// public static final String TYPE = "type";
		// public static final String WILD_TYPE = "wild_type";
		// public static final String SEQ_NUM = "sequence";

		// CUSTOM_LEVEL_MOVES_TABLE_NAME ="custom_level_moves";
		// public static class CustomLevelMovesTable {
		// public static final String CUSTOM_LEVEL_ID = "level_id";
		// public static final String MOVE_TYPE = "move_type";
		// public static final String SEQ_NUM = "sequence";
		// public static final String OPERATION_ID = "op_id";
		// }

		// TODO this almost worked misnumbered them, lost the seq=0 entries?
		// Was worried that it may end up seeing a sequence as an id for
		// later updates
		String sql = "update " + CUSTOM_LEVEL_MOVES_TABLE_NAME + " "
				+ " set op_id = (select o."
				+ CustomLevelOperationsTable.SEQ_NUM + " from "
				+ CUSTOM_LEVEL_OPERATIONS_TABLE_NAME + " o where o."
				+ BaseColumns._ID + "  =  "
				+ CustomLevelMovesTable.OPERATION_ID + ")";
		Log.i(DataBaseHelper.class.getName(), sql);
		db.execSQL(sql);

		// in progress, iterative approach
		// Cursor query = db.query(CUSTOM_LEVEL_MOVES_TABLE_NAME,
		// new String[] { BaseColumns._ID,
		// CustomLevelMovesTable.OPERATION_ID }, null, null,
		// null, null, null);
		// try {
		// if (query.moveToFirst()) {
		// while (!query.isAfterLast() ){
		// updateOperationIdsWithSequence(db, query);
		// query.moveToNext();
		// }
		// }
		// } finally {
		// query.close();
		// }

		// This avoids seeing a sequence as an id
		// but didn't work at all- all moves were lost
		// String createTableString = "CREATE TABLE temp_move_op " + " ("
		// + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
		// + " move_id INTEGER, "
		// // + " original_op_id INTEGER, "
		// + " new_op_id INTEGER;";
		// db.execSQL(createTableString);
		// db.execSQL("insert INTO temp_move_op (move_id, new_op_id) "
		// + "select m." + BaseColumns._ID + ",   o."
		// + CustomLevelOperationsTable.SEQ_NUM + " from "
		// + CUSTOM_LEVEL_MOVES_TABLE_NAME + " m, "
		// + CUSTOM_LEVEL_OPERATIONS_TABLE_NAME + " o WHERE m."
		// + CustomLevelMovesTable.OPERATION_ID + " = o."
		// + BaseColumns._ID + ";");
		//
		// db.execSQL("update "
		// + CUSTOM_LEVEL_MOVES_TABLE_NAME
		// + " m "
		// +
		// " set op_id = (select o.new_op_id from temp_move_op  o where o.move_id = m"
		// + BaseColumns._ID + ");");
		//
		// db.execSQL("drop table temp_move_op; ");
	}

	// private void updateOperationIdsWithSequence(SQLiteDatabase db, Cursor
	// query) {
	// String opIdStr = query.getString(1);
	// Cursor op = db.query(CUSTOM_LEVEL_MOVES_TABLE_NAME, new
	// String[]{CustomLevelMovesTable.SEQ_NUM}, BaseColumns._ID + " = ?", new
	// String[]{opIdStr}, null, null, null);
	// if (!op.moveToFirst()) {
	// throw new RuntimeException("Unable to find operation with Id " +
	// opIdStr);
	// }
	// long sequence = op.getLong(0);
	// db.update(table, values, whereClause, whereArgs)
	// }
}
