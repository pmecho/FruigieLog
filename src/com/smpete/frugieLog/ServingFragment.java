package com.smpete.frugieLog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.smpete.frugieLog.Frugie.FrugieColumns;
import com.smpete.frugieLog.Frugie.FrugieType;
import com.smpete.frugieLog.Frugie.PortionSize;
import com.smpete.frugieLog.MainControlFragment.OnMainControlChangedListener;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class ServingFragment extends Fragment{
	
	private Frugie currentFruit;
	private Frugie currentVeggie;
	/** Whether a half serving is selected */
	private boolean halfServing;
	
	private Date curDate;
	
	public ServingFragment(Date date){
		curDate = date;
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
//        // Ensure that the interface is implemented
//        try {
//            mListener = (OnMainControlChangedListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString() + " must implement OnMainControlChangedListener");
//        }
    }
    
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
        currentFruit = new Frugie(FrugieType.FRUIT);
        currentVeggie = new Frugie(FrugieType.VEGGIE);
        
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		// Inflate the layout for this fragment
		super.onCreateView(inflater, container, savedInstanceState);
		
        return inflater.inflate(R.layout.serving_fragment_layout, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		RadioButton fullRB = (RadioButton) getActivity().findViewById(R.id.full_radio);
		if(fullRB.isChecked()){
			halfServing = false;
		}else{
			halfServing = true;
		}
		
		
		
		// HACKINESS :(
		FrugieLogActivity act = (FrugieLogActivity)getActivity();
    	SimpleDateFormat dateFormat = new SimpleDateFormat(FrugieColumns.DATE_FORMAT);
    	String formattedDate = dateFormat.format(curDate);
    	
    	Cursor cursor = act.managedQuery(FrugieColumns.CONTENT_URI, 
    									null, 
    									FrugieColumns.DATE +"='" + formattedDate + "'", 
    									null, 
    									null);
    	if(cursor.moveToFirst()){
    		int idColumn = cursor.getColumnIndex(FrugieColumns._ID);
    		int fruitColumn = cursor.getColumnIndex(FrugieColumns.FRUIT);
    		int veggieColumn = cursor.getColumnIndex(FrugieColumns.VEGGIE);
    		currentFruit.setServingTenths(cursor.getShort(fruitColumn));
    		currentVeggie.setServingTenths(cursor.getShort(veggieColumn));
    	}
    	else{ // Need to insert new entry!
    		ContentValues values = new ContentValues();
    		
    		// Set defaults
    		values.put(FrugieColumns.DATE, formattedDate);
    		values.put(FrugieColumns.FRUIT, 0);
    		values.put(FrugieColumns.VEGGIE, 0);
    		
    		
    		Uri uri = act.getContentResolver().insert(FrugieColumns.CONTENT_URI, values);
    		currentFruit.setServingTenths((short) 0);
    		currentVeggie.setServingTenths((short) 0);
    	}
		updateStatsText();
		
	}

    
	@Override
	public void onPause(){
		super.onPause();
	}
	
    @Override
    public void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    }
    
    /**
     * Update serving size and set correct fruit and vegetable images
     * @param newServing Value of new serving size
     */
    public void setHalfServing(boolean newServing){
    	halfServing = newServing;
    	ImageView fruitImage = (ImageView) getActivity().findViewById(R.id.fruit_image);
    	ImageView veggieImage = (ImageView) getActivity().findViewById(R.id.veggie_image);
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

    	TextView fruitText = (TextView) getActivity().findViewById(R.id.current_fruit_text);
    	fruitText.setText("" + oneDigit.format((double)currentFruit.getServingTenths() / 10));

    	TextView veggieText = (TextView) getActivity().findViewById(R.id.current_veggie_text);
    	veggieText.setText("" + oneDigit.format((double)currentVeggie.getServingTenths() / 10));
    }
    
    
    public void setFruitTenths(short tenths){
    	currentFruit.setServingTenths(tenths);
    }
    
    public void setVeggieTenths(short tenths){
    	currentVeggie.setServingTenths(tenths);
    }
    
    public short getFruitTenths(){
    	return currentFruit.getServingTenths();
    }
    
    public short getVeggieTenths(){
    	return currentVeggie.getServingTenths();
    }
    

    public void modifyFruit(boolean increment){
    	if(increment){
	    	if(halfServing)
				currentFruit.incServing(PortionSize.HALF);
			else
				currentFruit.incServing(PortionSize.FULL);
    	}else{
    		if(halfServing)
    			currentFruit.decServing(PortionSize.HALF);
    		else
    			currentFruit.decServing(PortionSize.FULL);
    	}
    	updateStatsText();
    }
    
    public void modifyVeggie(boolean increment){
    	if(increment){
	    	if(halfServing)
	    		currentVeggie.incServing(PortionSize.HALF);
			else
				currentVeggie.incServing(PortionSize.FULL);
    	}else{
    		if(halfServing)
    			currentVeggie.decServing(PortionSize.HALF);
    		else
    			currentVeggie.decServing(PortionSize.FULL);
    	}
    	updateStatsText();
    }
}
