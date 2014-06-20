package com.vulcan.flightlogger.util;

import com.vulcan.flightlogger.geo.GPSUtils;
import android.content.SharedPreferences;
import android.util.Log;

public class PreferenceUtils {

	private static final String TAG = "PreferenceUtils";

	public static int getSharedPrefInteger(SharedPreferences sharedPref, String key, int defaultValue) {
		int intValue = defaultValue;
		try {
			String str = sharedPref.getString(key, Integer.toString(defaultValue));
			intValue = Integer.valueOf(str);
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error parsing int for \"" + key + "\" (" + e.getLocalizedMessage() + ")");
		}
		return intValue;
	}
	
	public static GPSUtils.TransectParsingMethod getSharedPrefTransectParsingMethod(SharedPreferences sharedPref, String key, GPSUtils.TransectParsingMethod defaultValue) {
		GPSUtils.TransectParsingMethod value = defaultValue;
		try {
			value = GPSUtils.getTransectParsingMethodForKey(sharedPref.getString(key, ""));
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error parsing tpm pref for \"" + key + "\" (" + e.getLocalizedMessage() + ")");
		}
		return value;
	}
	
	
}
