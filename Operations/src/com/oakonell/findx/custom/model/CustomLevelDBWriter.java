package com.oakonell.findx.custom.model;

import java.util.List;

import org.apache.commons.math3.fraction.Fraction;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.oakonell.findx.FindXApp;
import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.IMove;
import com.oakonell.findx.model.IMoveWithOperation;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.OperationVisitor;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.Defactor;
import com.oakonell.findx.model.ops.Divide;
import com.oakonell.findx.model.ops.Factor;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.Square;
import com.oakonell.findx.model.ops.SquareRoot;
import com.oakonell.findx.model.ops.Subtract;
import com.oakonell.findx.model.ops.Swap;
import com.oakonell.findx.model.ops.WildCard;

public class CustomLevelDBWriter {

	public void write(FindXApp app, Context context,
			CustomLevelBuilder builder) {
		DataBaseHelper helper = new DataBaseHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		Tracker googleTracker = app.getTracker();
		if (builder.getId() == 0) {
			googleTracker.send(new HitBuilders.EventBuilder()
					.setCategory("custom").setAction("add").build());
			add(builder, db);
		} else {
			// update
			googleTracker.send(new HitBuilders.EventBuilder()
					.setCategory("custom").setAction("update").build());
			update(builder, db);
		}
		db.close();

	}

	private void update(CustomLevelBuilder builder, SQLiteDatabase db) {
		ContentValues levelInfo = buildMainValues(builder);
		levelInfo.put(BaseColumns._ID, builder.getId());

		String dbId = Long.toString(builder.getId());
		int update = db.update(DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME,
				levelInfo, BaseColumns._ID + "=?", new String[] { dbId });
		if (update != 1) {
			throw new RuntimeException("Record not updated!?");
		}

		// Delete child rows, then re-insert
		db.delete(DataBaseHelper.CUSTOM_LEVEL_OPERATIONS_TABLE_NAME,
				DataBaseHelper.CustomLevelOperationsTable.CUSTOM_LEVEL_ID
						+ "=?", new String[] { dbId });
		db.delete(DataBaseHelper.CUSTOM_LEVEL_MOVES_TABLE_NAME,
				DataBaseHelper.CustomLevelMovesTable.CUSTOM_LEVEL_ID + "=?",
				new String[] { dbId });

		addOperations(builder, db, builder.getId());
		addMoves(builder, db, builder.getId());
	}

	private void add(CustomLevelBuilder builder, SQLiteDatabase db) {
		ContentValues levelInfo = buildMainValues(builder);

		long id = db.insert(DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME, null,
				levelInfo);
		builder.setId(id);

		addOperations(builder, db, id);
		int movesWritten = addMoves(builder, db, id);
		if (builder.getNumMoves() != movesWritten) {
			throw new RuntimeException(
					"Number of moves do not match: builder says "
							+ builder.getNumMoves() + ", but wrote "
							+ movesWritten);
		}
	}

	private ContentValues buildMainValues(CustomLevelBuilder builder) {
		ContentValues levelInfo = new ContentValues();

		Move start = (Move) builder.getMoves().get(0);
		Equation startEquation = start.getStartEquation();
		levelInfo.put(DataBaseHelper.CustomLevelTable.NAME, builder.getTitle());
		levelInfo.put(DataBaseHelper.CustomLevelTable.MIN_MOVES,
				builder.getNumMoves());

		levelInfo.put(DataBaseHelper.CustomLevelTable.LHS_CONST, startEquation
				.getLhs().getConstant().toString());
		levelInfo.put(DataBaseHelper.CustomLevelTable.LHS_X_COEFF,
				startEquation.getLhs().getXCoefficient().toString());
		levelInfo.put(DataBaseHelper.CustomLevelTable.LHS_X2_COEFF,
				startEquation.getLhs().getX2Coefficient().toString());

		levelInfo.put(DataBaseHelper.CustomLevelTable.RHS_CONST, startEquation
				.getRhs().getConstant().toString());
		levelInfo.put(DataBaseHelper.CustomLevelTable.RHS_X_COEFF,
				startEquation.getRhs().getXCoefficient().toString());
		levelInfo.put(DataBaseHelper.CustomLevelTable.RHS_X2_COEFF,
				startEquation.getRhs().getX2Coefficient().toString());

		levelInfo.put(DataBaseHelper.CustomLevelTable.IS_OPTIMAL,
				builder.isOptimized());
		levelInfo.put(DataBaseHelper.CustomLevelTable.IS_IMPORTED,
				builder.isImported());
		levelInfo.put(DataBaseHelper.CustomLevelTable.AUTHOR,
				builder.getAuthor());
		levelInfo.put(DataBaseHelper.CustomLevelTable.SERVER_ID,
				builder.getServerId());

		levelInfo.put(DataBaseHelper.CustomLevelTable.SOLUTION, builder
				.getSolution().toString());
		// put secondary solution as well
		Fraction secondarySolution = builder.getSecondarySolution();
		levelInfo.put(DataBaseHelper.CustomLevelTable.SOLUTION2,
				secondarySolution != null ? secondarySolution.toString() : "");

		levelInfo.put(DataBaseHelper.CustomLevelTable.SEQ_NUM,
				builder.getSequence());
		return levelInfo;
	}

	private int addMoves(CustomLevelBuilder builder, SQLiteDatabase db, long id) {
		int numMoves = 0;
		numMoves += addMovesOfType(db, id, builder.getOperations(),
				builder.getPrimaryMoves(), 0);
		numMoves += addMovesOfType(db, id, builder.getOperations(),
				builder.getSecondary1Moves(), 1);
		numMoves += addMovesOfType(db, id, builder.getOperations(),
				builder.getSecondary2Moves(), 2);
		return numMoves;
	}

