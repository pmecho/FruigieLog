package com.smpete.frugieLog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutDialog extends DialogFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.about, null);
		getDialog().setTitle(R.string.about);
		
		((TextView) v.findViewById(R.id.getting_started)).setText(Html.fromHtml(getString(R.string.about_getting_started)));
		((TextView) v.findViewById(R.id.cup_examples)).setText(Html.fromHtml(getString(R.string.about_examples_of_a_cup)));
		
		return v;
	}
}
