package com.vulcan.flightlogger.geo.data;


public class TransectStatus {
	
	public TransectStatus( 
			TransectPath path, // TODO - this should be an id/guid, if possible
			double distance,
			double crossTrackErr, 
			float bearing, 
			float speed)
	{
		this.mTransect = path;
		this.mCrossTrackError = crossTrackErr;
		this.mDistanceToEnd = distance;
		this.mBearing = bearing;
		this.mGroundSpeed = speed;
	}
	
	public TransectPath mTransect;
	public double mCrossTrackError;
	public double mDistanceToEnd;
	public float mBearing;
	public float mGroundSpeed;

}