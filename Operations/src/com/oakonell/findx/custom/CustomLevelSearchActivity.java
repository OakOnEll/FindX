package com.oakonell.findx.custom;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.oakonell.findx.PuzzleActivity;
import com.oakonell.findx.R;
import com.oakonell.findx.custom.model.CustomLevel;
import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.model.Levels;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class CustomLevelSearchActivity extends ListActivity {
	private ParseCustomLevelSearchAdapter adapter;
	private List<ParseObject> levels;
	private Dialog progressDialog;

	private class RemoteDataTask extends AsyncTask<Void, Void, Void> {
		// Override this method to do custom remote calls
		protected Void doInBackground(Void... params) {
			// Gets the current list of todos in sorted order
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
					"CustomLevel");
			query.orderByDescending("_created_at");
			query.include("createdBy");

			try {
				levels = query.find();
			} catch (ParseException e) {

			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			CustomLevelSearchActivity.this.progressDialog = ProgressDialog
					.show(CustomLevelSearchActivity.this, "", "Loading...",
							true);
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void result) {
			// Put the list of todos into the list view
			if (levels != null) {
				for (ParseObject level : levels) {
					adapter.add(level);
				}
			}
			adapter.notifyDataSetChanged();
			CustomLevelSearchActivity.this.progressDialog.dismiss();
			TextView empty = (TextView) findViewById(android.R.id.empty);
			if (levels.isEmpty()) {
				empty.setVisibility(View.VISIBLE);
			} else {
				empty.setVisibility(View.INVISIBLE);
			}

		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		levels = new ArrayList<ParseObject>();
		adapter = new ParseCustomLevelSearchAdapter(this, levels);
		setListAdapter(adapter);
		setContentView(R.layout.custom_level_search);

		TextView empty = (TextView) findViewById(android.R.id.empty);
		empty.setVisibility(View.INVISIBLE);

		new RemoteDataTask().execute();
		registerForContextMenu(getListView());
	}

	@Override
	protected void onListItemClick(ListView l, View v, final int position,
			long id) {
		super.onListItemClick(l, v, position, id);
		// TODO this should bring up the detailed view on the level
		//    show operations, comments, and modifiable rating bar

		final ProgressDialog dialog = ProgressDialog.show(this,
				"Loading level", "Please wait...");
		AsyncTask<Void, Void, CustomLevelBuilder> task = new AsyncTask<Void, Void, CustomLevelBuilder>() {
			@Override
			protected CustomLevelBuilder doInBackground(Void... params) {
				ParseObject level = levels.get(position);

				CustomLevelBuilder builder = ParseLevelHelper.load(level);

				builder.save();
				Levels.resetCustomStage();
				return builder;
			}

			@Override
			protected void onPostExecute(CustomLevelBuilder builder) {
				dialog.dismiss();
				finish();
				CustomLevel newlevel = Levels.getCustomStage().getLevelByDBId(
						builder.getId());
				finish();
				startPuzzle(newlevel.getId());
			}
		};
		task.execute();
	}

	private void startPuzzle(final String levelId) {
		Intent levelIntent = new Intent(CustomLevelSearchActivity.this,
				PuzzleActivity.class);
		levelIntent.putExtra(PuzzleActivity.PUZZLE_ID, levelId);
		levelIntent.putExtra(PuzzleActivity.IS_CUSTOM, true);
		startActivity(levelIntent);
	}
}
