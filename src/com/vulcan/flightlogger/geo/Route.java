package com.vulcan.flightlogger.geo;

import android.location.Location;

public class Route {	
	
	public String name;
	public RoutePoint[] wayPoints;
	public Location lastWaypoint;
	
	class RoutePoint
	{
		public String name;
		public Location[] wayPoints;		
	}
	
}
