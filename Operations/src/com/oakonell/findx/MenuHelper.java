package com.oakonell.findx;

import android.content.Context;
import android.content.Intent;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.oakonell.findx.settings.FindXPreferences;

public class MenuHelper {
	private static final int MENU_SETTINGS_ID = 2;

	public static boolean onCreateOptionsMenu(Context context, Menu menu) {
		MenuItem settingsItem = menu.add(Menu.NONE, MENU_SETTINGS_ID,
				Menu.NONE, R.string.menu_settings);
		settingsItem.setIcon(android.R.drawable.ic_menu_preferences);

		return true;
	}

	public static boolean onOptionsItemSelected(Context context,
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTINGS_ID: {
			Intent intent = new Intent(context, FindXPreferences.class);
			context.startActivity(intent);
			return true;
		}
		default:
			throw new RuntimeException("Invalid options item "
					+ item.getItemId());
		}
	}

}
