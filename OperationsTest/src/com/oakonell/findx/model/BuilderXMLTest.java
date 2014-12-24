package com.oakonell.findx.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.apache.commons.math3.fraction.Fraction;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import android.util.Base64;

import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.SquareRoot;
import com.oakonell.findx.model.ops.WildCard;
import com.oakonell.utils.xml.XMLUtils;

public class BuilderXMLTest extends TestCase {

	public void testFromXML() throws Exception {
		String encoded = "H4sIAAAAAAAAALPJseOyKbFLS8xLrlQoyUgssdEvAYok2gXlJ9noJwKZ-WAFLp5hni6uEMlkO0Mb_WQgQz8fLu_o4gKRrABJVoBV6ZriU6aLUGdohKQwF2RALphhhItRDFJTDNKRYwcABlMbhcIAAAA=";

		String xmlString = decode(encoded);

		System.out.println(xmlString);
		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
				.parse(new InputSource(new StringReader(xmlString)));

		CustomLevelBuilder builder = new CustomLevelBuilder();
		builder.read(doc);
		assertBuilderSucceeds(builder);
		
	}

	public void testWildToXML() throws ParserConfigurationException {
		CustomLevelBuilder builder = new CustomLevelBuilder();
		List<Operation> operations = builder.getOperations();

		List<IMove> moves = builder.getMoves();
		assertEquals(1, moves.size());
		assertNull(((Move) moves.get(0)).getOperation());
		assertEquals(0, builder.getNumMoves());

		Operation add = new WildCard(new Add(new Expression(5)));
		operations.add(add);

		Add add2 = new Add(new Expression(7));
		operations.add(add2);

		builder.apply(add);

		Document doc = builder.asXMLDoc("title", "author");
		System.out.println(XMLUtils.xmlDocumentToString(doc));

		CustomLevelBuilder newBuilder = new CustomLevelBuilder();
		newBuilder.read(doc);

		assertEquals("title", newBuilder.getTitle());
		assertEquals("author", newBuilder.getAuthor());

		assertBuilderSucceeds(newBuilder);
	}

	public void testSimpleToXML() throws ParserConfigurationException {
		CustomLevelBuilder builder = new CustomLevelBuilder();
		List<Operation> operations = builder.getOperations();

		List<IMove> moves = builder.getMoves();
		assertEquals(1, moves.size());
		assertNull(((Move) moves.get(0)).getOperation());
		assertEquals(0, builder.getNumMoves());

		Add add = new Add(new Expression(5));
		operations.add(add);

		Add add2 = new Add(new Expression(7));
		operations.add(add2);

		builder.apply(add);

		Document doc = builder.asXMLDoc("title", "author");
		System.out.println(XMLUtils.xmlDocumentToString(doc));

		CustomLevelBuilder newBuilder = new CustomLevelBuilder();
		newBuilder.read(doc);

		assertEquals("title", newBuilder.getTitle());
		assertEquals("author", newBuilder.getAuthor());

		assertBuilderSucceeds(newBuilder);
	}

	private void assertBuilderSucceeds(CustomLevelBuilder newBuilder) {
		Equation equation = newBuilder.getCurrentStartEquation();

		List<IMove> primaryMoves = newBuilder.getPrimaryMoves();
		MoveResult moveResult = null;
		for (IMove iEach : primaryMoves) {
			if (!(iEach instanceof IMoveWithOperation))
				continue;
			IMoveWithOperation each = (IMoveWithOperation) iEach;
			Operation op = each.getOperation();
			if (op == null)
				continue;

			System.out.println(equation + ": " + op);
			moveResult = op.applyMove(equation, 0, newBuilder.getOperations(),
					null);
			equation = moveResult.getPrimaryEndEquation();
		}
		if (equation != null && equation.isSolved()) {
			if (newBuilder.getSecondarySolution() != null) {
				fail(newBuilder.getTitle() + " has too many solutions");
			}
			return;
		}

		List<Operation> operations = new ArrayList<Operation>(
				newBuilder.getOperations());
		Multiply multiplyNegOne = Multiply.NEGATE;
		Collections.replaceAll(operations, new SquareRoot(), multiplyNegOne);
		for (Operation op : newBuilder.getOperations()) {
			if (!(op instanceof WildCard))
				continue;
			WildCard wild = (WildCard) op;
			if (wild.getActual() instanceof SquareRoot) {
				Collections.replaceAll(operations, op, multiplyNegOne);
			}
		}

		Equation secondEquation = moveResult.getSecondary1().getStartEquation();
		equation = secondEquation;
		List<IMove> secondOps;
		List<IMove> ops;
		// if
		// (secondEquation.equals(newBuilder.getSolution()levelSolution.getSecondaryEquation1()))
		// {
		ops = newBuilder.getSecondary1Moves();
		secondOps = newBuilder.getSecondary2Moves();
		// assertEquals("Level " + newBuilder.getTitle() + " " +
		// " can't find secondary solutions",
		// levelSolution.getSecondaryEquation2(), moveResult
		// .getSecondary2().getStartEquation());
		// } else {
		// ops = levelSolution.getSecondaryOperations2();
		// secondOps = levelSolution.getSecondaryOperations1();
		// assertEquals("Level " + each.getId() + " " + each.getName()
		// + " can't find secondary solutions",
		// levelSolution.getSecondaryEquation1(), secondEquation);
		// }
		for (IMove iEach : ops) {
			if (!(iEach instanceof IMoveWithOperation))
				continue;
			IMoveWithOperation each = (IMoveWithOperation) iEach;
			Operation op = each.getOperation();
			if (op == null)
				continue;
			System.out.println(equation + ": " + op);
			equation = op.apply(equation);
		}
		assertTrue("Level " + newBuilder.getTitle() + " "
				+ " is not solved by stored solution - end equation "
				+ equation, equation.isSolved());
		assertSolutionInLevel(equation.getRhs().getConstant(), newBuilder);

		equation = moveResult.getSecondary2().getStartEquation();
		for (IMove iEach : secondOps) {
			if (!(iEach instanceof IMoveWithOperation))
				continue;
			IMoveWithOperation each = (IMoveWithOperation) iEach;
			Operation op = each.getOperation();
			if (op == null)
				continue;
			System.out.println(equation + ": " + op);
			equation = op.apply(equation);
		}
		assertTrue(
				"Level " + newBuilder.getTitle() + " "
						+ " is not solved by stored solution- end equation "
						+ equation, equation.isSolved());
		assertSolutionInLevel(equation.getRhs().getConstant(), newBuilder);

	}

