package com.smpete.frugieLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.smpete.frugieLog.Frugie.FrugieColumns;
import com.smpete.frugieLog.charting.HistoryChart;

public class HistoryActivity extends SherlockFragmentActivity implements LoaderCallbacks<Cursor>{

	private static final String TAG = HistoryActivity.class.getSimpleName();
	private static final int ITEM_ID_EXPORT = 1;
	private static final String CSV_FILENAME = "FrugieData.csv";
	
	private HistoryChart mHistoryChart;

	private double[] mFruits;
	private double[] mVeggies;
	private String[] mDates;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.history);
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
		mHistoryChart = new HistoryChart(true);
        mHistoryChart.createChartView(this);
        mHistoryChart.hideTitle();
        layout.addView(mHistoryChart.getChartView());

        getSupportLoaderManager().initLoader(1, null, this);
        
        getSupportActionBar().setTitle(R.string.history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setSupportProgressBarIndeterminateVisibility(false);
        
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuItem export = menu.add(Menu.NONE, ITEM_ID_EXPORT, Menu.NONE, R.string.share_csv);
    	export.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    	return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case ITEM_ID_EXPORT:
			exportData();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void exportData() {
		final File cacheFile = new File(this.getCacheDir() + File.separator + CSV_FILENAME);
		
		new AsyncTask<Void, Void, Boolean>() {
			protected void onPreExecute() {
				setSupportProgressBarIndeterminateVisibility(true);
			}

			@Override
			protected Boolean doInBackground(Void... params) {
				
				
				PrintWriter pw = null;
			    try {
				    FileOutputStream fos = new FileOutputStream(cacheFile);
				    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
				    pw = new PrintWriter(osw);
				    
				    pw.println("Date,Fruits,Veggies,");
				    
				    for (int i = 0; i < mFruits.length; i++) {
				    	pw.println(String.format("%s,%.1f,%.1f,", mDates[i], mFruits[i], mVeggies[i]));
				    }
				 
			    } catch (IOException e) {
			    	Log.e(TAG, "Error creating .csv file", e);
			    	return false;
				} finally {
					if (pw != null) {
						pw.flush();
						pw.close();
					}
				}
				return true;
			}
			
			protected void onPostExecute(Boolean result) {
				setSupportProgressBarIndeterminateVisibility(false);
				if (result) {
				    final Intent emailIntent = new Intent(Intent.ACTION_SEND);
				    
				    emailIntent.setType("text/plain");
				 
				    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
				 
				    //Add the attachment by specifying a reference to our custom ContentProvider
					// and the specific file of interest
					emailIntent.putExtra(
							Intent.EXTRA_STREAM,
							Uri.parse("content://"
									+ CachedFileProvider.AUTHORITY + "/"
									+ CSV_FILENAME));
				    
				    startActivity(emailIntent);
				} else {
					Toast.makeText(getApplicationContext(), getString(R.string.failed_csv_creation), Toast.LENGTH_LONG).show();
				}
			}
			
		}.execute();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		
		SimpleDateFormat dateFormat = new SimpleDateFormat(FrugieColumns.DATE_FORMAT);
    	String nowDate = dateFormat.format(new Date());
    	Loader<Cursor> loader = new CursorLoader(this,
				FrugieColumns.CONTENT_URI, 
				new String[] {FrugieColumns.FRUIT, FrugieColumns.VEGGIE, FrugieColumns.DATE},
				"date <= ?",
				new String[] {nowDate},
				FrugieColumns.DATE + " DESC");
    	

    	return loader;
    }

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            int fruitColumn = data.getColumnIndex(FrugieColumns.FRUIT); 
            int veggieColumn = data.getColumnIndex(FrugieColumns.VEGGIE);
            int dateColumn = data.getColumnIndex(FrugieColumns.DATE);
            
            mFruits = new double[data.getCount()];
            mVeggies = new double[data.getCount()];
            mDates = new String[data.getCount()];
            
            do {
            	int i = data.getPosition();
                // Get the field values
                mFruits[i] = data.getDouble(fruitColumn) / 10;
                mVeggies[i] = data.getDouble(veggieColumn) / 10;
                mDates[i] = data.getString(dateColumn);
            } while (data.moveToNext());
            
            mHistoryChart.updateDataset(mFruits, mVeggies);
        }
        
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}
}
