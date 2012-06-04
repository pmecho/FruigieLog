package com.smpete.frugieLog;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.smpete.frugieLog.Frugie.PortionSize;

public class ServingFragment extends Fragment{
    
	private Frugie frugie;
	/** Whether a half serving is selected */
	private boolean halfServing;
	
	private Date curDate;
	private OnServingChangedListener mListener;
	private View view;
	private int mOffsetFromCurrentDate;
	
	
	public static ServingFragment newInstance(long date, boolean halfServing){
		ServingFragment frag = new ServingFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putLong("date", date);
        args.putBoolean("halfServing", halfServing);
        frag.setArguments(args);
		return frag;
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // Ensure that the interface is implemented
        try {
            mListener = (OnServingChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnServingChangedListener");
        }
    }
    
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

        Log.d("ServingFrag", "Create");
        curDate = getArguments() != null ? (new Date(getArguments().getLong("date"))) : (new Date());
        halfServing = getArguments() != null ? (getArguments().getBoolean("halfServing")) : false;
		frugie = mListener.onLoadData(curDate);
		
		Calendar currentCal = Calendar.getInstance();
		Calendar cal = (Calendar) currentCal.clone();
		Calendar temp = Calendar.getInstance();
		temp.setTime(curDate);
		cal.set(Calendar.YEAR, temp.get(Calendar.YEAR));
		cal.set(Calendar.MONTH, temp.get(Calendar.MONTH));
		cal.set(Calendar.DAY_OF_MONTH, temp.get(Calendar.DAY_OF_MONTH));
		
		int daysBetween = 0;
		while (cal.before(currentCal)) {
			cal.add(Calendar.DAY_OF_MONTH, 1);
			daysBetween++;
		}
		mOffsetFromCurrentDate = daysBetween;
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		// Inflate the layout for this fragment
		super.onCreateView(inflater, container, savedInstanceState);
		
		view = inflater.inflate(R.layout.serving_fragment_layout, container, false);

    	DecimalFormat oneDigit = new DecimalFormat("#,##0.0");

    	TextView fruitText = (TextView) view.findViewById(R.id.current_fruit_text);
    	fruitText.setText("" + oneDigit.format((double)frugie.getFruitServingTenths() / 10));

    	TextView veggieText = (TextView) view.findViewById(R.id.current_veggie_text);
    	veggieText.setText("" + oneDigit.format((double)frugie.getVeggieServingTenths() / 10));
    	
    	ImageButton decFruit = (ImageButton) view.findViewById(R.id.dec_fruit_button);
    	decFruit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				modifyFruit(false);
			}
		});
    	
    	ImageButton incFruit = (ImageButton) view.findViewById(R.id.inc_fruit_button);
    	incFruit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				modifyFruit(true);
			}
		});
    	
    	ImageButton decVeggie = (ImageButton) view.findViewById(R.id.dec_veggie_button);
    	decVeggie.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				modifyVeggie(false);
			}
		});
    	
    	ImageButton incVeggie = (ImageButton) view.findViewById(R.id.inc_veggie_button);
    	incVeggie.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				modifyVeggie(true);
			}
		});
    	
    	setHalfServing(halfServing);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume(){
		super.onResume();

		setHalfServing(mListener.onCheckHalfServing());
        Log.d("ServingFrag", "Resume");
	}
    
	@Override
	public void onPause(){
		super.onPause();
		mListener.onSaveState(this);
        Log.d("ServingFrag", "Pause");
	}
	
	@Override
	public void onStop(){
		super.onStop();
        Log.d("ServingFrag", "Stop");
	}
	
    @Override
    public void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    }
    
    public Date getDate(){
    	return curDate;
    }
    
    /**
     * Update serving size and set correct fruit and vegetable images
     * @param newServing Value of new serving size
     */
    public void setHalfServing(boolean newServing){
    	halfServing = newServing;
    	if (view != null) {
	    	ImageView fruitImage = (ImageView) view.findViewById(R.id.fruit_image);
	    	ImageView veggieImage = (ImageView) view.findViewById(R.id.veggie_image);
	    	if(halfServing){
	        	fruitImage.setImageResource(R.drawable.banana_half);
	        	veggieImage.setImageResource(R.drawable.carrot_half);
	    	}
	    	else{
	        	fruitImage.setImageResource(R.drawable.banana);
	        	veggieImage.setImageResource(R.drawable.carrot);
	    	}
    	}
    }
    
    /**
     * Updates the fruit and veggie portion text based on the current 
     * fruit and veggie objects
     */
    public void updateStatsText()
    {
    	DecimalFormat oneDigit = new DecimalFormat("#,##0.0");

    	TextView fruitText = (TextView) view.findViewById(R.id.current_fruit_text);
    	fruitText.setText("" + oneDigit.format((double)frugie.getFruitServingTenths() / 10));

    	TextView veggieText = (TextView) view.findViewById(R.id.current_veggie_text);
    	veggieText.setText("" + oneDigit.format((double)frugie.getVeggieServingTenths() / 10));
    }

    public short getFruitTenths(){
    	return frugie.getFruitServingTenths();
    }
    
    public short getVeggieTenths(){
    	return frugie.getVeggieServingTenths();
    }
    
    public long getFruigieId(){
    	return frugie.getId();
    }
    

    public void modifyFruit(boolean increment){
    	if(increment){
	    	if(halfServing)
	    		frugie.incServing(PortionSize.HALF, true);
			else
				frugie.incServing(PortionSize.FULL, true);
    	}else{
    		if(halfServing)
    			frugie.decServing(PortionSize.HALF, true);
    		else
    			frugie.decServing(PortionSize.FULL, true);
    	}
    	updateStatsText();
    	mListener.onFruitChanged(frugie.getFruitServingTenths()/10, mOffsetFromCurrentDate);
    }
    
    public void modifyVeggie(boolean increment){
    	if(increment){
	    	if(halfServing)
	    		frugie.incServing(PortionSize.HALF, false);
			else
				frugie.incServing(PortionSize.FULL, false);
    	}else{
    		if(halfServing)
    			frugie.decServing(PortionSize.HALF, false);
    		else
    			frugie.decServing(PortionSize.FULL, false);
    	}
    	updateStatsText();
    	mListener.onVeggieChanged(frugie.getVeggieServingTenths()/10, mOffsetFromCurrentDate);
    }
    
    public interface OnServingChangedListener{
    	public void onVeggieChanged(int newServingValue, int dayOffset);
    	public void onFruitChanged(int newServingValue, int dayOffset);
    	public void onSaveState(ServingFragment fragment);
    	public Frugie onLoadData(Date date);
    	public boolean onCheckHalfServing();
    }
}