	private void assertSolutionInLevel(Fraction constant,
			CustomLevelBuilder newBuilder) {
		if (newBuilder.getSolution().compareTo(constant) == 0)
			return;
		if (newBuilder.getSecondarySolution() != null) {
			if (newBuilder.getSecondarySolution().compareTo(constant) == 0)
				return;
		}
		fail("Solution " + constant + " is not in solutions: "
				+ newBuilder.getSolution() + ", "
				+ newBuilder.getSecondarySolution());
	}

	public void testSquareRootToXML() throws ParserConfigurationException {
		CustomLevelBuilder builder = new CustomLevelBuilder();
		SquareRoot squareRoot = new SquareRoot();
		Add add3 = new Add(new Expression(3));
		Add add5 = new Add(new Expression(5));
		builder.addOperation(squareRoot);
		builder.addOperation(add3);
		builder.addOperation(add5);

		builder.apply(add3);
		builder.apply(squareRoot);
		builder.apply(add3);
		builder.apply(add5);

		Equation equation = builder.getCurrentStartEquation();

		List<IMove> moves = builder.getMoves();
		assertEquals(8, moves.size());
		Move start = (Move) moves.get(0);
		Move moveAdd5 = (Move) moves.get(1);
		Move moveAdd3 = (Move) moves.get(2);
		IMove moveSquareRoot = moves.get(3);
		IMove sol1 = moves.get(4);
		Move move2Add3 = (Move) moves.get(5);
		IMove sol2 = moves.get(6);
		Move move3Add3 = (Move) moves.get(7);

		builder.moveDown(moveAdd5);
		assertEquals(8, moves.size());
		start = (Move) moves.get(0);
		moveAdd3 = (Move) moves.get(1);
		moveAdd5 = (Move) moves.get(2);
		moveSquareRoot = moves.get(3);
		sol1 = moves.get(4);
		move2Add3 = (Move) moves.get(5);
		sol2 = moves.get(6);
		move3Add3 = (Move) moves.get(7);

		builder.moveUp(moveAdd5);
		start = (Move) moves.get(0);
		moveAdd5 = (Move) moves.get(1);
		moveAdd3 = (Move) moves.get(2);
		moveSquareRoot = moves.get(3);
		sol1 = moves.get(4);
		move2Add3 = (Move) moves.get(5);
		sol2 = moves.get(6);
		move3Add3 = (Move) moves.get(7);

		builder.removeOperation(add5);
		assertEquals(7, moves.size());
		assertEquals(4, builder.getNumMoves());
		start = (Move) moves.get(0);
		moveAdd3 = (Move) moves.get(1);
		moveSquareRoot = moves.get(2);
		sol1 = moves.get(3);
		move2Add3 = (Move) moves.get(4);
		sol2 = moves.get(5);
		move3Add3 = (Move) moves.get(6);

		builder.addOperation(add5);
		builder.apply(add5);
		start = (Move) moves.get(0);
		moveAdd5 = (Move) moves.get(1);
		moveAdd3 = (Move) moves.get(2);
		moveSquareRoot = moves.get(3);
		sol1 = moves.get(4);
		move2Add3 = (Move) moves.get(5);
		sol2 = moves.get(6);
		move3Add3 = (Move) moves.get(7);

		builder.deleteMove(moveAdd5);

		start = (Move) moves.get(0);
		moveAdd3 = (Move) moves.get(1);
		moveSquareRoot = moves.get(2);
		sol1 = moves.get(3);
		move2Add3 = (Move) moves.get(4);
		sol2 = moves.get(5);
		move3Add3 = (Move) moves.get(6);

		Document doc = builder.asXMLDoc("title", "author");
		System.out.println(XMLUtils.xmlDocumentToString(doc));

		CustomLevelBuilder newBuilder = new CustomLevelBuilder();
		newBuilder.read(doc);

		assertEquals("title", newBuilder.getTitle());
		assertEquals("author", newBuilder.getAuthor());

		assertBuilderSucceeds(newBuilder);
	}

	public static String decode(String encodedString) throws IOException {
		byte[] bytes = Base64.decode(encodedString, Base64.URL_SAFE
				| Base64.NO_WRAP);
		String xmlString = decompress(bytes);
		return xmlString;
	}

	public static String decompress(byte[] compressed) throws IOException {
		final int BUFFER_SIZE = 32;
		ByteArrayInputStream is = new ByteArrayInputStream(compressed);
		GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
		StringBuilder string = new StringBuilder();
		byte[] data = new byte[BUFFER_SIZE];
		int bytesRead;
		while ((bytesRead = gis.read(data)) != -1) {
			string.append(new String(data, 0, bytesRead));
		}
		gis.close();
		is.close();
		return string.toString();
	}
}
