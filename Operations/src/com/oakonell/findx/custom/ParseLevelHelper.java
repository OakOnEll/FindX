package com.oakonell.findx.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionFormat;

import com.oakonell.findx.custom.model.CustomLevel;
import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.Operation.OperationType;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.Divide;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.Subtract;
import com.oakonell.findx.model.ops.Swap;
import com.oakonell.utils.StringUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ParseLevelHelper {
	public static String postLevel(CustomLevel theLevel) {
		try {
			ParseUser parseUser = ParseUser.getCurrentUser();

			ParseObject level = new ParseObject("CustomLevel");
			level.put("title", theLevel.getName());
			level.put("createdBy", parseUser);

			Equation startEquation = theLevel.getEquation();
			Equation equation = startEquation;
			for (Operation each : theLevel.getSolutionOperations()) {
				equation = each.apply(equation);
			}
			level.put("solution", equation.getRhs().getConstant().toString());

			addExpression("lhs_", startEquation.getLhs(), level);
			addExpression("rhs_", startEquation.getRhs(), level);

			level.put("numMoves", theLevel.getMinMoves());
			level.put("numOperations", theLevel.getOperations().size());
			level.save();
			String id = level.getObjectId();

			Map<Operation, ParseObject> opToParseOp = new HashMap<Operation, ParseObject>();
			int i = 0;
			for (Operation each : theLevel.getOperations()) {
				ParseObject parseOp = new ParseObject("LevelOperation");
				parseOp.put("level", level);
				parseOp.put("type", each.type().ordinal());
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
				ParseObject parseMove = new ParseObject("LevelMove");
				parseMove.put("level", level);
				parseMove.put("sequence", i);
				if (each != null) {
					ParseObject parseOp = opToParseOp.get(each);
					parseMove.put("operation", parseOp);
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
		level.put(prefix + "scalar", expr.getConstant().toString());
		level.put(prefix + "xcoeff", expr.getXCoefficient().toString());
	}

	public static CustomLevelBuilder load(ParseObject level) {
		FractionFormat format = new FractionFormat();

		Equation startEquation = readEquation(level);

		String title = level.getString("title");
		ParseUser creator = level.getParseUser("createdBy");
		
		String serverId = level.getObjectId();
		Fraction solution = format.parse(level.getString("solution"));

		Map<String, Operation> parseOpIdToOp = new HashMap<String, Operation>();
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
				"LevelOperation");
		query.whereEqualTo("level", level);
		List<ParseObject> operations;
		try {
			operations = query.find();
		} catch (ParseException e) {
			throw new RuntimeException("Error finding level's operations", e);
		}
		List<Operation> theOperations = new ArrayList<Operation>();
		for (ParseObject each : operations) {
			int typeIndex = each.getInt("type");
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
			default:
				throw new RuntimeException("Unexpected operator index "
						+ typeIndex);
			}
			parseOpIdToOp.put(each.getObjectId(), op);
			theOperations.add(op);
		}

		/*
		 * ParseObject parseMove = new ParseObject("LevelMove");
		 * parseMove.put("level", level); parseMove.put("sequence", i); if (each
		 * != null) { ParseObject parseOp = opToParseOp.get(each);
		 * parseMove.put("operation", parseOp); }
		 */
		ParseQuery<ParseObject> moveQuery = new ParseQuery<ParseObject>(
				"LevelMove");
		moveQuery.whereEqualTo("level", level);
		moveQuery.orderByAscending("sequence");
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
					.getParseObject("operation");
			if (parseOp != null) {
				op = parseOpIdToOp.get(parseOp.getObjectId());
				equation = op.apply(equation);
				moveOperations.add(op);
			}
		}

		final CustomLevelBuilder builder = new CustomLevelBuilder();

		builder.setIsImported(!creator.getObjectId().equals(ParseUser.getCurrentUser().getObjectId()));
		builder.setTitle(title);
		builder.setAuthor(creator.getString("nickname"));		
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

	public static Equation readEquation(ParseObject level) {
		Expression lhs = readExpression("lhs_", level);
		Expression rhs = readExpression("rhs_", level);
		return new Equation(lhs, rhs);
	}

	public static Expression readExpression(String prefix, ParseObject each) {
		FractionFormat format = new FractionFormat();

		Fraction scalar = Fraction.ZERO;
		Fraction xCcoeff = Fraction.ZERO;
		String scalarString = each.getString(prefix + "scalar");
		if (!StringUtils.isEmpty(scalarString)) {
			scalar = format.parse(scalarString);
		}
		String xCcoeffString = each.getString(prefix + "xcoeff");
		if (!StringUtils.isEmpty(xCcoeffString)) {
			xCcoeff = format.parse(xCcoeffString);
		}

		return new Expression(xCcoeff, scalar);
	}

}
