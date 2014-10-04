package com.oakonell.findx.custom;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.oakonell.findx.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class CustomLevelSearchActivity extends ListActivity {
	private ParseCustomLevelAdapter adapter;
	private List<ParseObject> levels;
	private Dialog progressDialog;

	private class RemoteDataTask extends AsyncTask<Void, Void, Void> {
		// Override this method to do custom remote calls
		protected Void doInBackground(Void... params) {
			// Gets the current list of todos in sorted order
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
					"CustomLevel");
			query.orderByDescending("_created_at");

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
		levels= new ArrayList<ParseObject>();
		adapter = new ParseCustomLevelAdapter(this, levels);
		setListAdapter(adapter);
		setContentView(R.layout.custom_level_search);

		TextView empty = (TextView) findViewById(android.R.id.empty);
		empty.setVisibility(View.INVISIBLE);

		new RemoteDataTask().execute();
		registerForContextMenu(getListView());
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		ParseObject level = levels.get(position);

		/*
		 * ParseObject level = new ParseObject("CustomLevel");
		 * level.put("title", title); level.put("author",
		 * parseUser.getUsername()); level.put("solution", solution);
		 * level.put("start_equation", moves.get(0).getStartEquation());
		 * level.put("numMoves", moves.size());
		 */

		Toast.makeText(this, "Selected " + level.getString("title"),
				Toast.LENGTH_SHORT).show();
	}

}
