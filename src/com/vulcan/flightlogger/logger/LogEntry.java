package com.vulcan.flightlogger.logger;

import android.location.Location;

public class LogEntry {
	Location loc;
	float currAlt;
	
	public String toString(String timestamp)
	{
		return String.format("%s - lat:%s lon:%s alt:%s speed:%s", 
				timestamp, 
				Double.toString(loc.getLatitude()),
				Double.toString(loc.getLongitude()),
				Float.toString(currAlt),
				Float.toString(loc.getSpeed()));
	}
}
