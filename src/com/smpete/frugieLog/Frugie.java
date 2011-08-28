package com.smpete.frugieLog;

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
	
	public static enum FrugieType{
		FRUIT,
		VEGGIE
	};

	private FrugieType name;
	// Easy way to not have to mess with floats/doubles
	private short servingTenths;


	/**
	 * Constructor
	 * 
	 * @param name Type of fruige to set
	 */
	public Frugie(FrugieType name){
		this.name = name;
		servingTenths = 0;
	}
	
	/**
	 * Accessor for name
	 * 
	 * @return Name of the Frugie
	 */
	public FrugieType getName(){
		return name;
	}
	
	/**
	 * Increment serving based on portion size
	 * 
	 * @param portion PortionSize to increment by
	 */
	public void incServing(PortionSize portion){
		switch(portion){
		case FULL:
			servingTenths += 10;
			break;
		case HALF:
			servingTenths += 5;
			break;
		default:
				
		}
		
		if(servingTenths < 0)
			servingTenths = 0;
	}
	
	/**
	 * Increment serving based on portion size
	 * 
	 * @param portion PortionSize to decrement by
	 */
	public void decServing(PortionSize portion){
		switch(portion){
		case FULL:
			servingTenths -= 10;
			break;
		case HALF:
			servingTenths -= 5;
			break;
		default:
				
		}
		if(servingTenths < 0)
			servingTenths = 0;
	}
	
	/**
	 * Accessor for tenths
	 * 
	 * @return Servings in tenths
	 */
	public short getServingTenths() {
		return servingTenths;
	}

	/**
	 * Mutator for tenths
	 * 
	 * @param servingTenths New serving amount to set
	 */
	public void setServingTenths(short servingTenths) {
		this.servingTenths = servingTenths;
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
