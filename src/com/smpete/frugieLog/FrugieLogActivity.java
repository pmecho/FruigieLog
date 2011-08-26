package com.smpete.frugieLog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.smpete.frugieLog.R;
import com.smpete.frugieLog.Food.FoodType;
import com.smpete.frugieLog.Food.PortionSize;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class FrugieLogActivity extends Activity implements OnClickListener {

	private DBAdapter db = new DBAdapter(this);
	private Food currentFruit;
	private Food currentVeggie;
	private boolean emptyCurrent = true;

	private final String SAVED_DATE_KEY = "date";
	private final String SAVED_HALF_SERVING_KEY = "serving";
	
	private Date curDate;
	/** Whether a half serving is selected */
	private boolean halfServing;
	
	
	// For guestures
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Restore saved data
        if(savedInstanceState != null){
	        // Check for saved date
	        long savedDate = savedInstanceState.getLong(SAVED_DATE_KEY);
	        if(savedDate == 0L)
	        	curDate = new Date();
	        else
	        	curDate = new Date(savedDate);
	        
	        // Set saved serving size, if empty then full serving will be set
	        setHalfServing(savedInstanceState.getBoolean(SAVED_HALF_SERVING_KEY));
        }
        else{
        	curDate = new Date();
        	setHalfServing(false);
        }

        // Gesture detection
        View mainView = (View) findViewById(R.id.screen_layout);
     
        gestureDetector = new GestureDetector(new MyGestureDetector());
        mainView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        });
        
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
    	//TODO Test!!
    	outState.putBoolean(SAVED_HALF_SERVING_KEY, halfServing);
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

    private void setHalfServing(boolean newServing){
    	halfServing = newServing;
    	RadioGroup radios = (RadioGroup) findViewById(R.id.serving_radio_group);
    	if(newServing)
    		radios.check(R.id.half_radio);
    	else
    		radios.check(R.id.full_radio);
    }
    
    private void changeDate(int days){
    	// Save old data
    	db.open();
    	long result = db.insertStats(curDate, currentFruit.getServingTenths(), currentVeggie.getServingTenths());

    	// Set new data
		Calendar cal = Calendar.getInstance();
		cal.setTime(curDate);
		cal.add(Calendar.DATE, days);
		curDate = cal.getTime();
		
    	setDataFromDB(curDate);
    	setDateText();
    	
    	db.close();
    }
    
    public void changeDate(View view){
    	if(view.getId() == R.id.inc_day_button)
    		changeDate(1);
    	else
    		changeDate(-1);

    }
    
    public void changeToFullServing(View view){
    	halfServing = false;
    	// Change images
    	ImageView fruitImage = (ImageView)findViewById(R.id.fruit_image);
    	fruitImage.setImageResource(R.drawable.banana);
    	ImageView veggieImage = (ImageView)findViewById(R.id.veggie_image);
    	veggieImage.setImageResource(R.drawable.carrot);
    }
    
    public void changeToHalfServing(View view){
    	halfServing = true;
    	// Change images
    	ImageView fruitImage = (ImageView)findViewById(R.id.fruit_image);
    	fruitImage.setImageResource(R.drawable.banana_half);
    	ImageView veggieImage = (ImageView)findViewById(R.id.veggie_image);
    	veggieImage.setImageResource(R.drawable.carrot_half);
    }
    
    
    public void incrementPortion(View view){
    	ImageButton button = (ImageButton)view;
    	if(button.getId() == R.id.inc_fruit_button){
    		if(halfServing)
    			currentFruit.incServing(PortionSize.HALF);
    		else
    			currentFruit.incServing(PortionSize.FULL);
    	}
    	else{
    		if(halfServing)
    			currentVeggie.incServing(PortionSize.HALF);
    		else
    			currentVeggie.incServing(PortionSize.FULL);
    	}
    	
    	updateText();
    }
    
    public void decrementPortion(View view){
    	ImageButton button = (ImageButton)view;
    	if(button.getId() == R.id.dec_fruit_button){
    		if(halfServing)
    			currentFruit.decServing(PortionSize.HALF);
    		else
    			currentFruit.decServing(PortionSize.FULL);
    	}
    	else{
    		if(halfServing)
    			currentVeggie.decServing(PortionSize.HALF);
    		else
    			currentVeggie.decServing(PortionSize.FULL);
    	}
    	updateText();
    }
    
    private void setDateText(){
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
//        
    	SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
    	SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");

    	TextView dateText = (TextView)findViewById(R.id.date_text);
    	dateText.setText(dateFormat.format(curDate));

    	TextView dayText = (TextView)findViewById(R.id.day_text);
    	dayText.setText(dayFormat.format(curDate));
    }
    
    private void updateText()
    {
    	DecimalFormat oneDigit = new DecimalFormat("#,##0.0");

    	TextView fruitText = (TextView)findViewById(R.id.current_fruit_text);
    	fruitText.setText("" + oneDigit.format((double)currentFruit.getServingTenths() / 10));

    	TextView veggieText = (TextView)findViewById(R.id.current_veggie_text);
    	veggieText.setText("" + oneDigit.format((double)currentVeggie.getServingTenths() / 10));
    }
    
    
    
    
    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(FrugieLogActivity.this, "Left Swipe", Toast.LENGTH_SHORT).show();
                    changeDate(1);
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(FrugieLogActivity.this, "Right Swipe", Toast.LENGTH_SHORT).show();
                    changeDate(-1);
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
        
        // It is necessary to return true from onDown for the onFling event to register
        @Override
        public boolean onDown(MotionEvent e) {
            	return true;
        }

    }




	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
    

    
}