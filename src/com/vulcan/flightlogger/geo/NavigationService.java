package com.vulcan.flightlogger.geo;

import java.util.ArrayList;
import java.util.List;

import com.vulcan.flightlogger.geo.data.FlightStatus;
import com.vulcan.flightlogger.geo.data.TransectPath;
import com.vulcan.flightlogger.geo.data.TransectStatus;

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
	// this will likely prove too simplistic in the long run
	private final int WAYPOINT_THRESHOLD_METERS = 500; 
	public static final double EARTH_RADIUS_METERS = 6371008.7714; // mean avg for WGS84 projection 
	
	public boolean doNavigation = false;
	
	public List<TransectPath> mTransectList;
	public TransectPath mCurrTransect;
	public Location mCurrentWaypt;
	private final IBinder mBinder = new LocalBinder();
	private final ArrayList<RouteUpdateListener> mListeners
			= new ArrayList<RouteUpdateListener>();

	public class LocalBinder extends Binder {
        public NavigationService getService() {
            return NavigationService.this;
        }
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
    public void registerListener(RouteUpdateListener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(RouteUpdateListener listener) {
        mListeners.remove(listener);
    }

	private void calcFlightStatus(Location currLoc) {
		// TODO Auto-generated method stub
		
	}

	private TransectStatus calcTransectStatus(Location currLoc) {
		// TODO validate before constructing TransectStatus
		double distance = currLoc.distanceTo(mCurrTransect.mEndWaypt);
		double crossTrackErr = calcCrossTrackError(currLoc, mCurrTransect.mStartWaypt, mCurrTransect.mEndWaypt);
		float currBearing = currLoc.bearingTo(mCurrTransect.mEndWaypt);
		float speed = currLoc.getSpeed();
		TransectStatus ts = new TransectStatus(mCurrTransect, distance, crossTrackErr,  currBearing, speed);
		return ts;
	}
	
	private double calcCrossTrackError(Location curr, Location start, Location end)
	{
		double dist = Math.asin(Math.sin(start.distanceTo(curr)/EARTH_RADIUS_METERS) * 
		         Math.sin(start.bearingTo(curr) - curr.bearingTo(end))) * EARTH_RADIUS_METERS;
		
		return dist;
	}
	
	public void setTransectRoute(List<TransectPath> transects)
	{
		mTransectList = transects;
		startNavigation();
	}
	
	// TODO - may want to think about a state machine to handle all of this
	private void startNavigation() {
		mCurrTransect = findNextTransect();
		if(mCurrTransect == null)
		{
			// signal the route is finished
		}
	
	}
	
	private TransectPath findNextTransect()
	{
		TransectPath currPath = null;
		
		for( TransectPath tPath : mTransectList)
		{
			if(tPath.status == FlightStatus.NOT_ACTIVATED || tPath.status == FlightStatus.NAVIGATE_TO)
			{
				// really we need to see where we currently are at, but to start, set it
				// to navigate to, and let the state be changed on the next location update
				tPath.status = FlightStatus.NAVIGATE_TO;
				currPath = tPath;
			}
		}
		return currPath;
	}
	
    private void sendTransectChange(TransectPath newTransect) {
        for (RouteUpdateListener listener : mListeners) {
        	listener.onTransectChange(newTransect);
        }
    }
    
    private void sendTransectUpdate(TransectStatus routeUpdate) {
        for (RouteUpdateListener listener : mListeners) {
        	listener.onRouteUpdate(routeUpdate);
        }
    }
	
	/**
	 * TODO - Will likely need a state machine to run navigation service
	 * based on Location updates
	 */


	/**
	 * Location callbacks
	 */
	@Override
	public void onLocationChanged(Location currLoc) {
		TransectStatus stat = calcTransectStatus(currLoc);
		sendTransectUpdate(stat);
		calcFlightStatus(currLoc);
	}
	
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO GPS is disabled

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO GPS is enabled

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO GPS status has changed

	}

}
