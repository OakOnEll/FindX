package com.oakonell.findx.custom.parse;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.oakonell.findx.R;
import com.oakonell.findx.custom.parse.ParseLevelHelper.ParseLevelRating;
import com.parse.ParseObject;

public class EditLevelRatingFragment extends DialogFragment {
	private ParseObject myParseComment;
	private ParseObject level;
	private Runnable continuation;

	public void initialize(ParseObject myRating, ParseObject level,
			Runnable continuation) {
		this.myParseComment = myRating;
		this.level = level;
		this.continuation = continuation;
	}

	@Override
	public final View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		// getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		// getDialog().getWindow().setBackgroundDrawable(
		// new ColorDrawable(Color.TRANSPARENT));
		final View view = inflater.inflate(
				R.layout.custom_level_edit_current_user_rate, container, false);
		getDialog().setCancelable(false);

		// setTitle(view, getString(R.string.nickname));

		final RatingBar myRatingBar = (RatingBar) view
				.findViewById(R.id.current_ratingBar);
		final EditText commentView = (EditText) view.findViewById(R.id.comment);

		float rating = (float) myParseComment
				.getDouble(ParseLevelRating.rating_field);
		String comment = myParseComment
				.getString(ParseLevelRating.comment_field);

		commentView.setText(comment);
		myRatingBar.setRating(rating);

		Button button = (Button) view.findViewById(R.id.submit);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final ProgressDialog progDialog = ProgressDialog.show(
						getActivity(), "Please wait...", "");
				final float newRating = myRatingBar.getRating();
				final String newComment = commentView.getText().toString();

				AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
					boolean success;

					@Override
					protected Void doInBackground(Void... params) {
						ParseLevelHelper.addOrModifyRatingComment(level,
								myParseComment, newRating, newComment, null);
						success = true;
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						progDialog.dismiss();
						if (success) {
							dismiss();
							if (continuation != null) {
								continuation.run();
							}
							return;
						}
					}

				};
				task.execute();
			}
		});

		return view;
	}

}
