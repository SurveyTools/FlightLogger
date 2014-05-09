package com.vulcan.flightlogger.logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class LoggingService extends Service {

	private File mLogDir = new File(Environment.getExternalStorageDirectory()
			.getAbsolutePath());
	protected final String TAG = this.getClass().getSimpleName();
	private String mCurrLogfileName;

	private final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		public LoggingService getService() {
			return LoggingService.this;
		}
	}

	public void startLog(String transectName) {
		mCurrLogfileName = createLogFilename(transectName);
	}

	public void writeEntry(LogEntry entry) {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
					openFileOutput(mCurrLogfileName, Context.MODE_PRIVATE));
			outputStreamWriter.write(entry.toString());
			outputStreamWriter.close();
		} catch (IOException e) {
			Log.e(TAG, "Log write failed: " + e.toString());
		}
	}

	// TODO - write into a buffer, and flush it every 20 entries or so. Make
	// sure its atomic, so we don't
	private void writeEntries() {

	}

	@Override
	// called when bound to an activity
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "starting logging service");
		return START_STICKY;
	}

	public boolean stopService(Intent intent) {
		return super.stopService(intent);
	}

	/*
	 * TODO - verify uniqueness of name, and verify that the constructed
	 * filename indeed can be written to by creating a file on the filesystem
	 */
	private String createLogFilename(String transectName) {
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
		return logFile.getAbsolutePath();
	}

}
