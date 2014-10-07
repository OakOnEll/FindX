package com.oakonell.findx.custom.parse;

import java.util.List;

import android.app.Activity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.oakonell.findx.R;
import com.oakonell.findx.custom.parse.ParseConnectivity.ParseUserExtra;
import com.oakonell.findx.custom.parse.ParseLevelHelper.ParseCustomLevel;
import com.parse.ParseObject;

public class ParseCustomLevelSearchAdapter extends ArrayAdapter<ParseObject> {
	public static class ViewHolder {

		public TextView title;
		public TextView equation;
		public TextView description;
		public RatingBar ratingBar;
		public TextView numRatings;
		public TextView authorship;

	}

	private Activity context;

	public ParseCustomLevelSearchAdapter(Activity context,
			List<ParseObject> objects) {
		super(context, R.layout.custom_level_item, objects);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		if (view == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			view = inflater.inflate(R.layout.custom_level_item, null);
			holder = new ViewHolder();
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

		holder.title.setText(title);
		holder.equation.setText(equationString);
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
