package com.vulcan.flightlogger;

import com.vulcan.flightlogger.altimeter.AltimeterService;
import com.vulcan.flightlogger.altimeter.AltitudeUpdateListener;
import com.vulcan.flightlogger.altimeter.SerialConsole;
import com.vulcan.flightlogger.geo.GPSDebugActivity;
import com.vulcan.flightlogger.geo.NavigationService;
import com.vulcan.flightlogger.geo.RouteListActivity;
import com.vulcan.flightlogger.geo.TransectUpdateListener;
import com.vulcan.flightlogger.geo.data.TransectStatus;
import com.vulcan.flightlogger.util.SquishyTextView;
import com.vulcan.flightlogger.FlightDatum;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Button;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;

public class FlightLogger extends USBAwareActivity implements AltitudeUpdateListener, TransectUpdateListener, OnMenuItemClickListener {

	// used for identifying Activities that return results
	static final int LOAD_GPX_FILE = 10001;
	static final int UI_UPDATE_TIMER_MILLIS = 500;
	public static final int UPDATE_IMAGE = 666;
	private AltimeterService mAltimeterService;
	private NavigationService mNavigationService;
	private TextView mAltitudeDisplay;
	private TextView mGroundSpeedDisplay;

	private Button mStatusButtonGPS;
	private Button mStatusButtonALT;
	private Button mStatusButtonBAT;
	private Button mStatusButtonBOX;

	private Drawable mStatusButtonBackgroundRed;
	private Drawable mStatusButtonBackgroundYellow;
	private Drawable mStatusButtonBackgroundGreen;
	private Drawable mStatusButtonBackgroundGrey;
	private Drawable mStatusButtonBackgroundIgnore;

	private int mStatusButtonTextColorOnRed;
	private int mStatusButtonTextColorOnYellow;
	private int mStatusButtonTextColorOnGreen;
	private int mStatusButtonTextColorOnGrey;
	private int mStatusButtonTextColorOnIgnore;

	// data
	protected AltitudeDatum mAltitudeData;
	protected GPSDatum mGPSData;
	protected BatteryDatum mBatteryData;
	protected BoxDatum mBoxData;

	private Handler mUpdateUIHandler;

