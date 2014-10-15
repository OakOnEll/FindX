package com.oakonell.findx.custom.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.oakonell.findx.MenuHelper;
import com.oakonell.findx.R;
import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.custom.parse.ParseConnectivity.ParseUserExtra;
import com.oakonell.findx.custom.parse.ParseCustomLevelSearchAdapter.CheckCallback;
import com.oakonell.findx.custom.parse.ParseLevelHelper.ParseCustomLevel;
import com.oakonell.findx.model.Levels;
import com.oakonell.utils.StringUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class CustomLevelSearchActivity extends SherlockListActivity {
	private static final SortCriteria SORT_BY_CREATION_DATE = new SortCriteria(
			2, "Creation date");
	private static final SortCriteria SORT_BY_RATING = new SortCriteria(1,
			"Average Rating");
	private ParseCustomLevelSearchAdapter adapter;
	private List<ParseObject> levels;
	private Dialog progressDialog;
	private String filter;
	SortCriteria sort = SORT_BY_CREATION_DATE;
	private boolean downloading = false;

	private Map<String, Integer> checkedPositionByIds = new HashMap<String, Integer>();
	private Button downloadChecked;

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
		setContentView(R.layout.custom_level_search);
		downloadChecked = (Button) findViewById(R.id.download_checked);
		downloadChecked.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				downloadChecked();
			}
		});
		configureDownloadCheckedButton();
		levels = new ArrayList<ParseObject>();

		CheckCallback callback = new CheckCallback() {
			@Override
			public void checkStateChanged(int position, boolean isChecked) {
				if (downloading)
					return;
				ParseObject level = levels.get(position);
				String id = level.getObjectId();
				if (isChecked) {
					checkedPositionByIds.put(id, position);
				} else {
					checkedPositionByIds.remove(id);
				}
				configureDownloadCheckedButton();
			}

		};

		adapter = new ParseCustomLevelSearchAdapter(this, levels,
				checkedPositionByIds, callback);
		setListAdapter(adapter);

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

	private void configureDownloadCheckedButton() {
		downloadChecked.setEnabled(!checkedPositionByIds.isEmpty());
		// if (checkedPositionByIds.isEmpty()) {
		// downloadChecked.setVisibility(View.GONE);
		// } else {
		// downloadChecked.setVisibility(View.VISIBLE);
		// }
	}

	protected void downloadChecked() {
		final Handler handler = new Handler();
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
				configureDownloadCheckedButton();
				downloading = false;
				progress.dismiss();
			}

			@Override
			protected void onCancelled() {
				adapter.notifyDataSetChanged();
				Levels.resetCustomStage();
				configureDownloadCheckedButton();
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(CustomLevelSearchActivity.this);
			return true;
		}
		// TODO settings requires game context?!
		return false;
		//return MenuHelper.onOptionsItemSelected(CustomLevelSearchActivity.this, item);
	}
}
