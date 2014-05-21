package com.vulcan.flightlogger.logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.vulcan.flightlogger.altimeter.AltimeterService;
import com.vulcan.flightlogger.altimeter.AltitudeUpdateListener;
import com.vulcan.flightlogger.geo.NavigationService;
import com.vulcan.flightlogger.geo.TransectUpdateListener;
import com.vulcan.flightlogger.geo.data.TransectStatus;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class LoggingService extends Service implements AltitudeUpdateListener,
		TransectUpdateListener {

	private static final long LOGGING_FREQUENCY_SECS = 30;
	private final String mLoggingDirName = "flightlogs";
	private File mLogDir = null;
	protected final String TAG = this.getClass().getSimpleName();
	private File mCurrLogfileName;
	private LogEntry mCurrLogEntry;

	protected NavigationService mNavigationService;
	protected AltimeterService mAltimeterService;

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

	public void startLog(String transectName) {
		startLog(transectName, LOGGING_FREQUENCY_SECS);
	}

	public void startLog(String transectName, float logFrequency) {
		mCurrLogEntry = new LogEntry();
		closeCurrentLog();
		mCurrLogfileName = createLogFile(transectName);
		mLogData = true;
		logData((long) logFrequency);
	}

	public void closeCurrentLog() {
		mLogData = false;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "starting logging service");
		createFlightLogDirectory();
		bindServices();
		return START_STICKY;
	}

	public boolean stopService(Intent intent) {
		closeCurrentLog();
		return super.stopService(intent);
	}

	private void writeEntry(LogEntry entry, String timestamp) {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
					openFileOutput(mCurrLogfileName.getAbsolutePath(),
							Context.MODE_PRIVATE));
			outputStreamWriter.write(entry.toLogEntry(timestamp));
			outputStreamWriter.close();
		} catch (IOException e) {
			Log.e(TAG, "Log write failed: " + e.toString());
		}
	}

	// TODO - If needed, consider write into a buffer, and flush it every 20 entries or so.
	private void logData(long logFrequencySecs) {
		final long logFrequencyMillis = logFrequencySecs * 1000;
		final Date startTime = new Date();
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.US);

		new Thread() {
			public void run() {
				while (mLogData) {
					try {
						synchronized (mCurrLogEntry) {
							// synchronized copy constructor to keep it atomic
							LogEntry entry = new LogEntry(mCurrLogEntry);
							writeEntry(entry, df.format(startTime));
						}
						Thread.sleep(logFrequencyMillis);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	/*
	 * TODO - verify uniqueness of name, and verify that the constructed
	 * filename indeed can be written to by creating a file on the filesystem
	 */
	private File createLogFile(String transectName) {
		File logFile = null;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.US);
		String osFriendlyName = transectName.replaceAll(" ", "_").toLowerCase(
				Locale.US);
		String logName = String.format("%s-%s", sdf.format(cal.getTime()),
				osFriendlyName);
		if (mLogDir.isDirectory() && mLogDir.canWrite()) {
			logFile = new File(mLogDir, logName);
			if (!logFile.exists()) {
				try {
					logFile.createNewFile();
				} catch (IOException e) {
					Log.e(TAG, e.getLocalizedMessage());
				}
			}
		}
		return logFile;
	}

	private boolean createFlightLogDirectory() {
		// Kinda crazy, will only create a directory if you use a
		// string constructor, as opposed to (File, String)
		String dirPath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
				+ File.separator + mLoggingDirName;
		File flightLogDir = new File(dirPath);
		if (!flightLogDir.exists()) {
			flightLogDir.mkdirs();
		}
		return flightLogDir.exists();
	}

	@Override
	public void onRouteUpdate(TransectStatus status) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAltitudeUpdate(float altValueInMeters) {
		// TODO Auto-generated method stub

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
