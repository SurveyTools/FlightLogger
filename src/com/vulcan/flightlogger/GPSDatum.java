package com.vulcan.flightlogger;

import android.util.Log;

public class GPSDatum extends FlightDatum {

	// TODO change to TransectStatus
	protected float mRawGroundSpeedMetersPerSecond; // raw float value
	protected double mRawCrossTrackErrorMeters;
	protected boolean mCrossTrackDataIsValid;

	static final String INVALID_GPS_STRING = "--";
	static final String IGNORE_GPS_STRING = "";
	static final String DEMO_GROUND_SPEED_STRING = "84"; // DEMO_MODE

	public GPSDatum(boolean ignore, boolean demoMode) {
		super(ignore, demoMode);
	}

	protected String calcDisplayGroundSpeedFromRaw(float rawGroundSpeedMetersPerSecond, boolean validData) {
		// convert. do units here too
		if (mIgnore) {
			// ignore data
			return IGNORE_GPS_STRING;
		} else if (validData) {
			// good data -- eval
			// float to int

			// knots for now.
			float knotsPerHour = metersPerSecondToKnotsPerHour(rawGroundSpeedMetersPerSecond);

			int intValue = (int) knotsPerHour;

			// int to string
			return Integer.toString(intValue);
		} else {
			// bad data
			return INVALID_GPS_STRING;
		}
	}

	@Override
	public void reset() {
		super.reset();
		setRawGroundSpeed(0, false, 0, false, curDataTimestamp());
	}

	public String getGroundSpeedDisplayText() {
		if (mIgnore)
			return IGNORE_GPS_STRING;
		else if (mDemoMode)
			return DEMO_GROUND_SPEED_STRING; // DEMO_MODE
		else if (!mDataIsValid || dataIsExpired())
			return INVALID_GPS_STRING;
		else
			return mValueToDisplay;
	}

	public float getTransectDeltaInFeet() {
		// TODO fix units
		return metersToFeet((float) mRawCrossTrackErrorMeters);
	}

	public boolean setRawGroundSpeed(float rawGroundSpeedMetersPerSecond, boolean validSpeed, double crossTrackErrorMeters, boolean validCrosstrack, long timestamp) {
		// snapshot cur data
		final String oldGroundSpeedDisplayValue = mValueToDisplay;
		final boolean oldGroundSpeedDataValid = mDataIsValid;

		// update our data
		mRawGroundSpeedMetersPerSecond = rawGroundSpeedMetersPerSecond;
		mRawCrossTrackErrorMeters = crossTrackErrorMeters;
		mDataIsValid = validSpeed;
		mCrossTrackDataIsValid = validCrosstrack;
		mDataTimestamp = timestamp;
		mValueToDisplay = calcDisplayGroundSpeedFromRaw(rawGroundSpeedMetersPerSecond, validSpeed);

		// TESTING Log.d("crosstrack", mRawCrossTrackErrorMeters + "meters");

		// see if anything changed
		boolean somethingChanged = false;
		somethingChanged |= mValueToDisplay.equals(oldGroundSpeedDisplayValue); // value

		if (!mIgnore)
			somethingChanged |= (mDataIsValid != oldGroundSpeedDataValid);

		// update the ui if anything change
		return somethingChanged;
	}
}
