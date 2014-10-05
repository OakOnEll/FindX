package com.oakonell.findx.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.math3.fraction.Fraction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.undobar.UndoBarController;
import com.cocosw.undobar.UndoBarController.UndoListener;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.GameHelper;
import com.oakonell.findx.Achievements;
import com.oakonell.findx.Achievements.AchievementContext;
import com.oakonell.findx.BackgroundMusicHelper;
import com.oakonell.findx.ChooseStageActivity;
import com.oakonell.findx.FindXApp;
import com.oakonell.findx.MenuHelper;
import com.oakonell.findx.PuzzleActivity;
import com.oakonell.findx.R;
import com.oakonell.findx.custom.PopupMenuDialogFragment.OnItemSelected;
import com.oakonell.findx.custom.model.CustomLevel;
import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.custom.model.CustomLevelDBReader;
import com.oakonell.findx.custom.model.CustomLevelDBWriter;
import com.oakonell.findx.custom.model.CustomStage;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Level;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.OperationVisitor;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.Divide;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.Subtract;
import com.oakonell.findx.model.ops.Swap;
import com.oakonell.utils.StringUtils;
import com.oakonell.utils.Utils;
import com.oakonell.utils.activity.dragndrop.DragController;
import com.oakonell.utils.activity.dragndrop.DragLayer;
import com.oakonell.utils.activity.dragndrop.DragSource;
import com.oakonell.utils.activity.dragndrop.DragView;
import com.oakonell.utils.activity.dragndrop.OnDropListener;
import com.oakonell.utils.share.ShareHelper;
import com.oakonell.utils.xml.XMLUtils;
import com.parse.ParseUser;

