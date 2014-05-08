package com.vulcan.flightlogger;

import com.vulcan.flightlogger.altimeter.AltimeterService;
import com.vulcan.flightlogger.altimeter.LaserAltimeterActivity;
import com.vulcan.flightlogger.altimeter.SerialConsole;
import com.vulcan.flightlogger.geo.GPSDebugActivity;
import com.vulcan.flightlogger.geo.NavigationService;
import com.vulcan.flightlogger.geo.RouteListActivity;

import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class FlightLogger extends USBAwareActivity {

	// used for identifying Activities that return results
	static final int LOAD_GPX_FILE = 10001;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);		
		startServices();
	}

	private void startServices() {
		// TODO - this becomes a RouteManagerService, or
		// whatever we call it. For now, spin up the AltimeterService
        Intent altIntent = new Intent(this, AltimeterService.class);
        //intent.putExtra(AltimeterService.USE_MOCK_DATA, true);
        startService(altIntent);	
        Intent navIntent = new Intent(this, NavigationService.class);
        startService(navIntent);
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
	}
	
	@Override
	protected void initUsbDevice(UsbDevice device) {
		super.initUsbDevice(device);
	}

}
