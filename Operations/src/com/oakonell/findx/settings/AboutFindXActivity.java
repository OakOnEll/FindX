package com.oakonell.findx.settings;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.oakonell.findx.R;
import com.oakonell.utils.Utils;

public class AboutFindXActivity extends SherlockFragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		final ActionBar ab = getSupportActionBar();
		// set defaults for logo
		ab.setDisplayUseLogoEnabled(true);
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setTitle(R.string.about_title);

		TextView versionText = (TextView) findViewById(R.id.version);
		versionText.setText(Utils.getVersion(this));

		WebView aboutText = (WebView) findViewById(R.id.about_description);
		aboutText.setBackgroundColor(0xFFFF);
		// aboutText.setMovementMethod(LinkMovementMethod.getInstance());
		// String text = getString(R.string.aboutDescription);
		aboutText.loadUrl("file:///android_asset/credits.html");
		// aboutText.setText(Html.fromHtml(text));
	}
}
