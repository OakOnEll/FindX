package com.oakonell.findx.custom.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionFormat;

import android.os.AsyncTask;

import com.oakonell.findx.custom.model.CustomLevel;
import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.custom.parse.ParseConnectivity.ParseUserExtra;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.Operation.OperationType;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.Divide;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.Square;
import com.oakonell.findx.model.ops.SquareRoot;
import com.oakonell.findx.model.ops.Subtract;
import com.oakonell.findx.model.ops.Swap;
import com.oakonell.utils.StringUtils;
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
		final String num_moves_field = "numMoves";
		final String num_operations_field = "numOperations";
		// some redundant fields, to allow easy sorting and retrieval with just
		// the level, no joins or server side sum/avg, which parse.com doesn't
		// support
		final String total_ratings_field = "total_ratings";
		final String avg_rating_field = "avg_rating";
		final String num_ratings_field = "num_ratings";

		final String lhs_expr_prefix = "lhs_";
		final String rhs_expr_prefix = "rhs_";
	}

	public interface ParseLevelOperation {
		final String classname = "LevelOperation";
		final String level_field = "level";
		final String type_field = "type";
	}

	public interface ParseLevelMove {
		final String classname = "LevelMove";
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

	public static String postLevel(CustomLevel theLevel) {
		try {
			ParseUser parseUser = ParseUser.getCurrentUser();

			ParseObject level = new ParseObject(ParseCustomLevel.classname);
			level.put(ParseCustomLevel.title_field, theLevel.getName());
			level.put(ParseCustomLevel.createdBy_field, parseUser);

			Equation startEquation = theLevel.getEquation();
			Equation equation = startEquation;
			for (Operation each : theLevel.getSolutionOperations()) {
				equation = each.apply(equation);
			}
			level.put(ParseCustomLevel.solution_field, equation.getRhs()
					.getConstant().toString());

			addExpression(ParseCustomLevel.lhs_expr_prefix,
					startEquation.getLhs(), level);
			addExpression(ParseCustomLevel.rhs_expr_prefix,
					startEquation.getRhs(), level);

			level.put(ParseCustomLevel.num_moves_field, theLevel.getMinMoves());
			level.put(ParseCustomLevel.num_operations_field, theLevel
					.getOperations().size());
			level.save();
			String id = level.getObjectId();

			Map<Operation, ParseObject> opToParseOp = new HashMap<Operation, ParseObject>();
			int i = 0;
			for (Operation each : theLevel.getOperations()) {
				ParseObject parseOp = new ParseObject(
						ParseLevelOperation.classname);
				parseOp.put(ParseLevelOperation.level_field, level);
				parseOp.put(ParseLevelOperation.type_field, each.type()
						.ordinal());
				switch (each.type()) {
				case ADD:
					addExpression("", ((Add) each).getExpression(), parseOp);
					break;
				case SUBTRACT:
					addExpression("", ((Subtract) each).getExpression(),
							parseOp);
					break;
				case MULTIPLY:
					addExpression("", new Expression(Fraction.ZERO,
							((Multiply) each).getFactor()), parseOp);
					break;
				case DIVIDE:
					addExpression("", new Expression(Fraction.ZERO,
							((Divide) each).getFactor()), parseOp);
					break;
				case SWAP:
					break;
				}
				opToParseOp.put(each, parseOp);
				parseOp.save();
				i++;
			}
			i = 0;

			for (Operation each : theLevel.getSolutionOperations()) {
				ParseObject parseMove = new ParseObject(
						ParseLevelMove.classname);
				parseMove.put(ParseLevelMove.level_field, level);
				parseMove.put(ParseLevelMove.sequence_field, i);
				if (each != null) {
					ParseObject parseOp = opToParseOp.get(each);
					parseMove.put(ParseLevelMove.operation_field, parseOp);
				}
				parseMove.save();
				i++;
			}
			return id;
		} catch (ParseException e) {
			throw new RuntimeException("Error writing level to parse", e);
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

	public static CustomLevelBuilder load(ParseObject level) {
		FractionFormat format = new FractionFormat();

		Equation startEquation = readEquation(level);

		String title = level.getString(ParseCustomLevel.title_field);
		ParseUser creator = level
				.getParseUser(ParseCustomLevel.createdBy_field);

		String serverId = level.getObjectId();
		Fraction solution = format.parse(level
				.getString(ParseCustomLevel.solution_field));

		Map<String, Operation> parseOpIdToOp = new HashMap<String, Operation>();
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
				ParseLevelOperation.classname);
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

		ParseQuery<ParseObject> moveQuery = new ParseQuery<ParseObject>(
				ParseLevelMove.classname);
		moveQuery.whereEqualTo(ParseLevelMove.level_field, level);
		moveQuery.orderByAscending(ParseLevelMove.sequence_field);
		List<ParseObject> parseMoves;
		try {
			parseMoves = moveQuery.find();
		} catch (ParseException e) {
			throw new RuntimeException("error finding level's moves", e);
		}

		List<Operation> moveOperations = new ArrayList<Operation>();
		Equation equation = startEquation;
		for (ParseObject each : parseMoves) {
			Operation op = null;
			ParseObject parseOp = (ParseObject) each
					.getParseObject(ParseLevelMove.operation_field);
			if (parseOp != null) {
				op = parseOpIdToOp.get(parseOp.getObjectId());
				equation = op.apply(equation);
				moveOperations.add(op);
			}
		}

		final CustomLevelBuilder builder = new CustomLevelBuilder();

		builder.setIsImported(!creator.getObjectId().equals(
				ParseUser.getCurrentUser().getObjectId()));
		builder.setTitle(title);
		builder.setAuthor(creator.getString(ParseUserExtra.nickname_field));
		builder.setSolution(solution);
		builder.setServerId(serverId);
		builder.getOperations().addAll(theOperations);
		builder.defaultMaxSequence();

		Collections.reverse(moveOperations);
		for (Operation each : moveOperations) {
			builder.apply(each);
		}

		return builder;
	}

	public static Operation loadOperationFrom(ParseObject each) {
		int typeIndex = each.getInt(ParseLevelOperation.type_field);
		OperationType type = OperationType.values()[typeIndex];
		Operation op = null;
		Expression expr = readExpression("", each);
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
		case SQUARE_ROOT:
			op = new SquareRoot();
			break;
		case SQUARE:
			op = new Square();
			break;
		default:
			throw new RuntimeException("Unexpected operator index " + typeIndex);
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

	public static void addOrModifyRatingComment(final ParseObject parseLevel,
			final ParseObject existingParseComment, final float rating,
			final String comment, final Runnable continuation) {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ParseObject parseComment = existingParseComment;
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
					continuation.run();
				}
			}

		};
		task.execute();
	}
}
