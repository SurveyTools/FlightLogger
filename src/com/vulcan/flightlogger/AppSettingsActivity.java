package com.vulcan.flightlogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.vulcan.flightlogger.geo.RouteChooserDialog;
import com.vulcan.flightlogger.geo.TransectChooserDialog;
import com.vulcan.flightlogger.util.SystemUtils;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.View.OnClickListener;

public class AppSettingsActivity extends FragmentActivity implements OnSharedPreferenceChangeListener {

	private AppSettingsFragment mSettingsList;
	
	private Button mResetButton;
	private Button mOkButton;

	private AppSettings mOriginalData;
	private AppSettings mWorkingData;

	private static final String LOGGER_TAG = "AppSettingsActivity";
	public static final String APP_SETTINGS_DATA_KEY = "ASOriginalData";
	private static final String AS_WORKING_DATA_KEY = "ASWorkingData";
	
	public static final String PREF1_BOOLEAN_KEY = "PREF1_BOOLEAN_KEY";
	public static final String PREF2_BOOLEAN_KEY = "PREF2_BOOLEAN_KEY";
	public static final String PREF3_STRING_KEY = "PREF3_STRING_KEY";
	public static final String PREF4_STRINGSET_KEY = "PREF4_STRINGSET_KEY";
	
	protected void immerseMe(String caller) {

		// IMMERSIVE_MODE
		// TESTING Log.i(LOGGER_TAG, "setting the window to immersive and sticky (from " + caller + ")");
		// ref: source code for View
		// also ref ref https://plus.google.com/+MichaelLeahy/posts/CqSCP653UrW

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // don't bump the bottom ok/cancel buttons up even when the 3 buttons are shown
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE // keeps the content laid out correctly, but activity bar still pushes downs
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // View would like its window to be layed out as if it has requested SYSTEM_UI_FLAG_FULLSCREEN, even if it currently hasn't
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // bottom 3 buttons
				| View.SYSTEM_UI_FLAG_FULLSCREEN // top bar (system bar)
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.app_settings);

		// IMMERSIVE_MODE note: setting the theme here didn't help setTheme(android.R.style.Theme_NoTitleBar);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mSettingsList = (AppSettingsFragment) getFragmentManager().findFragmentById(R.id.prefs_fragment);
		mResetButton = (Button) findViewById(R.id.as_reset_button);
		mOkButton = (Button) findViewById(R.id.as_ok_button);
		
		// SAVE_RESTORE_STATE
		if (savedInstanceState != null) {
			mOriginalData = savedInstanceState.getParcelable(APP_SETTINGS_DATA_KEY);
			mWorkingData = savedInstanceState.getParcelable(AS_WORKING_DATA_KEY);
		} 
		
		if (mOriginalData == null)
			mOriginalData = getIntent().getParcelableExtra(APP_SETTINGS_DATA_KEY);
		
		if (mWorkingData == null)
			mWorkingData =  new AppSettings(mOriginalData); // clone;
		
		mWorkingData.debugDump();
		
		updateFromWorkingData();

		setupButtons();
		setupColors();
		updateDataUI();

		// IMMERSIVE_MODE note: onSystemUiVisibilityChange hook didn't work
		// IMMERSIVE_MODE note: delayed change didn't help: mImmersiveRunnable = new Runnable() { @Override public void run() { immerseMe("runnable delayed"); } };
		// IMMERSIVE_MODE
		immerseMe("dlog OnCreate");
	}

	// SAVE_RESTORE_STATE
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(APP_SETTINGS_DATA_KEY, mOriginalData);
		outState.putParcelable(AS_WORKING_DATA_KEY, mWorkingData);
	}

	protected void resetAllToDefaults() {
		// wacky
		// note: stored in /data/user/0/com.vulcan.flightlogger/shared_prefs/com.vulcan.flightlogger_preferences.xml
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

		mSettingsList.fakeInvalidate();

	}
	
	protected void confirmResetAll() {
        new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.as_confirm_reset_title)
        .setMessage(R.string.as_confirm_reset_message)
        .setPositiveButton(R.string.as_confirm_reset_ok, new DialogInterface.OnClickListener() {

	            @Override
	            public void onClick(DialogInterface dialog, int which) {

	                // CONFIRMED
	            	resetAllToDefaults();

                    dialog.cancel();
	            }
	        })
	    .setNegativeButton(R.string.as_confirm_reset_cancel, null)
	    .show();
	}
	
	protected void setupButtons() {

		mResetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				confirmResetAll();
			}
		});

		mOkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finishWithDone();
			}
		});
	}

	protected void updateDataUI() {
		// APP_SETTINGS_WIP
		mOkButton.setEnabled(true);
	}
	
	protected void updateFromWorkingData() {
		// APP_SETTINGS_WIP
	}

	private void finishWithDone() {
		Intent intent = getIntent();
		intent.putExtra(APP_SETTINGS_DATA_KEY, mWorkingData);
		this.setResult(RESULT_OK, intent);
		finish();
	}

	private void setColorforViewWithID(int viewID, int colorID) {
		View v = findViewById(viewID);
		try {
			if (v != null)
				v.setBackgroundColor(getResources().getColor(colorID));
		} catch (Exception e) {
			Log.e("FlightSettings", e.getLocalizedMessage());
		}
	}

	private void setClearColorforViewWithID(int viewID) {
		View v = findViewById(viewID);
		if (v != null)
			v.setBackgroundColor(Color.TRANSPARENT);
	}

	protected void setupColors() {

		// override debug colors
		// TESTING flag
		if (true) {

			// whole screen
			setColorforViewWithID(R.id.as_background_wrapper, R.color.as_background_color);
			setColorforViewWithID(R.id.as_header, R.color.as_header_color);
			setColorforViewWithID(R.id.as_footer, R.color.as_footer_color);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			immerseMe("onWindowFocusChanged");
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// APP_SETTINGS_WIP - we get notified or each key
		Log.d(LOGGER_TAG, "onSharedPreferenceChanged: " + key);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSettingsList.getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		mSettingsList.getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}
}
