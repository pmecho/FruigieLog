package com.smpete.frugieLog;

import java.text.DecimalFormat;
import java.util.Date;
import com.smpete.frugieLog.Frugie.PortionSize;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

public class ServingFragment extends Fragment{
    
	private Frugie frugie;
	/** Whether a half serving is selected */
	private boolean halfServing;
	
	private Date curDate;
	private OnServingChangedListener mListener;
	private View view;

	public ServingFragment(Date date){
		curDate = date;
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
		frugie = mListener.onLoadData(curDate);
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
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
//		
//		RadioButton halfRB = (RadioButton) getActivity().findViewById(R.id.half_radio);
//		setHalfServing(halfRB.isChecked());
		setHalfServing(mListener.onCheckHalfServing());
	}

	@Override
	public void onResume(){
		super.onResume();
        Log.d("ServingFrag", "Resume");
	}
    
	@Override
	public void onPause(){
		super.onPause();
		mListener.onSaveState(this);
        Log.d("ServingFrag", "Pause");
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
    }
    
    public interface OnServingChangedListener{
    	public void onServingChanged();
    	public void onSaveState(ServingFragment fragment);
    	public Frugie onLoadData(Date date);
    	public boolean onCheckHalfServing();
    }
}
