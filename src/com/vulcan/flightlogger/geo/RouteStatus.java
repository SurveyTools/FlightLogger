package com.vulcan.flightlogger.geo;

import android.location.Location;

public class RouteStatus {
	
	public String mRouteName;
	public double mCrossTrackError;
	public double mBearing;
	public Location mCurrWaypt;
	public Location mNextWaypt;
	public double mGroundSpeed;
	// need to convey route state

}
