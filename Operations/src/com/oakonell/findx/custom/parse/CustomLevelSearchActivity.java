package com.oakonell.findx.custom.parse;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.oakonell.findx.PuzzleActivity;
import com.oakonell.findx.R;
import com.oakonell.findx.custom.parse.ParseConnectivity.ParseUserExtra;
import com.oakonell.findx.custom.parse.ParseLevelHelper.ParseCustomLevel;
import com.oakonell.utils.StringUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class CustomLevelSearchActivity extends ListActivity {
	private static final SortCriteria SORT_BY_CREATION_DATE = new SortCriteria(
			2, "Creation date");
	private static final SortCriteria SORT_BY_RATING = new SortCriteria(1,
			"Average Rating");
	private ParseCustomLevelSearchAdapter adapter;
	private List<ParseObject> levels;
	private Dialog progressDialog;
	private String filter;
	SortCriteria sort = SORT_BY_CREATION_DATE;

	private static class SortCriteria {
		final int val;
		final String description;

		SortCriteria(int val, String description) {
			this.val = val;
			this.description = description;
		}

		@Override
		public String toString() {
			return description;
		}

	}

	private class RemoteDataTask extends AsyncTask<Void, Void, Void> {
		// Override this method to do custom remote calls
		protected Void doInBackground(Void... params) {
			// Gets the current list of todos in sorted order
			ParseQuery<ParseObject> query;
			// whereMatches may slow down on large data sets..
			// might be better to store a redundant canonical uppercase value for each field bing used in case insensitive searches
			if (!StringUtils.isEmpty(filter)) {
				ParseQuery<ParseUser> query1 = ParseUser.getQuery();
				// query1.whereContains(ParseUserExtra.nickname_field, filter);
				query1.whereMatches(ParseUserExtra.nickname_field, "(" + filter
						+ ")", "i");
				ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>(
						ParseCustomLevel.classname);
				query2.whereMatchesQuery(ParseCustomLevel.createdBy_field,
						query1);

				ParseQuery<ParseObject> query3 = new ParseQuery<ParseObject>(
						ParseCustomLevel.classname);
				// query3.whereContains(ParseCustomLevel.title_field, filter);
				query3.whereMatches(ParseCustomLevel.title_field, "(" + filter
						+ ")", "i");

				List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
				queries.add(query2);
				queries.add(query3);

				query = ParseQuery.or(queries);
			} else {
				query = new ParseQuery<ParseObject>(ParseCustomLevel.classname);
			}

			// TODO use a constant for create at
			if (sort == SORT_BY_CREATION_DATE) {
				query.orderByDescending("_created_at");
			} else if (sort == SORT_BY_RATING) {
				query.orderByDescending(ParseCustomLevel.avg_rating_field);
			} else {
				throw new RuntimeException("Unexpected sort");
			}
			query.include(ParseCustomLevel.createdBy_field);

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

		ImageButton searchButton = (ImageButton) findViewById(R.id.search);
		final EditText searchText = (EditText) findViewById(R.id.search_text);
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				filter = searchText.getText().toString();
				adapter.clear();
				new RemoteDataTask().execute();
			}
		});

		Spinner sortBySpinner = (Spinner) findViewById(R.id.sort_by);

		List<SortCriteria> list = new ArrayList<SortCriteria>();

		list.add(SORT_BY_CREATION_DATE);
		list.add(SORT_BY_RATING);

		ArrayAdapter<SortCriteria> dataAdapter = new ArrayAdapter<SortCriteria>(
				this, android.R.layout.simple_spinner_item, list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		sortBySpinner.setAdapter(dataAdapter);
		sortBySpinner.setSelection(0);

		sortBySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				SortCriteria newSort = (SortCriteria) parent
						.getItemAtPosition(pos);
				if (newSort == sort) {
					return;
				}
				sort = newSort;
				filter = searchText.getText().toString();
				adapter.clear();
				new RemoteDataTask().execute();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// do nothing
			}

		});

		registerForContextMenu(getListView());
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (adapter.isEmpty()) {
			// otherwise, use the existing data
			new RemoteDataTask().execute();
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, final int position,
			long id) {
		super.onListItemClick(l, v, position, id);
		// TODO this should bring up the detailed view on the level
		// show operations, comments, and modifiable rating bar
		ParseObject level = levels.get(position);

		Intent levelIntent = new Intent(CustomLevelSearchActivity.this,
				CustomLevelDetailActivity.class);
		levelIntent.putExtra(CustomLevelDetailActivity.LEVEL_PARSE_ID,
				level.getObjectId());
		startActivity(levelIntent);
		// TODO deal with update to this record
	}

	private void startPuzzle(final String levelId) {
		Intent levelIntent = new Intent(CustomLevelSearchActivity.this,
				PuzzleActivity.class);
		levelIntent.putExtra(PuzzleActivity.PUZZLE_ID, levelId);
		levelIntent.putExtra(PuzzleActivity.IS_CUSTOM, true);
		startActivity(levelIntent);
	}
}
