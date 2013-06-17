package com.oakonell.findx;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;

public class BackgroundMusicHelper {
	private static MediaPlayer player;
	private static int resource;

	private static boolean continueMusic;

	private static void start(Context context, int res) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean playMusic = preferences.getBoolean(
				context.getString(R.string.pref_music_key), true);
		// preferences.registerOnSharedPreferenceChangeListener(listener)

		if (res != resource && player != null || !playMusic) {
			release();
		}
		resource = res;
		if (!playMusic) {
			return;
		}
		if (player == null) {
			player = MediaPlayer.create(context, res);
			player.setLooping(true);
		}
		player.start();
		continueMusic = false;
	}

	private static void release() {
		if (player != null) {
			player.release();
		}
		player = null;
	}

	public static void onActivityPause() {
		if (continueMusic) {
			return;
		}
		if (player != null) {
			player.pause();
		}
	}

	public static void onActivityResume(Context context, int res) {
		start(context, res);
	}

	public static void onActivtyCreate(Context context, int res) {
		start(context, res);
	}

	public static void onActivityDestroy() {
		if (player != null && player.isPlaying()) {
			return;
		}
		release();
	}

	public static void continueMusicOnNextActivity() {
		continueMusic = true;
	}
}