	private int addMovesOfType(SQLiteDatabase db, long id,
			List<Operation> operations, List<IMove> moves, int moveType) {
		int index = 0;
		int numWritten = 0;
		for (IMove iEach : moves) {
			if (!(iEach instanceof IMoveWithOperation)) {
				continue;
			}
			IMoveWithOperation each = (IMoveWithOperation) iEach;
			Operation operation = each.getOperation();
			if (operation == null) {
				continue;
			}
			ContentValues opInfo = new ContentValues();
			opInfo.put(DataBaseHelper.CustomLevelMovesTable.CUSTOM_LEVEL_ID, id);
			opInfo.put(DataBaseHelper.CustomLevelMovesTable.MOVE_TYPE, moveType);
			opInfo.put(DataBaseHelper.CustomLevelMovesTable.SEQ_NUM, index);
			int opIndex = operations.indexOf(operation);
			if (opIndex < 0 && operation == Multiply.NEGATE) {
				opIndex = operations.indexOf(new SquareRoot());
			}
			if (opIndex < 0) {
				throw new RuntimeException("Error finding index for operation "
						+ operation);
			}
			opInfo.put(DataBaseHelper.CustomLevelMovesTable.OPERATION_ID,
					opIndex);
			db.insert(DataBaseHelper.CUSTOM_LEVEL_MOVES_TABLE_NAME, null,
					opInfo);
			numWritten++;
			index++;
		}
		return numWritten;
	}

	private void addOperations(CustomLevelBuilder builder, SQLiteDatabase db,
			long id) {
		int i = 0;
		for (Operation each : builder.getOperations()) {
			final ContentValues opInfo = new ContentValues();
			opInfo.put(
					DataBaseHelper.CustomLevelOperationsTable.CUSTOM_LEVEL_ID,
					id);
			opInfo.put(DataBaseHelper.CustomLevelOperationsTable.TYPE, each
					.type().toString());
			opInfo.put(DataBaseHelper.CustomLevelOperationsTable.SEQ_NUM, i);

			final OperationVisitor visitor = new OperationVisitor() {
				@Override
				public void visitSwap(Swap swap) {
					// no data for operation row
				}

				@Override
				public void visitAdd(Add add) {
					// output constant and coeff
					opInfo.put(
							DataBaseHelper.CustomLevelOperationsTable.X2_COEFF,
							add.getExpression().getX2Coefficient().toString());
					opInfo.put(DataBaseHelper.CustomLevelOperationsTable.CONST,
							add.getExpression().getConstant().toString());
					opInfo.put(
							DataBaseHelper.CustomLevelOperationsTable.X_COEFF,
							add.getExpression().getXCoefficient().toString());
				}

				@Override
				public void visitSubtract(Subtract sub) {
					// output constant and coeff
					opInfo.put(
							DataBaseHelper.CustomLevelOperationsTable.X2_COEFF,
							sub.getExpression().getX2Coefficient().toString());
					opInfo.put(DataBaseHelper.CustomLevelOperationsTable.CONST,
							sub.getExpression().getConstant().toString());
					opInfo.put(
							DataBaseHelper.CustomLevelOperationsTable.X_COEFF,
							sub.getExpression().getXCoefficient().toString());
				}

				@Override
				public void visitMultiply(Multiply multiply) {
					// output constant and zero coeff
					opInfo.put(DataBaseHelper.CustomLevelOperationsTable.CONST,
							multiply.getFactor().toString());
					opInfo.put(
							DataBaseHelper.CustomLevelOperationsTable.X_COEFF,
							"0");
				}

				@Override
				public void visitDivide(Divide divide) {
					// output constant and zero coeff
					opInfo.put(DataBaseHelper.CustomLevelOperationsTable.CONST,
							divide.getFactor().toString());
					opInfo.put(
							DataBaseHelper.CustomLevelOperationsTable.X_COEFF,
							"0");
				}

				@Override
				public void visitFactor(Factor factor) {
					// output constant and coeff
					opInfo.put(
							DataBaseHelper.CustomLevelOperationsTable.X2_COEFF,
							factor.getExpression().getX2Coefficient()
									.toString());
					opInfo.put(DataBaseHelper.CustomLevelOperationsTable.CONST,
							factor.getExpression().getConstant().toString());
					opInfo.put(
							DataBaseHelper.CustomLevelOperationsTable.X_COEFF,
							factor.getExpression().getXCoefficient().toString());
				}

				@Override
				public void visitDefactor(Defactor defactor) {
					// output constant and coeff
					opInfo.put(
							DataBaseHelper.CustomLevelOperationsTable.X2_COEFF,
							defactor.getExpression().getX2Coefficient()
									.toString());
					opInfo.put(DataBaseHelper.CustomLevelOperationsTable.CONST,
							defactor.getExpression().getConstant().toString());
					opInfo.put(
							DataBaseHelper.CustomLevelOperationsTable.X_COEFF,
							defactor.getExpression().getXCoefficient()
									.toString());
				}

				@Override
				public void visitSquare(Square square) {
					// no data
				}

				@Override
				public void visitSquareRoot(SquareRoot squareRoot) {
					// no data
				}

				@Override
				public void visitWild(WildCard wild) {
					opInfo.put(
							DataBaseHelper.CustomLevelOperationsTable.WILD_TYPE,
							wild.getActual().type().toString());
					wild.getActual().accept(this);
				}
			};
			each.accept(visitor);

			long opId = db.insert(
					DataBaseHelper.CUSTOM_LEVEL_OPERATIONS_TABLE_NAME, null,
					opInfo);
			i++;
		}
	}

}
