package com.oakonell.findx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.math3.fraction.Fraction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.oakonell.findx.PuzzleActivity.Sounds;
import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Expression.UseParenthesis;
import com.oakonell.findx.model.Level;
import com.oakonell.findx.model.Puzzle;
import com.oakonell.utils.StringUtils;
import com.oakonell.utils.share.ShareHelper;

public class LevelSolvedDialogFragment extends SherlockDialogFragment {
	private Puzzle puzzle;
	private PuzzleActivity activity;
	private Handler handler = new Handler();

	public void initialize(Puzzle puzzle, PuzzleActivity activity) {
		this.puzzle = puzzle;
		this.activity = activity;
	}

	@Override
	public void onStart() {
		super.onStart();
		int delay = 1000;

		checkSolutionAnimation(delay);

	}

	private static class EmptyAnimationListener implements AnimationListener {
		private final LinkedList<Runnable> continuations;

		EmptyAnimationListener() {
			this.continuations = null;
		}

		EmptyAnimationListener(LinkedList<Runnable> continuations) {
			this.continuations = continuations;
		}

		@Override
		public final void onAnimationStart(Animation animation) {
		}

		@Override
		public final void onAnimationRepeat(Animation animation) {
		}

		@Override
		public final void onAnimationEnd(Animation animation) {
			onTheAnimationEnd(animation);
			if (continuations != null) {
				Runnable runnable = continuations.poll();
				if (runnable != null) {
					runnable.run();
				}
			}
		}

		protected void onTheAnimationEnd(Animation animation) {
			// subclasses override
		}

	}

