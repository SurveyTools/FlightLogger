package com.vulcan.flightlogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Layout;

import com.vulcan.flightlogger.FileChooserDialog.FileChooserListener;
import com.vulcan.flightlogger.geo.RouteChooserDialog.RouteChooserListener;
import com.vulcan.flightlogger.geo.TransectChooserDialog.TransectChooserListener;
import com.vulcan.flightlogger.geo.GPSUtils;
import com.vulcan.flightlogger.geo.RouteChooserDialog;
import com.vulcan.flightlogger.geo.TransectChooserDialog;
import com.vulcan.flightlogger.geo.data.Route;
import com.vulcan.flightlogger.geo.data.Transect;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.ImageButton;
import android.view.View.OnClickListener;

public class CourseSettingsActivity extends FragmentActivity implements OnClickListener, FileChooserListener, RouteChooserListener, TransectChooserListener {

	private View mFileBigButton;
	private View mRouteBigButton;
	private ListView mTransectList;

	private ImageView mFileIcon;
	private ImageView mRouteIcon;
	private ImageView mTransectIcon;
	
	private TextView mFile;
	private TextView mRoute;
	private TextView mTransect;

	private Button mCancelButton;
	private Button mOkButton;

	private CourseInfoIntent mOriginalData;
	private CourseInfoIntent mWorkingData;

	// objects based on mWorkingData
	private File mCurGpxFile;
	private List<Route> mCurRoutes;		
	private Route mCurRoute;
    private List<Transect> mCurTransects;
    private Transect mCurTransect;
    
	private ArrayList<Transect> mTransectArray;
	private TransectAdapter mTransectAdapter;

	private static final String LOGGER_TAG = "CourseSettingsActivity";
	private final int FS_DIALOG_STYLE = DialogFragment.STYLE_NORMAL;
	private final int FS_DIALOG_THEME = android.R.style.Theme_NoTitleBar_Fullscreen;
	

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

		setContentView(R.layout.flight_settings);

		// IMMERSIVE_MODE note: setting the theme here didn't help setTheme(android.R.style.Theme_NoTitleBar);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mFileBigButton = findViewById(R.id.fs_file_big_button);
		mRouteBigButton = findViewById(R.id.fs_route_big_button);
		mTransectList = (ListView) findViewById(R.id.fs_transect_list);
		
		mFileIcon = (ImageView) findViewById(R.id.fs_file_icon);
		mRouteIcon = (ImageView) findViewById(R.id.fs_route_icon);
		// TODO_FS_WIP mTransectIcon = (ImageView) findViewById(R.id.fs_transect_icon);

		mFile = (TextView) findViewById(R.id.fs_file_value);
		mRoute = (TextView) findViewById(R.id.fs_route_value);
		// TODO_FS_WIP mTransect = (TextView) findViewById(R.id.fs_transect_value);

		mCancelButton = (Button) findViewById(R.id.fs_cancel_button);
		mOkButton = (Button) findViewById(R.id.fs_ok_button);

		mOriginalData = getIntent().getParcelableExtra(CourseInfoIntent.INTENT_KEY);
		mWorkingData = new CourseInfoIntent(mOriginalData); // clone
		mWorkingData.debugDump();
		
		updateCurFileFromWorkingData();

		setupButtons();
		setupColors();
		updateDataUI();

		// IMMERSIVE_MODE note: onSystemUiVisibilityChange hook didn't work
		// IMMERSIVE_MODE note: delayed change didn't help: mImmersiveRunnable = new Runnable() { @Override public void run() { immerseMe("runnable delayed"); } };
		// IMMERSIVE_MODE
		immerseMe("dlog OnCreate");

