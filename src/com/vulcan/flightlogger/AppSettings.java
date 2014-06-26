package com.vulcan.flightlogger;

import com.vulcan.flightlogger.geo.GPSUtils;
import com.vulcan.flightlogger.util.PreferenceUtils;
import com.vulcan.flightlogger.util.ResourceUtils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class AppSettings {

	private ContextWrapper mContextWrapper;
	
	public boolean mPrefShowDebug;
	public int mPrefAltitudeTarget; // e.g. 300
	public int mPrefAltitudeRadius; // e.g. +/- 100'
	public int mPrefNavigationRadius; // e.g. +/- 200'
	public boolean mUseCustomParsingMethod;
	public GPSUtils.TransectParsingMethod mPrefTransectParsingMethod; // e.g. TransectParsingMethod.USE_DEFAULT

	private static final String LOGGER_TAG = "AppSettings";

	public static final String PREF_SHOW_DEBUG_KEY = "PREF_SHOW_DEBUG_KEY";
	public static final String PREF_ALTITUDE_TARGET_KEY = "PREF_ALTITUDE_TARGET_KEY";
	public static final String PREF_ALTITUDE_RADIUS_KEY = "PREF_ALTITUDE_RADIUS_KEY";
	public static final String PREF_NAVIGATION_RADIUS_KEY = "PREF_NAVIGATION_RADIUS_KEY";
	public static final String PREF_USE_CUSTOM_PARSING_METHOD_KEY = "PREF_USE_CUSTOM_PARSING_METHOD_KEY";
	public static final String PREF_TRANSECT_PARSING_METHOD_KEY = "PREF_TRANSECT_PARSING_METHOD_KEY";

	public AppSettings(ContextWrapper contextWrapper) {
		mContextWrapper = contextWrapper;
		refresh(contextWrapper);
	}

	// copy constructor
	public AppSettings(AppSettings srcData) {

		if (srcData != null) {
			mPrefShowDebug = srcData.mPrefShowDebug;
			mPrefAltitudeTarget = srcData.mPrefAltitudeTarget;
			mPrefAltitudeRadius = srcData.mPrefAltitudeRadius;
			mPrefNavigationRadius = srcData.mPrefNavigationRadius;
			mUseCustomParsingMethod = srcData.mUseCustomParsingMethod;
			mPrefTransectParsingMethod = srcData.mPrefTransectParsingMethod;

			mContextWrapper = srcData.mContextWrapper;
		} else {
			reset();
		}
	}

	public void debugDump() {
		Log.d(LOGGER_TAG, "mPrefShowDebug: " + mPrefShowDebug);
		Log.d(LOGGER_TAG, "mPrefAltitudeTarget: " + mPrefAltitudeTarget);
		Log.d(LOGGER_TAG, "mPrefAltitudeRadius: " + mPrefAltitudeRadius);
		Log.d(LOGGER_TAG, "mPrefNavigationRadius: " + mPrefNavigationRadius);
		Log.d(LOGGER_TAG, "mUseCustomParsingMethod: " + mUseCustomParsingMethod);
		Log.d(LOGGER_TAG, "mPrefTransectParsingMethod: " + mPrefTransectParsingMethod);
	}

	public void refresh(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		// TODO eval context.getSharedPreferences("userdetails", Context.MODE_PRIVATE)

		mPrefShowDebug = sharedPref.getBoolean(PREF_SHOW_DEBUG_KEY, ResourceUtils.getResourceBooleanFromString(context, R.string.pref_show_debug_default_value));
		mPrefAltitudeTarget = PreferenceUtils.getSharedPrefInteger(sharedPref, PREF_ALTITUDE_TARGET_KEY, ResourceUtils.getResourceIntegerFromString(context, R.string.pref_altitude_target_default_value));
		mPrefAltitudeRadius = PreferenceUtils.getSharedPrefInteger(sharedPref, PREF_ALTITUDE_RADIUS_KEY, ResourceUtils.getResourceIntegerFromString(context, R.string.pref_altitude_radius_default_value));
		mPrefNavigationRadius = PreferenceUtils.getSharedPrefInteger(sharedPref, PREF_NAVIGATION_RADIUS_KEY, ResourceUtils.getResourceIntegerFromString(context, R.string.pref_navigation_radius_default_value));
		mUseCustomParsingMethod = sharedPref.getBoolean(PREF_USE_CUSTOM_PARSING_METHOD_KEY, ResourceUtils.getResourceBooleanFromString(context, R.string.pref_use_custom_transect_parsing_method_default_value));
		mPrefTransectParsingMethod = PreferenceUtils.getSharedPrefTransectParsingMethod(sharedPref, PREF_TRANSECT_PARSING_METHOD_KEY, ResourceUtils.getResourceTransectParsingMethod(context, R.string.pref_transect_parsing_method_default_value));
		
		// TESTING debugDump();
	}

	public void reset() {
		mPrefShowDebug = ResourceUtils.getResourceBooleanFromString(mContextWrapper, R.string.pref_show_debug_default_value);
		mPrefAltitudeTarget = ResourceUtils.getResourceIntegerFromString(mContextWrapper, R.string.pref_altitude_target_default_value);
		mPrefAltitudeRadius = ResourceUtils.getResourceIntegerFromString(mContextWrapper, R.string.pref_altitude_radius_default_value);
		mPrefNavigationRadius = ResourceUtils.getResourceIntegerFromString(mContextWrapper, R.string.pref_navigation_radius_default_value);
		mUseCustomParsingMethod = ResourceUtils.getResourceBooleanFromString(mContextWrapper, R.string.pref_use_custom_transect_parsing_method_default_value);
		mPrefTransectParsingMethod = ResourceUtils.getResourceTransectParsingMethod(mContextWrapper, R.string.pref_transect_parsing_method_default_value);
	}
	
	public static GPSUtils.TransectParsingMethod getPrefTransectParsingMethod(Context context) {
		// note: could honor mUseCustomParsingMethod
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return PreferenceUtils.getSharedPrefTransectParsingMethod(sharedPref, PREF_TRANSECT_PARSING_METHOD_KEY, ResourceUtils.getResourceTransectParsingMethod(context, R.string.pref_transect_parsing_method_default_value));
	}
	
	public static boolean isPrefUseCustomTransectParsingKey(String key) {
		return PREF_USE_CUSTOM_PARSING_METHOD_KEY.equalsIgnoreCase(key);
	}
	
	public static boolean getPrefUseCustomTransectParsing(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(PREF_USE_CUSTOM_PARSING_METHOD_KEY, ResourceUtils.getResourceBooleanFromString(context, R.string.pref_show_debug_default_value));
	}
	
	public static boolean resetCustomTransectParsingMethodToDefault(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		GPSUtils.TransectParsingMethod oldMethod = PreferenceUtils.getSharedPrefTransectParsingMethod(prefs, PREF_TRANSECT_PARSING_METHOD_KEY, ResourceUtils.getResourceTransectParsingMethod(context, R.string.pref_transect_parsing_method_default_value));
	
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREF_TRANSECT_PARSING_METHOD_KEY, context.getResources().getString(R.string.pref_transect_parsing_method_default_value));
		editor.commit();

		GPSUtils.TransectParsingMethod newMethod = PreferenceUtils.getSharedPrefTransectParsingMethod(prefs, PREF_TRANSECT_PARSING_METHOD_KEY, ResourceUtils.getResourceTransectParsingMethod(context, R.string.pref_transect_parsing_method_default_value));
		
		// return true if something changed
		return oldMethod != newMethod;
}
	

}