package com.oakonell.findx.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.oakonell.findx.R;
import com.oakonell.findx.model.Level;
import com.oakonell.utils.preference.CommonPreferences;

public class FindXPreferences extends CommonPreferences {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        postCreate(AboutFindXActivity.class);

        Preference resetPref = findPreference(getString(R.string.pref_reset_level_progress_key));
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
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                        throw new RuntimeException("Unexpected button was clicked");
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = getResources().getString(R.string.pref_confirm_reset_levels);
        builder.setMessage(message).setPositiveButton(android.R.string.yes, dialogClickListener)
                .setNegativeButton(android.R.string.no, dialogClickListener).show();

    }
}
