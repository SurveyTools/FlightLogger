package com.vulcan.flightlogger.logger;

import com.vulcan.flightlogger.geo.data.Transect;

// value class for stats
public class TransectSummary 
{
	public Transect mTransect;
	public String mTransectName;
	public float mAvgSpeed;
	public float mAvgGpsAlt;
	public double mAvgLaserAlt;	
	
	private TransectSummary()
	{
		
	}
	
	public TransectSummary(Transect transect, String name, float avgSpeed, float avgGpsAltitude, float avgLaserAlt)
	{
		mTransect = transect;
		mTransectName = name;
		mAvgSpeed = avgSpeed;
		mAvgGpsAlt = avgGpsAltitude;
		mAvgLaserAlt = avgLaserAlt;
	}
	
	public boolean valid() {
		return mAvgSpeed > 0;
	}
	
}
