package com.vulcan.flightlogger.logger;
// value class for stats
public class TransectSummary 
{
	public String mTransectName;
	public float mAvgSpeed;
	public float mAvgGpsAlt;
	public double mAvgLaserAlt;	
	
	private TransectSummary()
	{
		
	}
	
	public TransectSummary(String name, float avgSpeed, float avgGpsAltitude, float avgLaserAlt)
	{
		mTransectName = name;
		mAvgSpeed = avgSpeed;
		mAvgGpsAlt = avgGpsAltitude;
		mAvgLaserAlt = avgLaserAlt;
	}
	
}
