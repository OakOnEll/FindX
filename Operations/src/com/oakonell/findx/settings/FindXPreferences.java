package com.oakonell.findx.settings;

import android.os.Bundle;

import com.oakonell.findx.BuildConfig;
import com.oakonell.findx.R;
import com.oakonell.utils.Utils;
import com.oakonell.utils.preference.CommonPreferences;
import com.oakonell.utils.preference.PrefsActivity;

public class FindXPreferences extends PrefsActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Utils.hasHoneycomb()) {
			addPreV11Resources();
		}

	}

	@Override
	protected int[] getPreV11PreferenceResources() {
		if (BuildConfig.DEBUG) {
			return new int[] { R.xml.preferences, R.xml.prefs_account,
					R.xml.prefs_develop, R.xml.prefs_about };
		}
		return new int[] { R.xml.preferences, R.xml.prefs_account,
				R.xml.prefs_about };
	}

	@Override
	protected PreferenceConfigurer getPreV11PreferenceConfigurer() {
		if (BuildConfig.DEBUG) {
			return configureMultiple(new ResetPreferenceConfigurer(this,
					getPrefFinder()), new AccountPrefConfigurer(this,
					getPrefFinder()), new DevelopPrefConfigurer(this,
					getPrefFinder()), new CommonPreferences(this,
					getPrefFinder(), AboutFindXActivity.class));
		}
		return configureMultiple(new ResetPreferenceConfigurer(this,
				getPrefFinder()), new AccountPrefConfigurer(this,
				getPrefFinder()), new CommonPreferences(this, getPrefFinder(),
				AboutFindXActivity.class));
	}
}
