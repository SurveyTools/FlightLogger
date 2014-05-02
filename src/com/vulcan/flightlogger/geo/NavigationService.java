package com.vulcan.flightlogger.geo;

import java.util.ArrayList;

import com.vulcan.flightlogger.geo.data.TransectPath;
import com.vulcan.flightlogger.geo.data.TransectStatus;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * A work in progress
 * @author jayl
 *
 */

public class NavigationService extends Service implements LocationListener {
	
	public static final double EARTH_RADIUS_METERS = 6371008.7714; // mean avg for WGS84 projection 
	
	// need to get a better number in here to save battery life, once testing
	private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
	
	// need to revisit this guy, to see if we need more accuracy. Currently they
	// sample at 3 seconds
	private static final long MIN_TIME_BETWEEN_UPDATES = 1000 * 3;
	
	private final String LOGGER_TAG = GPSDebugActivity.class.getSimpleName();

	private LocationManager mLocationManager;
	
	public boolean doNavigation = false;
	
	public TransectPath mCurrTransect;
	private final IBinder mBinder = new LocalBinder();
	private final ArrayList<TransectUpdateListener> mListeners
			= new ArrayList<TransectUpdateListener>();

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
	
    public void registerListener(TransectUpdateListener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(TransectUpdateListener listener) {
        mListeners.remove(listener);
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
    
    private void sendTransectUpdate(TransectStatus routeUpdate) {
        for (TransectUpdateListener listener : mListeners) {
        	listener.onRouteUpdate(routeUpdate);
        }
    }
    
	public void startNavigation(TransectPath transect) {
		initGps();
		mCurrTransect = transect;
		doNavigation = true;
	}
	
	public void stopNavigation() {
		doNavigation = false;
	}
	
	private void initGps() {
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// getting GPS status
		boolean isGPSEnabled = mLocationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (isGPSEnabled) {
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES,
					MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
			Log.d(LOGGER_TAG, "GPS Enabled");
		} else {
			Log.d(LOGGER_TAG, "GPS not enabled");
		}
	}

	/**
	 * Location callbacks
	 */
	@Override
	public void onLocationChanged(Location currLoc) {
		if (doNavigation)
		{
			TransectStatus stat = calcTransectStatus(currLoc);
			sendTransectUpdate(stat);
		}
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
