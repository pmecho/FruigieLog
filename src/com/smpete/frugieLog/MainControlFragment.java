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
	private Date date;
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
	        	date = new Date();
	        else
	        	date = new Date(savedDate);
	        
	        // Set saved serving size, if empty then full serving will be set
	        setHalfServing(savedInstanceState.getBoolean(SAVED_HALF_SERVING_KEY), true);
        }
        else{
        	date = new Date();
        	setHalfServing(false, true);
        }
        updateDateText();
	}

    
	@Override
	public void onPause(){
		super.onPause();
	}
	
//    @Override
//    public void onSaveInstanceState(Bundle outState){
//    	super.onSaveInstanceState(outState);
//    	// Save the current date
//    	outState.putLong(SAVED_DATE_KEY, date.getTime());
//    	//TODO Test!!
//    	outState.putBoolean(SAVED_HALF_SERVING_KEY, halfServing);
//    }
	
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
		return date;
	}
    
    public void setDate(Date date){
    	this.date = date;
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
    	dateText.setText(dateFormat.format(date));
    }
    
    public interface OnMainControlChangedListener{
    	public void onServingSizeChanged(boolean halfServing);
    }
}
