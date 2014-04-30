package com.vulcan.flightlogger.geo;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

public class Route {
	public String name;
	public List<Location> wayPoints;
	
	public Route()
	{
		wayPoints = new ArrayList<Location>();
	}
	
	public void addWayPoint(Location location)
	{
		wayPoints.add(location);
	}
	
	public String toString()
	{
		return name;
	}
	
}
