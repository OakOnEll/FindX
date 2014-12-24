package com.oakonell.findx.custom.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.util.Log;

import com.oakonell.findx.FindXApp;
import com.oakonell.findx.custom.model.AbstractEquationSolver.Solution;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.IMove;
import com.oakonell.findx.model.IMoveWithOperation;
import com.oakonell.findx.model.Level.LevelSolution;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.MoveResult;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.Operation.OperationType;
import com.oakonell.findx.model.OperationVisitor;
import com.oakonell.findx.model.Stage;
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
import com.oakonell.utils.xml.XMLUtils;

public class CustomLevelBuilder extends TempCorrectLevelBuilder {

	public CustomLevelBuilder() {
		setSolution(Fraction.ONE);
	}

	public List<IMove> getPrimaryMoves() {
		return primaryMoves;
	}

	public List<IMove> getSecondary1Moves() {
		return secondary1Moves;
	}

	public List<IMove> getSecondary2Moves() {
		return secondary2Moves;
	}

	public boolean canReplaceOperation(Operation operation) {
		// the same logistics apply to delete/edit, as an edit is effectively a
		// delete and apply the new operation
		return canDeleteOperation(operation);
	}

	public boolean canDeleteOperation(Operation op) {
		// TODO Check if eg any Factor operation used
		if (op instanceof SquareRoot) {
			// can't delete a square root operation if it is used
			IMove last = primaryMoves.get(primaryMoves.size() - 1);
			return !(last instanceof IMoveWithOperation && ((IMoveWithOperation) last)
					.getOperation().equals(op));
		}
		// otherwise, can't delete it if it is used in the secondary solutions
		return !usesOperationInSecondaries(op);
	}

	public boolean usesOperation(Operation operation) {
		return usesOperation(operation, primaryMoves)
				|| usesOperationInSecondaries(operation);
	}

	private boolean usesOperationInSecondaries(Operation op) {
		return usesOperation(op, secondary1Moves)
				|| usesOperation(op, secondary2Moves);
	}

	private boolean usesOperation(Operation op, List<IMove> moves) {
		for (IMove iEach : moves) {
			if (!(iEach instanceof IMoveWithOperation)) {
				continue;
			}
			IMoveWithOperation each = (IMoveWithOperation) iEach;

			if (each.getOperation() != null && each.getOperation().equals(op)) {
				return true;
			}
		}
		return false;
	}

	public void removeOperation(Operation operation) {
		if (!operations.remove(operation)) {
			return;
		}
		checkOperatorAppliability();

		// An earlier check canDeleteOperation should have been called, and this
		// can only be called after
		// we only allow deleting an operation if used in the "primary" moves
		if (usesOperationInSecondaries(operation)) {
			throw new RuntimeException(
					"Can't delete an operation used in a secondary solution");
		}

		// we can't simply re-apply all the moves
		// find the latest index of the operation in primaryMoves
		int index = primaryMoves.size() - 1;
		for (; index >= 0; index--) {
			IMove iMove = primaryMoves.get(index);
			if (!(iMove instanceof Move))
				continue;
			Move move = (Move) iMove;
			Operation op = move.getOperation();
			if (op == null)
				continue;
			if (op.equals(operation))
				break;
		}
		if (index <= 0)
			return;

		Move item = (Move) primaryMoves.get(index);

		List<IMove> subList = primaryMoves.subList(1, index + 1);
		List<IMove> toReapply = new ArrayList<IMove>(subList.subList(0,
				subList.size() - 1));
		int numDeleted = subList.size();
		subList.clear();
		primaryMoves.remove(0);
		for (int i = 0; i < numDeleted; i++) {
			decrementMoveNumbers();
		}
		primaryMoves.add(0, new Move(item.getEndEquation(), null, 0));

		Collections.reverse(toReapply);
		for (IMove iEach : toReapply) {
			Move each = (Move) iEach;
			Operation eachOperation = each.getOperation();
			if (eachOperation.equals(operation))
				continue;
			apply(eachOperation, true);
		}
	}

	public void setSolution2(Fraction secondarySolution) {
		this.secondarySolution = secondarySolution;
	}

