package com.oakonell.findx;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.GameHelper;
import com.oakonell.findx.Achievements.AchievementContext;

public abstract class GameActivity extends BaseGameActivity implements
		AchievementContext {

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
