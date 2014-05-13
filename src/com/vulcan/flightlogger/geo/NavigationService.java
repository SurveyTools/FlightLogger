package com.vulcan.flightlogger.geo;

import java.util.ArrayList;
import java.util.Random;

import com.vulcan.flightlogger.geo.data.Transect;
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
	
	// need to get a better number in here to save battery life, once testing
	private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
	
	// need to revisit this guy, to see if we need more accuracy. Currently we
	// sample at 3 seconds
	private static final long MIN_TIME_BETWEEN_UPDATES = 1000 * 3;
	
	// how many track mock samples to create from a transect path
	private final int NUM_MOCK_TRACKS = 100;
	
	private final String LOGGER_TAG = NavigationService.class.getSimpleName();

	private LocationManager mLocationManager;
	
	public boolean doNavigation = false;
	
	public Transect mCurrTransect;
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
		double dist = Math.asin(Math.sin(start.distanceTo(curr)/GPSUtils.EARTH_RADIUS_METERS) * 
		         Math.sin(start.bearingTo(curr) - curr.bearingTo(end))) * GPSUtils.EARTH_RADIUS_METERS;
		
		return dist;
	}
    
    private void sendTransectUpdate(TransectStatus routeUpdate) {
        for (TransectUpdateListener listener : mListeners) {
        	listener.onRouteUpdate(routeUpdate);
        }
    }
    
    // TODO - Get a 'demo' state in place, and depending on that state, 
    // either init 'real' GPS or mock GPS
	public void startNavigation(Transect transect) {
		initGps(MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES);
		mCurrTransect = transect;
		doNavigation = true;
	}
	
	public void stopNavigation() {
		doNavigation = false;
	}
	
	private void initGps(long millisBetweenUpdate, float minDistanceMoved) {
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// getting GPS status
		boolean isGPSEnabled = mLocationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (isGPSEnabled) {
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, millisBetweenUpdate,
					minDistanceMoved, this);
			Log.d(LOGGER_TAG, "GPS Enabled");
		} else {
			Log.d(LOGGER_TAG, "GPS not enabled");
		}
	}
	
	@SuppressWarnings("unused") // for now...
	private void initMockGps() {
		final long sleepTime = MIN_TIME_BETWEEN_UPDATES;
		
		mCurrTransect = buildMockTransect();
		
		final double latDelta = (mCurrTransect.mEndWaypt.getLatitude() - mCurrTransect.mStartWaypt.getLatitude()) / NUM_MOCK_TRACKS;
		final double lonDelta = (mCurrTransect.mEndWaypt.getLongitude() - mCurrTransect.mStartWaypt.getLongitude()) / NUM_MOCK_TRACKS;
		final Random rand = new Random();
		
		new Thread()
		{
			int progressIndex = 0;		
			
		    public void run() {
		        progressIndex++;
		        Location loc = calcNewMockLocation(progressIndex);
		        try {
		        	onLocationChanged(loc);
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }

			private Location calcNewMockLocation(int index) {
				float latJitter = rand.nextFloat() * (float)latDelta;
				float lonJitter = rand.nextFloat() * (float)lonDelta;
				Location newLoc = new Location("waypoint" + progressIndex);
		        newLoc.setSpeed(41); //meters, 80 knots or so...
		        newLoc.setLatitude(latDelta * progressIndex + latJitter);
		        newLoc.setLongitude(lonDelta * progressIndex + lonJitter);
				return newLoc;
			}
		}.start();
		
	}

	// this is here until we put the menus back in
	private Transect buildMockTransect() {
		Location start = new Location("work");
		start.setLatitude(47.688719);
		start.setLongitude(-122.372639);
		
		Location end = new Location("home");
		start.setLatitude(47.598383);
		start.setLongitude(-122.327537);
		
		Transect transect = new Transect();
		transect.mName = "My Ride Home";
		transect.mStartWaypt = start;
		transect.mEndWaypt = end;
		
		return transect;
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