	public void setSolution(Fraction solution) {
		if (this.solution != null && this.solution.equals(solution)) {
			return;
		}
		// can't set the solution if a multiple solution operation has been
		// applied
		if (secondarySolution != null) {
			throw new RuntimeException(
					"Can't change the solution when there's a square root applied");
		}

		// Alternatively test that the new solution is valid with the
		// current moves
		// eg, if there is a square root, it may not be a rational square root
		// and should not be allowed

		this.solution = solution == null ? Fraction.ZERO : solution;
		// adjust the moves list
		Equation solvedEquation = new Equation(new Expression(1, 0),
				new Expression(Fraction.ZERO, solution));
		Move solvedMove = new Move(solvedEquation, null, 0);
		if (primaryMoves.size() <= 1) {
			primaryMoves.clear();
			primaryMoves.add(solvedMove);
			return;
		}
		List<IMove> oldMoves = new ArrayList<IMove>(primaryMoves.subList(1,
				primaryMoves.size()));
		Collections.reverse(oldMoves);
		primaryMoves.clear();
		primaryMoves.add(solvedMove);
		for (IMove iEach : oldMoves) {
			Move each = (Move) iEach;
			Operation op = each.getOperation();
			apply(op);
		}
	}

	public void apply(Operation op) {
		apply(op, true);
	}

	protected void apply(Operation op, boolean adjustMoveNumbers) {
		markAsOptimized(false);

		if (op == null || !operations.contains(op)) {
			throw new IllegalArgumentException("Operation " + op
					+ " is not one of the level's valid operations");
		}

		Operation inverse = op.inverse();
		Equation newStartEquation;
		Move move = null;
		try {
			// top move should have no operation, just a starting equation
			move = (Move) primaryMoves.remove(0);
			newStartEquation = inverse.apply(move.getStartEquation());
		} catch (RuntimeException e) {
			primaryMoves.add(0, move);
			throw e;
		}

		// here, need to solve the TWO equations if there was a branch
		MoveResult applyMove = op.applyMove(newStartEquation, 1, null, null);
		if (applyMove.hasMultiple()) {
			// TODO put up a progress dialog for resolving roots
			// completely replace the moves list with these two solution moves
			int numMoves = primaryMoves.size();
			primaryMoves.clear();

			primaryMoves.add(0, applyMove.getPrimaryMove());

			Equation rootEquation1 = applyMove.getSecondary1()
					.getStartEquation();
			Equation rootEquation2 = applyMove.getSecondary2()
					.getStartEquation();
			EquationSolver solver = new EquationSolver();
			List<Operation> modOps = new ArrayList<Operation>(operations);
			modOps.remove(op);
			modOps.add(Multiply.NEGATE);
			// A Fudge factor, in case the chosen root equation needs a couple
			// of MULTIPLY operations
			int fudgeFactor = 3;
			Solution solution1 = solver.solve(rootEquation1, modOps, numMoves
					+ fudgeFactor, null);
			Solution solution2 = solver.solve(rootEquation2, modOps, numMoves
					+ fudgeFactor, null);
			if (solution1.solution.compareTo(solution) == 0) {
				secondarySolution = solution2.solution;
			} else if (solution2.solution.compareTo(solution) == 0) {
				secondarySolution = solution1.solution;
			} else {
				throw new RuntimeException(
						"Unexpected state- root solutions do not contain original solution.");
			}

			secondary1Moves.add(applyMove.getSecondary1());
			int i = 2;
			for (IMove each : solution1.primaryMoves) {
				if (!(each instanceof Move)) {
					continue;
				}
				if (((Move) each).getOperation() == null)
					continue;

				secondary1Moves.add(new Move(each.getStartEquation(),
						((Move) each).getOperation(), i++));
			}
			secondary2Moves.add(applyMove.getSecondary2());
			for (IMove each : solution2.primaryMoves) {
				if (!(each instanceof Move)) {
					continue;
				}
				if (((Move) each).getOperation() == null)
					continue;

				secondary2Moves.add(new Move(each.getStartEquation(),
						((Move) each).getOperation(), i++));
			}
		} else {
			if (adjustMoveNumbers) {
				// adjust each move's move Number
				incrementMoveNumbers();
			}
			primaryMoves.add(0, applyMove.getPrimaryMove());
		}

		primaryMoves.add(0, new Move(newStartEquation, null, 0));
	}

