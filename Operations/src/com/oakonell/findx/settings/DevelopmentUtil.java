package com.oakonell.findx.settings;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;

public class DevelopmentUtil {
	private static final String LogTag = DevelopmentUtil.class.getName();

	public static class Info {
		public Info(GameHelper helper) {
			accountName = Games.getCurrentAccountName(helper.getApiClient());
			scopes = Scopes.GAMES;
			achievementIntent = Games.Achievements.getAchievementsIntent(helper
					.getApiClient());
			allLeaderboardsIntent = Games.Leaderboards
					.getAllLeaderboardsIntent(helper.getApiClient());
		}

		public String scopes;
		public String accountName;
		public Intent achievementIntent;
		public Intent allLeaderboardsIntent;
	}

	public static void resetAchievements(Activity context, Info helper) {
		// as seen on
		// http://stackoverflow.com/questions/17658732/reset-achievements-leaderboard-from-my-android-application
		ProgressDialog dialog = ProgressDialog.show(context,
				"Resetting Achievements", "Please Wait...");
		new AchievementsResetterTask(context, helper, dialog)
				.execute((Void) null);
	}

	// public static void resetLeaderboards(Activity context, Info helper) {
	// ProgressDialog dialog = ProgressDialog.show(context,
	// "Resetting Leaderboards", "Please Wait...");
	// new LeaderboardResetterTask(context, helper, dialog)
	// .execute((Void) null);
	// }

	private static class AchievementsResetterTask extends
			AsyncTask<Void, Void, Void> {
		private Activity mContext;
		private Info helper;
		private ProgressDialog dialog;

		public AchievementsResetterTask(Activity con, Info helper,
				ProgressDialog dialog) {
			mContext = con;
			this.helper = helper;
			this.dialog = dialog;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				String scope = "oauth2:"
						+ "https://www.googleapis.com/auth/games";
				String accesstoken = GoogleAuthUtil.getToken(mContext,
						helper.accountName, scope);

				HttpClient client = new DefaultHttpClient();
				// Reset a single achievement like this:
				/*
				 * String acheivementid = "acheivementid"; HttpPost post = new
				 * HttpPost ( "https://www.googleapis.com"+
				 * "/games/v1management"+ "/achievements/"+ acheivementid+
				 * "/reset?access_token="+accesstoken );
				 */

				// This resets all achievements:
				HttpPost post = new HttpPost("https://www.googleapis.com"
						+ "/games/v1management" + "/achievements"
						+ "/reset?access_token=" + accesstoken);

				HttpResponse response = client.execute(post);
				int callResponseCode = response.getStatusLine().getStatusCode();
				String stringResponse = EntityUtils.toString(response
						.getEntity());
				Log.i(LogTag, "Reset achievements done: " + callResponseCode
						+ "-- " + stringResponse);
			} catch (Exception e) {
				Toast.makeText(mContext,
						"Error resetting achievements: " + e.getMessage(),
						Toast.LENGTH_SHORT).show();
				Log.e(LogTag, "Failed to reset: " + e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			dialog.dismiss();
			// Launch activity to refresh data on client.
			// NOTE: Incremental achievements will look like they are not reset.
			// However, next time you and some steps it will start from 0 and
			// gui will look ok.
			mContext.startActivityForResult(helper.achievementIntent, 0);
		}
	}

//	private static class LeaderboardResetterTask extends
//			AsyncTask<Void, Void, Void> {
//		private Activity mContext;
//		private Info helper;
//		private ProgressDialog dialog;
//
//		public LeaderboardResetterTask(Activity con, Info helper,
//				ProgressDialog dialog) {
//			mContext = con;
//			this.helper = helper;
//			this.dialog = dialog;
//		}
//
//		@Override
//		protected Void doInBackground(Void... params) {
//			try {
//				String scope = "oauth2:https://www.googleapis.com/auth/games";
//
//				String accesstoken = GoogleAuthUtil.getToken(mContext,
//						helper.accountName, scope);
//
//				FindXApp app = (FindXApp) mContext.getApplication();
//				for (String leaderboardid : app.getLeaderboards()
//						.getLeaderboardIds(mContext)) {
//					HttpClient client = new DefaultHttpClient();
//					// Reset leader board:
//					HttpPost post = new HttpPost("https://www.googleapis.com"
//							+ "/games/v1management" + "/leaderboards/"
//							+ leaderboardid + "/scores/reset?access_token="
//							+ accesstoken);
//
//					HttpResponse response = client.execute(post);
//					int callResponseCode = response.getStatusLine()
//							.getStatusCode();
//					String stringResponse = EntityUtils.toString(response
//							.getEntity());
//					Log.i(LogTag, "Reset leaderboard done: " + callResponseCode
//							+ "-- " + stringResponse);
//				}
//				Log.i(LogTag, "Reset leaderboards done.");
//			} catch (Exception e) {
//				Toast.makeText(mContext,
//						"Error resetting leaderboards: " + e.getMessage(),
//						Toast.LENGTH_SHORT).show();
//				Log.e(LogTag,
//						"Failed to reset leaderboards: " + e.getMessage(), e);
//			}
//
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void result) {
//			dialog.dismiss();
//			// Launch activity to refresh data on client.
//			mContext.startActivityForResult(helper.allLeaderboardsIntent, 0);
//		}
//	}

	private static void complain(final Activity activity, String message) {
		AlertDialog.Builder bld = new AlertDialog.Builder(activity);
		bld.setMessage(message);
		bld.setNeutralButton("OK", null);
		Log.d(LogTag, "Showing alert dialog: " + message);
		bld.show();
	}

}
