package com.smpete.frugieLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainControlFragment extends Fragment {

	private OnMainControlChangedListener mListener;
	private Date curDate;
	/** Whether a half serving is selected */
	private boolean halfServing;
	
	/** Constants for saved bundle keys */
	private final String SAVED_DATE_KEY = "id";
	private final String SAVED_HALF_SERVING_KEY = "serving";
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // Ensure that the interface is implemented
        try {
            mListener = (OnMainControlChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnMainControlChangedListener");
        }
    }
    
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		// Inflate the layout for this fragment
		super.onCreateView(inflater, container, savedInstanceState);
		
        return inflater.inflate(R.layout.main_control_fragment_layout, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
        // Restore saved data
        if(savedInstanceState != null){
	        // Check for saved date
	        long savedDate = savedInstanceState.getLong(SAVED_DATE_KEY);
	        if(savedDate == 0L)
	        	curDate = new Date();
	        else
	        	curDate = new Date(savedDate);
	        
	        // Set saved serving size, if empty then full serving will be set
	        setHalfServing(savedInstanceState.getBoolean(SAVED_HALF_SERVING_KEY), true);
        }
        else{
        	curDate = new Date();
        	setHalfServing(false, true);
        }
        updateDateText();
		mListener.onPostDateChange(curDate);
	}

    
	@Override
	public void onPause(){
		super.onPause();
	}
	
    @Override
    public void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    	// Save the current date
    	outState.putLong(SAVED_DATE_KEY, curDate.getTime());
    	//TODO Test!!
    	outState.putBoolean(SAVED_HALF_SERVING_KEY, halfServing);
    }
	
	/**
	 * Accessor method of halfServing
	 * @return true if serving size is set to half serving
	 */
	public boolean getHalfServing(){
		return halfServing;
	}

	/**
	 * Accessor method of date
	 * @return current date
	 */
	public Date getDate(){
		return curDate;
	}
	
	/**
     * Changes the current date based on the number of days to
     * increment or decrement
     * 
     * @param days Number of days to add to the current date
     */
    public void changeDate(int days){
    	// Save old data
    	mListener.onPreDateChange();

    	// Set new data
		Calendar cal = Calendar.getInstance();
		cal.setTime(curDate);
		cal.add(Calendar.DATE, days);
		curDate = cal.getTime();
		
		mListener.onPostDateChange(curDate);
		updateDateText();
    }
    
    public void setDate(Date date){
    	curDate = date;
    	updateDateText();
    }
    
    public void setHalfServing(boolean newServing, boolean updateRadios){
    	if(updateRadios){
        	RadioGroup radios = (RadioGroup) getActivity().findViewById(R.id.serving_radio_group);
        	if(newServing)
        		radios.check(R.id.half_radio);
        	else
        		radios.check(R.id.full_radio);
    	}
    	halfServing = newServing;
    	
    	//TODO
    	mListener.onServingSizeChanged(halfServing);
    }
    
    /**
     * Updates the date text cased on the current date
     */
    private void updateDateText(){
    	SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy");

    	TextView dateText = (TextView) getActivity().findViewById(R.id.date_text);
    	dateText.setText(dateFormat.format(curDate));
    }
    
    
    public interface OnMainControlChangedListener{
    	public void onPreDateChange();
    	public void onPostDateChange(Date date);
    	public void onServingSizeChanged(boolean halfServing);
    }
}
