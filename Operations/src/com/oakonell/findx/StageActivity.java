package com.oakonell.findx;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.GameHelper;
import com.oakonell.findx.Achievements.AchievementContext;
import com.oakonell.findx.model.Level;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Stage;

public class StageActivity extends BaseGameActivity implements
		AchievementContext {
	public static final String STAGE_ID = "stageId";

	private ArrayAdapter<Level> adapter;
	private Stage stage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.stage);

		Intent intent = getIntent();
		if (stage == null) {
			String stageId = intent.getStringExtra(STAGE_ID);
			if (stageId == null) {
				throw new IllegalArgumentException(
						"StageActivity launched without a stageId!");
			}
			stage = Levels.getStage(stageId);
		}

		GridView levelSelect = (GridView) findViewById(R.id.level_select);

		adapter = new ArrayAdapter<Level>(getApplication(),
				R.layout.level_select_grid_item, stage.getLevels()) {

			@Override
			public View getView(int position, View row, ViewGroup parent) {
				if (row == null) {
					row = getLayoutInflater().inflate(
							R.layout.level_select_grid_item, parent, false);
				}

				final Level level = getItem(position);
				TextView id = (TextView) row.findViewById(R.id.level_id);
				id.setText(level.getId());

				Button levelButton = (Button) row.findViewById(R.id.level_name);
				levelButton.setText(level.getName());

				levelButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						startPuzzle(level.getId());
					}
				});

				ImageView lock = (ImageView) row.findViewById(R.id.lock);

				int rating = level.getRating();
				RatingBar ratingBar = (RatingBar) row.findViewById(R.id.rating);
				ratingBar.setVisibility(rating > 0 ? View.VISIBLE
						: View.INVISIBLE);
				ratingBar.setRating(rating);
				if (level.isUnlocked()) {
					levelButton.setEnabled(true);
					lock.setVisibility(View.INVISIBLE);
				} else {
					levelButton.setEnabled(false);
					lock.setVisibility(View.VISIBLE);
				}
				return row;
			}
		};

		levelSelect.setAdapter(adapter);

		adapter.notifyDataSetChanged();

		ImageView back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent levelIntent = new Intent(StageActivity.this,
						ChooseStageActivity.class);
				startActivity(levelIntent);
				StageActivity.this.finish();
			}
		});
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
	public boolean onCreateOptionsMenu(Menu menu) {
		return MenuHelper.onCreateOptionsMenu(this, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return MenuHelper.onOptionsItemSelected(this, item);
	}

	@Override
	public void onSignInFailed() {
		Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSignInSucceeded() {
		FindXApp app = (FindXApp) getApplication();
		Intent settingsIntent = Games.getSettingsIntent(getApiClient());
		app.setSettingsIntent(settingsIntent);
	}

	@Override
	public GameHelper getHelper() {
		return getGameHelper();
	}

	@Override
	public Context getContext() {
		return this;
	}
}
