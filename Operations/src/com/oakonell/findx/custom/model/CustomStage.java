package com.oakonell.findx.custom.model;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.oakonell.findx.FindXApp;
import com.oakonell.findx.R;
import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.model.ILevel;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Stage;

public class CustomStage extends Stage {

	public CustomStage(String id, int titleId) {
		super(id, titleId, R.raw.minuet_in_g_loop, null);
	}

	public void reorderFromTo(ICustomLevel movedLevel, ICustomLevel myLevel) {
		int myOriginalSequence = myLevel.getSequence();
		DataBaseHelper helper = new DataBaseHelper(FindXApp.getContext());
		SQLiteDatabase db = helper.getWritableDatabase();
		String seqNumName = DataBaseHelper.CustomLevelTable.SEQ_NUM;
		if (myOriginalSequence < movedLevel.getSequence()) {
			// update all the sequences after and including
			// myLevelId +1 up to not including moved level
			db.execSQL("update " + DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME
					+ " set " + seqNumName + "=" + seqNumName + " + 1 "
					+ "where " + seqNumName + " >= " + myOriginalSequence
					+ " and " + seqNumName + " < " + movedLevel.getSequence());
			db.execSQL("update " + DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME
					+ " set " + seqNumName + "=" + myOriginalSequence
					+ " where " + BaseColumns._ID + " = "
					+ movedLevel.getDbId());
		} else {
			// update all the sequences after
			// moved level -1 up to my level
			db.execSQL("update " + DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME
					+ " set " + seqNumName + "=" + seqNumName + " - 1 "
					+ "where " + seqNumName + " > " + movedLevel.getSequence()
					+ " and " + seqNumName + " <= " + myOriginalSequence);
			db.execSQL("update " + DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME
					+ " set " + seqNumName + "=" + myOriginalSequence
					+ " where " + BaseColumns._ID + " = "
					+ movedLevel.getDbId());
		}
		db.close();
		Levels.resetCustomStage();
	}

	public void delete(ICustomLevel level) {
		long id = level.getDbId();

		deleteLevelById(id);

		Levels.resetCustomStage();
	}

	public static void deleteLevelById(long id) {
		DataBaseHelper helper = new DataBaseHelper(FindXApp.getContext());
		SQLiteDatabase db = helper.getWritableDatabase();

		String dbId = id + "";
		db.delete(DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME, BaseColumns._ID
				+ "=?", new String[] { dbId });
		db.delete(DataBaseHelper.CUSTOM_LEVEL_OPERATIONS_TABLE_NAME,
				DataBaseHelper.CustomLevelOperationsTable.CUSTOM_LEVEL_ID
						+ "=?", new String[] { dbId });
		db.delete(DataBaseHelper.CUSTOM_LEVEL_MOVES_TABLE_NAME,
				DataBaseHelper.CustomLevelMovesTable.CUSTOM_LEVEL_ID + "=?",
				new String[] { dbId });

		db.close();
	}

	public ICustomLevel getLevelByDBId(long id) {
		for (ILevel each : getLevels()) {
			ICustomLevel level = (ICustomLevel) each;
			if (level.getDbId() == id) {
				return level;
			}
		}
		return null;
	}

}
