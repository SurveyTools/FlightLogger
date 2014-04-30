package com.vulcan.flightlogger.altimeter;

import java.util.ArrayList;
import java.util.Arrays;

import slickdevlabs.apps.usb2seriallib.AdapterConnectionListener;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial;
import slickdevlabs.apps.usb2seriallib.USB2SerialAdapter;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.BaudRate;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.DataBits;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.ParityOption;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.StopBits;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class AltimeterService 
	extends Service 
	implements AdapterConnectionListener, USB2SerialAdapter.DataListener
{
	private final static float METERS_CONVERSION = (float) 3.28084;
	
	private final IBinder mBinder = new LocalBinder();
	private final ArrayList<AltitudeUpdateListener> mListeners = new ArrayList<AltitudeUpdateListener>();
	
	private float mCurrentAltitude;
	private USB2SerialAdapter mSelectedAdapter;

	public class LocalBinder extends Binder {
        public AltimeterService getService() {
            return AltimeterService.this;
        }
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
    public void onCreate() {
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
			for (AltitudeUpdateListener listener : mListeners)
			{
				listener.onAltitudeUpdate(mCurrentAltitude);
			}
		}
	}

	private boolean validateDataPayload(byte[] data) {
		// verify that the carriage return is the terminating character
		boolean isValid = ((int)data[data.length-1] == 13) && (data.length == 10);
		if (isValid)
		{
			byte [] stripMeters = Arrays.copyOfRange(data, 0, data.length-2);
			float feet = (float) (Float.parseFloat(new String(stripMeters)) * METERS_CONVERSION);
			mCurrentAltitude = feet;
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
