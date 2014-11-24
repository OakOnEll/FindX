package com.oakonell.findx.custom.parse;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnCloseListener;
import com.actionbarsherlock.widget.SearchView.OnSuggestionListener;
import com.oakonell.findx.BuildConfig;
import com.oakonell.findx.R;
import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.custom.parse.ParseConnectivity.ParseUserExtra;
import com.oakonell.findx.custom.parse.ParseCustomLevelSearchAdapter.CheckCallback;
import com.oakonell.findx.custom.parse.ParseLevelHelper.ParseCustomLevel;
import com.oakonell.findx.custom.parse.ParseLevelHelper.ParseCustomLevelFlag;
import com.oakonell.findx.model.Levels;
import com.oakonell.utils.StringUtils;
import com.oakonell.utils.Utils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class CustomLevelSearchActivity extends SherlockListActivity {
	private static final SortCriteria SORT_BY_CREATION_DATE = new SortCriteria(
			2, "Creation date");
	private static final SortCriteria SORT_BY_RATING = new SortCriteria(1,
			"Average Rating");
	private static final String SEARCH_FILE = "recent_search_terms.txt";
	private static final int MAX_TERMS = 100;

	private ParseCustomLevelSearchAdapter adapter;
	private List<ParseObject> levels;
	private Dialog progressDialog;
	private String filter;
	SortCriteria sort = SORT_BY_CREATION_DATE;
	private boolean downloading = false;

	private Map<String, Integer> checkedPositionByIds = new HashMap<String, Integer>();

	private ActionMode actionMode;

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
		private static final String PARSE_CREATED_AT_PROPERTY_NAME = "_created_at";

		// Override this method to do custom remote calls
		protected Void doInBackground(Void... params) {
			// Gets the current list of todos in sorted order
			ParseQuery<ParseObject> query;
			// whereMatches may slow down on large data sets..
			// might be better to store a redundant canonical uppercase value
			// for each field bing used in case insensitive searches
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

			query.whereLessThan(ParseCustomLevel.num_flags, 3);

			if (sort == SORT_BY_CREATION_DATE) {
				query.orderByDescending(PARSE_CREATED_AT_PROPERTY_NAME);
			} else if (sort == SORT_BY_RATING) {
				query.orderByDescending(ParseCustomLevel.avg_rating_field);
			} else {
				throw new RuntimeException("Unexpected sort");
			}
			query.include(ParseCustomLevel.createdBy_field);

			try {
				levels = query.find();
			} catch (ParseException e) {
				throw new RuntimeException("Error finding levels", e);
			}

			if (!levels.isEmpty()) {
				// TODO this will be non-scalable for large numbers of flagged
				// levels
				// find my flagged levels, to avoid showing them
				ParseQuery<ParseObject> myFlaggedQuery = new ParseQuery<ParseObject>(
						ParseCustomLevelFlag.classname);
				myFlaggedQuery.whereEqualTo(
						ParseCustomLevelFlag.flaggedBy_field,
						ParseUser.getCurrentUser());
				List<ParseObject> flagged;
				try {
					flagged = myFlaggedQuery.find();
				} catch (ParseException e) {
					throw new RuntimeException("Error finding levels", e);
				}
				if (flagged.isEmpty())
					return null;

				Set<String> flaggedIds = new HashSet<String>();
				for (ParseObject each : flagged) {
					flaggedIds.add(each.getParseObject(
							ParseCustomLevelFlag.level_field).getObjectId());
				}

				for (Iterator<ParseObject> iter = levels.iterator(); iter
						.hasNext();) {
					ParseObject level = iter.next();
					if (flaggedIds.contains(level.getObjectId())) {
						iter.remove();
					}
				}

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
			TextView empty = (TextView) findViewById(R.id.empty);
			if (levels.isEmpty()) {
				empty.setVisibility(View.VISIBLE);
			} else {
				empty.setVisibility(View.GONE);
			}

		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_level_search);
		if (BuildConfig.DEBUG) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectDiskReads().detectDiskWrites().detectNetwork() // or
																			// .detectAll()
																			// for
																			// all
																			// detectable
																			// problems
					.penaltyLog().build());
		}

		final ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayUseLogoEnabled(true);
		ab.setDisplayShowTitleEnabled(true);
		ab.setTitle("Shared Levels");

		levels = new ArrayList<ParseObject>();

		CheckCallback callback = new CheckCallback() {
			@Override
			public void checkStateChanged(int position, boolean isChecked) {
				if (downloading)
					return;

				if (isChecked && actionMode == null) {
					actionMode = startActionMode(new Callback() {

						@Override
						public boolean onCreateActionMode(
								com.actionbarsherlock.view.ActionMode mode,
								Menu menu) {
							MenuInflater inflater = mode.getMenuInflater();
							inflater.inflate(R.menu.custom_search_item_context,
									menu);
							return true;
						}

						@Override
						public boolean onActionItemClicked(
								com.actionbarsherlock.view.ActionMode mode,
								MenuItem item) {
							switch (item.getItemId()) {
							case R.id.menu_download:
								downloadChecked();
								return true;
							case R.id.menu_flag:
								flagChecked();
							}
							return false;
						}

						@Override
						public boolean onPrepareActionMode(
								com.actionbarsherlock.view.ActionMode mode,
								Menu menu) {
							return false;
						}

						@Override
						public void onDestroyActionMode(
								com.actionbarsherlock.view.ActionMode mode) {
							// uncheck any, and redraw
							checkedPositionByIds.clear();
							adapter.notifyDataSetChanged();
							actionMode = null;
						}

					});

				}

				ParseObject level = levels.get(position);
				String id = level.getObjectId();
				if (isChecked) {
					checkedPositionByIds.put(id, position);
				} else {
					checkedPositionByIds.remove(id);
				}
				conditionallyFinishActionMode();
			}

		};

		adapter = new ParseCustomLevelSearchAdapter(this, levels,
				checkedPositionByIds, callback);
		setListAdapter(adapter);

		// TextView empty = (TextView) findViewById(android.R.id.empty);
		// empty.setVisibility(View.INVISIBLE);

		ImageButton searchButton = (ImageButton) findViewById(R.id.search);
		final EditText searchText = (EditText) findViewById(R.id.search_text);
		if (Utils.hasHoneycomb()) {
			searchButton.setVisibility(View.GONE);
			searchText.setVisibility(View.GONE);
		} else {
			searchButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					filter = searchText.getText().toString();
					adapter.clear();
					new RemoteDataTask().execute();
				}
			});
		}

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

	private void conditionallyFinishActionMode() {
		if (checkedPositionByIds.isEmpty() && actionMode != null) {
			actionMode.finish();
		}
	}

	private void flagChecked() {
		final int max = checkedPositionByIds.size();
		final ProgressDialog progress = ProgressDialog.show(this,
				"Flagging Inappropriate levels", "Flagging 0/" + max + "...");
		downloading = true;
		AsyncTask<Void, Integer, Void> task = new AsyncTask<Void, Integer, Void>() {
			private Set<ParseObject> flagged = new HashSet<ParseObject>();

			@Override
			protected Void doInBackground(Void... params) {
				Set<Entry<String, Integer>> entrySet = new HashSet<Map.Entry<String, Integer>>(
						checkedPositionByIds.entrySet());
				int i = 0;
				for (Entry<String, Integer> entry : entrySet) {
					i++;
					publishProgress(i);
					Integer position = entry.getValue();
					ParseObject level = adapter.getItem(position);
					ParseLevelHelper.flagLevel(level);
					levels.remove(level);
					flagged.add(level);
					checkedPositionByIds.remove(entry.getKey());
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				progress.setMessage("Flagging " + values[0] + "/" + max + "...");
			}

			@Override
			protected void onPostExecute(Void result) {
				finishFlagging(progress);
			}

			private void finishFlagging(final ProgressDialog progress) {
				for (ParseObject level : flagged) {
					adapter.remove(level);
				}
				adapter.notifyDataSetChanged();
				Levels.resetCustomStage();
				downloading = false;
				progress.dismiss();
				conditionallyFinishActionMode();
			}

			@Override
			protected void onCancelled() {
				finishFlagging(progress);
			}

		};
		task.execute();
	}

	protected void downloadChecked() {
		final int max = checkedPositionByIds.size();
		final ProgressDialog progress = ProgressDialog.show(this,
				"Downloading levels", "Downloading 0/" + max + "...");
		downloading = true;
		AsyncTask<Void, Integer, Void> task = new AsyncTask<Void, Integer, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				Set<Entry<String, Integer>> entrySet = new HashSet<Map.Entry<String, Integer>>(
						checkedPositionByIds.entrySet());
				int i = 0;
				for (Entry<String, Integer> entry : entrySet) {
					i++;
					publishProgress(i);
					Integer position = entry.getValue();
					CustomLevelBuilder builder = ParseLevelHelper.load(adapter
							.getItem(position));
					builder.save();
					checkedPositionByIds.remove(entry.getKey());
					// int firstPosition =
					// getListView().getFirstVisiblePosition();
					// int lastPosition =
					// getListView().getLastVisiblePosition();
					// if (firstPosition <= position && position <=
					// lastPosition) {
					// int childViewIndex = position - firstPosition;
					// View child = getListView().getChildAt(childViewIndex);
					// final ViewHolder holder = (ViewHolder) child.getTag();
					// handler.post(new Runnable() {
					// @Override
					// public void run() {
					// holder.check.setEnabled(enabled);Checked(false);
					// }
					// });
					//
					// }
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				progress.setMessage("Downloading " + values[0] + "/" + max
						+ "...");
			}

			@Override
			protected void onPostExecute(Void result) {
				adapter.notifyDataSetChanged();
				Levels.resetCustomStage();
				downloading = false;
				progress.dismiss();
				conditionallyFinishActionMode();
			}

			@Override
			protected void onCancelled() {
				adapter.notifyDataSetChanged();
				Levels.resetCustomStage();
				conditionallyFinishActionMode();
				downloading = false;
				progress.dismiss();
			}

		};
		task.execute();
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

	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.custom_search, menu);

		if (Utils.hasHoneycomb()) {
			enableActionBarSearch(menu);
		}
		return true;
	}

	private void enableActionBarSearch(Menu menu) {
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
		final SearchView searchView = (SearchView) searchMenuItem
				.getActionView();
		// final View searchIcon = searchMenuItem.getActionView().findViewById(
		// R.id.abs__search_mag_icon);

		if (null != searchView) {
			SearchableInfo searchableInfo = searchManager
					.getSearchableInfo(getComponentName());
			searchView.setSearchableInfo(searchableInfo);
			searchView.setIconifiedByDefault(false);
			searchView.setQueryHint("Search Titles, Authors");
			((AutoCompleteTextView) searchView
					.findViewById(R.id.abs__search_src_text)).setThreshold(0);

		}

		searchMenuItem
				.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
					@Override
					public boolean onMenuItemActionExpand(MenuItem item) {
						return true;
					}

					@Override
					public boolean onMenuItemActionCollapse(MenuItem item) {
						if (filter != null) {
							filter = null;
							adapter.clear();
							new RemoteDataTask().execute();
						}
						return true;
					}
				});

		SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
			// boolean wasSearched;

			public boolean onQueryTextChange(String newText) {
				// if (newText.equals("") && wasSearched) {
				// searchIcon.setVisibility(View.VISIBLE);
				// }
				// this is your adapter that will be filtered
				// adapter.getFilter().filter(newText);
				suggestSearchTerms(searchView, newText);
				return true;
			}

			public boolean onQueryTextSubmit(String query) {
				// remove, if exists, to allow this to be added to the bottom,
				// as most recently used
				searchTerms.remove(query);
				searchTerms.add(query);

				// searchIcon.setVisibility(View.GONE);
				// wasSearched = true;
				searchView.clearFocus();
				filter = query;
				adapter.clear();
				new RemoteDataTask().execute();
				// // this is your adapter that will be filtered
				// adapter.getFilter().filter(query);
				return true;
			}
		};
		searchView.setOnQueryTextListener(queryTextListener);
		searchView.setOnSuggestionListener(new OnSuggestionListener() {
			@Override
			public boolean onSuggestionSelect(int position) {
				String selected = getSuggestion(position);
				searchView.setQuery(selected, true);
				return true;
			}

			private String getSuggestion(int position) {
				Cursor cursor = (Cursor) searchView.getSuggestionsAdapter()
						.getItem(position);
				String suggest1 = cursor.getString(cursor
						.getColumnIndex("text"));
				return suggest1;
			}

			@Override
			public boolean onSuggestionClick(int position) {
				return onSuggestionSelect(position);
			}
		});
		searchView.setOnCloseListener(new OnCloseListener() {
			@Override
			public boolean onClose() {
				// searchIcon.setVisibility(View.VISIBLE);
				filter = null;
				adapter.clear();
				new RemoteDataTask().execute();
				return true;
			}
		});
	}

	private Set<String> searchTerms = new LinkedHashSet<String>();

	@Override
	protected void onResume() {
		super.onResume();
		try {
			FileInputStream stream = openFileInput(SEARCH_FILE);
			Scanner scanner = new Scanner(stream);
			while (scanner.hasNext()) {
				String term = scanner.next();
				searchTerms.add(term);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			// ignore, use an empty LRU
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (searchTerms.isEmpty())
			return;

		try {
			FileOutputStream stream = openFileOutput(SEARCH_FILE,
					Context.MODE_PRIVATE);
			// trim the searched set
			int numToRemove = searchTerms.size() - MAX_TERMS;
			Iterator<String> iter = searchTerms.iterator();
			while (numToRemove > 0) {
				iter.remove();
			}
			OutputStreamWriter ow = new OutputStreamWriter(stream);
			BufferedWriter writer = new BufferedWriter(ow);
			for (String each : searchTerms) {
				writer.write(each);
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			Log.e("CustomLevelSearchActivity", "Error writing search terms", e);
		}

	}

	protected void suggestSearchTerms(SearchView searchView, String newText) {
		List<String> items = new ArrayList<String>();
		String newTextUpperCase = newText.toUpperCase();
		for (String each : searchTerms) {
			if (each.toUpperCase().startsWith(newTextUpperCase)) {
				items.add(each);
			}
		}
		Collections.reverse(items);

		// Load data from list to cursor
		MatrixCursor cursor = createCursor(items);

		searchView
				.setSuggestionsAdapter(new ExampleAdapter(this, cursor, items));
	}

	private MatrixCursor createCursor(List<String> items) {
		String[] columns = new String[] { "_id", "text" };

		MatrixCursor cursor = new MatrixCursor(columns);

		for (int i = 0; i < items.size(); i++) {
			Object[] temp = new Object[] { 0, "default" };
			temp[0] = i;
			temp[1] = items.get(i);
			cursor.addRow(temp);
		}
		return cursor;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(CustomLevelSearchActivity.this);
			return true;
		}
		// TODO settings requires game context?!
		return false;
		// return
		// MenuHelper.onOptionsItemSelected(CustomLevelSearchActivity.this,
		// item);
	}

	public class ExampleAdapter extends CursorAdapter {
		private List<String> items;

		public ExampleAdapter(Context context, Cursor cursor, List<String> items) {
			super(context, cursor, false);
			this.items = items;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// Show list item data from cursor
			TextView text = (TextView) view.getTag();
			text.setText(items.get(cursor.getPosition()));
			// Alternatively show data direct from database
			// text.setText(cursor.getString(cursor.getColumnIndex("column_name")));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.search_term, parent, false);
			final TextView text = (TextView) view.findViewById(R.id.text);
			view.setTag(text);
			ImageView delete = (ImageView) view.findViewById(R.id.cancel);
			delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CharSequence term = text.getText();
					searchTerms.remove(term);
					items.remove(term);

					MatrixCursor cursor = createCursor(items);
					changeCursor(cursor);
					notifyDataSetChanged();
				}
			});
			return view;
		}

	}

}
