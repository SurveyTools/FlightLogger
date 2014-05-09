package com.vulcan.flightlogger.geo;

import com.vulcan.flightlogger.geo.data.Transect;
import com.vulcan.flightlogger.geo.data.TransectStatus;

public interface TransectUpdateListener {
	
	public void onRouteUpdate(TransectStatus status);

}
