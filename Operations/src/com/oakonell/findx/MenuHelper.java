package com.oakonell.findx;

import android.content.Intent;

import com.actionbarsherlock.view.MenuItem;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.GameHelper;
import com.oakonell.findx.settings.DevelopmentUtil.Info;
import com.oakonell.findx.settings.FindXPreferences;

public class MenuHelper {

	public static boolean onOptionsItemSelected(BaseGameActivity context,
			MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings_id: {
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
