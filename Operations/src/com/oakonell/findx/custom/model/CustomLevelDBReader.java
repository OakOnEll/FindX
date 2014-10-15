package com.oakonell.findx.custom.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionFormat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.Operation.OperationType;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.Divide;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.Square;
import com.oakonell.findx.model.ops.SquareRoot;
import com.oakonell.findx.model.ops.Subtract;
import com.oakonell.findx.model.ops.Swap;
import com.oakonell.findx.model.ops.WildCard;

public class CustomLevelDBReader {
	private FractionFormat format = new FractionFormat();

	public long findDbIdByServerId(Context context, String parseLevelId) {
		DataBaseHelper helper = new DataBaseHelper(context);
		Cursor query = null;
		SQLiteDatabase db = null;
		try {
			db = helper.getReadableDatabase();

			query = db.query(DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME, null,
					DataBaseHelper.CustomLevelTable.SERVER_ID + "=?",
					new String[] { parseLevelId }, null, null, null);
			if (!query.moveToFirst()) {
				return -1;
			}
			return query.getLong(query.getColumnIndex(BaseColumns._ID));
		} finally {
			if (query != null)
				query.close();
			if (db != null)
				db.close();
		}

	}

	public void read(Context context, CustomLevelBuilder builder, long id) {
		DataBaseHelper helper = new DataBaseHelper(context);
		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor query = db.query(DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME, null,
				BaseColumns._ID + "=?", new String[] { Long.toString(id) },
				null, null, null);
		if (!query.moveToFirst()) {
			query.close();
			db.close();
			throw new RuntimeException("Level with id=" + id + " was not found");
		}
		readFromCursor(db, query, builder);

		query.close();
		db.close();
	}

	public CustomLevelBuilder readFromCursor(SQLiteDatabase db, Cursor query) {
		CustomLevelBuilder builder = new CustomLevelBuilder();
		readFromCursor(db, query, builder);
		return builder;
	}

