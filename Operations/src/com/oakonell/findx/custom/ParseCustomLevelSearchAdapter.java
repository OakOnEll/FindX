package com.oakonell.findx.custom;

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
import com.parse.ParseObject;

public class ParseCustomLevelSearchAdapter extends ArrayAdapter<ParseObject> {
	public static class ViewHolder {

		public TextView title;
		public TextView description;
		public RatingBar ratingBar;
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
			holder.ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
			holder.authorship = (TextView) view.findViewById(R.id.authorship);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		final ParseObject item = getItem(position);

		/*
		 * ParseObject level = new ParseObject("CustomLevel");
		 * level.put("title", title); level.put("author",
		 * parseUser.getUsername()); level.put("solution", solution);
		 * level.put("start_equation", moves.get(0).getStartEquation());
		 * level.put("numMoves", moves.size());
		 */
		String title = item.getString("title");
		String author = item.getParseUser("createdBy").getString("nickname");
		String equationString = ParseLevelHelper.readEquation(item).toString();
		int minMoves = item.getInt("numMoves");
		int numOperations = item.getInt("numOperations");
		long createdMillis = item.getCreatedAt().getTime();

		holder.title.setText(title);
		holder.description.setText("Solve " + equationString + " with "
				+ numOperations + " operations in " + minMoves + " moves.");

		CharSequence createdString = DateUtils.getRelativeDateTimeString(
				context, createdMillis, DateUtils.DAY_IN_MILLIS,
				DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
		holder.authorship.setText("Created by " + author + " on "
				+ createdString);

		return view;
	}

}