public class CustomStageActivity extends BaseGameActivity implements
		AchievementContext {
	private static final int DISMISS_BAR_DURATION = 2000;

	private ArrayAdapter<Level> adapter;
	private CustomStage stage;
	private DragController mDragController;
	private DragLayer mDragLayer;

	private List<Level> levels;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_stage);

		// TODO do this in an AsyncTask
		stage = Levels.getCustomStage();

		ImageView search = (ImageView) findViewById(R.id.search);
		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent levelIntent = new Intent(CustomStageActivity.this,
						CustomLevelSearchActivity.class);
				startActivity(levelIntent);

			}
		});

		GridView levelSelect = (GridView) findViewById(R.id.level_select);

		mDragController = new DragController(this);
		mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
		mDragLayer.setDragController(mDragController);
		mDragLayer.setGridView(levelSelect);

		mDragController.setDragListener(mDragLayer);

		levels = new ArrayList<Level>(stage.getLevels());
		adapter = new ArrayAdapter<Level>(getApplication(),
				R.layout.level_select_grid_item, levels) {

			@Override
			public View getView(int position, View inputRow, ViewGroup parent) {
				final CustomLevel level = (CustomLevel) getItem(position);

				final CustomLevelGridCell row;
				if (inputRow == null) {
					row = (CustomLevelGridCell) getLayoutInflater().inflate(
							R.layout.level_select_grid_item, parent, false);
				} else {
					row = (CustomLevelGridCell) inputRow;
				}
				row.setLevel(level);
				row.setOnDropListener(new OnDropListener() {
					@Override
					public void onDrop(View target, DragSource source, int x,
							int y, int xOffset, int yOffset, DragView dragView,
							Object dragInfo) {
						CustomLevelGridCell s = (CustomLevelGridCell) source;
						CustomLevel movedLevel = s.getLevel();
						CustomLevel myLevel = row.getLevel();
						stage.reorderFromTo(movedLevel, myLevel);
						levels.clear();
						levels.addAll(stage.getLevels());
						adapter.notifyDataSetChanged();
					}

					@Override
					public boolean acceptDrop(View target, DragSource source,
							int x, int y, int xOffset, int yOffset,
							DragView dragView, Object dragInfo) {
						return true;
					}

				});

				TextView id = (TextView) row.findViewById(R.id.level_id);
				id.setText(level.getId());

				final TextView levelButton = (TextView) row
						.findViewById(R.id.level_name);
				levelButton.setText(level.getName());

				View menu = row.findViewById(R.id.menu);
				menu.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						List<LevelMenuItem> menuItems = new ArrayList<CustomStageActivity.LevelMenuItem>();
						addMenuItems(level, menuItems);
						showPopupMenu(menuItems, v);
					}

				});

				int rating = level.getRating();
				RatingBar ratingBar = (RatingBar) row.findViewById(R.id.rating);
				ratingBar.setVisibility(rating > 0 ? View.VISIBLE
						: View.INVISIBLE);
				ratingBar.setRating(rating);

				ImageView lock = (ImageView) row.findViewById(R.id.lock);

				lock.setVisibility(View.INVISIBLE);

				View authorLayout = row.findViewById(R.id.author_layout);
				authorLayout.setVisibility(View.VISIBLE);

				boolean isImported = level.isImported();
				TextView authorText = (TextView) row.findViewById(R.id.author);

				if (isImported) {
					row.findViewById(R.id.byLabel).setVisibility(View.VISIBLE);
					authorText.setVisibility(View.VISIBLE);
					authorText.setText(level.getAuthor());
				} else {
					row.findViewById(R.id.byLabel)
							.setVisibility(View.INVISIBLE);
					authorText.setVisibility(View.INVISIBLE);
					authorText.setText("");
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
		levelSelect
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						return startDrag(view);
					}
				});

		adapter.notifyDataSetChanged();

		ImageView back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent levelIntent = new Intent(CustomStageActivity.this,
						ChooseStageActivity.class);
				startActivity(levelIntent);
				CustomStageActivity.this.finish();
			}
		});

		final ImageView buildCustom = (ImageView) findViewById(R.id.build_custom);
		buildCustom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent levelIntent = new Intent(CustomStageActivity.this,
						CustomPuzzleBuilderActivity.class);
				startActivity(levelIntent);
			}
		});
		// buildCustom.setFocusable(true);
		// // give feedback on presses
		// buildCustom.setOnTouchListener(new OnTouchListener() {
		// @Override
		// public boolean onTouch(View arg0, MotionEvent arg1) {
		// switch (arg1.getAction()) {
		// case MotionEvent.ACTION_DOWN: {
		// int bg = android.R.color.background_light;
		// buildCustom.setBackgroundResource(bg);
		// // a nice alternative, but how to undo
		// // buildCustom.setColorFilter(0xFFFF0000,
		// // PorterDuff.Mode.MULTIPLY);
		// break;
		// }
		// case MotionEvent.ACTION_UP:
		// case MotionEvent.ACTION_CANCEL: {
		// int bg = android.R.color.background_dark;
		// buildCustom.setBackgroundResource(bg);
		// // buildCustom.setColorFilter(0xFF000000,
		// // PorterDuff.Mode.MULTIPLY);
		// break;
		// }
		// }
		// return false;
		// }
		// });

		BackgroundMusicHelper.onActivtyCreate(this, stage.getBgMusicId());
	}

	private void shareLevel(final CustomLevel level) {
		// TODO can share
		// in progress levels- passed via URL encoding, as the old
		// posted level, via a new url that passes the parseObject id
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				shareLevel(level);
			}
		};

		String author;
		if (level.isImported()) {
			author = level.getAuthor();
		} else {
			author = getAuthor(runnable);
			if (author == null) {
				return;
			}
		}

		try {
			CustomLevelBuilder builder = new CustomLevelBuilder();
			builder.load(level.getDbId());

			String title = builder.getTitle();
			List<Operation> operations = builder.getOperations();
			List<Move> moves = builder.getMoves();

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			final Document doc = docBuilder.newDocument();
			Element root = doc.createElement("l");
			doc.appendChild(root);
			Element titleNode = doc.createElement("t");
			XMLUtils.setTextContent(titleNode, title);
			root.appendChild(titleNode);

			Element authorNode = doc.createElement("a");
			XMLUtils.setTextContent(authorNode, author);
			root.appendChild(authorNode);

			for (Operation each : operations) {
				final Element op = doc.createElement("o");
				root.appendChild(op);
				Element typeNode = doc.createElement("t");
				op.appendChild(typeNode);
				XMLUtils.setTextContent(typeNode, each.type().toString());
				OperationVisitor visitor = new OperationVisitor() {

					@Override
					public void visitSwap(Swap swap) {
					}

					@Override
					public void visitSubtract(Subtract sub) {
						Expression expression = sub.getExpression();
						appendExpression(expression);
					}

					private void appendExpression(Expression expression) {
						Element x = doc.createElement("x");
						Element constant = doc.createElement("c");
						op.appendChild(x);
						op.appendChild(constant);
						XMLUtils.setTextContent(x, expression.getXCoefficient()
								.toString());
						XMLUtils.setTextContent(constant, expression
								.getConstant().toString());
					}

					@Override
					public void visitAdd(Add add) {
						Expression expression = add.getExpression();
						appendExpression(expression);
					}

					@Override
					public void visitMultiply(Multiply multiply) {
						Fraction factor = multiply.getFactor();
						appendFraction(factor);
					}

					@Override
					public void visitDivide(Divide divide) {
						Fraction factor = divide.getFactor();
						appendFraction(factor);
					}

					private void appendFraction(Fraction factor) {
						Element constant = doc.createElement("c");
						op.appendChild(constant);
						XMLUtils.setTextContent(constant, factor.toString());
					}

				};
				each.accept(visitor);
			}
			List<Move> subList = moves.subList(1, moves.size());
			for (Move each : subList) {
				int opIndex = operations.indexOf(each.getOperation());
				Element op = doc.createElement("m");
				root.appendChild(op);
				XMLUtils.setTextContent(op, Integer.toString(opIndex));
			}
			Element sol = doc.createElement("s");
			root.appendChild(sol);
			XMLUtils.setTextContent(sol, builder.getSolution().toString());

			String xmlString = XMLUtils.xmlDocumentToString(doc);
			byte[] bytes = Utils.compress(xmlString);
			String encodedString = Base64.encodeToString(bytes, Base64.URL_SAFE
					| Base64.NO_WRAP);

			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(CustomStageActivity.this);
			String defaultText = CustomStageActivity.this
					.getString(R.string.default_custom_level_share_message);
			String shareText = preferences.getString(CustomStageActivity.this
					.getString(R.string.pref_default_share_custom_level_key),
					defaultText);

			shareText = shareText.replaceAll("%a", author);
			shareText = shareText.replaceAll("%u",
					"http://www.oakonell.com/findx/share/" + encodedString);
			shareText = shareText.replaceAll("%l",
					title + "\n " + level.getMultilineDescription() + "\n");

			String titleText = CustomStageActivity.this
					.getString(R.string.share_custom_level_title);
			titleText = titleText.replaceAll("%t", title);

			ShareHelper.share(CustomStageActivity.this, titleText, shareText);

		} catch (Exception e) {
			Toast.makeText(
					getApplicationContext(),
					"Encountered an exception trying to share the level:"
							+ e.getLocalizedMessage(), Toast.LENGTH_SHORT)
					.show();

		}
	}

	private String getAuthor(final Runnable runnable) {
		// get the author from preferences, if empty, prompt and save it to
		// preferences

		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		final String authorKey = getString(R.string.pref_share_author_key);
		String author = preferences.getString(authorKey, "");
		if (author == null || author.trim().length() == 0) {
			// prompt for name and update prefs

			final EditText input = new EditText(this);
			new AlertDialog.Builder(this)
					.setTitle(R.string.pref_dlg_ttl_share_author)
					.setMessage(R.string.pref_dlg_share_author_msg)
					.setView(input)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String author = input.getText().toString();
									Editor edit = preferences.edit();
									edit.putString(
											getString(R.string.pref_share_author_key),
											author);
									edit.commit();
									dialog.dismiss();
									runnable.run();
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Do nothing.
									dialog.dismiss();
								}
							}).show();

			return null;
		}
		return author;
	}

	private void startPuzzle(final String levelId) {
		Intent levelIntent = new Intent(CustomStageActivity.this,
				PuzzleActivity.class);
		levelIntent.putExtra(PuzzleActivity.PUZZLE_ID, levelId);
		levelIntent.putExtra(PuzzleActivity.IS_CUSTOM, true);
		BackgroundMusicHelper.continueMusicOnNextActivity();
		startActivity(levelIntent);
	}

	@Override
	protected void onRestart() {
		if (adapter != null) {
			// update ratings and enablement of the level buttons
			levels.clear();
			levels.addAll(stage.getLevels());
			adapter.notifyDataSetChanged();
		}
		super.onRestart();
	}

	@Override
	protected void onResume() {
		BackgroundMusicHelper.onActivityResume(this, stage.getBgMusicId());
		if (adapter != null) {
			// update ratings and enablement of the level buttons
			levels.clear();
			levels.addAll(stage.getLevels());
			adapter.notifyDataSetChanged();
		}
		super.onResume();
	}

	protected void deleteLevel(final CustomLevel level) {
		final AtomicBoolean delete = new AtomicBoolean(true);

		UndoBarController.UndoBar undoBar = new UndoBarController.UndoBar(this);
		undoBar.message("Deleted " + level.getName());
		undoBar.listener(new UndoListener() {
			@Override
			public void onUndo(Parcelable token) {
				// cancel the delete
				delete.set(false);
				Toast.makeText(CustomStageActivity.this,
						"Restored " + level.getName(), Toast.LENGTH_SHORT)
						.show();
				levels.clear();
				levels.addAll(stage.getLevels());
				adapter.notifyDataSetChanged();
			}
		});
		undoBar.duration(DISMISS_BAR_DURATION);
		undoBar.show(true);
		levels.remove(level);

		// schedule the delete...
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!delete.get()) {
					// was canceled
					return;
				}
				stage.delete(level);
				levels.clear();
				levels.addAll(stage.getLevels());
				adapter.notifyDataSetChanged();
			}
		}, DISMISS_BAR_DURATION + 200);

	}

	private void copyLevel(CustomLevel level) {
		Intent levelIntent = new Intent(CustomStageActivity.this,
				CustomPuzzleBuilderActivity.class);
		levelIntent.putExtra(CustomPuzzleBuilderActivity.LEVEL_ID,
				level.getDbId());
		levelIntent.putExtra(CustomPuzzleBuilderActivity.COPY, true);

		startActivity(levelIntent);
	}

	private void editLevel(final CustomLevel level) {
		Intent levelIntent = new Intent(CustomStageActivity.this,
				CustomPuzzleBuilderActivity.class);
		levelIntent.putExtra(CustomPuzzleBuilderActivity.LEVEL_ID,
				level.getDbId());
		startActivity(levelIntent);
	}

	public boolean startDrag(View v) {
		DragSource dragSource = (DragSource) v;

		// We are starting a drag. Let the DragController handle it.
		mDragController.startDrag(v, dragSource, dragSource,
				DragController.DRAG_ACTION_MOVE);

		return true;
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

	private void showPopupMenu(final List<LevelMenuItem> menus,
			View originatingView) {
		// builder.setTitle("Modify Match");
		String[] menuitems = new String[menus.size()];
		int i = 0;
		for (LevelMenuItem each : menus) {
			menuitems[i++] = each.text;
		}

		PopupMenuDialogFragment frag = new PopupMenuDialogFragment();
		frag.initialize(originatingView, menuitems, new OnItemSelected() {
			@Override
			public void onSelected(int which) {
				// // The 'which' argument contains the index
				// // position of the selected item
				menus.get(which).execute.execute(CustomStageActivity.this,
						adapter);
				adapter.notifyDataSetChanged();
			}
		});
		frag.show(getSupportFragmentManager(), "popup");
	}

	public interface ItemExecute {
		void execute(CustomStageActivity activity, ArrayAdapter<Level> adapter);
	}

	public static class LevelMenuItem {
		final String text;
		final ItemExecute execute;

		public LevelMenuItem(String text, ItemExecute execute) {
			this.text = text;
			this.execute = execute;
		}
	}

	private void addMenuItems(final CustomLevel level,
			List<LevelMenuItem> menuItems) {
		menuItems.add(new LevelMenuItem("Delete", new ItemExecute() {
			@Override
			public void execute(CustomStageActivity activity,
					ArrayAdapter<Level> adapter) {
				deleteLevel(level);
			}
		}));
		if (!level.isImported()) {
			menuItems.add(new LevelMenuItem("Copy", new ItemExecute() {
				@Override
				public void execute(CustomStageActivity activity,
						ArrayAdapter<Level> adapter) {
					copyLevel(level);
				}
			}));
			if (!level.savedToServer()) {
				menuItems.add(new LevelMenuItem("Edit", new ItemExecute() {
					@Override
					public void execute(CustomStageActivity activity,
							ArrayAdapter<Level> adapter) {
						editLevel(level);
					}
				}));
				menuItems.add(new LevelMenuItem("Post", new ItemExecute() {
					@Override
					public void execute(CustomStageActivity activity,
							ArrayAdapter<Level> adapter) {
						postLevel(level);
					}

				}));
			}
		}
		menuItems.add(new LevelMenuItem("Share", new ItemExecute() {
			@Override
			public void execute(CustomStageActivity activity,
					ArrayAdapter<Level> adapter) {
				shareLevel(level);
			}
		}));
	}

	private void postLevel(final CustomLevel theLevel) {
		ParseUser parseUser = ParseUser.getCurrentUser();
		if (parseUser == null) {
			// prompt to Log in
			Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
			return;
		}
		if (StringUtils.isEmpty(parseUser.getString("nickname"))) {
			Runnable continuation = new Runnable() {
				@Override
				public void run() {
					postLevel(theLevel);
				}
			};
			ParseConnectivity.createUniqueNickname(this, continuation);
			return;
		}

		final ProgressDialog dialog = ProgressDialog.show(this,
				"Saving to server", "Please wait...");
		dialog.setCancelable(false);

		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				String id = ParseLevelHelper.postLevel(theLevel);

				// update the level to note it is saved to server
				CustomLevelBuilder builder = new CustomLevelBuilder();
				CustomLevelDBReader reader = new CustomLevelDBReader();
				reader.read(CustomStageActivity.this, builder,
						theLevel.getDbId());
				builder.setServerId(id);
				CustomLevelDBWriter writer = new CustomLevelDBWriter();
				writer.write(CustomStageActivity.this, builder);

				theLevel.setServerId(id);

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				FindXApp app = (FindXApp) getApplication();
				Achievements achievements = app.getAchievements();
				achievements.setContributor(CustomStageActivity.this);
				dialog.dismiss();
				adapter.notifyDataSetChanged();
			}

		};
		task.execute();
	}
}
