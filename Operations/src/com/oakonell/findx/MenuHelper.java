package com.oakonell.findx;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.GameHelper;
import com.oakonell.findx.settings.DevelopmentUtil.Info;
import com.oakonell.findx.settings.FindXPreferences;

public class MenuHelper {
	private static final int MENU_SETTINGS_ID = 2;

	public static boolean onCreateOptionsMenu(Context context, Menu menu) {
		MenuItem settingsItem = menu.add(Menu.NONE, MENU_SETTINGS_ID,
				Menu.NONE, R.string.menu_settings);
		settingsItem.setIcon(android.R.drawable.ic_menu_preferences);

		return true;
	}

	public static boolean onOptionsItemSelected(BaseGameActivity context,
			MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTINGS_ID: {
			// create special intent
			Intent prefIntent = new Intent(context, FindXPreferences.class);

			Info info = null;
			FindXApp app = (FindXApp) context.getApplication();
			GameHelper helper = context.getGameHelper();
			if (helper.isSignedIn()) {
				info = new Info(helper);
			}
			app.setDevelopInfo(info);

			context.startActivity(prefIntent);
			return true;
		}
		default:
			throw new RuntimeException("Invalid options item "
					+ item.getItemId());
		}
	}

}
