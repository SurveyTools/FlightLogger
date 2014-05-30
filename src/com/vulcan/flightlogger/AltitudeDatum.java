package com.vulcan.flightlogger;

public class AltitudeDatum extends FlightDatum {

	protected float mRawAltitudeInMeters; // raw float value

	static final String INVALID_ALTITUDE_STRING = "--";
	static final String IGNORE_ALTITUDE_STRING = "";
	static final String DEMO_ALTITUDE_STRING = "312"; // DEMO_MODE

	public AltitudeDatum(boolean ignore, boolean demoMode) {
		super(ignore, demoMode);
	}

	// copy constructor
	public AltitudeDatum(AltitudeDatum srcDatum) {
		super(srcDatum);
		mRawAltitudeInMeters = srcDatum.mRawAltitudeInMeters;
	}

	protected String calcDisplayAltitudeFromRaw(float rawAltitudeInMeters, boolean validData) {
		// convert. do units here too
		if (mIgnore) {
			// ignore data
			return IGNORE_ALTITUDE_STRING;
		} else if (validData && !dataIsOld()) {
			// good data -- eval
			// meters to feet, and float to int
			int intValue = (int) metersToFeet(rawAltitudeInMeters);

			// int to string
			return Integer.toString(intValue);
		} else {
			// bad data
			return INVALID_ALTITUDE_STRING;
		}
	}

	@Override
	public void reset() {
		super.reset();
		setRawAltitudeInMeters(0, false, curDataTimestamp());
	}

	@Override
	public short getStatusColor() {
		if (mDemoMode)
			return FLIGHT_STATUS_GREEN; // DEMO_MODE

		// normal
		return super.getStatusColor();
	}

	public String getAltitudeDisplayText() {
		if (mIgnore)
			return IGNORE_ALTITUDE_STRING;
		else if (mDemoMode)
			return DEMO_ALTITUDE_STRING; // DEMO_MODE
		else if (!mDataIsValid || dataIsExpired())
			return INVALID_ALTITUDE_STRING;
		else
			return mValueToDisplay;
	}

	public float getAltitudeInFeet() {
		return metersToFeet(mRawAltitudeInMeters);
	}

	public boolean setRawAltitudeInMeters(float rawAltitudeInMeters, boolean validData, long timestamp) {

		// snapshot cur data
		final String oldAltitudeDisplayValue = (mValueToDisplay == null) ? new String() : new String(mValueToDisplay);
		final boolean oldAltitudeDataValid = mDataIsValid;
		final boolean oldDataOld = dataIsOld();
		final boolean oldDataExpired = dataIsExpired();
		final int oldStatusColor = getStatusColor();

		// update our data
		mRawAltitudeInMeters = rawAltitudeInMeters;
		mDataTimestamp = timestamp;
		mDataIsValid = validData && !dataIsExpired();// invalidate the data if
														// we're expired
		mValueToDisplay = calcDisplayAltitudeFromRaw(rawAltitudeInMeters, mDataIsValid);

		// see if anything changed - always check the value (since it might
		// change from a number to an ignore value)
		boolean somethingChanged = mValueToDisplay.equals(oldAltitudeDisplayValue); // value

		// MEC_TODO
		if (!mIgnore) {
			somethingChanged |= dataIsOld() != oldDataOld;
			somethingChanged |= dataIsExpired() != oldDataExpired;
			somethingChanged |= getStatusColor() != oldStatusColor;
			somethingChanged |= (mDataIsValid != oldAltitudeDataValid);
		}

		// update the ui if anything change
		return somethingChanged;
	}
}
