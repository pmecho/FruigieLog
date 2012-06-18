package com.smpete.frugieLog;

import java.text.DecimalFormat;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class CalculatorDialog extends DialogFragment {

	//////////////////////////////////////////////////////////////Ages:   	 2....4....9....14...19...31...51
	private static final double[][] FEMALE_GIRLIE_MEN  	= new double[][]{	{1.0, 1.0, 1.5, 1.5, 2.0, 1.5, 1.5},
																			{1.0, 1.5, 2.0, 2.5, 2.5, 2.5, 2.0}
																		};
	private static final double[][] FEMALE_OKAY_FOLK   	=  new double[][]{	{1.0, 1.5, 1.5, 2.0, 2.0, 2.0, 1.5},
																			{1.0, 1.5, 2.0, 2.5, 2.5, 2.5, 2.5}
																		};
	private static final double[][] FEMALE_SUPER_PEEPS 	= new double[][]{	{1.0, 1.5, 1.5, 2.0, 2.0, 2.0, 2.0},
																			{1.0, 1.5, 2.5, 3.0, 3.0, 3.0, 2.5}
																		};
	private static final double[][] MALE_GIRLIE_MEN    	= new double[][]{	{1.0, 1.5, 1.5, 2.0, 2.0, 2.0, 2.0},
																			{1.0, 1.5, 2.5, 3.0, 3.0, 3.0, 2.5}
																		};
	private static final double[][] MALE_OKAY_FOLK		= new double[][]{	{1.0, 1.5, 1.5, 2.0, 2.0, 2.0, 2.0},
																			{1.0, 1.5, 2.5, 3.0, 3.5, 3.5, 3.0}
																		};	
	private static final double[][] MALE_SUPER_PEEPS 	= new double[][]{	{1.0, 1.5, 2.0, 2.5, 2.5, 2.5, 2.0},
																			{1.0, 2.0, 2.5, 3.5, 4.0, 3.5, 3.0}
																		};

	private static final DecimalFormat CUPS_FORMAT = new DecimalFormat("#,##0.0 cups");
	private static final DecimalFormat CUP_FORMAT = new DecimalFormat("#,##0.0 cup");
	private TextView mAgeText;
	private RadioButton mMaleRadio;
	private RadioButton mFemaleRadio;
	private Spinner mActivitySpinner;
	private TextView mAmountsText;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.calculator, null);
		
		mAgeText = (TextView) v.findViewById(R.id.age);
		mMaleRadio = (RadioButton) v.findViewById(R.id.male);
		mFemaleRadio = (RadioButton) v.findViewById(R.id.female);
		mActivitySpinner = (Spinner) v.findViewById(R.id.activity_spinner);
		mAmountsText = (TextView) v.findViewById(R.id.amounts);
		getDialog().setTitle(R.string.calculator);
		((Button) v.findViewById(R.id.calculate)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onCalculate();
			}
		});
		
		OnClickListener mHideResultsOnClick = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mAmountsText.setVisibility(View.GONE);			
			}
		};
		mMaleRadio.setOnClickListener(mHideResultsOnClick);
		mFemaleRadio.setOnClickListener(mHideResultsOnClick);
		mAgeText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mAmountsText.setVisibility(View.GONE);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		mActivitySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mAmountsText.setVisibility(View.GONE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		ArrayAdapter<CharSequence> spinnerArrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.activity_levels, android.R.layout.simple_spinner_dropdown_item);
		mActivitySpinner.setAdapter(spinnerArrayAdapter);
	}
	
	public void onCalculate() {
		int age;
		try {
			age = Integer.parseInt(mAgeText.getText().toString());
		} catch (NumberFormatException e) {
			mAgeText.setText("0");
			age = 0;
		}
		boolean male = mMaleRadio.isChecked();
		int activityLevel = mActivitySpinner.getSelectedItemPosition();
		
		switch (activityLevel) {
		case 0:
			setText(age, male ? MALE_GIRLIE_MEN : FEMALE_GIRLIE_MEN);
			break;
		case 1:
			setText(age, male ? MALE_OKAY_FOLK : FEMALE_OKAY_FOLK);
			break;
		case 2:
			setText(age, male ? MALE_SUPER_PEEPS : FEMALE_SUPER_PEEPS);
			break;
		}
		
	}
	
	private void setText(int age, double[][] array) {
		int index;
		if (age < 4) {
			index = 0;
		} else if (age < 9) {
			index = 1;
		} else if (age < 14) {
			index = 2;
		} else if (age < 19) {
			index = 3;
		} else if (age < 31) {
			index = 4;
		} else {
			index = 5;
		}

		double fruit = array[0][index];
		double veggie = array[1][index];
		
		String fruitText;
		String veggieText;
		if (fruit > 1.0) {
			fruitText = CUPS_FORMAT.format(fruit);
		} else {
			fruitText = CUP_FORMAT.format(fruit);
		}
		
		if (veggie > 1.0) {
			veggieText = CUPS_FORMAT.format(veggie);
		} else {
			veggieText = CUP_FORMAT.format(veggie);
		}
		
		mAmountsText.setText(Html.fromHtml(getString(R.string.fruit_veggie_amounts, fruitText, veggieText)));
		mAmountsText.setVisibility(View.VISIBLE);
		
	}
	
}
