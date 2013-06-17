package com.oakonell.findx.custom.model;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.oakonell.findx.FindXApp;
import com.oakonell.findx.R;
import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.model.Level;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Stage;

public class CustomStage extends Stage {

    public CustomStage(String id, int titleId) {
        super(id, titleId, R.raw.minuet_in_g_loop, null);
    }

    public void reorderFromTo(CustomLevel movedLevel, CustomLevel myLevel) {
        int myOriginalSequence = myLevel.getSequence();
        DataBaseHelper helper = new DataBaseHelper(FindXApp.getContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        String seqNumName = DataBaseHelper.CustomLevelTable.SEQ_NUM;
        if (myOriginalSequence < movedLevel.getSequence()) {
            // update all the sequences after and including
            // myLevelId +1 up to not including moved level
            db.execSQL("update " + DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME + " set "
                    + seqNumName + "=" + seqNumName + " + 1 " +
                    "where " + seqNumName + " >= " + myOriginalSequence +
                    " and " + seqNumName + " < " + movedLevel.getSequence());
            db.execSQL("update " + DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME + " set "
                    + seqNumName + "=" + myOriginalSequence + " where "
                    + BaseColumns._ID
                    + " = " + movedLevel.getDbId());
        } else {
            // update all the sequences after
            // moved level -1 up to my level
            db.execSQL("update " + DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME + " set "
                    + seqNumName + "=" + seqNumName + " - 1 " +
                    "where " + seqNumName + " > " + movedLevel.getSequence() +
                    " and " + seqNumName + " <= " + myOriginalSequence);
            db.execSQL("update " + DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME + " set "
                    + seqNumName + "=" + myOriginalSequence + " where "
                    + BaseColumns._ID
                    + " = " + movedLevel.getDbId());
        }
        db.close();
        Levels.resetCustomStage();
    }

    public void delete(CustomLevel level) {
        DataBaseHelper helper = new DataBaseHelper(FindXApp.getContext());
        SQLiteDatabase db = helper.getWritableDatabase();

        long id = level.getDbId();
        String dbId = id + "";
        db.delete(DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME, BaseColumns._ID + "=?",
                new String[] { dbId });
        db.delete(DataBaseHelper.CUSTOM_LEVEL_OPERATIONS_TABLE_NAME,
                DataBaseHelper.CustomLevelOperationsTable.CUSTOM_LEVEL_ID + "=?",
                new String[] { dbId });
        db.delete(DataBaseHelper.CUSTOM_LEVEL_MOVES_TABLE_NAME,
                DataBaseHelper.CustomLevelMovesTable.CUSTOM_LEVEL_ID + "=?", new String[] { dbId });

        db.close();

        Levels.resetCustomStage();
    }

    public CustomLevel getLevelByDBId(long id) {
        for (Level each : getLevels()) {
            CustomLevel level = (CustomLevel) each;
            if (level.getDbId() == id) {
                return level;
            }
        }
        return null;
    }

}