	public void deleteMove(IMove iMove) {
		// can't delete a secondary move
		if (secondary1Moves.contains(iMove) || secondary2Moves.contains(iMove)) {
			throw new RuntimeException(
					"Can't replace move in the secondary solutions");
		}
		if (!(iMove instanceof Move)) {
			throw new RuntimeException("Can't delete a non-move " + iMove);
		}
		Move item = (Move) iMove;
		int index = primaryMoves.indexOf(item);
		List<IMove> subList = primaryMoves.subList(1, index + 1);
		List<IMove> toReapply = new ArrayList<IMove>(subList.subList(0,
				subList.size() - 1));
		Collections.reverse(toReapply);
		subList.clear();
		primaryMoves.remove(0);
		decrementMoveNumbers();
		primaryMoves.add(0, new Move(item.getEndEquation(), null, 0));
		for (IMove iEach : toReapply) {
			Move each = (Move) iEach;
			apply(each.getOperation(), false);
		}
	}

	public void replaceMove(Move item, Operation op) {
		// can't delete a secondary move
		if (secondary1Moves.contains(item) || secondary2Moves.contains(item)) {
			throw new RuntimeException(
					"Can't replace move in the secondary solutions");
		}
		int index = primaryMoves.indexOf(item);
		List<IMove> subList = primaryMoves.subList(1, index + 1);
		List<IMove> toReapply = new ArrayList<IMove>(subList.subList(0,
				subList.size() - 1));
		Collections.reverse(toReapply);
		subList.clear();
		primaryMoves.remove(0);
		primaryMoves.add(0, new Move(item.getEndEquation(), null, 0));
		apply(op);
		for (IMove iEach : toReapply) {
			Move each = (Move) iEach;
			apply(each.getOperation());
		}
	}

	public void replaceOperation(Operation operation, Operation newOperation) {
		// can't delete a secondary move
		if (usesOperationInSecondaries(operation)) {
			throw new RuntimeException(
					"Can't replace operation used in secondary solutions");
		}
		int index = operations.indexOf(operation);
		operations.add(index, newOperation);
		operations.remove(operation);
		checkOperatorAppliability();

		if (primaryMoves.size() <= 1) {
			return;
		}

		// revisit all the moves after the first use of the operation being
		// replaced
		Equation solvedEquation = new Equation(new Expression(1, 0),
				new Expression(Fraction.ZERO, solution));
		Move solvedMove = new Move(solvedEquation, null, 0);

		List<IMove> oldMoves = new ArrayList<IMove>(primaryMoves.subList(1,
				primaryMoves.size()));
		Collections.reverse(oldMoves);
		primaryMoves.clear();
		primaryMoves.add(solvedMove);
		for (IMove iEach : oldMoves) {
			Move each = (Move) iEach;
			Operation op = each.getOperation();
			if (!op.equals(operation)) {
				apply(op);
			} else {
				apply(newOperation);
			}
		}

	}

	protected LevelSolution getLevelSolution() {
		List<Integer> first = new ArrayList<Integer>();
		for (IMove iEach : primaryMoves) {
			if (!(iEach instanceof IMoveWithOperation))
				continue;

			IMoveWithOperation each = (IMoveWithOperation) iEach;
			if (each.getOperation() == null) {
				continue;
			}
			int indexOf = operations.indexOf(each.getOperation());
			if (indexOf == -1) {
				throw new RuntimeException("The custom level " + getId()
						+ " solution moves contains an invalid operation "
						+ each.getOperation());
			}
			first.add(indexOf);
		}

		if (secondarySolution == null || secondary1Moves.isEmpty()) {
			return new LevelSolution(first, getCurrentStartEquation(),
					getOperations());
		}
		List<Integer> secondary1 = new ArrayList<Integer>();
		addSecondaryOpIndices(secondary1, secondary1Moves);
		List<Integer> secondary2 = new ArrayList<Integer>();
		addSecondaryOpIndices(secondary2, secondary2Moves);

		List<Fraction> solutions = new ArrayList<Fraction>();
		solutions.add(solution);
		solutions.add(secondarySolution);
		return new LevelSolution(solutions, first, secondary1Moves.get(0)
				.getStartEquation(), secondary1, secondary2Moves.get(0)
				.getStartEquation(), secondary2);

	}

