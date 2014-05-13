package com.vulcan.flightlogger;

public class GPSDatum extends FlightDatum {

	protected float mRawGroundSpeed; // raw float value

	static final String INVALID_GPS_STRING = "--";
	static final String IGNORE_GPS_STRING = "";

	public GPSDatum(boolean ignore) {
		super(ignore);
	}

	protected String calcDisplayGroundSpeedFromRaw(float rawGroundSpeed, boolean validData) {
		// convert. do units here too
		if (mIgnore) {
			// ignore data
			return IGNORE_GPS_STRING;
		} else if (validData) {
			// good data -- eval
			// float to int
			int intValue = (int) rawGroundSpeed;

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
		setRawGPS(0, false, curDataTimestamp());
	}

	public String getGroundSpeedDisplayText() {
		if (mIgnore)
			return IGNORE_GPS_STRING;
		else if (!mDataIsValid || dataIsExpired())
			return INVALID_GPS_STRING;
		else
			return mValueToDisplay;
	}

	public boolean setRawGPS(float rawGroundSpeedValue, boolean validData, long timestamp) {
		// snapshot cur data
		final String oldGroundSpeedDisplayValue = mValueToDisplay;
		final boolean oldGroundSpeedDataValid = mDataIsValid;

		// update our data
		mRawGroundSpeed = rawGroundSpeedValue;
		mDataIsValid = validData;
		mDataTimestamp = timestamp;
		mValueToDisplay = calcDisplayGroundSpeedFromRaw(rawGroundSpeedValue, validData);

		// see if anything changed
		boolean somethingChanged = false;
		somethingChanged |= mValueToDisplay.equals(oldGroundSpeedDisplayValue); // value

		if (!mIgnore)
			somethingChanged |= (mDataIsValid != oldGroundSpeedDataValid);

		// update the ui if anything change
		return somethingChanged;
	}
}
