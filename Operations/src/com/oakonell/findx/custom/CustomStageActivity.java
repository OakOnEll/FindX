package com.oakonell.findx.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.math3.fraction.Fraction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.oakonell.findx.BackgroundMusicHelper;
import com.oakonell.findx.ChooseStageActivity;
import com.oakonell.findx.MenuHelper;
import com.oakonell.findx.PuzzleActivity;
import com.oakonell.findx.R;
import com.oakonell.findx.custom.model.CustomLevel;
import com.oakonell.findx.custom.model.CustomLevelBuilder;
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
import com.oakonell.utils.Utils;
import com.oakonell.utils.activity.dragndrop.DragController;
import com.oakonell.utils.activity.dragndrop.DragLayer;
import com.oakonell.utils.activity.dragndrop.DragSource;
import com.oakonell.utils.activity.dragndrop.DragView;
import com.oakonell.utils.activity.dragndrop.ImageDropTarget;
import com.oakonell.utils.activity.dragndrop.OnDragListener;
import com.oakonell.utils.activity.dragndrop.OnDropListener;
import com.oakonell.utils.share.ShareHelper;
import com.oakonell.utils.xml.XMLUtils;

public class CustomStageActivity extends Activity {

	private ArrayAdapter<Level> adapter;
	private CustomStage stage;
	private DragController mDragController;
	private DragLayer mDragLayer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_stage);

		// TODO do this in an AsyncTask
		stage = Levels.getCustomStage();

		GridView levelSelect = (GridView) findViewById(R.id.level_select);

		mDragController = new DragController(this);
		mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
		mDragLayer.setDragController(mDragController);
		mDragLayer.setGridView(levelSelect);

		final Map<View, Integer> textIdByDropTarget = new HashMap<View, Integer>();
		OnDragListener dragListener = new OnDragListener() {
			@Override
			public void onDragOver(View target, DragSource source, int x,
					int y, int xOffset, int yOffset, DragView dragView,
					Object dragInfo) {
			}

			@Override
			public void onDragExit(View target, DragSource source, int x,
					int y, int xOffset, int yOffset, DragView dragView,
					Object dragInfo) {
				Integer id = textIdByDropTarget.get(target);
				findViewById(id).setVisibility(View.INVISIBLE);
				int bg = android.R.color.background_dark;
				target.setBackgroundResource(bg);
			}

			@Override
			public void onDragEnter(View target, DragSource source, int x,
					int y, int xOffset, int yOffset, DragView dragView,
					Object dragInfo) {
				Integer id = textIdByDropTarget.get(target);
				findViewById(id).setVisibility(View.VISIBLE);
				int bg = android.R.color.background_light;
				target.setBackgroundResource(bg);
			}
		};

		ImageDropTarget trashCan = (ImageDropTarget) findViewById(R.id.delete_level);
		trashCan.setOnDragListener(dragListener);
		trashCan.setOnDropListener(new OnDropListener() {
			@Override
			public void onDrop(View target, DragSource source, int x, int y,
					int xOffset, int yOffset, DragView dragView, Object dragInfo) {
				deleteLevel(((CustomLevelGridCell) source).getLevel());
			}

			@Override
			public boolean acceptDrop(View target, DragSource source, int x,
					int y, int xOffset, int yOffset, DragView dragView,
					Object dragInfo) {
				return true;
			}
		});
		textIdByDropTarget.put(trashCan, R.id.delete_hint);
		mDragLayer.addTarget(trashCan);

		ImageDropTarget pencil = (ImageDropTarget) findViewById(R.id.edit_level);
		pencil.setOnDragListener(dragListener);
		pencil.setOnDropListener(new OnDropListener() {
			@Override
			public void onDrop(View view, DragSource source, int x, int y,
					int xOffset, int yOffset, DragView dragView, Object dragInfo) {
				CustomLevel level = ((CustomLevelGridCell) source).getLevel();
				if (level.isImported()) {
					Toast.makeText(CustomStageActivity.this,
							R.string.cannot_edit_imported_levels,
							Toast.LENGTH_SHORT).show();
					return;
				}

				Intent levelIntent = new Intent(CustomStageActivity.this,
						CustomPuzzleBuilderActivity.class);
				levelIntent.putExtra(CustomPuzzleBuilderActivity.LEVEL_ID,
						level.getDbId());
				startActivity(levelIntent);
			}

			@Override
			public boolean acceptDrop(View target, DragSource source, int x,
					int y, int xOffset, int yOffset, DragView dragView,
					Object dragInfo) {
				return true;
			}
		});
		textIdByDropTarget.put(pencil, R.id.edit_hint);
		mDragLayer.addTarget(pencil);

		ImageDropTarget share = (ImageDropTarget) findViewById(R.id.share_level);
		share.setOnDragListener(dragListener);
		share.setOnDropListener(new OnDropListener() {
			@Override
			public void onDrop(View view, DragSource source, int x, int y,
					int xOffset, int yOffset, DragView dragView, Object dragInfo) {
				CustomLevel level = ((CustomLevelGridCell) source).getLevel();
				shareLevel(level);
			}

			@Override
			public boolean acceptDrop(View target, DragSource source, int x,
					int y, int xOffset, int yOffset, DragView dragView,
					Object dragInfo) {
				return true;
			}
		});
		textIdByDropTarget.put(share, R.id.share_hint);
		mDragLayer.addTarget(share);

		mDragController.setDragListener(mDragLayer);

		adapter = new ArrayAdapter<Level>(getApplication(),
				R.layout.level_select_grid_item, stage.getLevels()) {

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

				final Button levelButton = (Button) row
						.findViewById(R.id.level_name);
				levelButton.setText(level.getName());

				levelButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						startPuzzle(level.getId());
					}
				});
				levelButton.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						return startDrag(row);
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

				// if (level.isUnlocked(CustomStageActivity.this)) {
				// levelButton.setEnabled(true);
				// } else {
				// levelButton.setEnabled(false);
				// }
				return row;
			}

		};

		levelSelect.setAdapter(adapter);

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

		final ImageDropTarget buildCustom = (ImageDropTarget) findViewById(R.id.build_custom);
		textIdByDropTarget.put(buildCustom, R.id.copy_hint);
		mDragLayer.addTarget(buildCustom);
		buildCustom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent levelIntent = new Intent(CustomStageActivity.this,
						CustomPuzzleBuilderActivity.class);
				startActivity(levelIntent);
			}
		});
		buildCustom.setFocusable(true);
		// give feedback on presses
		buildCustom.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				switch (arg1.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					int bg = android.R.color.background_light;
					buildCustom.setBackgroundResource(bg);
					// a nice alternative, but how to undo
					// buildCustom.setColorFilter(0xFFFF0000,
					// PorterDuff.Mode.MULTIPLY);
					break;
				}
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL: {
					int bg = android.R.color.background_dark;
					buildCustom.setBackgroundResource(bg);
					// buildCustom.setColorFilter(0xFF000000,
					// PorterDuff.Mode.MULTIPLY);
					break;
				}
				}
				return false;
			}
		});
		buildCustom.setOnDragListener(dragListener);
		buildCustom.setOnDropListener(new OnDropListener() {
			@Override
			public void onDrop(View view, DragSource source, int x, int y,
					int xOffset, int yOffset, DragView dragView, Object dragInfo) {
				CustomLevel level = ((CustomLevelGridCell) source).getLevel();
				if (level.isImported()) {
					Toast.makeText(CustomStageActivity.this,
							R.string.cannot_copy_imported_levels,
							Toast.LENGTH_SHORT).show();
					return;
				}

				Intent levelIntent = new Intent(CustomStageActivity.this,
						CustomPuzzleBuilderActivity.class);
				levelIntent.putExtra(CustomPuzzleBuilderActivity.LEVEL_ID,
						level.getDbId());
				levelIntent.putExtra(CustomPuzzleBuilderActivity.COPY, true);

				startActivity(levelIntent);
			}

			@Override
			public boolean acceptDrop(View target, DragSource source, int x,
					int y, int xOffset, int yOffset, DragView dragView,
					Object dragInfo) {
				return true;
			}
		});
		BackgroundMusicHelper.onActivtyCreate(this, stage.getBgMusicId());
	}

	private void shareLevel(final CustomLevel level) {
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

	protected void deleteLevel(final CustomLevel level) {
		// prompt and delete all moves after this operation
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(getText(R.string.confirm_delete_level));
		alertDialog.setMessage(getResources().getString(
				R.string.confirm_delete_level_text,
				level.getId() + " - " + level.getName()));
		alertDialog.setIcon(android.R.drawable.ic_delete);
		alertDialog.setButton(getText(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						stage.delete(level);
						// targeted update?
						adapter.notifyDataSetChanged();
						alertDialog.dismiss();
					}
				});
		alertDialog.setButton2(getText(android.R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						alertDialog.dismiss();
						return;
					}
				});
		alertDialog.show();
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

}
