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
import android.util.Log;

public class AltimeterService 
	extends Service 
	implements AdapterConnectionListener, USB2SerialAdapter.DataListener
{
	// how many samples for an alt avg.
	public final String USE_MOCK_DATA = "useMockData";
	private final int ALT_SAMPLE_COUNT = 5;
	private int mCurrentAltReading;
	private int[] mAltSample;
	
	private final static float METERS_PER_FOOT = (float) 3.28084;
	private final String LOGGER_TAG = AltimeterService.class.getSimpleName();
	
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
	// called when bound to an activity
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	// called when the service is unbound (runs independent of activity)
	// TODO - this can be called multiple times (service may be wacked by OS
	// add guards for that scenario
	public int onStartCommand (Intent intent, int flags, int startId)
	{
		// TODO - generate mock data
		boolean useMockData = intent.getBooleanExtra(USE_MOCK_DATA, false);
		if (useMockData)
		{
			// generate mock data
		}
		else
		{
			// since we are 
			mAltSample = new int[ALT_SAMPLE_COUNT];
			Log.d(LOGGER_TAG, "starting altimeter service");
			initSerialCommunication();
		}
		
		return START_STICKY;
	}
	
	// called once at instantiation
    public void onCreate() {
    	
    }
	
	private void initSerialCommunication() {
		SlickUSB2Serial.initialize(this);
		SlickUSB2Serial.connectProlific(AltimeterService.this);
	}

	@Override
	public void onDataReceived(int arg0, byte[] data) {
		if (validateDataPayload(data))
		{
			sampleAltitude();
		}
	}
	
	public void sendAltitudeUpdate()
	{
		for (AltitudeUpdateListener listener : mListeners)
		{
			listener.onAltitudeUpdate(mCurrentAltitude);
		}
		
	}

	private boolean validateDataPayload(byte[] data) {
		// verify that the carriage return is the terminating character
		boolean isValid = ((int)data[data.length-1] == 13) && (data.length == 10);
		if (isValid)
		{
			byte [] stripMeters = Arrays.copyOfRange(data, 0, data.length-2);
			float feet = (int) (Float.parseFloat(new String(stripMeters)) * METERS_PER_FOOT);
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
	
	public int sampleAltitude(){
		// use arraycopy with most recent 4 values
		return 0;
	}

}
