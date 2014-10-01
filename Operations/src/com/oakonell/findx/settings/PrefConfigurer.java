package com.oakonell.findx.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.oakonell.findx.R;
import com.oakonell.findx.model.Level;
import com.oakonell.utils.preference.PrefsActivity.PrefActivityPrefFinder;
import com.oakonell.utils.preference.PrefsActivity.PreferenceConfigurer;

public class PrefConfigurer implements PreferenceConfigurer {

	private Activity activity;
	private PrefActivityPrefFinder prefFinder;

	public PrefConfigurer(Activity activity, PrefActivityPrefFinder prefFinder) {
		this.activity = activity;
		this.prefFinder = prefFinder;
	}

	@Override
	public void configure() {
		// TODO Auto-generated method stub
		Preference resetPref = prefFinder.findPreference(activity
				.getString(R.string.pref_reset_level_progress_key));
		resetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				resetLevels();
				return true;
			}
		});

	}

	protected void resetLevels() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					Level.resetLevelProgress();
					activity.finish();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					break;
				default:
					throw new RuntimeException("Unexpected button was clicked");
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		String message = activity.getResources().getString(
				R.string.pref_confirm_reset_levels);
		builder.setMessage(message)
				.setPositiveButton(android.R.string.yes, dialogClickListener)
				.setNegativeButton(android.R.string.no, dialogClickListener)
				.show();

	}
}
