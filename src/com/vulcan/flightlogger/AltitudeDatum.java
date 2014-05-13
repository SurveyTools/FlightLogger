package com.vulcan.flightlogger;

public class AltitudeDatum extends FlightDatum {

	protected float mRawAltitude; // raw float value

	static final String INVALID_ALTITUDE_STRING = "--";
	static final String IGNORE_ALTITUDE_STRING = "";

	public AltitudeDatum(boolean ignore) {
		super(ignore);
	}

	protected String calcDisplayAltitudeFromRaw(float rawAltitude, boolean validData) {
		// convert. do units here too
		if (mIgnore) {
			// ignore data
			return IGNORE_ALTITUDE_STRING;
		} else if (validData && !dataIsOld()) {
			// good data -- eval
			// float to int
			int intValue = (int) rawAltitude;

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
		setRawAltitude(0, false, curDataTimestamp());
	}

	public String getAltitudeDisplayText() {
		if (mIgnore)
			return IGNORE_ALTITUDE_STRING;
		else if (!mDataIsValid || dataIsExpired())
			return INVALID_ALTITUDE_STRING;
		else
			return mValueToDisplay;
	}

	public boolean setRawAltitude(float rawAltitudeValue, boolean validData, long timestamp) {

		// snapshot cur data
		final String oldAltitudeDisplayValue = (mValueToDisplay == null) ? new String() : new String(mValueToDisplay);
		final boolean oldAltitudeDataValid = mDataIsValid;
		final boolean oldDataOld = dataIsOld();
		final boolean oldDataExpired = dataIsExpired();
		final int oldStatusColor = getStatusColor();

		// update our data
		mRawAltitude = rawAltitudeValue;
		mDataTimestamp = timestamp;
		mDataIsValid = validData && !dataIsExpired();// invalidate the data if
														// we're expired
		mValueToDisplay = calcDisplayAltitudeFromRaw(rawAltitudeValue, mDataIsValid);

		// see if anything changed - always check the value (since it might
		// change from a number to an ignore value)
		boolean somethingChanged = mValueToDisplay.equals(oldAltitudeDisplayValue); // value

		// superdevo
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
