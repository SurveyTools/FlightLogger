package com.vulcan.flightlogger.geo;

import android.location.Location;

public interface RouteUpdateListener {
	
	public void onRouteUpdate(Route newRoute);

	// this is just here for testing until we get real routes
	public void onLocationUpdate(Location location);

}
