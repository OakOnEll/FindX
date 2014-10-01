package com.oakonell.findx.settings;

import android.os.Bundle;

import com.oakonell.findx.BuildConfig;
import com.oakonell.findx.R;
import com.oakonell.utils.Utils;
import com.oakonell.utils.preference.CommonPreferences;
import com.oakonell.utils.preference.PrefsActivity;

public class FindXPreferences extends PrefsActivity {

	@Override
	public void onCreate(Bundle aSavedState) {
		super.onCreate(aSavedState);
		if (Utils.hasHoneycomb()) {
			addPreV11Resources();
		}
	}

	@Override
	protected int[] getPreV11PreferenceResources() {
		if (BuildConfig.DEBUG) {
			return new int[] { R.xml.preferences };
		}
		return new int[] { R.xml.preferences };
	}

	@Override
	protected PreferenceConfigurer getPreV11PreferenceConfigurer() {
		PrefConfigurer prefConfigurer = new PrefConfigurer(this,
				getPrefFinder());
		return configureMultiple(prefConfigurer, new CommonPreferences(this,
				getPrefFinder(), AboutFindXActivity.class));
	}

	protected boolean isValidFragment(String fragmentName) {
		return true;
	}

}
