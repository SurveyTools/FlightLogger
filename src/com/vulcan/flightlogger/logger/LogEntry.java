package com.vulcan.flightlogger.logger;

public class LogEntry {
	double mLat;
	double mLon; 
	float mAlt;
	float mSpeed;
	
	public LogEntry() {
		
	}

	public LogEntry(double currLat, double currLon, float currAlt, float currSpeed)
	{
		this.mLat = currLat;
		this.mLon = currLon;
		this.mAlt = currAlt;
		this.mSpeed = currSpeed;
	}
	
	// copy constructor to keep sampled data atomic
	public LogEntry(LogEntry cloned) {
		this.mLat = cloned.mLat;
		this.mLon = cloned.mLon;
		this.mAlt = cloned.mAlt;
		this.mSpeed = cloned.mSpeed;
	  }

	// TODO - this goes into a log entry formatter
	public String toLogEntry(String timestamp) {
		return String.format("%s - lat:%s lon:%s alt:%s speed:%s", timestamp,
				Double.toString(mLat),
				Double.toString(mLon), 
				Float.toString(mAlt),
				Float.toString(mSpeed));
	}
}
