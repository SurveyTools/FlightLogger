package com.vulcan.flightlogger.geo.data;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

/**
 * A Route contains all the transect paths we find in a GPX file
 * @author jayl
 */
public class Route {
	public String mName;
	public List<Location> mWayPoints;
	
	public Route()
	{
		mWayPoints = new ArrayList<Location>();
	}
	
	public void addWayPoint(Location location)
	{
		mWayPoints.add(location);
	}
	
	// convenience method for UI display when using ArrayAdapters
	public String toString()
	{
		return mName;
	}
	
}