		// auto-open if we have nothing
		// TESTING if (true) return;
		if (!mWorkingData.hasFile())
			doChooseFile();
	}

	protected void setupButtons() {

		mTransectList.setOnItemClickListener(new AdapterView.OnItemClickListener()  {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					mTransectList.setItemChecked(position, true);

					// TODO_FS_WIP (ugly)
					Transect transect = mTransectArray.get(position);
					mWorkingData.mTransectName = transect.mName;
					mWorkingData.mTransectDetails = transect.getDetailsName();
		    }
		});
		
		mFileBigButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doChooseFile();
			}
		});

		mRouteBigButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doChooseRoute();
			}
		});

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
		if (v == mTransectIcon) {
			// TRANSECT
			// TODO_FS_WIP doChooseTransect();
		}
	}
	
	protected void updateTransectListUI() {

		if (mCurTransects != null) {
			// TODO_FS_WIP eval
			mTransectArray = new ArrayList<Transect>(mCurTransects);
			mTransectAdapter = new TransectAdapter(this, mTransectArray);
			
			mTransectList.setAdapter(mTransectAdapter);
	    	mTransectList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			
    		// TODO_FS_WIP, update our actual list

			if (mTransectArray.size() > 0) {
				// find the cur transect
				for(int i=0;i<mTransectArray.size();i++) {
					if (mTransectArray.get(i) == mCurTransect) {
						// match!
						mTransectList.setItemChecked(i, true);
					}					
				}
			}
		}
	}
	
	protected void updateDataUI() {
		mFile.setText(mWorkingData.getShortFilename());
		mRoute.setText(mWorkingData.getShortRouteName());
		// TODO_FS_WIP mTransect.setText(mWorkingData.getFullTransectName());
		
		int numRoutes = (mCurRoutes == null) ? 0 : mCurRoutes.size();
		int numTransects = (mCurTransects == null) ? 0 : mCurTransects.size();
		
		// TODO_FS_WIP mRouteIcon.setEnabled(numRoutes > 1);
		// TODO_FS_WIP mTransectIcon.setEnabled(numTransects > 1);
		
		updateTransectListUI();
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
			setColorforViewWithID(R.id.fs_header, R.color.fs_header_color);
			setClearColorforViewWithID(R.id.fs_file_and_route_wrapper);
			setClearColorforViewWithID(R.id.fs_transect_body_wrapper);
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
			// TODO_FS_WIP setClearColorforViewWithID(R.id.fs_transect_wrapper);
			// TODO_FS_WIP setClearColorforViewWithID(R.id.fs_transect_icon);
			setClearColorforViewWithID(R.id.fs_transect_label);
			// TODO_FS_WIP setClearColorforViewWithID(R.id.fs_transect_value);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			immerseMe("onWindowFocusChanged");
		}
	}

    protected void clearCurTransect() {
    	mCurTransect = null;
    }
    
    protected void clearCurRouteAndDependencies() {
    	mCurRoute = null;
    	mCurTransects = null;
    	clearCurTransect();
    }
    
    protected void clearCurFileAndDependencies() {
    	mCurGpxFile = null;
    	mCurRoutes = null;
    	clearCurRouteAndDependencies();
    }
    
	protected void updateCurTransectFromWorkingData() {
		clearCurTransect();
    	if (mCurTransects != null) {
			// get the cur transect
    		if (mWorkingData.hasTransect()) {
    			// get the specified one
    			mCurTransect = GPSUtils.findTransectInList(mWorkingData.mTransectName, mCurTransects);
    		} else {
    			// get the default
    			mCurTransect = GPSUtils.getDefaultTransectFromList(mCurTransects);
    			
    			// update our working data
    			mWorkingData.mTransectName = (mCurTransect == null) ? null : mCurTransect.mName;
    			mWorkingData.mTransectDetails =  (mCurTransect == null) ? null : mCurTransect.getDetailsName();
    		}
    		
    		// TODO_FS_WIP, update mTransectList?
		}
	}

	protected void updateCurRouteFromWorkingData() {
		clearCurRouteAndDependencies();
    	if (mCurRoutes != null) {
			// get the route
    		if (mWorkingData.hasRoute()) {
    			// get the specified one
    			mCurRoute = GPSUtils.findRouteByName(mWorkingData.mRouteName, mCurRoutes);
    		} else {
    			// get the default
    			mCurRoute = GPSUtils.getDefaultRouteFromList(mCurRoutes);
 
    			// update our working data
    			mWorkingData.mRouteName = (mCurRoute == null) ? null : mCurRoute.mName;
    		}

			// get the transects
		    mCurTransects = GPSUtils.parseTransects(mCurRoute);
		    
		    // cascade
		    updateCurTransectFromWorkingData();
		}
	}

	protected void updateCurFileFromWorkingData() {
		clearCurFileAndDependencies();
		if (mWorkingData.hasFile()) {
			// get the file
			mCurGpxFile = new File(mWorkingData.mGpxName);

			// get the routes
			mCurRoutes = GPSUtils.parseRoute(mCurGpxFile);			
			
			// cascade
			updateCurRouteFromWorkingData();
		}
	}

	// from a chooser...
	protected void setFile(String gpxFilename) {
		mWorkingData.clearFileDataAndDependencies();
		mWorkingData.mGpxName = gpxFilename;

		updateCurFileFromWorkingData();
		updateDataUI();
	}

	// from a chooser...
	protected void setRoute(String routeName) {
		// file and route list are ok... route and transects need to be updated
		mWorkingData.clearRouteDataAndDependencies();
		mWorkingData.mRouteName = routeName;

		updateCurRouteFromWorkingData();
		updateDataUI();
	}

	// from a chooser...
	protected void setTransect(String transectName, String transectDetails) {
		mWorkingData.mTransectName = transectName;
		mWorkingData.mTransectDetails = transectDetails;

		updateCurTransectFromWorkingData();
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

	public void doChooseFile() {
		FragmentManager fm = getSupportFragmentManager();
		String startingDir = calcDownloadsDirectoryPath();
		FileChooserDialog dlog = FileChooserDialog.newInstance("Choose a GPX File", FS_DIALOG_STYLE, FS_DIALOG_THEME, startingDir, 0);
		dlog.show(fm, FileChooserDialog.FILE_CHOOSER_DIALOG_KEY);
		// IMMERSIVE_MODE NOTE: getWindow().getDecorView().postDelayed(mImmersiveRunnable, 500);
	}

	public void doChooseRoute() {
		FragmentManager fm = getSupportFragmentManager();
		RouteChooserDialog dlog = RouteChooserDialog.newInstance("Choose a Route", FS_DIALOG_STYLE,FS_DIALOG_THEME, mWorkingData.mGpxName, mWorkingData.mRouteName);
		dlog.show(fm, "choose_route");
	}

	public void doChooseTransect() {
		FragmentManager fm = getSupportFragmentManager();
		TransectChooserDialog dlog = TransectChooserDialog.newInstance("Choose a Transect", FS_DIALOG_STYLE, FS_DIALOG_THEME, mWorkingData.mGpxName, mWorkingData.mRouteName, mWorkingData.mTransectName, mWorkingData.mTransectDetails);
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
