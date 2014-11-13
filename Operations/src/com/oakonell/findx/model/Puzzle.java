package com.oakonell.findx.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.fraction.Fraction;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.oakonell.findx.custom.model.CustomLevelDBReader;
import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.model.Operation.OperationType;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.Divide;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.Square;
import com.oakonell.findx.model.ops.SquareRoot;
import com.oakonell.findx.model.ops.Subtract;
import com.oakonell.findx.model.ops.Swap;
import com.oakonell.findx.model.ops.WildCard;

public class Puzzle {
	private Level level;
	private int numUndosLeft;
	private List<IMove> moves = new ArrayList<IMove>();
	private List<Operation> operations;

	private IMove moveInWaiting;
	private Equation currentEquation;
	private int numMoves = 0;
	private int squareRootOpIndex = -1;

	public Puzzle(String puzzleId) {
		level = Levels.get(puzzleId);
		if (level == null) {
			throw new IllegalArgumentException("No level with id=" + puzzleId);
		}
		operations = new ArrayList<Operation>(level.getOperations());
		numUndosLeft = 2;
		currentEquation = level.getEquation();
	}

	private void initialize(String puzzleId, List<Integer> opIndices,
			int numUndosUsed, Map<Integer, Operation> builtWilds) {
		level = Levels.get(puzzleId);
		currentEquation = level.getEquation();
		moveInWaiting = null;
		numMoves = 0;
		squareRootOpIndex = -1;
		moves.clear();
		List<Operation> operations = new ArrayList<Operation>(
				level.getOperations());

		// restore the set wild operations
		for (Entry<Integer, Operation> entry : builtWilds.entrySet()) {
			Integer index = entry.getKey();
			Operation actualOp = entry.getValue();
			Operation op = operations.get(index);
			if (!(op instanceof WildCard)) {
				throw new RuntimeException("Not a wild card operation! " + op);
			}
			WildCard wildOp = (WildCard) op;
			wildOp.setActual(actualOp);
		}

		// apply the moves
		for (int index : opIndices) {
			if (index >= operations.size()) {
				throw new IllegalArgumentException(
						"Operator index out of bounds: " + index);
			}
			apply(operations.get(index));
		}
		numUndosLeft = 2 - numUndosUsed;
	}

	public Level getLevel() {
		return level;
	}

	public String getName() {
		return level.getName();
	}

	public String getId() {
		return level.getId();
	}

	public int getMinMoves() {
		return level.getMinMoves();
	}

