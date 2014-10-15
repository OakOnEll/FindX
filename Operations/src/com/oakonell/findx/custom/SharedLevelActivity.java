package com.oakonell.findx.custom;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.oakonell.findx.PuzzleActivity;
import com.oakonell.findx.R;
import com.oakonell.findx.custom.model.CustomLevel;
import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.Operation.OperationType;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.Divide;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.Subtract;
import com.oakonell.findx.model.ops.Swap;
import com.oakonell.utils.Utils;
import com.oakonell.utils.xml.XMLUtils;

public class SharedLevelActivity extends SherlockFragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_shared_level);

		final ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(false);
		ab.setDisplayUseLogoEnabled(true);
		ab.setDisplayShowTitleEnabled(true);

		Intent intent = getIntent();

		Uri uri = intent.getData();
		String path = uri.getPath();
		String pathPrefix = "findx/share/";
		int indexOf = path.indexOf(pathPrefix);
		try {
			String encodedString = path
					.substring(indexOf + pathPrefix.length());
			byte[] bytes = Base64.decode(encodedString, Base64.URL_SAFE
					| Base64.NO_WRAP);
			String xmlString = Utils.decompress(bytes);

			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(new InputSource(new StringReader(xmlString)));
			Element root = doc.getDocumentElement();
			String title = XMLUtils.getTextContent(XMLUtils
					.getChildElementByName(root, "t"));
			String author = XMLUtils.getTextContent(XMLUtils
					.getChildElementByName(root, "a"));
			List<Element> operationElems = XMLUtils.getChildElementsByName(
					root, "o");
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
			List<Element> moveElems = XMLUtils
					.getChildElementsByName(root, "m");
			List<Operation> moveOperations = new ArrayList<Operation>();
			for (Element each : moveElems) {
				String indexString = XMLUtils.getTextContent(each);
				int index = Integer.parseInt(indexString);
				moveOperations.add(operations.get(index));
			}
			Fraction solution = readFraction(root, "s");

			final CustomLevelBuilder builder = new CustomLevelBuilder();
			builder.setIsImported(true);
			builder.setTitle(title);
			builder.setAuthor(author);
			builder.setSolution(solution);
			builder.getOperations().addAll(operations);
			builder.defaultMaxSequence();

			Collections.reverse(moveOperations);

			// Foo 8 moves: divide by 3, add 1, add 33, add 33, add 33
			for (Operation each : moveOperations) {
				builder.apply(each);
			}

			EditText titleView = (EditText) findViewById(R.id.title);
			titleView.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence charsequence, int i,
						int j, int k) {
				}

				@Override
				public void beforeTextChanged(CharSequence charsequence, int i,
						int j, int k) {
				}

				@Override
				public void afterTextChanged(Editable editable) {
					builder.setTitle(editable.toString());
				}
			});
			TextView equationView = (TextView) findViewById(R.id.equation);
			TextView numMoves = (TextView) findViewById(R.id.num_moves);

			TextView op1View = (TextView) findViewById(R.id.op1);
			TextView op2View = (TextView) findViewById(R.id.op2);
			TextView op3View = (TextView) findViewById(R.id.op3);
			TextView op4View = (TextView) findViewById(R.id.op4);
			TextView op5View = (TextView) findViewById(R.id.op5);

			TextView authorView = (TextView) findViewById(R.id.author);
			authorView.setText(author);
			titleView.setText(title);
			equationView.setText(builder.getMoves().get(0).getStartEquation()
					.toString());
			numMoves.setText((builder.getMoves().size() - 1) + "");

			int numOps = operations.size();
			op1View.setText(operations.get(0).toString());
			if (numOps > 1) {
				op2View.setText(operations.get(1).toString());
				op2View.setVisibility(View.VISIBLE);
			}
			if (numOps > 2) {
				op3View.setText(operations.get(2).toString());
				op3View.setVisibility(View.VISIBLE);
			}
			if (numOps > 3) {
				op4View.setText(operations.get(3).toString());
				op4View.setVisibility(View.VISIBLE);
			}
			if (numOps > 4) {
				op5View.setText(operations.get(4).toString());
				op5View.setVisibility(View.VISIBLE);
			}

			Button importButton = (Button) findViewById(R.id.import_level);
			importButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					builder.save();
					Levels.resetCustomStage();
					CustomLevel level = Levels.getCustomStage().getLevelByDBId(
							builder.getId());
					startPuzzle(level.getId());

					finish();
				}
			});

		} catch (Exception e) {
			((TextView) findViewById(R.id.title)).setText(e
					.getLocalizedMessage());
		}
	}

	private FractionFormat format = new FractionFormat();

	private Expression readExpression(Element each) {
		return new Expression(readFraction(each, "x"), readFraction(each, "c"));
	}

	private Fraction readFactor(Element each) {
		return readFraction(each, "c");
	}

	private Fraction readFraction(Element each, String elemName) {
		String c = XMLUtils.getTextContent(each, elemName);
		return format.parse(c);
	}

	private void startPuzzle(final String levelId) {
		Intent levelIntent = new Intent(SharedLevelActivity.this,
				PuzzleActivity.class);
		levelIntent.putExtra(PuzzleActivity.PUZZLE_ID, levelId);
		levelIntent.putExtra(PuzzleActivity.IS_CUSTOM, true);
		startActivity(levelIntent);
	}

}
