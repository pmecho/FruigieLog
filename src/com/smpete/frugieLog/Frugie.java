package com.smpete.frugieLog;



import java.util.Date;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Simple class to hold a frugie object.  Can either be fruit or vegetable.
 * Not the best design, should be re-thought out
 * 
 * @author peter
 *
 */
public class Frugie{

    public static final String AUTHORITY = "com.smpete.frugieLog.frugieprovider";

	public static enum PortionSize{
		FULL,
		HALF
	};

	// Easy way to not have to mess with floats/doubles
	private short fruitServingTenths;
	// Easy way to not have to mess with floats/doubles
	private short veggieServingTenths;
	private long id;
	


	/**
	 * Constructor
	 * 
	 * @param name Type of fruige to set
	 */
	public Frugie(){
		id = -1;
		fruitServingTenths = 0;
		veggieServingTenths = 0;
	}
	
	
	public Frugie(long id, short fruitServingTenths, short veggieServingTenths){
		this.id = id;
		this.fruitServingTenths = fruitServingTenths;
		this.veggieServingTenths = veggieServingTenths;
	}
	
	/**
	 * Increment serving based on portion size
	 * 
	 * @param portion PortionSize to increment by
	 */
	public void incServing(PortionSize portion, boolean fruit){
		switch(portion){
		case FULL:
			if(fruit)
				fruitServingTenths += 10;
			else
				veggieServingTenths += 10;
			break;
		case HALF:
			if(fruit)
				fruitServingTenths += 5;
			else
				veggieServingTenths += 5;
			break;
		default:
				
		}
		
		if(fruitServingTenths < 0)
			fruitServingTenths = 0;
		if(veggieServingTenths < 0)
			veggieServingTenths = 0;
	}
	
	/**
	 * Increment serving based on portion size
	 * 
	 * @param portion PortionSize to decrement by
	 */
	public void decServing(PortionSize portion, boolean fruit){
		switch(portion){
		case FULL:
			if(fruit)
				fruitServingTenths -= 10;
			else
				veggieServingTenths -= 10;
			break;
		case HALF:
			if(fruit)
				fruitServingTenths -= 5;
			else
				veggieServingTenths -= 5;
			break;
		default:
				
		}
		
		if(fruitServingTenths < 0)
			fruitServingTenths = 0;
		if(veggieServingTenths < 0)
			veggieServingTenths = 0;
	}
	
	/**
	 * Accessor for tenths
	 * 
	 * @return Servings in tenths
	 */
	public short getFruitServingTenths() {
		return fruitServingTenths;
	}

	/**
	 * Mutator for tenths
	 * 
	 * @param servingTenths New serving amount to set
	 */
	public void setFruitServingTenths(short servingTenths) {
		this.fruitServingTenths = servingTenths;
	}
	
	/**
	 * Accessor for tenths
	 * 
	 * @return Servings in tenths
	 */
	public short getVeggieServingTenths() {
		return veggieServingTenths;
	}

	/**
	 * Mutator for tenths
	 * 
	 * @param servingTenths New serving amount to set
	 */
	public void setVeggieTenths(short servingTenths) {
		this.veggieServingTenths = veggieServingTenths;
	}
	
	public long getId() {
		return id;
	}

	/**
	 * Represents columns of the database table
	 * 
	 * @author peter
	 *
	 */
	public static final class FrugieColumns implements BaseColumns{
		
		// Don't instantiate this class...EVAR!
		private FrugieColumns() {}
	
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/fruitAndVeggie");
        
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.smpete.frugie";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.smpete.frugie";
        
        /**
         * Column - Date of entry 
         */
        public static final String DATE = "date";
        
		/**
		 * Format of the the date column
		 */
	    public static final String DATE_FORMAT = "yyyy-MM-dd";
	    
        /**
         * Column - Fruit servings in tenths
         */
        public static final String FRUIT = "fruitTenths";
        
        /**
         * Column - Veggie servings in tenths
         */
        public static final String VEGGIE = "veggieTenths";
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "date DESC";

	}
	
}
