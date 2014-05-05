package com.vulcan.flightlogger.altimeter;

import com.vulcan.flightlogger.R;
import com.vulcan.flightlogger.altimeter.AltimeterService.LocalBinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

public class LaserAltimeterActivity extends Activity implements
		AltitudeUpdateListener {

	private AltimeterService mAltimeterService;
    private boolean mBound = false;
	private TextView mAltitudeView;
	
	
    /** 
     * Defines callbacks for service binding, passed to bindService()
     * For local binds, this is where we will add and remove listeners
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mAltimeterService = (AltimeterService)binder.getService();
            mAltimeterService.registerListener(LaserAltimeterActivity.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	mAltimeterService.unregisterListener(LaserAltimeterActivity.this);
            mBound = false;
        }
    };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_altimeter);
		mAltitudeView = (TextView) findViewById(R.id.currAltitudeView);
	}
	
    protected void onStart() {
        super.onStart();
        // Bind to AltimeterService - we get a callback on the
        // binding which gives us a reference to the service
        Intent intent = new Intent(this, AltimeterService.class);
        this.bindService(intent, mConnection, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service, remove listener. We'll restart when we 
        // come back into the app     
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

	public void showAltimeterFailureAlert(String errMsg) {
		new AlertDialog.Builder(this)
				.setTitle("Altimeter Data Error")
				.setMessage(
						"The Altimeter failed to initialize: " + errMsg + "\nPress OK to try again")
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								initAltimeterCommunication();
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// do nothing
							}
						}).setIcon(android.R.drawable.ic_dialog_alert).show();
	}

	@Override
	public void onAltitudeUpdate(float altValue) {
		final String currAlt = Float.toString(altValue);
		runOnUiThread(new Runnable() {
			public void run() {
				mAltitudeView.setText(currAlt);
			}
		});
		
	}
	
	private void initAltimeterCommunication()
	{
        // Bind to AltimeterService - we get a callback on the
        // binding which gives us a reference to the service
        Intent intent = new Intent(this, AltimeterService.class);
        bindService(intent, mConnection, 0);
	}

	@Override
	public void onAltitudeError(String error) {
		// TODO Auto-generated method stub
		
	}

}
