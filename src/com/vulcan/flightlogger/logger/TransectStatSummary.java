package com.vulcan.flightlogger.logger;
// value class for stats
public class TransectStatSummary 
{
	public String mTransectName;
	public float mAvgSpeed;
	public float mAvgGpsAlt;
	public double mAvgLaserAlt;	
	
	private TransectStatSummary()
	{
		
	}
	
	public TransectStatSummary(String name, float avgSpeed, float avgGpsAltitude, float avgLaserAlt)
	{
		mTransectName = name;
		mAvgSpeed = avgSpeed;
		mAvgGpsAlt = avgGpsAltitude;
		mAvgLaserAlt = avgLaserAlt;
	}
	
}
