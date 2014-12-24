package com.oakonell.findx;

import java.util.concurrent.Callable;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.oakonell.findx.model.ILevel;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Stage;

public class StageActivity extends GameActivity {
	public static final String STAGE_ID = "stageId";

	private ArrayAdapter<ILevel> adapter;
	private Stage stage;

	private static class ViewHolder {
		TextView id;
		TextView levelButton;
		View menu;
		ImageView lock;
		RatingBar ratingBar;
		protected String theId;
	}

	static class LevelInfo {
		int rating;
		boolean unlocked;
	}

	interface WithLevelInfo {
		void call(LevelInfo info);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

		setContentView(R.layout.stage);

		final ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayUseLogoEnabled(true);
		ab.setDisplayShowTitleEnabled(true);
		ab.setTitle(R.string.selectLevel);

		Intent intent = getIntent();
		if (stage == null) {
			String stageId = intent.getStringExtra(STAGE_ID);
			if (stageId == null) {
				throw new IllegalArgumentException(
						"StageActivity launched without a stageId!");
			}
			stage = Levels.getStage(stageId);
		}

		View mainView = findViewById(R.id.selectLevel);
		mainView.setBackgroundResource(stage.getBackgroundDrawableResourceId());
		
		TextView label = (TextView) findViewById(R.id.stage_label);
		label.setText(stage.getTitleId());

		GridView levelSelect = (GridView) findViewById(R.id.level_select);

		adapter = new ArrayAdapter<ILevel>(getApplication(),
				R.layout.level_select_grid_item, stage.getLevels()) {

			@Override
			public View getView(int position, View row, ViewGroup parent) {
				ViewHolder holder;
				if (row == null) {
					row = getLayoutInflater().inflate(
							R.layout.level_select_grid_item, parent, false);
					holder = new ViewHolder();
					holder.id = (TextView) row.findViewById(R.id.level_id);
					holder.levelButton = (TextView) row
							.findViewById(R.id.level_name);
					holder.menu = row.findViewById(R.id.menu);
					holder.lock = (ImageView) row.findViewById(R.id.lock);
					holder.ratingBar = (RatingBar) row
							.findViewById(R.id.rating);

					row.setTag(holder);
				} else {
					holder = (ViewHolder) row.getTag();
				}

				final ILevel level = getItem(position);

				holder.id.setText(level.getId());
				holder.levelButton.setText(level.getName());
				holder.menu.setVisibility(View.INVISIBLE);

				holder.levelButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						startPuzzle(level.getId());
					}
				});

				final ViewHolder theHolder = holder;
				final View theRow = row;
				theHolder.ratingBar.setVisibility(View.INVISIBLE);
				theHolder.theId = level.getId();

				final Callable<LevelInfo> levelInfoCallable = new Callable<StageActivity.LevelInfo>() {
					@Override
					public LevelInfo call() throws Exception {
						LevelInfo info = new LevelInfo();
						info.rating = level.getRating();
						info.unlocked = level.isUnlocked();
						return info;
					}
				};
				final WithLevelInfo withInfo = new WithLevelInfo() {
					public void call(LevelInfo info) {
						if (!theHolder.theId.equals(level.getId()))
							return;

						theHolder.ratingBar
								.setVisibility(info.rating > 0 ? View.VISIBLE
										: View.INVISIBLE);
						theHolder.ratingBar.setRating(info.rating);
						if (info.unlocked) {
							theRow.setClickable(true);
							theHolder.levelButton.setEnabled(true);
							theHolder.lock.setVisibility(View.INVISIBLE);
						} else {
							theRow.setClickable(false);
							theHolder.levelButton.setEnabled(false);
							theHolder.lock.setVisibility(View.VISIBLE);
						}

					};
				};

				// with async, it "flickers" a little, and looks not pretty
				// with there only being ~12 levels in each pre-defined stage, this is not a big, noticable hit to not be async
				boolean useAsync = false;
				if (!useAsync) {
					LevelInfo info;
					try {
						info = levelInfoCallable.call();
					} catch (Exception e) {
						throw new RuntimeException("Error getting level info",
								e);
					}
					withInfo.call(info);
				} else {

					AsyncTask<Void, Void, LevelInfo> asyncTask = new AsyncTask<Void, Void, LevelInfo>() {
						@Override
						protected LevelInfo doInBackground(Void... params) {
							try {
								return levelInfoCallable.call();
							} catch (Exception e) {
								throw new RuntimeException(
										"Error getting level info", e);
							}
						}

						@Override
						protected void onPostExecute(LevelInfo info) {
							withInfo.call(info);
						}
					};
					asyncTask.execute();
				}

				return row;
			}
		};

		levelSelect.setAdapter(adapter);
		levelSelect
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						ILevel level = adapter.getItem(position);
						startPuzzle(level.getId());
					}
				});

		adapter.notifyDataSetChanged();

		BackgroundMusicHelper.onActivtyCreate(this, stage.getBgMusicId());
	}

	private void startPuzzle(final String levelId) {
		BackgroundMusicHelper.continueMusicOnNextActivity();
		Intent levelIntent = new Intent(StageActivity.this,
				PuzzleActivity.class);
		levelIntent.putExtra(PuzzleActivity.PUZZLE_ID, levelId);
		startActivity(levelIntent);
	}

	@Override
	protected void onRestart() {
		if (adapter != null) {
			// update ratings and enablement of the level buttons
			adapter.notifyDataSetChanged();
		}
		super.onRestart();
	}

	@Override
	protected void onResume() {
		BackgroundMusicHelper.onActivityResume(this, stage.getBgMusicId());
		if (adapter != null) {
			// update ratings and enablement of the level buttons
			adapter.notifyDataSetChanged();
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		BackgroundMusicHelper.onActivityDestroy();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		BackgroundMusicHelper.onActivityPause();
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return MenuHelper.onOptionsItemSelected(this, item);
	}

}
