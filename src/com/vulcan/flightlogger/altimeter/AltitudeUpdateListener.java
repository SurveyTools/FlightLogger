package com.vulcan.flightlogger.altimeter;

public interface AltitudeUpdateListener {
	
	public void onAltitudeUpdate(float altValue);
	
	public void onAltitudeError(String error);
	
	public void onConnectionEnabled();
	
	public void onConnectionDisabled();

}

