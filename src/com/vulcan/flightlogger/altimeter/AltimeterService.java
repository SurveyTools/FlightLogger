package com.vulcan.flightlogger.altimeter;

import java.nio.charset.Charset;
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
	
	public enum DriverType {
		AGLASER, LIGHTWARE 
	}
	
	public static final int FTDI_PRODUCT_ID = 24577;
	public static final int PROLIFIC_PRODUCT_ID = 8200;
	
	// used for mock data, assume a range of 300 ft +/- 20
	public final float MOCK_MAX_TOTAL_DELTA = 40/GPSUtils.FEET_PER_METER;
	public final float MOCK_DELTA_ALT = 2/GPSUtils.FEET_PER_METER;
	public final float MOCK_TARGET_ALT = 300/GPSUtils.FEET_PER_METER;
	public static final float ALTIMETER_OUT_OF_RANGE_THRESHOLD = 99999f; // AgLaser uses 99999.99.  SEE ALTIMETER_PASSES_99999_FOR_OUT_OF_RANGE_DATA

	// how many samples for an alt avg.
	public static final String USE_MOCK_DATA = "useMockData";
	private final int ALT_SAMPLE_COUNT = 5;
	private float mCurrentAltitudeInMeters;
	private boolean mGenMockData = false;
	private boolean mIsConnected = false;
	// TODO sample altitude
	private int[] mAltSample;
	// encapsulates driver setting deltas
	private AltimeterValidator mDataValidator;

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
		if(intent != null)
		{
			// generate mock data if the intent calls for it
			boolean useMockData = intent.getBooleanExtra(USE_MOCK_DATA, false);
			if (useMockData) {
				mGenMockData = true;
				mCurrentAltitudeInMeters = MOCK_TARGET_ALT;
				generateMockData();
			} else {
				mAltSample = new int[ALT_SAMPLE_COUNT];
			}
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
		super.onCreate();
	}

	// TODO Since we have generics support, we should refactor all the 
	// service listener stuff into a base class if we run out of things to do...
	public void registerListener(AltitudeUpdateListener listener) {
		mListeners.add(listener);
	}

	public void unregisterListener(AltitudeUpdateListener listener) {
		mListeners.remove(listener);
	}

	public void initSerialCommunication() {
		SlickUSB2Serial.initialize(this);
		SlickUSB2Serial.autoConnect(AltimeterService.this);
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

	public static boolean valueIsOutOfRange(float v) {
		return v >= ALTIMETER_OUT_OF_RANGE_THRESHOLD;
	}
	
	private boolean validateDataPayload(byte[] data) {
		// verify that the carriage return is the terminating character
		boolean isValid = false;
		
		float meters = mDataValidator.parseDataPayload(data);
		
		if(meters > -1)
		{
			isValid = true;
		}
		 this.mCurrentAltitudeInMeters = meters;
		 
		return isValid;
	}

	@Override
	public void onAdapterConnected(USB2SerialAdapter adapter) {
		BaudRate adapterRate;
		if (adapter.getProductId() == FTDI_PRODUCT_ID)
		{
			Log.d(LOGGER_TAG, "Connecting with " + adapter.toString());
			mDataValidator = new LightwareDataValidator();
			adapterRate = BaudRate.BAUD_460800;
		} else //assume its a prolific driver
		{
			mDataValidator = new AglaserDataValidator();
			adapterRate = BaudRate.BAUD_9600;
		}
		adapter.setDataListener(this);
		mIsConnected = true;
		mSelectedAdapter = adapter;
		mSelectedAdapter.setCommSettings(adapterRate,
				DataBits.DATA_8_BIT, ParityOption.PARITY_NONE,
				StopBits.STOP_1_BIT);
		// after we've gotten the baud rate set, if its 
		// the lightware laser, slow the output down to 2Hz,
		// as the buffer the library allocates for the FTDI chip is 4K(?)
		if (adapter.getProductId() == FTDI_PRODUCT_ID)
		{
			Charset ascii = Charset.forName("US-ASCII");
			byte[] slowDownCmd = "#S2".getBytes(ascii);
			adapter.sendData(slowDownCmd);
		}
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
