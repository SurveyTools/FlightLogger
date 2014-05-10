package com.vulcan.flightlogger;

import com.vulcan.flightlogger.altimeter.AltimeterService;
import com.vulcan.flightlogger.altimeter.AltitudeUpdateListener;
import com.vulcan.flightlogger.altimeter.LaserAltimeterActivity;
import com.vulcan.flightlogger.altimeter.SerialConsole;
import com.vulcan.flightlogger.altimeter.AltimeterService.LocalBinder;
import com.vulcan.flightlogger.geo.GPSDebugActivity;
import com.vulcan.flightlogger.geo.NavigationService;
import com.vulcan.flightlogger.geo.RouteListActivity;
import com.vulcan.flightlogger.util.SquishyTextView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Button;
import android.graphics.drawable.Drawable;

public class FlightLogger extends USBAwareActivity implements AltitudeUpdateListener {

	// used for identifying Activities that return results
	static final int LOAD_GPX_FILE = 10001;
	static final int MAX_DATA_LIFESPAN_MILLIS = 3000;
	private AltimeterService mAltimeterService;
	private boolean mBound = false;
	private TextView mAltitudeValue;
	
	private Button		mStatusButtonGPS;
	private Button		mStatusButtonALT;
	private Button		mStatusButtonBAT;
	private Button		mStatusButtonBOX;
	
	private Drawable	mStatusButtonBackgroundRed;
	private Drawable	mStatusButtonBackgroundYellow;
	private Drawable	mStatusButtonBackgroundGreen;
	private Drawable	mStatusButtonBackgroundGrey;
	private Drawable	mStatusButtonBackgroundIgnore;
	
	// altitude
	private float 	mCurAltitudeRawValue;
	private int		mCurAltitudeDisplayValue;
	private long	mCurAltitudeTimestampMillis;
	
    /** 
     * Defines callbacks for local service binding, ie bindService()
     * For local binds, this is where we will attach assign instance 
     * references, and add and remove listeners, 
     * since we have inprocess access to the class interface
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            mAltimeterService = (AltimeterService)binder.getService();
            mAltimeterService.initSerialCommunication();
            mAltimeterService.registerListener(FlightLogger.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	mAltimeterService.unregisterListener(FlightLogger.this);
            mBound = false;
        }
    };
    
    protected void onStart() {
        super.onStart();
        // Bind to AltimeterService - we get a callback on the
        // binding which gives us a reference to the service
        Intent intent = new Intent(this, AltimeterService.class);
        this.bindService(intent, mConnection, 0);
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
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		
		mAltitudeValue = (TextView) findViewById(R.id.nav_altitude_value);

		mStatusButtonGPS = (Button) findViewById(R.id.nav_header_status_gps);
		mStatusButtonALT = (Button) findViewById(R.id.nav_header_status_alt);
		mStatusButtonBAT = (Button) findViewById(R.id.nav_header_status_bat);
		mStatusButtonBOX = (Button) findViewById(R.id.nav_header_status_box);

		// backgrounds for status lights
		mStatusButtonBackgroundRed = getResources().getDrawable( R.drawable.nav_status_red );
		mStatusButtonBackgroundYellow = getResources().getDrawable( R.drawable.nav_status_yellow );
		mStatusButtonBackgroundGreen = getResources().getDrawable( R.drawable.nav_status_green );
		mStatusButtonBackgroundGrey = getResources().getDrawable( R.drawable.nav_status_grey );
		mStatusButtonBackgroundIgnore = getResources().getDrawable( R.drawable.nav_status_ignore );
	
		tv.setLayoutParams(lp);
		layout.addView(tv);

		setupSquishyFontView(R.id.nav_altitude_value, 190, 20);
		setupSquishyFontView(R.id.nav_speed_value, 130, 20);

		// TESTING flag - set to false for debugging layout
		if (true)
			setupColors();
		
		resetData();
	}

	private void startServices() {
		// TODO - this becomes a RouteManagerService, or
		// whatever we call it. For now, spin up the AltimeterService
        Intent altIntent = new Intent(this, AltimeterService.class);
        altIntent.putExtra(AltimeterService.USE_MOCK_DATA, false);
        startService(altIntent);	
        Intent navIntent = new Intent(this, NavigationService.class);
        startService(navIntent);
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
	
	protected void resetData() {
		mCurAltitudeRawValue = 0;
		mCurAltitudeDisplayValue = 0;
		mCurAltitudeTimestampMillis = 0;
		
		// todo, update UI?
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
		
		updateUI();

	}

	@Override
	protected void initUsbDevice(UsbDevice device) {
		super.initUsbDevice(device);
	}

	protected void updateAltitudeUI() {
		// main value
		final String altString = Integer.toString(mCurAltitudeDisplayValue);
		mAltitudeValue.setText(altString);
	}

	protected Boolean dataTimestampIsOld(long dataTimestampMillis) {
		
		if (dataTimestampMillis == 0)
			return true;
		
		long elapsedMilllis = curDataTimestamp() - dataTimestampMillis;
		
		if (elapsedMilllis > MAX_DATA_LIFESPAN_MILLIS)
			return true;
		
		// must be ok
		return false;
	}

	protected void updateStatusLights() {
		
		// altitude
		// todo: invalid, out of range, old

		if (mCurAltitudeDisplayValue == 0) {
			// no data
			mStatusButtonALT.setBackground(mStatusButtonBackgroundRed);
		}
		else if (dataTimestampIsOld(mCurAltitudeTimestampMillis)) {
			// old data
			mStatusButtonALT.setBackground(mStatusButtonBackgroundYellow);
		} else {
			// aok
			mStatusButtonALT.setBackground(mStatusButtonBackgroundGreen);
		}
	}

	protected void updateUI() {
		updateStatusLights();
		updateAltitudeUI();
	}
	
	protected int calcDisplayAltitudeFromRaw(float rawAltitude) {
		// convert.  do units here too
		return (int) rawAltitude;
	}
	
	protected long curDataTimestamp() {
		return System.currentTimeMillis();
	}
	
	protected void setRawAltitude(float altValue) {
		mCurAltitudeRawValue = altValue;
		int newAltDisplay = calcDisplayAltitudeFromRaw(altValue);
		
		if (newAltDisplay != mCurAltitudeDisplayValue) {
			// changed
			mCurAltitudeDisplayValue = newAltDisplay;
			updateUI();
		}
		
		// changed or not, take a timestamp
		mCurAltitudeTimestampMillis = curDataTimestamp();
		
		updateStatusLights();
	}
	
	public void onAltitudeUpdate(float altValue) {
		final float currAlt = altValue;
		runOnUiThread(new Runnable() {
			public void run() {
				setRawAltitude(currAlt);
			}
		});

	}

	@Override
	public void onAltitudeError(String error) {
		// TODO Auto-generated method stub
		
	}
}
