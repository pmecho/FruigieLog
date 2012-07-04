package com.smpete.frugieLog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AboutDialog extends DialogFragment {
	
	private static final String HIDE_RATE_KEY = "hideRate";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = LayoutInflater.from(getActivity()).inflate(R.layout.about, null);
		((TextView) v.findViewById(R.id.getting_started)).setText(Html.fromHtml(getString(R.string.about_getting_started)));
		((TextView) v.findViewById(R.id.cup_examples)).setText(Html.fromHtml(getString(R.string.about_examples_of_a_cup)));
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
			.setTitle(R.string.about)
			.setView(v)
			.setNegativeButton("Okay", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dismiss();
				}
			});
		
		if (getArguments() != null && getArguments().getBoolean(HIDE_RATE_KEY)) {
			return builder.create();
		} else {
			return builder.setPositiveButton("Rate this app!", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO market link
				}
			}).create();
		}
		
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (getActivity() != null) {
			UserPrefs.setShownWelcome(getActivity());
		}
	}

	public void hideRateButton() {
		Bundle bundle = new Bundle();
		bundle.putBoolean(HIDE_RATE_KEY, true);
		setArguments(bundle);
	}
}