	private void addSecondaryOpIndices(List<Integer> secondary1,
			List<IMove> sourceMoves) {
		for (IMove iEach : sourceMoves) {
			if (!(iEach instanceof IMoveWithOperation))
				continue;

			IMoveWithOperation each = (IMoveWithOperation) iEach;
			Operation operation = each.getOperation();
			if (operation == null) {
				continue;
			}
			int indexOf = operations.indexOf(operation);
			if (indexOf == -1) {
				if (!operation.equals(Multiply.NEGATE)) {
					throw new RuntimeException("The custom level " + getId()
							+ " solution moves contains an invalid operation "
							+ operation);
				}
				indexOf = operations.indexOf(new SquareRoot());
				if (indexOf == -1) {
					throw new RuntimeException("The custom level " + getId()
							+ " solution moves contains an invalid operation "
							+ operation);
				}
			}
			secondary1.add(indexOf);
		}
	}

	public void replaceMoves(Solution result) {
		// TODO validate that the moves are valid for eg square root

		primaryMoves.clear();
		primaryMoves.addAll(result.primaryMoves);

		secondary1Moves.clear();
		if (result.secondary1Moves != null) {
			secondary1Moves.addAll(result.secondary1Moves);
		}

		secondary2Moves.clear();
		if (result.secondary2Moves != null) {
			secondary2Moves.addAll(result.secondary2Moves);
		}
	}

	public void load(long id) {
		CustomLevelDBReader reader = new CustomLevelDBReader();
		reader.read(FindXApp.getContext(), this, id);
	}

	public void save(FindXApp app) {
		CustomLevelDBWriter writer = new CustomLevelDBWriter();
		writer.write(app, FindXApp.getContext(), this);
	}

	public CustomLevel convertToLevel(Stage custom) {
		CustomLevel level = new CustomLevel(this, custom);
		return level;
	}

	public List<IMove> getRawMoves() {
		return primaryMoves;
	}

	private Set<Operation> disallowed = new HashSet<Operation>();

	public boolean isAppliable(Operation operation) {
		return !disallowed.contains(operation);
	}

	@Override
	protected void checkOperatorAppliability() {
		disallowed.clear();
		for (Operation each : operations) {
			if (!each.isAppliableWith(operations)) {
				disallowed.add(each);
			}
		}
	}

	public void addOperations(List<Operation> theOperations) {
		getOperations().addAll(theOperations);
		checkOperatorAppliability();
	}

	public Document asXMLDoc(String title, String author)
			throws ParserConfigurationException {
		List<Operation> operations = getOperations();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		final Document doc = docBuilder.newDocument();
		Element root = doc.createElement("l2");
		doc.appendChild(root);

		Element titleNode = doc.createElement("t");
		XMLUtils.setTextContent(titleNode, title);
		root.appendChild(titleNode);

		Element authorNode = doc.createElement("a");
		XMLUtils.setTextContent(authorNode, author);
		root.appendChild(authorNode);

		Element equationNode = doc.createElement("e");
		root.appendChild(equationNode);
		Element lhsNode = doc.createElement("l");
		equationNode.appendChild(lhsNode);
		appendExpression(doc, lhsNode, getCurrentStartEquation().getLhs());
		Element rhsNode = doc.createElement("r");
		equationNode.appendChild(rhsNode);
		appendExpression(doc, rhsNode, getCurrentStartEquation().getRhs());

		for (Operation each : operations) {
			final Element op = doc.createElement("o");
			root.appendChild(op);
			Element typeNode = doc.createElement("t");
			op.appendChild(typeNode);
			XMLUtils.setTextContent(typeNode, each.type().toString());
			OperationVisitor visitor = new OperationVisitor() {

				@Override
				public void visitWild(WildCard wild) {
					Element typeNode = doc.createElement("wt");
					op.appendChild(typeNode);
					XMLUtils.setTextContent(typeNode, wild.getActual().type()
							.toString());
					wild.getActual().accept(this);
				}

				@Override
				public void visitSwap(Swap swap) {
				}

				@Override
				public void visitSquare(Square square) {
				}

				@Override
				public void visitSquareRoot(SquareRoot squareRoot) {
				}

				@Override
				public void visitFactor(Factor factor) {
					Expression expression = factor.getExpression();
					appendExpression(doc, op, expression);
				}

				@Override
				public void visitDefactor(Defactor defactor) {
					Expression expression = defactor.getExpression();
					appendExpression(doc, op, expression);
				}

				@Override
				public void visitSubtract(Subtract sub) {
					Expression expression = sub.getExpression();
					appendExpression(doc, op, expression);
				}

				@Override
				public void visitAdd(Add add) {
					Expression expression = add.getExpression();
					appendExpression(doc, op, expression);
				}

				@Override
				public void visitMultiply(Multiply multiply) {
					Fraction factor = multiply.getFactor();
					appendFraction(doc, op, factor);
				}

				@Override
				public void visitDivide(Divide divide) {
					Fraction factor = divide.getFactor();
					appendFraction(doc, op, factor);
				}

			};
			each.accept(visitor);
		}

		List<IMove> primaryMoves = getPrimaryMoves();
		appendMoves(doc, root, operations, primaryMoves, "m");
		List<IMove> secondary1Moves = getSecondary1Moves();
		appendMoves(doc, root, operations, secondary1Moves, "m1");
		List<IMove> secondary2Moves = getSecondary1Moves();
		appendMoves(doc, root, operations, secondary2Moves, "m2");

		Element sol = doc.createElement("s");
		root.appendChild(sol);
		XMLUtils.setTextContent(sol, getSolution().toString());

		if (getSecondarySolution() != null) {
			Element sol2 = doc.createElement("s2");
			root.appendChild(sol2);
			XMLUtils.setTextContent(sol2, getSecondarySolution().toString());
		}
		return doc;
	}

