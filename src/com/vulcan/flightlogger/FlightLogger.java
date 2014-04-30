package com.vulcan.flightlogger;

import java.io.File;

import com.vulcan.flightlogger.altimeter.LaserAltimeterActivity;
import com.vulcan.flightlogger.altimeter.SerialConsole;
import com.vulcan.flightlogger.geo.GPSActivity;
import com.vulcan.flightlogger.geo.RouteListActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class FlightLogger extends Activity {

	// used for identifying Activities that return results
	static final int LOAD_GPX_FILE = 10001;

	private final String LOGGER_TAG = FlightLogger.class.getSimpleName();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

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
			intent = new Intent(this, GPSActivity.class);
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
			intent = new Intent(this, RouteListActivity.class);
			startActivity(intent);
			break;
		}
		return true;
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

}
