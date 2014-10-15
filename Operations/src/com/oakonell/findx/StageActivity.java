package com.oakonell.findx;

import android.content.Intent;
import android.os.Bundle;
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
import com.oakonell.findx.model.Level;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Stage;

public class StageActivity extends GameActivity {
	public static final String STAGE_ID = "stageId";

	private ArrayAdapter<Level> adapter;
	private Stage stage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		TextView label = (TextView)findViewById(R.id.stage_label);
		label.setText(stage.getTitleId());
		
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

				TextView levelButton = (TextView) row
						.findViewById(R.id.level_name);
				levelButton.setText(level.getName());

				row.findViewById(R.id.menu).setVisibility(View.INVISIBLE);

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
					row.setClickable(true);
					levelButton.setEnabled(true);
					lock.setVisibility(View.INVISIBLE);
				} else {
					row.setClickable(false);
					levelButton.setEnabled(false);
					lock.setVisibility(View.VISIBLE);
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
						Level level = adapter.getItem(position);
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