	public List<IMove> getMoves() {
		return moves;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public boolean undo() {
		// Do not deal with undoing square root- not allowed, simple
		// need to restore the operator if it was one undone
		if (moves.size() > 0) {
			IMove removed = moves.remove(moves.size() - 1);
			IMove previous = moves.isEmpty() ? null : moves
					.get(moves.size() - 1);
			if (removed instanceof SecondaryEquationMove) {
				if (previous instanceof Move) {
					throw new RuntimeException("Can't undo beyond a solution");
				}
				// remove extra one
				moves.remove(moves.size() - 1);
				if (moves.isEmpty()) {
					currentEquation = level.getEquation();
				} else {
					Move move = (Move) moves.get(moves.size() - 1);
					currentEquation = move.getEndEquation();
				}
			} else {
				currentEquation = ((Move) removed).getStartEquation();
			}
			numMoves--;
			numUndosLeft--;
			return true;
		}
		return false;
	}

	public int getUndosLeft() {
		return numUndosLeft;
	}

	public boolean canUndo() {
		if (numUndosLeft <= 0)
			return false;
		if (moves.isEmpty())
			return false;
		if ((moves.get(moves.size() - 1) instanceof SecondaryEquationMove)
				&& (moves.get(moves.size() - 2) instanceof Move)) {
			return false;
		}
		return true;
	}

	public Level getNextLevel() {
		return level.getNextLevel();
	}

	public void writeState(SQLiteDatabase db) {
		// clear any existing records
		clearState(db);

		ContentValues levelInfo = new ContentValues();
		levelInfo.put(DataBaseHelper.CurrentLevelTable.LEVEL_ID, getId());
		levelInfo.put(DataBaseHelper.CurrentLevelTable.NUM_UNDOS_USED,
				getUndosUsed());
		db.insert(DataBaseHelper.CURRENT_LEVEL_STATE_TABLE_NAME, null,
				levelInfo);

		writeBuiltWilds(db);

		int seq = 1;
		for (IMove each : moves) {
			// simply write actual operation moves in order
			// it should be deterministic, and simply applying them in order
			// again will restore the state
			if (!(each instanceof IMoveWithOperation))
				continue;
			IMoveWithOperation opMove = (IMoveWithOperation) each;
			ContentValues moveInfo = new ContentValues();
			moveInfo.put(DataBaseHelper.CurrentLevelMovesTable.LEVEL_ID,
					getId());
			moveInfo.put(DataBaseHelper.CurrentLevelMovesTable.SEQUENCE, seq);
			int opIndex = getOperatorIndex(opMove);
			moveInfo.put(DataBaseHelper.CurrentLevelMovesTable.OP_INDEX,
					opIndex);
			db.insert(DataBaseHelper.CURRENT_LEVEL_MOVES_TABLE_NAME, null,
					moveInfo);
			seq++;
		}
	}

	private void writeBuiltWilds(SQLiteDatabase db) {
		// write any wild cards that were "set"
		int index = -1;
		for (Operation each : getOperations()) {
			index++;
			if (!(each instanceof WildCard))
				continue;
			WildCard eachWild = (WildCard) each;
			if (!eachWild.isBuilt())
				continue;

			Operation op = eachWild.getActual();
			final ContentValues opInfo = new ContentValues();
			opInfo.put(DataBaseHelper.CurrentLevelWildTable.LEVEL_ID, getId());
			opInfo.put(DataBaseHelper.CurrentLevelWildTable.INDEX, index);
			opInfo.put(DataBaseHelper.CurrentLevelWildTable.TYPE, op.type()
					.toString());

			final OperationVisitor visitor = new OperationVisitor() {
				@Override
				public void visitSwap(Swap swap) {
					// no data for operation row
				}

				@Override
				public void visitAdd(Add add) {
					// output constant and coeff
					opInfo.put(DataBaseHelper.CurrentLevelWildTable.X2_COEFF,
							add.getExpression().getX2Coefficient().toString());
					opInfo.put(DataBaseHelper.CurrentLevelWildTable.CONST, add
							.getExpression().getConstant().toString());
					opInfo.put(DataBaseHelper.CurrentLevelWildTable.X_COEFF,
							add.getExpression().getXCoefficient().toString());
				}

				@Override
				public void visitSubtract(Subtract sub) {
					// output constant and coeff
					opInfo.put(DataBaseHelper.CurrentLevelWildTable.X2_COEFF,
							sub.getExpression().getX2Coefficient().toString());
					opInfo.put(DataBaseHelper.CurrentLevelWildTable.CONST, sub
							.getExpression().getConstant().toString());
					opInfo.put(DataBaseHelper.CurrentLevelWildTable.X_COEFF,
							sub.getExpression().getXCoefficient().toString());
				}

				@Override
				public void visitMultiply(Multiply multiply) {
					// output constant and zero coeff
					opInfo.put(DataBaseHelper.CurrentLevelWildTable.CONST,
							multiply.getFactor().toString());
					opInfo.put(DataBaseHelper.CurrentLevelWildTable.X_COEFF,
							"0");
					opInfo.put(DataBaseHelper.CurrentLevelWildTable.X2_COEFF,
							"0");
				}

				@Override
				public void visitDivide(Divide divide) {
					// output constant and zero coeff
					opInfo.put(DataBaseHelper.CurrentLevelWildTable.CONST,
							divide.getFactor().toString());
					opInfo.put(DataBaseHelper.CurrentLevelWildTable.X_COEFF,
							"0");
					opInfo.put(DataBaseHelper.CurrentLevelWildTable.X2_COEFF,
							"0");
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
					throw new RuntimeException("a Wild can't wrap another Wild");
				}
			};
			op.accept(visitor);

			long opId = db.insert(DataBaseHelper.CURRENT_LEVEL_WILD_TABLE_NAME,
					null, opInfo);
			if (opId < 0) {
				throw new RuntimeException("Error adding built wild ");
			}
		}
	}

	private int getOperatorIndex(IMoveWithOperation move) {
		if ((move.getOperation() instanceof SquareRoot)
				&& squareRootOpIndex >= 0) {
			// if a square root operator was used, we marked its index
			return squareRootOpIndex;
		}

		// loop to find it
		int index = 0;
		for (Operation each : getOperations()) {
			// if the square root has been applied, a "multiply by -1" can
			// simply be the square root changed operator
			if (each.equals(Multiply.NEGATE) && squareRootOpIndex > 0) {
				return squareRootOpIndex;
			}

			// Wild cards are stored as the WildCard operation, and so will
			// match
			if (each.equals(move.getOperation())) {
				return index;
			}
			index++;
		}

		return -1;
	}

	private static void clearState(SQLiteDatabase db) {
		db.delete(DataBaseHelper.CURRENT_LEVEL_STATE_TABLE_NAME, null, null);
		db.delete(DataBaseHelper.CURRENT_LEVEL_MOVES_TABLE_NAME, null, null);
		db.delete(DataBaseHelper.CURRENT_LEVEL_WILD_TABLE_NAME, null, null);
	}

	public static String readPendingLevel(SQLiteDatabase db) {
		try {
			Cursor query = db.query(
					DataBaseHelper.CURRENT_LEVEL_STATE_TABLE_NAME, null, null,
					null, null, null, null);
			if (!query.moveToFirst()) {
				query.close();
				return null;
			}

			int idIndex = query
					.getColumnIndex(DataBaseHelper.CurrentLevelTable.LEVEL_ID);
			return query.getString(idIndex);
		} finally {
			// TODO is this right?
			clearState(db);
		}
	}

	public static void readState(SQLiteDatabase db, String inLevelId,
			Puzzle puzzle) {
		try {
			Cursor query = db.query(
					DataBaseHelper.CURRENT_LEVEL_STATE_TABLE_NAME, null, null,
					null, null, null, null);
			if (!query.moveToFirst()) {
				query.close();
				return;
			}

			int idIndex = query
					.getColumnIndex(DataBaseHelper.CurrentLevelTable.LEVEL_ID);
			String levelId = query.getString(idIndex);
			if (inLevelId != null && !inLevelId.equals(levelId)) {
				query.close();
				clearState(db);
				return;
			}

			int numUndosIndex = query
					.getColumnIndex(DataBaseHelper.CurrentLevelTable.NUM_UNDOS_USED);
			int numUndosUsed = query.getInt(numUndosIndex);
			query.close();

			List<Integer> opIndices = new ArrayList<Integer>();
			Cursor moveQuery = db.query(
					DataBaseHelper.CURRENT_LEVEL_MOVES_TABLE_NAME, null, null,
					null, null, null,
					DataBaseHelper.CurrentLevelMovesTable.SEQUENCE);
			while (moveQuery.moveToNext()) {
				// moveQuery.getColumnIndex(DataBaseHelper.CurrentLevelMovesTable.LEVEL_ID);
				int opIndexIndex = moveQuery
						.getColumnIndex(DataBaseHelper.CurrentLevelMovesTable.OP_INDEX);
				int opIndex = moveQuery.getInt(opIndexIndex);
				if (opIndex < 0) {
					throw new IllegalArgumentException(
							"Operator index out of bounds: " + opIndex);
				}
				opIndices.add(opIndex);
			}
			moveQuery.close();

			// Read wild operations that were set
			Map<Integer, Operation> builtWilds = readBuiltWilds(db);
			puzzle.initialize(inLevelId, opIndices, numUndosUsed, builtWilds);
		} finally {
			clearState(db);
		}

	}

	private static Map<Integer, Operation> readBuiltWilds(SQLiteDatabase db) {
		Map<Integer, Operation> built = new HashMap<Integer, Operation>();

		Cursor opQuery = db.query(DataBaseHelper.CURRENT_LEVEL_WILD_TABLE_NAME,
				null, null, null, null, null, null);
		while (opQuery.moveToNext()) {
			// moveQuery.getColumnIndex(DataBaseHelper.CurrentLevelMovesTable.LEVEL_ID);
			int opIndexIndex = opQuery
					.getColumnIndex(DataBaseHelper.CurrentLevelWildTable.INDEX);
			int opIndex = opQuery.getInt(opIndexIndex);
			if (opIndex < 0) {
				throw new IllegalArgumentException(
						"Operator index out of bounds: " + opIndex);
			}
			String typeString = opQuery.getString(opQuery
					.getColumnIndex(DataBaseHelper.CurrentLevelWildTable.TYPE));
			Long id = opQuery.getLong(opQuery.getColumnIndex(BaseColumns._ID));
			OperationType type = OperationType.valueOf(typeString);

			Operation op;
			switch (type) {
			case ADD:
				op = new Add(CustomLevelDBReader.readExpression(opQuery,
						DataBaseHelper.CurrentLevelWildTable.X2_COEFF,
						DataBaseHelper.CurrentLevelWildTable.X_COEFF,
						DataBaseHelper.CurrentLevelWildTable.CONST));
				break;
			case SUBTRACT:
				op = new Subtract(CustomLevelDBReader.readExpression(opQuery,
						DataBaseHelper.CurrentLevelWildTable.X2_COEFF,
						DataBaseHelper.CurrentLevelWildTable.X_COEFF,
						DataBaseHelper.CurrentLevelWildTable.CONST));
				break;
			case MULTIPLY:
				op = new Multiply(CustomLevelDBReader.readFraction(opQuery,
						DataBaseHelper.CurrentLevelWildTable.CONST));
				break;
			case DIVIDE:
				op = new Divide(CustomLevelDBReader.readFraction(opQuery,
						DataBaseHelper.CurrentLevelWildTable.CONST));
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
			case WILD:
				throw new RuntimeException(
						"Can't wrap a Wild with another Wild");
			default:
				throw new RuntimeException("Unknown type " + type
						+ " while reading current level wild " + id);
			}
			built.put(opIndex, op);
		}
		opQuery.close();

		return built;
	}

	public boolean hasAnyMoves() {
		return moves.size() > 0;
	}

	public void updateRating() {
		level.possibilyUpdateRating(getNumMoves(), getUndosUsed());
	}

	public int getRating() {
		return level.calculateRating(getNumMoves(), getUndosUsed());
	}

	public int getExistingRating() {
		return level.getRating();
	}

	public int getUndosUsed() {
		return 2 - numUndosLeft;
	}

	public String getMultilineDescription() {
		return level.getMultilineDescription();
	}

	public Stage getStage() {
		return level.getStage();
	}

	public Equation getStartEquation() {
		return level.getEquation();
	}

	public Equation getCurrentEquation() {
		return currentEquation;
	}

	public Fraction[] getSolutions() {
		List<Fraction> sols = level.getSolutions();
		return (Fraction[]) sols.toArray(new Fraction[sols.size()]);
	}

	public void apply(Operation operation) {
		if (!getOperations().contains(operation)) {
			throw new RuntimeException("Operation " + operation
					+ " is not a valid operation for puzzle " + getId());
		}
		Equation startEquation = currentEquation;

		numMoves++;
		if (operation instanceof SquareRoot) {
			squareRootOpIndex = operations.indexOf(new SquareRoot());
		}
		MoveResult moveResult = operation.applyMove(startEquation, numMoves,
				operations);
		moves.add(moveResult.getPrimaryMove());
		if (moveResult.hasMultiple()) {
			IMove secondary1 = moveResult.getSecondary1();
			moves.add(secondary1);
			currentEquation = secondary1.getStartEquation();
			moveInWaiting = moveResult.getSecondary2();
		} else {
			currentEquation = moveResult.getPrimaryEndEquation();
		}

		if (currentEquation.isSolved() && moveInWaiting != null) {
			moves.add(moveInWaiting);
			// firstSolution = move.getEndEquation().getRhs().getConstant();
			currentEquation = moveInWaiting.getStartEquation();
			moveInWaiting = null;
		}
	}

	public boolean isSolved() {
		return moveInWaiting == null && !moves.isEmpty()
				&& currentEquation.isSolved();
	}

	public int getNumMoves() {
		return numMoves;
	}

}
