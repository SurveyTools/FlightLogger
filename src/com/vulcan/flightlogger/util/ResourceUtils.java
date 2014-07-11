package com.vulcan.flightlogger.util;

import com.vulcan.flightlogger.geo.GPSUtils;

import android.content.Context;
import android.util.Log;

public class ResourceUtils {

	private static final String TAG = "ResourceUtils";

	public static String getResourceString(Context context, int rsrcID) {
		String str = null;
		try {
			str =  context.getResources().getString(rsrcID);
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error loading rsrc string for \"" + rsrcID + "\" (" + e.getLocalizedMessage() + ")");
		}
		
		return str;
	}

	public static int getResourceIntegerFromString(Context context, int rsrcID) {
		int intValue = -1;
		try {
			String str =  context.getResources().getString(rsrcID);
			intValue = Integer.valueOf(str);
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error loading rsrc int for \"" + rsrcID + "\" (" + e.getLocalizedMessage() + ")");
		}
		
		return intValue;
	}
	
	public static boolean getResourceBooleanFromString(Context context, int rsrcID) {
		boolean v = false;
		try {
			String str =  context.getResources().getString(rsrcID);
			v = Boolean.valueOf(str);
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error loading rsrc boolean for \"" + rsrcID + "\" (" + e.getLocalizedMessage() + ")");
		}
		
		return v;
	}
	
	public static GPSUtils.TransectParsingMethod getResourceTransectParsingMethod(Context context, int rsrcID) {
		GPSUtils.TransectParsingMethod value = GPSUtils.TransectParsingMethod.USE_DEFAULT; // APP_SETTINGS_WIP

		try {
			value = GPSUtils.getTransectParsingMethodForKey(context.getResources().getString(rsrcID));
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error loading tpm rsrc \"" + rsrcID + "\" (" + e.getLocalizedMessage() + ")");
		}
		
		return value;
	}
		
	public static GPSUtils.Distance2Unit getResourceDistanceUnits(Context context, int rsrcID) {
		GPSUtils.Distance2Unit value = GPSUtils.Distance2Unit.MILES; // APP_SETTINGS_WIP

		try {
			value = GPSUtils.getDistanceUnitForKey(context.getResources().getString(rsrcID));
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error loading distance unit rsrc \"" + rsrcID + "\" (" + e.getLocalizedMessage() + ")");
		}
		
		return value;
	}
		
	public static GPSUtils.VelocityUnit getResourceVelocityUnits(Context context, int rsrcID) {
		GPSUtils.VelocityUnit value = GPSUtils.VelocityUnit.KNOTS_AKA_NAUTICAL_MILES_PER_HOUR; // APP_SETTINGS_WIP

		try {
			value = GPSUtils.getVelocityUnitForKey(context.getResources().getString(rsrcID));
		} catch(Exception e) {
			// failed
			Log.e(TAG, "error loading speed unit rsrc \"" + rsrcID + "\" (" + e.getLocalizedMessage() + ")");
		}
		
		return value;
	}
}
