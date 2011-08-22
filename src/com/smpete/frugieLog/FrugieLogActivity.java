package com.smpete.frugieLog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.smpete.frugieLog.R;
import com.smpete.frugieLog.Food.FoodType;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class FrugieLogActivity extends Activity {

	private DBAdapter db = new DBAdapter(this);
	private Food currentFruit;
	private Food currentVeggie;
	private boolean emptyCurrent = true;
	
	private final String SAVED_DATE_KEY = "date";
	
	private Date curDate;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if(savedInstanceState != null){
	        // Check for saved date
	        long savedDate = savedInstanceState.getLong(SAVED_DATE_KEY);
	        if(savedDate != 0L)
	        	curDate = new Date(savedDate);
	        else
	        	curDate = new Date();
        }
        else
        	curDate = new Date();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.

        setDateText();        
        
        currentFruit = new Food(FoodType.FRUIT);
        currentVeggie = new Food(FoodType.VEGGIE);
        setDataFromDB(curDate);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }
    @Override
    public void onPause(){
    	super.onPause();
    	db.open();
    	long result = db.insertStats(curDate, currentFruit.getServingTenths(), currentVeggie.getServingTenths());
    	db.close();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    	// Save the current date
    	outState.putLong(SAVED_DATE_KEY, curDate.getTime());
    }
    
    private void setDataFromDB(Date date){
        db.open();
        
        short[] stats = db.getStats(date);
        if(stats != null){
        	emptyCurrent = false;
        	currentFruit.setServingTenths(stats[0]);
        	currentVeggie.setServingTenths(stats[1]);
        } else{
	        emptyCurrent = true;
	    	currentFruit.setServingTenths((short) 0);
	    	currentVeggie.setServingTenths((short) 0);
        }
    	
        db.close();
        updateText();
    }

    
    
    public void changeDate(View view){

    	// Save old data
    	db.open();
    	long result = db.insertStats(curDate, currentFruit.getServingTenths(), currentVeggie.getServingTenths());

    	// Set new data
    	if(view.getId() == R.id.incDay){
    		Calendar cal = Calendar.getInstance();
    		cal.setTime(curDate);
    		cal.add(Calendar.DATE, 1);
    		curDate = cal.getTime();
    	} else{
    		Calendar cal = Calendar.getInstance();
    		cal.setTime(curDate);
    		cal.add(Calendar.DATE, -1);
    		curDate = cal.getTime();
    	}
    
    	setDataFromDB(curDate);
    	setDateText();
    	
    	db.close();

    }
    
    public void changePortion(View view){
    	ImageButton button = (ImageButton)view;
    	if(button.getId() == R.id.fruitPortion){
    		currentFruit.switchPortion();
    		Log.d("ButtonClick", "Change fruit portion");
    	}
    	else
    		currentVeggie.switchPortion();
    		Log.d("ButtonClick", "Change veggie portion");
    }
    
    public void incrementPortion(View view){
    	ImageButton button = (ImageButton)view;
    	if(button.getId() == R.id.incFruit){
    		currentFruit.incServing();
    	}
    	else
    		currentVeggie.incServing();
    	
    	updateText();
    }
    
    public void decrementPortion(View view){
    	ImageButton button = (ImageButton)view;
    	if(button.getId() == R.id.decFruit){
    		currentFruit.decServing();
    	}
    	else
    		currentVeggie.decServing();
    	
    	updateText();
    }
    
    private void setDateText(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
    	TextView dateText = (TextView)findViewById(R.id.date);
    	dateText.setText(dateFormat.format(curDate));
    }
    
    private void updateText()
    {
    	DecimalFormat oneDigit = new DecimalFormat("#,##0.0");

    	TextView fruitText = (TextView)findViewById(R.id.currentFruit);
    	fruitText.setText("" + oneDigit.format((double)currentFruit.getServingTenths() / 10));

    	TextView veggieText = (TextView)findViewById(R.id.currentVeggie);
    	veggieText.setText("" + oneDigit.format((double)currentVeggie.getServingTenths() / 10));
    }
}