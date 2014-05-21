package com.vulcan.flightlogger.geo.data;

import android.location.Location;

/**
 * A Transect defines the data necessary to identify a transectpath
 * When 
 * @author jayl
 *
 */
public class Transect {
	public String mName; // e.g. "Transect 1"
	public Location mStartWaypt;
	public Location mEndWaypt;
	public FlightStatus status; 

	// convenience method for UI display when using ArrayAdapters
	public String toString() {
		// e.g. "Transect 1 (T03_S ~ T03_N)"
		return getFullName();
	}
	
	public String getDetailsName() {
		// e.g. "T03_S ~ T03_N"
		return calcDetailsName(mStartWaypt.getProvider(), mEndWaypt.getProvider());
	}
	
	public String getFullName() {
		// e.g. "Transect 1 (T03_S ~ T03_N)"
		return calcFullName(mName, mStartWaypt.getProvider(), mEndWaypt.getProvider());
	}
	
	public boolean matchesByName(String targetName) {
		if (mName != null)
			return mName.matches(targetName);
		return false;
	}

	// "T03_S ~ T03_N"
	static public String calcDetailsName(String waypointName1, String waypointName2) {
		String detailsName = null;
		
		if ((waypointName1 != null) || (waypointName2 != null)) {
			detailsName = new String();
			boolean didFirstWaypoint = false;

			if (!waypointName1.isEmpty()) {
				detailsName += waypointName1;
				didFirstWaypoint = true;
			}
			
			if (!waypointName2.isEmpty()) {
				if (didFirstWaypoint)
					detailsName += " ~ ";
					
				detailsName += waypointName2;
			}
		}
					
		return detailsName;
	}
	
	// e.g. "Transect 1 (T03_S ~ T03_N)"
	static public String calcFullName(String baseName, String transectDetails) {
		if (baseName != null) {
			String fullName = new String(baseName);

			if ((transectDetails != null) && !transectDetails.isEmpty()) {
				return fullName += " (" + transectDetails + ")";
			}
		}
					
		// no dice
		return null;
	}
	
	// e.g. "Transect 1 (T03_S ~ T03_N)"
	static public String calcFullName(String baseName, String waypointName1, String waypointName2) {
		return calcFullName(baseName, calcDetailsName(waypointName1, waypointName2));
	}
	

}
