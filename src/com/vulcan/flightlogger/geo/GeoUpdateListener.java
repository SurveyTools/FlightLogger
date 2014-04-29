package com.vulcan.flightlogger.geo;

import android.location.Location;

public interface GeoUpdateListener {
	
	public void onLocationUpdate(Location newLoc);
	
	public void onRouteUpdate(Route newRoute);

}
