package com.vulcan.flightlogger.geo;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
//import android.os.Handler;
import android.os.IBinder;


public class NavigationService extends Service implements LocationListener {
	
	// if within threshold, the next waypoint is chosen for navigation. 
	// Yes, this may be too simplistic in the long run, but we need to start somewhere
	private final int WAYPOINT_THRESHOLD_METERS = 200; 
	
	public List<Route> mRouteList;
	public Route mCurrentRoute;
	public Location mCurrentWaypt;
	private final IBinder mBinder = new LocalBinder();
	private final ArrayList<RouteUpdateListener> mListeners
			= new ArrayList<RouteUpdateListener>();
	// TODO - Not sure if we need to dispatch on separate thread yet
	// private final Handler mHandler = new Handler();
	
	public class LocalBinder extends Binder {
        public NavigationService getService() {
            return NavigationService.this;
        }
    }
	
    public void registerListener(RouteUpdateListener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(RouteUpdateListener listener) {
        mListeners.remove(listener);
    }

    private void sendRouteUpdate(RouteStatus routeUpdate) {
        for (RouteUpdateListener listener : mListeners) {
        	listener.onRouteUpdate(routeUpdate);
        }
    }

	@Override
	public void onLocationChanged(Location loc) {
		RouteStatus currStatus = calculateRouteStatus(loc);
    	if (currStatus != null)
    	{
    		sendRouteUpdate(currStatus);
    	}

	}

	// determine current waypoint, distance and deviation from
	// next waypoint
	private RouteStatus calculateRouteStatus(Location loc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	/*
	 * Let the user specify/override which route we are flying on. Must match 
	 * a route instance currently loaded by the service. We're assuming we are 
	 * in the same execution space. If that proves not true, we'll need to 
	 * to make a parceable, serializing the data
	 */
	public void setNewRouteWaypoint(Location newWayPt) //should we throw exception?
	{
		
	}
	
	public List<Route> getCurrentRoutes()
	{
		return mRouteList;
	}

}
