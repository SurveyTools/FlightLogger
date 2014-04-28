package com.vulcan.flightlogger.altimeter;

import java.util.Arrays;

import com.vulcan.flightlogger.R;

import slickdevlabs.apps.usb2seriallib.AdapterConnectionListener;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial;
import slickdevlabs.apps.usb2seriallib.USB2SerialAdapter;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.BaudRate;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.DataBits;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.ParityOption;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.StopBits;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class LaserAltimeterActivity extends Activity implements
		AdapterConnectionListener, USB2SerialAdapter.DataListener {

	private USB2SerialAdapter mSelectedAdapter;
	private TextView mAltitudeView;
	private String mCurrentAltitude;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_altimeter);

		mAltitudeView = (TextView) findViewById(R.id.currAltitudeView);

		initSerialCommunication();
	}

	private void initSerialCommunication() {
		SlickUSB2Serial.initialize(this);
		SlickUSB2Serial.connectProlific(LaserAltimeterActivity.this);
	}

	@Override
	public void onDataReceived(int arg0, byte[] data) {
		if (validateDataPayload(data))
		{
			//mCurrentAltitude = new String(data);
			Log.d("Altimeter", mCurrentAltitude);
			runOnUiThread(new Runnable() {
				public void run() {
					mAltitudeView.setText(mCurrentAltitude);
				}
			});
		}
	}

	private boolean validateDataPayload(byte[] data) {
		// verify that the carriage return is the terminating character
		boolean isValid = ((int)data[data.length-1] == 13) && (data.length == 10);
		if (isValid)
		{
			byte [] stripMeters = Arrays.copyOfRange(data, 0, data.length-2);
			float feet = (float) (Float.parseFloat(new String(stripMeters)) * 3.28084);
			mCurrentAltitude = String.format("%f ft", feet);
//			// next, is the payload a number > 0 && < 250m?
//			isValid = (f > 0 && f < 250);
		}

		return isValid;
	}

	@Override
	public void onAdapterConnected(USB2SerialAdapter adapter) {
		adapter.setDataListener(this);
		mSelectedAdapter = adapter;
		mSelectedAdapter.setCommSettings(BaudRate.BAUD_9600,
				DataBits.DATA_8_BIT, ParityOption.PARITY_NONE,
				StopBits.STOP_1_BIT);

	}

	@Override
	public void onAdapterConnectionError(int arg0, String errMsg) {
		showAltimeterFailureAlert(errMsg);
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
								initSerialCommunication();
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

}
