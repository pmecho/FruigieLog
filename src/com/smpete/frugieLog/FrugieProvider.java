package com.smpete.frugieLog;

import java.util.HashMap;

import com.smpete.frugieLog.Frugie.FrugieColumns;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


public class FrugieProvider extends ContentProvider {
    int id = 0;

    private static final String TAG = "FrugieProvider";

    private static final String DATABASE_NAME = "intake";
    private static final String TABLE_NAME = "fruitAndVeggie";
    private static final int DATABASE_VERSION = 1;
    
    private static final UriMatcher uriMatcher;
    private static HashMap<String, String> projectionMap;
    
    // URI return codes
    private static final int FRUGIES_URI_CODE = 1;
    private static final int  FRUGIE_ID_URI_CODE = 2;
    private static final int  FRUGIE_DATE_URI_CODE = 3;

    private DatabaseHelper dbHelper;

    private static class DatabaseHelper extends SQLiteOpenHelper{
        
    	DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(	"create table " + TABLE_NAME + " (" +
                    FrugieColumns._ID + " integer primary key autoincrement," +
                    FrugieColumns.DATE + " text unique," +
                    FrugieColumns.FRUIT + " integer default 0," +
                    FrugieColumns.VEGGIE + " integer default 0" +
                    ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion){
            Log.w(TAG, "Upgrading database from version " + oldVersion
                  + " to "
                  + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
    	SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    	qb.setTables(TABLE_NAME);
    	
    	switch(uriMatcher.match(uri)){
    	case FRUGIES_URI_CODE:
    		qb.setProjectionMap(projectionMap);
    		break;

    	case FRUGIE_ID_URI_CODE:
    		qb.setProjectionMap(projectionMap);
    		qb.appendWhere(FrugieColumns._ID + "=" + uri.getPathSegments().get(1));
    		break;

    	case FRUGIE_DATE_URI_CODE:
    		qb.setProjectionMap(projectionMap);
    		qb.appendWhere(FrugieColumns.DATE + "=" + uri.getPathSegments().get(1));
    		break;
    		
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
    	}

        // Run the query
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    	
        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues initialValues){
        // Validate the requested uri
        if (uriMatcher.match(uri) != FRUGIES_URI_CODE) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
        	throw new IllegalArgumentException("Please set date value at a minimum");
        }
        
        // Make sure that the fields are all set
        if (values.containsKey(FrugieColumns.FRUIT) == false) {
            values.put(FrugieColumns.FRUIT, 0);
        }
        if (values.containsKey(FrugieColumns.VEGGIE) == false) {
            values.put(FrugieColumns.VEGGIE, 0);
        }
        if (values.containsKey(FrugieColumns.DATE) == false) {
        	throw new IllegalArgumentException("Date column not set");
        }
        
        // Run the insert
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri frugieUri = ContentUris.withAppendedId(FrugieColumns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(frugieUri, null);
            return frugieUri;
        }
        
        // Insert failed so throw exception
        throw new SQLException("Failed to insert row into " + uri);
    }
    
    
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs){
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
    	int count;
    	switch(uriMatcher.match(uri)){
    	case FRUGIE_ID_URI_CODE:
    		String frugieId = uri.getPathSegments().get(1);
    		count =  db.update(TABLE_NAME, values, FrugieColumns._ID + "=" + frugieId
    				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
    		break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
    	}
    	
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
    	// Deletes are unsupported at this point
    	return 0;
    }
    

    
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
        case FRUGIES_URI_CODE:
            return FrugieColumns.CONTENT_TYPE;

        case FRUGIE_ID_URI_CODE:
    	case FRUGIE_DATE_URI_CODE:
            return FrugieColumns.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    
    @Override
    public boolean onCreate(){
    	dbHelper = new DatabaseHelper(getContext());
    	return true;
    }
    
    // Initialize uri matcher and projection map
    static {
    	uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    	uriMatcher.addURI(Frugie.AUTHORITY, TABLE_NAME, FRUGIES_URI_CODE);
    	uriMatcher.addURI(Frugie.AUTHORITY, TABLE_NAME + "/#", FRUGIE_ID_URI_CODE);
    	uriMatcher.addURI(Frugie.AUTHORITY, TABLE_NAME + "/date", FRUGIE_DATE_URI_CODE);

    	projectionMap = new HashMap<String, String>();
    	projectionMap.put(FrugieColumns._ID, FrugieColumns._ID);
    	projectionMap.put(FrugieColumns.DATE, FrugieColumns.DATE);
    	projectionMap.put(FrugieColumns.FRUIT, FrugieColumns.FRUIT);
    	projectionMap.put(FrugieColumns.VEGGIE, FrugieColumns.VEGGIE);
    }
}
