package com.oakonell.findx.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.oakonell.findx.R;
import com.oakonell.findx.model.Level;
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
	protected PreferenceConfigurer getPreV11PreferenceConfigurer() {
		return configureMultiple(new CommonPreferences(this, getPrefFinder(),
				AboutFindXActivity.class), new ResetPreferenceConfigurer(this,
				getPrefFinder()));
	}


	@Override
	protected int[] getPreV11PreferenceResources() {
		return new int[] { R.xml.preferences };
	}
}
