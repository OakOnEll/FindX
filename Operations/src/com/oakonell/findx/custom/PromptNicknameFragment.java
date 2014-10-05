package com.oakonell.findx.custom;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.oakonell.findx.R;

public class PromptNicknameFragment extends DialogFragment {
	private Runnable continuation;

	public void initialize(Runnable continuation) {
		this.continuation = continuation;
	}

	@Override
	public final View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		// getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		// getDialog().getWindow().setBackgroundDrawable(
		// new ColorDrawable(Color.TRANSPARENT));
		final View view = inflater.inflate(R.layout.dialog_prompt_nickname,
				container, false);
		getDialog().setCancelable(false);

		// setTitle(view, getString(R.string.nickname));

		final EditText nickname = (EditText) view.findViewById(R.id.nickname);

		Button button = (Button) view.findViewById(R.id.positive);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final ProgressDialog progDialog = ProgressDialog.show(getActivity(),
						"Please wait...", "");
				AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
					boolean success;

					@Override
					protected Void doInBackground(Void... params) {
						success = ParseConnectivity.modifyNickName(nickname
								.getText().toString());
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						progDialog.dismiss();
						if (success) {
							dismiss();
							if (continuation != null) {
								continuation.run();
							}
							return;
						}
						nickname.setError("Nickname not unique. Try another");
					}

				};
				task.execute();
			}
		});

		return view;
	}

	protected void setTitle(final View view, String title) {
		((TextView) view.findViewById(R.id.title)).setText(title);
		getDialog().setTitle(title);
	}

}
