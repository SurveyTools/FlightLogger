package com.vulcan.flightlogger;

import com.vulcan.flightlogger.geo.GPSUtils;
import com.vulcan.flightlogger.geo.GPSUtils.*;
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
	public float mPrefAltitudeTargetFeet; // e.g. 300, shown in mPrefAltitudeDisplayUnits, stored in PREF_ALT_NAV_STORAGE_UNITS (FEET)
	public float mPrefAltitudeRadiusFeet; // e.g. +/- 100', shown in mPrefAltitudeDisplayUnits, stored in PREF_ALT_NAV_STORAGE_UNITS (FEET)
	public float mPrefNavigationRadiusFeet; // e.g. +/- 200', shown in mPrefAltitudeDisplayUnits, stored in PREF_ALT_NAV_STORAGE_UNITS (FEET)
	public boolean mUseCustomParsingMethod;
	public TransectParsingMethod mPrefTransectParsingMethod; // e.g. TransectParsingMethod.USE_DEFAULT
	public DistanceUnit mPrefDistanceDisplayUnits;
	public VelocityUnit mPrefSpeedDisplayUnits;
	public DistanceUnit mPrefAltitudeDisplayUnits; // display
	
	public static final DistanceUnit ALT_NAV_STORAGE_UNITS = DistanceUnit.FEET;

	private static final String LOGGER_TAG = "AppSettings";

	public static final String PREF_SHOW_DEBUG_KEY = "PREF_SHOW_DEBUG_KEY";
	public static final String PREF_ALTITUDE_TARGET_KEY = "PREF_ALTITUDE_TARGET_KEY";
	public static final String PREF_ALTITUDE_RADIUS_KEY = "PREF_ALTITUDE_RADIUS_KEY";
	public static final String PREF_NAVIGATION_RADIUS_KEY = "PREF_NAVIGATION_RADIUS_KEY";
	public static final String PREF_USE_CUSTOM_PARSING_METHOD_KEY = "PREF_USE_CUSTOM_PARSING_METHOD_KEY";
	public static final String PREF_TRANSECT_PARSING_METHOD_KEY = "PREF_TRANSECT_PARSING_METHOD_KEY";
	
	// PREF_UNITS
	public static final String PREF_DISPLAY_UNITS_DISTANCE_KEY = "PREF_DISPLAY_UNITS_DISTANCE_KEY";
	public static final String PREF_DISPLAY_UNITS_SPEED_KEY = "PREF_DISPLAY_UNITS_SPEED_KEY";
	public static final String PREF_DISPLAY_UNITS_ALTITUDE_KEY = "PREF_DISPLAY_UNITS_ALTITUDE_KEY";
	public static final String PREF_ALT_NAV_UNITS_STORAGE_KEY = "PREF_ALT_NAV_UNITS_STORAGE_KEY";


	public AppSettings(ContextWrapper contextWrapper) {
		mContextWrapper = contextWrapper;
		refresh(contextWrapper);
	}

	// copy constructor
	public AppSettings(AppSettings srcData) {

		if (srcData != null) {
			mPrefShowDebug = srcData.mPrefShowDebug;
			mPrefAltitudeTargetFeet = srcData.mPrefAltitudeTargetFeet;
			mPrefAltitudeTargetFeet = srcData.mPrefAltitudeTargetFeet;
			mPrefNavigationRadiusFeet = srcData.mPrefNavigationRadiusFeet;
			mUseCustomParsingMethod = srcData.mUseCustomParsingMethod;
			mPrefTransectParsingMethod = srcData.mPrefTransectParsingMethod;
			mPrefDistanceDisplayUnits = srcData.mPrefDistanceDisplayUnits;
			mPrefSpeedDisplayUnits = srcData.mPrefSpeedDisplayUnits;
			mPrefAltitudeDisplayUnits = srcData.mPrefAltitudeDisplayUnits;

			mContextWrapper = srcData.mContextWrapper;
		} else {
			reset();
		}
	}

	public void debugDump() {
		Log.d(LOGGER_TAG, "mPrefShowDebug: " + mPrefShowDebug);
		Log.d(LOGGER_TAG, "mPrefAltitudeTargetFeet: " + mPrefAltitudeTargetFeet);
		Log.d(LOGGER_TAG, "mPrefAltitudeRadiusFeet: " + mPrefAltitudeRadiusFeet);
		Log.d(LOGGER_TAG, "mPrefNavigationRadiusFeet: " + mPrefNavigationRadiusFeet);
		Log.d(LOGGER_TAG, "mUseCustomParsingMethod: " + mUseCustomParsingMethod);
		Log.d(LOGGER_TAG, "mPrefTransectParsingMethod: " + mPrefTransectParsingMethod);
		Log.d(LOGGER_TAG, "mPrefDistanceDisplayUnits: " + mPrefDistanceDisplayUnits);
		Log.d(LOGGER_TAG, "mPrefSpeedDisplayUnits: " + mPrefSpeedDisplayUnits);
		Log.d(LOGGER_TAG, "mPrefAltitudeDisplayUnits: " + mPrefAltitudeDisplayUnits);
	}

	public void refresh(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		// TODO eval context.getSharedPreferences("userdetails", Context.MODE_PRIVATE)

		mPrefShowDebug = sharedPref.getBoolean(PREF_SHOW_DEBUG_KEY, ResourceUtils.getResourceBooleanFromString(context, R.string.pref_show_debug_default_value));

		mPrefAltitudeTargetFeet = PreferenceUtils.getSharedPrefStringAsInteger(sharedPref, PREF_ALTITUDE_TARGET_KEY, ResourceUtils.getResourceIntegerFromString(context, R.string.pref_altitude_target_default_value));
		mPrefAltitudeRadiusFeet = PreferenceUtils.getSharedPrefStringAsInteger(sharedPref, PREF_ALTITUDE_RADIUS_KEY, ResourceUtils.getResourceIntegerFromString(context, R.string.pref_altitude_radius_default_value));
		mPrefNavigationRadiusFeet = PreferenceUtils.getSharedPrefStringAsInteger(sharedPref, PREF_NAVIGATION_RADIUS_KEY, ResourceUtils.getResourceIntegerFromString(context, R.string.pref_navigation_radius_default_value));
		
		mUseCustomParsingMethod = sharedPref.getBoolean(PREF_USE_CUSTOM_PARSING_METHOD_KEY, ResourceUtils.getResourceBooleanFromString(context, R.string.pref_use_custom_transect_parsing_method_default_value));
		mPrefTransectParsingMethod = PreferenceUtils.getSharedPrefTransectParsingMethod(sharedPref, PREF_TRANSECT_PARSING_METHOD_KEY, ResourceUtils.getResourceTransectParsingMethod(context, R.string.pref_transect_parsing_method_default_value));
		
		mPrefDistanceDisplayUnits = PreferenceUtils.getSharedPrefDistanceUnits(sharedPref, PREF_DISPLAY_UNITS_DISTANCE_KEY, ResourceUtils.getResourceDistanceUnits(context, R.string.pref_distance_units_default_value));
		mPrefSpeedDisplayUnits = PreferenceUtils.getSharedPrefVelocityUnits(sharedPref, PREF_DISPLAY_UNITS_SPEED_KEY, ResourceUtils.getResourceVelocityUnits(context, R.string.pref_speed_units_default_value));
		mPrefAltitudeDisplayUnits = PreferenceUtils.getSharedPrefDistanceUnits(sharedPref, PREF_DISPLAY_UNITS_ALTITUDE_KEY, ResourceUtils.getResourceDistanceUnits(context, R.string.pref_altitude_units_default_value));

		// TESTING debugDump();
	}

	public void reset() {
		// TODO reset doesn't really work -- it's just for the memory copy (not the real prefs)
		mPrefShowDebug = ResourceUtils.getResourceBooleanFromString(mContextWrapper, R.string.pref_show_debug_default_value);

		mPrefAltitudeTargetFeet = ResourceUtils.getResourceIntegerFromString(mContextWrapper, R.string.pref_altitude_target_default_value);
		mPrefAltitudeRadiusFeet = ResourceUtils.getResourceIntegerFromString(mContextWrapper, R.string.pref_altitude_radius_default_value);
		mPrefNavigationRadiusFeet = ResourceUtils.getResourceIntegerFromString(mContextWrapper, R.string.pref_navigation_radius_default_value);
		
		mUseCustomParsingMethod = ResourceUtils.getResourceBooleanFromString(mContextWrapper, R.string.pref_use_custom_transect_parsing_method_default_value);
		mPrefTransectParsingMethod = ResourceUtils.getResourceTransectParsingMethod(mContextWrapper, R.string.pref_transect_parsing_method_default_value);

		mPrefDistanceDisplayUnits = ResourceUtils.getResourceDistanceUnits(mContextWrapper, R.string.pref_distance_units_default_value);
		mPrefSpeedDisplayUnits = ResourceUtils.getResourceVelocityUnits(mContextWrapper, R.string.pref_speed_units_default_value);
		mPrefAltitudeDisplayUnits = ResourceUtils.getResourceDistanceUnits(mContextWrapper, R.string.pref_altitude_units_default_value);
	}
	
	public static TransectParsingMethod getPrefTransectParsingMethod(Context context) {
		// note: could honor mUseCustomParsingMethod
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return PreferenceUtils.getSharedPrefTransectParsingMethod(sharedPref, PREF_TRANSECT_PARSING_METHOD_KEY, ResourceUtils.getResourceTransectParsingMethod(context, R.string.pref_transect_parsing_method_default_value));
	}
	
	public static DistanceUnit getPrefDistanceDisplayUnit(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return PreferenceUtils.getSharedPrefDistanceUnits(sharedPref, PREF_DISPLAY_UNITS_DISTANCE_KEY, ResourceUtils.getResourceDistanceUnits(context, R.string.pref_distance_units_default_value));
	}
	
	public static VelocityUnit getPrefSpeedDisplayUnit(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return PreferenceUtils.getSharedPrefVelocityUnits(sharedPref, PREF_DISPLAY_UNITS_SPEED_KEY, ResourceUtils.getResourceVelocityUnits(context, R.string.pref_speed_units_default_value));
	}
	
	public static DistanceUnit getPrefAltitudeDisplayUnit(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return PreferenceUtils.getSharedPrefDistanceUnits(sharedPref, PREF_DISPLAY_UNITS_ALTITUDE_KEY, ResourceUtils.getResourceDistanceUnits(context, R.string.pref_altitude_units_default_value));
	}
	
	public static boolean isPrefUseCustomTransectParsingKey(String key) {
		return PREF_USE_CUSTOM_PARSING_METHOD_KEY.equalsIgnoreCase(key);
	}
	
	public static boolean isPrefDisplayUnitsDistanceParsingKey(String key) {
		return PREF_DISPLAY_UNITS_DISTANCE_KEY.equalsIgnoreCase(key);
	}
	
	public static boolean isPrefDisplayUnitsSpeedParsingKey(String key) {
		return PREF_DISPLAY_UNITS_SPEED_KEY.equalsIgnoreCase(key);
	}
	
	public static boolean isPrefDisplayUnitsAltitudeParsingKey(String key) {
		return PREF_DISPLAY_UNITS_ALTITUDE_KEY.equalsIgnoreCase(key);
	}
	
	public static boolean getPrefUseCustomTransectParsing(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(PREF_USE_CUSTOM_PARSING_METHOD_KEY, ResourceUtils.getResourceBooleanFromString(context, R.string.pref_show_debug_default_value));
	}

	public static boolean resetCustomTransectParsingMethodToDefault(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		TransectParsingMethod oldMethod = PreferenceUtils.getSharedPrefTransectParsingMethod(prefs, PREF_TRANSECT_PARSING_METHOD_KEY, ResourceUtils.getResourceTransectParsingMethod(context, R.string.pref_transect_parsing_method_default_value));
	
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREF_TRANSECT_PARSING_METHOD_KEY, context.getResources().getString(R.string.pref_transect_parsing_method_default_value));
		editor.commit();

		TransectParsingMethod newMethod = PreferenceUtils.getSharedPrefTransectParsingMethod(prefs, PREF_TRANSECT_PARSING_METHOD_KEY, ResourceUtils.getResourceTransectParsingMethod(context, R.string.pref_transect_parsing_method_default_value));
		
		// return true if something changed
		return oldMethod != newMethod;
	}
}