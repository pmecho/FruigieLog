package com.smpete.frugieLog;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPrefs {

	private static final String PREFS = "com.smpete.frugieLog.prefs";
	private static final String HISTORY_LENGTH_KEY = "historyLength";
	private static final String SHOWN_WELCOME_KEY = "shownWelcome";

	public static void setHistoryLengthId(Context context, int length) {
		getPrefs(context).edit().putInt(HISTORY_LENGTH_KEY, length).commit();
	}
	
	public static int getHistoryLengthId(Context context) {
		return getPrefs(context).getInt(HISTORY_LENGTH_KEY, FrugieLogActivity.ITEM_ID_30_DAYS);
	}
	
	public static void setShownWelcome(Context context) {
		getPrefs(context).edit().putBoolean(SHOWN_WELCOME_KEY, true).commit();
	}
	
	public static boolean hasShownWelcome(Context context) {
		return getPrefs(context).getBoolean(SHOWN_WELCOME_KEY, false);
	}
	
	private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS, 0);
    }
}
