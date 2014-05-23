package com.vulcan.flightlogger;

import java.io.File;
import java.util.List;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.vulcan.flightlogger.FileChooserDialog.FileChooserListener;
import com.vulcan.flightlogger.geo.RouteChooserDialog.RouteChooserListener;
import com.vulcan.flightlogger.geo.TransectChooserDialog.TransectChooserListener;
import com.vulcan.flightlogger.geo.GPSUtils;
import com.vulcan.flightlogger.geo.RouteChooserDialog;
import com.vulcan.flightlogger.geo.TransectChooserDialog;
import com.vulcan.flightlogger.geo.data.Route;
import com.vulcan.flightlogger.geo.data.Transect;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.content.Intent;
import android.widget.ImageButton;
import android.view.View.OnClickListener;

public class CourseSettingsActivity extends FragmentActivity implements OnClickListener, FileChooserListener, RouteChooserListener, TransectChooserListener {

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
		if (!mWorkingData.hasFile())
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
			showChooseFileDialog();
		} else if (v == mRouteButton) {
			// ROUTE
			showChooseRouteDialog();
		} else if (v == mTransectButton) {
			// TRANSECT
			showChooseTransectDialog();
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
	    mWorkingData.mRouteName = (defaultRoute == null) ? null : defaultRoute.mName;
		mWorkingData.mTransectName =  (defaultTransect == null) ? null : defaultTransect.mName;
		mWorkingData.mTransectDetails =  (defaultTransect == null) ? null : defaultTransect.getDetailsName();
		
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
		mWorkingData.mTransectName =  (defaultTransect == null) ? null : defaultTransect.mName;
		mWorkingData.mTransectDetails =  (defaultTransect == null) ? null : defaultTransect.getDetailsName();
		
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
			}
		}
	}

	protected String calcDownloadsDirectoryPath() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return dir.toString();
	}
	
	void showChooseFileDialog() {
        FragmentManager fm = getSupportFragmentManager();
        String startingDir = calcDownloadsDirectoryPath();
        FileChooserDialog dlog = FileChooserDialog.newInstance("Choose a GPX File", DialogFragment.STYLE_NORMAL, 0, startingDir, 0);
	    dlog.show(fm, FileChooserDialog.FILE_CHOOSER_DIALOG_KEY);
	}

	void showChooseRouteDialog() {
        FragmentManager fm = getSupportFragmentManager();
		RouteChooserDialog dlog = RouteChooserDialog.newInstance("Choose a Route", 2, DialogFragment.STYLE_NORMAL, 
				mWorkingData.mGpxName, mWorkingData.mRouteName);
        dlog.show(fm, "choose_route");
	}

	void showChooseTransectDialog() {
        FragmentManager fm = getSupportFragmentManager();
        TransectChooserDialog dlog = TransectChooserDialog.newInstance("Choose a Transect", 2, DialogFragment.STYLE_NORMAL, 
        		mWorkingData.mGpxName, mWorkingData.mRouteName, mWorkingData.mTransectName, mWorkingData.mTransectDetails);
        dlog.show(fm, "choose_transect");
	}

	// FileChooserListener
    public void onFileItemSelected(String filename) {
        // optional Toast.makeText(this, "file selected " + filename, Toast.LENGTH_SHORT).show();
		setFile(filename);
    }

	// RouteChooserListener
    public void onRouteItemSelected(Route route) {
        // optional Toast.makeText(this, "route selected " + route.mName, Toast.LENGTH_SHORT).show();
		setRoute(route.mName);
    }

	// TransectChooserListener
    public void onTransectItemSelected(Transect transect) {
        // optional Toast.makeText(this, "transect selected " + transect.mName, Toast.LENGTH_SHORT).show();
		setTransect(transect.mName, transect.getDetailsName());
   }
}