	private void appendMoves(final Document doc, Element root,
			List<Operation> operations, List<IMove> primaryMoves, String tagName) {
		for (IMove iEach : primaryMoves) {
			// TODO
			if (!(iEach instanceof IMoveWithOperation)) {
				continue;
			}
			IMoveWithOperation each = (IMoveWithOperation) iEach;
			Operation operation = each.getOperation();
			if (operation == null)
				continue;
			int opIndex = operations.indexOf(operation);
			if (opIndex < 0 && operation == Multiply.NEGATE) {
				opIndex = operations.indexOf(new SquareRoot());
			}
			if (opIndex < 0) {
				throw new RuntimeException("Error finding index for operation "
						+ operation);
			}
			Element op = doc.createElement(tagName);
			root.appendChild(op);
			XMLUtils.setTextContent(op, Integer.toString(opIndex));
		}
	}

	private void appendExpression(Document doc, Element op,
			Expression expression) {
		Element x2 = doc.createElement("x2");
		Element x = doc.createElement("x");
		Element constant = doc.createElement("c");
		op.appendChild(x2);
		op.appendChild(x);
		op.appendChild(constant);
		XMLUtils.setTextContent(x2, expression.getX2Coefficient().toString());
		XMLUtils.setTextContent(x, expression.getXCoefficient().toString());
		XMLUtils.setTextContent(constant, expression.getConstant().toString());
	}

	private void appendFraction(Document doc, Element op, Fraction factor) {
		Element constant = doc.createElement("c");
		op.appendChild(constant);
		XMLUtils.setTextContent(constant, factor.toString());
	}

	public void read(Document doc) {
		Element root = doc.getDocumentElement();
		if (root.getNodeName().equals("l")) {
			readOld(doc);
			return;
		}
		String title = XMLUtils.getTextContent(XMLUtils.getChildElementByName(
				root, "t"));
		String author = XMLUtils.getTextContent(XMLUtils.getChildElementByName(
				root, "a"));

		setTitle(title);
		setAuthor(author);
		setIsImported(true);

		Fraction solution = readFraction(root, "s", false);
		setSolution(solution);
		Fraction solution2 = readFraction(root, "s2", true);
		if (solution2 != null) {
			setSolution2(solution2);
		}

		Element equationNode = XMLUtils.getChildElementByName(root, "e");
		Element lhsNode = XMLUtils.getChildElementByName(equationNode, "l");
		Expression lhs = readExpression(lhsNode);
		Element rhsNode = XMLUtils.getChildElementByName(equationNode, "r");
		Expression rhs = readExpression(rhsNode);
		Equation equation = new Equation(lhs, rhs);

		readOperations(root);
		readMoves(root, equation);

	}

