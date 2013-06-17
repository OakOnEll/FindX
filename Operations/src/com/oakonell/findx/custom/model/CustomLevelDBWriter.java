package com.oakonell.findx.custom.model;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.OperationVisitor;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.Divide;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.Subtract;
import com.oakonell.findx.model.ops.Swap;

public class CustomLevelDBWriter {

    public void write(Context context, CustomLevelBuilder builder) {
        DataBaseHelper helper = new DataBaseHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (builder.getId() == 0) {
            add(builder, db);
        } else {
            // update
            update(builder, db);
        }
        db.close();

    }

    private void update(CustomLevelBuilder builder, SQLiteDatabase db) {
        ContentValues levelInfo = buildMainValues(builder);
        levelInfo.put(BaseColumns._ID, builder.getId());

        String dbId = Long.toString(builder.getId());
        int update = db.update(DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME, levelInfo, BaseColumns._ID + "=?",
                new String[] { dbId });
        if (update != 1) {
            throw new RuntimeException("Record not updated!?");
        }

        // Delete child rows, then re-insert
        db.delete(DataBaseHelper.CUSTOM_LEVEL_OPERATIONS_TABLE_NAME,
                DataBaseHelper.CustomLevelOperationsTable.CUSTOM_LEVEL_ID + "=?",
                new String[] { dbId });
        db.delete(DataBaseHelper.CUSTOM_LEVEL_MOVES_TABLE_NAME,
                DataBaseHelper.CustomLevelMovesTable.CUSTOM_LEVEL_ID + "=?", new String[] { dbId });

        Map<Operation, Long> operationIds = new HashMap<Operation, Long>();
        addOperations(builder, db, builder.getId(), operationIds);
        addMoves(builder, db, builder.getId(), operationIds);
    }

    private void add(CustomLevelBuilder builder, SQLiteDatabase db) {
        ContentValues levelInfo = buildMainValues(builder);

        long id = db.insert(DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME, null, levelInfo);
        builder.setId(id);

        Map<Operation, Long> operationIds = new HashMap<Operation, Long>();
        addOperations(builder, db, id, operationIds);
        addMoves(builder, db, id, operationIds);
    }

    private ContentValues buildMainValues(CustomLevelBuilder builder) {
        ContentValues levelInfo = new ContentValues();

        Move start = builder.getMoves().get(0);
        Equation startEquation = start.getStartEquation();
        levelInfo.put(DataBaseHelper.CustomLevelTable.NAME, builder.getTitle());
        // TODO calculate minimum moves, if possible
        levelInfo.put(DataBaseHelper.CustomLevelTable.MIN_MOVES, builder.getMoves().size() - 1);

        levelInfo.put(DataBaseHelper.CustomLevelTable.LHS_CONST, startEquation.getLhs().getConstant().toString());
        levelInfo.put(DataBaseHelper.CustomLevelTable.LHS_X_COEFF, startEquation.getLhs().getXCoefficient()
                .toString());

        levelInfo.put(DataBaseHelper.CustomLevelTable.RHS_CONST, startEquation.getRhs().getConstant().toString());
        levelInfo.put(DataBaseHelper.CustomLevelTable.RHS_X_COEFF, startEquation.getRhs().getXCoefficient()
                .toString());
        levelInfo.put(DataBaseHelper.CustomLevelTable.IS_OPTIMAL, builder.isOptimized());
        levelInfo.put(DataBaseHelper.CustomLevelTable.IS_IMPORTED, builder.isImported());
        levelInfo.put(DataBaseHelper.CustomLevelTable.AUTHOR, builder.getAuthor());

        levelInfo.put(DataBaseHelper.CustomLevelTable.SOLUTION, builder.getSolution().toString());
        levelInfo.put(DataBaseHelper.CustomLevelTable.SEQ_NUM, builder.getSequence());
        return levelInfo;
    }

    private void addMoves(CustomLevelBuilder builder, SQLiteDatabase db, long id, Map<Operation, Long> operationIds) {
        int j = 0;
        for (Move each : builder.getMoves()) {
            Operation operation = each.getOperation();
            if (operation == null) {
                continue;
            }
            ContentValues opInfo = new ContentValues();
            opInfo.put(DataBaseHelper.CustomLevelMovesTable.CUSTOM_LEVEL_ID, id);
            opInfo.put(DataBaseHelper.CustomLevelMovesTable.SEQ_NUM, j);
            opInfo.put(DataBaseHelper.CustomLevelMovesTable.OPERATION_ID, operationIds.get(operation));
            db.insert(DataBaseHelper.CUSTOM_LEVEL_MOVES_TABLE_NAME, null, opInfo);
            j++;
        }
    }

    private void
            addOperations(CustomLevelBuilder builder, SQLiteDatabase db, long id, Map<Operation, Long> operationIds) {
        int i = 0;
        for (Operation each : builder.getOperations()) {
            final ContentValues opInfo = new ContentValues();
            opInfo.put(DataBaseHelper.CustomLevelOperationsTable.CUSTOM_LEVEL_ID, id);
            opInfo.put(DataBaseHelper.CustomLevelOperationsTable.TYPE, each.type().toString());
            opInfo.put(DataBaseHelper.CustomLevelOperationsTable.SEQ_NUM, i);

            final OperationVisitor visitor = new OperationVisitor() {
                @Override
                public void visitSwap(Swap swap) {
                    // no data for operation row
                }

                @Override
                public void visitAdd(Add add) {
                    // output constant and coeff
                    opInfo.put(DataBaseHelper.CustomLevelOperationsTable.CONST, add.getExpression().getConstant()
                            .toString());
                    opInfo.put(DataBaseHelper.CustomLevelOperationsTable.X_COEFF, add.getExpression()
                            .getXCoefficient().toString());
                }

                @Override
                public void visitSubtract(Subtract sub) {
                    // output constant and coeff
                    opInfo.put(DataBaseHelper.CustomLevelOperationsTable.CONST, sub.getExpression().getConstant()
                            .toString());
                    opInfo.put(DataBaseHelper.CustomLevelOperationsTable.X_COEFF, sub.getExpression()
                            .getXCoefficient().toString());
                }

                @Override
                public void visitMultiply(Multiply multiply) {
                    // output constant and zero coeff
                    opInfo.put(DataBaseHelper.CustomLevelOperationsTable.CONST, multiply.getFactor().toString());
                    opInfo.put(DataBaseHelper.CustomLevelOperationsTable.X_COEFF, "0");
                }

                @Override
                public void visitDivide(Divide divide) {
                    // output constant and zero coeff
                    opInfo.put(DataBaseHelper.CustomLevelOperationsTable.CONST, divide.getFactor().toString());
                    opInfo.put(DataBaseHelper.CustomLevelOperationsTable.X_COEFF, "0");
                }

            };
            each.accept(visitor);

            long opId = db.insert(DataBaseHelper.CUSTOM_LEVEL_OPERATIONS_TABLE_NAME, null, opInfo);
            operationIds.put(each, opId);
            i++;
        }
    }

}