	private void checkSolutionAnimation(int delay) {
		final View view = getView();
		final ExpressionViews lhsViews = new ExpressionViews();
		lhsViews.x2coeff = (TextView) view.findViewById(R.id.lhs_x2coeff);
		lhsViews.x2coeff_lbl = (TextView) view
				.findViewById(R.id.lhs_x2coeff_lbl);
		lhsViews.xcoeff_add = (TextView) view.findViewById(R.id.lhs_xcoeff_add);
		lhsViews.xcoeff = (TextView) view.findViewById(R.id.lhs_xcoeff);
		lhsViews.xcoeff_lbl = (TextView) view.findViewById(R.id.lhs_xcoeff_lbl);
		lhsViews.const_add = (TextView) view.findViewById(R.id.lhs_const_add);
		lhsViews.constant = (TextView) view.findViewById(R.id.lhs_const);

		final ExpressionViews rhsViews = new ExpressionViews();
		rhsViews.x2coeff = (TextView) view.findViewById(R.id.rhs_x2coeff);
		rhsViews.x2coeff_lbl = (TextView) view
				.findViewById(R.id.rhs_x2coeff_lbl);
		rhsViews.xcoeff_add = (TextView) view.findViewById(R.id.rhs_xcoeff_add);
		rhsViews.xcoeff = (TextView) view.findViewById(R.id.rhs_xcoeff);
		rhsViews.xcoeff_lbl = (TextView) view.findViewById(R.id.rhs_xcoeff_lbl);
		rhsViews.const_add = (TextView) view.findViewById(R.id.rhs_const_add);
		rhsViews.constant = (TextView) view.findViewById(R.id.rhs_const);

		Equation equation = puzzle.getLevel().getEquation();
		final Expression lhs = equation.getLhs();
		final Expression rhs = equation.getRhs();

		// haven't been able to get this to run at the right time, so the
		// positions return valid numbers
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				startSolutionCheckAnimation(view, lhs, rhs, lhsViews, rhsViews);
			}
		}, delay);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.level_finished, container,
				true);

		// setStyle(R.style.LevelCompleteDialog,0 );
		setCancelable(false);

		getDialog().setTitle(R.string.level_finished_title);

		final int rating = puzzle.getRating();
		final int existingRating = puzzle.getExistingRating();

		FindXApp app = (FindXApp) activity.getApplication();
		app.getAchievements().testAndSetLevelCompleteAchievements(activity,
				puzzle);

		puzzle.updateRating();

		final TextView finishText = (TextView) view
				.findViewById(R.id.level_finish_text);

		final RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating);
		ratingBar.setEnabled(false);

		populateViews(finishText, rating, existingRating, ratingBar, view);

		Button retry = (Button) view.findViewById(R.id.retry);
		retry.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				restart();
			}

		});

		Button check_solution_again = (Button) view
				.findViewById(R.id.check_solution_again);
		check_solution_again.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				populateViews(finishText, rating, existingRating, ratingBar,
						view);
				checkSolutionAnimation(10);
			}
		});

		Button nextLevelButton = (Button) view.findViewById(R.id.next_level);
		final Level nextLevel = puzzle.getNextLevel();
		if (nextLevel == null) {
			nextLevelButton.setEnabled(false);
		} else {
			nextLevelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
					nextLevel(nextLevel);
				}

			});
		}

		Button share = (Button) view.findViewById(R.id.share);
		share.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				share();
			}
		});

		Button mainMenu = (Button) view.findViewById(R.id.goto_main_menu);
		mainMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				mainMenu();
			}

		});

		return view;
	}

	private static class ExpressionViews {
		TextView x2coeff;
		TextView x2coeff_lbl;
		TextView xcoeff_add;
		TextView xcoeff;
		TextView xcoeff_lbl;
		TextView const_add;
		TextView constant;

	}

	private void populateViews(TextView finishText, int rating,
			int existingRating, RatingBar ratingBar, View view) {
		activity.soundManager.playSound(Sounds.APPLAUSE);
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
		ratingBar.setRating(rating);

		ExpressionViews lhsViews = new ExpressionViews();
		lhsViews.x2coeff = (TextView) view.findViewById(R.id.lhs_x2coeff);
		lhsViews.x2coeff_lbl = (TextView) view
				.findViewById(R.id.lhs_x2coeff_lbl);
		lhsViews.xcoeff_add = (TextView) view.findViewById(R.id.lhs_xcoeff_add);
		lhsViews.xcoeff = (TextView) view.findViewById(R.id.lhs_xcoeff);
		lhsViews.xcoeff_lbl = (TextView) view.findViewById(R.id.lhs_xcoeff_lbl);
		lhsViews.const_add = (TextView) view.findViewById(R.id.lhs_const_add);
		lhsViews.constant = (TextView) view.findViewById(R.id.lhs_const);

		ExpressionViews rhsViews = new ExpressionViews();
		rhsViews.x2coeff = (TextView) view.findViewById(R.id.rhs_x2coeff);
		rhsViews.x2coeff_lbl = (TextView) view
				.findViewById(R.id.rhs_x2coeff_lbl);
		rhsViews.xcoeff_add = (TextView) view.findViewById(R.id.rhs_xcoeff_add);
		rhsViews.xcoeff = (TextView) view.findViewById(R.id.rhs_xcoeff);
		rhsViews.xcoeff_lbl = (TextView) view.findViewById(R.id.rhs_xcoeff_lbl);
		rhsViews.const_add = (TextView) view.findViewById(R.id.rhs_const_add);
		rhsViews.constant = (TextView) view.findViewById(R.id.rhs_const);

		Equation equation = puzzle.getLevel().getEquation();
		Expression lhs = equation.getLhs();
		Expression rhs = equation.getRhs();
		initializeExpressionView(lhs, lhsViews);
		initializeExpressionView(rhs, rhsViews);

		initializeSolutions(view);
	}

	private void initializeSolutions(View view) {
		TextView sol1 = (TextView) view.findViewById(R.id.x_sol1);

		sol1.setText(Expression.fractionToString(puzzle.getCurrentEquation()
				.getRhs().getConstant(), UseParenthesis.NO));

		// TODO handle second solution
		View secondSolution = view.findViewById(R.id.sol2_layout);
		TextView sol2 = (TextView) view.findViewById(R.id.x_sol2);
	}

	private void startSolutionCheckAnimation(final View view,
			final Expression lhs, final Expression rhs,
			final ExpressionViews lhsViews, final ExpressionViews rhsViews) {

		final TextView solutionCheck = (TextView) view.findViewById(R.id.check);
		solutionCheck.setText("");
		final LinkedList<Runnable> animations = new LinkedList<Runnable>();
		// animate left hand side
		animations.add(new Runnable() {
			@Override
			public void run() {
				animateExpr(view, lhs, lhsViews, animations);
			}
		});
		// animate right hand side
		animations.add(new Runnable() {
			@Override
			public void run() {
				animateExpr(view, rhs, rhsViews, animations);
			}
		});
		// animate check mark?
		animations.add(new Runnable() {
			@Override
			public void run() {
				// check mark
				solutionCheck.setText(Html
						.fromHtml("<font color=\"#32cd32\"><big><bold>\u2713</bold></big></font>"));

			}

		});
		animations.remove().run();
	}

	private void animateExpr(final View view, final Expression expr,
			final ExpressionViews exprViews,
			final LinkedList<Runnable> animations) {
		final TextView sol1VIew = (TextView) view.findViewById(R.id.x_sol1);
		final Fraction sol1 = puzzle.getCurrentEquation().getRhs()
				.getConstant();

		List<Runnable> localList = new ArrayList<Runnable>();
		// replace X^2
		if (expr.hasX2Coefficient()) {
			localList.add(new Runnable() {
				@Override
				public void run() {
					animateReplaceX2(view, sol1VIew, sol1,
							expr.getX2Coefficient(), exprViews.x2coeff_lbl,
							animations);
				}
			});
		}
		// replace X
		if (expr.hasXCoefficient()) {
			localList.add(new Runnable() {
				@Override
				public void run() {
					animateReplaceX(view, sol1VIew, sol1,
							expr.getXCoefficient(), exprViews.xcoeff_lbl, "",
							animations);
				}
			});
		}

		// evaluate each term
		if (expr.hasX2Coefficient()) {
			localList.add(new Runnable() {
				@Override
				public void run() {
					animateEvaluateX2(view, sol1VIew, sol1,
							expr.getX2Coefficient(), exprViews.x2coeff,
							exprViews.x2coeff_lbl, animations);
				}
			});
		}
		if (expr.hasXCoefficient()
				&& expr.getXCoefficient().compareTo(Fraction.ONE) != 0) {
			localList.add(new Runnable() {
				@Override
				public void run() {
					animateEvaluateX(view, sol1VIew, sol1,
							expr.getXCoefficient(), exprViews.xcoeff_add,
							exprViews.xcoeff, exprViews.xcoeff_lbl, animations);
				}
			});
		}

		// sum terms
		// TODO
		if (expr.hasX2Coefficient()) {
			if (expr.hasXCoefficient()) {
				// x2 + x terms
				Fraction xTerm = expr.getXCoefficient().multiply(sol1);
				Fraction x2Term = expr.getX2Coefficient().multiply(sol1)
						.multiply(sol1);
				final Fraction sum = x2Term.add(xTerm);
				localList.add(new Runnable() {
					@Override
					public void run() {
						animateSum(exprViews.x2coeff_lbl, exprViews.xcoeff_add,
								exprViews.xcoeff_lbl, sum, animations);
					}
				});
				if (expr.hasConstant()) {
					// add the constant
					final Fraction sum2 = sum.add(expr.getConstant());
					localList.add(new Runnable() {
						@Override
						public void run() {
							animateSum(exprViews.xcoeff_lbl,
									exprViews.const_add, exprViews.constant,
									sum2, animations);
						}
					});
				}
			} else if (expr.hasConstant()) {
				// x2 + constant terms
				Fraction x2Term = expr.getX2Coefficient().multiply(sol1)
						.multiply(sol1);
				Fraction constTerm = expr.getConstant();
				final Fraction sum = x2Term.add(constTerm);
				localList.add(new Runnable() {
					@Override
					public void run() {
						animateSum(exprViews.x2coeff_lbl, exprViews.const_add,
								exprViews.constant, sum, animations);
					}
				});
			} else {
				// there was only the x^2 term
			}
		} else if (expr.hasXCoefficient()) {
			if (expr.hasConstant()) {
				// x + constant terms
				Fraction xTerm = expr.getXCoefficient().multiply(sol1);
				Fraction constTerm = expr.getConstant();
				final Fraction sum = xTerm.add(constTerm);
				localList.add(new Runnable() {
					@Override
					public void run() {
						animateSum(exprViews.xcoeff_lbl, exprViews.const_add,
								exprViews.constant, sum, animations);
					}
				});
			} else {
				// there was only the x term
			}
		}

		animations.addAll(0, localList);
		animations.remove().run();
	}

	private void animateSum(final TextView firstView, final TextView addView,
			final TextView secondView, final Fraction sum,
			final LinkedList<Runnable> animations) {
		final Drawable original = firstView.getBackground();
		ScaleAnimation grow = new ScaleAnimation(1, 2, 1, 2,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		grow.setAnimationListener(new EmptyAnimationListener() {
			@Override
			public void onTheAnimationEnd(Animation animation) {
				// firstView.setBackgroundDrawable(original);
				// secondView.setBackgroundDrawable(original);
				// addView.setBackgroundDrawable(original);

				firstView.setVisibility(View.GONE);
				addView.setVisibility(View.GONE);
				secondView.setText(Expression.fractionToString(sum,
						UseParenthesis.NO));

				// TODO conditionally animate moving to common denominator..

				final ScaleAnimation shrink = new ScaleAnimation(2, 1, 2, 1,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				shrink.setDuration(1500);
				shrink.setAnimationListener(new EmptyAnimationListener(
						animations));
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						secondView.startAnimation(shrink);
					}
				}, 10);
			}
		});

		ScaleAnimation leftGrow = new ScaleAnimation(1, 2, 1, 2,
				Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF,
				0.5f);
		ScaleAnimation rightGrow = new ScaleAnimation(1, 2, 1, 2,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
				0.5f);

		grow.setDuration(1500);
		leftGrow.setDuration(1500);
		rightGrow.setDuration(1500);

		// int newBack = Color.BLACK;
		// firstView.setBackgroundColor(newBack);
		// secondView.setBackgroundColor(newBack);
		// addView.setBackgroundColor(newBack);

		firstView.startAnimation(leftGrow);
		addView.startAnimation(grow);
		secondView.startAnimation(rightGrow);
	}

	protected void animateEvaluateX2(final View view, final TextView sol1vIew,
			final Fraction sol1, final Fraction x2Coefficient,
			final TextView x2coeff, final TextView x2coeff_lbl,
			final LinkedList<Runnable> animations) {
		final Fraction sol1_2 = sol1.multiply(sol1);

		ScaleAnimation grow = new ScaleAnimation(1, 2, 1, 2,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		final AtomicBoolean bool = new AtomicBoolean(false);
		grow.setDuration(1500);
		grow.setAnimationListener(new EmptyAnimationListener() {
			@Override
			public void onTheAnimationEnd(Animation animation) {
				if (!bool.compareAndSet(false, true))
					return;
				boolean useParens = x2Coefficient.compareTo(Fraction.ONE) != 0;
				x2coeff_lbl.setText(Expression.fractionToString(sol1_2,
						useParens ? UseParenthesis.FORCE : UseParenthesis.NO));

				final ScaleAnimation shrink = new ScaleAnimation(2, 1, 2, 1,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				shrink.setDuration(1500);
				shrink.setAnimationListener(new EmptyAnimationListener(
						animations));
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						x2coeff_lbl.startAnimation(shrink);
					}
				}, 10);
			}
		});

		animations.addFirst(new Runnable() {
			@Override
			public void run() {
				animateEvaluateX(view, sol1vIew, sol1_2, x2Coefficient, null,
						x2coeff, x2coeff_lbl, animations);
			}
		});

		x2coeff_lbl.startAnimation(grow);
	}

	protected void animateEvaluateX(View view, TextView sol1vIew,
			final Fraction sol1, final Fraction xCoefficient,
			final TextView xcoeff_add, final TextView xcoeff,
			final TextView xcoeff_lbl, final LinkedList<Runnable> animations) {
		Fraction aCoeff = xCoefficient;
		if (xCoefficient.compareTo(Fraction.ZERO) < 0 && xcoeff_add != null
				&& xcoeff_add.getVisibility() == View.VISIBLE) {
			aCoeff = aCoeff.negate();
		}
		final Fraction coeff = aCoeff;

		if (xCoefficient.compareTo(Fraction.ONE) == 0
				|| xCoefficient.compareTo(Fraction.MINUS_ONE) == 0) {
			xcoeff_lbl.setText(Expression.fractionToString(
					sol1.multiply(coeff), UseParenthesis.NO));
			xcoeff.setVisibility(View.GONE);
			Runnable continuation = animations.poll();
			continuation.run();
			return;
		}
		// animate/grow the views
		// replace with a single visible view for the evaluated term's value
		ScaleAnimation leftGrow = new ScaleAnimation(1, 2, 1, 2,
				Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF,
				0.5f);
		leftGrow.setDuration(1500);
		leftGrow.setAnimationListener(new EmptyAnimationListener() {
			@Override
			public void onTheAnimationEnd(Animation animation) {
				xcoeff_lbl.setText(Expression.fractionToString(
						sol1.multiply(coeff), UseParenthesis.NO));
				xcoeff.setVisibility(View.GONE);

				final ScaleAnimation shrink = new ScaleAnimation(2, 1, 2, 1,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				shrink.setDuration(1500);
				shrink.setAnimationListener(new EmptyAnimationListener(
						animations));
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						xcoeff_lbl.startAnimation(shrink);
					}
				}, 10);
			}
		});

		ScaleAnimation rightGrow = new ScaleAnimation(1, 2, 1, 2,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rightGrow.setDuration(1500);

		xcoeff.startAnimation(leftGrow);
		xcoeff_lbl.startAnimation(rightGrow);

	}

	private void animateReplaceX2(View view, TextView sol1vIew, Fraction sol1,
			Fraction coeff, TextView target,
			final LinkedList<Runnable> animations) {
		animateReplaceX(view, sol1vIew, sol1, coeff, target,
				"<sup><small>2</small></sup>", animations);
	}

	private void animateReplaceX(View view, TextView sol1VIew,
			final Fraction sol1, final Fraction fraction,
			final TextView target, final String suffix,
			final LinkedList<Runnable> animations) {
		int[] targetLocation = new int[2];
		target.getLocationOnScreen(targetLocation);

		int[] sourceLocation = new int[2];
		sol1VIew.getLocationOnScreen(sourceLocation);

		final LinearLayout.LayoutParams origTargetParms = (LinearLayout.LayoutParams) target
				.getLayoutParams();

		View parent = view.findViewById(R.id.frame);
		int[] parentLocation = new int[2];
		parent.getLocationOnScreen(parentLocation);
		final TextView anim_text = (TextView) view.findViewById(R.id.anim_text);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				sol1VIew.getWidth(), sol1VIew.getHeight());
		params.leftMargin = sourceLocation[0] - parentLocation[0];
		params.topMargin = sourceLocation[1] - parentLocation[1];
		anim_text.setLayoutParams(params);
		anim_text.setText(Expression.fractionToString(sol1, UseParenthesis.NO));

		// pre-adjust the size of 'x' to hold the solution

		LinearLayout.LayoutParams targetParms = new LinearLayout.LayoutParams(
				Math.max(sol1VIew.getWidth(), target.getWidth()), Math.max(
						sol1VIew.getHeight(), target.getHeight()));
		target.setLayoutParams(targetParms);

		float x = targetLocation[0] - sourceLocation[0];
		float y = targetLocation[1] - sourceLocation[1];
		// AnimationSet animSet = new AnimationSet(true);
		TranslateAnimation move = new TranslateAnimation(0, x, 0, y);
		move.setDuration(1500);
		move.setAnimationListener(new EmptyAnimationListener() {
			@Override
			public void onTheAnimationEnd(Animation animation) {
				// if (fraction.compareTo(object))
				// TODO pretty up the injection of the x value
				boolean useParens = fraction.compareTo(Fraction.ONE) != 0
						|| (sol1.compareTo(Fraction.ZERO) < 0 && !StringUtils
								.isEmpty(suffix));
				target.setText(Html.fromHtml(Expression.fractionToString(sol1,
						useParens ? UseParenthesis.FORCE : UseParenthesis.NO)
						+ suffix));

				target.setLayoutParams(origTargetParms);
				anim_text.setVisibility(View.GONE);

				final ScaleAnimation shrink = new ScaleAnimation(2, 1, 2, 1,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				shrink.setDuration(1500);
				shrink.setAnimationListener(new EmptyAnimationListener(
						animations));
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						target.startAnimation(shrink);
					}
				}, 10);
			}
		});

		ScaleAnimation grow = new ScaleAnimation(1, 2, 1, 2,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		grow.setDuration(1500);

		AnimationSet set = new AnimationSet(true);
		set.addAnimation(move);
		// if (sol1.toString().length() > 4) {
		// ScaleAnimation shrink = new ScaleAnimation(1, 0.5f, 1, 0.5f,
		// Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
		// 0f);
		// shrink.setDuration(1500);
		// set.addAnimation(shrink);
		// }
		anim_text.startAnimation(set);
		target.startAnimation(grow);
	}

	private void initializeExpressionView(Expression lhs,
			ExpressionViews lhsViews) {
		boolean hadAny = false;

		lhsViews.x2coeff.setVisibility(View.VISIBLE);
		lhsViews.x2coeff_lbl.setVisibility(View.VISIBLE);
		lhsViews.xcoeff_add.setVisibility(View.VISIBLE);
		lhsViews.xcoeff.setVisibility(View.VISIBLE);
		lhsViews.xcoeff_lbl.setVisibility(View.VISIBLE);
		lhsViews.const_add.setVisibility(View.VISIBLE);
		lhsViews.x2coeff_lbl.setVisibility(View.VISIBLE);

		lhsViews.x2coeff_lbl.setText(Html
				.fromHtml("x<sup><small>2</small></sup>"));
		lhsViews.xcoeff_lbl.setText(Html.fromHtml("x"));

		// x^2
		Fraction x2Coeff = lhs.getX2Coefficient();
		if (x2Coeff.compareTo(Fraction.ZERO) == 0) {
			lhsViews.x2coeff.setVisibility(View.GONE);
			lhsViews.x2coeff_lbl.setVisibility(View.GONE);
		} else {
			hadAny = true;
			if (x2Coeff.compareTo(Fraction.ONE) == 0) {
				lhsViews.x2coeff.setVisibility(View.GONE);
			} else if (x2Coeff.compareTo(Fraction.MINUS_ONE) == 0) {
				lhsViews.x2coeff.setText("-");
			} else {
				boolean useParens = x2Coeff.getDenominator() != 1;
				lhsViews.x2coeff.setText(Expression.fractionToString(x2Coeff,
						useParens ? UseParenthesis.FORCE : UseParenthesis.NO));
			}
		}

		// x
		Fraction xCoefficient = lhs.getXCoefficient();
		String sign = "  +  ";
		if (!hadAny) {
			lhsViews.xcoeff_add.setVisibility(View.GONE);
		} else {
			if (xCoefficient.compareTo(Fraction.ZERO) < 0) {
				sign = "  -  ";
				xCoefficient = xCoefficient.negate();
			}
		}
		lhsViews.xcoeff_add.setText(sign);
		if (xCoefficient.compareTo(Fraction.ZERO) == 0) {
			lhsViews.xcoeff_add.setVisibility(View.GONE);
			lhsViews.xcoeff.setVisibility(View.GONE);
			lhsViews.xcoeff_lbl.setVisibility(View.GONE);
		} else {
			hadAny = true;
			if (xCoefficient.compareTo(Fraction.ONE) == 0) {
				lhsViews.xcoeff.setVisibility(View.GONE);
			} else if (xCoefficient.compareTo(Fraction.MINUS_ONE) == 0) {
				lhsViews.xcoeff.setText("-");
			} else {
				boolean useParens = xCoefficient.getDenominator() != 1;
				lhsViews.xcoeff.setText(Expression.fractionToString(
						xCoefficient, useParens ? UseParenthesis.FORCE
								: UseParenthesis.NO));
			}
		}

		// constant
		Fraction constant = lhs.getConstant();
		sign = "  +  ";
		if (!hadAny) {
			lhsViews.const_add.setVisibility(View.GONE);
		} else {
			if (constant.compareTo(Fraction.ZERO) < 0) {
				sign = "  -  ";
				constant = constant.negate();
			}
		}
		lhsViews.const_add.setText(sign);
		if (constant.compareTo(Fraction.ZERO) == 0 && hadAny) {
			lhsViews.const_add.setVisibility(View.GONE);
			lhsViews.constant.setVisibility(View.GONE);
		} else {
			lhsViews.constant.setText(Expression.fractionToString(constant,
					UseParenthesis.NO));
		}

	}

	private void nextLevel(final Level nextLevel) {
		activity.saveState = false;
		Runnable nextLevelRunnable = new Runnable() {
			@Override
			public void run() {
				BackgroundMusicHelper.continueMusicOnNextActivity();
				Intent nextLevelIntent = new Intent(activity,
						PuzzleActivity.class);
				nextLevelIntent.putExtra(PuzzleActivity.PUZZLE_ID,
						nextLevel.getId());
				nextLevelIntent.putExtra(PuzzleActivity.IS_CUSTOM,
						activity.fromCustom);
				activity.startActivity(nextLevelIntent);
				activity.finish();
			}
		};
		activity.soundManager.playSound(Sounds.ERASE);
		activity.animateFade(activity.findViewById(R.id.main_layout), activity,
				PuzzleActivity.ERASE_LENGTH_MS, nextLevelRunnable);
	}

	private void restart() {
		activity.restart(true);
	}

	private void share() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(activity);
		String defaultText = activity
				.getString(R.string.default_level_finish_share_message);
		String levelName = puzzle.getId() + "-" + puzzle.getName();

		String titleText = activity
				.getString(R.string.level_finish_share_level_finish_title);
		titleText = titleText.replaceAll("%l", levelName);

		String parametrizedText = preferences.getString(activity
				.getString(R.string.pref_default_share_level_finish_key),
				defaultText);

		String text = parametrizedText.replaceAll("%l", levelName);
		text = text.replaceAll("%m", "" + (puzzle.getNumMoves()));
		text = text.replaceAll("%u", "" + puzzle.getUndosUsed());
		text = text.replaceAll("%d", "" + puzzle.getMultilineDescription());

		ShareHelper.share(activity, titleText, text);
	}

	private void mainMenu() {
		activity.saveState = false;
		activity.launchLevelSelect();
		activity.finish();
	}
}
