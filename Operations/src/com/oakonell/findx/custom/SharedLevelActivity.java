package com.oakonell.findx.custom;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
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
import com.oakonell.findx.FindXApp;
import com.oakonell.findx.PuzzleActivity;
import com.oakonell.findx.R;
import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.custom.model.ICustomLevel;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Operation;
import com.oakonell.utils.Utils;

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

		final Button importButton = (Button) findViewById(R.id.import_level);

		final CustomLevelBuilder builder = new CustomLevelBuilder();

		Uri uri = intent.getData();
		String path = uri.getPath();
		String pathPrefix = "findx/share/";
		int indexOf = path.indexOf(pathPrefix);
		try {
			String xmlString;
			if (indexOf > 0) {
				String encodedString = path.substring(indexOf
						+ pathPrefix.length());
				xmlString = decode(encodedString);

			} else {
				indexOf = path.indexOf("findx/custom");
				if (indexOf < 0) {
					throw new RuntimeException(
							"Invalid Url for SharedLevelActivity '" + uri + "'");
				}
				xmlString = decode(uri.getQueryParameter("level"));
			}
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(new InputSource(new StringReader(xmlString)));
			builder.defaultMaxSequence();
			builder.read(doc);
		} catch (Exception e) {
			importButton.setEnabled(false);
			((TextView) findViewById(R.id.error_text))
					.setText("Error importing:" + e.getLocalizedMessage());
		}

		EditText titleView = (EditText) findViewById(R.id.title);
		titleView.setText(builder.getTitle());

		TextView authorView = (TextView) findViewById(R.id.author);
		authorView.setText(builder.getAuthor());

		titleView.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence charsequence, int i, int j,
					int k) {
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
		equationView.setText(builder.getCurrentStartEquation().toString());

		TextView numMoves = (TextView) findViewById(R.id.num_moves);
		numMoves.setText((builder.getNumMoves()) + "");

		TextView op1View = (TextView) findViewById(R.id.op1);
		TextView op2View = (TextView) findViewById(R.id.op2);
		TextView op3View = (TextView) findViewById(R.id.op3);
		TextView op4View = (TextView) findViewById(R.id.op4);
		TextView op5View = (TextView) findViewById(R.id.op5);
		TextView op6View = (TextView) findViewById(R.id.op6);

		List<Operation> operations = builder.getOperations();
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
		if (numOps > 5) {
			op6View.setText(operations.get(5).toString());
			op6View.setVisibility(View.VISIBLE);
		}

		importButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				builder.save((FindXApp) getApplication());
				Levels.resetCustomStage();
				ICustomLevel level = Levels.getCustomStage().getLevelByDBId(
						builder.getId());
				startPuzzle(level.getId());

				finish();
			}
		});

	}

	public static String decode(String encodedString) throws IOException {
		byte[] bytes = Base64.decode(encodedString, Base64.URL_SAFE
				| Base64.NO_WRAP);
		String xmlString = Utils.decompress(bytes);
		return xmlString;
	}

	private void startPuzzle(final String levelId) {
		Intent levelIntent = new Intent(SharedLevelActivity.this,
				PuzzleActivity.class);
		levelIntent.putExtra(PuzzleActivity.PUZZLE_ID, levelId);
		levelIntent.putExtra(PuzzleActivity.IS_CUSTOM, true);
		startActivity(levelIntent);
	}

}
