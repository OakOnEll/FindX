package com.oakonell.findx;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuInflater;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;
import com.oakonell.findx.Achievements.AchievementContext;
import com.oakonell.findx.custom.CustomStageActivity;
import com.oakonell.findx.custom.parse.ParseConnectivity;
import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Puzzle;
import com.oakonell.findx.model.Stage;
import com.oakonell.utils.activity.AppLaunchUtils;

public class ChooseStageActivity extends GameActivity implements
		AchievementContext {
	private ArrayAdapter<Stage> adapter;
	private static final int RC_UNUSED = 0;

	private View signOutView;
	private View signInView;
	protected Runnable onSignIn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		checkForPendingPuzzle();

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
		ab.setDisplayHomeAsUpEnabled(false);
		ab.setDisplayUseLogoEnabled(true);
		ab.setDisplayShowTitleEnabled(true);

		setContentView(R.layout.choose_stage);

		signInView = findViewById(R.id.sign_in_bar);
		signOutView = findViewById(R.id.sign_out_bar);
		SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
		signInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getGameHelper().beginUserInitiatedSignIn();
			}
		});

		Button signOutButton = (Button) findViewById(R.id.sign_out_button);
		signOutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ParseConnectivity.logout();
				getGameHelper().signOut();
				signOut();
				// show login button
				findViewById(R.id.sign_in_bar).setVisibility(View.VISIBLE);
				// Sign-in failed, so show sign-in button on main menu
				findViewById(R.id.sign_out_bar).setVisibility(View.INVISIBLE);
			}
		});
		//
		// waiting = (ProgressBar) view.findViewById(R.id.waiting);
		// if (isWaiting) {
		// setInactive();
		// } else {
		// setActive();
		// }

		if (getGameHelper().isSignedIn()) {
			showLogout();
		} else {
			showLogin();
		}

		View viewAchievements = findViewById(R.id.view_achievements);
		viewAchievements.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getGameHelper().isSignedIn()) {
					startActivityForResult(Games.Achievements
							.getAchievementsIntent(getGameHelper()
									.getApiClient()), RC_UNUSED);
				} else {
					onSignIn = new Runnable() {
						@Override
						public void run() {
							startActivityForResult(Games.Achievements
									.getAchievementsIntent(getGameHelper()
											.getApiClient()), RC_UNUSED);													
						}
					};
					getGameHelper().beginUserInitiatedSignIn();
				}
			}
		});

		GridView stageSelect = (GridView) findViewById(R.id.stage_select);

		adapter = new ArrayAdapter<Stage>(getApplication(),
				R.layout.stage_select_grid_item, Levels.getStages()) {

			@Override
			public View getView(int position, View inputRow, ViewGroup parent) {
				View row = inputRow;
				if (row == null) {
					row = getLayoutInflater().inflate(
							R.layout.stage_select_grid_item, parent, false);
				}

				final Stage stage = getItem(position);
				TextView id = (TextView) row.findViewById(R.id.level_id);
				id.setText(stage.getId());

				TextView stageButton = (TextView) row
						.findViewById(R.id.level_name);
				stageButton.setText(stage.getTitleId());

				stageButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						startStage(stage.getId());
					}
				});

				ImageView lock = (ImageView) row.findViewById(R.id.lock);

				// TODO db hit?  (But may flicker annoyingly if attempt to async it- small number of calls to db may not warrant it)
				if (stage.isUnlocked()) {
					row.setEnabled(true);
					stageButton.setEnabled(true);
					lock.setVisibility(View.INVISIBLE);
				} else {
					row.setEnabled(false);
					stageButton.setEnabled(false);
					lock.setVisibility(View.VISIBLE);
				}
				return row;
			}

		};

		stageSelect.setAdapter(adapter);
		// stageSelect
		// .setOnItemClickListener(new AdapterView.OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view,
		// int position, long id) {
		// Stage stage = adapter.getItem(position);
		// startStage(stage.getId());
		// }
		// });

		Button buildLevel = (Button) findViewById(R.id.custom);
		buildLevel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent levelIntent = new Intent(ChooseStageActivity.this,
						CustomStageActivity.class);
				startActivity(levelIntent);
			}
		});
		AppLaunchUtils.appLaunched(this, null);
		BackgroundMusicHelper.onActivtyCreate(this,
				R.raw.prelude_no_8_in_e_flat_minor_loop);
	}

	private void startStage(final String stageId) {
		Intent levelIntent = new Intent(ChooseStageActivity.this,
				StageActivity.class);
		levelIntent.putExtra(StageActivity.STAGE_ID, stageId);
		startActivity(levelIntent);
	}

	private void checkForPendingPuzzle() {
		AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				DataBaseHelper helper = new DataBaseHelper(
						ChooseStageActivity.this);
				SQLiteDatabase db = helper.getWritableDatabase();

				String id = Puzzle.readPendingLevel(db);
				db.close();
				return id;
			}

			@Override
			protected void onPostExecute(String id) {
				if (id != null) {
					startPuzzle(id);
				}
			}
		};
		asyncTask.execute();
	}

	private void startPuzzle(final String levelId) {
		Intent levelIntent = new Intent(ChooseStageActivity.this,
				PuzzleActivity.class);
		levelIntent.putExtra(PuzzleActivity.PUZZLE_ID, levelId);
		startActivity(levelIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {

		return MenuHelper.onOptionsItemSelected(this, item);
	}

	@Override
	protected void onPause() {
		BackgroundMusicHelper.onActivityPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		BackgroundMusicHelper.onActivityResume(this,
				R.raw.prelude_no_8_in_e_flat_minor_loop);
		if (adapter != null) {
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
	public void onSignInFailed() {
		onSignIn = null;
		super.onSignInFailed();
		showLogin();
	}

	@Override
	public void onSignInSucceeded() {
		showLogout();

		ParseConnectivity.connect(this, getGameHelper());

		FindXApp app = (FindXApp) getApplication();
		Intent settingsIntent = Games.getSettingsIntent(getApiClient());
		app.setSettingsIntent(settingsIntent);

		Achievements achievements = app.getAchievements();
		if (achievements.hasPending()) {
			achievements.pushToGoogle(this);
		}
		
		if (onSignIn != null) {
			onSignIn.run();
		}

	}

	private void showLogin() {
		if (signInView == null)
			return;

		// show login button
		signInView.setVisibility(View.VISIBLE);
		// Sign-in failed, so show sign-in button on main menu
		signOutView.setVisibility(View.INVISIBLE);
	}

	private void showLogout() {
		// disable/hide login button
		if (signInView == null)
			return;

		signInView.setVisibility(View.INVISIBLE);
		// show sign out button
		signOutView.setVisibility(View.VISIBLE);
		TextView signedInAsText = (TextView) findViewById(R.id.signed_in_as_text);
		if (signedInAsText == null)
			return;
		String name = Games.Players.getCurrentPlayer(
				getGameHelper().getApiClient()).getDisplayName();
		// Games.getCurrentAccountName(getGameHelper().getApiClient())
		signedInAsText.setText(getResources().getString(
				R.string.you_are_signed_in_as, name));
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
