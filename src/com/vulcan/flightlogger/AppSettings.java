package com.vulcan.flightlogger;

import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import com.vulcan.flightlogger.geo.data.Transect;

public class AppSettings implements Parcelable {

	public boolean mPref1Boolean;
	public boolean mPref2Boolean;
	public String mPref3String;
	public Set<String> mPref4StringSet;

	private static final String LOGGER_TAG = "AppSettings";

	public AppSettings(Context context) {

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		
		// APP_SETTINGS_WIP
		mPref1Boolean = sharedPref.getBoolean(AppSettingsActivity.PREF1_BOOLEAN_KEY, false);
		mPref2Boolean = sharedPref.getBoolean(AppSettingsActivity.PREF2_BOOLEAN_KEY, false);
		mPref3String = sharedPref.getString(AppSettingsActivity.PREF3_STRING_KEY, "");
		mPref4StringSet = sharedPref.getStringSet(AppSettingsActivity.PREF4_STRINGSET_KEY, null);
	}

	// copy constructor
	public AppSettings(AppSettings srcData) {

		if (srcData != null) {
			mPref1Boolean = srcData.mPref1Boolean;
			mPref2Boolean = srcData.mPref2Boolean;
			mPref3String = srcData.mPref3String;
			mPref4StringSet = srcData.mPref4StringSet;
		} else {
			// TODO - defaults
			// APP_SETTINGS_WIP
			mPref1Boolean = false;
			mPref2Boolean = false;
			mPref3String = "";
			mPref4StringSet = null;
		}
	}

	public void clearAll() {
	}

	// parcel part
	public AppSettings(Parcel in) {
		
		// APP_SETTINGS_WIP - hacked out for now
		if (true)
			return;
		
		String[] data = new String[4];

		in.readStringArray(data);
		
		this.mPref1Boolean = Boolean.parseBoolean(data[0]);
		this.mPref2Boolean =  Boolean.parseBoolean(data[1]);
		this.mPref3String = data[2];
		// APP_SETTINGS_WIP this.mPref4StringSet = data[3];
	}

	public void debugDump() {
		Log.d(LOGGER_TAG, "pref1: " + mPref1Boolean);
		Log.d(LOGGER_TAG, "pref2: " + mPref2Boolean);
		Log.d(LOGGER_TAG, "pref3: " + mPref3String);
		Log.d(LOGGER_TAG, "pref4: " + mPref4StringSet);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// APP_SETTINGS_WIP, 4th, etc.

        // APP_SETTINGS_WIP - hacked out for now dest.writeStringArray(values.toArray(new String[values.size()]));

       //  dest.writeStringArray(new String[] { String.valueOf(this.mPref1Boolean), String.valueOf(this.mPref2Boolean), this.mPref3String, this.mPref4StringSet.toString()});
	}

	public static final Parcelable.Creator<AppSettings> CREATOR = new Parcelable.Creator<AppSettings>() {

		@Override
		public AppSettings createFromParcel(Parcel source) {
			return new AppSettings(source);
		}

		@Override
		public AppSettings[] newArray(int size) {
			return new AppSettings[size];
		}
	};

}