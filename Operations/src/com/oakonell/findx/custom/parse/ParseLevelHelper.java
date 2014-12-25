package com.oakonell.findx.custom.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionFormat;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.oakonell.findx.FindXApp;
import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.custom.model.CustomLevelDBReader;
import com.oakonell.findx.custom.model.ICustomLevel;
import com.oakonell.findx.custom.parse.ParseConnectivity.ParseUserExtra;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.Operation.OperationType;
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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ParseLevelHelper {

	public interface ExpressionField {
		final String scalar_field = "scalar";
		final String xcoeff_field = "xcoeff";
		final String x2coeff_field = "x2coeff";
	}

	public interface ParseCustomLevel {
		final String classname = "CustomLevel";

		final String title_field = "title";
		final String createdBy_field = "createdBy";
		final String solution_field = "solution";
		final String solution2_field = "solution2";
		// some redundant fields, for easy use in searching since parse.com
		// doesn't support aggregate functions
		final String num_moves_field = "numMoves";
		final String num_operations_field = "numOperations";
		// some redundant fields, to allow easy sorting and retrieval with just
		// the level, no joins or server side sum/avg, which parse.com doesn't
		// support
		final String total_ratings_field = "total_ratings";
		final String avg_rating_field = "avg_rating";
		final String num_ratings_field = "num_ratings";

		final String num_flags = "numFlags";

		// prefix for standard ExpressionField columns for the start equation
		final String lhs_expr_prefix = "lhs_";
		final String rhs_expr_prefix = "rhs_";

		final String download_counter = "downloads";

	}

	public interface ParseLevelOperation {
		final String classname = "LevelOperation";
		final String level_field = "level";
		final String sequence_field = "sequence";
		final String type_field = "type";
		final String wild_type_field = "wild_type";
	}

	public interface ParseLevelMove {
		final String classname = "LevelMove";
		final String move_type_field = "move_type";
		final String level_field = "level";
		final String sequence_field = "sequence";
		final String operation_field = "operation";
	}

	public interface ParseLevelRating {
		final String classname = "LevelRating";
		final String level_field = "level";
		final String createdBy_field = "createdBy";
		final String comment_field = "comment";
		final String rating_field = "rating";
	}

	public interface ParseCustomLevelFlag {
		final String classname = "LevelFlag";
		final String level_field = "level";
		final String flaggedBy_field = "flaggedBy";
	}

	public static String postLevel(FindXApp app, Context context,
			ICustomLevel theLevel) {
		try {

			ParseObject level = postMainLevel(theLevel);
			String id = level.getObjectId();

			postLevelOperations(theLevel, level);

			postLevelMoves(level, theLevel.getLevelSolution()
					.getFirstOperations(), 0);
			postLevelMoves(level, theLevel.getLevelSolution()
					.getSecondaryOperations1(), 1);
			postLevelMoves(level, theLevel.getLevelSolution()
					.getSecondaryOperations2(), 2);

			Tracker googleTracker = app.getTracker();
			googleTracker.send(new HitBuilders.EventBuilder()
					.setCategory("custom").setAction("post").setLabel(id)
					.build());

			return id;
		} catch (ParseException e) {
			throw new RuntimeException("Error writing level to parse", e);
		}
	}

	private static ParseObject postMainLevel(ICustomLevel theLevel)
			throws ParseException {
		ParseUser parseUser = ParseUser.getCurrentUser();

		ParseObject level = new ParseObject(ParseCustomLevel.classname);
		level.put(ParseCustomLevel.title_field, theLevel.getName());
		level.put(ParseCustomLevel.createdBy_field, parseUser);

		level.put(ParseCustomLevel.num_flags, 0);

		Equation startEquation = theLevel.getEquation();
		Fraction solution1 = theLevel.getSolutions().get(0);
		level.put(ParseCustomLevel.solution_field, solution1.toString());
		if (theLevel.getSolutions().size() > 1) {
			Fraction solution2 = theLevel.getSolutions().get(1);
			level.put(ParseCustomLevel.solution2_field, solution2.toString());
		} else {
			level.put(ParseCustomLevel.solution2_field, "0");
		}

		addExpression(ParseCustomLevel.lhs_expr_prefix, startEquation.getLhs(),
				level);
		addExpression(ParseCustomLevel.rhs_expr_prefix, startEquation.getRhs(),
				level);

		level.put(ParseCustomLevel.num_moves_field, theLevel.getMinMoves());
		level.put(ParseCustomLevel.num_operations_field, theLevel
				.getOperations().size());
		level.save();
		return level;
	}

	private static void postLevelOperations(ICustomLevel theLevel,
			ParseObject level) throws ParseException {
		int opSequence = 0;
		for (Operation each : theLevel.getOperations()) {
			ParseObject parseOp = new ParseObject(ParseLevelOperation.classname);
			parseOp.put(ParseLevelOperation.level_field, level);
			parseOp.put(ParseLevelOperation.sequence_field, opSequence);
			OperationType type = each.type();
			parseOp.put(ParseLevelOperation.type_field, type.ordinal());
			Operation op = each;
			if (type == OperationType.WILD) {
				WildCard wild = (WildCard) op;
				op = wild.getActual();
				type = op.type();
				parseOp.put(ParseLevelOperation.wild_type_field, type.ordinal());
			}

			switch (type) {
			case ADD:
				addExpression("", ((Add) op).getExpression(), parseOp);
				break;
			case SUBTRACT:
				addExpression("", ((Subtract) op).getExpression(), parseOp);
				break;
			case MULTIPLY:
				addExpression(
						"",
						new Expression(Fraction.ZERO, ((Multiply) op)
								.getFactor()), parseOp);
				break;
			case DIVIDE:
				addExpression(
						"",
						new Expression(Fraction.ZERO, ((Divide) op).getFactor()),
						parseOp);
				break;
			case FACTOR:
				addExpression("", ((Factor) op).getExpression(), parseOp);
				break;
			case DEFACTOR:
				addExpression("", ((Defactor) op).getExpression(), parseOp);
				break;
			case SQUARE_ROOT:
				break;
			case SWAP:
				break;
			default:
				throw new RuntimeException("Unhandled Operator type " + type);
			}
			parseOp.save();
			opSequence++;
		}
	}

	private static void postLevelMoves(ParseObject level,
			List<Integer> opIndexes, int moveType) throws ParseException {
		if (opIndexes == null) {
			return;
		}
		int i;
		i = 0;
		for (Integer index : opIndexes) {
			ParseObject parseMove = new ParseObject(ParseLevelMove.classname);
			parseMove.put(ParseLevelMove.level_field, level);
			parseMove.put(ParseLevelMove.move_type_field, moveType);
			parseMove.put(ParseLevelMove.sequence_field, i);
			parseMove.put(ParseLevelMove.operation_field, index);
			parseMove.save();
			i++;
		}
	}

	private static void addExpression(String prefix, Expression expr,
			ParseObject level) {
		level.put(prefix + ExpressionField.scalar_field, expr.getConstant()
				.toString());
		level.put(prefix + ExpressionField.xcoeff_field, expr.getXCoefficient()
				.toString());
		level.put(prefix + ExpressionField.x2coeff_field, expr
				.getX2Coefficient().toString());
	}

	public static CustomLevelBuilder load(FindXApp app, Context context,
			ParseObject level) {
		Tracker googleTracker = app.getTracker();
		googleTracker.send(new HitBuilders.EventBuilder().setCategory("custom")
				.setAction("load").setLabel(level.getObjectId()).build());

		level.increment(ParseCustomLevel.download_counter);
		try {
			level.save();
		} catch (ParseException e1) {
			googleTracker.send(new HitBuilders.EventBuilder().setCategory("custom")
					.setAction("incDownloadsErr").setLabel(level.getObjectId()).build());
			Log.e("ParseLevelHelper", "Unable to increment download count", e1);
		}

		FractionFormat format = new FractionFormat();

		Equation startEquation = readEquation(level);

		String title = level.getString(ParseCustomLevel.title_field);
		ParseUser creator = level
				.getParseUser(ParseCustomLevel.createdBy_field);

		String serverId = level.getObjectId();
		Fraction solution = format.parse(level
				.getString(ParseCustomLevel.solution_field));
		Fraction solution2 = format.parse(level
				.getString(ParseCustomLevel.solution2_field));

		Map<String, Operation> parseOpIdToOp = new HashMap<String, Operation>();
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
				ParseLevelOperation.classname);
		query.orderByAscending(ParseLevelOperation.sequence_field);
		query.whereEqualTo(ParseLevelOperation.level_field, level);
		List<ParseObject> operations;
		try {
			operations = query.find();
		} catch (ParseException e) {
			throw new RuntimeException("Error finding level's operations", e);
		}
		List<Operation> theOperations = new ArrayList<Operation>();
		for (ParseObject each : operations) {
			Operation op = loadOperationFrom(each);
			parseOpIdToOp.put(each.getObjectId(), op);
			theOperations.add(op);
		}

		// get the moves in a single query
		ParseQuery<ParseObject> moveQuery = new ParseQuery<ParseObject>(
				ParseLevelMove.classname);
		moveQuery.whereEqualTo(ParseLevelMove.level_field, level);
		moveQuery.orderByAscending(ParseLevelMove.move_type_field);
		moveQuery.orderByAscending(ParseLevelMove.sequence_field);
		List<ParseObject> parseMoves;
		try {
			parseMoves = moveQuery.find();
		} catch (ParseException e) {
			throw new RuntimeException("error finding level's moves", e);
		}
		// split them by move type
		List<Integer> primMoves = new ArrayList<Integer>();
		List<Integer> sec1Moves = new ArrayList<Integer>();
		List<Integer> sec2Moves = new ArrayList<Integer>();
		for (ParseObject each : parseMoves) {
			int moveType = each.getInt(ParseLevelMove.move_type_field);
			int index = each.getInt(ParseLevelMove.operation_field);
			switch (moveType) {
			case 0:
				primMoves.add(index);
				break;
			case 1:
				sec1Moves.add(index);
				break;
			case 2:
				sec2Moves.add(index);
				break;
			default:
				throw new RuntimeException("Invalid 'moveType' " + moveType);
			}
		}

		final CustomLevelBuilder builder = new CustomLevelBuilder();

		ParseUser currentUser = ParseUser.getCurrentUser();
		builder.setIsImported(currentUser == null
				|| !creator.getObjectId().equals(currentUser.getObjectId()));
		builder.setTitle(title);
		builder.setAuthor(creator.getString(ParseUserExtra.nickname_field));
		builder.setSolution(solution);
		// TODO ugh, I should use a null for soution 2
		if (sec1Moves.isEmpty() && solution2.compareTo(Fraction.ZERO) == 0) {
			builder.setSolution2(null);
		} else {
			builder.setSolution2(solution2);
		}
		builder.setServerId(serverId);
		builder.addOperations(theOperations);
		builder.defaultMaxSequence();

		CustomLevelDBReader.populateBuilderMovesFromOperationIndices(builder,
				startEquation, primMoves, sec1Moves, sec2Moves);

		return builder;
	}

	public static Operation loadOperationFrom(ParseObject each) {
		int typeIndex = each.getInt(ParseLevelOperation.type_field);
		OperationType type = OperationType.values()[typeIndex];
		Operation op = null;
		Expression expr = readExpression("", each);

		WildCard wildOp = null;
		if (type == OperationType.WILD) {
			wildOp = new WildCard();
			int wildTypeIndex = each
					.getInt(ParseLevelOperation.wild_type_field);
			OperationType wildType = OperationType.values()[wildTypeIndex];
			type = wildType;
		}

		switch (type) {
		case ADD:
			op = new Add(expr);
			break;
		case SUBTRACT:
			op = new Subtract(expr);
			break;
		case MULTIPLY:
			op = new Multiply(expr.getConstant());
			break;
		case DIVIDE:
			op = new Divide(expr.getConstant());
			break;
		case SWAP:
			op = new Swap();
			break;
		case FACTOR:
			op = new Factor(expr);
			break;
		case DEFACTOR:
			op = new Defactor(expr);
			break;
		case SQUARE_ROOT:
			op = new SquareRoot();
			break;
		case SQUARE:
			op = new Square();
			break;
		default:
			throw new RuntimeException("Unexpected operator index " + typeIndex);
		}
		if (wildOp != null) {
			wildOp.setActual(op);
			op = wildOp;
		}
		return op;
	}

	public static Equation readEquation(ParseObject level) {
		Expression lhs = readExpression(ParseCustomLevel.lhs_expr_prefix, level);
		Expression rhs = readExpression(ParseCustomLevel.rhs_expr_prefix, level);
		return new Equation(lhs, rhs);
	}

	public static Expression readExpression(String prefix, ParseObject each) {
		FractionFormat format = new FractionFormat();

		Fraction scalar = Fraction.ZERO;
		Fraction xCcoeff = Fraction.ZERO;
		Fraction x2Ccoeff = Fraction.ZERO;
		String scalarString = each.getString(prefix
				+ ExpressionField.scalar_field);
		if (!StringUtils.isEmpty(scalarString)) {
			scalar = format.parse(scalarString);
		}

		String xCcoeffString = each.getString(prefix
				+ ExpressionField.xcoeff_field);
		if (!StringUtils.isEmpty(xCcoeffString)) {
			xCcoeff = format.parse(xCcoeffString);
		}

		String x2CcoeffString = each.getString(prefix
				+ ExpressionField.x2coeff_field);
		if (!StringUtils.isEmpty(x2CcoeffString)) {
			x2Ccoeff = format.parse(x2CcoeffString);
		}

		return new Expression(x2Ccoeff, xCcoeff, scalar);
	}

	interface OnRatingLoaded {
		void ratingLoaded(ParseObject rating);
	}

	public static void getMyRatingComment(ParseObject proxiedParseLevel,
			final OnRatingLoaded onRatingLoaded) {
		ParseQuery<ParseObject> myCommentQuery = new ParseQuery<ParseObject>(
				ParseLevelRating.classname);
		myCommentQuery.whereEqualTo(ParseLevelRating.level_field,
				proxiedParseLevel);
		myCommentQuery.whereEqualTo(ParseLevelRating.createdBy_field,
				ParseUser.getCurrentUser());
		myCommentQuery.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> comments, ParseException e) {
				if (e != null) {
					// TODO error
					throw new RuntimeException("Error loading my comment", e);
				}
				if (comments.isEmpty()) {
					onRatingLoaded.ratingLoaded(null);
					return;
				}
				if (comments.size() > 1) {
					throw new RuntimeException(
							"Error- more than 1 of my comments for this level!");
				}
				ParseObject myParseComment = comments.get(0);
				onRatingLoaded.ratingLoaded(myParseComment);
			}
		});
	}

	public static void addOrModifyRatingComment(final ParseObject parseLevel,
			final ParseObject existingParseComment, final float rating,
			final String comment, final OnRatingLoaded continuation) {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			private ParseObject parseComment;

			@Override
			protected Void doInBackground(Void... params) {
				parseComment = existingParseComment;
				if (parseComment == null) {
					parseComment = new ParseObject(ParseLevelRating.classname);
					parseLevel.increment(ParseCustomLevel.total_ratings_field,
							rating);
					parseLevel.increment(ParseCustomLevel.num_ratings_field);
				} else {
					// modify the denormalized rating columns on the level
					double currentUserRating = parseComment
							.getDouble(ParseLevelRating.rating_field);
					parseLevel.increment(ParseCustomLevel.total_ratings_field,
							rating - currentUserRating);
				}
				// another redundant field, to allow for sorting by avg rating
				double total = parseLevel
						.getInt(ParseCustomLevel.total_ratings_field);
				int numberRatings = parseLevel
						.getInt(ParseCustomLevel.num_ratings_field);
				parseLevel.put(ParseCustomLevel.avg_rating_field, total
						/ numberRatings);

				parseComment.put(ParseLevelRating.level_field, parseLevel);
				parseComment.put(ParseLevelRating.rating_field, rating);
				parseComment.put(ParseLevelRating.comment_field, comment);
				parseComment.put(ParseLevelRating.createdBy_field,
						ParseUser.getCurrentUser());
				try {
					parseLevel.save();
					parseComment.save();
				} catch (ParseException e) {
					throw new RuntimeException("Error saving comment/rating", e);
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (continuation != null) {
					continuation.ratingLoaded(parseComment);
				}
			}

		};
		task.execute();
	}

	public static void flagLevel(ParseObject level) {
		ParseUser parseUser = ParseUser.getCurrentUser();
		ParseObject flaggedLevel = new ParseObject(
				ParseCustomLevelFlag.classname);
		flaggedLevel.put(ParseCustomLevelFlag.flaggedBy_field, parseUser);
		flaggedLevel.put(ParseCustomLevelFlag.level_field, level);

		level.increment(ParseCustomLevel.num_flags);
		try {
			level.save();
			flaggedLevel.save();
		} catch (ParseException e) {
			throw new RuntimeException("Error flagging level", e);
		}
	}

	public static void checkLevelAuthorAndTitleNotUnique(
			final ICustomLevel theLevel, final Runnable unique,
			final Runnable notUnique) {
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {

				ParseUser parseUser = ParseUser.getCurrentUser();
				String name = theLevel.getName();

				ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
						ParseCustomLevel.classname);
				query.whereEqualTo(ParseCustomLevel.title_field, name);
				query.whereEqualTo(ParseCustomLevel.createdBy_field, parseUser);
				try {
					return query.count() > 0;
				} catch (ParseException e) {
					throw new RuntimeException("Parse exception", e);
				}
			}

			@Override
			protected void onPostExecute(Boolean exists) {
				if (exists) {
					notUnique.run();
					return;
				}
				unique.run();
			}

		};
		task.execute();
	}
}