	private void readFromCursor(SQLiteDatabase db, Cursor query,
			CustomLevelBuilder builder) {
		builder.setId(query.getLong(query.getColumnIndex(BaseColumns._ID)));
		builder.setTitle(query.getString(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.NAME)));
		String solutionString = query.getString(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.SOLUTION));
		builder.setSolution(format.parse(solutionString));
		builder.setSequence(query.getInt(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.SEQ_NUM)));

		Expression lhs = readExpression(query,
				DataBaseHelper.CustomLevelTable.LHS_X2_COEFF,
				DataBaseHelper.CustomLevelTable.LHS_X_COEFF,
				DataBaseHelper.CustomLevelTable.LHS_CONST);
		Expression rhs = readExpression(query,
				DataBaseHelper.CustomLevelTable.RHS_X2_COEFF,
				DataBaseHelper.CustomLevelTable.RHS_X_COEFF,
				DataBaseHelper.CustomLevelTable.RHS_CONST);
		Equation startEquation = new Equation(lhs, rhs);

		// load the operations, and place in id->op map
		Map<Long, Operation> operationsById = new HashMap<Long, Operation>();
		readOperations(db, builder, operationsById);
		readMoves(db, builder, operationsById, startEquation);

		boolean isOptimized = query.getInt(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.IS_OPTIMAL)) > 0;
		builder.markAsOptimized(isOptimized);

		builder.setIsImported(query.getInt(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.IS_IMPORTED)) > 0);
		builder.setAuthor(query.getString(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.AUTHOR)));
		builder.setServerId(query.getString(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.SERVER_ID)));
	}

	private Expression readExpression(Cursor query, String x2CoeffColName,
			String xCoeffColName, String constColName) {
		Fraction x2coeff = readFraction(query, x2CoeffColName);
		Fraction coeff = readFraction(query, xCoeffColName);
		Fraction constVal = readFraction(query, constColName);

		return new Expression(x2coeff, coeff, constVal);
	}

	private void readMoves(SQLiteDatabase db, CustomLevelBuilder builder,
			Map<Long, Operation> operationsById, Equation startEquation) {
		Cursor opQuery = db.query(DataBaseHelper.CUSTOM_LEVEL_MOVES_TABLE_NAME,
				null, DataBaseHelper.CustomLevelMovesTable.CUSTOM_LEVEL_ID
						+ "=?",
				new String[] { Long.toString(builder.getId()) }, null, null,
				DataBaseHelper.CustomLevelMovesTable.SEQ_NUM);

		Equation eq = startEquation;
		List<Move> moves = builder.getMoves();
		moves.clear();

		moves.add(new Move(eq, null));
		while (opQuery.moveToNext()) {
			Long opId = opQuery
					.getLong(opQuery
							.getColumnIndex(DataBaseHelper.CustomLevelMovesTable.OPERATION_ID));
			Operation operation = operationsById.get(opId);
			if (operation == null) {
				throw new RuntimeException("No operation in map with id "
						+ opId + ", while reading custom level "
						+ builder.getId());
			}
			Move move = new Move(eq, operation);
			moves.add(move);
			eq = move.getEndEquation();
		}
		// add an extra null move?

		opQuery.close();

	}

	private void readOperations(SQLiteDatabase db, CustomLevelBuilder builder,
			Map<Long, Operation> operationsById) {
		Cursor opQuery = db.query(
				DataBaseHelper.CUSTOM_LEVEL_OPERATIONS_TABLE_NAME, null,
				DataBaseHelper.CustomLevelOperationsTable.CUSTOM_LEVEL_ID
						+ "=?",
				new String[] { Long.toString(builder.getId()) }, null, null,
				DataBaseHelper.CustomLevelOperationsTable.SEQ_NUM);

		while (opQuery.moveToNext()) {
			String typeString = opQuery
					.getString(opQuery
							.getColumnIndex(DataBaseHelper.CustomLevelOperationsTable.TYPE));
			Long id = opQuery.getLong(opQuery.getColumnIndex(BaseColumns._ID));
			OperationType type = OperationType.valueOf(typeString);
			WildCard wildOp = null;
			if (type == OperationType.WILD) {
				wildOp = new WildCard();
				typeString = opQuery
						.getString(opQuery
								.getColumnIndex(DataBaseHelper.CustomLevelOperationsTable.WILD_TYPE));
				type = OperationType.valueOf(typeString);
			}
			Operation op;
			switch (type) {
			case ADD:
				op = new Add(readExpression(opQuery,
						DataBaseHelper.CustomLevelOperationsTable.X2_COEFF,
						DataBaseHelper.CustomLevelOperationsTable.X_COEFF,
						DataBaseHelper.CustomLevelOperationsTable.CONST));
				break;
			case SUBTRACT:
				op = new Subtract(readExpression(opQuery,
						DataBaseHelper.CustomLevelOperationsTable.X2_COEFF,
						DataBaseHelper.CustomLevelOperationsTable.X_COEFF,
						DataBaseHelper.CustomLevelOperationsTable.CONST));
				break;
			case MULTIPLY:
				op = new Multiply(readFraction(opQuery,
						DataBaseHelper.CustomLevelOperationsTable.CONST));
				break;
			case DIVIDE:
				op = new Divide(readFraction(opQuery,
						DataBaseHelper.CustomLevelOperationsTable.CONST));
				break;
			case SQUARE:
				op = new Square();
				break;
			case SQUARE_ROOT:
				op = new SquareRoot();
				break;
			case SWAP:
				op = new Swap();
				break;
			default:
				throw new RuntimeException("Unknown type " + type
						+ " while reading custom level " + builder.getId());
			}
			if (wildOp != null) {
				wildOp.setActual(op);
				op = wildOp;
			}
			operationsById.put(id, op);
			builder.addOperation(op);
		}

		opQuery.close();
	}

	private Fraction readFraction(Cursor query, String constColName) {
		String constString = query
				.getString(query.getColumnIndex(constColName));
		if (constString == null)
			return Fraction.ZERO;
		Fraction constVal = format.parse(constString);
		return constVal;
	}

}
