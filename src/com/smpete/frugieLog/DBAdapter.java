package com.smpete.frugieLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBAdapter {
    int id = 0;
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_FRUIT = "fruitTenths";
    public static final String COLUMN_VEGGIE = "veggieTenths";
    private static final String TAG = "DBAdapter";

    private static final String DATABASE_NAME = "intake";
    private static final String TABLE_NAME = "fruitAndVeggie";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE =
        "create table " + TABLE_NAME + " (" +
        "	_id integer primary key autoincrement," +
        "	date text unique," +
        "	fruitTenths integer default 0," +
        "	veggieTenths integer default 0" +
        ");";

    private final Context context;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx){
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper{
        
    	DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(DATABASE_CREATE);
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
    
    //---opens the database---
    public DBAdapter open() throws SQLException{
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---
    public void close(){
        DBHelper.close();
    }
    
    public long insertDate(String date){
    	ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        return db.insert(TABLE_NAME, null, values);
    }
    
    public short[] getStats(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
    	String args[] = {COLUMN_FRUIT, COLUMN_VEGGIE, dateFormat.format(date)};
    	String columns[] = {COLUMN_FRUIT, COLUMN_VEGGIE};
    	String where = dateFormat.format(date);
    	Cursor cursor = db.query(TABLE_NAME, columns, "date='"+where+"'", null, null, null, null);
        if(cursor.moveToFirst()) {
        	short[] array = {cursor.getShort(0),cursor.getShort(1)};
        	return array;
        } else
        	return null;
    }
    
    public long insertStats(Date date, short fruit, short veggie){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
        ContentValues values = new ContentValues();
        if(getStats(date) == null){
            values.put(COLUMN_DATE, dateFormat.format(date));
        	values.put(COLUMN_FRUIT, (short)fruit);
        	values.put(COLUMN_VEGGIE, (short)veggie);
        	return db.insert(TABLE_NAME, null, values);
        } else{
        	values.put(COLUMN_FRUIT, (short)fruit);
        	values.put(COLUMN_VEGGIE, (short)veggie);
        	return db.update(TABLE_NAME, values, "date='" + dateFormat.format(date) + "'", null);
        }
    }

}
