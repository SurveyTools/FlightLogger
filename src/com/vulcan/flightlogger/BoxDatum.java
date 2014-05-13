package com.vulcan.flightlogger;

public class BoxDatum extends FlightDatum {

	protected float mRawBox; // raw float value

	static final String INVALID_BOX_STRING = "";
	static final String IGNORE_BOX_STRING = "";

	public BoxDatum(boolean ignore) {
		super(ignore);
	}

	protected String calcDisplayBoxFromRaw(float rawAltitude, boolean validData) {
		// convert. do units here too
		if (mIgnore) {
			// ignore data
			return IGNORE_BOX_STRING;
		} else if (validData) {
			// good data -- eval
			// float to int
			int intValue = (int) rawAltitude;

			// int to string
			return Integer.toString(intValue);
		} else {
			// bad data
			return INVALID_BOX_STRING;
		}
	}

	@Override
	public void reset() {
		super.reset();
		setRawBox(0, false, curDataTimestamp());
	}

	public boolean setRawBox(float rawBoxValue, boolean validData, long timestamp) {
		// snapshot cur data
		final String oldBoxDisplayValue = mValueToDisplay;
		final boolean oldBoxDataValid = mDataIsValid;

		// update our data
		mRawBox = rawBoxValue;
		mDataIsValid = validData;
		mDataTimestamp = timestamp;
		mValueToDisplay = calcDisplayBoxFromRaw(rawBoxValue, validData);

		// see if anything changed
		boolean somethingChanged = false;
		somethingChanged |= mValueToDisplay.equals(oldBoxDisplayValue); // value

		if (!mIgnore)
			somethingChanged |= (mDataIsValid != oldBoxDataValid);

		// update the ui if anything change
		return somethingChanged;
	}
}
