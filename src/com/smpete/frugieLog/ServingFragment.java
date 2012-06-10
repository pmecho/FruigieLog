package com.smpete.frugieLog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.smpete.frugieLog.Frugie.FrugieColumns;
import com.smpete.frugieLog.Frugie.PortionSize;

public class ServingFragment extends Fragment implements LoaderCallbacks<Cursor>{

	private static final DecimalFormat SERVING_FORMAT = new DecimalFormat("#,##0.0");
	private static final SimpleDateFormat FRUGIE_DATE_FORMAT = new SimpleDateFormat(FrugieColumns.DATE_FORMAT);
	
	private Frugie mFrugie;
	/** Whether a half serving is selected */
	private boolean mHalfServing;
	
	private Date mCurDate;
	private OnServingChangedListener mListener;
	private int mOffsetFromCurrentDate;
	
	private TextView mFruitText;
	private TextView mVeggieText;
	private ImageView mFruitImage;
	private ImageView mVeggieImage;
	
	private int mDateLoaderId;
	private int mFruitDelta;
	private int mVeggieDelta;
	
	
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
        mCurDate = new Date(getArguments().getLong("date"));
        mHalfServing = getArguments().getBoolean("halfServing");
		
		Calendar currentCal = Calendar.getInstance();
		Calendar cal = (Calendar) currentCal.clone();
		Calendar temp = Calendar.getInstance();
		temp.setTime(mCurDate);
		cal.set(Calendar.YEAR, temp.get(Calendar.YEAR));
		cal.set(Calendar.MONTH, temp.get(Calendar.MONTH));
		cal.set(Calendar.DAY_OF_MONTH, temp.get(Calendar.DAY_OF_MONTH));
		mDateLoaderId = temp.get(Calendar.YEAR) * 1000 + temp.get(Calendar.DAY_OF_YEAR);
		
		if (cal.after(currentCal)) {
			mOffsetFromCurrentDate = -1;
		} else {
			int daysBetween = 0;
			while (cal.before(currentCal)) {
				cal.add(Calendar.DAY_OF_MONTH, 1);
				daysBetween++;
			}
			mOffsetFromCurrentDate = daysBetween;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.serving_fragment_layout, container, false);

    	mFruitText = (TextView) v.findViewById(R.id.current_fruit_text);
    	mVeggieText = (TextView) v.findViewById(R.id.current_veggie_text);
      	mFruitImage = (ImageView) v.findViewById(R.id.fruit_image);
      	mVeggieImage = (ImageView) v.findViewById(R.id.veggie_image);
          	
