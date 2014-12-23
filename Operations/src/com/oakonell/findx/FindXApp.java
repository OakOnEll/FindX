package com.oakonell.findx;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.oakonell.findx.settings.DevelopmentUtil.Info;
import com.parse.Parse;

public class FindXApp extends Application {
	private static Context mContext;
	private Achievements achievements = new Achievements();
	private Intent settingsIntent;

	private Tracker tracker;

	public Tracker getTracker() {
		if (tracker != null) {
			return tracker;
		}
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
		analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
		tracker = analytics.newTracker(getString(R.xml.analytics));
		return tracker;

	}

	public Achievements getAchievements() {
		return achievements;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, getString(R.string.parse_app_id),
				getString(R.string.parse_client_key));
		mContext = this;
	}

	public static Context getContext() {
		return mContext;
	}

	public Intent getSettingsIntent() {
		return settingsIntent;
	}

	public void setSettingsIntent(Intent settingsIntent) {
		this.settingsIntent = settingsIntent;
	}

	private Info info;

	public void setDevelopInfo(Info info) {
		this.info = info;
	}

	public Info getDevelopInfo() {
		return info;
	}
}
