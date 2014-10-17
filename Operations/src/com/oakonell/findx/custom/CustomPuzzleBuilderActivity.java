package com.oakonell.findx.custom;

import java.util.List;

import org.apache.commons.math3.exception.MathParseException;
import org.apache.commons.math3.fraction.Fraction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.oakonell.findx.GameActivity;
import com.oakonell.findx.MenuHelper;
import com.oakonell.findx.R;
import com.oakonell.findx.custom.OperationBuilderDialog.OperationBuiltContinuation;
import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.custom.model.CustomLevelBuilder.OptimizedListener;
import com.oakonell.findx.custom.model.RandomHelper;
import com.oakonell.findx.custom.widget.FractionEditText;
import com.oakonell.findx.custom.widget.FractionEditText.OnFractionChanged;
import com.oakonell.findx.custom.widget.PopupContextMenu;
import com.oakonell.findx.custom.widget.PopupContextMenu.ButtonContextMenuOnClickListener;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Move;
import com.oakonell.findx.model.Operation;
import com.oakonell.utils.NumberPicker;

public class CustomPuzzleBuilderActivity extends GameActivity {
	public static final String LEVEL_ID = "id";
	public static final String COPY = "copy";
	private static final int MAX_OPERATORS = 5;
	protected static final double LARGE_SEARCH_SPACE = Math.pow(5, 12);
	private CustomLevelBuilder builder = new CustomLevelBuilder();

	ArrayAdapter<Move> adapter;
	private FractionEditText xSolution;
	private Button saveButton;
	private EditText title;
	private TextView addOperator;

