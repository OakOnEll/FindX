package com.oakonell.findx.custom.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionFormat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.IMove;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.MoveResult;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.Operation.OperationType;
import com.oakonell.findx.model.SecondaryEquationMove;
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
import com.oakonell.utils.StringUtils;

public class CustomLevelDBReader {
	private static FractionFormat format = new FractionFormat();

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
		Equation startEquation = readMainValues(query, builder);

		// load the operations, and place in id->op map
		readOperations(db, builder);
		readMoves(db, builder, startEquation);
		// verify minMoves against the number of moves read
		int minMoves = query.getInt(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.MIN_MOVES));
		if (builder.getNumMoves() != minMoves) {
			// throw new RuntimeException("num moves did not match- bulider's "
			// + builder.getNumMoves() + " vs table's " + minMoves);
		}
	}

	private Equation readMainValues(Cursor query, CustomLevelBuilder builder) {
		builder.setId(query.getLong(query.getColumnIndex(BaseColumns._ID)));
		builder.setTitle(query.getString(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.NAME)));

		String solutionString = query.getString(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.SOLUTION));
		builder.setSolution(format.parse(solutionString));
		// deal with secondary solution
		String solution2String = query.getString(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.SOLUTION2));
		if (!StringUtils.isEmpty(solution2String)) {
			Fraction secondarySolution = format.parse(solution2String);
			builder.setSolution2(secondarySolution);
		}

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

		boolean isOptimized = query.getInt(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.IS_OPTIMAL)) > 0;
		builder.markAsOptimized(isOptimized);

		builder.setIsImported(query.getInt(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.IS_IMPORTED)) > 0);
		builder.setAuthor(query.getString(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.AUTHOR)));
		builder.setServerId(query.getString(query
				.getColumnIndex(DataBaseHelper.CustomLevelTable.SERVER_ID)));

		Equation startEquation = new Equation(lhs, rhs);
		return startEquation;
	}

	public static Expression readExpression(Cursor query,
			String x2CoeffColName, String xCoeffColName, String constColName) {
		Fraction x2coeff = readFraction(query, x2CoeffColName);
		Fraction coeff = readFraction(query, xCoeffColName);
		Fraction constVal = readFraction(query, constColName);

		return new Expression(x2coeff, coeff, constVal);
	}

	private void readMoves(SQLiteDatabase db, CustomLevelBuilder builder,
			Equation startEquation) {

		List<Integer> primaryMoveOpIds = readMoveOperationIds(db,
				builder.getId(), "0");
		List<Integer> secondary1MoveOpIds = readMoveOperationIds(db,
				builder.getId(), "1");
		List<Integer> secondary2MoveOpIds = readMoveOperationIds(db,
				builder.getId(), "2");

		populateBuilderMovesFromOperationIndices(builder, startEquation,
				primaryMoveOpIds, secondary1MoveOpIds, secondary2MoveOpIds);
	}

	public static void populateBuilderMovesFromOperationIndices(
			CustomLevelBuilder builder, Equation startEquation,
			List<Integer> primaryMoveOpIds, List<Integer> secondary1MoveOpIds,
			List<Integer> secondary2MoveOpIds) {
		int moveNum = 1;
		Equation equation = startEquation;
		builder.getPrimaryMoves().clear();
		builder.getPrimaryMoves().add(new Move(startEquation, null, 0));
		MoveResult branchResult = null;
		for (Integer id : primaryMoveOpIds) {
			if (branchResult != null) {
				throw new RuntimeException(
						"Shouldn't have any primary moves after a branch");
			}
			Operation operation = builder.getOperations().get(id);
			if (operation == null) {
				throw new RuntimeException("No operation in map with id " + id
						+ ", while reading custom level " + builder.getId());
			}
			MoveResult moveResult = operation
					.applyMove(equation, moveNum, null);
			builder.getPrimaryMoves().add(moveResult.getPrimaryMove());
			if (moveResult.hasMultiple()) {
				branchResult = moveResult;
			} else {
				equation = moveResult.getPrimaryEndEquation();
			}

			moveNum++;
		}
		EquationAndMove eqAndMove = new EquationAndMove();
		if (secondary1MoveOpIds.isEmpty()) {
			if (branchResult != null) {
				throw new RuntimeException("No secondary operators");
			}
			return;
		}

		eqAndMove.equation = branchResult.getSecondary1().getStartEquation();
		eqAndMove.moveNum = moveNum;
		addSecondaryMoves(builder, secondary1MoveOpIds,
				builder.getSecondary1Moves(), eqAndMove, 1);
		eqAndMove.equation = branchResult.getSecondary2().getStartEquation();
		addSecondaryMoves(builder, secondary2MoveOpIds,
				builder.getSecondary2Moves(), eqAndMove, 2);
	}

	private static void addSecondaryMoves(CustomLevelBuilder builder,
			List<Integer> opIds, List<IMove> moves, EquationAndMove eqAndMove,
			int i) {
		moves.add(new SecondaryEquationMove(eqAndMove.equation, i));
		for (Integer id : opIds) {
			Operation operation = builder.getOperations().get(id);
			if (operation == null) {
				throw new RuntimeException("No operation in map with id " + id
						+ ", while reading custom level " + builder.getId());
			}
			// in the secondary moves phase, the original square root has turned
			// into a Negate
			if (operation instanceof SquareRoot) {
				operation = Multiply.NEGATE;
			}
			IMove imove;
			Move move = new Move(eqAndMove.equation, operation,
					eqAndMove.moveNum);
			imove = move;
			eqAndMove.equation = move.getEndEquation();
			eqAndMove.moveNum++;
			moves.add(imove);
		}
	}

	private static class EquationAndMove {
		Equation equation;
		int moveNum;
	}

	private List<Integer> readMoveOperationIds(SQLiteDatabase db, long dbId,
			String moveType) {
		List<Integer> result = new ArrayList<Integer>();
		Cursor opQuery = db.query(DataBaseHelper.CUSTOM_LEVEL_MOVES_TABLE_NAME,
				null, DataBaseHelper.CustomLevelMovesTable.CUSTOM_LEVEL_ID
						+ "=? and "
						+ DataBaseHelper.CustomLevelMovesTable.MOVE_TYPE
						+ " = ?",
				new String[] { Long.toString(dbId), moveType }, null, null,
				DataBaseHelper.CustomLevelMovesTable.SEQ_NUM);

		while (opQuery.moveToNext()) {
			Integer opId = opQuery
					.getInt(opQuery
							.getColumnIndex(DataBaseHelper.CustomLevelMovesTable.OPERATION_ID));
			result.add(opId);
		}
		opQuery.close();
		return result;
	}

	private void readOperations(SQLiteDatabase db, CustomLevelBuilder builder) {
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
			case DEFACTOR:
				op = new Defactor(readExpression(opQuery,
						DataBaseHelper.CustomLevelOperationsTable.X2_COEFF,
						DataBaseHelper.CustomLevelOperationsTable.X_COEFF,
						DataBaseHelper.CustomLevelOperationsTable.CONST));
				break;
				
			case FACTOR:
				op = new Factor(readExpression(opQuery,
						DataBaseHelper.CustomLevelOperationsTable.X2_COEFF,
						DataBaseHelper.CustomLevelOperationsTable.X_COEFF,
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
			builder.addOperation(op);
		}

		opQuery.close();
	}

	public static Fraction readFraction(Cursor query, String constColName) {
		String constString = query
				.getString(query.getColumnIndex(constColName));
		if (constString == null)
			return Fraction.ZERO;
		Fraction constVal = format.parse(constString);
		return constVal;
	}

}
