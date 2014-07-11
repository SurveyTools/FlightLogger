package com.vulcan.flightlogger.util;

import com.vulcan.flightlogger.geo.GPSUtils;
import com.vulcan.flightlogger.geo.GPSUtils.*;

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
	
	public static TransectParsingMethod getSharedPrefTransectParsingMethod(SharedPreferences sharedPref, String key, TransectParsingMethod defaultValue) {
		TransectParsingMethod value = defaultValue;
		try {
			value = GPSUtils.getTransectParsingMethodForKey(sharedPref.getString(key, ""));
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error parsing tpm pref for \"" + key + "\" (" + e.getLocalizedMessage() + ")");
		}
		return value;
	}
	
	public static DistanceUnit getSharedPrefDistanceUnits(SharedPreferences sharedPref, String key, DistanceUnit defaultValue) {
		DistanceUnit value = defaultValue;
		try {
			value = GPSUtils.getDistanceUnitForKey(sharedPref.getString(key, ""));
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error parsing distance unit pref for \"" + key + "\" (" + e.getLocalizedMessage() + ")");
		}
		return value;
	}
	
	public static VelocityUnit getSharedPrefVelocityUnits(SharedPreferences sharedPref, String key, VelocityUnit defaultValue) {
		VelocityUnit value = defaultValue;
		try {
			value = GPSUtils.getVelocityUnitForKey(sharedPref.getString(key, ""));
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error parsing speed unit pref for \"" + key + "\" (" + e.getLocalizedMessage() + ")");
		}
		return value;
	}
}