	CalculateMinMovesProgressTask task;
	private RandomHelper randomHelper = new RandomHelper();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_builder);

		final ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayUseLogoEnabled(true);
		ab.setDisplayShowTitleEnabled(true);

		Intent intent = getIntent();
		long levelId = intent.getLongExtra(LEVEL_ID, 0);
		// TODO async task these builder accesses?
		if (levelId != 0) {
			builder.load(levelId);
		} else {
			// get the max sequence
			builder.defaultMaxSequence();
		}
		if (intent.getBooleanExtra(COPY, false)) {
			builder.prepareAsCopy();
			builder.setTitle(getText(R.string.copy_prefix) + builder.getTitle());
		}

		final ListView movesView = (ListView) findViewById(R.id.moves);

		adapter = new ArrayAdapter<Move>(getApplication(), R.layout.move_build,
				builder.getMoves()) {

			@Override
			public View getView(int position, View row, ViewGroup parent) {
				if (row == null) {
					row = getLayoutInflater().inflate(R.layout.move_build,
							parent, false);
				}

				final Move item = getItem(position);

				TextView moveNum = (TextView) row.findViewById(R.id.move_num);
				View upButton = row.findViewById(R.id.up);
				View downButton = row.findViewById(R.id.down);
				TextView op = (TextView) row.findViewById(R.id.operation);

				if (position > 0) {
					moveNum.setText(position + "");
					if (position > 1) {
						upButton.setVisibility(View.VISIBLE);
					} else {
						upButton.setVisibility(View.INVISIBLE);
					}
					if (position < builder.getMoves().size() - 1) {
						downButton.setVisibility(View.VISIBLE);
					} else {
						downButton.setVisibility(View.INVISIBLE);
					}
					upButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							builder.moveUp(item);
							handleOperatorButtons();
							adapter.notifyDataSetChanged();
						}
					});
					downButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							builder.moveDown(item);
							handleOperatorButtons();
							adapter.notifyDataSetChanged();
						}
					});
					op.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							PopupContextMenu menuDialog = new PopupContextMenu(
									CustomPuzzleBuilderActivity.this, 2);
							menuDialog.addItem(getResources(),
									R.string.edit_move, 2);
							menuDialog.addItem(getResources(),
									R.string.delete_move, 3);
							menuDialog
									.setOnClickListener(new ButtonContextMenuOnClickListener() {
										@Override
										public void onClick(int menuId) {
											switch (menuId) {
											case 2:
												// edit
												editMove(item);
												break;
											case 3:
												// delete
												deleteMove(item);
												break;
											default:
												throw new RuntimeException(
														"No such menu option "
																+ menuId);
											}
										}
									});
							Dialog menu = menuDialog
									.createMenu(getText(R.string.move_menu_title));
							menu.show();
						}
					});
				} else {
					moveNum.setText("");
					upButton.setVisibility(View.INVISIBLE);
					downButton.setVisibility(View.INVISIBLE);
					op.setOnClickListener(null);
				}

				Operation operation = item.getOperation();

				op.setText(Html.fromHtml(operation != null ? operation
						.toString() : ""));

				TextView eq = (TextView) row.findViewById(R.id.equation);
				eq.setText(Html.fromHtml(item.getEndEquation().toString()));

				return row;
			}

		};
		saveButton = (Button) findViewById(R.id.save);

		addOperator = (TextView) findViewById(R.id.add_op);
		addOperator.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addAnOperator();
			}

		});

		handleOperatorButtons();

		Button addRandomMoves = (Button) findViewById(R.id.add_random_moves);
		addRandomMoves.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				addRandomMoves();
			}
		});

		xSolution = (FractionEditText) findViewById(R.id.x_equals);
		title = (EditText) findViewById(R.id.title);

		xSolution.setOnFractionChanged(new OnFractionChanged() {
			@Override
			public void fractionParseError(FractionEditText view, Editable value) {
				handleSaveButtonEnablement();
			}

			@Override
			public void fractionChanged(FractionEditText view, Fraction fraction) {
				handleSaveButtonEnablement();
				builder.setSolution(fraction);
				handleOperatorButtons();
				adapter.notifyDataSetChanged();
			}
		});

		movesView.setAdapter(adapter);

		xSolution.setFraction(builder.getSolution());
		title.setText(builder.getTitle());
		title.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence charsequence, int i, int j,
					int k) {
			}

			@Override
			public void beforeTextChanged(CharSequence charsequence, int i,
					int j, int k) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (editable.toString().trim().length() == 0) {
					title.setError(getText(R.string.title_required));
					return;
				}
				title.setError(null);
				handleSaveButtonEnablement();
				builder.setTitle(editable.toString());
			}
		});

		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!builder.isOptimized()) {
					promptToOptimizeOrSave();
					return;
				}
				saveLevel();
			}

		});

		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmAndLeave(new Runnable() {
					@Override
					public void run() {
						CustomPuzzleBuilderActivity.this.finish();
					}
				});
			}
		});

		final Button minimizeMoves = (Button) findViewById(R.id.calc_min_moves);
		minimizeMoves.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				promptAndLaunchOptimizer();
			}
		});

		minimizeMoves.setEnabled(builder.isOptimized());
		builder.setOptimizedListener(new OptimizedListener() {
			@Override
			public void isOptimized(boolean optimized) {
				minimizeMoves.setEnabled(!optimized
						&& !builder.getOperations().isEmpty());
			}
		});

		task = (CalculateMinMovesProgressTask) getLastNonConfigurationInstance();
		if (task != null) {
			task.setParent(this);
		}

		handleSaveButtonEnablement();
	}

	protected void promptToOptimizeOrSave() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.puzzle_solution_not_optimized_title)
				.setMessage(R.string.puzzle_solution_not_optimized)
				.setPositiveButton(R.string.optimize,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								promptAndLaunchOptimizer();
							}
						})
				.setNeutralButton(R.string.save,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								saveLevel();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						}).show();
	}

	private void saveLevel() {
		builder.save();
		// TODO async task this?
		Levels.resetCustomStage();
		CustomPuzzleBuilderActivity.this.finish();
	}

	private void promptAndLaunchOptimizer() {
		int branchRatio = builder.getOperations().size();
		int depth = builder.getMoves().size();
		if (Math.pow(branchRatio, depth) > LARGE_SEARCH_SPACE) {
			// provide a warning that may take a while
			new AlertDialog.Builder(this)
					.setTitle(R.string.large_search_space_title)
					.setMessage(R.string.large_search_space)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.dismiss();
									launchOptimizer();
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
		} else {
			launchOptimizer();
		}
	}

	private void launchOptimizer() {
		task = new CalculateMinMovesProgressTask(
				CustomPuzzleBuilderActivity.this, builder);
		task.execute();
	}

	private void addRandomMoves() {
		// prompt for how many moves
		final NumberPicker input = new NumberPicker(this);
		// input.setInputType(InputType.TYPE_CLASS_NUMBER);
		new AlertDialog.Builder(this)
				.setTitle(R.string.add_random_moves_dlg_title)
				.setMessage(R.string.how_many_random_moves)
				.setView(input)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								int num = input.getNumber();
								addRandomMoves(num);
								dialog.dismiss();
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
	}

	private void addRandomMoves(int numMoves) {
		randomHelper.addRandomMoves(builder, numMoves);
		// finish up
		handleSaveButtonEnablement();
		handleOperatorButtons();
		adapter.notifyDataSetChanged();
	}

	private void handleSaveButtonEnablement() {
		try {
			xSolution.getFraction();
		} catch (MathParseException e) {
			saveButton.setEnabled(false);
			return;
		}
		if (title.getText().length() == 0 || title.getError() != null) {
			saveButton.setEnabled(false);
			return;
		}
		saveButton.setEnabled(builder.getMoves().size() > 1);
	}

	private void handleOperatorButtons() {
		boolean canAddMoreOperations = builder.getOperations().size() < MAX_OPERATORS;
		addOperator.setEnabled(canAddMoreOperations);

		List<Operation> operations = builder.getOperations();
		handleOperatorButton(R.id.op1, operations, 0);
		handleOperatorButton(R.id.op2, operations, 1);
		handleOperatorButton(R.id.op3, operations, 2);
		handleOperatorButton(R.id.op4, operations, 3);
		handleOperatorButton(R.id.op5, operations, 4);
	}

	private void handleOperatorButton(int op1, List<Operation> operations, int i) {
		final TextView opButton = (TextView) findViewById(op1);
		if (i >= operations.size()) {
			opButton.setVisibility(View.GONE);
			return;
		}
		final Operation operation = operations.get(i);
		opButton.setVisibility(View.VISIBLE);
		opButton.setText(Html.fromHtml(operation.toString()));

		Equation currentEquation = builder.getMoves().get(0).getStartEquation();
		if (operation.inverse().canApply(currentEquation)) {
			opButton.setEnabled(true);
		} else {
			opButton.setEnabled(false);
		}

		opButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// present choices to - delete or modify (really replace)
				return presentOpButtonChoices(opButton, operation);
			}
		});
		opButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				builder.apply(operation);
				handleSaveButtonEnablement();
				handleOperatorButtons();
				adapter.notifyDataSetChanged();
			}
		});
	}

	private boolean presentOpButtonChoices(final TextView opButton,
			final Operation operation) {
		PopupContextMenu menuDialog = new PopupContextMenu(this, 1);
		menuDialog.addItem(getResources(), R.string.edit_operation, 2);
		menuDialog.addItem(getResources(), R.string.delete_operation, 3);
		menuDialog.setOnClickListener(new ButtonContextMenuOnClickListener() {
			@Override
			public void onClick(int menuId) {
				switch (menuId) {
				case 2:
					// edit
					editOperation(operation);
					break;
				case 3:
					// delete
					deleteOperation(opButton, operation);
					break;
				default:
					throw new RuntimeException("No such menu option " + menuId);
				}
			}
		});
		Dialog menu = menuDialog
				.createMenu(getText(R.string.operation_menu_title));
		menu.show();
		return false;
	}

	private void deleteOperation(TextView opButton, final Operation operation) {
		if (builder.usesOperation(operation)) {
			// prompt and delete all moves after this operation
			final AlertDialog alertDialog = new AlertDialog.Builder(this)
					.create();
			alertDialog.setTitle(R.string.confirm_delete_operation);
			alertDialog.setMessage(getResources().getString(
					R.string.confirm_delete_operation_is_used,
					operation.toString()));
			alertDialog.setIcon(android.R.drawable.ic_delete);
			alertDialog.setButton(getText(android.R.string.ok),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// delete any moves after this operation
							builder.removeOperation(operation);
							// targeted update?
							handleOperatorButtons();
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
		} else {
			builder.removeOperation(operation);
			// targeted update?
			handleOperatorButtons();
		}
	}

	private void editOperation(final Operation operation) {
		OperationBuilderDialog expressionBuilder = new OperationBuilderDialog(
				randomHelper);
		expressionBuilder.buildExpression(this, operation,
				new OperationBuiltContinuation() {
					@Override
					public void operationBuilt(Operation newOperation) {
						builder.replaceOperation(operation, newOperation);
						// TODO targeted updated?
						handleOperatorButtons();
						adapter.notifyDataSetChanged();
					}
				});
	}

	private void addAnOperator() {
		OperationBuilderDialog expressionBuilder = new OperationBuilderDialog(
				randomHelper);
		expressionBuilder.buildExpression(this, null,
				new OperationBuiltContinuation() {
					@Override
					public void operationBuilt(Operation operation) {
						builder.addOperation(operation);
						// TODO targeted updated?
						handleOperatorButtons();
					}
				});
	}

	private void confirmAndLeave(final Runnable run) {
		if (builder.getOperations().isEmpty() && builder.getMoves().size() <= 1) {
			run.run();
			return;
		}
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(R.string.confirm_lose_level_changes_title);
		alertDialog.setMessage(getResources().getString(
				R.string.confirm_lose_level_changes));
		alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
		alertDialog.setCancelable(true);
		alertDialog.setButton(getText(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						run.run();
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

	@Override
	public void onBackPressed() {
		confirmAndLeave(new Runnable() {
			@Override
			public void run() {
				CustomPuzzleBuilderActivity.super.onBackPressed();
			}
		});
	}

	private void deleteMove(final Move item) {
		// confirm to delete
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(R.string.confirm_delete_move_title);
		alertDialog.setMessage(getResources().getString(
				R.string.confirm_delete_move));
		alertDialog.setIcon(android.R.drawable.ic_delete);
		alertDialog.setButton(getText(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// delete any moves after this operation
						builder.deleteMove(item);
						handleOperatorButtons();
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

	private void editMove(final Move item) {
		final CharSequence[] items = new CharSequence[builder.getOperations()
				.size()];
		int i = 0;
		for (Operation each : builder.getOperations()) {
			items[i] = each.toString();
			i++;
		}
		final int defaultItemIndex = builder.getOperations().indexOf(
				item.getOperation());

		AlertDialog.Builder dbuilder = new AlertDialog.Builder(this);
		dbuilder.setTitle(R.string.choose_new_operation);
		dbuilder.setSingleChoiceItems(items, defaultItemIndex,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int index) {
						Operation op = builder.getOperations().get(index);
						if (index != defaultItemIndex) {
							builder.replaceMove(item, op);
							handleOperatorButtons();
							adapter.notifyDataSetChanged();
						} else {
							//
						}
						dialog.dismiss();
					}
				});
		final AlertDialog alert = dbuilder.create();
		alert.show();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			confirmAndLeave(new Runnable() {
				@Override
				public void run() {
					NavUtils.navigateUpFromSameTask(CustomPuzzleBuilderActivity.this);
				}
			});
			return true;
		}
		return MenuHelper.onOptionsItemSelected(this, item);
	}

}
