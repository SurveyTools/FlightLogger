package com.vulcan.flightlogger.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
import com.vulcan.flightlogger.geo.NavigationService;
import com.vulcan.flightlogger.geo.TransectUpdateListener;
import com.vulcan.flightlogger.geo.data.Transect;
import com.vulcan.flightlogger.geo.data.TransectStatus;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaScannerConnection;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class LoggingService extends Service implements AltitudeUpdateListener,
		TransectUpdateListener {
	private static final long LOGGING_FREQUENCY_SECS = 10;
	private final String mLoggingDirName = "flightlogs";
	private LogWriter mLogFormatter;
	private File mLogDir = null;
	protected final String TAG = this.getClass().getSimpleName();
	private File mCurrLogfileName;
	private LogEntry mCurrLogEntry;

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
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mNavigationService.unregisterListener(LoggingService.this);
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
	private boolean mLogData;

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

	public void startLog(Transect transect) {
		startLog((transect == null) ? null : transect.calcBaseFilename(), LOGGING_FREQUENCY_SECS);
	}

	public void startLog(String transectName) {
		startLog(transectName, LOGGING_FREQUENCY_SECS);
	}

	public void stopLog() 
	{
		// clone the File object so we can run it in a separate thread
		File currLog = new File(mCurrLogfileName.getAbsolutePath());
		convertLogToGPXFormat(currLog);
		closeCurrentLog();
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
						final FileOutputStream fos = new FileOutputStream(gpxFile, true);
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
		return mLogData && (mCurrLogfileName != null);
	}

	public void startLog(String transectName, float logFrequency) {
		if (mLogData == false)
		{
			stopLog();
			mCurrLogEntry = new LogEntry();
			mCurrLogfileName = createCSVLogFile(transectName);
			mLogData = true;
			logData((long) logFrequency);
		}
	}

	public void closeCurrentLog() {
		mLogData = false;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mLogFormatter == null)
			mLogFormatter = new LogWriter();
		Log.d(TAG, "starting logging service");
		createFlightLogDirectory();
		bindServices();
		return START_STICKY;
	}

	public boolean stopService(Intent intent) {
		closeCurrentLog();
		return super.stopService(intent);
	}

	// TODO - If needed, consider write into a buffer, and flush it every 20 entries or so.
	private void logData(long logFrequencySecs) {
		final long logFrequencyMillis = logFrequencySecs * 1000;
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.US);

		new Thread() {
			public void run() {
				while (mLogData) {
					try {
						LogEntry entry;
						synchronized (mCurrLogEntry) {
							// synchronized copy constructor to keep it atomic
							entry = new LogEntry(mCurrLogEntry);
						}
						writeEntry(entry, df.format(new Date()));
						Thread.sleep(logFrequencyMillis);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	private void writeEntry(LogEntry entry, String timestamp) {
		try {
			String csvEntry = mLogFormatter.writeGenericCSVRecord(
					timestamp,
					Double.toString(entry.mLat),
					Double.toString(entry.mLon), 
					Float.toString(entry.mAlt),
					Float.toString(entry.mSpeed));
		        FileOutputStream fos = new FileOutputStream(this.mCurrLogfileName, true);
		        PrintStream writer = new PrintStream(fos);
		        writer.append(csvEntry);
		        writer.flush();
		        writer.close();
		    } catch (FileNotFoundException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		    	Log.e(TAG, "Log write failed: " + e.toString());
		    }
	}

	private File createCSVLogFile(String transectName) {
		File logFile = null;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-k.m.s", Locale.US);
		String osFriendlyName = (transectName == null) ? "no-transect" : transectName.replaceAll(" ", "_").toLowerCase(
				Locale.US);
		String logName = String.format("%s-%s.csv", sdf.format(cal.getTime()),osFriendlyName);
		
		logFile = createLogFile(logName);
		
		return logFile;
	}

	private File createLogFile(String logName) {
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
		if (status != null && mLogData)
		{
			this.mCurrLogEntry.mLat =  status.mCurrGpsLat;
			this.mCurrLogEntry.mLon = status.mCurrGpsLon;
			this.mCurrLogEntry.mSpeed = status.mGroundSpeed;
		}

	}

	@Override
	public void onAltitudeUpdate(float altValueInMeters) {
		this.mCurrLogEntry.mAlt = altValueInMeters * GPSUtils.FEET_PER_METER;;	
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

}
