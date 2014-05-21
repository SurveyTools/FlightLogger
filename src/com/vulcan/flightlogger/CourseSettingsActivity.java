package com.vulcan.flightlogger;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import com.vulcan.flightlogger.geo.GPSUtils;
import com.vulcan.flightlogger.geo.RouteListActivity;
import com.vulcan.flightlogger.geo.TransectListActivity;
import com.vulcan.flightlogger.geo.data.Route;
import com.vulcan.flightlogger.geo.data.Transect;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.content.Intent;
import android.content.res.Resources;
import android.widget.ImageButton;
import android.view.View.OnClickListener;

import org.apache.commons.io.FilenameUtils;

public class CourseSettingsActivity extends Activity implements OnClickListener {

	private ImageButton mFileButton;
	private ImageButton mRouteButton;
	private ImageButton mTransectButton;

	private TextView mFile;
	private TextView mRoute;
	private TextView mTransect;

	private Button mCancelButton;
	private Button mOkButton;

	private CourseInfoIntent mOriginalData;
	private CourseInfoIntent mWorkingData;

	static final int SELECT_GPX_FILE = 10001;
	static final int SELECT_GPX_ROUTE = 10002;
	static final int SELECT_GPX_TRANSECT = 10003;

	private static final String LOGGER_TAG = "CourseSettingsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.flight_settings);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mFileButton = (ImageButton) findViewById(R.id.fs_file_icon);
		mRouteButton = (ImageButton) findViewById(R.id.fs_route_icon);
		mTransectButton = (ImageButton) findViewById(R.id.fs_transect_icon);

		mFile = (TextView) findViewById(R.id.fs_file_value);
		mRoute = (TextView) findViewById(R.id.fs_route_value);
		mTransect = (TextView) findViewById(R.id.fs_transect_value);

		mCancelButton = (Button) findViewById(R.id.fs_cancel_button);
		mOkButton = (Button) findViewById(R.id.fs_ok_button);

		mOriginalData = getIntent().getParcelableExtra(CourseInfoIntent.INTENT_KEY);
		mWorkingData = new CourseInfoIntent(mOriginalData); // clone
		mWorkingData.debugDump();

		setupButtons();
		setupColors();
		updateDataUI();
		
