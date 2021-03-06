package com.oakonell.findx.custom.parse;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.TextView;

import com.oakonell.findx.R;
import com.oakonell.findx.custom.model.CustomLevelDBReader;
import com.oakonell.findx.custom.parse.ParseConnectivity.ParseUserExtra;
import com.oakonell.findx.custom.parse.ParseLevelHelper.ParseCustomLevel;
import com.parse.ParseObject;

public class ParseCustomLevelSearchAdapter extends ArrayAdapter<ParseObject> {
	public static class ViewHolder {

		public CheckBox check;
		public TextView title;
		public TextView equation;
		public TextView description;
		public RatingBar ratingBar;
		public TextView numRatings;
		public TextView authorship;
		public String id;
		protected boolean userUpdate = true;

	}

	private Activity context;
	private CheckCallback callback;
	Map<String, Integer> checkedPositionByIds;

	public interface CheckCallback {
		void checkStateChanged(int position, boolean isChecked);
	}

	public ParseCustomLevelSearchAdapter(Activity context,
			List<ParseObject> objects,
			Map<String, Integer> checkedPositionByIds, CheckCallback callback) {
		super(context, R.layout.custom_level_item, objects);
		this.context = context;
		this.callback = callback;
		this.checkedPositionByIds = checkedPositionByIds;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		if (view == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			view = inflater.inflate(R.layout.custom_level_item, null);
			holder = new ViewHolder();
			holder.check = (CheckBox) view.findViewById(R.id.check);
			holder.title = (TextView) view.findViewById(R.id.title);
			holder.description = (TextView) view.findViewById(R.id.description);
			holder.equation = (TextView) view.findViewById(R.id.equation);
			holder.ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
			holder.numRatings = (TextView) view.findViewById(R.id.num_ratings);
			holder.authorship = (TextView) view.findViewById(R.id.authorship);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		final ParseObject item = getItem(position);

		String title = item.getString(ParseCustomLevel.title_field);
		String author = item.getParseUser(ParseCustomLevel.createdBy_field)
				.getString(ParseUserExtra.nickname_field);
		String equationString = ParseLevelHelper.readEquation(item).toString();
		int minMoves = item.getInt(ParseCustomLevel.num_moves_field);
		int numOperations = item.getInt(ParseCustomLevel.num_operations_field);
		long createdMillis = item.getCreatedAt().getTime();

		int numRatings = item.getInt(ParseCustomLevel.num_ratings_field);
		double totalRatings = item
				.getDouble(ParseCustomLevel.total_ratings_field);
		float rating = numRatings == 0 ? 0
				: (float) (totalRatings / numRatings);

		final CustomLevelDBReader reader = new CustomLevelDBReader();
		holder.check.setOnCheckedChangeListener(null);

		// update whether this level is on device or can be downloaded
		final ViewHolder theHolder = holder;
		theHolder.check.setVisibility(View.INVISIBLE);
		theHolder.id = item.getObjectId();
		AsyncTask<Void, Void, Long> asyncTask = new AsyncTask<Void, Void, Long>() {

			@Override
			protected Long doInBackground(Void... params) {
				return reader.findDbIdByServerId(context, item.getObjectId());
			}

			@Override
			protected void onPostExecute(Long dbId) {
				// if the view has been reused before the db read was finished,
				// just ignore the result
				if (!theHolder.id.equals(item.getObjectId())) {
					return;
				}
				theHolder.check.setVisibility(View.VISIBLE);
				theHolder.userUpdate  = false;
				if (dbId > 0) {
					theHolder.check.setChecked(true);
					theHolder.check.setEnabled(false);
				} else {
					if (checkedPositionByIds.get(item.getObjectId()) != null) {
						theHolder.check.setChecked(true);
					} else {
						theHolder.check.setChecked(false);
					}
					theHolder.check.setEnabled(true);
				}
				theHolder.userUpdate = true;
			}
		};
		asyncTask.execute();

		holder.check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (theHolder.userUpdate && callback != null) {
					callback.checkStateChanged(position, isChecked);
				}
			}
		});

		holder.title.setText(title);
		holder.equation.setText(Html.fromHtml(equationString));
		holder.description.setText("Solvable with " + numOperations
				+ " operations in " + minMoves + " moves.");

		CharSequence createdString = DateUtils.getRelativeDateTimeString(
				context, createdMillis, DateUtils.DAY_IN_MILLIS,
				DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
		holder.authorship.setText("Created by " + author + " on "
				+ createdString);
		holder.numRatings.setText("(" + numRatings + ")");
		holder.ratingBar.setRating(rating);

		return view;
	}

}
