package com.oakonell.findx.custom.parse;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.merge.MergeAdapter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.oakonell.findx.BuildConfig;
import com.oakonell.findx.FindXApp;
import com.oakonell.findx.GameActivity;
import com.oakonell.findx.PuzzleActivity;
import com.oakonell.findx.R;
import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.custom.model.CustomLevelDBReader;
import com.oakonell.findx.custom.model.ICustomLevel;
import com.oakonell.findx.custom.parse.EditLevelRatingFragment.OnRatingResult;
import com.oakonell.findx.custom.parse.ParseConnectivity.ParseUserExtra;
import com.oakonell.findx.custom.parse.ParseLevelHelper.OnRatingLoaded;
import com.oakonell.findx.custom.parse.ParseLevelHelper.ParseCustomLevel;
import com.oakonell.findx.custom.parse.ParseLevelHelper.ParseLevelOperation;
import com.oakonell.findx.custom.parse.ParseLevelHelper.ParseLevelRating;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Operation;
import com.oakonell.utils.StringUtils;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class CustomLevelDetailActivity extends GameActivity {
	public static final String LEVEL_PARSE_ID = "levelParseId";
	private MergeAdapter mainAdapter = new MergeAdapter();

	private List<Operation> operations = new ArrayList<Operation>();
	private List<ParseObject> comments = new ArrayList<ParseObject>();

	private ArrayAdapter<Operation> operationsAdapter;
	private CommentsAdapter commentsAdapter;

	private String levelId;
	private ParseObject level;

	private ProgressBar waiting;

	private TextView title;
	private TextView author;
	private RatingBar ratingBar;
	private TextView num_rankings;
	private TextView equation;
	private TextView solvable_desription;
	protected ParseObject myParseComment;
	private View currentUserRatingView;
	private TextView myCommentView;
	private RatingBar myRatingBar;
	private TextView downloads;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_level_detail);
		LayoutInflater inflater = LayoutInflater.from(this);
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
		// the detail page can come from many parents, need to rely on user
		// using back instead
		ab.setDisplayHomeAsUpEnabled(false);
		ab.setDisplayUseLogoEnabled(true);
		ab.setDisplayShowTitleEnabled(true);
		ab.setTitle("Level Detail");

		levelId = getIntent().getStringExtra(LEVEL_PARSE_ID);
		if (levelId == null) {
			Intent intent = getIntent();

			Uri uri = intent.getData();
			levelId = uri.getQueryParameter("id");
		}
		if (levelId == null) {
			// TODO give a warning that no level exists
		}

		waiting = (ProgressBar) findViewById(R.id.waiting);
		waiting.setVisibility(View.VISIBLE);

		ListView listView = (ListView) findViewById(R.id.list);
		mainAdapter = new MergeAdapter();

		View headerView = inflater.inflate(R.layout.custom_level_detail_header,
				null);
		title = (TextView) headerView.findViewById(R.id.title);
		author = (TextView) headerView.findViewById(R.id.author);
		downloads = (TextView) headerView.findViewById(R.id.downloads);
		equation = (TextView) headerView.findViewById(R.id.equation);
		solvable_desription = (TextView) headerView
				.findViewById(R.id.solvable_desription);
		ratingBar = (RatingBar) headerView.findViewById(R.id.ratingBar);
		num_rankings = (TextView) headerView.findViewById(R.id.num_rankings);
		// TODO use the rating bar on change as the edit trigger

		mainAdapter.addView(headerView);

		operationsAdapter = new OperationAdapter(this, operations);

		mainAdapter.addAdapter(operationsAdapter);

		View commentHeaderView = inflater.inflate(R.layout.comments_header,
				null);
		mainAdapter.addView(commentHeaderView);

		currentUserRatingView = inflater.inflate(
				R.layout.custom_level_current_user_rate, null);
		Button editRating = (Button) currentUserRatingView
				.findViewById(R.id.edit);
		editRating.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editRating();
			}
		});
		myCommentView = (TextView) currentUserRatingView
				.findViewById(R.id.comment);
		myRatingBar = (RatingBar) currentUserRatingView
				.findViewById(R.id.current_ratingBar);
		myRatingBar.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					editRating();
				}
				return true;
			}
		});
		myRatingBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editRating();
			}
		});

		mainAdapter.addView(currentUserRatingView);

		commentsAdapter = new CommentsAdapter(this, comments);
		mainAdapter.addAdapter(commentsAdapter);

		// listView.addFooterView(v);

		listView.setAdapter(mainAdapter);

	}

	protected void editRating() {
		ParseUser parseUser = ParseUser.getCurrentUser();
		if (parseUser == null) {
			setOnSignIn(new Runnable() {
				@Override
				public void run() {
					ParseObject proxiedParseLevel = ParseObject
							.createWithoutData(ParseCustomLevel.classname,
									levelId);
					loadMyComment(proxiedParseLevel, new Runnable() {
						@Override
						public void run() {
							editRating();
						}
					});
				}
			});
			getGameHelper().beginUserInitiatedSignIn();
			return;
		}
		if (StringUtils.isEmpty(parseUser
				.getString(ParseUserExtra.nickname_field))) {
			Runnable continuation = new Runnable() {
				@Override
				public void run() {
					editRating();
				}
			};
			ParseConnectivity.createUniqueNickname(this, continuation);
			return;
		}

		// need to log in to edit a rating

		EditLevelRatingFragment frag = new EditLevelRatingFragment();
		frag.initialize(myParseComment, level, new OnRatingResult() {
			@Override
			public void rated(ParseObject rating) {
				myParseComment = rating;
				updateMyRatingInfo();
				updateHeader(level);
				// TODO release the my comment progress
				Toast.makeText(CustomLevelDetailActivity.this,
						"Updated rating and comment", Toast.LENGTH_SHORT)
						.show();
			}
		});
		frag.show(getSupportFragmentManager(), "");
		// RatingBar currentUserRating = (RatingBar) currentUserRatingView
		// .findViewById(R.id.current_ratingBar);
		// TextView commentView = (TextView) currentUserRatingView
		// .findViewById(R.id.comment);
		//
		// float rating = currentUserRating.getRating();
		// String comment = commentView.getText().toString();
		//
		// // TODO show a progress/waiting in the "my comment section"
		// ParseLevelHelper.addOrModifyRatingComment(level, myParseComment,
		// rating, comment, new Runnable() {
		// @Override
		// public void run() {
		// updateHeader(level);
		// // TODO release the my comment progress
		// Toast.makeText(CustomLevelDetailActivity.this,
		// "Updated rating and comment",
		// Toast.LENGTH_SHORT).show();
		// }
		// });
	}

	private void updateMyRatingInfo() {
		float rating = (float) myParseComment
				.getDouble(ParseLevelRating.rating_field);
		String comment = myParseComment
				.getString(ParseLevelRating.comment_field);

		myCommentView.setText(comment);
		myRatingBar.setRating(rating);
	}

	@Override
	protected void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);

		loadLevelDetails();
	}

	private void loadLevelDetails() {
		// find the level
		ParseQuery<ParseObject> levelQuery = ParseQuery
				.getQuery(ParseCustomLevel.classname);
		levelQuery.include(ParseCustomLevel.createdBy_field);
		levelQuery.getInBackground(levelId, new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
				if (e == null) {
					level = object;
					waiting.setVisibility(View.GONE);
					updateHeader(object);
				} else {
					TextView errorView = (TextView) findViewById(R.id.error_text);
					errorView.setVisibility(View.VISIBLE);
					errorView.setText("Error getting level- " + levelId + ": "
							+ e.getLocalizedMessage());
					waiting.setVisibility(View.GONE);
					Log.e("CustomLevelDetailActivity", "Error getting level- "
							+ levelId, e);
				}
			}
		});

		ParseObject proxiedParseLevel = ParseObject.createWithoutData(
				ParseCustomLevel.classname, levelId);
		// find the level's operations
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
				ParseLevelOperation.classname);
		query.whereEqualTo(ParseLevelOperation.level_field, proxiedParseLevel);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> list, ParseException e) {
				if (e != null) {
					// TODO
					throw new RuntimeException(
							"Error getting level operations", e);
				} else {
					operations.clear();
					for (ParseObject opObject : list) {
						Operation op = ParseLevelHelper
								.loadOperationFrom(opObject);

						operations.add(op);
					}
					operationsAdapter.notifyDataSetChanged();
				}
			}
		});
		ParseUser currentUser = ParseUser.getCurrentUser();

		// load my comment
		loadMyComment(proxiedParseLevel, null);

		// load all but my comment
		ParseQuery<ParseObject> otherCommentQuery = new ParseQuery<ParseObject>(
				ParseLevelRating.classname);
		otherCommentQuery.whereEqualTo(ParseLevelOperation.level_field,
				proxiedParseLevel);
		otherCommentQuery.whereNotEqualTo(ParseLevelRating.createdBy_field,
				currentUser);
		otherCommentQuery.include(ParseLevelRating.createdBy_field);
		otherCommentQuery.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> theComments, ParseException e) {
				if (e != null) {
					// TODO
					throw new RuntimeException("Error getting other comments",
							e);
				}
				comments.clear();
				comments.addAll(theComments);
				commentsAdapter.notifyDataSetChanged();
			}
		});
	}

	private void loadMyComment(ParseObject proxiedParseLevel,
			final Runnable continuation) {
		ParseLevelHelper.getMyRatingComment(proxiedParseLevel,
				new OnRatingLoaded() {
					@Override
					public void ratingLoaded(ParseObject myRating) {
						if (myRating != null) {
							myParseComment = myRating;
							updateMyRatingInfo();
						}
						if (continuation != null) {
							continuation.run();
						}
					}
				});
	}

	protected void updateHeader(ParseObject object) {
		String equationString = ParseLevelHelper.readEquation(object)
				.toString();
		int numMoves = object
				.getInt(ParseLevelHelper.ParseCustomLevel.num_moves_field);
		int numDownloads = object
				.getInt(ParseLevelHelper.ParseCustomLevel.download_counter);
		String authorName = object.getParseUser(
				ParseLevelHelper.ParseCustomLevel.createdBy_field).getString(
				ParseUserExtra.nickname_field);
		String titleString = object
				.getString(ParseLevelHelper.ParseCustomLevel.title_field);
		String solvableDescriptionString = "Solvable in " + numMoves
				+ " moves with the following operations:";
		int totalRating = object
				.getInt(ParseLevelHelper.ParseCustomLevel.total_ratings_field);
		int numRatings = object
				.getInt(ParseLevelHelper.ParseCustomLevel.num_ratings_field);
		float rating = numRatings == 0 ? 0 : ((float) totalRating) / numRatings;

		long createdMillis = object.getCreatedAt().getTime();
		CharSequence createdString = DateUtils.getRelativeDateTimeString(this,
				createdMillis, DateUtils.DAY_IN_MILLIS,
				DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);

		title.setText(titleString);
		author.setText("Created by " + authorName + " on " + createdString);
		if (numDownloads == 1) {
			downloads.setText(numDownloads + " download");
		} else {
			downloads.setText(numDownloads + " downloads");
		}

		equation.setText(Html.fromHtml(equationString));
		solvable_desription.setText(solvableDescriptionString);

		ratingBar.setRating(rating);
		num_rankings.setText("" + numRatings);

		if (myParseComment != null) {
			myCommentView.setText(myParseComment
					.getString(ParseLevelRating.comment_field));
			myRatingBar.setRating((float) myParseComment
					.getDouble(ParseLevelRating.rating_field));
		}

	}

	private static class CommentsAdapter extends ArrayAdapter<ParseObject> {
		private static class ViewHolder {
			RatingBar ratingBar;
			TextView commenter;
			TextView comment;
		}

		private Activity context;

		public CommentsAdapter(Activity context, List<ParseObject> list) {
			super(context, R.layout.custom_level_detail_user_rate, list);
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ViewHolder holder;
			if (view == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				view = inflater.inflate(R.layout.custom_level_detail_user_rate,
						null);
				holder = new ViewHolder();
				holder.ratingBar = (RatingBar) view
						.findViewById(R.id.ratingBar);
				holder.commenter = (TextView) view.findViewById(R.id.commenter);
				holder.comment = (TextView) view.findViewById(R.id.comment);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			final ParseObject item = getItem(position);

			String comment = item.getString(ParseLevelRating.comment_field);
			float rating = (float) item
					.getDouble(ParseLevelRating.rating_field);
			ParseUser commentuser = item
					.getParseUser(ParseLevelRating.createdBy_field);
			String author = commentuser == null ? "unknown" : commentuser
					.getString(ParseUserExtra.nickname_field);

			holder.ratingBar.setRating(rating);
			holder.commenter.setText(author);
			if (StringUtils.isEmpty(comment)) {
				holder.comment.setVisibility(View.GONE);
				holder.comment.setText("");
			} else {
				holder.comment.setVisibility(View.VISIBLE);
				holder.comment.setText(comment);
			}
			return view;
		}
	}

	private static class OperationAdapter extends ArrayAdapter<Operation> {
		private static class ViewHolder {
			TextView operation;
		}

		private Activity context;

		public OperationAdapter(Activity context, List<Operation> list) {
			super(context, R.layout.operation_item, list);
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ViewHolder holder;
			if (view == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				view = inflater.inflate(R.layout.operation_item, null);
				holder = new ViewHolder();
				holder.operation = (TextView) view.findViewById(R.id.operation);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			final Operation operation = getItem(position);

			holder.operation.setText(Html.fromHtml(operation.toString()));

			return view;
		}
	}

	protected void downloadAndStartPuzzle() {
		final ProgressDialog dialog = ProgressDialog.show(this,
				"Loading level", "Please wait...");
		AsyncTask<Void, Void, CustomLevelBuilder> task = new AsyncTask<Void, Void, CustomLevelBuilder>() {
			@Override
			protected CustomLevelBuilder doInBackground(Void... params) {
				CustomLevelBuilder builder = ParseLevelHelper.load(
						(FindXApp) getApplication(),
						CustomLevelDetailActivity.this, level);

				builder.save((FindXApp) getApplication());
				Levels.resetCustomStage();
				return builder;
			}

			@Override
			protected void onPostExecute(CustomLevelBuilder builder) {
				dialog.dismiss();
				ICustomLevel newlevel = Levels.getCustomStage().getLevelByDBId(
						builder.getId());
				startPuzzle(newlevel.getId());
			}
		};
		task.execute();
	}

	protected void flagAndCloseLevel() {
		final ProgressDialog dialog = ProgressDialog.show(this,
				"Flagging level", "Please wait...");
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				ParseLevelHelper.flagLevel(level);
				return null;
			}

			@Override
			protected void onPostExecute(Void arg) {
				dialog.dismiss();
				CustomLevelDetailActivity.this.finish();
			}
		};
		task.execute();
	}

	private void startPuzzle(final String levelId) {
		Intent levelIntent = new Intent(CustomLevelDetailActivity.this,
				PuzzleActivity.class);
		levelIntent.putExtra(PuzzleActivity.PUZZLE_ID, levelId);
		levelIntent.putExtra(PuzzleActivity.IS_CUSTOM, true);
		startActivity(levelIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.custom_level_detail, menu);

		final CustomLevelDBReader reader = new CustomLevelDBReader();
		final long dbId = reader.findDbIdByServerId(this, levelId);
		MenuItem play = menu.findItem(R.id.menu_play);
		MenuItem download = menu.findItem(R.id.menu_download);
		if (dbId > 0) {
			download.setVisible(false);
		} else {
			play.setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		} else if (item.getItemId() == R.id.menu_play) {
			final CustomLevelDBReader reader = new CustomLevelDBReader();
			final long dbId = reader.findDbIdByServerId(this, levelId);
			// Ugh-ly..
			ICustomLevel theLevel = Levels.getCustomStage()
					.getLevelByDBId(dbId);
			startPuzzle(theLevel.getId());
			return true;
		} else if (item.getItemId() == R.id.menu_download) {
			downloadAndStartPuzzle();
			return true;
		} else if (item.getItemId() == R.id.menu_flag) {
			flagAndCloseLevel();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}

}
