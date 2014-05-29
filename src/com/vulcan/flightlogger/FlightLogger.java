package com.vulcan.flightlogger;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.vulcan.flightlogger.altimeter.AltimeterService;
import com.vulcan.flightlogger.altimeter.AltitudeUpdateListener;
import com.vulcan.flightlogger.altimeter.SerialConsole;
import com.vulcan.flightlogger.geo.GPSDebugActivity;
import com.vulcan.flightlogger.geo.NavigationService;
import com.vulcan.flightlogger.geo.TransectUpdateListener;
import com.vulcan.flightlogger.geo.data.Transect;
import com.vulcan.flightlogger.geo.data.TransectStatus;
import com.vulcan.flightlogger.logger.LoggingService;
import com.vulcan.flightlogger.util.SquishyTextView;
import com.vulcan.flightlogger.FlightDatum;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
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
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.graphics.drawable.Drawable;

public class FlightLogger extends USBAwareActivity implements AltitudeUpdateListener, TransectUpdateListener, OnMenuItemClickListener {

	// used for identifying Activities that return results
	static final int LOAD_FLIGHT_PATH = 10011;
	static final int UI_UPDATE_TIMER_MILLIS = 500;
	static final boolean DEMO_MODE = false;
	public static final int UPDATE_IMAGE = 666;
	public static final String LOG_CLASSNAME = "FlightLogger";
	private static final String SAVED_FLIGHT_DATA_KEY = "FlightData";

	private AltimeterService mAltimeterService;
	private NavigationService mNavigationService;
	private LoggingService mLogger;
	private TransectILSView mNavigationDisplay;

	// file info
	private Button mFileIconButton;
	private TextView mFileAndRouteDisplay;
	private TextView mTransectDisplay;
	private TextView mFileMessageDisplay;
	
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
	
	private Drawable mModeButtonBorderRed;
	private Drawable mModeButtonBorderGrey;
	private Drawable mModeButtonBorderGreen;

	private int mModeButtonTextColorOnRed;
	private int mModeButtonTextColorOnGrey;
	private int mModeButtonTextColorOnGreen;

	private int mStatusTextColorRed;
	private int mStatusTextColorGreen;
	private int mStatusTextColorGrey;

	private Drawable mFileIconBackgroundWhite;
	private Drawable mFileIconBackgroundRed;
	private Drawable mFileIconBackgroundYellow;
	private Drawable mFileIconBackgroundGreen;

	private TextView mStatusDisplayLeft;
	private Button mStartStopButton;
	private TextView mStatusDisplayRight;

	private int mStatusButtonTextColorOnRed;
	private int mStatusButtonTextColorOnYellow;
	private int mStatusButtonTextColorOnGreen;
	private int mStatusButtonTextColorOnGrey;
	private int mStatusButtonTextColorOnIgnore;

	// data
	protected CourseInfoIntent mFlightData;
	protected AltitudeDatum mAltitudeData;
	protected GPSDatum mGPSData;
	protected BatteryDatum mBatteryData;
	protected BoxDatum mBoxData;
	
	// STATE
	protected Transect mCurTransect;

	private Handler mUpdateUIHandler;
	private NumberFormat mDistanceStatusFormatter;


	/**
	 * Defines callbacks for local service binding, ie bindService() For local
	 * binds, this is where we will attach assign instance references, and add
	 * and remove listeners, since we have inprocess access to the class
	 * interface
	 */
	private void navServiceChanged() {
		if (mNavigationService != null)
		{
			mCurTransect = mNavigationService.mCurrTransect;
			updateUI();
		}
	}
	
	private ServiceConnection mNavigationConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			com.vulcan.flightlogger.geo.NavigationService.LocalBinder binder = (com.vulcan.flightlogger.geo.NavigationService.LocalBinder) service;
			mNavigationService = (NavigationService) binder.getService();
			mNavigationService.registerListener(FlightLogger.this);
			
