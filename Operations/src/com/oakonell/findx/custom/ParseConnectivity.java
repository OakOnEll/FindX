package com.oakonell.findx.custom;

import java.security.MessageDigest;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;
import com.oakonell.findx.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class ParseConnectivity {
	private static final String TAG = "ParseConnectivity";

	public void login(Context context, GameHelper helper) {
		// TODO check network connectitivity
		String privateEmail = Games
				.getCurrentAccountName(helper.getApiClient());
		String opaqueUserName = getParseOpaqueUserName(context, privateEmail);

		String password = getParsePassword(context, privateEmail);
		if (password == null)
			return;

		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", opaqueUserName);

		ParseUser parseUser;
		try {
			parseUser = query.getFirst();
		} catch (ParseException e) {
			// no results found for query exception, if not found
			parseUser = null;
		}
		if (parseUser != null) {
			login(context, helper, opaqueUserName, password);
			return;
		}
		createUser(context, helper, opaqueUserName, password);
	}

	private void reportError(Context context, String error) {
		Log.e(TAG, error);
		Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
	}

	private String getParseOpaqueUserName(Context context, String user) {
		// TODO
		try {
			MessageDigest digester = MessageDigest.getInstance("SHA-1");
			String pre = "User" + context.getString(R.string.hash_key);
			String post = context.getString(R.string.hash_key);
			byte[] digestBytes = digester.digest((pre + user + post)
					.getBytes("UTF-8"));
			return Base64.encodeToString(digestBytes, Base64.DEFAULT);
		} catch (Exception e) {
			reportError(
					context,
					"Error getting password to sign in to Parse.com: "
							+ e.getMessage());
			return null;
		}
	}

	private String getParsePassword(Context context, String user) {
		// TODO
		try {
			MessageDigest digester = MessageDigest.getInstance("SHA-1");
			String pre = context.getString(R.string.hash_key);
			String post = context.getString(R.string.hash_key);
			byte[] digestBytes = digester.digest((pre + user + post)
					.getBytes("UTF-8"));
			return Base64.encodeToString(digestBytes, Base64.DEFAULT);
		} catch (Exception e) {
			reportError(
					context,
					"Error getting password to sign in to Parse.com: "
							+ e.getMessage());
			return null;
		}
	}

	private void createUser(final Context context, GameHelper helper,
			String username, String password) {
		ParseUser user = new ParseUser();
		user.setUsername(username);
		user.setPassword(password);
		// user.setEmail(mEmail);
		user.signUpInBackground(new SignUpCallback() {
			public void done(ParseException e) {
				if (e == null) {
					// we're good
				} else {
					// Sign up didn't succeed. Look at the ParseException
					// to figure out what went wrong
					reportError(
							context,
							"Error creating a new user for Parse.com: "
									+ e.getMessage());
					// signUpMsg("Account already taken.");
				}
			}

		});

	}

	private void login(final Context context, GameHelper helper, String user,
			String password) {
		ParseUser.logInInBackground(user, password, new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException e) {
				if (e == null) {
					// we're good
				} else {
					reportError(context,
							"Error signing in to Parse.com: " + e.getMessage());
				}
			}
		});
	}

	public static void connect(final Activity context, final GameHelper helper) {
		if (ParseUser.getCurrentUser() != null)
			return;
		// AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>()
		// {
		// @Override
		// protected Void doInBackground(Void... params) {
		ParseConnectivity newMe = new ParseConnectivity();
		newMe.login(context, helper);
		// return null;
		// }
		//
		// };
		// task.execute();
	}

	public static void logout() {
		ParseUser.logOut();
	}

	public static void createUniqueNickname(FragmentActivity context, Runnable continuation) {
		PromptNicknameFragment frag = new PromptNicknameFragment();
		frag.initialize(continuation);
		frag.show(context.getSupportFragmentManager(), "popup");
	}

	public static boolean modifyNickName(String nick) {
		ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
		userQuery.whereEqualTo("nickname", nick);
		List<ParseUser> users;
		try {
			users = userQuery.find();
		} catch (ParseException e) {
			throw new RuntimeException(
					"Error trying to find user with nickname", e);
		}
		if (!users.isEmpty()) {
			return false;
		}

		ParseUser currentUser = ParseUser.getCurrentUser();
		currentUser.put("nickname", nick);
		try {
			currentUser.save();
		} catch (ParseException e) {
			throw new RuntimeException("Error saving user's nickname", e);
		}

		return true;
	}

}
