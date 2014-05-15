package com.vulcan.flightlogger.altimeter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.vulcan.flightlogger.geo.GPSUtils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import slickdevlabs.apps.usb2seriallib.AdapterConnectionListener;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial;
import slickdevlabs.apps.usb2seriallib.USB2SerialAdapter;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.BaudRate;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.DataBits;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.ParityOption;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.StopBits;

public class AltimeterService extends Service implements
		AdapterConnectionListener, USB2SerialAdapter.DataListener {
	// used for mock data, assume a range of 300 ft +/- 20
	public final float MOCK_MAX_TOTAL_DELTA = 40/GPSUtils.METERS_PER_FOOT;
	public final float MOCK_DELTA_ALT = 2/GPSUtils.METERS_PER_FOOT;
	public final float MOCK_TARGET_ALT = 300/GPSUtils.METERS_PER_FOOT;

	// how many samples for an alt avg.
	public static final String USE_MOCK_DATA = "useMockData";
	private final int ALT_SAMPLE_COUNT = 5;
	private float mCurrentAltitudeInMeters;
	private boolean mGenMockData = false;
	private boolean mIsConnected = false;
	// TODO sample altitude
	private int[] mAltSample;

	private final String LOGGER_TAG = AltimeterService.class.getSimpleName();

	private final IBinder mBinder = new LocalBinder();
	private final ArrayList<AltitudeUpdateListener> mListeners = new ArrayList<AltitudeUpdateListener>();

	private USB2SerialAdapter mSelectedAdapter;

	public class LocalBinder extends Binder {
		public AltimeterService getService() {
			return AltimeterService.this;
		}
	}

	@Override
	// called when bound to an activity
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// called when the service is unbound (runs independent of activity)
	// TODO - this can be called multiple times (service may be wacked by OS
	// add guards for that scenario
	public int onStartCommand(Intent intent, int flags, int startId) {
		// generate mock data if the intent calls for it
		boolean useMockData = intent.getBooleanExtra(USE_MOCK_DATA, false);
		if (useMockData) {
			mGenMockData = true;
			mCurrentAltitudeInMeters = MOCK_TARGET_ALT;
			generateMockData();
		} else {
			mAltSample = new int[ALT_SAMPLE_COUNT];
		}
		Log.d(LOGGER_TAG, "starting altimeter service");
		return START_STICKY;
	}

	public boolean stopService(Intent intent) {
		mGenMockData = false;
		return super.stopService(intent);
	}

	public void generateMockData() {
		final Random rand = new Random();

		new Thread() {
			public void run() {
				while (mGenMockData == true) {
					
					// flow up and down
					mCurrentAltitudeInMeters +=  ((rand.nextFloat() * MOCK_DELTA_ALT)) - (MOCK_DELTA_ALT / 2.0f);
					
					// pin it in the range
					if (mCurrentAltitudeInMeters > (MOCK_TARGET_ALT + MOCK_MAX_TOTAL_DELTA))
						mCurrentAltitudeInMeters = MOCK_TARGET_ALT + MOCK_MAX_TOTAL_DELTA;
					else if (mCurrentAltitudeInMeters < (MOCK_TARGET_ALT - MOCK_MAX_TOTAL_DELTA))
						mCurrentAltitudeInMeters = MOCK_TARGET_ALT - MOCK_MAX_TOTAL_DELTA;
								
					// TESTING mCurrentAltitudeInMeters = MOCK_TARGET_ALT + MOCK_MAX_TOTAL_DELTA / 2.0f;
					
					sendAltitudeUpdate();
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	// called once at instantiation
	public void onCreate() {

	}

	public void registerListener(AltitudeUpdateListener listener) {
		mListeners.add(listener);
	}

	public void unregisterListener(AltitudeUpdateListener listener) {
		mListeners.remove(listener);
	}

	public void initSerialCommunication() {
		SlickUSB2Serial.initialize(this);
		SlickUSB2Serial.connectProlific(AltimeterService.this);
	}

	@Override
	public void onDataReceived(int arg0, byte[] data) {
		if (validateDataPayload(data)) {
			sendAltitudeUpdate();
		}
	}

	public void sendAltitudeUpdate() {
		for (AltitudeUpdateListener listener : mListeners) {
			listener.onAltitudeUpdate(mCurrentAltitudeInMeters);
		}
	}
	
	public boolean isConnected()
	{
		return mIsConnected;
	}

	private boolean validateDataPayload(byte[] data) {
		// verify that the carriage return is the terminating character
		boolean isValid = ((int) data[data.length - 1] == 13)
				&& (data.length == 10);
		if (isValid) {
			byte[] stripMeters = Arrays.copyOfRange(data, 0, data.length - 2);
			float meters = Float.parseFloat(new String(stripMeters));
			mCurrentAltitudeInMeters = meters;
		}

		return isValid;
	}

	@Override
	public void onAdapterConnected(USB2SerialAdapter adapter) {
		adapter.setDataListener(this);
		mIsConnected = true;
		mSelectedAdapter = adapter;
		mSelectedAdapter.setCommSettings(BaudRate.BAUD_9600,
				DataBits.DATA_8_BIT, ParityOption.PARITY_NONE,
				StopBits.STOP_1_BIT);

	}

	@Override
	public void onAdapterConnectionError(int arg0, String errMsg) {
		mIsConnected = false;
		for (AltitudeUpdateListener listener : mListeners) {
			listener.onAltitudeError(errMsg);
		}

	}

	// TODO - record the five last samples, and assign the average to
	// the current altitude value
	public int sampleAltitude() {
		// use System.arraycopy with most recent 4 values
		return 0;
	}

}
