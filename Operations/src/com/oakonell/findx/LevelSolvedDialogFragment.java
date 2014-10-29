package com.oakonell.findx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.math3.fraction.Fraction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.FrameLayout.LayoutParams;
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
	// 6633b5e5
	private int color = Color.argb(0xFF, 0x33, 0xb5, 0xe5);
	private int pauseGrownDuration = 500;
	private int sumPauseGrownDuration = 1000;
	private int moveDuration = 500;
	private int growDuration = 700;
	private int shrinkDuration = 700;
	private int postDelay = 10;
	private ExpressionViews lhsViews;
	private ExpressionViews rhsViews;
	private SolutionViews sol2Views;
	private SolutionViews sol1Views;

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
		Equation equation = puzzle.getLevel().getEquation();
		final Expression lhs = equation.getLhs();
		final Expression rhs = equation.getRhs();

		// haven't been able to get this to run at the right time, so the
		// positions return valid numbers
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				startSolutionsCheckAnimation(getView(), lhs, rhs, lhsViews,
						rhsViews);
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

		lhsViews = new ExpressionViews();
		lhsViews.x2coeff = (TextView) view.findViewById(R.id.lhs_x2coeff);
		lhsViews.x2coeff_lbl = (TextView) view
				.findViewById(R.id.lhs_x2coeff_lbl);
		lhsViews.xcoeff_add = (TextView) view.findViewById(R.id.lhs_xcoeff_add);
		lhsViews.xcoeff = (TextView) view.findViewById(R.id.lhs_xcoeff);
		lhsViews.xcoeff_lbl = (TextView) view.findViewById(R.id.lhs_xcoeff_lbl);
		lhsViews.const_add = (TextView) view.findViewById(R.id.lhs_const_add);
		lhsViews.constant = (TextView) view.findViewById(R.id.lhs_const);

		rhsViews = new ExpressionViews();
		rhsViews.x2coeff = (TextView) view.findViewById(R.id.rhs_x2coeff);
		rhsViews.x2coeff_lbl = (TextView) view
				.findViewById(R.id.rhs_x2coeff_lbl);
		rhsViews.xcoeff_add = (TextView) view.findViewById(R.id.rhs_xcoeff_add);
		rhsViews.xcoeff = (TextView) view.findViewById(R.id.rhs_xcoeff);
		rhsViews.xcoeff_lbl = (TextView) view.findViewById(R.id.rhs_xcoeff_lbl);
		rhsViews.const_add = (TextView) view.findViewById(R.id.rhs_const_add);
		rhsViews.constant = (TextView) view.findViewById(R.id.rhs_const);

		sol1Views = new SolutionViews();
		sol1Views.sol = (TextView) view.findViewById(R.id.x_sol1);
		sol1Views.solCheck = (TextView) view.findViewById(R.id.check1);
		sol1Views.container = view.findViewById(R.id.sol1_layout);

		sol2Views = new SolutionViews();
		sol2Views.sol = (TextView) view.findViewById(R.id.x_sol2);
		sol2Views.solCheck = (TextView) view.findViewById(R.id.check2);
		sol2Views.container = view.findViewById(R.id.sol2_layout);

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

		final Expression lhs = puzzle.getStartEquation().getLhs();
		final Expression rhs = puzzle.getStartEquation().getRhs();
		final Runnable resetViews = new Runnable() {
			public void run() {
				resetViews(view, lhs, rhs, lhsViews, rhsViews);
			};
		};
		sol1Views.container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				populateViews(finishText, rating, existingRating, ratingBar,
						view);
				Fraction sol1 = puzzle.getSolutions()[0];

				startSolutionCheckAnimation(view, sol1, sol1Views, lhs, rhs,
						lhsViews, rhsViews, resetViews);
			}
		});
		sol2Views.container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				populateViews(finishText, rating, existingRating, ratingBar,
						view);
				Fraction sol2 = puzzle.getSolutions()[1];
				Expression lhs = puzzle.getStartEquation().getLhs();
				Expression rhs = puzzle.getStartEquation().getRhs();
				startSolutionCheckAnimation(view, sol2, sol2Views, lhs, rhs,
						lhsViews, rhsViews, resetViews);
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

	private static class SolutionViews {
		TextView sol;
		TextView solCheck;
		View container;
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

		Equation equation = puzzle.getLevel().getEquation();
		Expression lhs = equation.getLhs();
		Expression rhs = equation.getRhs();
		resetViews(view, lhs, rhs, lhsViews, rhsViews);

	}

	private void initializeSolutions(View view) {
		Fraction[] solutions = puzzle.getSolutions();
		sol1Views.sol.setText(Expression.fractionToString(solutions[0],
				UseParenthesis.NO));

		if (solutions.length > 1) {
			sol2Views.container.setVisibility(View.VISIBLE);
			sol2Views.sol.setText(Expression.fractionToString(solutions[1],
					UseParenthesis.NO));
		}
	}

	protected void startSolutionsCheckAnimation(final View view,
			final Expression lhs, final Expression rhs,
			final ExpressionViews lhsViews, final ExpressionViews rhsViews) {
		Fraction solutions[] = puzzle.getSolutions();
		Fraction sol1 = solutions[0];

		final Runnable reset = new Runnable() {
			@Override
			public void run() {
				resetViews(view, lhs, rhs, lhsViews, rhsViews);
			}
		};
		Runnable next = reset;

		if (solutions.length > 1) {
			final Fraction sol2 = solutions[1];
			Runnable checkSecondSolution = new Runnable() {
				@Override
				public void run() {
					resetViews(view, lhs, rhs, lhsViews, rhsViews);
					startSolutionCheckAnimation(view, sol2, sol2Views, lhs,
							rhs, lhsViews, rhsViews, reset);
				}
			};
			next = checkSecondSolution;
		}
		startSolutionCheckAnimation(view, sol1, sol1Views, lhs, rhs, lhsViews,
				rhsViews, next);

	}

	private void resetViews(final View view, final Expression lhs,
			final Expression rhs, final ExpressionViews lhsViews,
			final ExpressionViews rhsViews) {
		initializeExpressionView(lhs, lhsViews);
		initializeExpressionView(rhs, rhsViews);
		initializeSolutions(view);
	}

	private void startSolutionCheckAnimation(final View view,
			final Fraction solution, final SolutionViews solutionViews,
			final Expression lhs, final Expression rhs,
			final ExpressionViews lhsViews, final ExpressionViews rhsViews,
			Runnable continuation) {

		solutionViews.solCheck.setText("");
		final LinkedList<Runnable> animations = new LinkedList<Runnable>();
		// animate left hand side
		animations.add(new Runnable() {
			@Override
			public void run() {
				animateExpr(view, solution, solutionViews, lhs, lhsViews,
						animations);
			}
		});
		// animate right hand side
		animations.add(new Runnable() {
			@Override
			public void run() {
				animateExpr(view, solution, solutionViews, rhs, rhsViews,
						animations);
			}
		});
		// animate check mark?
		animations.add(new Runnable() {
			@Override
			public void run() {
				// check mark
				solutionViews.solCheck.setText(Html
						.fromHtml("<font color=\"#32cd32\"><bold>\u2713</bold></font>"));
				Runnable runnable = animations.poll();
				if (runnable != null) {
					handler.postDelayed(runnable, pauseGrownDuration);
				}
			}

		});
		animations.add(continuation);
		animations.remove().run();
	}

	private void animateExpr(final View view, final Fraction sol1,
			final SolutionViews solViews, final Expression expr,
			final ExpressionViews exprViews,
			final LinkedList<Runnable> animations) {

		List<Runnable> localList = new ArrayList<Runnable>();
		// replace X^2
		if (expr.hasX2Coefficient()) {
			localList.add(new Runnable() {
				@Override
				public void run() {
					animateReplaceX2(view, solViews, sol1,
							expr.getX2Coefficient(), exprViews, animations);
				}
			});
		}
		// replace X
		if (expr.hasXCoefficient()) {
			localList.add(new Runnable() {
				@Override
				public void run() {
					animateReplaceX(view, solViews, sol1,
							expr.getXCoefficient(), exprViews, animations);
				}
			});
		}

		// evaluate each term
		if (expr.hasX2Coefficient()) {
			localList.add(new Runnable() {
				@Override
				public void run() {
					animateEvaluateX2(view, solViews, sol1,
							expr.getX2Coefficient(), exprViews, animations);
				}
			});
		}
		if (expr.hasXCoefficient()
				&& expr.getXCoefficient().compareTo(Fraction.ONE) != 0) {
			localList.add(new Runnable() {
				@Override
				public void run() {
					animateEvaluateX(view, solViews, sol1,
							expr.getXCoefficient(), exprViews, animations);
				}
			});
		}

		// sum terms
		if (expr.hasX2Coefficient()) {
			if (expr.hasXCoefficient()) {
				// x2 + x terms
				final Fraction xTerm = expr.getXCoefficient().multiply(sol1);
				final Fraction x2Term = expr.getX2Coefficient().multiply(sol1)
						.multiply(sol1);
				final Fraction sum = x2Term.add(xTerm);
				localList.add(new Runnable() {
					@Override
					public void run() {
						animateSum(x2Term, exprViews.x2coeff_lbl,
								exprViews.xcoeff_add, xTerm,
								exprViews.xcoeff_lbl, animations);
					}
				});
				if (expr.hasConstant()) {
					// add the constant
					localList.add(new Runnable() {
						@Override
						public void run() {
							animateSum(sum, exprViews.xcoeff_lbl,
									exprViews.const_add, expr.getConstant(),
									exprViews.constant, animations);
						}
					});
				}
			} else if (expr.hasConstant()) {
				// x2 + constant terms
				final Fraction x2Term = expr.getX2Coefficient().multiply(sol1)
						.multiply(sol1);
				final Fraction constTerm = expr.getConstant();
				localList.add(new Runnable() {
					@Override
					public void run() {
						animateSum(x2Term, exprViews.x2coeff_lbl,
								exprViews.const_add, constTerm,
								exprViews.constant, animations);
					}
				});
			} else {
				// there was only the x^2 term
			}
		} else if (expr.hasXCoefficient()) {
			if (expr.hasConstant()) {
				// x + constant terms
				final Fraction xTerm = expr.getXCoefficient().multiply(sol1);
				final Fraction constTerm = expr.getConstant();
				localList.add(new Runnable() {
					@Override
					public void run() {
						animateSum(xTerm, exprViews.xcoeff_lbl,
								exprViews.const_add, constTerm,
								exprViews.constant, animations);
					}
				});
			} else {
				// there was only the x term
			}
		}

		animations.addAll(0, localList);
		animations.remove().run();
	}

	private void animateSum(final Fraction firstTerm, final TextView firstView,
			final TextView addView, final Fraction secondTerm,
			final TextView secondView, final LinkedList<Runnable> animations) {
		final Fraction sum = firstTerm.add(secondTerm);
		final int currentTextColor = firstView.getCurrentTextColor();
		final Drawable originalBackground = firstView.getBackground();
		// final int backColor = getBackgroundColor(firstView);
		// int newBackColor = Color.argb(0xFF, 0x99, 0x99, 0x99);
		// int newBackColor = Color.argb(0x00, 0, 0, 0);

		ScaleAnimation grow = new ScaleAnimation(1, 2, 1, 2,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		grow.setAnimationListener(new EmptyAnimationListener() {
			@Override
			public void onTheAnimationEnd(Animation animation) {
				// firstView.setBackgroundDrawable(original);
				// secondView.setBackgroundDrawable(original);
				// addView.setBackgroundDrawable(original);

				// conditionally animate moving to common denominator..
				final Runnable setAndShrink = new Runnable() {
					@Override
					public void run() {
						firstView.setVisibility(View.GONE);
						addView.setVisibility(View.GONE);
						secondView.setText(Expression.fractionToString(sum,
								UseParenthesis.NO));

						final ScaleAnimation pause = new ScaleAnimation(2, 2,
								2, 2, Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);
						pause.setDuration(pauseGrownDuration);
						pause.setAnimationListener(new EmptyAnimationListener() {
							@Override
							protected void onTheAnimationEnd(Animation animation) {
								final ScaleAnimation shrink = new ScaleAnimation(
										2, 1, 2, 1, Animation.RELATIVE_TO_SELF,
										0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
								shrink.setDuration(shrinkDuration);
								shrink.setAnimationListener(new EmptyAnimationListener(
										animations) {
									@Override
									protected void onTheAnimationEnd(
											Animation animation) {
										firstView
												.setTextColor(currentTextColor);
										addView.setTextColor(currentTextColor);
										secondView
												.setTextColor(currentTextColor);
										firstView
												.setBackgroundDrawable(originalBackground);
										addView.setBackgroundDrawable(originalBackground);
										secondView
												.setBackgroundDrawable(originalBackground);
									}
								});
								handler.postDelayed(new Runnable() {
									@Override
									public void run() {
										secondView.startAnimation(shrink);
									}
								}, postDelay);
							}
						});
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								secondView.startAnimation(pause);
							}
						}, postDelay);

					}

				};

				int resultDenom = sum.getDenominator();
				if (resultDenom != 1) {
					int newFirstNum = firstTerm.getNumerator();
					int newSecondNum = secondTerm.getNumerator();
					if (resultDenom != firstTerm.getDenominator()) {
						newFirstNum = firstTerm.getNumerator()
								* (resultDenom / firstTerm.getDenominator());
					}
					if (resultDenom != secondTerm.getDenominator()) {
						newSecondNum = secondTerm.getNumerator()
								* (resultDenom / secondTerm.getDenominator());
					}
					firstView.setText(newFirstNum + "/" + resultDenom);
					secondView.setText(Math.abs(newSecondNum) + "/"
							+ resultDenom);

					final ScaleAnimation leftPause = new ScaleAnimation(2, 2,
							2, 2, Animation.RELATIVE_TO_SELF, 1f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					final ScaleAnimation rightPause = new ScaleAnimation(2, 2,
							2, 2, Animation.RELATIVE_TO_SELF, 0f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					final ScaleAnimation pause = new ScaleAnimation(2, 2, 2, 2,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);

					pause.setDuration(sumPauseGrownDuration);
					leftPause.setDuration(sumPauseGrownDuration);
					rightPause.setDuration(sumPauseGrownDuration);
					pause.setAnimationListener(new EmptyAnimationListener() {
						@Override
						protected void onTheAnimationEnd(Animation animation) {
							handler.postDelayed(setAndShrink, postDelay);
						}
					});
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							// need to pause the first and add views, as well
							firstView.startAnimation(leftPause);
							addView.startAnimation(pause);
							secondView.startAnimation(rightPause);
						}
					}, postDelay);

				} else {
					setAndShrink.run();
				}

			}
		});

		ScaleAnimation leftGrow = new ScaleAnimation(1, 2, 1, 2,
				Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF,
				0.5f);
		ScaleAnimation rightGrow = new ScaleAnimation(1, 2, 1, 2,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
				0.5f);

		grow.setDuration(growDuration);
		leftGrow.setDuration(growDuration);
		rightGrow.setDuration(growDuration);

		// int newBack = Color.BLACK;
		// firstView.setBackgroundColor(newBack);
		// secondView.setBackgroundColor(newBack);
		// addView.setBackgroundColor(newBack);
		firstView.setTextColor(color);
		addView.setTextColor(color);
		secondView.setTextColor(color);
		// firstView.setAlpha(1);
		// secondView.setAlpha(1);

		// leftGrow.setBackgroundColor(Color.BLACK);
		// grow.setBackgroundColor(Color.BLACK);
		// rightGrow.setBackgroundColor(Color.BLACK);

		// firstView.setBackgroundColor(newBackColor);
		// //addView.setBackgroundColor(newBackColor);
		// secondView.setBackgroundColor(newBackColor);

		firstView.startAnimation(leftGrow);
		addView.startAnimation(grow);
		secondView.startAnimation(rightGrow);
	}

	protected void animateEvaluateX2(final View view,
			final SolutionViews solViews, final Fraction sol1,
			final Fraction x2Coefficient, final ExpressionViews exprViews,
			final LinkedList<Runnable> animations) {
		final int currentTextColor = exprViews.x2coeff_lbl
				.getCurrentTextColor();
		final Fraction sol1_2 = sol1.multiply(sol1);

		ScaleAnimation grow = new ScaleAnimation(1, 2, 1, 2,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		final AtomicBoolean bool = new AtomicBoolean(false);
		grow.setDuration(growDuration);
		grow.setAnimationListener(new EmptyAnimationListener() {
			@Override
			public void onTheAnimationEnd(Animation animation) {
				if (!bool.compareAndSet(false, true))
					return;
				boolean useParens = x2Coefficient.compareTo(Fraction.ONE) != 0;
				exprViews.x2coeff_lbl.setText(Expression.fractionToString(
						sol1_2, useParens ? UseParenthesis.FORCE
								: UseParenthesis.NO));

				final ScaleAnimation pause = new ScaleAnimation(2, 2, 2, 2,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				pause.setDuration(pauseGrownDuration);
				pause.setAnimationListener(new EmptyAnimationListener() {
					@Override
					protected void onTheAnimationEnd(Animation animation) {
						final ScaleAnimation shrink = new ScaleAnimation(2, 1,
								2, 1, Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);
						shrink.setDuration(shrinkDuration);
						shrink.setAnimationListener(new EmptyAnimationListener(
								animations) {
							@Override
							protected void onTheAnimationEnd(Animation animation) {
								exprViews.x2coeff_lbl
										.setTextColor(currentTextColor);
							}
						});
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								exprViews.x2coeff_lbl.startAnimation(shrink);
							}
						}, postDelay);
					}
				});
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						exprViews.x2coeff_lbl.startAnimation(pause);
					}
				}, postDelay);

			}
		});

		animations.addFirst(new Runnable() {
			@Override
			public void run() {
				animateBasicEvaluateX(view, solViews, sol1_2, x2Coefficient,
						null, exprViews.x2coeff, exprViews.x2coeff_lbl,
						animations);
			}
		});

		exprViews.x2coeff_lbl.setTextColor(color);
		exprViews.x2coeff_lbl.startAnimation(grow);
	}

	protected void animateEvaluateX(View view, SolutionViews solutionViews,
			final Fraction solution, final Fraction xCoefficient,
			ExpressionViews expr, final LinkedList<Runnable> animations) {
		animateBasicEvaluateX(view, solutionViews, solution, xCoefficient,
				expr.xcoeff_add, expr.xcoeff, expr.xcoeff_lbl, animations);
	}

	protected void animateBasicEvaluateX(View view,
			SolutionViews solutionViews, final Fraction solution,
			final Fraction xCoefficient, final TextView xcoeffAddView,
			final TextView xcoeffView, final TextView xcoeffLblView,
			final LinkedList<Runnable> animations) {
		final int currentTextColor = xcoeffView.getCurrentTextColor();
		Fraction aCoeff = xCoefficient;
		if (xCoefficient.compareTo(Fraction.ZERO) < 0 && xcoeffAddView != null
				&& xcoeffAddView.getVisibility() == View.VISIBLE) {
			aCoeff = aCoeff.negate();
		}
		final Fraction coeff = aCoeff;

		if (xCoefficient.compareTo(Fraction.ONE) == 0
				|| xCoefficient.compareTo(Fraction.MINUS_ONE) == 0) {
			xcoeffLblView.setText(Expression.fractionToString(
					solution.multiply(coeff), UseParenthesis.NO));
			xcoeffView.setVisibility(View.GONE);
			Runnable continuation = animations.poll();
			continuation.run();
			return;
		}
		// animate/grow the views
		// replace with a single visible view for the evaluated term's value
		ScaleAnimation leftGrow = new ScaleAnimation(1, 2, 1, 2,
				Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF,
				0.5f);
		leftGrow.setDuration(growDuration);

		ScaleAnimation rightGrow = new ScaleAnimation(1, 2, 1, 2,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rightGrow.setDuration(growDuration);

		xcoeffView.setTextColor(color);
		xcoeffLblView.setTextColor(color);
		ScaleAnimation pauseRightGrow = new ScaleAnimation(1, 1, 1, 1,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
				0.5f);
		pauseRightGrow.setStartOffset(growDuration);
		pauseRightGrow.setDuration(pauseGrownDuration);
		ScaleAnimation pauseLeftGrow = new ScaleAnimation(1, 1, 1, 1,
				Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF,
				0.5f);
		pauseLeftGrow.setStartOffset(growDuration);
		pauseLeftGrow.setDuration(pauseGrownDuration);

		AnimationSet leftGrowSet = new AnimationSet(true);
		leftGrowSet.addAnimation(leftGrow);
		leftGrowSet.addAnimation(pauseLeftGrow);
		AnimationSet rightGrowSet = new AnimationSet(true);
		rightGrowSet.addAnimation(rightGrow);
		rightGrowSet.addAnimation(pauseRightGrow);

		leftGrowSet.setAnimationListener(new EmptyAnimationListener() {
			@Override
			public void onTheAnimationEnd(Animation animation) {
				xcoeffLblView.setText(Expression.fractionToString(
						solution.multiply(coeff), UseParenthesis.NO));
				xcoeffView.setVisibility(View.GONE);

				final ScaleAnimation pause = new ScaleAnimation(2, 2, 2, 2,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				pause.setDuration(pauseGrownDuration);
				pause.setAnimationListener(new EmptyAnimationListener() {
					@Override
					protected void onTheAnimationEnd(Animation animation) {
						final ScaleAnimation shrink = new ScaleAnimation(2, 1,
								2, 1, Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);
						shrink.setDuration(shrinkDuration);
						shrink.setAnimationListener(new EmptyAnimationListener(
								animations) {
							@Override
							protected void onTheAnimationEnd(Animation animation) {
								xcoeffView.setTextColor(currentTextColor);
								xcoeffLblView.setTextColor(currentTextColor);
							}
						});
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								xcoeffLblView.startAnimation(shrink);
							}
						}, postDelay);
					}
				});
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						xcoeffLblView.startAnimation(pause);
					}
				}, postDelay);

			}
		});

		xcoeffView.startAnimation(leftGrowSet);
		xcoeffLblView.startAnimation(rightGrowSet);

	}

	private void animateReplaceX2(View view, SolutionViews solViews,
			Fraction sol1, Fraction coeff, ExpressionViews exprViews,
			final LinkedList<Runnable> animations) {
		animateBasicReplaceX(view, solViews, sol1, coeff,
				exprViews.x2coeff_lbl, "<sup><small>2</small></sup>",
				animations);
	}

	private void animateReplaceX(View view, SolutionViews solViews,
			final Fraction sol1, final Fraction coeff,
			ExpressionViews exprViews, final LinkedList<Runnable> animations) {
		animateBasicReplaceX(view, solViews, sol1, coeff, exprViews.xcoeff_lbl,
				"", animations);
	}

	private void animateBasicReplaceX(View view, SolutionViews solViews,
			final Fraction solution, final Fraction coeff,
			final TextView target, final String suffix,
			final LinkedList<Runnable> animations) {
		final int currentTextColor = target.getCurrentTextColor();
		int[] targetLocation = new int[2];
		target.getLocationOnScreen(targetLocation);

		int[] sourceLocation = new int[2];
		solViews.sol.getLocationOnScreen(sourceLocation);

		final LinearLayout.LayoutParams origTargetParms = (LinearLayout.LayoutParams) target
				.getLayoutParams();

		View parent = view.findViewById(R.id.frame);
		int[] parentLocation = new int[2];
		parent.getLocationOnScreen(parentLocation);
		final TextView anim_text = (TextView) view.findViewById(R.id.anim_text);
		FrameLayout.LayoutParams params = (LayoutParams) anim_text
				.getLayoutParams();
		// new
		// FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
		// solViews.sol.getWidth(), solViews.sol.getHeight());
		params.leftMargin = sourceLocation[0] - parentLocation[0];
		params.topMargin = sourceLocation[1] - parentLocation[1];
		anim_text.setLayoutParams(params);
		anim_text.setText(Expression.fractionToString(solution,
				UseParenthesis.NO));

		// pre-adjust the size of 'x' to hold the solution

		LinearLayout.LayoutParams targetParms = new LinearLayout.LayoutParams(
				Math.max(solViews.sol.getWidth(), target.getWidth()), Math.max(
						solViews.sol.getHeight(), target.getHeight()));
		target.setLayoutParams(targetParms);

		float x = targetLocation[0] - sourceLocation[0];
		float y = targetLocation[1] - sourceLocation[1];
		// AnimationSet animSet = new AnimationSet(true);
		final TranslateAnimation move = new TranslateAnimation(0, x, 0, y);
		move.setDuration(moveDuration);
		move.setAnimationListener(new EmptyAnimationListener() {
			@Override
			public void onTheAnimationEnd(Animation animation) {
				// pretty up the injection of the x value
				boolean useParens = coeff.compareTo(Fraction.ONE) != 0
						|| (solution.compareTo(Fraction.ZERO) < 0 && !StringUtils
								.isEmpty(suffix));
				target.setText(Html.fromHtml(Expression.fractionToString(
						solution, useParens ? UseParenthesis.FORCE
								: UseParenthesis.NO)
						+ suffix));

				target.setLayoutParams(origTargetParms);
				anim_text.setVisibility(View.GONE);

				final ScaleAnimation pause = new ScaleAnimation(2, 2, 2, 2,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				pause.setDuration(pauseGrownDuration);
				pause.setAnimationListener(new EmptyAnimationListener() {
					@Override
					protected void onTheAnimationEnd(Animation animation) {
						final ScaleAnimation shrink = new ScaleAnimation(2, 1,
								2, 1, Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);
						shrink.setDuration(shrinkDuration);
						shrink.setAnimationListener(new EmptyAnimationListener(
								animations) {
							@Override
							protected void onTheAnimationEnd(Animation animation) {
								anim_text.setTextColor(currentTextColor);
								target.setTextColor(currentTextColor);
							}
						});
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								target.startAnimation(shrink);
							}
						}, postDelay);
					}
				});
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						target.startAnimation(pause);
					}
				}, postDelay);
			}
		});

		final ScaleAnimation grow = new ScaleAnimation(1, 2, 1, 2,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		grow.setDuration(moveDuration);

		anim_text.setTextColor(color);
		target.setTextColor(color);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				anim_text.startAnimation(move);
				target.startAnimation(grow);
			}
		}, postDelay);
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