    	// Set button callbacks
    	ImageButton decFruit = (ImageButton) v.findViewById(R.id.dec_fruit_button);
    	decFruit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				modifyFruit(false);
			}
		});
    	
    	ImageButton incFruit = (ImageButton) v.findViewById(R.id.inc_fruit_button);
    	incFruit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				modifyFruit(true);
			}
		});
    	
    	ImageButton decVeggie = (ImageButton) v.findViewById(R.id.dec_veggie_button);
    	decVeggie.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				modifyVeggie(false);
			}
		});
    	
    	ImageButton incVeggie = (ImageButton) v.findViewById(R.id.inc_veggie_button);
    	incVeggie.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				modifyVeggie(true);
			}
		});
    	
    	setHalfServing(mHalfServing);
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(mDateLoaderId, null, this);
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
		
		//TODO run in an AsyncTask?
		// Save Frugie
		if (mFruitDelta != 0 || mVeggieDelta != 0) {
			Uri uri = ContentUris.withAppendedId(FrugieColumns.CONTENT_URI, mFrugie.getId());
			ContentValues values = new ContentValues();
			values.put(FrugieColumns.FRUIT, mFrugie.getFruitServingTenths());
			values.put(FrugieColumns.VEGGIE, mFrugie.getVeggieServingTenths());
	    	
	    	int i = getActivity().getContentResolver().update(uri, values, null, null);
			mFruitDelta = 0;
			mVeggieDelta = 0;
	        Log.d("ServingFrag", "Pause, updated: " + i);
		}
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
    
    private void setFrugie(Frugie frugie) {
    	mFrugie = frugie;
    	updateStatsText();
    }
    
    public Date getDate(){
    	return mCurDate;
    }
    
    /**
     * Update serving size and set correct fruit and vegetable images
     * @param newServing Value of new serving size
     */
    public void setHalfServing(boolean newServing){
    	mHalfServing = newServing;
    	if (isResumed()) { //TODO is this the right check???
	    	if(mHalfServing){
	        	mFruitImage.setImageResource(R.drawable.banana_half);
	        	mVeggieImage.setImageResource(R.drawable.carrot_half);
	    	}
	    	else{
	        	mFruitImage.setImageResource(R.drawable.banana);
	        	mVeggieImage.setImageResource(R.drawable.carrot);
	    	}
    	}
    }
    
    /**
     * Updates the fruit and veggie portion text based on the current 
     * fruit and veggie objects
     */
    public void updateStatsText()
    {
    	mFruitText.setText("" + SERVING_FORMAT.format((double)mFrugie.getFruitServingTenths() / 10));
    	mVeggieText.setText("" + SERVING_FORMAT.format((double)mFrugie.getVeggieServingTenths() / 10));
    }

    public long getFruigieId(){
    	return mFrugie.getId();
    }
    

    public void modifyFruit(boolean increment){
    	if(increment){
    		mFrugie.incServing(mHalfServing ? PortionSize.HALF : PortionSize.FULL, true);
    		mFruitDelta += mHalfServing ? 1 : 2;
    	}else{
    		mFrugie.decServing(mHalfServing ? PortionSize.HALF : PortionSize.FULL, true);
    		mFruitDelta -= mHalfServing ? 1 : 2;
    	}
    	
    	updateStatsText();
    	mListener.onFruitChanged((double)mFrugie.getFruitServingTenths()/10, mOffsetFromCurrentDate);
    }
    
    public void modifyVeggie(boolean increment){
    	if(increment){
	    	mFrugie.incServing(mHalfServing ? PortionSize.HALF : PortionSize.FULL, false);
	    	mVeggieDelta += mHalfServing ? 1 : 2;
    	}else{
    		mFrugie.decServing(mHalfServing ? PortionSize.HALF : PortionSize.FULL, false);
    		mVeggieDelta -= mHalfServing ? 1 : 2;
    	}
    	
    	updateStatsText();
    	mListener.onVeggieChanged((double)mFrugie.getVeggieServingTenths()/10, mOffsetFromCurrentDate);
    }
    
    public interface OnServingChangedListener{
    	public void onVeggieChanged(double newServingValue, int dayOffset);
    	public void onFruitChanged(double newServingValue, int dayOffset);
    	public boolean onCheckHalfServing();
    }

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.wtf("AHHH", "Creating loader for " + FRUGIE_DATE_FORMAT.format(mCurDate));
		String formattedDate =  FRUGIE_DATE_FORMAT.format(mCurDate);
		return new CursorLoader(getActivity(), 
				FrugieColumns.CONTENT_URI, 
//				null,
				new String[] {FrugieColumns._ID, FrugieColumns.FRUIT, FrugieColumns.VEGGIE}, 
				FrugieColumns.DATE + " = ?", 
				new String[] {formattedDate}, 
				null);
		
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.wtf("AHHH", "Load finished for " + FRUGIE_DATE_FORMAT.format(mCurDate));
		Frugie frugie;
		if (data.moveToFirst()) {
			int idColumn = data.getColumnIndex(FrugieColumns._ID);
    		int fruitColumn = data.getColumnIndex(FrugieColumns.FRUIT);
    		int veggieColumn = data.getColumnIndex(FrugieColumns.VEGGIE);
    		frugie = new Frugie(data.getLong(idColumn), 
    				data.getShort(fruitColumn), 
    				data.getShort(veggieColumn));
		} else {
			
			//TODO Run in an async?
			ContentValues values = new ContentValues();
    		
    		// Set defaults
    		values.put(FrugieColumns.DATE, FRUGIE_DATE_FORMAT.format(mCurDate));
    		values.put(FrugieColumns.FRUIT, 0);
    		values.put(FrugieColumns.VEGGIE, 0);

    		Uri uri = getActivity().getContentResolver().insert(FrugieColumns.CONTENT_URI, values);
    		frugie = new Frugie(ContentUris.parseId(uri), (short)0, (short)0);
		}
		
		setFrugie(frugie);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
	}
}
