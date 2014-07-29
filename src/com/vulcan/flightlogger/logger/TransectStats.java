package com.vulcan.flightlogger.logger;

public class TransectStats { 

	public String mTransectName;
	public float mAvgAirspeed;
	public float mAvgAltitude;
	
	private TransectStats()
	{
		// force the object to have a name
	}
	
	public TransectStats(String name)
	{
		mTransectName = name;
	}
	
}