		// auto-open if we have nothing
		if (mWorkingData.isEmpty())
			onClick(mFileButton);
	}

	protected void setupButtons() {
		mFileButton.setOnClickListener(this);
		mRouteButton.setOnClickListener(this);
		mTransectButton.setOnClickListener(this);

		mCancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finishWithCancel();
			}
		});

		mOkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finishWithDone();
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		if (v == mFileButton) {
			// FILE
			Intent intent = new Intent(this, FileBrowser.class);
			intent.putExtra(TransectListActivity.GPX_FILE_NAME_STRING_KEY, mWorkingData.mGpxName);
			this.startActivityForResult(intent, SELECT_GPX_FILE);
		} else if (v == mRouteButton) {
			// ROUTE
			Intent intent = new Intent(this, RouteListActivity.class);
			intent.putExtra(TransectListActivity.GPX_FILE_NAME_STRING_KEY, mWorkingData.mGpxName);
			this.startActivityForResult(intent, SELECT_GPX_ROUTE);
		} else if (v == mTransectButton) {
			// TRANSECT
			Intent intent = new Intent(this, TransectListActivity.class);
			intent.putExtra(TransectListActivity.GPX_FILE_NAME_STRING_KEY, mWorkingData.mGpxName);
			intent.putExtra(TransectListActivity.ROUTE_NAME_STRING_KEY, mWorkingData.mRouteName);
			intent.putExtra(TransectListActivity.TRANSECT_NAME_STRING_KEY, mWorkingData.mTransectName);
			intent.putExtra(TransectListActivity.TRANSECT_DETAILS_STRING_KEY, mWorkingData.mTransectDetails);
			this.startActivityForResult(intent, SELECT_GPX_TRANSECT);
		}
	}

	protected void updateDataUI() {
		mFile.setText(mWorkingData.getShortFilename());
		mRoute.setText(mWorkingData.getShortRouteName());
		mTransect.setText(mWorkingData.getFullTransectName());
	}

	private void finishWithCancel() {
		Intent intent = getIntent();
		intent.putExtra(CourseInfoIntent.INTENT_KEY, mOriginalData);
		this.setResult(RESULT_CANCELED, intent);
		finish();
	}

	private void finishWithDone() {
		Intent intent = getIntent();
		intent.putExtra(CourseInfoIntent.INTENT_KEY, mWorkingData);
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
			setColorforViewWithID(R.id.fs_background_wrapper, R.color.fs_background_color);
			setColorforViewWithID(R.id.fs_footer, R.color.fs_footer_color);

			// file
			setClearColorforViewWithID(R.id.fs_file_wrapper);
			setClearColorforViewWithID(R.id.fs_file_icon);
			setClearColorforViewWithID(R.id.fs_file_label);
			setClearColorforViewWithID(R.id.fs_file_value);

			// route
			setClearColorforViewWithID(R.id.fs_route_wrapper);
			setClearColorforViewWithID(R.id.fs_route_icon);
			setClearColorforViewWithID(R.id.fs_route_label);
			setClearColorforViewWithID(R.id.fs_route_value);

			// transect
			setClearColorforViewWithID(R.id.fs_transect_wrapper);
			setClearColorforViewWithID(R.id.fs_transect_icon);
			setClearColorforViewWithID(R.id.fs_transect_label);
			setClearColorforViewWithID(R.id.fs_transect_value);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
			// note: requires Android 4.4 / api level 16 & 19
			View decorView = getWindow().getDecorView();
			// alt - let the bottom bar show for the 'back' functionality
			// (SYSTEM_UI_FLAG_HIDE_NAVIGATION)
			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}
	
	protected void setFile(String gpxFilename) {
		mWorkingData.mGpxName = gpxFilename;
		
	    // defaults
	    Route defaultRoute = GPSUtils.getDefaultRouteFromFilename(gpxFilename);
	    Transect defaultTransect = GPSUtils.getDefaultTransectFromRoute(defaultRoute);

	    // set the subordinates
	    mWorkingData.mRouteName = defaultRoute.mName;
		mWorkingData.mTransectName = defaultTransect.mName;
		mWorkingData.mTransectDetails = defaultTransect.getDetailsName();
		
		updateDataUI();
	}

	protected void setRoute(String routeName) {
		mWorkingData.mRouteName = routeName;
		
	    // defaults (matching the target route above)
	    File gpxFileObj = new File(mWorkingData.mGpxName);
	    List<Route> routes = GPSUtils.parseRoute(gpxFileObj);
	    Route routeObj = GPSUtils.findRouteByName(routeName, routes);
	    Transect defaultTransect = GPSUtils.getDefaultTransectFromRoute(routeObj);

	    // set the subordinates
		mWorkingData.mTransectName = defaultTransect.mName;
		mWorkingData.mTransectDetails = defaultTransect.getDetailsName();
		
		updateDataUI();
	}

	protected void setTransect(String transectName, String transectDetails) {
		mWorkingData.mTransectName = transectName;
		mWorkingData.mTransectDetails = transectDetails;
		
		updateDataUI();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case SELECT_GPX_FILE:
				setFile(data.getStringExtra(FileBrowser.FILE_NAME_STRING_KEY));
				break;

			case SELECT_GPX_ROUTE:
				setRoute(data.getStringExtra(RouteListActivity.ROUTE_NAME_STRING_KEY));
				break;

			case SELECT_GPX_TRANSECT:
				setTransect(data.getStringExtra(TransectListActivity.TRANSECT_NAME_STRING_KEY), data.getStringExtra(TransectListActivity.TRANSECT_DETAILS_STRING_KEY));
				break;
			}
		}
	}
}
