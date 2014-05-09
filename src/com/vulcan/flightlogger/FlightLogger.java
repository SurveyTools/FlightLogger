package com.vulcan.flightlogger;

import com.vulcan.flightlogger.altimeter.LaserAltimeterActivity;
import com.vulcan.flightlogger.altimeter.SerialConsole;
import com.vulcan.flightlogger.geo.GPSDebugActivity;
import com.vulcan.flightlogger.geo.RouteListActivity;
import com.vulcan.flightlogger.util.SquishyTextView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class FlightLogger extends Activity {

	// used for identifying Activities that return results
	static final int LOAD_GPX_FILE = 10001;

	private final String LOGGER_TAG = FlightLogger.class.getSimpleName();

	void setupSquishyFontView(int groupID, int ideal, int min) {
		SquishyTextView squishyTextView = (SquishyTextView) findViewById(groupID);
		if (squishyTextView != null) {
			squishyTextView.setIdealTextSizeDP(ideal);
			squishyTextView.setMinimumTextSizeDP(min);
		}
	}

	void setColorforViewWithID(int groupID, int colorID) {
		View v = findViewById(groupID);
		if (v != null)
			v.setBackgroundColor(getResources().getColor(colorID));
	}

	void setBlackColorforViewWithID(int groupID) {
		setColorforViewWithID(groupID, R.color.nav_background);
	}

	void setHeaderColorforViewWithID(int groupID) {
		setColorforViewWithID(groupID, R.color.nav_header_bg);
	}

	void setFooterColorforViewWithID(int groupID) {
		setColorforViewWithID(groupID, R.color.nav_footer_bg);
	}

	protected void setupColors() {
		// whole screen
		setBlackColorforViewWithID(R.id.main_layout);

		// left & right block
		setBlackColorforViewWithID(R.id.navscreenLeft);
		setBlackColorforViewWithID(R.id.navscreenRight);

		// altitude
		setBlackColorforViewWithID(R.id.nav_altitude_group_wrapper);
		setBlackColorforViewWithID(R.id.nav_altitude_group);
		setBlackColorforViewWithID(R.id.nav_altitude_value);
		setBlackColorforViewWithID(R.id.nav_altitude_righthalf);
		setBlackColorforViewWithID(R.id.nav_altitude_label);
		setBlackColorforViewWithID(R.id.nav_altitude_units);

		// speed
		setBlackColorforViewWithID(R.id.nav_speed_group_wrapper);
		setBlackColorforViewWithID(R.id.nav_speed_group);
		setBlackColorforViewWithID(R.id.nav_speed_value);
		setBlackColorforViewWithID(R.id.nav_speed_righthalf);
		setBlackColorforViewWithID(R.id.nav_speed_label);
		setBlackColorforViewWithID(R.id.nav_speed_units);

		// header
		setHeaderColorforViewWithID(R.id.nav_header);
		setHeaderColorforViewWithID(R.id.nav_header_left);
		setHeaderColorforViewWithID(R.id.nav_header_right);
		setHeaderColorforViewWithID(R.id.nav_header_settings_button);

		// footer
		setFooterColorforViewWithID(R.id.nav_footer);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		// TESTING Log.i("main", "onCreate!");

		ViewGroup layout = (ViewGroup) findViewById(R.id.navscreenLeft);
		TransectILSView tv = new TransectILSView(this);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		tv.setLayoutParams(lp);
		layout.addView(tv);

		setupSquishyFontView(R.id.nav_altitude_value, 190, 20);
		setupSquishyFontView(R.id.nav_speed_value, 130, 20);

		// TESTING flag - set to false for debugging layout
		if (true)
			setupColors();
	}

	/**
	 * Action menu handling
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.action_show_gps_debug:
			intent = new Intent(this, GPSDebugActivity.class);
			startActivity(intent);
			break;
		// use laser altimeter
		case R.id.action_show_serial_console:
			intent = new Intent(this, SerialConsole.class);
			startActivity(intent);
			break;
		// use laser altimeter
		case R.id.action_show_laser_alt:
			intent = new Intent(this, LaserAltimeterActivity.class);
			startActivity(intent);
			break;
		case R.id.action_show_route_list:
			// load gpx
			intent = new Intent(this, FileBrowser.class);
			this.startActivityForResult(intent, LOAD_GPX_FILE);
		}
		return true;
	}

	/**
	 * Callbacks from activities that return results
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == LOAD_GPX_FILE) {
			// Make sure the load activity was successful
			if (resultCode == RESULT_OK) {
				String gpxName = data.getStringExtra("gpxfile");
				Log.d(LOGGER_TAG, "GPX filename: " + gpxName);
				Intent it = new Intent(this, RouteListActivity.class);
				it.putExtra("gpxfile", gpxName);
				startActivity(it);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();

		// note: requires Android 4.4 / api level 16 & 19
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

	}

}
