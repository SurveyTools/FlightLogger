package com.vulcan.flightlogger.geo;

import java.io.File;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class NavigationService extends Service implements LocationListener {
	
	public List<Route> mRouteList;
	public Route mCurrentRoute;
	private final IBinder mBinder = new LocalBinder();
	
	public class LocalBinder extends Binder {
        public NavigationService getService() {
            return NavigationService.this;
        }
    }

	@Override
	public void onLocationChanged(Location loc) {
		calculateCrossTrack(loc);

	}

	private void calculateCrossTrack(Location loc) {
		// TODO Auto-generated method stub
		
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
	
	public void loadRoutesFromGpx(File gpxFile)
	{
		
		
	}
	
	/*
	 * Let the user specify/override which route we are flying on. Must match 
	 * a route instance currently loaded by the service. We're assuming we are 
	 * in the same execution space. If that proves not true, we'll need to 
	 * to make a parceable, serializing the data
	 */
	public void setNewRoute(Route newRoute) //should we throw exception?
	{
		for (Route r : mRouteList)
		{
			if(r.equals(newRoute))
			{
				mCurrentRoute = newRoute;
			}
		}
	}
	
	/*
	 * We assume the route list is an ordered list - we fly
	 * the first route in the list, and end with the last 
	 * route in the list. 
	 */	
	public void nextRoute()
	{
		int index = mRouteList.indexOf(mCurrentRoute);
		if (index < 0 || index + 1 >= mRouteList.size())
		{
			// signal the routes are complete
		}
		else 
		{
			// set the next route in the list to be the current route
			// TODO - signal we are moving to next 
			mCurrentRoute = mRouteList.get(++index);
		}
	}
	
	public List<Route> getCurrentRoutes()
	{
		return mRouteList;
	}

}
