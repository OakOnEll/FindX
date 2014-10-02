package com.oakonell.findx;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.GameHelper;
import com.oakonell.findx.Achievements.AchievementContext;
import com.oakonell.findx.DelayedTextView.SoundInfo;
import com.oakonell.findx.DelayedTextView.TextViewInfo;
import com.oakonell.findx.custom.CustomStageActivity;
import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.model.Level;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.Puzzle;
import com.oakonell.utils.share.ShareHelper;

public class PuzzleActivity extends BaseGameActivity implements
		AchievementContext {
	private static final int FINISHED_DELAY_MS = 250;
	public static final String PUZZLE_ID = "puzzleId";
	public static final String IS_CUSTOM = "custom";

	private static final int BOO_LENGTH_MS = 1500;
	private static final int ERASE_LENGTH_MS = 1500;
	private static final int UNDO_ERASE_MS = 700;

	private static enum Sounds {
		APPLAUSE, BOO, ERASE, UNDO, CHALK;
	}

	private static final int DIALOG_LEVEL_FINISHED = 1;

	private Puzzle puzzle;
	private boolean saveState = true;
	private boolean fromCustom = false;

	private ArrayAdapter<Move> adapter;
	private SoundManager soundManager;
	private Typeface chalkFont;

	private boolean animateNewMove;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.puzzle);

		chalkFont = Typeface.createFromAsset(getAssets(), "fonts/lcchalk_.ttf");

		soundManager = new SoundManager(this);
		soundManager.addSound(Sounds.APPLAUSE, R.raw.applause);
		soundManager.addSound(Sounds.BOO, R.raw.boo);
		soundManager.addSound(Sounds.ERASE, R.raw.chalkboard_erase);
		soundManager.addSound(Sounds.UNDO, R.raw.chalkboard_erase_short);
		soundManager.addSound(Sounds.CHALK, R.raw.writing_on_chalkboard1);

		saveState = true;

		Intent intent = getIntent();
		if (puzzle == null) {
			String puzzleId = intent.getStringExtra(PUZZLE_ID);
			puzzle = new Puzzle(puzzleId);
		}
		fromCustom = intent.getBooleanExtra(IS_CUSTOM, false);

		BackgroundMusicHelper.onActivtyCreate(this, puzzle.getStage()
				.getBgMusicId());

		TextView levelLabel = (TextView) findViewById(R.id.level_label);
		levelLabel.setTypeface(chalkFont);

		TextView name = (TextView) findViewById(R.id.level_name);
		name.setText(puzzle.getName());
		name.setTypeface(chalkFont);

		TextView id = (TextView) findViewById(R.id.level_id);
		id.setText(puzzle.getId());
		id.setTypeface(chalkFont);

		TextView minMovesLabel = (TextView) findViewById(R.id.min_moves_label);
		minMovesLabel.setTypeface(chalkFont);
		TextView minMoves = (TextView) findViewById(R.id.min_moves);
		minMoves.setText(Integer.toString(puzzle.getMinMoves()));
		minMoves.setTypeface(chalkFont);

		final ListView movesView = (ListView) findViewById(R.id.moves);

		// LayoutInflater inflater = getLayoutInflater();
		// View header = inflater.inflate(R.layout.move, (ViewGroup)
		// findViewById(R.id.move_root));
		// movesView.addHeaderView(header, null, false);

		TextView moveNum = (TextView) findViewById(R.id.move_num);
		moveNum.setText(R.string.move_num);
		moveNum.setTypeface(chalkFont);

		TextView operation = (TextView) findViewById(R.id.operation);
		operation.setText(R.string.operation);
		operation.setTypeface(chalkFont);

		TextView equation = (TextView) findViewById(R.id.equation);
		equation.setText(R.string.equation);
		equation.setTypeface(chalkFont);

		ViewGroup startEquationLayout = (ViewGroup) findViewById(R.id.start_equation_move);
		startEquationLayout.findViewById(R.id.move_num).setVisibility(
				View.INVISIBLE);
		startEquationLayout.findViewById(R.id.operation).setVisibility(
				View.INVISIBLE);
		TextView startEquationView = (TextView) startEquationLayout
				.findViewById(R.id.equation);
		startEquationView.setTypeface(chalkFont);
		startEquationView.setText(puzzle.getStartEquation().toString());

		ViewGroup headerSeparatorLayout = (ViewGroup) findViewById(R.id.header_separator_layout);
		TextView headerMoveNumView = (TextView) headerSeparatorLayout
				.findViewById(R.id.move_num);
		headerMoveNumView.setText("---");
		headerMoveNumView.setTypeface(chalkFont);
		TextView headerOperationView = (TextView) headerSeparatorLayout
				.findViewById(R.id.operation);
		headerOperationView.setText("-----------");
		headerOperationView.setTypeface(chalkFont);
		TextView headerEquationView = (TextView) headerSeparatorLayout
				.findViewById(R.id.equation);
		headerEquationView.setTypeface(chalkFont);
		headerEquationView.setText("-------------");

		initializeAdapter(movesView);

		configureOperationButtons(movesView);

		final Button undo = (Button) findViewById(R.id.undo);
		handleUndoEnabling();
		undo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (puzzle.isSolved()) {
					return;
				}
				if (!puzzle.canUndo()) {
					// huh, model out of date...?
					handleUndoEnabling();
					handleRestartEnabling();
					return;
				}

				Runnable undoContinuation = new Runnable() {
					@Override
					public void run() {
						puzzle.undo();
						adapter.notifyDataSetChanged();
						handleUndoEnabling();
						handleRestartEnabling();
					}
				};

				soundManager.playSound(Sounds.UNDO);
				int lastIndex = puzzle.getMoves().size() - 1;
				int lastVisiblePosition = movesView.getLastVisiblePosition();
				int visiblePosition = movesView.getFirstVisiblePosition();
				// start fade animation
				if (lastVisiblePosition <= lastIndex) {
					// the move is visible, animate it to disappear
					View move = movesView.getChildAt(lastIndex
							- visiblePosition);
					if (move != null) {
						animateFade(move, PuzzleActivity.this, UNDO_ERASE_MS,
								undoContinuation);
					} else {
						undoContinuation.run();
					}
				} else {
					undoContinuation.run();
				}
			}
		});

		Button restart = (Button) findViewById(R.id.restart);
		handleRestartEnabling();
		restart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (puzzle.isSolved()) {
					return;
				}
				if (!puzzle.hasAnyMoves()) {
					restart(false);
					return;
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(
						PuzzleActivity.this);
				builder.setMessage(R.string.restart_level)
						.setCancelable(true)
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
										restart(true);
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				builder.show();
			}
		});

		Button giveUp = (Button) findViewById(R.id.give_up);
		giveUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (puzzle.isSolved()) {
					return;
				}
				confirmAndQuit();
			}
		});

	}

	private void initializeAdapter(final ListView movesView) {
		adapter = new ArrayAdapter<Move>(getApplication(), R.layout.move,
				puzzle.getMoves()) {

			@Override
			public View getView(int position, View inputRow, ViewGroup parent) {
				View row = inputRow;
				if (row == null) {
					row = getLayoutInflater().inflate(R.layout.move, parent,
							false);
				}
				row.setVisibility(View.VISIBLE);

				Move item = getItem(position);

				DelayedTextView moveNum = (DelayedTextView) row
						.findViewById(R.id.move_num);
				DelayedTextView op = (DelayedTextView) row
						.findViewById(R.id.operation);
				DelayedTextView eq = (DelayedTextView) row
						.findViewById(R.id.equation);

				if (inputRow == null) {
					moveNum.setTypeface(chalkFont);
					op.setTypeface(chalkFont);
					eq.setTypeface(chalkFont);
				}

				String moveNumString = (position + 1) + "";
				Operation operation = item.getOperation();
				List<Move> moves = puzzle.getMoves();
				int index = moves.indexOf(item);
				Log.i("Puzzle", (inputRow == null ? "inputRow is null" : "")
						+ ", for move " + index + " out of " + moves.size()
						+ " moves: animate = " + animateNewMove);
				if (index != -1 && index == moves.size() - 1 && animateNewMove) {
					// clear it out- it is a reused view
					moveNum.writeText("");
					op.writeText("");
					eq.writeText("");

					animateNewMove = false;
					SoundInfo soundInfo = new SoundInfo();
					soundInfo.soundManager = soundManager;
					soundInfo.streamId = soundManager.playSound(Sounds.CHALK,
							true);

					TextViewInfo eqInfo = new TextViewInfo();
					eqInfo.text = item.getEndEquation().toString();
					eqInfo.textView = eq;
					eqInfo.animationFinished = new Runnable() {
						@Override
						public void run() {
							moveAnimationFinished();
						}
					};

					TextViewInfo opInfo = new TextViewInfo();
					opInfo.text = operation != null ? operation.toString() : "";
					opInfo.textView = op;
					opInfo.next = eqInfo;

					TextViewInfo moveInfo = new TextViewInfo();
					moveInfo.text = moveNumString;
					moveInfo.textView = moveNum;
					moveInfo.next = opInfo;

					moveNum.writeWithDelay(soundInfo, moveInfo);
					// moveNum.setText(moveNumString);
					// op.setText(operation != null ? operation.toString() :
					// "");
					// eq.setText(item.getEndEquation().toString());
				} else {
					moveNum.writeText(moveNumString);
					op.writeText(operation != null ? operation.toString() : "");
					eq.writeText(item.getEndEquation().toString());
				}
				return row;
			}

		};

		movesView.setAdapter(adapter);
	}

	private void configureOperationButtons(final ListView movesView) {
		int i = 1;
		for (Operation each : puzzle.getOperations()) {
			int resourceId = 0;
			switch (i) {
			case 1:
				resourceId = R.id.op1;
				break;
			case 2:
				resourceId = R.id.op2;
				break;
			case 3:
				resourceId = R.id.op3;
				break;
			case 4:
				resourceId = R.id.op4;
				break;
			case 5:
				resourceId = R.id.op5;
				break;
			default:
				throw new RuntimeException("Unexpected number of operations");
			}
			Button op1 = (Button) findViewById(resourceId);
			configureOperationButton(movesView, puzzle.getMoves(), adapter,
					each, op1);
			i++;
		}
	}

	private void configureOperationButton(final ListView movesView,
			final List<Move> moves, final ArrayAdapter<Move> adapter,
			final Operation operation1, Button op1) {
		op1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (puzzle.isSolved()) {
					return;
				}
				// soundManager.playSound(operation1.type());
				// TODO adjust for moves not having starting equation as special
				// move
				puzzle.apply(operation1);
				animateNewMove = true;
				handleUndoEnabling();
				handleRestartEnabling();

				// adapter.notifyDataSetChanged();
				movesView.setSelection(moves.size() - 1);

				if (puzzle.isSolved()) {
					levelIsFinished = true;
					// wait for animation to finish
				}
			}
		});
		op1.setText(operation1.toString());
		op1.setVisibility(View.VISIBLE);
	}

	private boolean levelIsFinished;

	private void moveAnimationFinished() {
		if (levelIsFinished) {
			Handler handler = new Handler();
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					levelFinished();
				}
			};
			handler.postDelayed(runnable, FINISHED_DELAY_MS);
		}
	}

	protected void handleUndoEnabling() {
		Button undo = (Button) PuzzleActivity.this.findViewById(R.id.undo);
		undo.setEnabled(puzzle.canUndo());
		undo.setText(getString(R.string.undo) + "(" + puzzle.getUndosLeft()
				+ ")");
	}

	protected void handleRestartEnabling() {
		Button undo = (Button) PuzzleActivity.this.findViewById(R.id.restart);
		undo.setEnabled(puzzle.hasAnyMoves() || puzzle.getUndosUsed() > 0);
	}

	protected void levelFinished() {
		showDialog(DIALOG_LEVEL_FINISHED);
	}

	private void restart(boolean animate) {
		Runnable restart = new Runnable() {
			@Override
			public void run() {
				saveState = false;
				Intent restartLevel = new Intent(PuzzleActivity.this,
						PuzzleActivity.class);
				restartLevel.putExtra(PUZZLE_ID, puzzle.getId());
				restartLevel.putExtra(IS_CUSTOM, fromCustom);
				BackgroundMusicHelper.continueMusicOnNextActivity();
				PuzzleActivity.this.startActivity(restartLevel);
				finish();
			}
		};

		if (animate && soundManager.shouldPlayFx()) {
			soundManager.playSound(Sounds.ERASE);
			animateFade(findViewById(R.id.moves), this, ERASE_LENGTH_MS,
					restart);
		} else {
			restart.run();
		}
	}

	public static void animateFade(final View panel, Context ctx, int millis,
			final Runnable continuation) {
		Animation animation = new AlphaAnimation(1.0f, 0.0f);
		animation.setDuration(millis);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				panel.setVisibility(View.INVISIBLE);
				if (continuation != null) {
					continuation.run();
				}
			}
		});

		panel.startAnimation(animation);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LEVEL_FINISHED:
			final Dialog dialog = new Dialog(this, R.style.LevelCompleteDialog);
			dialog.setCancelable(false);

			dialog.setContentView(R.layout.level_finished);
			dialog.setTitle(R.string.level_finished_title);

			int rating = puzzle.getRating();
			int existingRating = puzzle.getExistingRating();

			FindXApp app = (FindXApp) getApplication();
			app.getAchievements().testAndSetLevelCompleteAchievements(this,
					puzzle);

			puzzle.updateRating();

			TextView finishText = (TextView) dialog
					.findViewById(R.id.level_finish_text);
			soundManager.playSound(Sounds.APPLAUSE);
			if (rating > existingRating) {
				if (existingRating > 0) {
					finishText.setText(R.string.level_new_record);
				} else {
					finishText.setText(R.string.level_solved);
				}
			} else if (rating == existingRating) {
				finishText.setText(R.string.level_record_tied);
			} else {
				finishText.setText(R.string.level_solved);
			}

			RatingBar ratingBar = (RatingBar) dialog.findViewById(R.id.rating);
			ratingBar.setRating(rating);
			ratingBar.setEnabled(false);

			Button retry = (Button) dialog.findViewById(R.id.retry);
			retry.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					restart(true);
				}
			});

			Button nextLevelButton = (Button) dialog
					.findViewById(R.id.next_level);
			final Level nextLevel = puzzle.getNextLevel();
			if (nextLevel == null) {
				nextLevelButton.setEnabled(false);
			} else {
				nextLevelButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						saveState = false;
						dialog.dismiss();
						Runnable nextLevelRunnable = new Runnable() {
							@Override
							public void run() {
								BackgroundMusicHelper
										.continueMusicOnNextActivity();
								Intent nextLevelIntent = new Intent(
										PuzzleActivity.this,
										PuzzleActivity.class);
								nextLevelIntent.putExtra(PUZZLE_ID,
										nextLevel.getId());
								nextLevelIntent.putExtra(IS_CUSTOM, fromCustom);
								PuzzleActivity.this
										.startActivity(nextLevelIntent);
								finish();
							}
						};
						soundManager.playSound(Sounds.ERASE);
						animateFade(PuzzleActivity.this
								.findViewById(R.id.main_layout),
								PuzzleActivity.this, ERASE_LENGTH_MS,
								nextLevelRunnable);
					}
				});
			}

			Button share = (Button) dialog.findViewById(R.id.share);
			share.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					SharedPreferences preferences = PreferenceManager
							.getDefaultSharedPreferences(PuzzleActivity.this);
					String defaultText = PuzzleActivity.this
							.getString(R.string.default_level_finish_share_message);
					String levelName = puzzle.getId() + "-" + puzzle.getName();

					String titleText = PuzzleActivity.this
							.getString(R.string.level_finish_share_level_finish_title);
					titleText = titleText.replaceAll("%l", levelName);

					String parametrizedText = preferences.getString(
							PuzzleActivity.this
									.getString(R.string.pref_default_share_level_finish_key),
							defaultText);

					String text = parametrizedText.replaceAll("%l", levelName);
					text = text.replaceAll("%m", ""
							+ (puzzle.getMoves().size() - 1));
					text = text.replaceAll("%u", "" + puzzle.getUndosUsed());
					text = text.replaceAll("%d",
							"" + puzzle.getMultilineDescription());

					ShareHelper.share(PuzzleActivity.this, titleText, text);
				}
			});

			Button mainMenu = (Button) dialog.findViewById(R.id.goto_main_menu);
			mainMenu.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					saveState = false;
					launchLevelSelect();
					finish();
				}

			});

			return dialog;
		default:
			return null;
		}

	}

	@Override
	public void onBackPressed() {
		if (!puzzle.hasAnyMoves()) {
			saveState = false;
			launchLevelSelect();
			return;
		}
		confirmAndQuit();
	}

	private void confirmAndQuit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				PuzzleActivity.this);
		builder.setMessage(R.string.quit_level)
				.setCancelable(true)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog,
									int id) {
								saveState = false;
								Runnable exit = new Runnable() {
									@Override
									public void run() {
										launchLevelSelect();
										dialog.dismiss();

									}
								};
								if (soundManager.shouldPlayFx()) {
									Handler handler = new Handler();
									soundManager.playSound(Sounds.BOO);
									handler.postDelayed(exit, BOO_LENGTH_MS);
								} else {
									exit.run();
								}
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		builder.show();
	}

	@Override
	protected void onPause() {
		BackgroundMusicHelper.onActivityPause();
		if (saveState) {
			// persist the moves
			DataBaseHelper helper = new DataBaseHelper(this);
			SQLiteDatabase db = helper.getWritableDatabase();

			puzzle.writeState(db);
			db.close();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		BackgroundMusicHelper.onActivityResume(this, puzzle.getStage()
				.getBgMusicId());

		// restore the moves
		DataBaseHelper helper = new DataBaseHelper(this);
		SQLiteDatabase db = helper.getWritableDatabase();

		Puzzle.readState(db, puzzle.getId(), puzzle);
		db.close();

		adapter.notifyDataSetChanged();
	}

	private void launchLevelSelect() {
		Class<? extends Activity> toLaunch = StageActivity.class;
		if (fromCustom) {
			toLaunch = CustomStageActivity.class;
		}
		BackgroundMusicHelper.continueMusicOnNextActivity();
		Intent levelSelect = new Intent(PuzzleActivity.this, toLaunch);
		if (!fromCustom) {
			levelSelect.putExtra(StageActivity.STAGE_ID, puzzle.getStage()
					.getId());
		}
		levelSelect.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(levelSelect);
		finish();
	}

	@Override
	protected void onDestroy() {
		BackgroundMusicHelper.onActivityDestroy();
		soundManager.release();
		super.onDestroy();
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