	private void readMoves(Element root, Equation startEquation) {
		List<Integer> primaryMoveOpIds = readMoveOperationIds(root, "m");
		List<Integer> secondary1MoveOpIds = readMoveOperationIds(root, "m1");
		List<Integer> secondary2MoveOpIds = readMoveOperationIds(root, "m2");

		try {
			CustomLevelDBReader.populateBuilderMovesFromOperationIndices(this,
					startEquation, primaryMoveOpIds, secondary1MoveOpIds,
					secondary2MoveOpIds);
		} catch (Exception e) {
			Log.e("CustomLevelDBReader", "Error reading moves from level ", e);
		}
	}

	private List<Integer> readMoveOperationIds(Element root, String elemName) {
		List<Element> moves = XMLUtils.getChildElementsByName(root, elemName);
		List<Integer> result = new ArrayList<Integer>(moves.size());
		for (Element each : moves) {
			String text = XMLUtils.getTextContent(each);
			result.add(Integer.parseInt(text));
		}
		return result;
	}

	private void readOperations(Element root) {
		List<Element> operationElems = XMLUtils.getChildElementsByName(root,
				"o");
		for (Element each : operationElems) {
			String typeString = XMLUtils.getTextContent(XMLUtils
					.getChildElementByName(each, "t"));
			OperationType type = OperationType.valueOf(typeString);

			WildCard wildOp = null;
			if (type == OperationType.WILD) {
				String wildTypeString = XMLUtils.getTextContent(XMLUtils
						.getChildElementByName(each, "wt"));
				OperationType wildType = OperationType.valueOf(wildTypeString);
				wildOp = new WildCard();
				type = wildType;
			}

			Operation op = null;
			switch (type) {
			case ADD:
				op = new Add(readExpression(each));
				break;
			case SUBTRACT:
				op = new Subtract(readExpression(each));
				break;
			case MULTIPLY:
				op = new Multiply(readFactor(each));
				break;
			case DIVIDE:
				op = new Divide(readFactor(each));
				break;
			case SWAP:
				op = new Swap();
				break;
			case SQUARE_ROOT:
				op = new SquareRoot();
				break;
			default:
			}
			if (wildOp != null) {
				wildOp.setActual(op);
				op = wildOp;
			}
			addOperation(op);
		}
	}

	private void readOld(Document doc) {
		Element root = doc.getDocumentElement();

		String title = XMLUtils.getTextContent(XMLUtils.getChildElementByName(
				root, "t"));
		String author = XMLUtils.getTextContent(XMLUtils.getChildElementByName(
				root, "a"));

		List<Element> operationElems = XMLUtils.getChildElementsByName(root,
				"o");
		List<Operation> operations = new ArrayList<Operation>();
		for (Element each : operationElems) {
			String typeString = XMLUtils.getTextContent(XMLUtils
					.getChildElementByName(each, "t"));
			OperationType type = OperationType.valueOf(typeString);

			Operation op = null;
			switch (type) {
			case ADD:
				op = new Add(readExpression(each));
				break;
			case SUBTRACT:
				op = new Subtract(readExpression(each));
				break;
			case MULTIPLY:
				op = new Multiply(readFactor(each));
				break;
			case DIVIDE:
				op = new Divide(readFactor(each));
				break;
			case SWAP:
				op = new Swap();
				break;
			default:
			}
			operations.add(op);
		}
		List<Element> moveElems = XMLUtils.getChildElementsByName(root, "m");
		List<Operation> moveOperations = new ArrayList<Operation>();
		for (Element each : moveElems) {
			String indexString = XMLUtils.getTextContent(each);
			int index = Integer.parseInt(indexString);
			moveOperations.add(operations.get(index));
		}
		Fraction solution = readFraction(root, "s", false);

		setIsImported(true);
		setTitle(title);
		setAuthor(author);
		setSolution(solution);
		addOperations(operations);

		Collections.reverse(moveOperations);

		for (Operation each : moveOperations) {
			apply(each);
		}
	}

	private FractionFormat format = new FractionFormat();

	private Expression readExpression(Element each) {
		return new Expression(readFraction(each, "x2", false), readFraction(
				each, "x", false), readFraction(each, "c", false));
	}

	private Fraction readFactor(Element each) {
		return readFraction(each, "c", false);
	}

	private Fraction readFraction(Element each, String elemName,
			boolean returnNull) {
		Element node = XMLUtils.getChildElementByName(each, elemName);
		if (node == null) {
			if (returnNull) {
				return null;
			}
			return Fraction.ZERO;
		}
		String c = XMLUtils.getTextContent(node);
		return format.parse(c);
	}

}