	/**
	 * Defines callbacks for local service binding, ie bindService() For local
	 * binds, this is where we will attach assign instance references, and add
	 * and remove listeners, since we have inprocess access to the class
	 * interface
	 */
	private ServiceConnection mNavigationConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			com.vulcan.flightlogger.geo.NavigationService.LocalBinder binder = (com.vulcan.flightlogger.geo.NavigationService.LocalBinder) service;
			mNavigationService = (NavigationService) binder.getService();
			mNavigationService.registerListener(FlightLogger.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mNavigationService.unregisterListener(FlightLogger.this);
		}
	};

	private ServiceConnection mAltimeterConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			com.vulcan.flightlogger.altimeter.AltimeterService.LocalBinder binder = (com.vulcan.flightlogger.altimeter.AltimeterService.LocalBinder) service;
			mAltimeterService = (AltimeterService) binder.getService();
			mAltimeterService.initSerialCommunication();
			mAltimeterService.registerListener(FlightLogger.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mAltimeterService.unregisterListener(FlightLogger.this);
		}
	};

	protected void onStart() {
		super.onStart();
		// Bind to AltimeterService - we get a callback on the
		// binding which gives us a reference to the service
		Intent intent = new Intent(this, AltimeterService.class);
		this.bindService(intent, mAltimeterConnection, 0);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		startServices();

		setContentView(R.layout.main);
		// TESTING Log.i("main", "onCreate!");

		ViewGroup layout = (ViewGroup) findViewById(R.id.navscreenLeft);
		TransectILSView tv = new TransectILSView(this);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		// superdevo
		mAltitudeData = new AltitudeDatum(false);
		mGPSData = new GPSDatum(false);
		mBatteryData = new BatteryDatum(false);
		mBoxData = new BoxDatum(true);

		mAltitudeDisplay = (TextView) findViewById(R.id.nav_altitude_value);
		mGroundSpeedDisplay = (TextView) findViewById(R.id.nav_speed_value);

		mStatusButtonGPS = (Button) findViewById(R.id.nav_header_status_gps);
		mStatusButtonALT = (Button) findViewById(R.id.nav_header_status_alt);
		mStatusButtonBAT = (Button) findViewById(R.id.nav_header_status_bat);
		mStatusButtonBOX = (Button) findViewById(R.id.nav_header_status_box);

		// backgrounds for status lights
		mStatusButtonBackgroundRed = getResources().getDrawable(R.drawable.nav_status_red);
		mStatusButtonBackgroundYellow = getResources().getDrawable(R.drawable.nav_status_yellow);
		mStatusButtonBackgroundGreen = getResources().getDrawable(R.drawable.nav_status_green);
		mStatusButtonBackgroundGrey = getResources().getDrawable(R.drawable.nav_status_grey);
		mStatusButtonBackgroundIgnore = getResources().getDrawable(R.drawable.nav_status_ignore);

		tv.setLayoutParams(lp);
		layout.addView(tv);

		setupSquishyFontView(R.id.nav_altitude_value, 190, 20);
		setupSquishyFontView(R.id.nav_speed_value, 130, 20);

		mUpdateUIHandler = new Handler();

		setupColors();

		resetData();
	}

	private void startServices() {
		// TODO - this becomes a RouteManagerService, or
		// whatever we call it. For now, spin up the AltimeterService
		Intent altIntent = new Intent(this, AltimeterService.class);
		// altIntent.putExtra(AltimeterService.USE_MOCK_DATA, true);
		startService(altIntent);
		Intent navIntent = new Intent(this, NavigationService.class);
		startService(navIntent);
	}

	protected void updateBatteryStatus(Intent batteryStatus) {
		if (mBatteryData.updateBatteryStatus(batteryStatus))
			updateBatteryUI();
	}

	private void setupSquishyFontView(int groupID, int ideal, int min) {
		SquishyTextView squishyTextView = (SquishyTextView) findViewById(groupID);
		if (squishyTextView != null) {
			squishyTextView.setIdealTextSizeDP(ideal);
			squishyTextView.setMinimumTextSizeDP(min);
		}
	}

	private void setColorforViewWithID(int groupID, int colorID) {
		View v = findViewById(groupID);
		if (v != null)
			v.setBackgroundColor(getResources().getColor(colorID));
	}

	private void setBlackColorforViewWithID(int groupID) {
		setColorforViewWithID(groupID, R.color.nav_background);
	}

	private void setHeaderColorforViewWithID(int groupID) {
		setColorforViewWithID(groupID, R.color.nav_header_bg);
	}

	private void setFooterColorforViewWithID(int groupID) {
		setColorforViewWithID(groupID, R.color.nav_footer_bg);
	}

	protected void setupColors() {

		// override debug colors
		// TESTING flag
		if (true) {

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

		// status button colors
		mStatusButtonTextColorOnRed = getResources().getColor(R.color.nav_header_status_text_over_red);
		mStatusButtonTextColorOnYellow = getResources().getColor(R.color.nav_header_status_text_over_yellow);
		mStatusButtonTextColorOnGreen = getResources().getColor(R.color.nav_header_status_text_over_green);
		mStatusButtonTextColorOnGrey = getResources().getColor(R.color.nav_header_status_text_over_grey);
		mStatusButtonTextColorOnIgnore = getResources().getColor(R.color.nav_header_status_text_over_ignore);
	}

	protected void resetData() {
		mAltitudeData.reset();
		mGPSData.reset();
		mBatteryData.reset();
		mBoxData.reset();
		// note: might want to update the ui (last param) depending on use
	}

	public boolean showSettingsPopup(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		popup.setOnMenuItemClickListener(this);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, popup.getMenu());
		popup.show();
		return true;
	}

	public boolean onMenuItemClick(MenuItem item) {
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
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

		// TESTING mAltitudeData.setRawAltitude(299, true, curDataTimestamp());

		updateUI();

		mUpdateUIHandler.postDelayed(mUpdateUIRunnable, UI_UPDATE_TIMER_MILLIS);
	}

	@Override
	public void onPause() {
		super.onPause();
		mUpdateUIHandler.removeCallbacks(mUpdateUIRunnable);
	}

	private final Runnable mUpdateUIRunnable = new Runnable() {
		@Override
		public void run() {
			updateUI();
			mUpdateUIHandler.postDelayed(mUpdateUIRunnable, UI_UPDATE_TIMER_MILLIS);
		}
	};

	@Override
	protected void initUsbDevice(UsbDevice device) {
		super.initUsbDevice(device);
	}

	protected void updateStatusButton(Button button, FlightDatum dataStatus) {

		if (button != null) {
			Drawable buttonBG = mStatusButtonBackgroundGrey; // default
			int textColor = mStatusButtonTextColorOnGrey;

			// status to background color
			switch (dataStatus.getStatusColor()) {
			case FlightDatum.FLIGHT_STATUS_RED:
				buttonBG = mStatusButtonBackgroundRed;
				textColor = mStatusButtonTextColorOnRed;
				break;

			case FlightDatum.FLIGHT_STATUS_YELLOW:
				buttonBG = mStatusButtonBackgroundYellow;
				textColor = mStatusButtonTextColorOnYellow;
				break;

			case FlightDatum.FLIGHT_STATUS_GREEN:
				buttonBG = mStatusButtonBackgroundGreen;
				textColor = mStatusButtonTextColorOnGreen;
				break;

			case FlightDatum.FLIGHT_STATUS_UNKNOWN:
				buttonBG = mStatusButtonBackgroundGrey;
				textColor = mStatusButtonTextColorOnGrey;
				break;

			case FlightDatum.FLIGHT_STATUS_IGNORE:
				buttonBG = mStatusButtonBackgroundIgnore;
				textColor = mStatusButtonTextColorOnIgnore;
				break;

			}

			button.setBackground(buttonBG);
			button.setTextColor(textColor);
		}
	}

	protected void updateRouteUI() {
		// todo
		// icon color
		// route
		// transect
	}

	protected void updateAltitudeUI() {
		updateStatusButton(mStatusButtonALT, mAltitudeData);
		mAltitudeDisplay.setText(mAltitudeData.getAltitudeDisplayText());
	}

	protected void updateGPSUI() {
		updateStatusButton(mStatusButtonGPS, mGPSData);
		mGroundSpeedDisplay.setText(mGPSData.getGroundSpeedDisplayText());
	}

	protected void updateBatteryUI() {
		updateStatusButton(mStatusButtonBAT, mBatteryData);
	}

	protected void updateBoxUI() {
		updateStatusButton(mStatusButtonBOX, mBoxData);
	}

	protected void updateFooterUI() {
		// todo...
		// status
		// start/stop button
		// remaining
	}

	protected void updateUI() {
		updateRouteUI();
		updateAltitudeUI();
		updateGPSUI();
		updateBatteryUI();
		updateBoxUI();
		updateFooterUI();
	}

	protected long curDataTimestamp() {
		return System.currentTimeMillis();
	}

	public void onAltitudeUpdate(float altitudeInMeters) {
		// rough validation
		if (altitudeInMeters < 32000) {
			final float currAltitudeInMeters = altitudeInMeters;
			final long timestamp = curDataTimestamp();
			runOnUiThread(new Runnable() {
				public void run() {
					// update the altitude data (and ui if something changed)
					if (mAltitudeData.setRawAltitudeInMeters(currAltitudeInMeters, true, timestamp))
						updateAltitudeUI();
				}
			});
		}
	}

	@Override
	public void onAltitudeError(String error) {
		// TODO Auto-generated method stub
		updateAltitudeUI();
	}

	public void doTimerCallback() {

		try {
			// TESTING Log.d("doTimerCallback", "updating the ui...");
			updateUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConnectionEnabled() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionDisabled() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRouteUpdate(TransectStatus status) {

		// ground speed update
		if (status != null) {
			final float groundSpeed = status.mGroundSpeed;
			final long timestamp = curDataTimestamp();
			runOnUiThread(new Runnable() {
				public void run() {
					// update the altitude data (and ui if something changed)
					if (mGPSData.setRawGroundSpeed(groundSpeed, true, timestamp))
						updateGPSUI();
				}
			});
		}
	}
}
