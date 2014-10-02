package com.oakonell.findx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.achievement.Achievements.UpdateAchievementResult;
import com.google.example.games.basegameutils.GameHelper;
import com.oakonell.findx.model.Level;
import com.oakonell.findx.model.Puzzle;

public class Achievements {
	private static final String TAG = Achievements.class.getName();

	private List<Achievement> achievements = new ArrayList<Achievements.Achievement>();
	private List<Achievement> levelEndAchievements = new ArrayList<Achievements.Achievement>();

	Achievement trivial = new BooleanAchievement(
			R.string.achievement_the_trivial_solution,
			R.string.achievement_the_trivial_solution_label, "Trivial Solution") {

		@Override
		public void testAndSet(AchievementContext context, Puzzle puzzle) {
			if (!puzzle.getId().equals("1-1"))
				return;

			unlock(context);
		}

	};

	Achievement improvement = new BooleanAchievement(
			R.string.achievement_room_for_improvement,
			R.string.achievement_room_for_improvement_label,
			"Room for Improvement") {

		@Override
		public void testAndSet(AchievementContext context, Puzzle puzzle) {
			int existingRating = puzzle.getExistingRating();
			if (existingRating == 0) {
				return;
			}
			if (puzzle.getRating() <= puzzle.getExistingRating()) {
				return;
			}

			unlock(context);
		}

	};

	class PerfectStateAchievement extends BooleanAchievement {
		private String stageId;

		public PerfectStateAchievement(int achievementId, int stringId,
				String name, String stageId) {
			super(achievementId, stringId, name);
			this.stageId = stageId;
		}

		@Override
		public void testAndSet(AchievementContext context, Puzzle puzzle) {
			if (!puzzle.getStage().getId().equals(stageId)) {
				return;
			}
			for (Level each : puzzle.getStage().getLevels()) {
				// TODO this seemed to be not work when resolving the last one for a three star
				if (puzzle.getId().equals(each.getId())) {
					if (puzzle.getRating() != 3 && each.getRating() != 3) {
						return;
					}
				} else {
					if (each.getRating() != 3) {
						return;
					}
				}
			}

			unlock(context);
		}

	};

	public Achievements() {
		levelEndAchievements.add(trivial);
		levelEndAchievements.add(improvement);

		levelEndAchievements.add(new PerfectStateAchievement(
				R.string.achievement_apprenticed,
				R.string.achievement_apprenticed_label, "Apprenticed", "1"));
		levelEndAchievements.add(new PerfectStateAchievement(
				R.string.achievement_journeyman,
				R.string.achievement_journeyman_label, "JourneyMan", "2"));
		levelEndAchievements.add(new PerfectStateAchievement(
				R.string.achievement_the_masters_degree,
				R.string.achievement_the_masters_degree_label,
				"Master's Degree", "3"));

		achievements.addAll(levelEndAchievements);
	}

	private interface Achievement {
		void push(AchievementContext context);

		boolean isPending();

		String getName();

		void testAndSet(AchievementContext context, Puzzle puzzle);

	}

	private abstract static class BooleanAchievement implements Achievement {
		private boolean value = false;
		private final int achievementId;
		private final int stringId;
		private final String name;

		BooleanAchievement(int achievementId, int stringId, String name) {
			this.achievementId = achievementId;
			this.stringId = stringId;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public boolean isPending() {
			return value;
		}

		public void push(AchievementContext context) {
			if (value) {
				Games.Achievements.unlock(context.getHelper().getApiClient(),
						context.getContext().getString(achievementId));
				value = false;
			}
		}

		public void unlock(final AchievementContext context) {
			boolean isSignedIn = context.getHelper().isSignedIn();
			if (isSignedIn) {
				PendingResult<UpdateAchievementResult> result = Games.Achievements
						.unlockImmediate(context.getHelper().getApiClient(),
								context.getContext().getString(achievementId));
				result.setResultCallback(new ResultCallback<UpdateAchievementResult>() {
					@Override
					public void onResult(
							UpdateAchievementResult achievementResult) {
						if (!achievementResult.getStatus().isSuccess())
							return;
						if (achievementResult.getStatus().getStatusCode() == GamesStatusCodes.STATUS_ACHIEVEMENT_UNLOCKED) {

							Tracker myTracker = GoogleAnalytics.getInstance(
									context.getContext()).newTracker(
									R.string.ga_trackingId);
							Log.i(TAG, "Unlocked achievement " + getName());
							Map<String, String> event = new HitBuilders.EventBuilder()
									.setCategory(
											context.getContext()
													.getString(
															R.string.an_achievement_unlocked))
									.setAction(getName()).build();
							myTracker.send(event);
						}
					}
				});
			}
			if (!context.getHelper().isSignedIn() || BuildConfig.DEBUG) {
				if (!value || BuildConfig.DEBUG) {
					Toast.makeText(
							context.getContext(),
							context.getContext().getString(
									R.string.offline_achievement_label)
									+ " "
									+ context.getContext().getString(stringId),
							Toast.LENGTH_LONG).show();
				}
				value = true;
			}
		}
	}

	private static abstract class IncrementalAchievement implements Achievement {
		private int count = 0;
		private final int achievementId;
		private final int stringId;
		private final String name;

		IncrementalAchievement(int achievementId, int stringId, String name) {
			this.achievementId = achievementId;
			this.stringId = stringId;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public boolean isPending() {
			return count > 0;
		}

		public void push(AchievementContext context) {
			if (count > 0) {
				Games.Achievements.increment(
						context.getHelper().getApiClient(), context
								.getContext().getString(achievementId), count);
				count = 0;
			}
		}

		public void increment(AchievementContext context) {
			if (context.getHelper().isSignedIn()) {
				Games.Achievements.increment(
						context.getHelper().getApiClient(), context
								.getContext().getString(achievementId), 1);
			} else {
				count++;
			}
		}
	}

	public boolean hasPending() {
		for (Achievement each : achievements) {
			if (each.isPending())
				return true;
		}
		return false;
	}

	public void pushToGoogle(AchievementContext context) {
		if (!context.getHelper().isSignedIn())
			return;

		for (Achievement each : achievements) {
			each.push(context);
		}

	}

	public void testAndSetLevelCompleteAchievements(AchievementContext context,
			Puzzle puzzle) {
		for (Achievement each : levelEndAchievements) {
			try {
				each.testAndSet(context, puzzle);
			} catch (RuntimeException e) {
				String text = "Error testing level end achievement "
						+ each.getName();
				if (BuildConfig.DEBUG) {
					Toast.makeText(context.getContext(),
							text + ": " + e.getMessage(), Toast.LENGTH_LONG)
							.show();
				}
				Tracker myTracker = GoogleAnalytics.getInstance(
						context.getContext())
						.newTracker(R.string.ga_trackingId);
				Map<String, String> event = new HitBuilders.ExceptionBuilder()
						.setDescription(
								new StandardExceptionParser(context
										.getContext(), null).getDescription(
										Thread.currentThread().getName(), e))
						.setFatal(false).build();
				myTracker.send(event);
				// don't crash game due to faulty implementation of achievement,
				// just log it
				Log.e(TAG, text + ": " + e.getMessage());
			}
		}
	}

	public interface AchievementContext {
		GameHelper getHelper();

		Context getContext();
	}

}