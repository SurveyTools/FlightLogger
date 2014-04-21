package com.vulcan.flightlogger;

import java.io.File;
import java.util.ArrayList;

import android.os.Environment;
import android.util.Log;

public class FilesystemMgr 
{
	private static final String LOG_DIRECTORY = "flightlogs";
	private static final String LOGGER_TAG = FlightLogger.class.getSimpleName();
	
	public static File getLogDirectory() {
	    // Get the directory for the app's private pictures directory. 
		File logDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), LOG_DIRECTORY);
	    if (!logDir.mkdirs()) {
	        Log.e(LOGGER_TAG, "Log directory not created");
	    }
	    return logDir;
	}
	
	private static File getGPXDirectory() {
	    // Get the directory for the app's private pictures directory. 
	    File gpxDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
	    return gpxDir;
	}
	
	private static boolean isGPXFile(String fileName)
	{
		String ext = fileName.substring((fileName.lastIndexOf(".") + 1), fileName.length());
		return ext.equalsIgnoreCase("gpx");
	}
	
	public static ArrayList<File> listGPXFiles() 
	{
		// get all the files from a directory
		ArrayList<File> files = new ArrayList<File>();
		File gpxDir = FilesystemMgr.getGPXDirectory();
		if(gpxDir.isDirectory())
		{
			
			File[] fList = gpxDir.listFiles();
			for (File file : fList) {
			    if (file.isFile()) {
			    	String fileName = file.getName();
			    	if(isGPXFile(fileName))
			    	{
			    		files.add(file);
			    	}
			    }
			}
		}
		return files;
	}
	
	
	public static boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	public static boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}

}
