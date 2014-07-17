package com.vulcan.flightlogger.util;

import com.vulcan.flightlogger.R;
import com.vulcan.flightlogger.geo.GPSUtils;
import com.vulcan.flightlogger.geo.GPSUtils.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferenceUtils {

	private static final String TAG = "PreferenceUtils";

	public static int getSharedPrefStringAsInteger(SharedPreferences sharedPref, String key, int defaultValue) {
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
	
	public static float getSharedPrefStringAsFloat(SharedPreferences sharedPref, String key, float defaultValue) {
		float floatValue = defaultValue;
		try {
			String str = sharedPref.getString(key, Float.toString(defaultValue));
			floatValue = Float.valueOf(str);
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error parsing float for \"" + key + "\" (" + e.getLocalizedMessage() + ")");
		}
		return floatValue;
	}
	
	public static void setSharedPrefInteger(SharedPreferences.Editor editor, String key, int value) {
		try {
			editor.putString(key, String.valueOf(value));
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error setting int for \"" + key + "\" (" + e.getLocalizedMessage() + ")");
		}
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
	
	public static boolean setSharedPrefDistanceUnits(SharedPreferences sharedPrefs, String key, DistanceUnit units) {
		boolean worked = false;
		try {
			String distanceKey = GPSUtils.getKeyForDistanceUnit(units);

			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putString(key, distanceKey);
			editor.commit();
			worked = true;
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error parsing distance unit pref for \"" + key + "\" (" + e.getLocalizedMessage() + ")");
		}
		return worked;
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

	public static void resetSharedPrefsToDefaults(Context context) {

		// wacky
		// note: stored in /data/user/0/com.vulcan.flightlogger/shared_prefs/com.vulcan.flightlogger_preferences.xml

		try {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.clear();
			editor.commit();
			PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error resettings shared prefs to defaults (" + e.getLocalizedMessage() + ")");
		}
	}

}
