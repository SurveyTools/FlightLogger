package com.vulcan.flightlogger.geo.data;

import android.location.Location;

/**
 * A Transect defines the data necessary to identify a transectpath
 * When 
 * @author jayl
 *
 */
public class Transect {
	public String mName;
	public Location mStartWaypt;
	public Location mEndWaypt;
	public FlightStatus status; 
}