			// update nav service stuff
			navServiceChanged();
			
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
	
	private ServiceConnection mLoggerConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			com.vulcan.flightlogger.logger.LoggingService.LocalBinder binder = (com.vulcan.flightlogger.logger.LoggingService.LocalBinder) service;
			mLogger = (LoggingService) binder.getService();
	//		mLogger.registerListener(FlightLogger.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			//mLogger.unregisterListener(FlightLogger.this);
		}
	};

	protected void onStart() {
		super.onStart();
		startServices();
	}

	private void bindServices() {
		Intent intent = new Intent(this, AltimeterService.class);
		this.bindService(intent, mAltimeterConnection, 0);
		Intent intent2 = new Intent(this, NavigationService.class);
		this.bindService(intent2, mNavigationConnection, 0);
		Intent intent3 = new Intent(this, LoggingService.class);
		this.bindService(intent3, mLoggerConnection, 0);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		bindServices();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		ViewGroup layout = (ViewGroup) findViewById(R.id.navscreenLeft);
		TransectILSView tv = new TransectILSView(this);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		boolean demoMode = false; // DEMO_MODE
		mAltitudeData = new AltitudeDatum(false, demoMode);
		mGPSData = new GPSDatum(false, demoMode);
		mBatteryData = new BatteryDatum(false, demoMode);
		mBoxData = new BoxDatum(false, demoMode);
		
		mFileIconButton = (Button) findViewById(R.id.nav_header_file_button);
		mFileAndRouteDisplay = (TextView) findViewById(R.id.nav_header_route_text);
		mTransectDisplay = (TextView) findViewById(R.id.nav_header_transect_text);
		mFileMessageDisplay = (TextView) findViewById(R.id.nav_header_message);

		mAltitudeDisplay = (TextView) findViewById(R.id.nav_altitude_value);
		mGroundSpeedDisplay = (TextView) findViewById(R.id.nav_speed_value);
		mNavigationDisplay = tv;

		mStatusButtonGPS = (Button) findViewById(R.id.nav_header_status_gps);
		mStatusButtonALT = (Button) findViewById(R.id.nav_header_status_alt);
		mStatusButtonBAT = (Button) findViewById(R.id.nav_header_status_bat);
		mStatusButtonBOX = (Button) findViewById(R.id.nav_header_status_box);

		// footer
		mStatusDisplayLeft = (TextView) findViewById(R.id.nav_footer_status_left);
		mStartStopButton = (Button) findViewById(R.id.nav_footer_mode_button);
		mStatusDisplayRight = (TextView) findViewById(R.id.nav_footer_status_right);

		// backgrounds for status lights
		mStatusButtonBackgroundRed = getResources().getDrawable(R.drawable.nav_status_red);
		mStatusButtonBackgroundYellow = getResources().getDrawable(R.drawable.nav_status_yellow);
		mStatusButtonBackgroundGreen = getResources().getDrawable(R.drawable.nav_status_green);
		mStatusButtonBackgroundGrey = getResources().getDrawable(R.drawable.nav_status_grey);
		mStatusButtonBackgroundIgnore = getResources().getDrawable(R.drawable.nav_status_ignore);

		// mode button
		mModeButtonBorderRed = getResources().getDrawable(R.drawable.roundedbutton_red);
		mModeButtonBorderGrey = getResources().getDrawable(R.drawable.roundedbutton_grey);
		mModeButtonBorderGreen = getResources().getDrawable(R.drawable.roundedbutton_green);

		// file button
		mFileIconBackgroundWhite = getResources().getDrawable(R.drawable.filefolder);
		mFileIconBackgroundRed = getResources().getDrawable(R.drawable.filefolder_red);
		mFileIconBackgroundYellow = getResources().getDrawable(R.drawable.fileicon_yellow);
		mFileIconBackgroundGreen = getResources().getDrawable(R.drawable.fileicon_green);

		// formatters
		mDistanceStatusFormatter = new DecimalFormat("#0.00");

		tv.setLayoutParams(lp);
		layout.addView(tv);

		setupSquishyFontView(R.id.nav_altitude_value, 190, 20);
		setupSquishyFontView(R.id.nav_speed_value, 130, 20);

		resetData();

		if (savedInstanceState != null) {
			mFlightData = savedInstanceState.getParcelable(SAVED_FLIGHT_DATA_KEY);
			
			if (mNavigationService != null)
				mCurTransect = mNavigationService.mCurrTransect;
		}
		else if (DEMO_MODE) {
			mFlightData = new CourseInfoIntent("Example_survey_route.gpx", "Session 1", "Transect 3", "T03_S ~ T03_N", 0);
		} else {
			mFlightData = new CourseInfoIntent(null, null, null, null, 0);
		}

		mUpdateUIHandler = new Handler();

		setupColors();
	}

	private void startServices() {
		// TODO - this becomes a RouteManagerService, or
		// whatever we call it. For now, spin up the AltimeterService
		Intent altIntent = new Intent(this, AltimeterService.class);
		//altIntent.putExtra(AltimeterService.USE_MOCK_DATA, true);
		startService(altIntent);
		Intent navIntent = new Intent(this, NavigationService.class);
		//navIntent.putExtra(NavigationService.USE_MOCK_DATA, true);
		startService(navIntent);
		Intent loggerIntent = new Intent(this, LoggingService.class);
		startService(loggerIntent);
	}

	protected void updateBatteryStatus(Intent batteryStatus) {
		if (mBatteryData.updateBatteryStatus(batteryStatus))
			updateBatteryUI();
		
		// the box status cues off of the usb fast/slow charing
		if (mBoxData.updateBoxWithBatteryStatus(batteryStatus))
			updateBoxUI();
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

		mStatusTextColorRed = getResources().getColor(R.color.nav_footer_status_text_red);
		mStatusTextColorGreen = getResources().getColor(R.color.nav_footer_status_text_green);
		mStatusTextColorGrey = getResources().getColor(R.color.nav_footer_status_text_grey);
	    
		mModeButtonTextColorOnRed = getResources().getColor(R.color.nav_footer_mode_text_over_red);
		mModeButtonTextColorOnGrey = getResources().getColor(R.color.nav_footer_mode_text_over_grey);
		mModeButtonTextColorOnGreen = getResources().getColor(R.color.nav_footer_mode_text_over_green);
	}

	protected void resetData() {
		
		mCurTransect = null;
		
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
		}
		return true;
	}
	
	protected void showError(String message) {
		Log.e(LOG_CLASSNAME,"Error: " + message);
        Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
	}
	
	protected void setLogging(boolean on) {
		if (mLogger != null) {
			mLogger.stopLog();
			if (on) {
				try {
					mLogger.startLog(mCurTransect);
				} catch(Exception e) {
					showError(e.getLocalizedMessage());
				}
			}
		}
		
		updateUI();
	}

	protected void setFlightData(CourseInfoIntent data) {
		mFlightData = data;
		
		mCurTransect = Transect.newTransect(data);

		if (mCurTransect != null) {
			
			if (mNavigationService != null) {
				mNavigationService.startNavigation(mCurTransect);
			}
			
			setLogging(false);

			updateRouteUI();
		}
	}
	
	/**
	 * Callbacks from activities that return results
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == LOAD_FLIGHT_PATH) {
			// Make sure the load activity was successful
			if (resultCode == RESULT_OK) {
				CourseInfoIntent fData = data.getParcelableExtra(CourseInfoIntent.INTENT_KEY);
				if (fData != null) {
					setFlightData(fData);
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		if(mAltimeterConnection != null)
			unbindService(mAltimeterConnection);
		if(mNavigationConnection != null)
			unbindService(mNavigationConnection);
		
		super.onDestroy();
		// TODO eval for teardown?
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
			// note: requires Android 4.4 / api level 16 & 19
			View decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
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
		
		if ((mFlightData == null) || !mFlightData.hasFile()) {
			// RED - just show the message
			mFileIconButton.setBackground(mFileIconBackgroundRed);

			mFileMessageDisplay.setVisibility(View.VISIBLE);
			mFileAndRouteDisplay.setVisibility(View.INVISIBLE);
			mTransectDisplay.setVisibility(View.INVISIBLE);
		
			mFileMessageDisplay.setText(R.string.nav_no_gpx_error_text);
			mFileAndRouteDisplay.setText(null);
			mTransectDisplay.setText(null);
		} else if (mFlightData.hasFileButNotEverythingElse()) {
			// YELLOW
			mFileIconButton.setBackground(mFileIconBackgroundYellow);

			mFileMessageDisplay.setVisibility(View.INVISIBLE);
			mFileAndRouteDisplay.setVisibility(View.VISIBLE);
			mTransectDisplay.setVisibility(View.VISIBLE);

			mFileMessageDisplay.setText(null);
			mFileAndRouteDisplay.setText(mFlightData.getShortFilename());
			mTransectDisplay.setText(mFlightData.getShortTransectName());
		} else if (mFlightData.isFullyReady()) {
			// GREEN
			mFileIconButton.setBackground(mFileIconBackgroundWhite); // optional green (kinda distracting)

			mFileMessageDisplay.setVisibility(View.INVISIBLE);
			mFileAndRouteDisplay.setVisibility(View.VISIBLE);
			mTransectDisplay.setVisibility(View.VISIBLE);

			mFileMessageDisplay.setText(null);
			mFileAndRouteDisplay.setText(mFlightData.getShortFilename());
			mTransectDisplay.setText(mFlightData.getShortTransectName());
		}
	}

	protected void updateNavigationUI() {
		mNavigationDisplay.update(mAltitudeData, mGPSData);
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
	
	// STATE
	protected boolean isTransectReady() {
		return (mCurTransect == null) ? false : mCurTransect.isValid();
	}

	// STATE
	protected boolean isLogging() {
		return (mLogger == null) ? false : mLogger.isLogging();
	}

	// STATE
	protected boolean isNavigating() {
		return (mNavigationService == null) ? false : mNavigationService.isNavigating();
	}

	protected void updateStatusRight(boolean toStart) {
		// right status
		if (mFlightData.isFullyReady()) {
			// we expect to be recording the transect
			if (mGPSData.mDataIsValid && !mGPSData.mIgnore && !mGPSData.dataIsExpired()) {
				double metersToNext = toStart ? mNavigationService.calcMetersToStart() : mNavigationService.calcMetersToEnd();
		        String kmString = (metersToNext == NavigationService.METERS_NOT_AVAILABLE ? "??" : mDistanceStatusFormatter.format(metersToNext / 1000f));
				mStatusDisplayRight.setText((toStart ? "Start" : "Stop") + " in " + kmString + " km");
			} else {
				// right status
				if (mGPSData.mIgnore) 
					mStatusDisplayRight.setText("(Ignore GPS)");
				else
					mStatusDisplayRight.setText("GPS N/A");
			}
		} else {
			// not recording a transect
			mStatusDisplayRight.setText("No Transect");
		}
		
		// CROSSTRACK_TESTING_POI - show actual crosstrack
		// mStatusDisplayRight.setText("xTrackErr: " + mGPSData.mRawCrossTrackErrorMeters);
	}
	
	protected void updateFooterUI() {

		// START/STOP button only reflects the logging state
		
		if (isLogging()) {
			// left status
			mStatusDisplayLeft.setText("** RECORDING **");
			mStatusDisplayLeft.setTextColor(mStatusTextColorGreen);
			
			// mode button
			mStartStopButton.setText("STOP LOGGING");
			mStartStopButton.setBackground(mModeButtonBorderGreen);
			mStartStopButton.setTextColor(mModeButtonTextColorOnGreen);
			mStartStopButton.setEnabled(true);

			// right status
			updateStatusRight(false);
		} else {
			// left status
			mStatusDisplayLeft.setText("Waiting to Start");
			mStatusDisplayLeft.setTextColor(mStatusTextColorRed);

			// mode button
			mStartStopButton.setText("START LOGGING");
			mStartStopButton.setBackground(mModeButtonBorderRed);
			mStartStopButton.setTextColor(mModeButtonTextColorOnRed);
			mStartStopButton.setEnabled(true);
			
			// right status
			updateStatusRight(true);
		}
		
/*		
 		work in progres...
 		
		if (isNavigating()) {
			// left status
			mStatusDisplayLeft.setText("Waiting to Start");
			mStatusDisplayLeft.setTextColor(mStatusTextColorGreen);

			// mode button
			mStartStopButton.setText("START");
			mStartStopButton.setBackground(mModeButtonBorderGreen);
			mStartStopButton.setTextColor(mModeButtonTextColorOnGreen);
			mStartStopButton.setEnabled(true);
			
			// right status
			updateStatusRight(true);
		} else if (!isTransectReady()) {
			// left status
			mStatusDisplayLeft.setText("Not Ready");
			mStatusDisplayLeft.setTextColor(mStatusTextColorRed);

			// mode button
			mStartStopButton.setText("START");
			mStartStopButton.setBackground(mModeButtonBorderGrey);
			mStartStopButton.setTextColor(mModeButtonTextColorOnGrey);
			mStartStopButton.setEnabled(false);
			
			// right status
			mStatusDisplayRight.setText("No Transect Found");
		} else {
			// left status
			mStatusDisplayLeft.setText("");
			mStatusDisplayLeft.setTextColor(mStatusTextColorGrey);
			
			// mode button
			mStartStopButton.setText("START");
			mStartStopButton.setBackground(mModeButtonBorderGrey);
			mStartStopButton.setTextColor(mModeButtonTextColorOnGrey);
			mStartStopButton.setEnabled(false);
			
			// right status
			mStatusDisplayRight.setText("");
		}
		*/
	}

	protected void updateUI() {
		updateRouteUI();
		updateNavigationUI();
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
		final float currAltitudeInMeters = altitudeInMeters;
		final long timestamp = curDataTimestamp();
		runOnUiThread(new Runnable() {
			public void run() {
				// update the altitude data (and ui if something changed)
				if (mAltitudeData.setRawAltitudeInMeters(currAltitudeInMeters, true, timestamp)) {
					updateAltitudeUI();
					updateNavigationUI();
				}
			}
		});
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
				final double crossTrackErrorMeters = status.mCrossTrackError;
				final long timestamp = curDataTimestamp();
				final boolean crosstrackValid = status.isTransectValid();
				runOnUiThread(new Runnable() {
					public void run() {
						// update the altitude data (and ui if something changed)
						if (mGPSData.setRawGroundSpeed(groundSpeed, true, crossTrackErrorMeters, crosstrackValid, timestamp)) {
							updateGPSUI();
							updateNavigationUI();
						}
					}
				});
		}
	}
	
	public boolean browseGpxFiles(View v)
	{
		// load gpx
		Intent intent = new Intent(this, CourseSettingsActivity.class);
		intent.putExtra(CourseInfoIntent.INTENT_KEY, mFlightData);
		
		this.startActivityForResult(intent, LOAD_FLIGHT_PATH);
		
		return true;
	}

	
	public void onToggleStartStop(View v) {
		setLogging(!isLogging());
	}
	
	protected void onSaveInstanceState (Bundle outState) {
		   super.onSaveInstanceState(outState);
		outState.putParcelable(SAVED_FLIGHT_DATA_KEY, mFlightData);
	}


}
