package com.oakonell.findx.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DataBaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "findx.db";
	private static final int DATABASE_VERSION = 9;

	public static final String CUSTOM_LEVEL_TABLE_NAME = "custom_level";

	public static class CustomLevelTable {
		public static final String NAME = "name";
		public static final String MIN_MOVES = "min_moves";
		public static final String MAX_MOVES = "max_moves";
		public static final String SEQ_NUM = "sequence";

		// equation columns
		public static final String LHS_X_COEFF = "lhs_x_coeff";
		public static final String LHS_CONST = "lhs_const";

		public static final String RHS_X_COEFF = "rhs_x_coeff";
		public static final String RHS_CONST = "rhs_const";

		// solution
		public static final String SOLUTION = "solution";

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
		public static final String SEQ_NUM = "sequence";

		// expression columns
		public static final String X_COEFF = "lhs_x_coeff";
		public static final String CONST = "lhs_const";
	}

	public static final String CUSTOM_LEVEL_MOVES_TABLE_NAME = "custom_level_moves";

	public static class CustomLevelMovesTable {
		public static final String CUSTOM_LEVEL_ID = "level_id";
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

				CustomLevelTable.LHS_X_COEFF + " TEXT, "
				+ CustomLevelTable.LHS_CONST + " TEXT, "
				+ CustomLevelTable.RHS_X_COEFF + " TEXT, "
				+ CustomLevelTable.RHS_CONST + " TEXT, " +

				CustomLevelTable.SOLUTION + " TEXT, " +

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
				+ CustomLevelOperationsTable.SEQ_NUM + " INTEGER, "
				+ CustomLevelOperationsTable.X_COEFF + " TEXT, "
				+ CustomLevelOperationsTable.CONST + " TEXT " + ");";
		sqLiteDatabase.execSQL(createTableString);

		createTableString = "CREATE TABLE " + CUSTOM_LEVEL_MOVES_TABLE_NAME
				+ " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CustomLevelMovesTable.CUSTOM_LEVEL_ID + " INTEGER, "
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

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 9 && newVersion >= 9) {
			db.execSQL("ALTER TABLE " + CUSTOM_LEVEL_TABLE_NAME
					+ " ADD COLUMN " + CustomLevelTable.TO_DELETE + " INTEGER;");
		}
		if (oldVersion < 8 && newVersion >= 8) {
			db.execSQL("ALTER TABLE " + CUSTOM_LEVEL_TABLE_NAME
					+ " ADD COLUMN " + CustomLevelTable.SERVER_ID + " STRING;");
		}
		if (oldVersion < 7 && newVersion >= 7) {
			db.execSQL("ALTER TABLE " + CUSTOM_LEVEL_TABLE_NAME
					+ " ADD COLUMN " + CustomLevelTable.AUTHOR + " STRING;");
		}
		// db.execSQL("DROP TABLE IF EXISTS " + CURRENT_LEVEL_STATE_TABLE_NAME +
		// ";");
		// db.execSQL("DROP TABLE IF EXISTS " + CURRENT_LEVEL_MOVES_TABLE_NAME +
		// ";");
		// db.execSQL("DROP TABLE IF EXISTS " +
		// CUSTOM_LEVEL_OPERATIONS_TABLE_NAME + ";");
		// db.execSQL("DROP TABLE IF EXISTS " + CUSTOM_LEVEL_TABLE_NAME + ";");
		//
		// db.execSQL("DROP TABLE IF EXISTS " + LEVEL_PROGRESS_TABLE_NAME +
		// ";");
		// onCreate(db);
	}

}
