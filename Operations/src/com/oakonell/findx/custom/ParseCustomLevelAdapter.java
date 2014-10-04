package com.oakonell.findx.custom;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.oakonell.findx.R;
import com.parse.ParseObject;

public class ParseCustomLevelAdapter extends ArrayAdapter<ParseObject> {
	public static class ViewHolder {

		public TextView title;
		public TextView equation;
		public TextView numMoves;
		public RatingBar ratingBar;
		public TextView author;
		public TextView createdDate;

	}

	private Activity context;

	public ParseCustomLevelAdapter(Activity context, List<ParseObject> objects) {
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
			holder.equation = (TextView) view.findViewById(R.id.equation);
			holder.numMoves = (TextView) view.findViewById(R.id.num_moves);
			holder.ratingBar = (RatingBar) view.findViewById(R.id.ratingBar1);
			holder.author = (TextView) view.findViewById(R.id.author);
			holder.createdDate = (TextView) view.findViewById(R.id.created);
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
		holder.title.setText(item.getString("title"));
		holder.equation.setText(item.getString("start_equation"));
		holder.numMoves.setText(item.getInt("numMoves") + "");
		holder.author.setText(item.getString("author"));
		
		return view;
	}

}
