package com.vulcan.flightlogger.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.io.FilenameUtils;

import com.vulcan.flightlogger.altimeter.AltimeterService;
import com.vulcan.flightlogger.altimeter.AltitudeUpdateListener;
import com.vulcan.flightlogger.geo.GPSUtils;
import com.vulcan.flightlogger.geo.GPSUtils.VelocityUnit;
import com.vulcan.flightlogger.geo.GPSUtils.DistanceUnit;
import com.vulcan.flightlogger.geo.NavigationService;
import com.vulcan.flightlogger.geo.TransectUpdateListener;
import com.vulcan.flightlogger.geo.data.Transect;
import com.vulcan.flightlogger.geo.data.TransectStatus;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class LoggingService extends Service implements AltitudeUpdateListener,
		TransectUpdateListener, SensorEventListener {
	private static final long LOGGING_FREQUENCY_SECS = 1;
	private final String mLoggingDirName = "flightlogs";
	private final String mGlobalLogname = "flightlog.csv";
	private final String mTransectLogname = "transects.csv";
	private LogWriter mLogFormatter;
	private File mLogDir = null;
	protected final String TAG = this.getClass().getSimpleName();
	private File mTransectLogfileName;
	private File mGlobalFlightLog;
	private LogEntry mCurrLogEntry;
	private boolean mLogTransectData;
	private boolean mLogFlightData;
	private String mCurrTransectName;
	//private Timer mLoggingTimer;

	//sensor data
	private SensorManager mSensorManager;
	Sensor mAccelerometer;
	Sensor mMagnetometer;
	private float[] mGravityData;
	private float[] mGeomagneticData;

	protected NavigationService mNavigationService;
	protected AltimeterService mAltimeterService;
	
	private final ArrayList<LoggingStatusListener> mListeners = new ArrayList<LoggingStatusListener>();

	// references to the services consumed
	private ServiceConnection mNavigationConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			com.vulcan.flightlogger.geo.NavigationService.LocalBinder binder = (com.vulcan.flightlogger.geo.NavigationService.LocalBinder) service;
			mNavigationService = (NavigationService) binder.getService();
			mNavigationService.registerListener(LoggingService.this);
			
			// Register the sensor listeners here, as we always need GPS service
		    mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		    mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		    
		    mSensorManager.registerListener(LoggingService.this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
		    mSensorManager.registerListener(LoggingService.this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
		 
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mNavigationService.unregisterListener(LoggingService.this);
			mSensorManager.unregisterListener(LoggingService.this, mAccelerometer);
			mSensorManager.unregisterListener(LoggingService.this, mMagnetometer);
		}
	};

	private ServiceConnection mAltimeterConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			com.vulcan.flightlogger.altimeter.AltimeterService.LocalBinder binder = (com.vulcan.flightlogger.altimeter.AltimeterService.LocalBinder) service;
			mAltimeterService = (AltimeterService) binder.getService();
			mAltimeterService.registerListener(LoggingService.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mAltimeterService.unregisterListener(LoggingService.this);
		}
	};

	private void bindServices() {
		if (mAltimeterService == null) {
			Intent intent = new Intent(this, AltimeterService.class);
			this.bindService(intent, mAltimeterConnection, 0);
		}
		if (mNavigationService == null) {
			Intent intent2 = new Intent(this, NavigationService.class);
			this.bindService(intent2, mNavigationConnection, 0);
		}
	}
	
	

	private final IBinder mBinder = new LocalBinder();
	
	@Override
    public void onCreate() {
		Log.d(this.getClass().getName(), "logging service started");
        // code to execute when the service is first created
    }

	@Override
	// called when bound to an activity
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public LoggingService getService() {
			return LoggingService.this;
		}
	}

	public void startTransectLog(Transect transect, DistanceUnit dUnit, VelocityUnit airUnit) {
		startLog((transect == null) ? null : transect.calcBaseFilename(), LOGGING_FREQUENCY_SECS);
	}

	public void startTransectLog(Transect transect) {
		startTransectLog(transect, DistanceUnit.METERS, VelocityUnit.NAUTICAL_MILES_PER_HOUR);
	}
	
	public void stopLog() 
	{
		// clone the File object so we can run it in a separate thread
		if (mTransectLogfileName != null)
		{
			File currLog = new File(mTransectLogfileName.getAbsolutePath());
			convertLogToGPXFormat(currLog);
			closeCurrentLog();
		}
	}
	
    public void registerListener(LoggingStatusListener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(LoggingStatusListener listener) {
        mListeners.remove(listener);
    }
	
	// convert contents referenced by an immutable File 
	public void convertLogToGPXFormat(final File currLog) {
		if(currLog.canRead())
		{
			new Thread() {
				public void run() {
					String gpxLog = FilenameUtils.removeExtension(currLog.getName()) + ".gpx";
					File gpxFile = createLogFile(gpxLog);
					try {
						final FileInputStream fis = new FileInputStream(currLog);
						final FileOutputStream fos = new FileOutputStream(gpxFile);
						final GPXLogConverter gpxLogger = new GPXLogConverter();
						gpxLogger.writeGPXFile(fis, fos);					
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	public boolean isLogging() {
		return mLogTransectData && (mTransectLogfileName != null);
	}

	public void startLog(String transectName, float logFrequency) {
		mLogTransectData = true;
		this.mCurrTransectName = transectName;
	}

	public void closeCurrentLog() {
		mLogTransectData = false;
		this.mCurrTransectName = "";
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mLogFormatter == null)
			mLogFormatter = new LogWriter();
		Log.d(TAG, "starting logging service");
		bindServices();
		logFlightData(LOGGING_FREQUENCY_SECS);
		return START_STICKY;
	}

	public boolean stopService(Intent intent) {
		closeCurrentLog();
		return super.stopService(intent);
	}
	
	// TODO - If needed, consider write into a buffer, and flush it every 20 entries or so.
	private void logFlightData(long logFrequencySecs) {
		final long logFrequencyMillis = logFrequencySecs * 1000;
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.US);
		
		mLogFlightData = true;			
		mCurrLogEntry = new LogEntry();
		
		createGlobalFlightLog();
		createTransectCSVLog();

		new Thread() {
			public void run() {
				while (mLogFlightData) {
					try {
						LogEntry entry;
						synchronized (mCurrLogEntry) {
							// synchronized copy constructor to keep it atomic
							entry = new LogEntry(mCurrLogEntry);
						}
						String entryTime = df.format(new Date());
						writeLogEntries(entry, entryTime);
						Thread.sleep(logFrequencyMillis);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						// mLogFightData = false;
					}
				}
			}
		}.start();
	}
	
	private void writeLogEntries(LogEntry entry, String timestamp) {
		String csvEntry = mLogFormatter.writeGenericCSVRecord(
				timestamp,
				Double.toString(entry.mLat),
				Double.toString(entry.mLon), 
				Float.toString(entry.mAlt),
				Float.toString(entry.mSpeed));
		if (this.mLogTransectData == true)
			writeLogEntry(this.mTransectLogfileName, csvEntry);
		writeLogEntry(this.mGlobalFlightLog, csvEntry);
	}
	
	private void writeLogEntry(File logName, String entry) 
	{
		try {
		        FileOutputStream fos = new FileOutputStream(logName, true);
		        PrintStream writer = new PrintStream(fos);
		        writer.append(entry);
		        writer.flush();
		        writer.close();
		    }  catch (IOException e) {
		    	Log.e(TAG, "Log write failed: " + e.toString());
		    }
	}
	
	private File createTransectCSVLog() 
	{
		File logFile = createLogFile(this.mTransectLogname);	
		return logFile;
	}
	
	private File createGlobalFlightLog() 
	{
		return createLogFile(mGlobalLogname);	
	}

	private File createLogFile(String logName) 
	{
		File logFile = null;
		logName = FilenameUtils.normalize(logName);

		if (mLogDir == null || (mLogDir.exists() == false))
		{
			createFlightLogDirectory();
		}
		if (mLogDir.isDirectory() && mLogDir.canWrite()) {
			logFile = new File(mLogDir, logName);
			if (!logFile.exists()) {
				try {
					logFile.createNewFile();
				} catch (IOException e) {
					Log.e(TAG, e.getLocalizedMessage());
				}
			}
			MediaScannerConnection.scanFile(this, new String[] {logFile.toString()}, null, null);
		}
		return logFile;
	}

	private boolean createFlightLogDirectory() {
		String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + mLoggingDirName;
		File flightLogDir = new File(dirPath);
		flightLogDir.mkdirs();
		mLogDir = flightLogDir;
		// hack to make directory visible
		return flightLogDir.exists();
	}

	@Override
	public void onRouteUpdate(TransectStatus status) {
		if (status != null && mLogTransectData)
		{
			this.mCurrLogEntry.mLat =  status.mCurrGpsLat;
			this.mCurrLogEntry.mLon = status.mCurrGpsLon;
			this.mCurrLogEntry.mSpeed = status.mGroundSpeed;
			this.mCurrLogEntry.mGpsAlt = status.mCurrGpsAlt;
		}
	}

	@Override
	public void onAltitudeUpdate(float altValueInMeters) {
		// note: we get altitude updates when we're not logging
		if (mCurrLogEntry != null)
			this.mCurrLogEntry.mAlt = altValueInMeters;
	}
	
	@Override
	public void onAltitudeError(String error) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionEnabled() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionDisabled() {
		// TODO Auto-generated method stub

	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {  }
	
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			mGravityData = event.values;
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			mGeomagneticData = event.values;
		if (mGravityData != null && mGeomagneticData != null) 
		{
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravityData, mGeomagneticData);
			if (success) 
			{
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				float azimuth = orientation[0]; // orientation contains: azimut, pitch and roll
				float pitch = orientation[1];
				float roll = orientation[2];
			}
		}
		// work on data
	}

}
