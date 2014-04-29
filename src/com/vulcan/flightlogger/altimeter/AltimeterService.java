package com.vulcan.flightlogger.altimeter;

import java.util.Arrays;

import slickdevlabs.apps.usb2seriallib.AdapterConnectionListener;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial;
import slickdevlabs.apps.usb2seriallib.USB2SerialAdapter;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.BaudRate;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.DataBits;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.ParityOption;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.StopBits;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class AltimeterService 
	extends IntentService 
	implements AdapterConnectionListener, USB2SerialAdapter.DataListener
{
	private String mCurrentAltitude;
	private USB2SerialAdapter mSelectedAdapter;

	public AltimeterService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		initSerialCommunication();

	}
	
	private void initSerialCommunication() {
		SlickUSB2Serial.initialize(this);
		SlickUSB2Serial.connectProlific(AltimeterService.this);
	}

	@Override
	public void onDataReceived(int arg0, byte[] data) {
		if (validateDataPayload(data))
		{
			//mCurrentAltitude = new String(data);
			Log.d("Altimeter", mCurrentAltitude);
//			runOnUiThread(new Runnable() {
//				public void run() {
//					mAltitudeView.setText(mCurrentAltitude);
//				}
//			});
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
		
	}
}
