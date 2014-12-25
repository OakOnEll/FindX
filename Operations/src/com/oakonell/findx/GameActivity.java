package com.oakonell.findx;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.GameHelper;
import com.oakonell.findx.Achievements.AchievementContext;
import com.oakonell.findx.custom.parse.ParseConnectivity;

public abstract class GameActivity extends BaseGameActivity implements
		AchievementContext {

	public GameActivity() {
		super();
	}

	private Runnable onSignIn;

	protected void setOnSignIn(Runnable onSignIn) {
		this.onSignIn = onSignIn;
	}
	
	@Override
	public void onSignInFailed() {
		if (BuildConfig.DEBUG) {
			Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
		}
		onSignIn = null;
	}

	@Override
	public void onSignInSucceeded() {
		FindXApp app = getFindXApplication();
		Intent settingsIntent = Games.getSettingsIntent(getApiClient());
		app.setSettingsIntent(settingsIntent);

		ParseConnectivity.connect(this, getGameHelper());

		Achievements achievements = app.getAchievements();
		if (achievements.hasPending()) {
			achievements.pushToGoogle(this);
		}

		if (onSignIn != null) {
			onSignIn.run();
			onSignIn = null;
		}

	}

	@Override
	public GameHelper getHelper() {
		return getGameHelper();
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public FindXApp getFindXApplication() {
		return (FindXApp) getApplication();
	}
}
