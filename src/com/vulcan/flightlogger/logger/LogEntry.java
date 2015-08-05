package com.vulcan.flightlogger.logger;

public class LogEntry {
	double mLat;
	double mLon; 
	float mAlt;
	float mSpeed;
	double mGpsAlt;
	
	public LogEntry() 
	{
		this.mLat = 0;
		this.mLon = 0;
		this.mAlt = 0; // need to know the difference between true 0 and no response.
		this.mSpeed = 0;
		this.mGpsAlt = 0; 
	}

	public LogEntry(double currLat, double currLon, float currAlt, float currSpeed, float gpsAlt)
	{
		this.mLat = currLat;
		this.mLon = currLon;
		this.mAlt = currAlt;
		this.mSpeed = currSpeed;
		this.mGpsAlt = gpsAlt;
	}
	
	public void clearEntry()
	{
		this.mLat = 0;
		this.mLon = 0;
		this.mAlt = 0;
		this.mSpeed = 0;
		this.mGpsAlt = 0;		
	}
	
	// copy constructor to keep sampled data atomic
	public LogEntry(LogEntry cloned) {
		this.mLat = cloned.mLat;
		this.mLon = cloned.mLon;
		this.mAlt = cloned.mAlt;
		this.mSpeed = cloned.mSpeed;
		this.mGpsAlt = cloned.mGpsAlt;
	  }
	
	
	public boolean isValidEntry()
	{
		return (this.mLat != 0.0) && (this.mLon != 0.0);
	}

}
